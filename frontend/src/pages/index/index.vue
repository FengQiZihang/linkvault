<template>
  <!-- 
    系统核心首页 (Home View)
    职责：展示登录用户的状态、快捷卡片统计、横向滑动标签导航以及显示最近保存的收藏链接卡片。
    约束：遵守 Harness 空间步长与全局配色，完全复用 TagChip 与 LinkCard 组件。
   -->
  <view class="home-container">
    
    <!-- 顶部导航 Greeting 区域 -->
    <view class="greeting-header">
      <view class="user-info-row" @click="navigateToProfile">
        <!-- 动态显示 SVG 角色头像 -->
        <view class="user-avatar-box">
          <image class="user-avatar-img" :src="userStore.userInfo?.avatarSvg || '/static/avatars/avatar-01.svg'" mode="aspectFit"></image>
        </view>
        <view class="user-text">
          <text class="user-welcome">{{ welcomeTimeText }}，</text>
          <text class="user-name">{{ userStore.userInfo?.nickname || '新用户' }}</text>
        </view>
      </view>
      <!-- 右侧前往个人中心小图标 -->
      <view class="profile-arrow-btn" @click="navigateToProfile">
        <u-icon name="setting" size="20" color="#8888a0"></u-icon>
      </view>
    </view>

    <!-- 搜索条快捷区域（点击直接跳转至搜索页） -->
    <view class="search-section" @click="navigateToSearch">
      <view class="mock-search-bar">
        <u-icon name="search" size="18" color="#55556a" customStyle="margin-right: 12rpx;"></u-icon>
        <text class="search-placeholder">搜索标题或备注…</text>
      </view>
    </view>

    <!-- 主滚动区域 -->
    <view class="home-main-content">
      
      <!-- 快捷分类统计卡片 (Quick cards) -->
      <view class="quick-stats-grid">
        <!-- 统计卡片 1：全部收藏 -->
        <view class="stat-card special" @click="navigateToTagResult('all')">
          <u-icon name="bookmark" size="26" color="#f59e0b" customStyle="margin-bottom: 8rpx;"></u-icon>
          <text class="stat-label">全部收藏</text>
          <text class="stat-value">{{ bookmarkStore.stats.totalBookmarkCount }}</text>
        </view>
        
        <!-- 统计卡片 2：标签数 -->
        <view class="stat-card" @click="navigateToTags">
          <u-icon name="tags" size="26" color="#f59e0b" customStyle="margin-bottom: 8rpx;"></u-icon>
          <text class="stat-label">标签数</text>
          <text class="stat-value">{{ bookmarkStore.stats.tagCount }}</text>
        </view>
        
        <!-- 统计卡片 3：未打标签 -->
        <view class="stat-card" @click="navigateToTagResult('untagged')">
          <u-icon name="email" size="26" color="#f59e0b" customStyle="margin-bottom: 8rpx;"></u-icon>
          <text class="stat-label">未打标签</text>
          <text class="stat-value">{{ bookmarkStore.stats.untaggedBookmarkCount }}</text>
        </view>
      </view>

      <!-- 标签横向滑动选择栏 -->
      <view class="tags-filter-section">
        <view class="section-title-row">
          <text class="section-title">常用标签</text>
          <text class="manage-btn" @click="navigateToTags">管理 →</text>
        </view>
        
        <!-- 横滑滑块 -->
        <scroll-view class="tags-horizontal-scroll" scroll-x="true" show-scrollbar="false">
          <view class="tags-scroll-wrapper">
            <!-- 遍历渲染从 Store 读取的动态标签池 -->
            <tag-chip
              v-for="tag in bookmarkStore.sortedTags"
              :key="tag.name"
              :name="tag.name"
              :pinned="tag.pinned"
              :count="tag.count"
              @click="handleTagClick"
            ></tag-chip>
            
            <!-- 特殊的加号按钮直接跳去新建/管理标签 -->
            <view class="add-tag-btn" @click="navigateToTags">＋ 新建</view>
          </view>
        </scroll-view>
      </view>

      <!-- 最近保存列表 -->
      <view class="links-list-section">
        <view class="section-title-row">
          <text class="section-title">最近保存</text>
          <text class="manage-btn" @click="navigateToTagResult('all')">全部</text>
        </view>
        
        <!-- 动态渲染书签卡片 -->
        <view v-if="bookmarkStore.bookmarks.length > 0" class="cards-list">
          <link-card
            v-for="bookmark in bookmarkStore.bookmarks"
            :key="bookmark.bookmarkId"
            :bookmark="bookmark"
            @click="navigateToDetail"
            @tag-click="handleTagClick"
          ></link-card>
        </view>
        
        <!-- 空状态展示 -->
        <view v-else class="empty-list-box">
          <u-empty
            mode="data"
            icon="http://cdn.uviewui.com/uview/empty/data.png"
            text="暂无任何收藏记录"
            customStyle="margin-top: 48rpx;"
          ></u-empty>
        </view>
      </view>
      
    </view>

    <!-- 悬浮在右下角的手动导入链接浮动按钮 (FAB) -->
    <view class="floating-action-button" @click="navigateToShare">
      <text class="fab-icon">＋</text>
    </view>

    <!-- 底部自定义导航栏 -->
    <CustomTabBar active-tab="home"></CustomTabBar>
  </view>
