<template>
  <!-- 
    手动导入书签页面 (Share / Import View)
    职责：提供输入链接 URL、网页标题、添加备注及选择绑定分类标签的表单，保存到本地状态库中。
    约束：遵守 Harness 步长规范与配色规范，采用组件库的原生输入控件、自定义的 ConfirmModal 和 TagChip 组件。
   -->
  <view class="share-container">
    
    <!-- 头部自定义导航栏（带返回） -->
    <view class="share-header">
      <view class="back-btn" @click="handleBack">←</view>
      <text class="header-title">手动导入</text>
      <view class="header-placeholder"></view>
    </view>

    <!-- 主表单滚动视图 -->
    <view class="share-content-box">
        
        <!-- ==================== 阶段 1：URL 粘贴输入 ==================== -->
        <view v-if="stage === 1" class="form-card">
          <view class="form-item">
            <text class="form-label">粘贴链接</text>
            <u-textarea
              v-model="url"
              placeholder="粘贴 Bilibili、YouTube、X、微信公众号等链接"
              border="surround"
              height="140rpx"
            ></u-textarea>
          </view>
          
          <view class="actions-group-row">
            <view class="btn-box">
              <u-button
                type="info"
                :plain="true"
                text="清空"
                customStyle="border-color: #2e2e35; color: #8888a0;"
                @click="handleClearClick"
              ></u-button>
            </view>
            <view class="btn-box">
              <u-button
                type="primary"
                text="保存链接"
                :disabled="!isValidUrl || loading"
                :loading="loading"
                @click="handleSaveLink"
              ></u-button>
            </view>
          </view>
        </view>

        <!-- ==================== 阶段 2：已保存展示与整理 ==================== -->
        <view v-if="stage === 2" class="form-card">
          <!-- 绿色已保存提示 -->
          <view class="saved-badge-row">
            <view class="saved-badge">
              <u-icon name="checkmark-circle-fill" size="16" color="#4ade80" customStyle="margin-right: 8rpx;"></u-icon>
              <text>链接已保存</text>
            </view>
          </view>

          <!-- 网页元信息解析卡片 -->
          <view v-if="linkMeta" class="link-preview-card">
            <view class="platform-row">
                    <image
                v-if="getPlatformMeta(linkMeta.platform).iconUrl"
                class="platform-icon-img"
                :src="getPlatformMeta(linkMeta.platform).iconUrl"
                mode="aspectFit"
              ></image>
              <u-icon
                v-else
                :name="getPlatformMeta(linkMeta.platform).iconName || 'link'"
                size="16"
                color="#8888a0"
              ></u-icon>
              <text class="platform-name">
                {{ getPlatformMeta(linkMeta.platform).label }}
                <text v-if="linkMeta.publisher"> · {{ linkMeta.publisher }}</text>
              </text>
            </view>
            <text class="preview-title">{{ linkMeta.title || '未解析标题' }}</text>
            <text v-if="formattedPublishTime" class="preview-time">发布时间：{{ formattedPublishTime }}</text>
            <text class="preview-url">{{ linkMeta.originalUrl }}</text>
          </view>

          <!-- 备注输入 -->
          <view class="form-item">
            <text class="form-label">备注 <text class="accent-label">（为什么保存它？）</text></text>
            <u-textarea
              v-model="note"
              placeholder="随手写下当时的判断，例如：后面照着配环境用、这个观点适合做选题参考…"
              border="surround"
              count
              maxlength="1000"
              height="160rpx"
            ></u-textarea>
          </view>

          <!-- 分类标签多选 -->
          <view class="form-item">
            <text class="form-label">标签</text>
            <view class="tag-selector-area">
              <tag-chip
                v-for="tag in bookmarkStore.tags"
                :key="tag.name"
                :name="tag.name"
                :pinned="tag.pinned"
                :selected="selectedTags.includes(tag.name)"
                @click="toggleTagSelection"
              ></tag-chip>

              <!-- 新增标签的虚线按钮 -->
              <view class="add-tag-trigger-btn" @click="showAddTagModal = true">
                <text class="plus-icon">＋</text> 新建标签
              </view>
            </view>
          </view>

          <!-- 最终保存动作 -->
          <view class="complete-btn-wrap">
            <u-button
              type="primary"
              text="完成保存"
              @click="handleSaveComplete"
            ></u-button>
          </view>
        </view>
        
      </view>

    <!-- 二次确认清空弹框 -->
    <confirm-modal
      :show="showClearConfirm"
      title="清空表单确认"
      content="确认要清空当前已输入的网页链接吗？该操作不可撤销。"
      confirmText="确认清空"
      :isDanger="true"
      @confirm="executeClear"
      @cancel="showClearConfirm = false"
    ></confirm-modal>

    <!-- 新建标签输入模态框 -->
    <confirm-modal
      :show="showAddTagModal"
      title="新增分类标签"
      content="请输入新标签的名称（不能与已有标签重名）："
      confirmText="创建"
      @confirm="executeCreateTag"
      @cancel="closeAddTagModal"
    >
      <view class="modal-input-container">
        <u-input
          v-model="newTagName"
          placeholder="输入新标签名称，最长10字"
          border="surround"
          maxlength="10"
          clearable
        ></u-input>
      </view>
    </confirm-modal>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { useBookmarkStore, PLATFORM_MAP } from '@/store/bookmark.js';
