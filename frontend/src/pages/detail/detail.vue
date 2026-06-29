<template>
  <!-- 
    书签备注与详情页面 (Bookmark Detail View)
    职责：展示单条链接的全部信息、在外部浏览器打开、复制链接、编辑备注信息以及修改标签绑定。
    约束：遵守 Harness 配色规范，包含多端条件编译（针对 H5 和 App 原生打包外置浏览器跳转），写满详尽注释。
   -->
  <view class="detail-container">
    
    <!-- 主体内容滚动区域 -->
    <view v-if="bookmark" class="detail-content-box">
        
        <!-- 表单卡片外框 -->
        <view class="detail-card">
          
          <!-- 第一行：平台元数据信息 -->
          <view class="platform-row">
            <view class="platform-badge">
              <image
                v-if="platformMeta.iconUrl"
                class="platform-icon-img"
                :src="platformMeta.iconUrl"
                mode="aspectFit"
              ></image>
              <u-icon
                v-else
                :name="platformMeta.iconName || 'link'"
                size="14"
                color="#f5f5f7"
              ></u-icon>
              <text>{{ platformLabel }}</text>
            </view>
            <text class="saved-time-text">保存时间：{{ formattedSavedTime }}</text>
          </view>
          
          <!-- 第二行：书签大标题 -->
          <view class="title-row">
            <text class="detail-title">{{ bookmark.title }}</text>
          </view>

          <!-- 第三行：网页网址链接与复制交互 -->
          <view class="detail-section">
            <text class="section-label">链接地址 (URL)</text>
            <view class="url-copy-row">
              <text class="url-text">{{ bookmark.originalUrl }}</text>
              <view class="copy-btn-wrap">
                <u-button
                  type="primary"
                  :plain="true"
                  text="复制"
                  size="mini"
                  @click="handleCopyUrl"
                ></u-button>
              </view>
            </view>
          </view>

          <!-- 第四行：记忆备注区块（核心编辑触点） -->
          <view class="detail-section">
            <view class="section-label-row">
              <text class="section-label">记忆备注 (Note)</text>
              <view class="edit-link-btn" @click="openEditNoteModal">
                <u-icon name="edit-pen" size="14" color="#f59e0b" customStyle="margin-right: 4rpx;"></u-icon>
                <text>编辑</text>
              </view>
            </view>
            <!-- 备注文本，若为空显示暂无备注 -->
            <view 
              :class="['note-display-box', { 'is-empty': !bookmark.note }]"
              @click="openEditNoteModal"
            >
              <text class="note-text">{{ bookmark.note || '暂无备注，点击此处即可编辑备注原因。' }}</text>
            </view>
          </view>

          <!-- 第五行：绑定的分类标签组 -->
          <view class="detail-section">
            <view class="section-label-row">
              <text class="section-label">关联分类标签</text>
              <text class="edit-link-btn" @click="openAddTagModal">＋ 新增绑定</text>
            </view>
            <view class="detail-tags-row">
              
              <!-- 渲染绑定的标签，带解绑✕按钮 -->
              <view 
                v-for="tag in bookmark.tags" 
                :key="tag" 
                class="detail-tag-tag"
              >
                <text class="tag-text">#{{ tag }}</text>
                <view class="remove-tag-btn" @click.stop="handleRemoveTag(tag)">✕</view>
              </view>
              
              <!-- 若当前无标签提示 -->
              <view v-if="!bookmark.tags || bookmark.tags.length === 0" class="empty-tags-tip">
                <text>暂无关联分类，这属于“未归档”链接。</text>
              </view>
              
            </view>
          </view>
          
        </view>

        <!-- 底部大操作按钮组（外置浏览器打开、删除） -->
        <view class="detail-actions-group">
          <!-- 动作 1：用外置浏览器查看网址（使用多端条件编译） -->
          <u-button
            type="primary"
            icon="share-square"
            text="在外部浏览器打开链接"
            @click="handleOpenBrowser"
          ></u-button>
          
          <!-- 动作 2：删除书签（危险红） -->
          <view class="delete-btn-wrap">
            <u-button
              type="error"
              :plain="true"
              icon="trash"
              text="删除当前收藏"
              @click="showDeleteConfirm = true"
            ></u-button>
          </view>
        </view>
        
      </view>

    <!-- ====== 交互弹框 0：右上角 ⋯ 更多操作菜单 ====== -->
    <confirm-modal
      :show="showActionModal"
      title="更多操作"
      content="请选择对当前收藏的操作项目："
      confirmText="取消"
      :showCancel="false"
      @confirm="showActionModal = false"
    >
      <view class="actions-list-container">
        <!-- 动作 1：编辑备注 -->
        <view class="menu-action-row" @click="handleActionEditNote">
          <u-icon name="edit-pen" size="18" color="#8888a0" customStyle="margin-right: 16rpx;"></u-icon>
          <text class="action-text">编辑备注</text>
        </view>
        <!-- 动作 2：调整标签 -->
        <view class="menu-action-row" @click="handleActionAdjustTags">
          <u-icon name="tags" size="18" color="#8888a0" customStyle="margin-right: 16rpx;"></u-icon>
          <text class="action-text">调整标签…</text>
        </view>
        <!-- 动作 3：删除当前收藏 -->
        <view class="menu-action-row is-danger" @click="handleActionDelete">
          <u-icon name="trash" size="18" color="#f87171" customStyle="margin-right: 16rpx;"></u-icon>
          <text class="action-text">删除当前收藏</text>
        </view>
      </view>
    </confirm-modal>

    <!-- ====== 交互弹框 1：编辑备注弹窗 ====== -->
    <confirm-modal
      :show="showNoteModal"
      title="修改书签备注"
      content="请在下方输入新的记忆备注原因："
      confirmText="保存修改"
      @confirm="executeSaveNote"
      @cancel="showNoteModal = false"
    >
      <view class="modal-textarea-container">
        <u-textarea
          v-model="editNoteValue"
          placeholder="请输入备注，如：记录该 Skill 在命令行自动化的应用步骤"
          count
          maxlength="1000"
          height="160rpx"
        ></u-textarea>
      </view>
    </confirm-modal>

    <!-- ====== 交互弹框 2：删除链接二次警告弹窗 ====== -->
    <confirm-modal
      :show="showDeleteConfirm"
      title="删除收藏确认"
      content="此操作不可逆！\n\n确认要将该链接从收藏夹中彻底删除吗？"
      confirmText="确认删除"
      :isDanger="true"
      @confirm="executeDelete"
      @cancel="showDeleteConfirm = false"
    ></confirm-modal>

    <!-- ====== 交互弹框 3：调整标签绑定选择抽屉 ====== -->
    <confirm-modal
      :show="showAddTagModal"
      title="调整分类标签"
      content="请勾选需要给当前链接绑定的分类标签："
      confirmText="确定调整"
      @confirm="executeSaveTags"
      @cancel="showAddTagModal = false"
    >
      <!-- 网格展示全局的所有标签池，并呈现当前临时勾选状态 -->
      <view class="modal-tag-selector-pool">
        <view class="tag-pool-grid">
          <view
            v-for="tag in bookmarkStore.tags"
            :key="tag.name"
            :class="['pool-tag-item', { 'is-active': tempSelectedTags.includes(tag.name) }]"
            @click="toggleTempTagSelection(tag.name)"
          >
            <text class="pool-tag-text">
              {{ tempSelectedTags.includes(tag.name) ? '✓ ' : '＋ ' }}{{ tag.name }}
            </text>
          </view>
          
          <view v-if="bookmarkStore.tags.length === 0" class="no-tags-hint">
            <text>系统全局暂无标签，请先去首页新建标签。</text>
          </view>
        </view>
      </view>
    </confirm-modal>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { useBookmarkStore, PLATFORM_MAP } from '@/store/bookmark.js';
