<template>
  <!-- 
    标签筛选与分类结果页面 (Tag Result View)
    职责：承接从首页统计卡片、常用标签、全部按钮等跳入的筛选，支持多标签 AND 筛选与未打标签筛选。
    约束：遵守 Harness 配色规范与 8px 步长，逻辑代码包含联调搜索接口的完整上拉加载与下拉刷新。
   -->
  <view class="tag-result-container">
    
    <!-- 顶部导航行 -->
    <view class="result-header">
      <view class="back-btn" @click="handleBack">
        <u-icon name="arrow-left" size="20" color="#e8e8f0"></u-icon>
      </view>
      <text class="header-title">标签筛选</text>
      <view class="header-placeholder"></view>
    </view>

    <!-- 顶部状态与过滤操作栏 -->
    <view class="filter-panel-card">
      <view class="title-row">
        <view class="title-box">
          <u-icon :name="modeIcon" size="22" color="#f59e0b" customStyle="margin-right: 12rpx;"></u-icon>
          <text class="title-text">{{ filterTitle }}</text>
        </view>
      </view>
      <text class="count-text">共 {{ totalCount }} 个收藏</text>
      
      <!-- 联合筛选提示语及清除按钮 -->
      <view class="filter-toolbar">
        <text class="toolbar-hint">可选择多个标签，结果同时满足所有已选标签</text>
        <text 
          :class="['clear-btn', { 'is-disabled': !hasFilters }]"
          @click="clearFilters"
        >清除</text>
      </view>
      
      <!-- 横向滑动候选标签池 -->
      <scroll-view class="candidates-scroll" scroll-x="true" show-scrollbar="false">
        <view class="candidates-wrapper">
          <!-- “未归档”作为一个快捷的排除性过滤项，与具体标签筛选互斥 -->
          <tag-chip
            name="未打标签"
            :selected="isUntaggedMode"
            :showPinIcon="false"
            @click="toggleUntaggedFilter"
          ></tag-chip>
          
          <!-- 全局可用标签池列表 -->
          <tag-chip
            v-for="tag in bookmarkStore.tags"
            :key="tag.name"
            :name="tag.name"
            :pinned="tag.pinned"
            :selected="activeTags.includes(tag.name)"
            @click="toggleTagFilter"
          ></tag-chip>
        </view>
      </scroll-view>
    </view>

    <!-- 书签结果滚动列表区域 -->
    <view class="results-list-box">
      <!-- 循环渲染书签卡片 -->
      <view v-if="bookmarkStore.bookmarks.length > 0" class="cards-list">
        <link-card
          v-for="bookmark in bookmarkStore.bookmarks"
          :key="bookmark.bookmarkId"
          :bookmark="bookmark"
          @click="navigateToDetail"
          @tag-click="toggleTagFilter"
        ></link-card>
        
        <!-- 加载状态提示 -->
        <view class="loading-more-bar">
          <text v-if="hasMore" class="loading-text">正在加载更多...</text>
          <text v-else class="loading-text">没有更多收藏记录了</text>
        </view>
      </view>
      
      <!-- 空状态展示 -->
      <view v-else class="empty-results-box">
        <u-empty
          mode="data"
          icon="http://cdn.uviewui.com/uview/empty/data.png"
          text="暂无匹配当前过滤条件的收藏"
          customStyle="margin-top: 64rpx;"
        ></u-empty>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad, onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import { useBookmarkStore } from '@/store/bookmark.js';
import TagChip from '@/components/TagChip.vue';
import LinkCard from '@/components/LinkCard.vue';

// 载入书签 Store
const bookmarkStore = useBookmarkStore();

// 筛选模式：'all'（全部） | 'tag'（特定标签筛选） | 'untagged'（未归类）
const filterMode = ref('all');

// 当前选定的联合 AND 过滤标签集合
const activeTags = ref([]);

// 分页控制
const page = ref(1);
const totalCount = computed(() => bookmarkStore.total);
const hasMore = computed(() => bookmarkStore.bookmarks.length < totalCount.value);

// 下拉刷新触发标志
const refresherTriggered = ref(false);

/**
 * 属性计算：判断是否包含任何有效的过滤条件
 */
const hasFilters = computed(() => {
  return filterMode.value === 'untagged' || activeTags.value.length > 0;
});

/**
 * 属性计算：判断是否在未打标签模式
 */
const isUntaggedMode = computed(() => {
  return filterMode.value === 'untagged';
});

/**
 * 属性计算：标题对应的 矢量图标名称
 */
const modeIcon = computed(() => {
  if (filterMode.value === 'untagged') return 'email';
  if (activeTags.value.length > 0) return 'grid';
  return 'bookmark';
});

/**
 * 属性计算：自适应生成的页面大标题文本
 */
const filterTitle = computed(() => {
  if (filterMode.value === 'untagged') return '未归类收藏';
  if (activeTags.value.length > 0) {
    return activeTags.value.map(name => `#${name}`).join(' + ');
  }
  return '全部收藏';
});

/**
 * 路由加载时的初始参数劫持与校验
 * @param {Object} query 路由传入参数
 */
onLoad(async (query) => {
  await bookmarkStore.fetchTags();
  
  if (query) {
    if (query.mode === 'all') {
      filterMode.value = 'all';
    } else if (query.mode === 'untagged') {
      filterMode.value = 'untagged';
    } else if (query.tagName) {
      filterMode.value = 'tag';
      activeTags.value = [decodeURIComponent(query.tagName)];
    }
  }
  
  fetchFilteredData();
});