import TagChip from '@/components/TagChip.vue';
import ConfirmModal from '@/components/ConfirmModal.vue';

// 载入书签 Store
const bookmarkStore = useBookmarkStore();

// 流程步骤状态：1 粘贴 URL，2 填写备注标签
const stage = ref(1);

// 表单响应式字段
const url = ref('');
const note = ref('');
const selectedTags = ref([]);

// 接口解析返回的元数据
const bookmarkId = ref(null);
const linkMeta = ref(null);

// 各种弹窗及状态控制
const loading = ref(false);
const showClearConfirm = ref(false);
const showAddTagModal = ref(false);
const newTagName = ref('');

onShow(() => {
  bookmarkStore.fetchTags().catch(err => {
    uni.showToast({
      title: err.message || '标签加载失败',
      icon: 'none'
    });
  });
});

/**
 * 属性计算：快捷校验 URL 格式是否输入合规
 */
const isValidUrl = computed(() => {
  return /(https?:\/\/[^\s]+)/i.test(url.value);
});

/**
 * 属性计算：格式化时间展示
 */
const formattedPublishTime = computed(() => {
  if (!linkMeta.value || !linkMeta.value.publishedAt) return '';
  return linkMeta.value.publishedAt.substring(0, 16).replace('T', ' ');
});

const getPlatformMeta = (platform) => {
  return PLATFORM_MAP[platform] || PLATFORM_MAP.OTHER;
};

/**
 * 阶段 1：点击“保存链接”动作
 */
const handleSaveLink = async () => {
  const matched = url.value.match(/(https?:\/\/[^\s]+)/i);
  if (!matched) return;

  const extractedUrl = matched[0];
  url.value = extractedUrl; // 自动修正为纯 URL 并在界面回显

  loading.value = true;
  try {
    const res = await bookmarkStore.importBookmark(extractedUrl);
    bookmarkId.value = res.bookmarkId;
    linkMeta.value = res.link;

    if (res.duplicated) {
      uni.showToast({
        title: '该链接已存在',
        icon: 'none'
      });
      // 级联拉取已存在收藏详情来回显备注与标签
      const detail = await bookmarkStore.fetchBookmarkDetail(res.bookmarkId);
      note.value = detail.note || '';
      selectedTags.value = detail.tags || [];
    } else {
      note.value = '';
      selectedTags.value = [];
    }

    stage.value = 2; // 顺利移步阶段 2
  } catch (err) {
    uni.showToast({
      title: err.message || '保存失败，请检查网址格式',
      icon: 'none'
    });
  } finally {
    loading.value = false;
  }
};

/**
 * 切换标签的选中/解绑状态 (多选)
 * @param {string} name 标签名
 */
const toggleTagSelection = (name) => {
  if (selectedTags.value.includes(name)) {
    selectedTags.value = selectedTags.value.filter(t => t !== name);
  } else {
    selectedTags.value.push(name);
  }
};

/**
 * 点击“清空内容”触发二次防错弹窗
 */
const handleClearClick = () => {
  if (url.value.trim()) {
    showClearConfirm.value = true;
  } else {
    executeClear();
  }
};

/**
 * 真实执行清空表单
 */
const executeClear = () => {
  url.value = '';
  showClearConfirm.value = false;
};

/**
 * 创建新标签动作
 */
const executeCreateTag = async () => {
  const name = newTagName.value.trim();
  if (!name) {
    uni.showToast({
      title: '标签名不能为空',
      icon: 'none'
    });
    return;
  }

  // 检查是否重名
  const isDuplicate = bookmarkStore.tags.some(t => t.name === name);
  if (isDuplicate) {
    uni.showToast({
      title: '该标签已存在',
      icon: 'none'
    });
    return;
  }

  try {
    const newTag = await bookmarkStore.createTag(name);
    selectedTags.value.push(newTag.name); // 自动勾选
    closeAddTagModal();
    uni.showToast({
      title: '标签创建成功',
      icon: 'success'
    });
  } catch (err) {
    uni.showToast({
      title: err.message || '创建标签失败',
      icon: 'none'
    });
  }
};

/**
 * 关闭新增标签弹窗并重置输入
 */
const closeAddTagModal = () => {
  showAddTagModal.value = false;
  newTagName.value = '';
};

