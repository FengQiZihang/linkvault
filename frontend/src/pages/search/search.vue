<template>
  <!-- 
    高级检索搜索页面 (Advanced Search View)
    职责：提供输入关键字、组合多个分类标签进行联合交集 (AND) 筛选，并展示标题高亮的匹配卡片列表。
    约束：遵守 Harness 规范，引入 uview-plus 官方 <u-search> 组件及无状态 LinkCard。
   -->
  <view class="search-container">
    
    <!-- 顶部固定搜索区域 -->
    <view class="search-header-bar">
      <view class="search-input-wrap">
        <u-search
          v-model="keyword"
          placeholder="搜索网页标题、记忆备注等..."
          :showAction="true"
          actionText="取消"
          :animation="true"
          bgColor="#222227"
          @custom="handleCancel"
          @search="triggerSearch"
          @change="triggerSearch"
        ></u-search>
      </view>
    </view>

    <!-- 联合标签筛选标签条 (若无筛选标签，只显示添加标签提示) -->
    <view class="search-tags-filter-bar">
      <view class="tags-row-wrapper">
        <text class="filter-tip-label">联合过滤标签：</text>
        <view class="active-tags-list">
          
          <!-- 渲染已添加的过滤标签块，带 ✕ 移除 -->
          <view 
            v-for="tag in activeSearchTags" 
            :key="tag" 
            class="active-tag-chip"
            @click="removeSearchTag(tag)"
          >
            <text class="active-tag-text">{{ tag }}</text>
            <text class="active-tag-remove-char">✕</text>
          </view>
          
          <!-- 触发追加标签筛选的按钮 -->
          <view class="add-filter-tag-btn" @click="showTagPickerModal = true">
            <text class="plus-icon">＋</text> 标签筛选
          </view>
          
        </view>
      </view>
    </view>

    <!-- 统计信息行 -->
    <view class="search-stats-row">
      <text class="stats-text">
        检索到 <text class="highlight-count">{{ searchedBookmarks.length }}</text> 条书签
        <text v-if="keyword.trim()">（关键词 "{{ keyword }}"）</text>
        <text v-if="activeSearchTags.length > 0">（标签：{{ activeSearchTags.join(' + ') }}）</text>
      </text>
    </view>

    <!-- 书签搜索结果列表 -->
    <view class="results-list-box">
      
      <!-- 循环渲染书签列表，传入关键字进行标题内富文本高亮渲染 -->
      <view v-if="searchedBookmarks.length > 0" class="cards-list">
        <link-card
          v-for="bookmark in searchedBookmarks"
          :key="bookmark.bookmarkId"
          :bookmark="bookmark"
          :highlightKeyword="keyword"
          @click="navigateToDetail"
          @tag-click="addSearchTag"
        ></link-card>
      </view>
      
      <!-- 搜索无结果时呈现空状态 -->
      <view v-else class="empty-results-box">
        <u-empty
          mode="search"
          icon="http://cdn.uviewui.com/uview/empty/search.png"
          text="未检索到匹配的收藏记录"
          customStyle="margin-top: 64rpx;"
        ></u-empty>
      </view>
      
    </view>

    <!-- 挂载底部自定义TabBar -->
    <CustomTabBar active-tab="search"></CustomTabBar>

    <!-- ====== 交互弹框：多选标签联合筛选抽屉 ====== -->
    <confirm-modal
      :show="showTagPickerModal"
      title="选择联合过滤标签 (可多选)"
      content="选择以下已有标签进行组合筛选（必须同时绑定选中的所有标签）："
      confirmText="完成筛选"
      :showCancel="false"
      @confirm="showTagPickerModal = false"
    >
      <view class="modal-tag-selector-pool">
        <view class="tag-pool-grid">
          <!-- 
            遍历所有标签池。
            若该标签已经被选定过滤，呈现 active 高亮选中样式。
           -->
          <view
            v-for="tag in bookmarkStore.tags"
            :key="tag.name"
            :class="['pool-tag-item', { 'is-active': activeSearchTags.includes(tag.name) }]"
            @click="toggleSearchTag(tag.name)"
          >
            <text class="pool-tag-text">
              {{ activeSearchTags.includes(tag.name) ? '✓ ' : '＋ ' }}{{ tag.name }}
            </text>
          </view>
          
          <view v-if="bookmarkStore.tags.length === 0" class="no-tags-hint">
            <text>系统全局暂无标签，无法进行筛选。</text>
          </view>
        </view>
      </view>
    </confirm-modal>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { useBookmarkStore } from '@/store/bookmark.js';
import LinkCard from '@/components/LinkCard.vue';
import ConfirmModal from '@/components/ConfirmModal.vue';
import CustomTabBar from '@/components/CustomTabBar.vue';

// 载入书签 Store
const bookmarkStore = useBookmarkStore();

// 搜索栏响应式关键字绑定
const keyword = ref('');
const searchedBookmarks = ref([]);

// 记录已选择用来做联合过滤的标签列表 (AND并集筛选)
const activeSearchTags = ref([]);

// 标签选择器弹窗状态
const showTagPickerModal = ref(false);

