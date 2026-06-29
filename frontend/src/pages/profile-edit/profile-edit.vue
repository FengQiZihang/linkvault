<template>
  <!-- 
    编辑用户资料页面 (Profile Edit View)
    职责：供用户更新修改已设定的昵称和头像 Emoji 字符。
    约束：遵守 Harness 配色规范与 8px 步长，通过 Pinia UserStore 触发更新，包含取消与返回导航。
   -->
  <view class="profile-edit-container">
    
    <!-- 头部自定义导航栏（带返回） -->
    <view class="edit-header">
      <view class="back-btn" @click="handleCancel">←</view>
      <text class="header-title">编辑用户资料</text>
      <view class="header-placeholder"></view>
    </view>

    <!-- 主滚动区域 -->
    <view class="edit-content-box">
        
        <view class="setup-header">
          <text class="setup-title">头像与昵称</text>
          <text class="setup-subtitle">修改昵称，并从预置头像中选一个代表你。</text>
        </view>
        
        <!-- 头像选取展示和九宫格区域 -->
        <view class="avatar-section">
          <view class="avatar-circle">
            <image class="avatar-img" :src="selectedAvatarSvg" mode="aspectFit"></image>
          </view>
          <text class="avatar-hint">当前头像</text>
          
          <!-- 下方预设可选的 SVG 角色头像 选项 -->
          <view class="avatar-grid">
            <view 
              v-for="item in AVATAR_OPTIONS" 
              :key="item.id"
              :class="['avatar-item', { 'is-active': selectedAvatarSvg === item.svgUrl }]"
              @click="selectedAvatarSvg = item.svgUrl"
            >
              <image class="avatar-item-img" :src="item.svgUrl" mode="aspectFit"></image>
            </view>
          </view>
        </view>
        
        <!-- 表单及保存取消按钮区块 -->
        <view class="edit-form-card">
          <view class="form-item">
            <text class="form-label">昵称</text>
            <u-input
              v-model="nickname"
              placeholder="给自己起个名字"
              border="surround"
              clearable
              maxlength="15"
            ></u-input>
          </view>
          
          <view class="btns-stack">
            <view class="submit-btn-wrap">
              <u-button
                type="primary"
                text="保存修改"
                :disabled="!nickname.trim() || !selectedAvatarSvg"
                @click="handleSave"
              ></u-button>
            </view>
            <view class="cancel-btn-wrap">
              <u-button
                type="info"
                :plain="true"
                text="取消"
                customStyle="border-color: #2e2e35; color: #8888a0;"
                @click="handleCancel"
              ></u-button>
            </view>
          </view>
        </view>
        
      </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { useUserStore, AVATAR_OPTIONS, avatarUrlToSvg } from '@/store/user.js';

// 获取用户 Store
const userStore = useUserStore();

// 从 store 中回填现有数据，若缺失则默认选中第 1 个阳光猫
const selectedAvatarSvg = ref(userStore.userInfo?.avatarSvg || AVATAR_OPTIONS[0].svgUrl);
const nickname = ref(userStore.userInfo?.nickname || '风启自航');

/**
 * 确认保存修改后的资料
 */
const handleSave = async () => {
  const cleanNickname = nickname.value.trim();
  if (!cleanNickname || !selectedAvatarSvg.value) return;
  
  try {
    await userStore.updateProfile(cleanNickname, selectedAvatarSvg.value);
    uni.showToast({
      title: '修改成功',
      icon: 'success'
    });
    
    // 成功后延时返回个人中心
    setTimeout(() => {
      uni.navigateBack();
    }, 800);
    
  } catch (err) {
    uni.showToast({
      title: err.message || '修改失败',
      icon: 'none'
    });
  }
};

/**
 * 取消并返回上一页
 */
const handleCancel = () => {
  uni.navigateBack();
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
.profile-edit-container {
  width: 100vw;
  min-height: 100vh;
  background-color: $uni-bg-color; // 石墨黑
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

/* 顶部自定义导航栏 */
.edit-header {
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



.edit-content-box {
  padding: 40rpx 48rpx 80rpx;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.setup-header {
  margin-bottom: 48rpx;
  display: flex;
  flex-direction: column;
  
  .setup-title {
    font-size: 36rpx;
    font-weight: 700;
    color: $uni-text-color;
    margin-bottom: 12rpx;
  }
  
  .setup-subtitle {
    font-size: 24rpx;
    color: $uni-text-color-grey;
    line-height: 1.5;
  }
}

/* 头像交互选取区域 */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 48rpx;
  
  .avatar-circle {
    width: 150rpx;
    height: 150rpx;
    border-radius: 50%;
    background: linear-gradient(135deg, $uni-bg-color-hover, $uni-border-color);
    border: 2rpx dashed rgba(245, 158, 11, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.3);
    
    .avatar-img {
      width: 100rpx;
      height: 100rpx;
      border-radius: 50%;
    }
  }
  
  .avatar-hint {
    margin-top: 12rpx;
    font-size: 22rpx;
    color: $uni-text-color-placeholder;
    margin-bottom: 32rpx;
  }
  
  .avatar-grid {
    display: grid;
    grid-template-columns: repeat(3, 100rpx);
    gap: 20rpx;
    justify-content: center;
    width: 100%;
    
    .avatar-item {
      height: 100rpx;
      border-radius: 50%;
      background-color: $uni-bg-color-grey;
      border: 1px solid $uni-border-color;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.15s ease;
      cursor: pointer;
      
      .avatar-item-img {
        width: 60rpx;
        height: 60rpx;
        border-radius: 50%;
      }
      
      .avatar-item-char {
        font-size: 44rpx;
      }
      
      &:active {
        transform: scale(0.92);
      }
      
      &.is-active {
        border-color: $uni-color-primary;
        background-color: rgba(245, 158, 11, 0.08);
        box-shadow: 0 0 16rpx rgba(245, 158, 11, 0.3);
        transform: scale(1.05);
      }
    }
  }
}

.edit-form-card {
  background-color: $uni-bg-color-grey;
  border: 1px solid $uni-border-color;
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
  }
}

.btns-stack {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-top: 16rpx;
  
  .submit-btn-wrap, .cancel-btn-wrap {
    width: 100%;
  }
}
</style>