</template>

<script setup>
import { computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { useUserStore } from '@/store/user.js';
import { useBookmarkStore } from '@/store/bookmark.js';
import TagChip from '@/components/TagChip.vue';
import LinkCard from '@/components/LinkCard.vue';
import CustomTabBar from '@/components/CustomTabBar.vue';

// 载入状态管理
const userStore = useUserStore();
const bookmarkStore = useBookmarkStore();

/**
 * 每次进入首页，执行安全拦截
 */
onShow(() => {
  if (!userStore.isLoggedIn) {
    uni.reLaunch({
      url: '/pages/login/login'
    });
  } else if (!userStore.isProfileSetup) {
    uni.reLaunch({
      url: '/pages/setup/setup'
    });
  } else {
    // 默认加载 5 条作为首页最近保存
    bookmarkStore.fetchHomeData().catch(err => {
      uni.showToast({
        title: err.message || '首页数据加载失败',
        icon: 'none'
      });
    });
  }
});

/**
 * 属性计算：智能问候语
 */
const welcomeTimeText = computed(() => {
  const hour = new Date().getHours();
  if (hour < 6) return '夜猫子好';
  if (hour < 12) return '早上好';
  if (hour < 14) return '中午好';
  if (hour < 18) return '下午好';
  return '晚上好';
});

/**
 * 路由导航：跳转到标签筛选二级页面
 * @param {string} mode 模式：'all' | 'untagged'
 */
const navigateToTagResult = (mode) => {
  uni.navigateTo({
    url: `/pages/tagresult/tagresult?mode=${mode}`
  });
};

/**
 * 处理常用标签或卡片标签点击，跳转到标签筛选结果页面
 * @param {string} tagName 标签名称
 */
const handleTagClick = (tagName) => {
  uni.navigateTo({
    url: `/pages/tagresult/tagresult?tagName=${encodeURIComponent(tagName)}`
  });
};

/**
 * 路由导航：跳转到个人中心
 */
const navigateToProfile = () => {
  uni.navigateTo({
    url: '/pages/profile/profile'
  });
};

/**
 * 路由导航：跳转到检索页
 */
const navigateToSearch = () => {
  uni.navigateTo({
    url: '/pages/search/search'
  });
};

/**
 * 路由导航：跳转至手动添加页
 */
const navigateToShare = () => {
  uni.navigateTo({
    url: '/pages/share/share'
  });
};

/**
 * 路由导航：跳转至标签配置管理页
 */
const navigateToTags = () => {
  uni.navigateTo({
    url: '/pages/tags/tags'
  });
};

/**
 * 路由导航：跳转到书签的备注与操作详情页面
 * @param {number} bookmarkId 书签ID
 */
const navigateToDetail = (bookmarkId) => {
  uni.navigateTo({
    url: `/pages/detail/detail?id=${bookmarkId}`
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
/* 首页容器：暗黑底色 */
.home-container {
  min-height: 100vh;
  background-color: $uni-bg-color; // 石墨黑
  box-sizing: border-box;
  padding-bottom: 160rpx;                  /* 留足底栏的高度留白，防止被 TabBar 遮挡 */
}

/* 顶部问候栏 */
.greeting-header {
  padding: 32rpx 36rpx 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: $uni-bg-color;
  z-index: 10;
  
  .user-info-row {
    display: flex;
    align-items: center;
    gap: 16rpx;
    
    .user-avatar-box {
      width: 56rpx;
      height: 56rpx;
      border-radius: 50%;
      overflow: hidden;
      display: flex;
      align-items: center;
      justify-content: center;
      
      .user-avatar-img {
        width: 100%;
        height: 100%;
        border-radius: 50%;
      }
    }
    
    .user-text {
      display: flex;
      flex-direction: column;
      
      .user-welcome {
        font-size: 20rpx;
        color: $uni-text-color-placeholder;
      }
      
      .user-name {
        font-size: 28rpx;
        color: $uni-text-color;
        font-weight: bold;
      }
    }
  }
  
  .profile-arrow-btn {
    font-size: 32rpx;
    color: $uni-text-color-grey;
    padding: 10rpx;
    
    &:active {
      opacity: 0.7;
    }
  }
}

/* 模拟搜索框区域 */
.search-section {
  padding: 16rpx 36rpx;
  background-color: $uni-bg-color;
  
  .mock-search-bar {
    background-color: $uni-bg-color-grey; // 暗岩灰
    border: 1px solid $uni-border-color;
    border-radius: 12rpx;
    padding: 16rpx 24rpx;
    display: flex;
    align-items: center;
    gap: 12rpx;
    cursor: pointer;
    
    .search-icon {
      font-size: 24rpx;
    }
    
    .search-placeholder {
      font-size: 24rpx;
      color: $uni-text-color-placeholder;
    }
  }
}

.home-main-content {
  padding: 16rpx 36rpx 0;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

/* 快捷卡片三网格 */
.quick-stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
  margin-bottom: 40rpx;
  width: 100%;
  
  .stat-card {
    background-color: $uni-bg-color-grey; // 暗岩灰
    border: 1px solid $uni-border-color;
    border-radius: 16rpx;
    padding: 20rpx 16rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    transition: all 0.15s ease;
    cursor: pointer;
    
    .stat-icon {
      font-size: 36rpx;
      margin-bottom: 8rpx;
    }
    
    .stat-label {
      font-size: 20rpx;
      color: $uni-text-color-grey;
      margin-bottom: 4rpx;
      text-align: center;
    }
    
    .stat-value {
      font-size: 32rpx;
      font-weight: 700;
      color: $uni-text-color;
    }
    
    /* 快捷选项选中态高亮 */
    &.is-active {
      border-color: $uni-color-primary;
      background-color: rgba(245, 158, 11, 0.08);
      
      .stat-value {
        color: $uni-color-primary;
      }
    }
    
    /* 全部收藏卡片微高亮特殊配色，增加视觉节奏 */
    &.special {
      border-color: rgba(245, 158, 11, 0.25);
      background-color: rgba(245, 158, 11, 0.03);
      
      .stat-value {
        color: $uni-color-primary;
      }
      
      &.is-active {
        background-color: rgba(245, 158, 11, 0.08);
        border-color: $uni-color-primary;
      }
    }
    
    &:active {
      transform: scale(0.96);
    }
  }
}

/* 段落统一样式头 */
.section-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  
  .section-title {
    font-size: 22rpx;
    font-weight: 500;
    color: $uni-text-color-grey;
    text-transform: uppercase;
    letter-spacing: 1rpx;
  }
  
  .manage-btn {
    font-size: 22rpx;
    color: $uni-color-primary; // 琥珀金
    cursor: pointer;
  }
}

/* 标签横滑部分 */
.tags-filter-section {
  margin-bottom: 32rpx;
  width: 100%;
}

.tags-horizontal-scroll {
  width: 100%;
  white-space: nowrap; // 必须是不换行
}

.tags-scroll-wrapper {
  display: inline-flex;
  align-items: center;
  gap: 16rpx;
  padding-bottom: 8rpx; // 留出滑块空间
  
  /* 横滑栏最末尾的新建按钮，辅助视觉提示 */
  .add-tag-btn {
    display: inline-flex;
    align-items: center;
    border: 1px dashed $uni-border-color;
    border-radius: 40rpx;
    padding: 8rpx 20rpx;
    font-size: 24rpx;
    color: $uni-text-color-placeholder;
    cursor: pointer;
    
    &:active {
      color: $uni-color-primary;
      border-color: $uni-color-primary;
    }
  }
}

/* 收藏列表 */
.links-list-section {
  width: 100%;
}

.cards-list {
  display: flex;
  flex-direction: column;
}

.empty-list-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

/* 右下角圆形悬浮操作按钮 (FAB) */
.floating-action-button {
  position: absolute;
  right: 36rpx;
  bottom: 160rpx;
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background-color: $uni-color-primary; // 琥珀金
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 32rpx rgba(245, 158, 11, 0.4);
  z-index: 99;
  cursor: pointer;
  transition: all 0.1s ease;
  
  .fab-icon {
    font-size: 48rpx;
    color: #0e0e11; // 深色背景符号，形成明暗强对比
    font-weight: bold;
    margin-bottom: 4rpx;
  }
  
  &:active {
    transform: scale(0.9) rotate(90deg);
  }
}
</style>
