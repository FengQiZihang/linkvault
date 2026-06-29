<template>
  <!-- 
    完善个人信息页面 (Setup View)
    职责：提供新登录用户自定义头像 (Emoji) 和昵称的界面，保存后引导用户顺利开启 LinkVault 主站。
    约束：严格遵循 8px 空间系统，去除任何硬编码色值，使用 Pinia UserStore 模块进行数据写入。
   -->
  <view class="setup-container">
    <scroll-view class="setup-scroll-area" scroll-y="true">
      <view class="setup-content-box">
        
        <!-- 页面说明性标题 -->
        <view class="setup-header">
          <text class="setup-title">完善个人信息</text>
          <text class="setup-subtitle">设置昵称，并从预置头像中选一个代表你。</text>
        </view>
        
        <!-- 头像选取展示和九宫格区域 -->
        <view class="avatar-section">
          <!-- 上方当前选定大头像展示圈 -->
          <view class="avatar-circle">
            <image class="avatar-img" :src="selectedAvatarSvg" mode="aspectFit"></image>
          </view>
          <text class="avatar-hint">已选定的头像</text>
          
          <!-- 下方九宫格预设可选的 SVG 角色头像 选项 -->
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
        
        <!-- 表单及提交按钮区块 -->
        <view class="setup-form-card">
          <!-- 昵称编辑输入行 -->
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
          
          <!-- 进入系统提交按钮 -->
          <view class="submit-btn-wrap">
            <u-button
              type="primary"
              text="进入 LinkVault →"
              :disabled="!nickname.trim() || !selectedAvatarSvg"
              @click="handleSetupComplete"
            ></u-button>
          </view>
        </view>
        
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { useUserStore, AVATAR_OPTIONS, avatarUrlToSvg } from '@/store/user.js';

// 获取用户 Store
const userStore = useUserStore();

// 页面响应式数据绑定，默认取 store 中已有的 SVG（若有），或默认选中第 1 个阳光猫
const selectedAvatarSvg = ref(userStore.userInfo?.avatarSvg || AVATAR_OPTIONS[0].svgUrl);
// 昵称初始默认值采用“风启自航”，支持修改
const nickname = ref(userStore.userInfo?.nickname || '风启自航');

/**
 * 点击完成信息完善，并写入本地 Store 持久化
 */
const handleSetupComplete = async () => {
  const cleanNickname = nickname.value.trim();
  if (!cleanNickname || !selectedAvatarSvg.value) return;
  
  try {
    // 1. 调用后端保存昵称与预置头像
    await userStore.updateProfile(cleanNickname, selectedAvatarSvg.value);
    
    uni.showToast({
      title: '信息完善成功',
      icon: 'success'
    });
    
    // 2. 销毁当前路由栈直接切换进入首页，防止通过系统返回键退回 setup 页
    setTimeout(() => {
      uni.reLaunch({
        url: '/pages/index/index'
      });
    }, 800);
    
  } catch (err) {
    uni.showToast({
      title: err.message || '更新信息失败',
      icon: 'none'
    });
  }
};
</script>

<script>
// 基础 easycom 注册隔离配置
export default {
  options: {
    styleIsolation: 'shared'
  }
}
</script>

<style lang="scss">
/* 完善信息页面外层暗黑画布 */
.setup-container {
  width: 100vw;
  height: 100vh;
  background-color: $uni-bg-color; // 全局深黑色
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 局部滚动条 */
.setup-scroll-area {
  flex: 1;
  width: 100%;
  height: 100%;
}

.setup-content-box {
  padding: 60rpx 48rpx 80rpx;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

/* 页面顶部标题及副标题 */
.setup-header {
  margin-top: 24rpx;
  margin-bottom: 56rpx;
  display: flex;
  flex-direction: column;
  
  .setup-title {
    font-size: 40rpx;
    font-weight: 700;
    color: $uni-text-color;             // 亮白文本色
    margin-bottom: 12rpx;
  }
  
  .setup-subtitle {
    font-size: 26rpx;
    color: $uni-text-color-grey;        // 辅助灰
    line-height: 1.5;
  }
}

/* 头像交互选取区域 */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 56rpx;
  
  /* 当前选定的大头像外圆圈 */
  .avatar-circle {
    width: 160rpx;
    height: 160rpx;
    border-radius: 50%;
    background: linear-gradient(135deg, $uni-bg-color-hover, $uni-border-color); // 渐变底色
    border: 2rpx dashed rgba(245, 158, 11, 0.4); // 琥珀金边框装饰
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.3);
    
    .avatar-img {
      width: 110rpx;
      height: 110rpx;
      border-radius: 50%;
    }
  }
  
  .avatar-hint {
    margin-top: 16rpx;
    font-size: 22rpx;
    color: $uni-text-color-placeholder;
    margin-bottom: 36rpx;
  }
  
  /* 头像选取九宫格 */
  .avatar-grid {
    display: grid;
    grid-template-columns: repeat(3, 110rpx); // 三列布局，列宽等大
    gap: 24rpx;                              // 12px 间距
    justify-content: center;
    width: 100%;
    
    .avatar-item {
      height: 110rpx;
      border-radius: 50%;
      background-color: $uni-bg-color-grey;  // 暗岩灰
      border: 1px solid $uni-border-color;   // 边框色
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.15s ease;
      cursor: pointer;
      
      .avatar-item-img {
        width: 64rpx;
        height: 64rpx;
        border-radius: 50%;
      }
      
      /* 点击按压态微缩 */
      &:active {
        transform: scale(0.92);
      }
      
      /* 选中激活状态边框及阴影，琥珀金主色呈现 */
      &.is-active {
        border-color: $uni-color-primary;
        background-color: rgba(245, 158, 11, 0.08);
        box-shadow: 0 0 16rpx rgba(245, 158, 11, 0.3);
        transform: scale(1.05); // 稍微放大
      }
    }
  }
}

/* 完善表单卡片 */
.setup-form-card {
  background-color: $uni-bg-color-grey;     // 暗岩灰
  border: 1px solid $uni-border-color;       // 边框
  border-radius: 20rpx;                      // 10px 步长
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

.submit-btn-wrap {
  margin-top: 16rpx;
}
</style>