import ConfirmModal from '@/components/ConfirmModal.vue';

// 载入书签 Store
const bookmarkStore = useBookmarkStore();

// 当前查看的书签实体 ID 与数据引用
const bookmarkId = ref(null);
const bookmark = computed(() => {
  return bookmarkStore.bookmarks.find(b => b.bookmarkId === bookmarkId.value) || null;
});

// 弹窗状态管理
const showActionModal = ref(false);
const showNoteModal = ref(false);
const showDeleteConfirm = ref(false);
const showAddTagModal = ref(false);

// 输入框及临时状态绑定字段
const editNoteValue = ref('');
const tempSelectedTags = ref([]);

/**
 * 页面加载时接纳传入的书签主键参数，并执行保底容错
 * @param {Object} query 路由传入的 query 参数
 */
onLoad(async (query) => {
  if (!query || !query.id) {
    uni.showToast({
      title: '参数错误',
      icon: 'none'
    });
    setTimeout(() => uni.navigateBack(), 1000);
    return;
  }
  
  bookmarkId.value = parseInt(query.id);

  try {
    await Promise.all([
      bookmarkStore.fetchTags(),
      bookmarkStore.fetchBookmarkDetail(bookmarkId.value)
    ]);
  } catch (err) {
    uni.showToast({
      title: err.message || '该收藏不存在',
      icon: 'none'
    });
    setTimeout(() => uni.navigateBack(), 1000);
  }
});

