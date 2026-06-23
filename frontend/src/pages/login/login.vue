<template>
  <!-- 
    登录注册视图页面 (Login View)
    职责：供用户通过输入手机号码、获取并验证验证码登录进系统。
    约束：遵守“暗岩与琥珀”全局暗黑设计规范，所有间距按 8px 步长排列，无死写十六进制魔法色。
   -->
  <view class="login-container">
    <!-- 主滚动区域 -->
    <scroll-view class="login-scroll-area" scroll-y="true">
      <view class="login-content-box">
        
        <!-- 头部 Logo 和标语区 -->
        <view class="logo-area">
          <view class="logo-text">Link<text class="logo-accent">Vault</text></view>
          <text class="logo-tagline">存得住，找得回，看得懂自己为什么存。</text>
        </view>
        
        <!-- 表单录入区块 -->
        <view class="form-card">
          <!-- 手机号录入行 -->
          <view class="form-item">
            <text class="form-label">手机号码</text>
            <view class="phone-input-row">
              <view class="country-prefix">+86</view>
              <view class="input-wrap">
                <u-input
                  v-model="phone"
                  type="number"
                  maxlength="11"
                  placeholder="请输入11位手机号"
                  border="surround"
                  clearable
                ></u-input>
              </view>
            </view>
          </view>
          
          <!-- 验证码录入行 -->
          <view class="form-item">
            <text class="form-label">短信验证码</text>
            <view class="code-input-row">
              <view class="input-wrap">
                <u-input
                  v-model="code"
                  type="number"
                  maxlength="6"
                  placeholder="6位短信验证码"
                  border="surround"
                  clearable
                ></u-input>
              </view>
              <!-- 发送验证码按钮：自带倒计时交互控制 -->
              <view class="btn-send-wrap">
                <u-button
                  :type="isCounting ? 'info' : 'primary'"
                  :plain="true"
                  :disabled="isCounting || !isValidPhone"
                  :text="sendBtnText"
                  @click="handleSendCode"
                ></u-button>
              </view>
            </view>
          </view>
          
          <!-- 确认提交按钮 -->
          <view class="submit-btn-wrap">
            <u-button
              type="primary"
              text="登录 / 注册"
              :disabled="!isValidPhone || code.length !== 6"
              @click="handleLogin"
            ></u-button>
          </view>
          
          <!-- 隐私与合规说明提示语 -->
          <view class="policy-tips">
            <text>未注册手机号验证后将自动注册新账号</text>
            <text>登录即代表您已阅读并同意《服务协议》与《隐私政策》</text>
          </view>
        </view>
        
      </view>
    </scroll-view>
    
    <!-- 接入自定义封装的通用提示框，验证 uview-plus 弹出兼容性 -->
    <confirm-modal
      :show="showInfoModal"
      title="验证码已发送"
      :content="modalContent"
      confirmText="我已知晓"
      :showCancel="false"
      @confirm="showInfoModal = false"
    ></confirm-modal>
  </view>
</template>

<script setup>
import { ref, computed, onBeforeUnmount } from 'vue';
import { useUserStore } from '@/store/user.js';
import ConfirmModal from '@/components/ConfirmModal.vue';

// 引入用户状态 Store
const userStore = useUserStore();

// 表单响应式字段
const phone = ref('');
const code = ref('');

// 倒计时交互逻辑相关状态
const isCounting = ref(false);
const countdown = ref(60);
const sendBtnText = ref('发送验证码');
let timer = null;

// 通用确认框展示状态
const showInfoModal = ref(false);
const modalContent = ref('');

/**
 * 计算属性：利用正则快速验证输入的手机号格式是否正确
 */
const isValidPhone = computed(() => {
  const reg = /^1[3-9]\d{9}$/;
  return reg.test(phone.value);
});

/**
 * 启动发送验证码倒计时计数器
 */
const startCountdown = () => {
  isCounting.value = true;
  countdown.value = 60;
  sendBtnText.value = `${countdown.value}s`;
  
  timer = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) {
      clearInterval(timer);
      isCounting.value = false;
      sendBtnText.value = '发送验证码';
    } else {
      sendBtnText.value = `${countdown.value}s`;
    }
  }, 1000);
};

/**
 * 点击发送验证码动作
 */
