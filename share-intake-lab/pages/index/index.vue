<template>
  <view class="page">
    <view class="hero card">
      <view class="hero-title">Android 系统分享采集实验室</view>
      <view class="hero-desc">目标只有一个：完整保留系统分享进入应用时携带的原始数据，方便真机逐个平台对比差异。</view>
      <view class="hero-actions">
        <button class="primary-btn" @click="captureIntent('manual-refresh')">重新读取当前分享</button>
        <button class="ghost-btn" @click="copyLatest">复制最新记录 JSON</button>
      </view>
      <view class="tips">
        <text>当前环境：{{ environmentText }}</text>
        <text>记录数：{{ records.length }}</text>
      </view>
    </view>

    <view class="card checklist">
      <view class="section-title">建议测试顺序</view>
      <view class="step" v-for="item in checklist" :key="item">{{ item }}</view>
    </view>

    <view v-if="latestRecord" class="card latest">
      <view class="section-title">最新分享记录</view>
      <view class="meta-row"><text class="label">时间</text><text class="value">{{ latestRecord.createdAt }}</text></view>
      <view class="meta-row"><text class="label">触发方式</text><text class="value">{{ latestRecord.trigger }}</text></view>
      <view class="meta-row"><text class="label">Action</text><text class="value break">{{ latestRecord.snapshot.action || '空' }}</text></view>
      <view class="meta-row"><text class="label">Type</text><text class="value break">{{ latestRecord.snapshot.type || '空' }}</text></view>
      <view class="meta-row"><text class="label">EXTRA_TEXT</text><text class="value break">{{ latestRecord.snapshot.extraText || '空' }}</text></view>
      <view class="meta-row"><text class="label">DataString</text><text class="value break">{{ latestRecord.snapshot.dataString || '空' }}</text></view>
      <view class="meta-row"><text class="label">ClipData</text><text class="value">{{ latestRecord.snapshot.clipData ? '有' : '无' }}</text></view>
      <view class="meta-row"><text class="label">Extras键数</text><text class="value">{{ Object.keys(latestRecord.snapshot.extras || {}).length }}</text></view>

      <view class="section-subtitle">标记来源平台</view>
      <view class="chips">
        <view
          v-for="option in platformOptions"
          :key="option"
          class="chip"
          :class="{ active: latestRecord.platformLabel === option }"
          @click="markPlatform(latestRecord.id, option)"
        >
          {{ option }}
        </view>
      </view>

      <view class="section-subtitle">测试备注</view>
      <textarea
        class="note-area"
        :value="latestRecord.testerNote"
        placeholder="例如：B站分享正文前面会带标题，X 会把链接和文案拼在一起……"
        @blur="saveNote(latestRecord.id, $event.detail.value)"
      />

      <view class="section-subtitle">最新记录 JSON</view>
      <view class="json-box">{{ latestJson }}</view>
    </view>

    <view class="card">
      <view class="section-row">
        <view class="section-title">历史记录</view>
        <view class="danger-link" @click="confirmClear">清空全部</view>
      </view>
      <view v-if="!records.length" class="empty">还没有采集记录。请在手机里从目标 App 使用“系统分享”打开本应用。</view>
      <view v-for="record in records" :key="record.id" class="record-item">
        <view class="record-head">
          <view>
            <view class="record-title">{{ record.platformLabel }} · {{ formatTime(record.createdAt) }}</view>
            <view class="record-subtitle">{{ record.snapshot.action || '无 Action' }} / {{ record.snapshot.type || '无 Type' }}</view>
          </view>
          <view class="record-trigger">{{ record.trigger }}</view>
        </view>
        <view class="record-body">{{ record.snapshot.extraText || record.snapshot.dataString || record.snapshot.rawIntent }}</view>
        <view class="record-actions">
          <view class="mini-link" @click="copyRecord(record)">复制 JSON</view>
          <view class="mini-link" @click="toggleExpanded(record.id)">{{ expandedId === record.id ? '收起' : '展开' }}</view>
        </view>
        <view v-if="expandedId === record.id" class="json-box compact">{{ formatRecord(record) }}</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import {
  captureCurrentIntent,
  clearRecords,
  getPlatformOptions,
  listRecords,
  startIntentPolling,
  updateRecordMeta,
  waitForAppPlusReady
} from '../../common/shareCapture'

const records = ref([])
const expandedId = ref('')
const platformOptions = getPlatformOptions()
const checklist = [
  '1. 先在 HBuilderX 里运行到 Android 自定义调试基座。',
  '2. 在 B站 分享同一条视频到本应用，记录一条样本。',
  '3. 在 YouTube 分享同一条视频到本应用，记录一条样本。',
  '4. 在 X 分享同一条帖子到本应用，记录一条样本。',
  '5. 在 微信公众号 分享同一篇文章到本应用，记录一条样本。',
  '6. 对比四条记录里的 action / type / EXTRA_TEXT / extras / clipData。'
]

let stopPolling = null
let stopPlusReadyWait = null

const latestRecord = computed(() => records.value[0] || null)
const latestJson = computed(() => (latestRecord.value ? formatRecord(latestRecord.value) : ''))
const environmentText = computed(() => {
  // #ifdef APP-PLUS
  return 'App 真机/基座环境'
  // #endif
  // #ifndef APP-PLUS
  return '非 App 环境，无法读取 Android 分享 Intent'
  // #endif
})

