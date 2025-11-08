<!--
 * @Author: lingbai
 * @Date: 2025-01-27 15:35:00
 * @LastEditTime: 2025-01-27 15:35:00
 * @Description: 本地图标组件 - 用于显示本地矢量图标，支持动态加载、大小调节和颜色滤镜
 * @FilePath: /frontend/src/components/LocalIcon.vue
-->
<template>
  <img 
    :src="iconSrc" 
    :alt="name"
    :style="iconStyle"
    class="local-icon"
    @error="handleImageError"
    @load="handleImageLoad"
  />
</template>

<script setup>
import { computed, ref } from 'vue'

/**
 * LocalIcon 组件属性定义
 * @author lingbai
 * @since 2025-01-27
 */
const props = defineProps({
  /**
   * 图标名称，对应 /public/icons/ 目录下的图标文件名（不含扩展名）
   */
  name: {
    type: String,
    required: true,
    validator: (value) => {
      // 验证图标名称格式，只允许字母、数字、下划线和连字符
      return /^[a-zA-Z0-9_-]+$/.test(value)
    }
  },
  /**
   * 图标大小，支持数字（像素）或字符串格式
   */
  size: {
    type: [String, Number],
    default: 24,
    validator: (value) => {
      // 如果是数字，必须大于0
      if (typeof value === 'number') {
        return value > 0
      }
      // 如果是字符串，验证CSS尺寸格式
      return /^\d+(\.\d+)?(px|em|rem|%|vw|vh)$/.test(value)
    }
  },
  /**
   * 颜色滤镜，支持预定义颜色名称或十六进制颜色值
   */
  color: {
    type: String,
    default: '',
    validator: (value) => {
      if (!value) return true
      // 支持预定义颜色名称或十六进制颜色值
      const predefinedColors = ['red', 'blue', 'green', 'orange', 'purple', 'yellow', 'gray', 'black', 'white']
      const hexColorRegex = /^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$/
      return predefinedColors.includes(value.toLowerCase()) || hexColorRegex.test(value)
    }
  }
})

// 图标加载状态
const imageLoaded = ref(false)
const imageError = ref(false)

/**
 * 计算图标源路径
 * 优先尝试 PNG 格式，如果不存在则尝试 SVG 格式
 * @author lingbai
 * @since 2025-01-27
 */
const iconSrc = computed(() => {
  // 默认使用 PNG 格式
  return `/icons/${props.name}.png`
})

/**
 * 计算图标样式
 * 包括尺寸设置和颜色滤镜
 * @author lingbai
 * @since 2025-01-27
 */
const iconStyle = computed(() => {
  const style = {
    display: 'inline-block',
    verticalAlign: 'middle',
    transition: 'all 0.3s ease', // 添加过渡动画
    userSelect: 'none' // 禁止选择
  }

  // 设置尺寸
  if (typeof props.size === 'number') {
    style.width = `${props.size}px`
    style.height = `${props.size}px`
  } else {
    style.width = props.size
    style.height = props.size
  }

  // 设置颜色滤镜
  if (props.color) {
    style.filter = getColorFilter(props.color)
  }

  // 如果图片加载失败，添加错误样式
  if (imageError.value) {
    style.backgroundColor = '#f5f5f5'
    style.border = '1px dashed #ddd'
    style.borderRadius = '4px'
  }

  return style
})

/**
 * 根据颜色值生成CSS滤镜
 * 支持预定义颜色名称和十六进制颜色值
 * @param {string} color - 颜色值
 * @returns {string} CSS滤镜字符串
 * @author lingbai
 * @since 2025-01-27
 */
