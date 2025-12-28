<template>
  <el-dialog
    v-model="visible"
    title="裁剪图片"
    width="900px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    destroy-on-close
    @open="handleOpen"
    @closed="handleClosed"
  >
    <div class="cropper-main">
      <!-- 裁剪画布区域 -->
      <div 
        class="canvas-container" 
        ref="containerRef"
        @mousedown="handleMouseDown"
        @mousemove="handleMouseMove"
        @mouseup="handleMouseUp"
        @mouseleave="handleMouseUp"
        @wheel.prevent="handleWheel"
      >
        <canvas ref="canvasRef"></canvas>
        
        <!-- 裁剪框 -->
        <div 
          class="crop-box"
          :style="cropBoxStyle"
        >
          <div class="crop-grid">
            <div class="grid-line horizontal" style="top: 33.33%"></div>
            <div class="grid-line horizontal" style="top: 66.66%"></div>
            <div class="grid-line vertical" style="left: 33.33%"></div>
            <div class="grid-line vertical" style="left: 66.66%"></div>
          </div>
          <div class="crop-border"></div>
          <!-- 四角拖拽点 -->
          <div class="resize-handle nw" @mousedown.stop="startResize('nw', $event)"></div>
          <div class="resize-handle ne" @mousedown.stop="startResize('ne', $event)"></div>
          <div class="resize-handle sw" @mousedown.stop="startResize('sw', $event)"></div>
          <div class="resize-handle se" @mousedown.stop="startResize('se', $event)"></div>
        </div>
        
        <!-- 遮罩层 -->
        <div class="mask-overlay">
          <div class="mask-top" :style="maskTopStyle"></div>
          <div class="mask-bottom" :style="maskBottomStyle"></div>
          <div class="mask-left" :style="maskLeftStyle"></div>
          <div class="mask-right" :style="maskRightStyle"></div>
        </div>
      </div>
      
      <!-- 信息栏 -->
      <div class="info-bar">
        <div class="info-item">
          <span class="label">原图：</span>
          <span class="value">{{ originalSize }}</span>
        </div>
        <div class="info-item">
          <span class="label">输出：</span>
          <span class="value">{{ outputWidth }} × {{ outputHeight }} px</span>
        </div>
        <div class="info-item">
          <span class="label">缩放：</span>
          <span class="value">{{ Math.round(scale * 100) }}%</span>
        </div>
        <div class="info-tip">
          <el-icon><InfoFilled /></el-icon>
          滚轮缩放 · 拖动图片移动 · 拖动角点调整裁剪框
        </div>
      </div>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <div class="footer-left">
          <el-button @click="resetView">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
          <el-button @click="fitToContainer">适应窗口</el-button>
          <el-button @click="rotate(-90)">左旋90°</el-button>
          <el-button @click="rotate(90)">右旋90°</el-button>
        </div>
        <div class="footer-right">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" @click="handleConfirm" :loading="processing">
            确认裁剪
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, nextTick, onBeforeUnmount } from 'vue'
import { InfoFilled, RefreshLeft } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  imageSrc: { type: String, default: '' },
  outputWidth: { type: Number, default: 1920 },
  outputHeight: { type: Number, default: 600 },
  outputType: { type: String, default: 'image/jpeg' },
  outputQuality: { type: Number, default: 0.92 }
})

const emit = defineEmits(['update:modelValue', 'crop', 'cancel'])

const visible = ref(false)
const containerRef = ref(null)
const canvasRef = ref(null)
const processing = ref(false)
const originalSize = ref('')

// 图片状态
const imageObj = ref(null)
const scale = ref(1)
const rotation = ref(0)
const offsetX = ref(0)
const offsetY = ref(0)

// 裁剪框状态
const cropBox = ref({ x: 0, y: 0, width: 0, height: 0 })

// 拖拽状态
const isDragging = ref(false)
const isResizing = ref(false)
const resizeHandle = ref('')
const dragStart = ref({ x: 0, y: 0 })
const dragStartOffset = ref({ x: 0, y: 0 })
const dragStartCrop = ref({ x: 0, y: 0, width: 0, height: 0 })

// 宽高比
const aspectRatio = computed(() => props.outputWidth / props.outputHeight)

// 裁剪框样式
const cropBoxStyle = computed(() => ({
  left: `${cropBox.value.x}px`,
  top: `${cropBox.value.y}px`,
  width: `${cropBox.value.width}px`,
  height: `${cropBox.value.height}px`
}))

