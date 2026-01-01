<template>
  <div 
    class="product-card" 
    :class="{ 'product-card--hover': !disableHover }"
    @click="handleClick"
  >
    <!-- 商品图片区域 -->
    <div class="product-card__image-wrapper">
      <img 
        :src="imageUrl" 
        :alt="product.name || product.productName"
        class="product-card__image"
        @error="handleImageError"
      />
      <!-- 标签徽章 -->
      <div class="product-card__badges">
        <span v-if="product.isNew || product.is_new" class="badge badge--new">新品</span>
        <span v-if="product.isHot || product.is_hot" class="badge badge--hot">热卖</span>
        <span v-if="discountPercent > 0" class="badge badge--discount">-{{ discountPercent }}%</span>
      </div>
      <!-- 收藏按钮 -->
      <button 
        v-if="showFavorite"
        class="product-card__favorite"
        :class="{ 'is-favorited': isFavorited }"
        @click.stop="toggleFavorite"
      >
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
        </svg>
      </button>
    </div>

    <!-- 商品信息区域 -->
    <div class="product-card__content">
      <!-- 商品名称 -->
      <h3 class="product-card__title">
        {{ product.name || product.productName }}
      </h3>

      <!-- 商品描述 -->
      <p v-if="showDescription && productDescription" class="product-card__desc">
        {{ productDescription }}
      </p>

      <!-- 价格区域 -->
      <div class="product-card__price-row">
        <div class="product-card__price">
          <span class="price-symbol">¥</span>
          <span class="price-integer">{{ priceInteger }}</span>
          <span class="price-decimal">.{{ priceDecimal }}</span>
        </div>
        <span v-if="originalPrice && originalPrice > currentPrice" class="product-card__original-price">
          ¥{{ formatPrice(originalPrice) }}
        </span>
      </div>

      <!-- 统计信息 -->
      <div class="product-card__stats">
        <div class="stat-item">
          <span class="stat-label">已售</span>
          <span class="stat-value">{{ formatSales(salesCount) }}</span>
        </div>
        <div v-if="showRating && ratingValue" class="stat-item stat-item--rating">
          <div class="rating-stars">
            <svg v-for="i in 5" :key="i" class="star" :class="{ filled: i <= Math.round(ratingValue) }" viewBox="0 0 24 24">
              <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
            </svg>
          </div>
          <span class="rating-value">{{ ratingValue.toFixed(1) }}</span>
        </div>
      </div>

      <!-- 店铺信息（可选） -->
      <div v-if="showShop && product.shopName" class="product-card__shop">
        <span class="shop-icon">🏪</span>
        <span class="shop-name">{{ product.shopName }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  product: {
    type: Object,
    required: true
  },
  showFavorite: {
    type: Boolean,
    default: true
  },
  showDescription: {
    type: Boolean,
    default: true
  },
  showRating: {
    type: Boolean,
    default: true
  },
  showShop: {
    type: Boolean,
    default: false
  },
  disableHover: {
    type: Boolean,
    default: false
  },
  defaultImage: {
    type: String,
    default: '/images/placeholder.png'
  }
})

const emit = defineEmits(['click', 'favorite'])

// 图片加载失败标志
const imageLoadError = ref(false)

// 收藏状态
const isFavorited = ref(props.product.isFavorited || props.product.is_favorited || false)

// 计算属性：图片URL
const imageUrl = computed(() => {
  if (imageLoadError.value) return props.defaultImage
  let url = props.product.image || props.product.mainImage || props.product.main_image || ''
  // 处理逗号分隔的多图片URL，取第一个
  if (url && url.includes(',')) {
    url = url.split(',')[0].trim()
  }
  return url || props.defaultImage
})

// 计算属性：商品描述
const productDescription = computed(() => {
  return props.product.description || props.product.desc || ''
})

// 计算属性：当前价格
const currentPrice = computed(() => {
  return Number(props.product.price) || 0
})

// 计算属性：原价
const originalPrice = computed(() => {
  return Number(props.product.originalPrice || props.product.marketPrice || props.product.market_price) || 0
})

// 计算属性：价格整数部分
const priceInteger = computed(() => {
  return Math.floor(currentPrice.value)
})