/**
 * 阶段 2：执行完成保存动作并返回首页
 */
const handleSaveComplete = async () => {
  if (!bookmarkId.value) return;

  try {
    await bookmarkStore.organizeBookmark(bookmarkId.value, note.value.trim(), selectedTags.value);
    uni.showToast({
      title: '已完成保存',
      icon: 'success'
    });

    setTimeout(() => {
      handleBack();
    }, 800);
  } catch (err) {
    uni.showToast({
      title: err.message || '整理提交失败',
      icon: 'none'
    });
  }
};

/**
 * 路由返回处理
 */
const handleBack = () => {
  const pages = getCurrentPages();
  if (pages.length > 1) {
    uni.navigateBack();
  } else {
    uni.reLaunch({
      url: '/pages/index/index'
    });
  }
};
</script>

<script>
export default {
  options: {
    styleIsolation: 'shared'
  }
}
</script>

<style lang="scss">
.share-container {
  width: 100vw;
  min-height: 100vh;
  background-color: $uni-bg-color; // 石墨黑
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

/* 头部 */
.share-header {
  height: 88rpx;
  background-color: $uni-bg-color;
  border-bottom: 1px solid $uni-border-color;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  box-sizing: border-box;
  z-index: 10;
  
  .back-btn {
    font-size: 40rpx;
    color: $uni-text-color-grey;
    cursor: pointer;
    width: 60rpx;
  }
  
  .header-title {
    font-size: 30rpx;
    font-weight: 700;
    color: $uni-text-color;
  }
  
  .header-placeholder {
    width: 60rpx;
  }
}



.share-content-box {
  padding: 32rpx 36rpx 80rpx;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

/* 表单卡片 */
.form-card {
  background-color: $uni-bg-color-grey; // 暗岩灰
  border: 1px solid $uni-border-color;   // 边框色
  border-radius: 20rpx;
  padding: 36rpx 32rpx;
  display: flex;
  flex-direction: column;
  gap: 32rpx;
  box-sizing: border-box;
  width: 100%;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  
  .form-label {
    font-size: 24rpx;
    color: $uni-text-color-grey;
    font-weight: 500;
    
    .accent-label {
      color: $uni-color-primary; // 琥珀金提示
    }
  }
}

/* 已保存徽章 */
.saved-badge-row {
  display: flex;
  margin-bottom: 8rpx;
  
  .saved-badge {
    display: inline-flex;
    align-items: center;
    gap: 8rpx;
    background: rgba(74, 222, 128, 0.15); // 绿色徽章
    border: 1px solid rgba(74, 222, 128, 0.3);
    color: #4ade80;
    padding: 8rpx 20rpx;
    border-radius: 40rpx;
    font-size: 22rpx;
    font-weight: bold;
  }
}

/* 链接网页预览卡片 */
.link-preview-card {
  background-color: rgba(0, 0, 0, 0.2); // 纯黑透明打底
  border: 1px solid $uni-border-color;
  border-radius: 16rpx;
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  
  .platform-row {
    display: flex;
    align-items: center;
    gap: 12rpx;
    
    .platform-icon-img {
      width: 32rpx;
      height: 32rpx;
      border-radius: 7rpx;
    }
    
    .platform-name {
      font-size: 20rpx;
      color: $uni-text-color-placeholder;
      font-weight: bold;
    }
  }
  
  .preview-title {
    font-size: 26rpx;
    font-weight: 700;
    color: $uni-text-color;
    line-height: 1.5;
  }
  
  .preview-time {
    font-size: 20rpx;
    color: $uni-text-color-placeholder;
  }
  
  .preview-url {
    font-size: 20rpx;
    color: $uni-text-color-placeholder;
    word-break: break-all;
    line-height: 1.4;
  }
}

/* 标签面板 */
.tag-selector-area {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  width: 100%;
  
  .add-tag-trigger-btn {
    display: inline-flex;
    align-items: center;
    border: 1px dashed $uni-border-color;
    border-radius: 40rpx;
    padding: 8rpx 20rpx;
    font-size: 24rpx;
    color: $uni-text-color-placeholder;
    cursor: pointer;
    background-color: transparent;
    transition: all 0.15s ease;
    
    .plus-icon {
      font-size: 24rpx;
      margin-right: 4rpx;
    }
    
    &:active {
      color: $uni-color-primary;
      border-color: $uni-color-primary;
      background-color: rgba(245, 158, 11, 0.02);
    }
  }
}

/* 底部操作 */
.actions-group-row {
  display: flex;
  gap: 24rpx;
  width: 100%;
  
  .btn-box {
    flex: 1;
  }
}

.complete-btn-wrap {
  margin-top: 16rpx;
  width: 100%;
}

.modal-input-container {
  margin-top: 24rpx;
  width: 100%;
}
</style>