// 遮罩样式
const maskTopStyle = computed(() => ({
  height: `${cropBox.value.y}px`
}))
const maskBottomStyle = computed(() => ({
  top: `${cropBox.value.y + cropBox.value.height}px`,
  height: `calc(100% - ${cropBox.value.y + cropBox.value.height}px)`
}))
const maskLeftStyle = computed(() => ({
  top: `${cropBox.value.y}px`,
  width: `${cropBox.value.x}px`,
  height: `${cropBox.value.height}px`
}))
const maskRightStyle = computed(() => ({
  top: `${cropBox.value.y}px`,
  left: `${cropBox.value.x + cropBox.value.width}px`,
  width: `calc(100% - ${cropBox.value.x + cropBox.value.width}px)`,
  height: `${cropBox.value.height}px`
}))

watch(() => props.modelValue, (val) => { visible.value = val })
watch(visible, (val) => { emit('update:modelValue', val) })

const handleOpen = () => {
  nextTick(() => {
    setTimeout(loadImage, 100)
  })
}

const loadImage = () => {
  if (!props.imageSrc) return
  
  const img = new Image()
  img.crossOrigin = 'anonymous'
  img.onload = () => {
    imageObj.value = img
    originalSize.value = `${img.naturalWidth} × ${img.naturalHeight}`
    initCanvas()
  }
  img.src = props.imageSrc
}

const initCanvas = () => {
  if (!containerRef.value || !canvasRef.value || !imageObj.value) return
  
  const container = containerRef.value
  const canvas = canvasRef.value
  const img = imageObj.value
  
  // 设置画布大小
  canvas.width = container.clientWidth
  canvas.height = container.clientHeight
  
  // 计算初始缩放，让图片填满容器
  const scaleX = canvas.width / img.naturalWidth
  const scaleY = canvas.height / img.naturalHeight
  scale.value = Math.max(scaleX, scaleY) * 0.85
  
  // 居中图片
  offsetX.value = (canvas.width - img.naturalWidth * scale.value) / 2
  offsetY.value = (canvas.height - img.naturalHeight * scale.value) / 2
  
  // 初始化裁剪框（居中，尽量大）
  initCropBox()
  
  // 绘制
  draw()
}

const initCropBox = () => {
  if (!canvasRef.value) return
  
  const canvas = canvasRef.value
  const containerWidth = canvas.width
  const containerHeight = canvas.height
  
  // 计算裁剪框大小（保持宽高比，尽量大）
  let cropWidth, cropHeight
  
  if (containerWidth / containerHeight > aspectRatio.value) {
    // 容器更宽，以高度为基准
    cropHeight = containerHeight * 0.7
    cropWidth = cropHeight * aspectRatio.value
  } else {
    // 容器更高，以宽度为基准
    cropWidth = containerWidth * 0.85
    cropHeight = cropWidth / aspectRatio.value
  }
  
  // 居中
  cropBox.value = {
    x: (containerWidth - cropWidth) / 2,
    y: (containerHeight - cropHeight) / 2,
    width: cropWidth,
    height: cropHeight
  }
}

const draw = () => {
  if (!canvasRef.value || !imageObj.value) return
  
  const canvas = canvasRef.value
  const ctx = canvas.getContext('2d')
  const img = imageObj.value
  
  // 清空画布
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  
  // 保存状态
  ctx.save()
  
  // 移动到图片中心
  const centerX = offsetX.value + (img.naturalWidth * scale.value) / 2
  const centerY = offsetY.value + (img.naturalHeight * scale.value) / 2
  
  ctx.translate(centerX, centerY)
  ctx.rotate((rotation.value * Math.PI) / 180)
  ctx.translate(-centerX, -centerY)
  
  // 绘制图片
  ctx.drawImage(
    img,
    offsetX.value,
    offsetY.value,
    img.naturalWidth * scale.value,
    img.naturalHeight * scale.value
  )
  
  ctx.restore()
}

// 鼠标事件处理
const handleMouseDown = (e) => {
  if (isResizing.value) return
  
  isDragging.value = true
  dragStart.value = { x: e.clientX, y: e.clientY }
  dragStartOffset.value = { x: offsetX.value, y: offsetY.value }
}

