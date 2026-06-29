<template>
  <!-- 
    标签芯片组件 (TagChip)
    职责：用于在首页横滑栏、详情页展示、搜索筛选页中呈现标签，提供“普通态”、“置顶态（带📌图标）”及“选中态（高亮）”的不同视觉变化。
    设计要求：纯展示型 stateless 组件，所有样式均引用全局 CSS 变量/SCSS，严禁硬编码颜色值。
   -->
  <view 
    :class="[
      'tag-chip', 
      { 
        'is-pinned': pinned, 
        'is-selected': selected 
      }
    ]"
    @click="handleTap"
  >
    <!-- 置顶状态下展示 矢量别针图标 -->
    <u-icon v-if="pinned && showPinIcon" name="pushpin-fill" size="12" color="#fbbf24" customStyle="margin-right: 2rpx;"></u-icon>
    
    <!-- 标签名称 -->
    <text class="tag-name">{{ name }}</text>
    
    <!-- 数量统计（如果有传入则展示，例如“(5)”） -->
    <text v-if="count !== undefined && count > 0" class="tag-count">({{ count }})</text>
  </view>
</template>

<script setup>
/**
 * 组件接收的 Props 属性定义
 */
const props = defineProps({
  // 标签显示的名称
  name: {
    type: String,
    required: true
  },
  // 是否置顶 (默认否)
  pinned: {
    type: Boolean,
    default: false
  },
  // 是否处于选中状态 (默认否)
  selected: {
    type: Boolean,
    default: false
  },
  // 关联的书签数量，不传则不显示数量徽标
  count: {
    type: Number,
    default: undefined
  },
  // 是否在置顶时展示小别针图标 (默认展示)
  showPinIcon: {
    type: Boolean,
    default: true
  }
});

/**
 * 组件对外抛出的自定义事件定义
 */
const emit = defineEmits(['click']);

/**
 * 处理用户点击动作，向上抛出当前标签名称
 */
const handleTap = () => {
  emit('click', props.name);
};
</script>

<style lang="scss">
/* 标签芯片基础样式 */
.tag-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;                /* 强制不收缩，防横滑容器内压扁 */
  align-self: center;            /* 强制居中，防高度被拉伸 stretch */
  height: 56rpx;                 /* 限制固定高度为大药丸 */
  gap: 8rpx;                     /* 8px 步长系统微调 */
  padding: 0 24rpx;              /* 左右加宽，符合 12px padding 步长 */
  background-color: $uni-bg-color-grey; // 背景：暗岩灰
  border: 1px solid $uni-border-color;   // 边框色
  border-radius: 28rpx;          /* 完美药丸圆角 */
  cursor: pointer;
  transition: all 0.15s ease;
  box-sizing: border-box;
  
  // 内部文本样式，禁止换行
  .tag-name {
    font-size: 22rpx;
    color: $uni-text-color-grey;  // 默认辅助文字灰
    font-weight: 500;
    white-space: nowrap;          /* 强制文字不能发生换行 */
  }
  
  // 内部计数样式
  .tag-count {
    font-size: 20rpx;
    color: $uni-text-color-placeholder;
  }
  
  // 置顶别针图标样式
  .pin-icon {
    font-size: 22rpx;
  }

  /* 状态一：置顶态 (Pinned) 样式 */
  &.is-pinned {
    background-color: rgba(251, 191, 36, 0.1); // 使用柔和透明琥珀背景
    border-color: rgba(251, 191, 36, 0.4);
    
    .tag-name {
      color: $uni-color-warning; // 黄色警告色替代
    }
  }

  /* 状态二：选中激活态 (Selected) 样式 */
  &.is-selected {
    background-color: rgba(245, 158, 11, 0.18); // 琥珀金主色背景
    border-color: $uni-color-primary;          // 主色边框
    
    .tag-name {
      color: $uni-text-color;       // 亮白文本色
      font-weight: 700;
    }
    
    .tag-count {
      color: $uni-text-color;
    }
  }

  /* 点击时的微交互反馈 */
  &:active {
    opacity: 0.8;
    transform: scale(0.96);
  }
}
</style>
