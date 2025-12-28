<template>
  <div class="home-banner-carousel">
    <!-- 加载状态 -->
    <div v-if="loading" class="carousel-loading">
      <el-skeleton :rows="0" animated>
        <template #template>
          <el-skeleton-item variant="image" style="width: 100%; height: 400px; border-radius: 8px;" />
        </template>
      </el-skeleton>
    </div>
    
    <!-- 轮播图内容 -->
    <el-carousel
      v-else
      ref="carouselRef"
      :height="height"
      :interval="autoplayInterval"
      :autoplay="autoplay && displayBanners.length > 1"
      indicator-position="outside"
      arrow="hover"
      @change="handleCarouselChange"
    >
      <!-- 有数据时显示轮播图 -->
      <el-carousel-item 
        v-for="(banner, index) in displayBanners" 
        :key="banner.id || index"
      >
        <div 
          class="banner-item" 
          @click="handleBannerClick(banner, index)"
          :style="{ cursor: banner.targetUrl ? 'pointer' : 'default' }"
        >
          <img 
            :src="banner.imageUrl" 
            :alt="banner.title || '轮播图'"
            class="banner-image"
            @error="handleImageError($event, index)"
          />
          <!-- 标题叠加层 -->
          <div v-if="banner.title" class="banner-overlay">
            <h3 class="banner-title">{{ banner.title }}</h3>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// 组件属性
const props = defineProps({
  // 轮播图高度
  height: {
    type: String,
    default: '400px'
  },
  // 自动轮播间隔（毫秒）
  autoplayInterval: {
    type: Number,
    default: 5000
  },
  // 是否自动轮播
  autoplay: {
    type: Boolean,
    default: true
  }
})

// 事件
const emit = defineEmits(['click', 'change', 'load', 'error'])

// 轮播图引用
const carouselRef = ref(null)

// 状态
const banners = ref([])
const loading = ref(true)
const currentIndex = ref(0)
const impressionTracked = ref(new Set()) // 已追踪曝光的轮播图ID
const failedImages = ref(new Set()) // 已失败的图片索引，防止无限循环

// 内联占位图 (灰色渐变背景 + 图标)
const PLACEHOLDER_IMAGE = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTkyMCIgaGVpZ2h0PSI2MDAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGRlZnM+PGxpbmVhckdyYWRpZW50IGlkPSJnIiB4MT0iMCUiIHkxPSIwJSIgeDI9IjEwMCUiIHkyPSIxMDAlIj48c3RvcCBvZmZzZXQ9IjAlIiBzdHlsZT0ic3RvcC1jb2xvcjojZjVmNWY1O3N0b3Atb3BhY2l0eToxIi8+PHN0b3Agb2Zmc2V0PSIxMDAlIiBzdHlsZT0ic3RvcC1jb2xvcjojZTBlMGUwO3N0b3Atb3BhY2l0eToxIi8+PC9saW5lYXJHcmFkaWVudD48L2RlZnM+PHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0idXJsKCNnKSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LWZhbWlseT0iQXJpYWwsc2Fucy1zZXJpZiIgZm9udC1zaXplPSI0OCIgZmlsbD0iIzk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPui9ruaSreWbvjwvdGV4dD48L3N2Zz4='

// 默认轮播图（无数据时显示，使用内联占位图）
const defaultBanners = [
  {
    id: 'default-1',
    imageUrl: PLACEHOLDER_IMAGE,
    title: '欢迎来到在线商城',
    targetUrl: ''
  },
  {
    id: 'default-2',
    imageUrl: PLACEHOLDER_IMAGE,
    title: '精选好物等你发现',
    targetUrl: ''
  }
]

// 显示的轮播图（有数据用真实数据，无数据用默认）
const displayBanners = computed(() => {
  if (banners.value.length > 0) {
    return banners.value
  }
  return defaultBanners
})

/**
 * 加载活跃轮播图
 */
const loadBanners = async () => {
  loading.value = true
  
  try {
    const { getActiveBanners } = await import('@/api/banner')
    const response = await getActiveBanners()
    
    if (response.success && response.data) {
      banners.value = response.data
      emit('load', banners.value)
      
      if (banners.value.length > 0) {
        trackImpression(banners.value[0])
      }
    } else {
      console.warn('获取轮播图返回空数据，使用默认轮播图')
      banners.value = []
    }
  } catch (err) {
    console.warn('加载轮播图失败，使用默认轮播图:', err.message)
    banners.value = []
    emit('error', err)
  } finally {
    loading.value = false
  }
}