const handleSendCode = async () => {
  if (!isValidPhone.value) return;

  try {
    const result = await userStore.sendCode(phone.value);
    countdown.value = result?.cooldownSeconds || 60;
    startCountdown();

    modalContent.value = `短信验证码已发送至 +86 ${phone.value.substring(0, 3)}****${phone.value.substring(7)}。开发环境固定验证码为 123456。`;
    showInfoModal.value = true;
  } catch (err) {
    uni.showToast({
      title: err.message || '发送验证码失败',
      icon: 'none'
    });
  }
};

/**
 * 点击执行登录操作
 */
const handleLogin = async () => {
  if (!isValidPhone.value || code.value.length !== 6) return;
  
  try {
    // 1. 提交至后端进行登录鉴权与数据写入
    await userStore.login(phone.value, code.value);
    
    uni.showToast({
      title: '登录成功',
      icon: 'success'
    });
    
    // 2. 检查当前用户的资料完备度，决定跳转终点
    setTimeout(() => {
      if (userStore.isProfileSetup) {
        // 若资料已全，重定向销毁栈直接入首页
        uni.reLaunch({
          url: '/pages/index/index'
        });
      } else {
        // 若新用户尚未设置头像，则去信息完善页，同样销毁之前页面栈防物理回退
        uni.reLaunch({
          url: '/pages/setup/setup'
        });
      }
    }, 800);
    
  } catch (err) {
    uni.showToast({
      title: err.message || '登录失败，请重试',
      icon: 'none'
    });
  }
};

// 页面销毁前必须清理倒计时定时器，防内存溢出
onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer);
  }
});
</script>

<script>
// uni-app 经典配置以支持子组件的 easycom 规范
export default {
  options: {
    styleIsolation: 'shared'
  }
}
</script>

<style lang="scss">
/* 登录页面最外层高雅暗黑背景 */
.login-container {
  width: 100vw;
  height: 100vh;
  background-color: $uni-bg-color; // 全局深底色
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 局部滚动 */
.login-scroll-area {
  flex: 1;
  width: 100%;
  height: 100%;
}

.login-content-box {
  padding: 80rpx 48rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-sizing: border-box;
}

/* 头部 Logo 样式 */
.logo-area {
  margin-top: 64rpx;                    // 32px 步长
  margin-bottom: 96rpx;                 // 48px 步长
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .logo-text {
    font-size: 64rpx;
    font-weight: 700;
    color: $uni-text-color;             // 亮白
    letter-spacing: -2rpx;
    
    .logo-accent {
      color: $uni-color-primary;          // 琥珀金主色
    }
  }
  
  .logo-tagline {
    margin-top: 24rpx;
    font-size: 26rpx;
    color: $uni-text-color-grey;        // 辅助灰
    text-align: center;
    line-height: 1.6;
  }
}

/* 输入表单卡片 */
.form-card {
  width: 100%;
  background-color: $uni-bg-color-grey; // 暗岩灰
  border: 1px solid $uni-border-color;   // 边框
  border-radius: 24rpx;                  // 12px 步长
  padding: 48rpx 36rpx;
  display: flex;
  flex-direction: column;
  gap: 32rpx;                            // 表单项间距：16px 步长
  box-sizing: border-box;
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

/* 手机号输入行结构 */
.phone-input-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  width: 100%;
  
  .country-prefix {
    background-color: $uni-bg-color-hover; // 选中太中岩灰
    border: 1px solid $uni-border-color;
    border-radius: 8rpx;
    padding: 24rpx 28rpx;
    color: $uni-text-color-grey;
    font-size: 28rpx;
    font-weight: bold;
    height: 78rpx;                         // 统一度量高度
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
  }
  
  .input-wrap {
    flex: 1;
  }
}

/* 验证码输入行结构 */
.code-input-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  width: 100%;
  
  .input-wrap {
    flex: 1;
  }
  
  .btn-send-wrap {
    width: 200rpx;                         // 限制发送按钮宽度
    box-sizing: border-box;
  }
}

.submit-btn-wrap {
  margin-top: 16rpx;
}

/* 隐私小字说明 */
.policy-tips {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  text-align: center;
  font-size: 20rpx;
  color: $uni-text-color-placeholder;
  line-height: 1.5;
  margin-top: 16rpx;
}
</style>