const handleMouseMove = (e) => {
  if (isDragging.value) {
    const dx = e.clientX - dragStart.value.x
    const dy = e.clientY - dragStart.value.y
    offsetX.value = dragStartOffset.value.x + dx
    offsetY.value = dragStartOffset.value.y + dy
    draw()
  } else if (isResizing.value) {
    handleResize(e)
  }
}

const handleMouseUp = () => {
  isDragging.value = false
  isResizing.value = false
  resizeHandle.value = ''
}

// 滚轮缩放
const handleWheel = (e) => {
  const delta = e.deltaY > 0 ? 0.9 : 1.1
  const newScale = Math.max(0.1, Math.min(5, scale.value * delta))
  
  // 以鼠标位置为中心缩放
  const rect = containerRef.value.getBoundingClientRect()
  const mouseX = e.clientX - rect.left
  const mouseY = e.clientY - rect.top
  
  const ratio = newScale / scale.value
  offsetX.value = mouseX - (mouseX - offsetX.value) * ratio
  offsetY.value = mouseY - (mouseY - offsetY.value) * ratio
  
  scale.value = newScale
  draw()
}

// 裁剪框调整
const startResize = (handle, e) => {
  isResizing.value = true
  resizeHandle.value = handle
  dragStart.value = { x: e.clientX, y: e.clientY }
  dragStartCrop.value = { ...cropBox.value }
}

const handleResize = (e) => {
  if (!containerRef.value) return
  
  const dx = e.clientX - dragStart.value.x
  const dy = e.clientY - dragStart.value.y
  const container = containerRef.value
  const minSize = 50
  
  let newX = dragStartCrop.value.x
  let newY = dragStartCrop.value.y
  let newWidth = dragStartCrop.value.width
  let newHeight = dragStartCrop.value.height
  
  switch (resizeHandle.value) {
    case 'se':
      newWidth = Math.max(minSize, dragStartCrop.value.width + dx)
      newHeight = newWidth / aspectRatio.value
      break
    case 'sw':
      newWidth = Math.max(minSize, dragStartCrop.value.width - dx)
      newHeight = newWidth / aspectRatio.value
      newX = dragStartCrop.value.x + dragStartCrop.value.width - newWidth
      break
    case 'ne':
      newWidth = Math.max(minSize, dragStartCrop.value.width + dx)
      newHeight = newWidth / aspectRatio.value
      newY = dragStartCrop.value.y + dragStartCrop.value.height - newHeight
      break
    case 'nw':
      newWidth = Math.max(minSize, dragStartCrop.value.width - dx)
      newHeight = newWidth / aspectRatio.value
      newX = dragStartCrop.value.x + dragStartCrop.value.width - newWidth
      newY = dragStartCrop.value.y + dragStartCrop.value.height - newHeight
      break
  }
  
  // 边界检查
  newX = Math.max(0, Math.min(container.clientWidth - newWidth, newX))
  newY = Math.max(0, Math.min(container.clientHeight - newHeight, newY))
  
  cropBox.value = { x: newX, y: newY, width: newWidth, height: newHeight }
}

// 旋转
const rotate = (deg) => {
  rotation.value = (rotation.value + deg) % 360
  draw()
}

// 重置
const resetView = () => {
  rotation.value = 0
  initCanvas()
}

// 适应窗口
const fitToContainer = () => {
  if (!canvasRef.value || !imageObj.value) return
  
  const canvas = canvasRef.value
  const img = imageObj.value
  
  const scaleX = canvas.width / img.naturalWidth
  const scaleY = canvas.height / img.naturalHeight
  scale.value = Math.min(scaleX, scaleY) * 0.9
  
  offsetX.value = (canvas.width - img.naturalWidth * scale.value) / 2
  offsetY.value = (canvas.height - img.naturalHeight * scale.value) / 2
  
  draw()
}