// 计算属性：价格小数部分
const priceDecimal = computed(() => {
  const decimal = (currentPrice.value % 1).toFixed(2).substring(2)
  return decimal
})

// 计算属性：销量
const salesCount = computed(() => {
  return props.product.sales || props.product.salesCount || props.product.sales_count || 0
})

// 计算属性：评分
const ratingValue = computed(() => {
  return Number(props.product.rating) || 0
})

// 计算属性：折扣百分比
const discountPercent = computed(() => {
  if (originalPrice.value > 0 && currentPrice.value < originalPrice.value) {
    return Math.round((1 - currentPrice.value / originalPrice.value) * 100)
  }
  return 0
})

// 格式化价格
const formatPrice = (price) => {
  return Number(price).toFixed(2)
}

// 格式化销量
const formatSales = (sales) => {
  if (sales >= 10000) {
    return (sales / 10000).toFixed(1) + 'w'
  } else if (sales >= 1000) {
    return (sales / 1000).toFixed(1) + 'k'
  }
  return sales
}

// 图片加载失败处理
const handleImageError = () => {
  imageLoadError.value = true
}

// 点击卡片
const handleClick = () => {
  emit('click', props.product)
}

// 切换收藏状态
const toggleFavorite = () => {
  isFavorited.value = !isFavorited.value
  emit('favorite', { product: props.product, isFavorited: isFavorited.value })
}
</script>

<style scoped>
.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  position: relative;
}

.product-card--hover:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
}

/* 图片区域 */
.product-card__image-wrapper {
  position: relative;
  width: 100%;
  padding-top: 100%; /* 1:1 宽高比 */
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  overflow: hidden;
}

.product-card__image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.product-card--hover:hover .product-card__image {
  transform: scale(1.05);
}

/* 徽章 */
.product-card__badges {
  position: absolute;
  top: 8px;
  left: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.badge {
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.badge--new {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.badge--hot {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.badge--discount {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

/* 收藏按钮 */
.product-card__favorite {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  backdrop-filter: blur(4px);
}

.product-card__favorite svg {
  width: 18px;
  height: 18px;
  color: #ccc;
  transition: all 0.3s ease;
}

.product-card__favorite:hover {
  background: #fff;
  transform: scale(1.1);
}

.product-card__favorite:hover svg {
  color: #ff6b6b;
}

.product-card__favorite.is-favorited svg {
  color: #ff6b6b;
}

/* 内容区域 */
.product-card__content {
  padding: 12px 14px 14px;
}

/* 标题 */
.product-card__title {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a2e;
  line-height: 1.4;
  margin: 0 0 6px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 39px;
}

/* 描述 */
.product-card__desc {
  font-size: 12px;
  color: #8b8b9a;
  line-height: 1.4;
  margin: 0 0 10px 0;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 价格行 */
.product-card__price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}

.product-card__price {
  display: flex;
  align-items: baseline;
  color: #ff4757;
  font-weight: 700;
}

.price-symbol {
  font-size: 12px;
  margin-right: 1px;
}

.price-integer {
  font-size: 20px;
  line-height: 1;
}

.price-decimal {
  font-size: 12px;
}

.product-card__original-price {
  font-size: 12px;
  color: #b4b4c4;
  text-decoration: line-through;
}

/* 统计信息 */
.product-card__stats {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 8px;
  border-top: 1px solid #f0f0f5;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
}

.stat-label {
  color: #b4b4c4;
}

.stat-value {
  color: #666;
  font-weight: 500;
}

.stat-item--rating {
  display: flex;
  align-items: center;
  gap: 4px;
}

.rating-stars {
  display: flex;
  gap: 1px;
}

.star {
  width: 12px;
  height: 12px;
  color: #ddd;
}

.star.filled {
  color: #ffc107;
}

.rating-value {
  font-size: 11px;
  color: #ffc107;
  font-weight: 600;
}

/* 店铺信息 */
.product-card__shop {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f5;
  font-size: 11px;
  color: #8b8b9a;
}

.shop-icon {
  font-size: 12px;
}

.shop-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 响应式 */
@media (max-width: 640px) {
  .product-card__content {
    padding: 10px 12px 12px;
  }
  
  .product-card__title {
    font-size: 13px;
    min-height: 36px;
  }
  
  .price-integer {
    font-size: 18px;
  }
}
</style>

