<template>
  <!-- 
    系统自定义底部导航栏 (CustomTabBar)
    职责：供首页、搜索页、标签页和个人中心页进行底部 Tab 切换，支持完整的移动端 APK 适配。
    设计要求：纯展示受控组件，避免原生 TabBar 配置的复杂资产和兼容性，使用 Emoji 渲染。
   -->
  <view class="custom-tabbar-wrap">
    
    <!-- Tab 项 1：首页 -->
    <view 
      class="tab-item"
      :class="{ 'is-active': activeTab === 'home' }"
      @click="handleTabSwitch('home')"
    >
      <u-icon 
        :name="activeTab === 'home' ? 'home-fill' : 'home'" 
        size="22" 
        :color="activeTab === 'home' ? '#f59e0b' : '#8888a0'"
        customStyle="margin-bottom: 4rpx;"
      ></u-icon>
      <text class="tab-text">首页</text>
    </view>
    
    <!-- Tab 项 2：搜索 -->
    <view 
      class="tab-item"
      :class="{ 'is-active': activeTab === 'search' }"
      @click="handleTabSwitch('search')"
    >
      <u-icon 
        name="search" 
        size="22" 
        :color="activeTab === 'search' ? '#f59e0b' : '#8888a0'"
        customStyle="margin-bottom: 4rpx;"
      ></u-icon>
      <text class="tab-text">搜索</text>
    </view>
    
    <!-- Tab 项 3：标签 -->
    <view 
      class="tab-item"
      :class="{ 'is-active': activeTab === 'tags' }"
      @click="handleTabSwitch('tags')"
    >
      <u-icon 
        :name="activeTab === 'tags' ? 'tags-fill' : 'tags'" 
        size="22" 
        :color="activeTab === 'tags' ? '#f59e0b' : '#8888a0'"
        customStyle="margin-bottom: 4rpx;"
      ></u-icon>
      <text class="tab-text">标签</text>
    </view>
    
    <!-- Tab 项 4：我的 -->
    <view 
      class="tab-item"
      :class="{ 'is-active': activeTab === 'profile' }"
      @click="handleTabSwitch('profile')"
    >
      <u-icon 
        :name="activeTab === 'profile' ? 'account-fill' : 'account'" 
        size="22" 
        :color="activeTab === 'profile' ? '#f59e0b' : '#8888a0'"
        customStyle="margin-bottom: 4rpx;"
      ></u-icon>
      <text class="tab-text">我的</text>
    </view>
    
  </view>
</template>

<script setup>
/**
 * 属性定义
 */
const props = defineProps({
  // 当前处于激活高亮状态的 Tab 别名 ('home' | 'search' | 'tags' | 'profile')
  activeTab: {
    type: String,
    required: true
  }
});

/**
 * 处理 Tab 切换跳转
 * @param {string} tabName Tab别名
 */
const handleTabSwitch = (tabName) => {
  // 如果点击的是当前已激活的，不做重复跳转
  if (tabName === props.activeTab) return;
  
  let targetUrl = '';
  if (tabName === 'home') targetUrl = '/pages/index/index';
  else if (tabName === 'search') targetUrl = '/pages/search/search';
  else if (tabName === 'tags') targetUrl = '/pages/tags/tags';
  else if (tabName === 'profile') targetUrl = '/pages/profile/profile';
  
  // 采用 reLaunch 重启路由，清理物理回退栈，保证底栏切换流畅
  uni.reLaunch({
    url: targetUrl
  });
};
</script>

<style lang="scss">
/* 贴底导航栏容器 */
.custom-tabbar-wrap {
  position: fixed;
  left: 0;
  bottom: 0;
  width: 100%;
  height: 100rpx;                         /* 标准高度 50px */
  background-color: $uni-bg-color-grey;   /* 背景：暗岩灰 */
  border-top: 1px solid $uni-border-color; /* 上边框线 */
  display: flex;
  align-items: center;
  justify-content: space-around;
  z-index: 999;                           /* 确保浮在所有页面元素之上 */
  box-sizing: border-box;
  padding-bottom: env(safe-area-inset-bottom); /* 适配 iOS/Android 刘海屏底部安全高度 */
}

/* 导航项 */
.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  height: 100%;
  cursor: pointer;
  transition: all 0.15s ease;
  
  .tab-icon {
    font-size: 36rpx;
    margin-bottom: 4rpx;
    opacity: 0.4;                         /* 默认置灰不显眼 */
  }
  
  .tab-text {
    font-size: 20rpx;
    color: $uni-text-color-placeholder;
    font-weight: 500;
  }
  
  /* 激活态高亮显示，采用琥珀金 */
  &.is-active {
    .tab-icon {
      opacity: 1;                         /* 恢复亮色 */
      transform: scale(1.1);
    }
    
    .tab-text {
      color: $uni-color-primary;          /* 琥珀金主色 */
      font-weight: 700;
    }
  }
  
  &:active {
    opacity: 0.8;
  }
}
</style>