/**
 * 处理轮播图点击
 * @param {Object} banner 轮播图数据
 * @param {Number} index 索引
 */
const handleBannerClick = async (banner, index) => {
  // 触发点击事件
  emit('click', { banner, index })
  
  // 如果有跳转链接，记录点击并跳转
  if (banner.targetUrl) {
    // 跳转到目标URL
    navigateToTarget(banner.targetUrl)
  }
}

/**
 * 导航到目标URL
 * @param {String} targetUrl 目标URL
 */
const navigateToTarget = (targetUrl) => {
  if (!targetUrl) return
  
  // 判断是否为外部链接
  if (targetUrl.startsWith('http://') || targetUrl.startsWith('https://')) {
    window.open(targetUrl, '_blank')
  } else {
    // 内部链接，兼容旧格式 /product/detail/xxx -> /product/xxx
    let finalUrl = targetUrl
    if (targetUrl.startsWith('/product/detail/')) {
      finalUrl = targetUrl.replace('/product/detail/', '/product/')
    }
    // 使用当前窗口跳转
    window.location.href = finalUrl
  }
}

/**
 * 处理轮播图切换
 * @param {Number} index 当前索引
 */
const handleCarouselChange = (index) => {
  currentIndex.value = index
  emit('change', index)
  
  // 追踪曝光
  const banner = displayBanners.value[index]
  if (banner) {
    trackImpression(banner)
  }
}

/**
 * 追踪轮播图曝光
 * 注意：当前禁用曝光追踪，后端服务准备好后可启用
 * @param {Object} banner 轮播图数据
 */
const trackImpression = (banner) => {
  // 默认轮播图不追踪
  if (!banner.id || String(banner.id).startsWith('default')) {
    return
  }
  
  // 避免重复追踪
  if (impressionTracked.value.has(banner.id)) {
    return
  }
  
  impressionTracked.value.add(banner.id)
  
  // 曝光追踪暂时禁用
  // recordBannerImpression(banner.id).catch(err => {
  //   console.warn('记录曝光失败:', err)
  // })
}

/**
 * 处理图片加载错误
 * @param {Event} event 事件对象
 * @param {Number} index 索引
 */
const handleImageError = (event, index) => {
  // 检查是否已经处理过该图片，防止无限循环
  if (failedImages.value.has(index)) {
    return
  }
  
  // 标记该图片已失败
  failedImages.value.add(index)
  
  // 使用内联SVG占位图替换
  event.target.src = PLACEHOLDER_IMAGE
}

/**
 * 手动切换到指定索引
 * @param {Number} index 目标索引
 */
const setActiveItem = (index) => {
  if (carouselRef.value) {
    carouselRef.value.setActiveItem(index)
  }
}

/**
 * 切换到上一张
 */
const prev = () => {
  if (carouselRef.value) {
    carouselRef.value.prev()
  }
}

/**
 * 切换到下一张
 */
const next = () => {
  if (carouselRef.value) {
    carouselRef.value.next()
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadBanners()
})

// 暴露方法给父组件
defineExpose({
  loadBanners,
  setActiveItem,
  prev,
  next,
  banners,
  currentIndex
})
</script>

<style scoped>
.home-banner-carousel {
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
}

/* 加载状态 */
.carousel-loading {
  width: 100%;
}

/* 轮播图项 */
.banner-item {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.banner-item:hover .banner-image {
  transform: scale(1.02);
}

/* 标题叠加层 */
.banner-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  padding: 40px 30px 30px;
  pointer-events: none;
}

.banner-title {
  color: white;
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  line-height: 1.4;
}

/* 自定义轮播图样式 */
:deep(.el-carousel__container) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-carousel__item) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-carousel__arrow) {
  background-color: rgba(0, 0, 0, 0.3);
  transition: background-color 0.3s;
}

:deep(.el-carousel__arrow:hover) {
  background-color: rgba(0, 0, 0, 0.5);
}

:deep(.el-carousel__indicators--outside) {
  margin-top: 12px;
}

:deep(.el-carousel__indicator--outside button) {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: #dcdfe6;
  opacity: 1;
}

:deep(.el-carousel__indicator--outside.is-active button) {
  width: 24px;
  border-radius: 5px;
  background-color: #e3101e;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .banner-overlay {
    padding: 30px 20px 20px;
  }
  
  .banner-title {
    font-size: 18px;
  }
}

@media (max-width: 480px) {
  .banner-overlay {
    padding: 20px 15px 15px;
  }
  
  .banner-title {
    font-size: 16px;
  }
}
</style>
