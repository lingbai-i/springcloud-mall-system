<template>
  <div class="banner-preview-container">
    <div class="preview-header">
      <span class="preview-title">效果预览</span>
      <el-switch
        v-model="showGuidelines"
        active-text="显示安全区域"
        inactive-text=""
        size="small"
      />
    </div>
    
    <div 
      class="preview-wrapper"
      :style="{ width: containerWidth + 'px' }"
    >
      <div 
        class="preview-content"
        :style="previewStyle"
      >
        <!-- 轮播图图片 -->
        <div class="banner-image-wrapper">
          <img 
            v-if="imageUrl"
            :src="imageUrl"
            :alt="title || '轮播图预览'"
            class="banner-image"
            @load="onImageLoad"
            @error="onImageError"
          />
          <div v-else class="banner-placeholder">
            <el-icon :size="48"><Picture /></el-icon>
            <span>请上传轮播图图片</span>
          </div>
        </div>
        
        <!-- 安全区域指引 -->
        <div v-if="showGuidelines" class="safe-zone-overlay">
          <div class="safe-zone">
            <span class="safe-zone-label">安全区域</span>
          </div>
          <div class="danger-zone left"></div>
          <div class="danger-zone right"></div>
          <div class="danger-zone top"></div>
          <div class="danger-zone bottom"></div>
        </div>
        
        <!-- 标题叠加层 -->
        <div v-if="title" class="banner-title-overlay">
          <span class="banner-title-text">{{ title }}</span>
        </div>
        
        <!-- 轮播指示器模拟 -->
        <div class="carousel-indicators">
          <span class="indicator active"></span>
          <span class="indicator"></span>
          <span class="indicator"></span>
        </div>
      </div>
    </div>
    
    <!-- 尺寸提示 -->
    <div class="size-info">
      <el-icon><InfoFilled /></el-icon>
      <span>推荐尺寸: 1920 × 600 像素 (宽高比 3.2:1)</span>
      <span v-if="imageDimensions" class="current-size">
        | 当前: {{ imageDimensions.width }} × {{ imageDimensions.height }}
        <el-tag 
          v-if="!isDimensionValid" 
          type="warning" 
          size="small"
          style="margin-left: 8px;"
        >
          尺寸不符
        </el-tag>
      </span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Picture, InfoFilled } from '@element-plus/icons-vue'

// 推荐尺寸常量
const RECOMMENDED_WIDTH = 1920
const RECOMMENDED_HEIGHT = 600
const ASPECT_RATIO = RECOMMENDED_WIDTH / RECOMMENDED_HEIGHT // 3.2

const props = defineProps({
  imageUrl: {
    type: String,
    default: ''
  },
  title: {
    type: String,
    default: ''
  },
  containerWidth: {
    type: Number,
    default: 600
  }
})

// 是否显示安全区域指引
const showGuidelines = ref(false)

// 图片实际尺寸
const imageDimensions = ref(null)

// 计算预览高度，保持3.2:1的宽高比
const previewHeight = computed(() => {
  return Math.round(props.containerWidth / ASPECT_RATIO)
})

// 预览样式
const previewStyle = computed(() => ({
  width: '100%',
  height: previewHeight.value + 'px',
  aspectRatio: `${RECOMMENDED_WIDTH} / ${RECOMMENDED_HEIGHT}`
}))

// 检查图片尺寸是否符合推荐
const isDimensionValid = computed(() => {
  if (!imageDimensions.value) return true
  const { width, height } = imageDimensions.value
  // 允许10%的误差
  const widthValid = Math.abs(width - RECOMMENDED_WIDTH) / RECOMMENDED_WIDTH <= 0.1
  const heightValid = Math.abs(height - RECOMMENDED_HEIGHT) / RECOMMENDED_HEIGHT <= 0.1
  return widthValid && heightValid
})

// 图片加载成功
const onImageLoad = (event) => {
  const img = event.target
  imageDimensions.value = {
    width: img.naturalWidth,
    height: img.naturalHeight
  }
}

// 图片加载失败
const onImageError = () => {
  imageDimensions.value = null
}

// 监听imageUrl变化，重置尺寸信息
watch(() => props.imageUrl, () => {
  imageDimensions.value = null
})

// 暴露给父组件的方法和数据
defineExpose({
  imageDimensions,
  isDimensionValid,
  RECOMMENDED_WIDTH,
  RECOMMENDED_HEIGHT,
  ASPECT_RATIO
})
</script>

<style scoped>
.banner-preview-container {
  background-color: #f5f5f5;
  border-radius: 8px;
  padding: 16px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.preview-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.preview-wrapper {
  margin: 0 auto;
  overflow: hidden;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.preview-content {
  position: relative;
  background-color: #e0e0e0;
  overflow: hidden;
}

.banner-image-wrapper {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  gap: 8px;
}

.banner-placeholder span {
  font-size: 14px;
}

/* 安全区域指引 */
.safe-zone-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.safe-zone {
  position: absolute;
  top: 10%;
  left: 10%;
  right: 10%;
  bottom: 10%;
  border: 2px dashed #52c41a;
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
  padding: 8px;
}

.safe-zone-label {
  background-color: #52c41a;
  color: white;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 2px;
}

.danger-zone {
  position: absolute;
  background-color: rgba(255, 77, 79, 0.2);
}

.danger-zone.left {
  top: 0;
  left: 0;
  width: 10%;
  height: 100%;
}

.danger-zone.right {
  top: 0;
  right: 0;
  width: 10%;
  height: 100%;
}

.danger-zone.top {
  top: 0;
  left: 10%;
  right: 10%;
  height: 10%;
}

.danger-zone.bottom {
  bottom: 0;
  left: 10%;
  right: 10%;
  height: 10%;
}

/* 标题叠加层 */
.banner-title-overlay {
  position: absolute;
  bottom: 40px;
  left: 0;
  right: 0;
  padding: 12px 20px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));
}

.banner-title-text {
  color: white;
  font-size: 16px;
  font-weight: 500;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

/* 轮播指示器 */
.carousel-indicators {
  position: absolute;
  bottom: 12px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
}

.indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.5);
  transition: all 0.3s;
}

.indicator.active {
  width: 24px;
  border-radius: 4px;
  background-color: white;
}

/* 尺寸提示 */
.size-info {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.current-size {
  color: #606266;
}
</style>
