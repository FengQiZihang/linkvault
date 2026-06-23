<script>
import { useUserStore } from '@/store/user.js';

export default {
  onLaunch: function () {
    console.log('App Launch');
    
    // 实例化用户状态管理库
    const userStore = useUserStore();
    
    // 启动安全屏障：根据登录态和资料完备度进行单向重定向
    if (!userStore.isLoggedIn) {
      console.log('[App] 拦截：未检测到有效登录态，重定向至 /pages/login/login');
      uni.reLaunch({
        url: '/pages/login/login'
      });
    } else if (!userStore.isProfileSetup) {
      console.log('[App] 拦截：已登录但信息未完善，重定向至 /pages/setup/setup');
      uni.reLaunch({
        url: '/pages/setup/setup'
      });
    }
  },
  onShow: function () {
    console.log('App Show');
  },
  onHide: function () {
    console.log('App Hide');
  },
}
</script>

<style lang="scss">
/* 1. 注入 uview-plus 的全部全局基础原子样式类 */
@import 'uview-plus/index.scss';

/* 2. 重置全局基础标签样式 */
page {
  background-color: #0e0e11; /* 默认使用深石墨黑底层色，防止打包后白屏背景闪过 */
  font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Helvetica,
    Segoe UI, Arial, Roboto, 'PingFang SC', 'miui', 'Hiragino Sans GB', 'Microsoft Yahei',
    sans-serif;
  color: #e8e8f0;
}

/* 3. 强力覆盖：确保 uview-plus 的所有输入框/文本域在暗黑背景下背景、文字和边框一致 */
.u-input,
.u-textarea,
.u-search__content {
  background-color: $uni-bg-color-hover !important; /* 背景统一为中岩灰 #222227 */
  border-color: $uni-border-color !important;       /* 边框色统一为 #2e2e35 */
}

.u-input__content__field, 
.u-textarea__field, 
.u-search__content__input,
input, 
textarea {
  color: $uni-text-color !important;      /* 输入文字强行设为亮白色 #e8e8f0 */
  background-color: transparent !important; /* 强制背景色为透明，露出父级容器的暗黑背景色 */
}

/* placeholder 颜色加深，使其与输入文字有明确的主次对比 */
.u-input__content__placeholder,
.u-textarea__placeholder,
.u-search__content__placeholder,
.u-search__content__input__placeholder {
  color: #55556a !important;      /* 占位符暗灰色 */
}

</style>
