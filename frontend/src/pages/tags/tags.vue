<template>
  <!-- 
    全局标签管理页面 (Tags Management View)
    职责：提供新建标签、置顶控制、标签重命名以及高级标签合并/迁移和安全删除的集中操作界面。
    约束：遵守 Harness 配色规范与 8px 步长，逻辑代码包含详尽级联动作注释。
   -->
  <view class="tags-container">
    
    <!-- 顶部直接快速创建新标签 -->
    <view class="create-tag-bar">
      <view class="create-input-wrap">
        <u-input
          v-model="newTagName"
          placeholder="创建新标签，如：技术栈"
          border="surround"
          maxlength="10"
          clearable
        ></u-input>
      </view>
      <view class="create-btn-wrap">
        <u-button
          type="primary"
          text="创建"
          :disabled="!newTagName.trim()"
          @click="handleCreateTag"
        ></u-button>
      </view>
    </view>

    <!-- 标签列表展示区 -->
    <view class="tags-list-content">
      
      <!-- 分区 1：置顶标签区块 (若无置顶，则不渲染本区块) -->
      <view v-if="pinnedTags.length > 0" class="tag-group-section">
        <view class="group-header">
          <u-icon name="pushpin-fill" size="14" color="#fbbf24" customStyle="margin-right: 8rpx;"></u-icon>
          <text class="group-title">置顶分类标签</text>
        </view>
        <view class="group-list">
          <!-- 渲染置顶标签项 -->
          <view 
            v-for="tag in pinnedTags" 
            :key="tag.name" 
            class="tag-list-item"
            @click="openActionSheet(tag)"
          >
            <u-icon name="pushpin-fill" size="16" color="#fbbf24" customStyle="margin-right: 12rpx;"></u-icon>
            <text class="item-name">{{ tag.name }}</text>
            <text class="item-count">{{ tag.count }} 个收藏</text>
            <u-icon name="setting" size="16" color="#55556a"></u-icon>
          </view>
        </view>
      </view>

      <!-- 分区 2：常规标签区块 -->
      <view class="tag-group-section">
        <view class="group-header">
          <u-icon name="tags-fill" size="14" color="#8888a0" customStyle="margin-right: 8rpx;"></u-icon>
          <text class="group-title">常规分类标签</text>
        </view>
        <view v-if="regularTags.length > 0" class="group-list">
          <!-- 渲染普通标签项 -->
          <view 
            v-for="tag in regularTags" 
            :key="tag.name" 
            class="tag-list-item"
            @click="openActionSheet(tag)"
          >
            <u-icon name="tags-fill" size="16" color="#8888a0" customStyle="margin-right: 12rpx;"></u-icon>
            <text class="item-name">{{ tag.name }}</text>
            <text class="item-count">{{ tag.count }} 个收藏</text>
            <u-icon name="setting" size="16" color="#55556a"></u-icon>
          </view>
        </view>
        <!-- 零标签时友好提示 -->
        <view v-else class="empty-placeholder">
          <text class="placeholder-text">暂无任何常规标签，可在上方快速创建。</text>
        </view>
      </view>
      
    </view>

    <!-- ====== 交互弹框 1：标签操作菜单抽屉 (ConfirmModal 伪装底座菜单) ====== -->
    <confirm-modal
      :show="showActionModal"
      :title="`标签管理: “${selectedTag?.name}”`"
      :content="`已绑定 ${selectedTag?.count} 个收藏。请选择对当前标签的批量操作项目：`"
      confirmText="取消"
      :showCancel="false"
      @confirm="showActionModal = false"
    >
      <view class="actions-list-container">
        <!-- 动作 1：去首页筛选过滤 -->
        <view class="menu-action-row" @click="handleActionFilter">
          <u-icon name="search" size="18" color="#8888a0" customStyle="margin-right: 16rpx;"></u-icon>
          <text class="action-text">在首页筛选此标签</text>
        </view>
        <!-- 动作 2：置顶/解置顶 -->
        <view class="menu-action-row" @click="handleActionTogglePin">
          <u-icon :name="selectedTag?.pinned ? 'pushpin-fill' : 'tags-fill'" size="18" color="#8888a0" customStyle="margin-right: 16rpx;"></u-icon>
          <text class="action-text">{{ selectedTag?.pinned ? '取消置顶' : '置顶标签' }}</text>
        </view>
        <!-- 动作 3：重命名 -->
        <view class="menu-action-row" @click="handleActionRenameClick">
          <u-icon name="edit-pen" size="18" color="#8888a0" customStyle="margin-right: 16rpx;"></u-icon>
          <text class="action-text">重命名标签</text>
        </view>
        <!-- 动作 4：数据迁移合并（仅在 count > 0 时有意义） -->
        <view v-if="selectedTag?.count > 0" class="menu-action-row" @click="handleActionMergeClick">
          <u-icon name="attach" size="18" color="#8888a0" customStyle="margin-right: 16rpx;"></u-icon>
          <text class="action-text">将本标签合并到其他分类</text>
        </view>
        <!-- 动作 5：删除标签（危险红，count > 0 时会触发警告不能删除） -->
        <view class="menu-action-row is-danger" @click="handleActionDeleteClick">
          <u-icon name="trash" size="18" color="#f87171" customStyle="margin-right: 16rpx;"></u-icon>
          <text class="action-text">删除标签</text>
        </view>
      </view>
    </confirm-modal>

    <!-- ====== 交互弹框 2：重命名二次确认框 ====== -->
    <confirm-modal
      :show="showRenameModal"
      title="重命名分类标签"
      :content="`请输入 “${selectedTag?.name}” 的新名称：`"
      confirmText="确定重命名"
      @confirm="executeRename"
      @cancel="showRenameModal = false"
    >
      <view class="modal-input-wrap">
        <u-input
          v-model="renameValue"
          placeholder="请输入新的名字，防重名"
          border="surround"
          maxlength="10"
          clearable
        ></u-input>
      </view>
    </confirm-modal>

    <!-- ====== 交互弹框 3：安全删除校验弹窗 ====== -->
    <confirm-modal
      :show="showDeleteConfirm"
      title="安全删除标签"
      :content="deleteConfirmMessage"
      :confirmText="selectedTag?.count > 0 ? '确认' : '确认彻底删除'"
      :isDanger="selectedTag?.count === 0"
      :showCancel="selectedTag?.count === 0"
      @confirm="executeDelete"
      @cancel="showDeleteConfirm = false"
    ></confirm-modal>

    <!-- ====== 交互弹框 4：标签迁移合并目的地选择抽屉 ====== -->
    <confirm-modal
      :show="showMergeModal"
      title="标签合并目的地"
      :content="`请选择迁入的目的地标签，合并后“${selectedTag?.name}”下的所有书签将全数转移绑定，并且“${selectedTag?.name}”会被自动删除：`"
      confirmText="取消"
      :showCancel="false"
      @confirm="showMergeModal = false"
    >
      <view class="merge-destinations-scroll-wrap" @touchmove.stop>
        <scroll-view class="merge-scroll" scroll-y="true">
          <view class="destinations-list">
            <!-- 遍历渲染除了当前选定标签外的其他所有可用标签 -->
            <view 
              v-for="target in mergeCandidates" 
              :key="target.name" 
              class="dest-item"
              @click="handleMergeSelect(target.name)"
            >
              <view class="dest-name-box">
                <u-icon 
                  :name="target.pinned ? 'pushpin-fill' : 'tags-fill'" 
                  size="16" 
                  :color="target.pinned ? '#fbbf24' : '#8888a0'"
                ></u-icon>
                <text class="dest-name">{{ target.name }}</text>
              </view>
              <text class="dest-count">当前包含 {{ target.count }} 个收藏</text>
            </view>
          </view>
        </scroll-view>
      </view>
    </confirm-modal>

    <!-- ====== 交互弹框 5：最终合并合并确认弹窗 ====== -->
    <confirm-modal
      :show="showMergeConfirm"
      title="确认合并标签"
      :content="`此操作不可逆！\n\n确认将“${selectedTag?.name}”合并到“${mergeTargetName}”？\n合并后，原“${selectedTag?.name}”将自动删除，其关联的 ${selectedTag?.count} 条收藏数据将全部划转至“${mergeTargetName}”下。`"
      confirmText="确认合并"
      :isDanger="true"
      @confirm="executeMerge"
      @cancel="showMergeConfirm = false"
    ></confirm-modal>

    <!-- 底部自定义导航栏 -->
    <CustomTabBar active-tab="tags"></CustomTabBar>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { useBookmarkStore } from '@/store/bookmark.js';
