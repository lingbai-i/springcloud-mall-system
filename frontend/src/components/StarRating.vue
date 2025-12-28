<template>
  <div 
    class="star-rating"
    :class="{ disabled: disabled }"
    role="slider"
    :aria-label="label || '评分'"
    :aria-valuenow="modelValue"
    :aria-valuemin="1"
    :aria-valuemax="5"
    :aria-disabled="disabled"
  >
    <span v-if="label" class="star-rating-label">{{ label }}</span>
    <div class="stars-container" ref="starsContainer">
      <span
        v-for="star in 5"
        :key="star"
        class="star"
        :class="{ 
          active: star <= (hoverValue || modelValue),
          hovering: hoverValue > 0
        }"
        :style="{ color: getStarColor(star) }"
        role="button"
        :aria-label="`${star}星`"
        :tabindex="disabled ? -1 : 0"
        @mouseenter="handleMouseEnter(star, $event)"
        @mousemove="handleMouseMove($event)"
        @mouseleave="handleMouseLeave"
        @click="handleClick(star)"
        @keydown.enter="handleClick(star)"
        @keydown.space.prevent="handleClick(star)"
        @touchstart="handleTouchStart(star, $event)"
        @touchmove="handleTouchMove($event)"
        @touchend="handleTouchEnd"
      >
        ★
      </span>
      <!-- 悬停提示框 -->
      <div 
        v-show="hoverValue > 0 && !disabled"
        class="star-tooltip"
        :style="tooltipStyle"
        role="tooltip"
      >
        {{ starConfig[hoverValue]?.text }}
      </div>
    </div>
    <span v-if="showText && modelValue > 0" class="rating-text" :style="{ color: getStarColor(modelValue) }">
      {{ starConfig[modelValue]?.text }}
    </span>
  </div>
</template>

<script setup>
import { ref, computed, defineProps, defineEmits } from 'vue'

const props = defineProps({
  modelValue: {
    type: Number,
    default: 0
  },
  disabled: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'default',
    validator: (value) => ['small', 'default', 'large'].includes(value)
  },
  label: {
    type: String,
    default: ''
  },
  showText: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

// 星级配置
const starConfig = {
  1: { text: '1分 失望', color: '#808080' },
  2: { text: '2分 不满', color: '#808080' },
  3: { text: '3分 一般', color: '#FFD700' },
  4: { text: '4分 满意', color: '#FFD700' },
  5: { text: '5分 惊喜', color: '#FF0000' }
}

// 响应式状态
const hoverValue = ref(0)
const tooltipPosition = ref({ x: 0, y: 0 })
const starsContainer = ref(null)

// 获取星星颜色
const getStarColor = (star) => {
  const activeValue = hoverValue.value || props.modelValue
  if (star <= activeValue) {
    return starConfig[activeValue]?.color || '#ccc'
  }
  return '#ccc'
}

// 提示框样式
const tooltipStyle = computed(() => {
  const config = starConfig[hoverValue.value]
  return {
    left: `${tooltipPosition.value.x}px`,
    top: `${tooltipPosition.value.y}px`,
    backgroundColor: config?.color || '#808080',
    color: '#fff'
  }
})

// 鼠标进入星星
const handleMouseEnter = (star, event) => {
  if (props.disabled) return
  hoverValue.value = star
  updateTooltipPosition(event)
}

// 鼠标移动
const handleMouseMove = (event) => {
  if (props.disabled || hoverValue.value === 0) return
  updateTooltipPosition(event)
}

// 鼠标离开
const handleMouseLeave = () => {
  if (props.disabled) return
  hoverValue.value = 0
}

// 更新提示框位置
const updateTooltipPosition = (event) => {
  if (!starsContainer.value) return
  const containerRect = starsContainer.value.getBoundingClientRect()
  tooltipPosition.value = {
    x: event.clientX - containerRect.left,
    y: -35
  }
}

// 点击星星
const handleClick = (star) => {
  if (props.disabled) return
  emit('update:modelValue', star)
  emit('change', star)
}

// 触摸开始（移动端支持）
const handleTouchStart = (star, event) => {
  if (props.disabled) return
  event.preventDefault()
  hoverValue.value = star
  const touch = event.touches[0]
  if (starsContainer.value) {
    const containerRect = starsContainer.value.getBoundingClientRect()
    tooltipPosition.value = {
      x: touch.clientX - containerRect.left,
      y: -35
    }
  }
}

// 触摸移动
const handleTouchMove = (event) => {
  if (props.disabled) return
  event.preventDefault()
  const touch = event.touches[0]
  if (!starsContainer.value) return
  
  const containerRect = starsContainer.value.getBoundingClientRect()
  const stars = starsContainer.value.querySelectorAll('.star')
  
  // 计算触摸位置对应的星级
  for (let i = 0; i < stars.length; i++) {
    const starRect = stars[i].getBoundingClientRect()
    if (touch.clientX >= starRect.left && touch.clientX <= starRect.right) {
      hoverValue.value = i + 1
      tooltipPosition.value = {
        x: touch.clientX - containerRect.left,
        y: -35
      }
      break
    }
  }
}

// 触摸结束
const handleTouchEnd = () => {
  if (props.disabled) return
  if (hoverValue.value > 0) {
    emit('update:modelValue', hoverValue.value)
    emit('change', hoverValue.value)
  }
  hoverValue.value = 0
}
</script>

<style scoped>
.star-rating {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  user-select: none;
}

.star-rating.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.star-rating-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.stars-container {
  position: relative;
  display: inline-flex;
  gap: 4px;
}

.star {
  font-size: 24px;
  cursor: pointer;
  color: #ccc;
  transition: color 0.2s ease, transform 0.15s ease;
  line-height: 1;
}

.star-rating.disabled .star {
  cursor: not-allowed;
}

.star:hover:not(.star-rating.disabled .star) {
  transform: scale(1.1);
}

.star:focus {
  outline: 2px solid #409eff;
  outline-offset: 2px;
  border-radius: 2px;
}

.star.active {
  color: inherit;
}

/* 尺寸变体 */
.star-rating[data-size="small"] .star {
  font-size: 18px;
}

.star-rating[data-size="large"] .star {
  font-size: 32px;
}

/* 提示框样式 */
.star-tooltip {
  position: absolute;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 12px;
  white-space: nowrap;
  pointer-events: none;
  transform: translateX(-50%);
  z-index: 1000;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: background-color 0.2s ease;
}

.star-tooltip::after {
  content: '';
  position: absolute;
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
  border-width: 6px 6px 0;
  border-style: solid;
  border-color: inherit;
  border-left-color: transparent;
  border-right-color: transparent;
  border-bottom-color: transparent;
}

/* 评分文字 */
.rating-text {
  font-size: 14px;
  font-weight: 500;
  margin-left: 4px;
  transition: color 0.2s ease;
}

/* 响应式 */
@media (max-width: 768px) {
  .star {
    font-size: 28px;
    padding: 4px;
  }
  
  .star-tooltip {
    font-size: 14px;
    padding: 8px 14px;
  }
}
</style>