/**
 * 属性计算：智能翻译并展示平台标签名
 */
const platformMeta = computed(() => {
  if (!bookmark.value) return PLATFORM_MAP.OTHER;
  return PLATFORM_MAP[bookmark.value.platform] || PLATFORM_MAP.OTHER;
});

const platformLabel = computed(() => {
  return platformMeta.value.label;
});

/**
 * 属性计算：格式化时间展示
 */
const formattedSavedTime = computed(() => {
  if (!bookmark.value || !bookmark.value.savedAt) return '';
  return bookmark.value.savedAt.substring(0, 16).replace('T', ' ');
});

/**
 * 复制链接到系统剪贴板
 */
const handleCopyUrl = () => {
  if (!bookmark.value) return;
  uni.setClipboardData({
    data: bookmark.value.originalUrl
  });
};

/**
 * 导航栏右上角 ⋯ 触发入口
 */
const openDetailMoreModal = () => {
  showActionModal.value = true;
};

/**
 * 更多菜单动作 1：触发编辑备注
 */
const handleActionEditNote = () => {
  showActionModal.value = false;
  openEditNoteModal();
};

/**
 * 更多菜单动作 2：触发调整标签
 */
const handleActionAdjustTags = () => {
  showActionModal.value = false;
  openAddTagModal();
};

/**
 * 更多菜单动作 3：触发删除确认
 */
const handleActionDelete = () => {
  showActionModal.value = false;
  showDeleteConfirm.value = true;
};

/**
 * 触发修改备注弹窗
 */
const openEditNoteModal = () => {
  editNoteValue.value = bookmark.value?.note || '';
  showNoteModal.value = true;
};

/**
 * 确认保存修改后的备注
 */
const executeSaveNote = async () => {
  try {
    await bookmarkStore.updateBookmarkNote(bookmarkId.value, editNoteValue.value.trim());
    showNoteModal.value = false;
    uni.showToast({
      title: '备注修改成功',
      icon: 'success'
    });
  } catch (err) {
    uni.showToast({
      title: err.message || '备注修改失败',
      icon: 'none'
    });
  }
};

/**
 * 触发标签调整抽屉
 */
const openAddTagModal = () => {
  tempSelectedTags.value = bookmark.value ? [...bookmark.value.tags] : [];
  showAddTagModal.value = true;
};

/**
 * 调整标签弹窗内的临时勾选或反选
 * @param {string} name 标签名称
 */
const toggleTempTagSelection = (name) => {
  if (tempSelectedTags.value.includes(name)) {
    tempSelectedTags.value = tempSelectedTags.value.filter(t => t !== name);
  } else {
    tempSelectedTags.value.push(name);
  }
};

/**
 * 确认执行全量标签更新调整
 */
const executeSaveTags = async () => {
  try {
    await bookmarkStore.updateBookmarkTags(bookmarkId.value, tempSelectedTags.value);
    showAddTagModal.value = false;
    uni.showToast({
      title: '标签调整成功',
      icon: 'success'
    });
  } catch (err) {
    uni.showToast({
      title: err.message || '调整标签失败',
      icon: 'none'
    });
  }
};

