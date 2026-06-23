<template>
  <!-- 
    个人中心与设置页面 (Profile View)
    职责：展示当前登录的用户信息、提供关于说明以及处理安全退出登录交互。
    约束：遵守 Harness 规范与 8px 步长，操作通过 Pinia UserStore 触发，写满详尽注释。
   -->
  <view class="profile-container">
    
    <view class="profile-main-content">
      
      <!-- 顶部用户信息展示大卡片 -->
      <view class="user-profile-card">
        <view class="avatar-box">
          <text class="avatar-char">{{ userStore.userInfo?.avatar || '🦊' }}</text>
        </view>
        <view class="user-details">
          <text class="user-nickname">{{ userStore.userInfo?.nickname || '新用户' }}</text>
          <text class="user-phone">绑定手机：+86 {{ desensitizedPhone }}</text>
        </view>
      </view>

      <view class="profile-stats-grid">
        <view class="profile-stat-item">
          <text class="stat-value">{{ bookmarkStore.stats.totalBookmarkCount }}</text>
          <text class="stat-label">全部收藏</text>
        </view>
        <view class="profile-stat-item">
          <text class="stat-value">{{ bookmarkStore.stats.tagCount }}</text>
          <text class="stat-label">标签数</text>
        </view>
        <view class="profile-stat-item" @click="navigateToUntagged">
          <text class="stat-value">{{ bookmarkStore.stats.untaggedBookmarkCount }}</text>
          <text class="stat-label">未打标签</text>
        </view>
      </view>

      <!-- 个人设置功能列表（遵循 8px 空间系统） -->
      <view class="settings-list-group">
        
        <!-- 功能行 1：编辑个人资料（跳回 setup 页面进行编辑） -->
        <view class="setting-item-row" @click="navigateToEditProfile">
          <text class="item-icon">✏️</text>
          <text class="item-label">编辑个人资料</text>
          <text class="item-value">修改头像/昵称</text>
          <text class="item-arrow">></text>
        </view>
        
        <!-- 功能行 2：意见反馈 -->
        <view class="setting-item-row" @click="showFeedbackModal = true">
          <text class="item-icon">💬</text>
          <text class="item-label">意见反馈</text>
          <text class="item-value">提供产品建议</text>
          <text class="item-arrow">></text>
        </view>

        <!-- 功能行 3：关于 LinkVault -->
        <view class="setting-item-row" @click="showAboutModal = true">
          <text class="item-icon">💡</text>
          <text class="item-label">关于 LinkVault</text>
          <text class="item-value">v1.0.0</text>
          <text class="item-arrow">></text>
        </view>
        
        <!-- 分割线 -->
        <view class="group-divider"></view>
        
        <!-- 功能行 4：安全退出登录（独立单行，危险动作红字） -->
        <view class="setting-item-row is-logout-btn" @click="showLogoutConfirm = true">
          <text class="item-icon danger-color">🚪</text>
          <text class="item-label danger-color">退出登录</text>
          <text class="item-arrow danger-color">></text>
        </view>

      </view>

    </view>

    <!-- ====== 交互弹框 1：关于 LinkVault 提示框 ====== -->
    <confirm-modal
      :show="showAboutModal"
      title="关于 LinkVault"
      content="LinkVault 个人私有收藏夹 MVP\n\n“存得住，找得回，看得懂自己为什么存。”\n\n版本：v1.0.0\n技术栈：uni-app + Vue 3 + Pinia + uview-plus"
      confirmText="知道了"
      :showCancel="false"
      @confirm="showAboutModal = false"
    ></confirm-modal>

    <!-- ====== 交互弹框 2：退出登录二次确认框 ====== -->
    <confirm-modal
      :show="showLogoutConfirm"
      title="退出登录确认"
      content="确认要退出登录当前账号吗？\n\n退出后，下次登录需要重新输入短信验证码校验。"
      confirmText="确认退出"
      :isDanger="true"
      @confirm="executeLogout"
      @cancel="showLogoutConfirm = false"
    ></confirm-modal>

    <!-- ====== 交互弹框 3：意见反馈提示框 ====== -->
    <confirm-modal
      :show="showFeedbackModal"
      title="意见反馈与支持"
      content="如果您在使用 LinkVault 时遇到任何问题，或有更好的功能建议，欢迎随时反馈给我们：\n\n官方邮箱：support@linkvault.com\n官方微信：LinkVault 助手\n\n我们将认真聆听您的声音，感谢您的厚爱与支持！"
      confirmText="知道了"
      :showCancel="false"
      @confirm="showFeedbackModal = false"
    ></confirm-modal>

    <!-- 底部自定义导航栏 -->
    <CustomTabBar active-tab="profile"></CustomTabBar>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { useUserStore } from '@/store/user.js';