function refreshRecords(nextRecords) {
  records.value = nextRecords || listRecords()
}

function captureIntent(trigger) {
  const result = captureCurrentIntent(trigger)
  if (!result.ok) {
    uni.showToast({ title: result.message, icon: 'none', duration: 2500 })
    refreshRecords()
    return
  }
  refreshRecords(result.records)
  uni.showToast({ title: '已保留一条分享记录', icon: 'none' })
}

function markPlatform(id, platformLabel) {
  const nextRecords = updateRecordMeta(id, { platformLabel })
  refreshRecords(nextRecords)
}

function saveNote(id, testerNote) {
  const nextRecords = updateRecordMeta(id, { testerNote })
  refreshRecords(nextRecords)
}

function formatRecord(record) {
  return JSON.stringify(record, null, 2)
}

function copyRecord(record) {
  uni.setClipboardData({ data: formatRecord(record), showToast: true })
}

function copyLatest() {
  if (!latestRecord.value) {
    uni.showToast({ title: '暂无记录可复制', icon: 'none' })
    return
  }
  copyRecord(latestRecord.value)
}

function toggleExpanded(id) {
  expandedId.value = expandedId.value === id ? '' : id
}

function confirmClear() {
  uni.showModal({
    title: '清空记录',
    content: '将删除本地保存的所有分享采集记录，仅影响当前设备。',
    success: (res) => {
      if (!res.confirm) {
        return
      }
      refreshRecords(clearRecords())
      expandedId.value = ''
    }
  })
}

function formatTime(value) {
  return value.replace('T', ' ').replace('.000Z', 'Z')
}

onMounted(() => {
  refreshRecords()
  // #ifdef APP-PLUS
  stopPlusReadyWait = waitForAppPlusReady((ready) => {
    if (!ready) {
      uni.showToast({ title: 'plus 环境初始化超时', icon: 'none' })
      return
    }
    captureIntent('app-mounted')
    stopPolling = startIntentPolling((result) => {
      refreshRecords(result.records)
      uni.showToast({ title: '收到新的系统分享', icon: 'none' })
    })
  })
  // #endif
})

onBeforeUnmount(() => {
  if (stopPlusReadyWait) {
    stopPlusReadyWait()
  }
  if (stopPolling) {
    stopPolling()
  }
})
</script>

<style>
.page {
  padding: 24rpx;
}
.card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 12rpx 32rpx rgba(15, 23, 42, 0.06);
}
.hero-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #111827;
}
.hero-desc {
  margin-top: 12rpx;
  color: #4b5563;
  line-height: 1.6;
}
.hero-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 20rpx;
}
.primary-btn,
.ghost-btn {
  flex: 1;
  font-size: 26rpx;
  border-radius: 16rpx;
}
.primary-btn {
  background: #2563eb;
  color: #ffffff;
}
.ghost-btn {
  background: #e5e7eb;
  color: #111827;
}
.tips {
  margin-top: 16rpx;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  color: #6b7280;
  font-size: 24rpx;
}
.section-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #111827;
}
.section-subtitle {
  margin-top: 20rpx;
  margin-bottom: 12rpx;
  font-size: 26rpx;
  font-weight: 600;
  color: #1f2937;
}
.step {
  margin-top: 12rpx;
  color: #374151;
  line-height: 1.6;
}
.meta-row {
  display: flex;
  gap: 16rpx;
  margin-top: 12rpx;
  align-items: flex-start;
}
.label {
  width: 170rpx;
  color: #6b7280;
  flex-shrink: 0;
}
.value {
  color: #111827;
  flex: 1;
}
.break {
  word-break: break-all;
}
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}
.chip {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: #e5e7eb;
  color: #374151;
}
.chip.active {
  background: #dbeafe;
  color: #1d4ed8;
}
.note-area {
  width: 100%;
  min-height: 180rpx;
  background: #f9fafb;
  border-radius: 16rpx;
  padding: 16rpx;
  box-sizing: border-box;
}
.json-box {
  margin-top: 12rpx;
  background: #111827;
  color: #e5e7eb;
  border-radius: 16rpx;
  padding: 18rpx;
  font-size: 22rpx;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}
.compact {
  margin-top: 14rpx;
}
.section-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.danger-link {
  color: #dc2626;
  font-size: 24rpx;
}
.empty {
  margin-top: 18rpx;
  color: #6b7280;
  line-height: 1.7;
}
.record-item {
  margin-top: 20rpx;
  border-top: 1px solid #e5e7eb;
  padding-top: 20rpx;
}
.record-head {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
}
.record-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #111827;
}
.record-subtitle {
  margin-top: 8rpx;
  color: #6b7280;
  font-size: 24rpx;
}
.record-trigger {
  color: #2563eb;
  font-size: 22rpx;
  flex-shrink: 0;
}
.record-body {
  margin-top: 14rpx;
  color: #374151;
  line-height: 1.6;
  word-break: break-all;
}
.record-actions {
  display: flex;
  gap: 24rpx;
  margin-top: 14rpx;
}
.mini-link {
  color: #2563eb;
  font-size: 24rpx;
}
</style>