onShow(async () => {
  try {
    await bookmarkStore.fetchTags();
    await triggerSearch();
  } catch (err) {
    uni.showToast({
      title: err.message || '搜索数据加载失败',
      icon: 'none'
    });
  }
});

/**
 * 触发搜索动作
 */
const triggerSearch = async () => {
  try {
    searchedBookmarks.value = await bookmarkStore.searchBookmarks({
      keyword: keyword.value,
      tagNames: activeSearchTags.value,
      page: 1,
      pageSize: 20
    });
  } catch (err) {
    uni.showToast({
      title: err.message || '搜索失败',
      icon: 'none'
    });
  }
};

const refreshSearch = () => {
  triggerSearch();
};

/**
 * 将标签加入联合检索条件中 (防重复)
 * @param {string} tagName 标签名称
 */
const addSearchTag = (tagName) => {
  if (!activeSearchTags.value.includes(tagName)) {
    activeSearchTags.value.push(tagName);
    refreshSearch();
  }
};

/**
 * 从联合检索中解绑特定标签条件
 * @param {string} tagName 标签名称
 */
const removeSearchTag = (tagName) => {
  activeSearchTags.value = activeSearchTags.value.filter(t => t !== tagName);
  refreshSearch();
};

/**
 * 在弹出框中点击标签切换选中状态 (多选)
 * @param {string} name 标签名称
 */
const toggleSearchTag = (name) => {
  if (activeSearchTags.value.includes(name)) {
    removeSearchTag(name);
  } else {
    addSearchTag(name);
  }
};

/**
 * 退出当前搜索页，返回首页或返回上一页
 */
const handleCancel = () => {
  keyword.value = '';
  triggerSearch();
  const pages = getCurrentPages();
  if (pages.length > 1) {
    uni.navigateBack();
  } else {
    uni.reLaunch({
      url: '/pages/index/index'
    });
  }
};

/**
 * 路由导航：查看书签详情
 * @param {number} id 书签ID
 */
const navigateToDetail = (id) => {
  uni.navigateTo({
    url: `/pages/detail/detail?id=${id}`
  });
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
/* 搜索页面暗黑大画布 */
.search-container {
  width: 100vw;
  min-height: 100vh;
  background-color: $uni-bg-color; // 石墨黑
  display: flex;
  flex-direction: column;
  padding-bottom: 160rpx;                  /* 底部留足 160rpx 留白，防止被 TabBar 遮挡 */
  box-sizing: border-box;
}

/* 顶部搜索框固定条 */
.search-header-bar {
  padding: 24rpx 36rpx;
  background-color: $uni-bg-color;
  border-bottom: 1px solid $uni-border-color;
  z-index: 10;
  
  .search-input-wrap {
    width: 100%;
  }
}

/* 联合检索标签横向面板 */
.search-tags-filter-bar {
  padding: 16rpx 36rpx;
  border-bottom: 1px solid $uni-border-color;
  background-color: $uni-bg-color;
  
  .tags-row-wrapper {
    display: flex;
    flex-direction: column;
    gap: 12rpx;
    width: 100%;
  }
  
  .filter-tip-label {
    font-size: 20rpx;
    color: $uni-text-color-placeholder;
  }
  
  .active-tags-list {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 16rpx;
    
    /* 已添加筛选条件小标签 */
    .active-tag-chip {
      display: inline-flex;
      align-items: center;
      background-color: rgba(245, 158, 11, 0.15); // 琥珀色
      border: 1px solid rgba(245, 158, 11, 0.3);
      border-radius: 40rpx;
      padding: 6rpx 16rpx;
      gap: 8rpx;
      cursor: pointer;
      
      .active-tag-text {
        font-size: 22rpx;
        color: $uni-color-primary;
        font-weight: 500;
      }
      
      .active-tag-remove-char {
        font-size: 20rpx;
        color: $uni-text-color-placeholder;
      }
      
      &:active {
        opacity: 0.7;
      }
    }
    
    /* 触发追加筛选按钮 */
    .add-filter-tag-btn {
      display: inline-flex;
      align-items: center;
      border: 1px dashed $uni-border-color;
      border-radius: 40rpx;
      padding: 6rpx 16rpx;
      font-size: 22rpx;
      color: $uni-text-color-placeholder;
      cursor: pointer;
      transition: all 0.15s ease;
      
      .plus-icon {
        font-size: 22rpx;
      }
      
      &:active {
        color: $uni-color-primary;
        border-color: $uni-color-primary;
      }
    }
  }
}

/* 统计条 */
.search-stats-row {
  padding: 16rpx 36rpx;
  background-color: rgba(0, 0, 0, 0.1);
  
  .stats-text {
    font-size: 22rpx;
    color: $uni-text-color-placeholder;
    line-height: 1.5;
    
    .highlight-count {
      color: $uni-color-primary;
      font-weight: bold;
      margin: 0 4rpx;
    }
  }
}



.results-list-box {
  padding: 24rpx 36rpx 60rpx;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.cards-list {
  display: flex;
  flex-direction: column;
}

.empty-results-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

/* 标签模态选择器泳池 */
.modal-tag-selector-pool {
  margin-top: 24rpx;
  width: 100%;
  
  .tag-pool-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
    max-height: 400rpx;
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
    
    /* 选中高亮状态 */
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
</style>