/**
 * 页面展示时刷新统计和标签池，保持计数最新
 */
onShow(() => {
  bookmarkStore.fetchTags();
});

/**
 * 执行数据加载
 * @param {boolean} isRefresh 是否清空之前的列表
 */
const fetchFilteredData = async (isRefresh = true) => {
  if (isRefresh) {
    page.value = 1;
  }
  
  try {
    await bookmarkStore.searchBookmarks({
      keyword: '',
      tagNames: filterMode.value === 'tag' ? activeTags.value : [],
      untagged: filterMode.value === 'untagged',
      page: page.value,
      pageSize: 20
    });
  } catch (err) {
    uni.showToast({
      title: err.message || '加载失败',
      icon: 'none'
    });
  }
};

/**
 * 交互事件：切换未打标签过滤状态
 */
const toggleUntaggedFilter = () => {
  if (filterMode.value === 'untagged') {
    filterMode.value = 'all';
  } else {
    filterMode.value = 'untagged';
    activeTags.value = []; // 清空其余标签条件
  }
  fetchFilteredData();
};

/**
 * 交互事件：点击可选标签池，进行联合交集过滤或解除
 * @param {string} name 标签名
 */
const toggleTagFilter = (name) => {
  filterMode.value = 'tag';
  if (activeTags.value.includes(name)) {
    activeTags.value = activeTags.value.filter(t => t !== name);
    if (activeTags.value.length === 0) {
      filterMode.value = 'all';
    }
  } else {
    activeTags.value.push(name);
  }
  fetchFilteredData();
};

/**
 * 交互事件：清除所有过滤条件，恢复全部展示
 */
const clearFilters = () => {
  if (!hasFilters.value) return;
  filterMode.value = 'all';
  activeTags.value = [];
  fetchFilteredData();
};

/**
 * 下拉刷新逻辑
 */
const onRefresh = async () => {
  refresherTriggered.value = true;
  try {
    await fetchFilteredData(true);
  } finally {
    refresherTriggered.value = false;
  }
};

/**
 * 上拉触底加载更多逻辑
 */
const onLoadMore = async () => {
  if (!hasMore.value) return;
  page.value++;
  try {
    // 联合检索上拉分页
    await bookmarkStore.searchBookmarks({
      keyword: '',
      tagNames: filterMode.value === 'tag' ? activeTags.value : [],
      untagged: filterMode.value === 'untagged',
      page: page.value,
      pageSize: 20
    });
  } catch (err) {
    uni.showToast({
      title: err.message || '加载更多失败',
      icon: 'none'
    });
  }
};

/**
 * 路由导航：跳转详情页
 * @param {number} id 书签ID
 */
const navigateToDetail = (id) => {
  uni.navigateTo({
    url: `/pages/detail/detail?id=${id}`
  });
};

/**
 * 导航返回
 */
const handleBack = () => {
  // 检查路由栈深，避免直接 navigateBack 引起异常
  const pages = getCurrentPages();
  if (pages.length > 1) {
    uni.navigateBack();
  } else {
    uni.reLaunch({
      url: '/pages/index/index'
    });
  }
};

// 原生下拉刷新
onPullDownRefresh(async () => {
  try {
    await fetchFilteredData(true);
  } finally {
    uni.stopPullDownRefresh();
  }
});

// 原生上拉触底加载
onReachBottom(() => {
  onLoadMore();
});
</script>

<script>
export default {
  options: {
    styleIsolation: 'shared'
  }
}
</script>

<style lang="scss">
.tag-result-container {
  width: 100vw;
  min-height: 100vh;
  background-color: $uni-bg-color; // 石墨黑
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

/* 顶部导航 */
.result-header {
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

/* 状态与过滤区卡片 */
.filter-panel-card {
  background-color: $uni-bg-color-grey; // 暗岩灰
  border-bottom: 1px solid $uni-border-color;
  padding: 32rpx 36rpx;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  
  .title-row {
    margin-bottom: 8rpx;
    
    .title-text {
      font-size: 32rpx;
      font-weight: 700;
      color: $uni-text-color;
      display: flex;
      align-items: center;
      gap: 12rpx;
    }
  }
  
  .count-text {
    font-size: 22rpx;
    color: $uni-text-color-placeholder;
    margin-bottom: 24rpx;
  }
  
  .filter-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;
    
    .toolbar-hint {
      font-size: 20rpx;
      color: $uni-text-color-placeholder;
      flex: 1;
    }
    
    .clear-btn {
      font-size: 22rpx;
      color: $uni-color-primary; // 琥珀金
      cursor: pointer;
      font-weight: bold;
      padding: 4rpx 12rpx;
      
      &.is-disabled {
        color: $uni-text-color-disable;
        cursor: default;
      }
    }
  }
}

/* 标签横滑 */
.candidates-scroll {
  width: 100%;
  white-space: nowrap;
}

.candidates-wrapper {
  display: inline-flex;
  gap: 16rpx;
  padding-bottom: 8rpx;
}



.results-list-box {
  padding: 16rpx 36rpx 60rpx;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.cards-list {
  display: flex;
  flex-direction: column;
}

.loading-more-bar {
  padding: 32rpx 0;
  text-align: center;
  
  .loading-text {
    font-size: 22rpx;
    color: $uni-text-color-placeholder;
  }
}

.empty-results-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
</style>