/**
 * 单个标签快速一键解绑操作（原详情卡片上的 ✕ 按钮）
 * @param {string} name 标签名
 */
const handleRemoveTag = async (name) => {
  try {
    await bookmarkStore.removeBookmarkTag(bookmarkId.value, name);
    uni.showToast({
      title: '已解除标签关联',
      icon: 'none'
    });
  } catch (err) {
    uni.showToast({
      title: err.message || '解除标签失败',
      icon: 'none'
    });
  }
};

/**
 * 多端适配跳转：在系统外置浏览器打开目标网址
 */
const handleOpenBrowser = () => {
  if (!bookmark.value) return;
  const targetUrl = bookmark.value.originalUrl;
  
  // #ifdef APP-PLUS
  plus.runtime.openURL(targetUrl, (err) => {
    uni.showToast({
      title: '调用浏览器失败',
      icon: 'none'
    });
  });
  // #endif

  // #ifdef H5
  window.open(targetUrl, '_blank');
  // #endif
};

/**
 * 确认彻底删除本条书签记录，删除后回退上级路由
 */
const executeDelete = async () => {
  try {
    await bookmarkStore.deleteBookmark(bookmarkId.value);
    showDeleteConfirm.value = false;
    
    uni.showToast({
      title: '删除成功',
      icon: 'success'
    });
    
    setTimeout(() => {
      uni.navigateBack();
    }, 800);
  } catch (err) {
    uni.showToast({
      title: err.message || '删除失败',
      icon: 'none'
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
/* 详情页暗黑容器底框 */
.detail-container {
  width: 100vw;
  min-height: 100vh;
  background-color: $uni-bg-color; // 石墨黑
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.detail-content-box {
  padding: 32rpx 36rpx 80rpx;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

/* 详情大卡片 */
.detail-card {
  background-color: $uni-bg-color-grey; // 暗岩灰
  border: 1px solid $uni-border-color;   // 边框色
  border-radius: 20rpx;                  // 10px 步长
  padding: 36rpx 32rpx;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  width: 100%;
  margin-bottom: 48rpx;
  
  /* 平台行 */
  .platform-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    
    .platform-badge {
      display: inline-flex;
      align-items: center;
      gap: 8rpx;
      padding: 6rpx 16rpx;
      background-color: $uni-bg-color-hover; // 中岩灰
      border: 1px solid $uni-border-color;
      border-radius: 8rpx;
      font-size: 22rpx;
      color: $uni-text-color;
      font-weight: bold;

      .platform-icon-img {
        width: 28rpx;
        height: 28rpx;
        border-radius: 6rpx;
      }
    }
    
    .saved-time-text {
      font-size: 20rpx;
      color: $uni-text-color-placeholder;
    }
  }
  
  /* 标题行 */
  .title-row {
    margin-bottom: 40rpx;
    border-bottom: 1px solid $uni-border-color;
    padding-bottom: 32rpx;
    
    .detail-title {
      font-size: 32rpx;
      font-weight: 700;
      color: $uni-text-color; // 亮白
      line-height: 1.5;
      word-break: break-all;
    }
  }
}

/* 段落块定义 */
.detail-section {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 36rpx;
  
  &:last-child {
    margin-bottom: 0; // 去除最底部的间距
  }
  
  .section-label-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .section-label {
    font-size: 24rpx;
    color: $uni-text-color-placeholder;
    font-weight: 500;
    letter-spacing: 1rpx;
  }
  
  .edit-link-btn {
    font-size: 24rpx;
    color: $uni-color-primary; // 琥珀金
    cursor: pointer;
  }
}

/* URL 展示行 */
.url-copy-row {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  background-color: rgba(0, 0, 0, 0.2); // 极暗底纹，区分文本
  padding: 16rpx 20rpx;
  border-radius: 8rpx;
  border: 1px solid $uni-border-color;
  
  .url-text {
    flex: 1;
    font-size: 24rpx;
    color: $uni-text-color-grey;
    word-break: break-all;
    line-height: 1.5;
  }
  
  .copy-btn-wrap {
    flex-shrink: 0;
  }
}

/* 备注显示框 */
.note-display-box {
  background-color: rgba(255, 255, 255, 0.015);
  border-left: 6rpx solid $uni-color-primary; // 琥珀色大左装饰条
  padding: 20rpx 24rpx;
  border-radius: 0 12rpx 12rpx 0;
  cursor: pointer;
  
  .note-text {
    font-size: 26rpx;
    color: $uni-text-color;
    line-height: 1.7;
    word-break: break-all;
  }
  
  /* 备注空状态斜体样式变灰 */
  &.is-empty {
    border-left-color: $uni-border-color;
    
    .note-text {
      color: $uni-text-color-placeholder;
      font-style: italic;
    }
  }
}

/* 关联标签组 */
.detail-tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  width: 100%;
  
  .detail-tag-tag {
    display: inline-flex;
    align-items: center;
    background-color: rgba(245, 158, 11, 0.08); // 主色背景
    border: 1px solid rgba(245, 158, 11, 0.2);
    border-radius: 8rpx;
    padding: 6rpx 14rpx;
    gap: 8rpx;
    
    .tag-text {
      font-size: 22rpx;
      color: $uni-color-primary; // 琥珀金
      font-weight: 500;
    }
    
    .remove-tag-btn {
      font-size: 22rpx;
      color: $uni-text-color-placeholder;
      cursor: pointer;
      padding: 0 4rpx;
      
      &:active {
        color: $uni-color-error; // 删除危险红
      }
    }
  }
  
  .empty-tags-tip {
    font-size: 24rpx;
    color: $uni-text-color-placeholder;
    font-style: italic;
  }
}

/* 底部操作组 */
.detail-actions-group {
  display: flex;
  flex-direction: column;
  gap: 24rpx; // 12px 间距
  width: 100%;
  
  .delete-btn-wrap {
    margin-top: 8rpx;
  }
}

/* 模态框 textarea 包裹 */
.modal-textarea-container {
  margin-top: 24rpx;
  width: 100%;
}

/* 标签追加选择泳池 */
.modal-tag-selector-pool {
  margin-top: 24rpx;
  width: 100%;
  
  .tag-pool-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
    max-height: 360rpx;
    overflow-y: auto;
    padding-bottom: 16rpx;
  }
  
  .pool-tag-item {
    padding: 8rpx 20rpx;
    border: 1px solid $uni-border-color;
    border-radius: 40rpx;
    background-color: $uni-bg-color-hover;
    cursor: pointer;
    transition: all 0.15s ease;
    
    .pool-tag-text {
      font-size: 24rpx;
      color: $uni-text-color-grey;
    }
    
    &.is-active {
      border-color: $uni-color-primary;
      background-color: rgba(245, 158, 11, 0.08);
      
      .pool-tag-text {
        color: $uni-color-primary;
        font-weight: bold;
      }
    }
    
    &:active {
      transform: scale(0.96);
    }
  }
  
  .no-tags-hint {
    padding: 16rpx;
    text-align: center;
    font-size: 24rpx;
    color: $uni-text-color-placeholder;
    width: 100%;
  }
}

/* 更多操作菜单 */
.actions-list-container {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-top: 16rpx;
  width: 100%;
  
  .menu-action-row {
    display: flex;
    align-items: center;
    gap: 20rpx;
    padding: 28rpx 20rpx;
    background-color: rgba(255, 255, 255, 0.02);
    border-radius: 10rpx;
    cursor: pointer;
    transition: all 0.15s ease;
    
    .action-icon {
      font-size: 36rpx;
    }
    
    .action-text {
      font-size: 26rpx;
      color: $uni-text-color;
      font-weight: 500;
    }
    
    &.is-danger {
      background-color: rgba(248, 113, 113, 0.04);
      
      .action-text {
        color: $uni-color-error;
      }
    }
    
    &:active {
      background-color: $uni-bg-color-hover;
      transform: scale(0.98);
    }
  }
}
</style>