import { useBookmarkStore } from '@/store/bookmark.js';
import ConfirmModal from '@/components/ConfirmModal.vue';
import CustomTabBar from '@/components/CustomTabBar.vue';

// 载入状态管理
const userStore = useUserStore();
const bookmarkStore = useBookmarkStore();

// 弹框控制状态
const showAboutModal = ref(false);
const showLogoutConfirm = ref(false);
const showFeedbackModal = ref(false);

onShow(() => {
  Promise.all([
    userStore.fetchCurrentUser(),
    bookmarkStore.fetchStats()
  ]).catch(err => {
    uni.showToast({
      title: err.message || '个人数据加载失败',
      icon: 'none'
    });
  });
});

/**
 * 属性计算：手机号码脱敏展示（例如 138****5678）
 */
const desensitizedPhone = computed(() => {
  const phone = userStore.userInfo?.phone || '';
  if (!phone || phone.length !== 11) return '138****0000';
  return `${phone.substring(0, 3)}****${phone.substring(7)}`;
});

/**
 * 路由导航：前往编辑修改个人资料
 */
const navigateToEditProfile = () => {
  uni.navigateTo({
    url: '/pages/profile-edit/profile-edit'
  });
};

const navigateToUntagged = () => {
  uni.navigateTo({
    url: '/pages/tagresult/tagresult?mode=untagged'
  });
};



/**
 * 确认执行安全退出登录，重定向销毁路由栈
 */
const executeLogout = () => {
  showLogoutConfirm.value = false;
  
  uni.showToast({
    title: '已退出登录',
    icon: 'success'
  });
  
  // 执行退出逻辑并重定位
  setTimeout(() => {
    userStore.logout();
  }, 600);
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
/* 个人中心大容器，使用全局深色 */
.profile-container {
  min-height: 100vh;
  background-color: $uni-bg-color; // 石墨黑
  box-sizing: border-box;
  padding-bottom: 160rpx;                  /* 底部留足 160rpx 留白，防止被 TabBar 遮挡 */
}

.profile-main-content {
  display: flex;
  flex-direction: column;
}

/* 用户资料头部卡片 */
.user-profile-card {
  padding: 64rpx 48rpx;
  background-color: $uni-bg-color-grey; // 暗岩灰
  border-bottom: 1px solid $uni-border-color;
  display: flex;
  align-items: center;
  gap: 32rpx;
  
  /* 头像圈 */
  .avatar-box {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    background-color: $uni-bg-color-hover; // 中岩灰
    border: 1px solid $uni-border-color;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.2);
    
    .avatar-char {
      font-size: 56rpx;
    }
  }
  
  .user-details {
    display: flex;
    flex-direction: column;
    gap: 8rpx;
    
    .user-nickname {
      font-size: 36rpx;
      font-weight: 700;
      color: $uni-text-color; // 亮白
    }
    
    .user-phone {
      font-size: 22rpx;
      color: $uni-text-color-placeholder;
    }
  }
}

/* 功能行区块组 */
.settings-list-group {
  padding: 40rpx 36rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;                          // 8px 步长
  box-sizing: border-box;
}

.profile-stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
  padding: 32rpx 36rpx 0;
  box-sizing: border-box;
}

.profile-stat-item {
  background-color: $uni-bg-color-grey;
  border: 1px solid $uni-border-color;
  border-radius: 12rpx;
  padding: 24rpx 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  
  .stat-value {
    font-size: 34rpx;
    color: $uni-color-primary;
    font-weight: 700;
  }
  
  .stat-label {
    font-size: 22rpx;
    color: $uni-text-color-placeholder;
  }
}

/* 通用功能设置行 */
.setting-item-row {
  background-color: $uni-bg-color-grey; // 暗岩灰
  border: 1px solid $uni-border-color;
  border-radius: 12rpx;
  padding: 32rpx 28rpx;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.15s ease;
  
  .item-icon {
    font-size: 32rpx;
    margin-right: 16rpx;
  }
  
  .item-label {
    flex: 1;
    font-size: 26rpx;
    color: $uni-text-color;
    font-weight: 500;
  }
  
  .item-value {
    font-size: 22rpx;
    color: $uni-text-color-placeholder;
    margin-right: 16rpx;
  }
  
  .item-arrow {
    font-size: 24rpx;
    color: $uni-text-color-placeholder;
  }
  
  /* 退出登录专属红色警告颜色 */
  &.is-logout-btn {
    border-color: rgba(248, 113, 113, 0.25);
    background-color: rgba(248, 113, 113, 0.02);
  }
  
  .danger-color {
    color: $uni-color-error;
  }

  &:active {
    background-color: $uni-bg-color-hover; // 选中态中岩灰
    transform: scale(0.99);
  }
}

/* 分割线 */
.group-divider {
  height: 1px;
  background-color: $uni-border-color;
  margin: 16rpx 0;
}
</style>