// 确认裁剪
const handleConfirm = () => {
  if (!imageObj.value) return
  
  processing.value = true
  
  try {
    const img = imageObj.value
    const outputCanvas = document.createElement('canvas')
    outputCanvas.width = props.outputWidth
    outputCanvas.height = props.outputHeight
    const ctx = outputCanvas.getContext('2d')
    
    // 计算裁剪区域在原图上的位置
    const cropX = (cropBox.value.x - offsetX.value) / scale.value
    const cropY = (cropBox.value.y - offsetY.value) / scale.value
    const cropW = cropBox.value.width / scale.value
    const cropH = cropBox.value.height / scale.value
    
    // 处理旋转
    if (rotation.value !== 0) {
      const tempCanvas = document.createElement('canvas')
      const tempCtx = tempCanvas.getContext('2d')
      
      if (rotation.value === 90 || rotation.value === -270) {
        tempCanvas.width = img.naturalHeight
        tempCanvas.height = img.naturalWidth
        tempCtx.translate(tempCanvas.width, 0)
        tempCtx.rotate(Math.PI / 2)
      } else if (rotation.value === -90 || rotation.value === 270) {
        tempCanvas.width = img.naturalHeight
        tempCanvas.height = img.naturalWidth
        tempCtx.translate(0, tempCanvas.height)
        tempCtx.rotate(-Math.PI / 2)
      } else if (Math.abs(rotation.value) === 180) {
        tempCanvas.width = img.naturalWidth
        tempCanvas.height = img.naturalHeight
        tempCtx.translate(tempCanvas.width, tempCanvas.height)
        tempCtx.rotate(Math.PI)
      }
      
      tempCtx.drawImage(img, 0, 0)
      ctx.drawImage(tempCanvas, cropX, cropY, cropW, cropH, 0, 0, props.outputWidth, props.outputHeight)
    } else {
      ctx.drawImage(img, cropX, cropY, cropW, cropH, 0, 0, props.outputWidth, props.outputHeight)
    }
    
    outputCanvas.toBlob((blob) => {
      processing.value = false
      if (blob) {
        emit('crop', blob)
        visible.value = false
      }
    }, props.outputType, props.outputQuality)
  } catch (error) {
    console.error('Crop error:', error)
    processing.value = false
  }
}

const handleCancel = () => {
  visible.value = false
  emit('cancel')
}

const handleClosed = () => {
  imageObj.value = null
  scale.value = 1
  rotation.value = 0
  offsetX.value = 0
  offsetY.value = 0
  originalSize.value = ''
}

onBeforeUnmount(() => {
  imageObj.value = null
})
</script>

<style scoped>
.cropper-main {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.canvas-container {
  position: relative;
  width: 100%;
  height: 500px;
  background: #1a1a1a;
  border-radius: 8px;
  overflow: hidden;
  cursor: move;
}

.canvas-container canvas {
  display: block;
  width: 100%;
  height: 100%;
}

/* 裁剪框 */
.crop-box {
  position: absolute;
  border: 2px solid #52c41a;
  box-sizing: border-box;
  pointer-events: none;
  z-index: 10;
}

.crop-border {
  position: absolute;
  inset: 0;
  border: 1px dashed rgba(255, 255, 255, 0.5);
}

.crop-grid .grid-line {
  position: absolute;
  background: rgba(255, 255, 255, 0.3);
}

.crop-grid .grid-line.horizontal {
  left: 0;
  right: 0;
  height: 1px;
}

.crop-grid .grid-line.vertical {
  top: 0;
  bottom: 0;
  width: 1px;
}

/* 拖拽点 */
.resize-handle {
  position: absolute;
  width: 16px;
  height: 16px;
  background: #52c41a;
  border: 2px solid #fff;
  border-radius: 50%;
  pointer-events: auto;
  cursor: nwse-resize;
  z-index: 11;
}

.resize-handle.nw { top: -8px; left: -8px; cursor: nwse-resize; }
.resize-handle.ne { top: -8px; right: -8px; cursor: nesw-resize; }
.resize-handle.sw { bottom: -8px; left: -8px; cursor: nesw-resize; }
.resize-handle.se { bottom: -8px; right: -8px; cursor: nwse-resize; }

/* 遮罩 */
.mask-overlay {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 5;
}

.mask-overlay > div {
  position: absolute;
  background: rgba(0, 0, 0, 0.6);
}

.mask-top { top: 0; left: 0; right: 0; }
.mask-bottom { left: 0; right: 0; }
.mask-left { left: 0; }
.mask-right { }

/* 信息栏 */
.info-bar {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 10px 16px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 13px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.info-item .label { color: #909399; }
.info-item .value { color: #303133; font-weight: 500; }

.info-tip {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: auto;
  color: #909399;
  font-size: 12px;
}

/* 底部按钮 */
.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-left, .footer-right {
  display: flex;
  gap: 8px;
}
</style>
