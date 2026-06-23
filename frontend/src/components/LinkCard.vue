<template>
  <!-- 
    书签卡片组件 (LinkCard)
    职责：用于在首页列表、标签过滤页及搜索结果页中呈现单个书签的缩略图、标题、平台来源、发布时间及备注信息。
    设计要求：纯展示型 stateless 组件。当传入关键字时，支持标题的关键词高亮显示。
   -->
  <view class="link-card" @click="handleCardTap">
    <!-- 顶部：标题区域（若有匹配关键字，需要高亮展示） -->
    <view class="card-title-row">
      <rich-text :nodes="computedTitle" class="card-title"></rich-text>
    </view>
    
    <!-- 中部：备注区域（若备注为空，则不占位渲染） -->
    <view v-if="bookmark.note" class="card-note-row">
      <text class="card-note">{{ bookmark.note }}</text>
    </view>
    
    <!-- 底部：元数据与标签区块 -->
    <view class="card-footer">
      <!-- 平台及时间信息 -->
      <view class="meta-info">
        <text class="platform-icon">{{ platformIcon }}</text>
        <text class="platform-label">{{ platformLabel }}</text>
        <text class="divider-dot">·</text>
        <text class="time-label">{{ formattedTime }}</text>
      </view>
      
      <!-- 卡片内的标签横向列表（若无任何标签，则不显示该块） -->
      <view v-if="bookmark.tags && bookmark.tags.length > 0" class="tag-row">
        <view 
          v-for="tag in bookmark.tags" 
          :key="tag" 
          class="card-tag-chip"
          @click.stop="handleTagTap(tag)"
        >
          <text class="card-tag-text">#{{ tag }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue';
import { PLATFORM_MAP } from '@/store/bookmark.js';

/**
 * 组件接收的 Props 属性定义
 */
const props = defineProps({
  // 书签/链接实体数据对象
  bookmark: {
    type: Object,
    required: true
  },
  // 高亮搜索关键字
  highlightKeyword: {
    type: String,
    default: ''
  }
});

/**
 * 组件对外抛出的自定义事件定义
 */
const emit = defineEmits(['click', 'tag-click']);

/**
 * 属性计算：智能匹配并展示平台所对应的 Emoji 图标
 */
const platformIcon = computed(() => {
  const meta = PLATFORM_MAP[props.bookmark.platform] || PLATFORM_MAP.OTHER;
  return meta.icon;
});

/**
 * 属性计算：智能匹配平台的中文别名
 */
const platformLabel = computed(() => {
  const meta = PLATFORM_MAP[props.bookmark.platform] || PLATFORM_MAP.OTHER;
  return meta.label;
});

/**
 * 属性计算：格式化时间展示（只截取日期，友好便于人工阅读）
 */
const formattedTime = computed(() => {
  if (!props.bookmark.savedAt) return '';
  const dateStr = props.bookmark.savedAt;
  // 简易处理 "2026-04-26T09:10:00.000Z" -> "2026-04-26"
  return dateStr.substring(0, 10);
});

/**
 * 属性计算：支持检索关键字的标题高亮渲染逻辑
 */
const computedTitle = computed(() => {
  const originalTitle = props.bookmark.title || '未命名网页';
  if (!props.highlightKeyword) {
    return originalTitle;
  }
  
  // 使用正则匹配关键字，并替换为带样式的 html span 节点进行富文本渲染
  const escapedKeyword = props.highlightKeyword.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
  const regex = new RegExp(`(${escapedKeyword})`, 'gi');
  // 利用 SCSS 映射的主色调高亮包裹
  return originalTitle.replace(regex, `<span style="color: #f59e0b; font-weight: bold;">$1</span>`);
});

/**
 * 点击整个卡片，通知父页面打开此书签的详情视图
 */
const handleCardTap = () => {
  emit('click', props.bookmark.bookmarkId);
};

/**
 * 点击卡片底部的特定标签，通知外部页面执行标签过滤
 * @param {string} tagName 标签名称
 */
const handleTagTap = (tagName) => {
  emit('tag-click', tagName);
};
</script>

<style lang="scss">
/* 书签卡片外层容器 */
.link-card {
  background-color: $uni-bg-color-grey; // 背景：暗岩灰
  border: 1px solid $uni-border-color;   // 边框色
  border-radius: 16rpx;                  // 圆角遵循 8px 步长
  padding: 24rpx 28rpx;                  // 内边距符合空间步长规则
  margin-bottom: 20rpx;                  // 列表项间距
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  transition: all 0.1s ease;

  /* 点击按压态微反馈 */
  &:active {
    background-color: $uni-bg-color-hover; // 变换为点击态中岩灰
    transform: scale(0.99);
  }

  /* 标题区块 */
  .card-title-row {
    margin-bottom: 12rpx;
    
    .card-title {
      font-size: 28rpx;
      color: $uni-text-color;            // 亮白主文本色
      line-height: 1.5;
      font-weight: 500;
      word-break: break-all;
    }
  }

  /* 备注区块 */
  .card-note-row {
    margin-bottom: 18rpx;
    padding: 12rpx 16rpx;
    background-color: rgba(255, 255, 255, 0.02); // 极轻微的微弱高亮作为卡片内部微卡片
    border-left: 4rpx solid $uni-color-primary;  // 琥珀金小竖条装饰
    border-radius: 4rpx;

    .card-note {
      font-size: 24rpx;
      color: $uni-text-color-grey;       // 辅助灰文本
      line-height: 1.6;
      word-break: break-all;
    }
  }

  /* 页脚元数据区块 */
  .card-footer {
    display: flex;
    flex-direction: column;
    gap: 16rpx;
    
    // 元数据行 (平台、时间)
    .meta-info {
      display: flex;
      align-items: center;
      font-size: 22rpx;
      color: $uni-text-color-placeholder;
      
      .platform-icon {
        margin-right: 6rpx;
        font-size: 24rpx;
      }
      
      .platform-label {
        font-weight: 500;
      }
      
      .divider-dot {
        margin: 0 10rpx;
        font-weight: bold;
      }
    }
    
    // 卡片内微标签排布行
    .tag-row {
      display: flex;
      flex-wrap: wrap;
      gap: 12rpx;
      
      .card-tag-chip {
        padding: 4rpx 12rpx;
        background-color: rgba(245, 158, 11, 0.08); // 极其淡雅的主色背景
        border: 1px solid rgba(245, 158, 11, 0.2);
        border-radius: 8rpx;
        
        .card-tag-text {
          font-size: 20rpx;
          color: $uni-color-primary; // 琥珀金主色
          font-weight: 500;
        }
        
        &:active {
          opacity: 0.7;
        }
      }
    }
  }
}
</style>