const getColorFilter = (color) => {
  // 预定义颜色映射表
  const colorMap = {
    // 基础颜色
    red: 'hue-rotate(0deg) saturate(1.5) brightness(1)',
    blue: 'hue-rotate(240deg) saturate(1.5) brightness(1)',
    green: 'hue-rotate(120deg) saturate(1.5) brightness(1)',
    orange: 'hue-rotate(30deg) saturate(1.5) brightness(1)',
    purple: 'hue-rotate(270deg) saturate(1.5) brightness(1)',
    yellow: 'hue-rotate(60deg) saturate(1.5) brightness(1)',
    
    // 灰度和单色
    gray: 'grayscale(1) brightness(0.8)',
    black: 'brightness(0) saturate(0)',
    white: 'brightness(2) saturate(0)',
    
    // 常用UI颜色
    primary: 'hue-rotate(210deg) saturate(1.5) brightness(1)', // Element Plus 主色
    success: 'hue-rotate(120deg) saturate(1.3) brightness(1.1)',
    warning: 'hue-rotate(45deg) saturate(1.5) brightness(1.1)',
    danger: 'hue-rotate(0deg) saturate(1.8) brightness(1)',
    info: 'hue-rotate(200deg) saturate(1.2) brightness(1)'
  }

  // 如果是预定义颜色，直接返回对应滤镜
  const lowerColor = color.toLowerCase()
  if (colorMap[lowerColor]) {
    return colorMap[lowerColor]
  }

  // 如果是十六进制颜色，转换为HSL并生成滤镜
  if (/^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$/.test(color)) {
    const hue = hexToHue(color)
    return `hue-rotate(${hue}deg) saturate(1.2) brightness(1)`
  }

  // 默认返回无滤镜
  return 'none'
}

/**
 * 将十六进制颜色转换为色相值
 * @param {string} hex - 十六进制颜色值
 * @returns {number} 色相值（0-360度）
 * @author lingbai
 * @since 2025-01-27
 */
const hexToHue = (hex) => {
  // 移除 # 符号
  hex = hex.replace('#', '')
  
  // 如果是3位十六进制，扩展为6位
  if (hex.length === 3) {
    hex = hex.split('').map(char => char + char).join('')
  }
  
  // 转换为RGB
  const r = parseInt(hex.substr(0, 2), 16) / 255
  const g = parseInt(hex.substr(2, 2), 16) / 255
  const b = parseInt(hex.substr(4, 2), 16) / 255
  
  // 计算HSL中的H值
  const max = Math.max(r, g, b)
  const min = Math.min(r, g, b)
  const diff = max - min
  
  let hue = 0
  if (diff !== 0) {
    switch (max) {
      case r:
        hue = ((g - b) / diff) % 6
        break
      case g:
        hue = (b - r) / diff + 2
        break
      case b:
        hue = (r - g) / diff + 4
        break
    }
  }
  
  return Math.round(hue * 60)
}

/**
 * 处理图片加载成功事件
 * @author lingbai
 * @since 2025-01-27
 */
const handleImageLoad = () => {
  imageLoaded.value = true
  imageError.value = false
}

/**
 * 处理图片加载失败事件
 * 当PNG格式加载失败时，可以尝试其他格式或显示占位符
 * @author lingbai
 * @since 2025-01-27
 */
const handleImageError = () => {
  imageError.value = true
  imageLoaded.value = false
  
  // 在开发环境下输出警告信息
  if (import.meta.env.DEV) {
    console.warn(`LocalIcon: 无法加载图标 "${props.name}"，请检查 /public/icons/${props.name}.png 文件是否存在`)
  }
}
</script>

<style scoped>
/**
 * LocalIcon 组件样式
 * @author lingbai
 * @since 2025-01-27
 */
.local-icon {
  /* 基础样式 */
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  
  /* 防止图片拖拽 */
  -webkit-user-drag: none;
  -khtml-user-drag: none;
  -moz-user-drag: none;
  -o-user-drag: none;
  user-drag: none;
  
  /* 平滑渲染 */
  image-rendering: -webkit-optimize-contrast;
  image-rendering: crisp-edges;
  
  /* 响应式处理 */
  @media (max-width: 768px) {
    /* 在小屏幕设备上稍微缩小图标 */
    transform: scale(0.9);
  }
}

/* 悬停效果（可选） */
.local-icon:hover {
  opacity: 0.8;
  transform: scale(1.05);
}

/* 加载失败时的占位样式 */
.local-icon[src=""] {
  background-color: #f5f5f5;
  border: 1px dashed #ddd;
  border-radius: 4px;
  position: relative;
}

.local-icon[src=""]:before {
  content: "?";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #999;
  font-size: 12px;
}
</style>