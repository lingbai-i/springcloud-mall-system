<template>
  <div 
    class="image-magnifier-container"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
    @mousemove="handleMouseMove"
    @touchstart.passive="handleTouchStart"
    @touchmove.passive="handleTouchMove"
    @touchend="handleTouchEnd"
    ref="containerRef"
  >
    <!-- 原始图片 -->
    <img 
      :src="src" 
      :alt="alt"
      class="magnifier-image"
      ref="imageRef"
      @load="handleImageLoad"
      @click="$emit('click')"
    />
    
    <!-- 放大镜遮罩层 -->
    <div 
      v-show="showMagnifier && isImageLoaded"
      class="magnifier-lens"
      :style="lensStyle"
    ></div>
    
    <!-- 放大预览区域 -->
    <div 
      v-show="showMagnifier && isImageLoaded"
      class="magnifier-preview"
      :style="previewStyle"
    >
      <img 
        :src="src" 
        :alt="alt"
        class="preview-image"
        :style="previewImageStyle"
      />
    </div>
    
    <!-- 放大镜提示 -->
    <div v-if="showHint && !showMagnifier" class="magnifier-hint">
      <span>🔍 悬停查看大图</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  src: {
    type: String,
    required: true
  },
  alt: {
    type: String,
    default: ''
  },
  zoomLevel: {
    type: Number,
    default: 2.5
  },
  lensSize: {
    type: Number,
    default: 150
  },
  previewWidth: {
    type: Number,
    default: 400
  },
  previewHeight: {
    type: Number,
    default: 400
  },
  showHint: {
    type: Boolean,
    default: true
  }
})

defineEmits(['click'])

const containerRef = ref(null)
const imageRef = ref(null)
const showMagnifier = ref(false)
const isImageLoaded = ref(false)
const mouseX = ref(0)
const mouseY = ref(0)
const imageRect = ref({ width: 0, height: 0, left: 0, top: 0 })

// 计算放大镜镜头样式
const lensStyle = computed(() => {
  const halfLens = props.lensSize / 2
  let x = mouseX.value - halfLens
  let y = mouseY.value - halfLens
  
  // 限制镜头在图片范围内
  x = Math.max(0, Math.min(x, imageRect.value.width - props.lensSize))
  y = Math.max(0, Math.min(y, imageRect.value.height - props.lensSize))
  
  return {
    width: `${props.lensSize}px`,
    height: `${props.lensSize}px`,
    left: `${x}px`,
    top: `${y}px`
  }
})

// 计算预览区域样式
const previewStyle = computed(() => {
  return {
    width: `${props.previewWidth}px`,
    height: `${props.previewHeight}px`
  }
})

// 计算预览图片样式
const previewImageStyle = computed(() => {
  const halfLens = props.lensSize / 2
  let x = mouseX.value - halfLens
  let y = mouseY.value - halfLens
  
  // 限制镜头在图片范围内
  x = Math.max(0, Math.min(x, imageRect.value.width - props.lensSize))
  y = Math.max(0, Math.min(y, imageRect.value.height - props.lensSize))
  
  // 计算放大后的图片尺寸
  const scaledWidth = imageRect.value.width * props.zoomLevel
  const scaledHeight = imageRect.value.height * props.zoomLevel
  
  // 计算偏移量
  const offsetX = -(x * props.zoomLevel)
  const offsetY = -(y * props.zoomLevel)
  
  return {
    width: `${scaledWidth}px`,
    height: `${scaledHeight}px`,
    transform: `translate(${offsetX}px, ${offsetY}px)`
  }
})

const updateImageRect = () => {
  if (imageRef.value) {
    const rect = imageRef.value.getBoundingClientRect()
    const containerRect = containerRef.value.getBoundingClientRect()
    imageRect.value = {
      width: rect.width,
      height: rect.height,
      left: rect.left - containerRect.left,
      top: rect.top - containerRect.top
    }
  }
}

const handleImageLoad = () => {
  isImageLoaded.value = true
  updateImageRect()
}

const handleMouseEnter = () => {
  showMagnifier.value = true
  updateImageRect()
}

const handleMouseLeave = () => {
  showMagnifier.value = false
}

const handleMouseMove = (e) => {
  if (!containerRef.value) return
  
  const rect = containerRef.value.getBoundingClientRect()
  mouseX.value = e.clientX - rect.left
  mouseY.value = e.clientY - rect.top
}

// 触摸事件处理（移动端支持）
let touchTimeout = null

const handleTouchStart = (e) => {
  e.preventDefault()
  showMagnifier.value = true
  updateImageRect()
  handleTouchPosition(e)
}

const handleTouchMove = (e) => {
  e.preventDefault()
  handleTouchPosition(e)
}

const handleTouchEnd = () => {
  // 延迟隐藏，让用户有时间看清放大效果
  touchTimeout = setTimeout(() => {
    showMagnifier.value = false
  }, 300)
}

const handleTouchPosition = (e) => {
  if (!containerRef.value || !e.touches[0]) return
  
  const rect = containerRef.value.getBoundingClientRect()
  mouseX.value = e.touches[0].clientX - rect.left
  mouseY.value = e.touches[0].clientY - rect.top
}

// 窗口大小变化时更新图片尺寸
const handleResize = () => {
  updateImageRect()
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (touchTimeout) {
    clearTimeout(touchTimeout)
  }
})
</script>

<style scoped>
.image-magnifier-container {
  position: relative;
  display: inline-block;
  width: 100%;
  height: 100%;
  cursor: crosshair;
}

.magnifier-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.magnifier-lens {
  position: absolute;
  border: 2px solid #409eff;
  border-radius: 4px;
  background: rgba(64, 158, 255, 0.1);
  pointer-events: none;
  z-index: 10;
  transition: opacity 0.15s ease;
}

.magnifier-preview {
  position: absolute;
  left: calc(100% + 20px);
  top: 0;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

.preview-image {
  display: block;
  max-width: none;
  transition: transform 0.05s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 放大镜提示 */
.magnifier-hint {
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
  pointer-events: none;
  opacity: 0.8;
  transition: opacity 0.3s;
}

.image-magnifier-container:hover .magnifier-hint {
  opacity: 0;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .magnifier-preview {
    display: none;
  }
  
  .magnifier-lens {
    display: none;
  }
  
  .magnifier-hint {
    display: none;
  }
  
  .image-magnifier-container {
    cursor: pointer;
  }
}

/* 当预览区域超出右侧边界时，显示在左侧 */
@media (max-width: 1200px) {
  .magnifier-preview {
    left: auto;
    right: calc(100% + 20px);
  }
}
</style>