import ConfirmModal from '@/components/ConfirmModal.vue';
import CustomTabBar from '@/components/CustomTabBar.vue';

// 载入书签 Store
const bookmarkStore = useBookmarkStore();

// 创建新标签响应式字段
const newTagName = ref('');

// 选中的标签实体对象，用于各模态弹窗共享上下文
const selectedTag = ref(null);

// 控制各个弹窗的状态
const showActionModal = ref(false);
const showRenameModal = ref(false);
const showDeleteConfirm = ref(false);
const showMergeModal = ref(false);
const showMergeConfirm = ref(false);

// 各种弹窗输入绑定值
const renameValue = ref('');
const mergeTargetName = ref('');

onShow(() => {
  bookmarkStore.fetchTags().catch(err => {
    uni.showToast({
      title: err.message || '标签加载失败',
      icon: 'none'
    });
  });
});

/**
 * 属性计算：过滤获取所有的置顶标签
 */
const pinnedTags = computed(() => {
  return bookmarkStore.sortedTags.filter(t => t.pinned);
});

/**
 * 属性计算：过滤获取常规非置顶标签
 */
const regularTags = computed(() => {
  return bookmarkStore.sortedTags.filter(t => !t.pinned);
});

/**
 * 属性计算：获取除了当前选中的标签之外的合并候选标签池
 */
