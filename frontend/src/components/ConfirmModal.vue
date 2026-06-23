<template>
  <!-- 
    通用底部确认/提示模态弹窗 (ConfirmModal)
    职责：用于删除、登出、合并等关键交互的二次安全确认，也可以退化为单纯的 Alert 信息告知弹窗。
    设计要求：基于 uview-plus 官方 <u-popup> 组件进行深度二次封装，统一样式、阴影、动画与关闭逻辑。
   -->
  <u-popup 
    :show="show" 
    mode="bottom" 
    :round="16" 
    :safeAreaInsetBottom="true"
    @close="handleCancel"
  >
    <view class="modal-wrapper">
      <!-- 顶部中央小提手（视觉装饰，增强移动端质感） -->
      <view class="modal-handle-bar"></view>
      
      <!-- 模态框头部标题 -->
      <view class="modal-header">
        <text class="modal-title">{{ title }}</text>
      </view>
      
      <!-- 模态框正文区 -->
      <view class="modal-body">
        <text class="modal-content">{{ content }}</text>
        
        <!-- 默认插槽：支持外部传入表单、单选框或额外的自定义内容（如合并标签时的目的列表选择） -->
        <slot></slot>
      </view>
      
      <!-- 模态框底部按钮操作区 -->
      <view class="modal-footer-btns">
        <!-- 取消按钮：仅当 showCancel 为 true 时渲染展示 -->
        <view v-if="showCancel" class="btn-box">
          <u-button 
            type="info" 
            :plain="true" 
            :text="cancelText" 
            customStyle="border-color: #2e2e35; color: #8888a0;"
            @click="handleCancel"
          ></u-button>
        </view>
        
        <!-- 确认按钮：若 isDanger 为 true，触发红色危险主题提示 -->
        <view class="btn-box">
          <u-button 
            :type="isDanger ? 'error' : 'primary'" 
            :text="confirmText" 
            @click="handleConfirm"
          ></u-button>
        </view>
      </view>
    </view>
  </u-popup>
</template>

<script setup>
/**
 * 组件接收的 Props 属性定义
 */
defineProps({
  // 控制弹窗开启与关闭的显示状态
  show: {
    type: Boolean,
    required: true
  },
  // 提示框标题
  title: {
    type: String,
    default: '提示'
  },
  // 提示的正文内容文本
  content: {
    type: String,
    default: ''
  },
  // 确认按钮文本，默认"确认"
  confirmText: {
    type: String,
    default: '确认'
  },
  // 取消按钮文本，默认"取消"
  cancelText: {
    type: String,
    default: '取消'
  },
  // 是否展示取消按钮 (若为 false，则起单纯的 Alert 强告知弹窗作用)
  showCancel: {
    type: Boolean,
    default: true
  },
  // 是否为危险操作 (如删除、登出、注销等)，红色警告视觉
  isDanger: {
    type: Boolean,
    default: false
  }
});

/**
 * 组件对外抛出的自定义事件定义
 */
const emit = defineEmits(['confirm', 'cancel']);

/**
 * 触发确认回调
 */
const handleConfirm = () => {
  emit('confirm');
};

/**
 * 触发取消或遮罩层关闭回调
 */
const handleCancel = () => {
  emit('cancel');
};
</script>

<style lang="scss">
/* 底部弹窗统一外层包裹容器 */
.modal-wrapper {
  background-color: $uni-bg-color-grey; // 容器色：暗岩灰
  padding: 16rpx 40rpx 48rpx;            // 上、左右、下内边距，遵循步长体系
  display: flex;
  flex-direction: column;
  box-sizing: border-box;

  /* 顶部提手装饰条 */
  .modal-handle-bar {
    width: 80rpx;
    height: 8rpx;
    background-color: $uni-border-color; // 采用边框色调
    border-radius: 4rpx;
    align-self: center;                  // 居中排布
    margin-bottom: 32rpx;                // 16px 步长
  }

  /* 头部区域 */
  .modal-header {
    margin-bottom: 24rpx;                // 12px 步长
    text-align: left;
    
    .modal-title {
      font-size: 32rpx;
      font-weight: 700;
      color: $uni-text-color;            // 亮白文本色
    }
  }

  /* 正文内容区域 */
  .modal-body {
    margin-bottom: 48rpx;                // 24px 步长
    
    .modal-content {
      font-size: 26rpx;
      color: $uni-text-color-grey;       // 辅助灰文本
      line-height: 1.6;
      white-space: pre-wrap;             // 维持正文内的换行格式
      word-break: break-all;
    }
  }

  /* 操作按钮排布行 */
  .modal-footer-btns {
    display: flex;
    gap: 24rpx;                          // 按钮组间距：12px 步长
    width: 100%;
    
    .btn-box {
      flex: 1;                           // 按钮等宽自适应
    }
  }
}
</style>