const mergeCandidates = computed(() => {
  if (!selectedTag.value) return [];
  return bookmarkStore.sortedTags.filter(t => t.name !== selectedTag.value.name);
});

/**
 * 属性计算：动态计算删除确认的正文提示语（Harness 级联保护）
 */
const deleteConfirmMessage = computed(() => {
  if (!selectedTag.value) return '';
  if (selectedTag.value.count > 0) {
    return `删除被拒！\n\n当前标签 “${selectedTag.value.name}” 目前正与 ${selectedTag.value.count} 个收藏相关联。\n\n根据系统安全约束，禁止直接删除有关联的书签标签！请返回先执行“将本标签合并到其他分类”进行数据转移。`;
  }
  return `确认删除空标签 “${selectedTag.value.name}” 吗？删除后将无法恢复。`;
});

/**
 * 点击执行创建新全局标签动作
 */
const handleCreateTag = async () => {
  const name = newTagName.value.trim();
  if (!name) return;
  
  try {
    await bookmarkStore.createTag(name);
    newTagName.value = '';
    uni.showToast({
      title: '标签创建成功',
      icon: 'success'
    });
  } catch (err) {
    uni.showToast({
      title: err.message || '创建失败',
      icon: 'none'
    });
  }
};

/**
 * 点击列表项打开操作卡片抽屉
 * @param {Object} tag 标签实体
 */
const openActionSheet = (tag) => {
  selectedTag.value = tag;
  showActionModal.value = true;
};

const handleActionFilter = () => {
  showActionModal.value = false;
  uni.navigateTo({
    url: `/pages/tagresult/tagresult?tagName=${encodeURIComponent(selectedTag.value.name)}`
  });
};

/**
 * 动作 2：执行标签置顶切换
 */
const handleActionTogglePin = async () => {
  showActionModal.value = false;
  try {
    await bookmarkStore.toggleTagPin(selectedTag.value.name);
    uni.showToast({
      title: '状态已更新',
      icon: 'success'
    });
  } catch (err) {
    uni.showToast({
      title: err.message || '状态更新失败',
      icon: 'none'
    });
  }
};

/**
 * 动作 3：点击触发重命名弹窗
 */
const handleActionRenameClick = () => {
  showActionModal.value = false;
  renameValue.value = selectedTag.value.name;
  showRenameModal.value = true;
};

/**
 * 动作 3 确认执行：重命名
 */
const executeRename = async () => {
  const cleanNew = renameValue.value.trim();
  if (!cleanNew) {
    uni.showToast({
      title: '名字不能为空',
      icon: 'none'
    });
    return;
  }
  
  try {
    await bookmarkStore.renameTag(selectedTag.value.name, cleanNew);
    showRenameModal.value = false;
    uni.showToast({
      title: '修改成功',
      icon: 'success'
    });
  } catch (err) {
    uni.showToast({
      title: err.message || '重命名失败',
      icon: 'none'
    });
  }
};

/**
 * 动作 4：点击打开数据合并目的地抽屉
 */
const handleActionMergeClick = () => {
  showActionModal.value = false;
  mergeTargetName.value = '';
  showMergeModal.value = true;
};

/**
 * 合并目的地选择，开启最终确认
 * @param {string} targetName 目的标签名字
 */
const handleMergeSelect = (targetName) => {
  showMergeModal.value = false;
  mergeTargetName.value = targetName;
  showMergeConfirm.value = true;
};

/**
 * 动作 4 确认执行：执行标签合并级联删除
 */
const executeMerge = async () => {
  if (!selectedTag.value || !mergeTargetName.value) return;
  
  try {
    await bookmarkStore.mergeTag(selectedTag.value.name, mergeTargetName.value);
    showMergeConfirm.value = false;
    uni.showToast({
      title: '合并完成',
      icon: 'success'
    });
  } catch (err) {
    uni.showToast({
      title: err.message || '合并失败',
      icon: 'none'
    });
  }
};

/**
 * 动作 5：点击触发删除确认
 */
const handleActionDeleteClick = () => {
  showActionModal.value = false;
  showDeleteConfirm.value = true;
};

/**
 * 动作 5 确认执行：空标签彻底删除
 */
const executeDelete = async () => {
  if (selectedTag.value.count > 0) {
    // 再次级联检验防错
    showDeleteConfirm.value = false;
    return;
  }
  
  try {
    await bookmarkStore.deleteTag(selectedTag.value.name);
    showDeleteConfirm.value = false;
    uni.showToast({
      title: '删除成功',
      icon: 'success'
    });
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
/* 标签管理页暗黑容器 */
.tags-container {
  min-height: 100vh;
  background-color: $uni-bg-color; // 石墨黑
  box-sizing: border-box;
  padding-bottom: 160rpx;                  /* 留足底栏的高度留白，防止被 TabBar 遮挡 */
}

/* 顶部快速新建栏 */
.create-tag-bar {
  padding: 24rpx 36rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
  border-bottom: 1px solid $uni-border-color;
  background-color: $uni-bg-color;
  z-index: 10;
  
  .create-input-wrap {
    flex: 1;
  }
  
  .create-btn-wrap {
    width: 140rpx;
  }
}

.tags-list-content {
  padding: 24rpx 36rpx 0;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

/* 标签分组 */
.tag-group-section {
  margin-bottom: 48rpx;
  
  .group-header {
    margin-bottom: 16rpx;
    
    .group-title {
      font-size: 24rpx;
      color: $uni-text-color-placeholder;
      font-weight: 500;
      letter-spacing: 1rpx;
    }
  }
  
  .group-list {
    background-color: $uni-bg-color-grey; // 暗岩灰
    border: 1px solid $uni-border-color;
    border-radius: 16rpx;
    overflow: hidden;                     // 防止内嵌子元素溢出圆角
  }
  
  /* 零标签提示 */
  .empty-placeholder {
    padding: 32rpx;
    text-align: center;
    border: 1px dashed $uni-border-color;
    border-radius: 16rpx;
    
    .placeholder-text {
      font-size: 24rpx;
      color: $uni-text-color-placeholder;
    }
  }
}

/* 标签列表行 */
.tag-list-item {
  display: flex;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1px solid $uni-border-color;
  gap: 16rpx;
  cursor: pointer;
  
  &:last-child {
    border-bottom: none; // 去除最末尾的下边框
  }
  
  .item-pin-icon {
    font-size: 32rpx;
  }
  
  .item-name {
    flex: 1;
    font-size: 28rpx;
    color: $uni-text-color;
    font-weight: 500;
  }
  
  .item-count {
    font-size: 24rpx;
    color: $uni-text-color-grey;
  }
  
  .item-arrow {
    font-size: 24rpx;
    color: $uni-text-color-placeholder;
  }
  
  &:active {
    background-color: $uni-bg-color-hover; // 选中态变中岩灰
  }
}

/* 交互抽屉动作列表容器 */
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
    
    /* 危险动作选项 */
    &.is-danger {
      background-color: rgba(248, 113, 113, 0.04);
      
      .action-text {
        color: $uni-color-error; // 红色
      }
    }
    
    &:active {
      background-color: $uni-bg-color-hover;
      transform: scale(0.98);
    }
  }
}

/* 重命名弹窗 input 外部包裹 */
.modal-input-wrap {
  margin-top: 24rpx;
  width: 100%;
}

/* 合并目的地列表容器 */
.merge-destinations-scroll-wrap {
  margin-top: 24rpx;
  height: 400rpx;                        // 显式指定固定高度，解决 scroll-y 失效
  width: 100%;
  box-sizing: border-box;
}

.merge-scroll {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  touch-action: pan-y;
  -webkit-overflow-scrolling: touch;
}

.destinations-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  padding-bottom: 24rpx;
  
  .dest-item {
    background-color: $uni-bg-color-hover;
    border: 1px solid $uni-border-color;
    border-radius: 12rpx;
    padding: 24rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
    cursor: pointer;
    
    .dest-name-box {
      display: flex;
      align-items: center;
      gap: 12rpx;
    }
    
    .dest-name {
      font-size: 26rpx;
      color: $uni-text-color;
      font-weight: 500;
    }
    
    .dest-count {
      font-size: 22rpx;
      color: $uni-text-color-placeholder;
    }
    
    &:active {
      border-color: $uni-color-primary;
      background-color: rgba(245, 158, 11, 0.04);
    }
  }
}
</style>
