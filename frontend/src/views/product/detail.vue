<template>
  <div class="product-detail-container">
    <div class="product-detail" v-if="product">
      <!-- 商品图片区域 -->
      <div class="product-images">
        <div class="main-image">
          <img :src="currentImage" :alt="product.name" @click="showImagePreview">
        </div>
        <div class="thumbnail-list" v-if="product.images && product.images.length > 1">
          <div 
            v-for="(image, index) in product.images" 
            :key="index"
            class="thumbnail-item"
            :class="{ active: currentImage === image }"
            @click="currentImage = image">
            <img :src="image" :alt="`${product.name} ${index + 1}`">
          </div>
        </div>
      </div>

      <!-- 商品信息区域 -->
      <div class="product-info">
        <div class="product-header">
          <h1 class="product-title">{{ product.name }}</h1>
          <div class="product-subtitle" v-if="product.subtitle">{{ product.subtitle }}</div>
        </div>

        <!-- 价格信息 -->
        <div class="price-section">
          <div class="current-price">
            <span class="currency">¥</span>
            <span class="price">{{ product.price }}</span>
          </div>
          <div class="original-price" v-if="product.originalPrice && product.originalPrice > product.price">
            <span>原价：¥{{ product.originalPrice }}</span>
          </div>
          <div class="discount" v-if="product.discount">
            <el-tag type="danger" size="small">{{ product.discount }}折</el-tag>
          </div>
        </div>

        <!-- 商品标签 -->
        <div class="product-tags" v-if="product.tags && product.tags.length > 0">
          <el-tag 
            v-for="tag in product.tags" 
            :key="tag"
            type="info" 
            size="small"
            class="tag-item">
            {{ tag }}
          </el-tag>
        </div>

        <!-- 规格选择 -->
        <div class="spec-section" v-if="product.specifications && product.specifications.length > 0">
          <h3>选择规格</h3>
          <div class="spec-options">
            <el-radio-group v-model="selectedSpec" class="spec-group">
              <el-radio-button 
                v-for="spec in product.specifications" 
                :key="spec.id"
                :value="spec.id"
                :disabled="spec.stock <= 0">
                {{ spec.name }}
                <span v-if="spec.stock <= 0" class="out-of-stock">（缺货）</span>
              </el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <!-- 数量选择 -->
        <div class="quantity-section">
          <h3>购买数量</h3>
          <div class="quantity-controls">
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="maxQuantity"
              size="large"
              controls-position="right">
            </el-input-number>
            <span class="stock-info">库存：{{ currentStock }}件</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button 
            type="primary" 
            size="large"
            :loading="addingToCart"
            :disabled="currentStock <= 0"
            @click="addToCart"
            class="add-to-cart-btn">
            <LocalIcon name="gouwuche" :size="16" />
            {{ currentStock <= 0 ? '暂时缺货' : '加入购物车' }}
          </el-button>
          <el-button 
            type="danger" 
            size="large"
            :disabled="currentStock <= 0"
            @click="buyNow"
            class="buy-now-btn">
            立即购买
          </el-button>
        </div>

        <!-- 服务保障 -->
        <div class="service-section">
          <h3>服务保障</h3>
          <div class="service-list">
            <div class="service-item">
              <LocalIcon name="anquan" :size="16" />
              <span>正品保证</span>
            </div>
            <div class="service-item">
              <LocalIcon name="kuaidi" :size="16" />
              <span>快速配送</span>
            </div>
            <div class="service-item">
              <LocalIcon name="tuihuan" :size="16" />
              <span>7天无理由退换</span>
            </div>
            <div class="service-item">
              <LocalIcon name="kefu" :size="16" />
              <span>售后服务</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 商品详情标签页 -->
    <div class="product-tabs">
      <el-tabs v-model="activeTab" class="detail-tabs">
        <el-tab-pane label="商品详情" name="detail">
          <div class="detail-content" v-html="product?.description || '暂无详细描述'"></div>
        </el-tab-pane>
        <el-tab-pane label="规格参数" name="params">
          <div class="params-content">
            <el-descriptions :column="2" border>
              <el-descriptions-item 
                v-for="param in product?.parameters" 
                :key="param.name"
                :label="param.name">
                {{ param.value }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-tab-pane>
        <el-tab-pane label="用户评价" name="reviews">
          <div class="reviews-content">
            <div class="reviews-summary">
              <div class="rating-overview">
                <div class="rating-score">
                  <span class="score">{{ product?.rating || 5.0 }}</span>
                  <el-rate 
                    v-model="product.rating" 
                    disabled 
                    show-score
                    text-color="#ff9900">
                  </el-rate>
                </div>
                <div class="rating-count">共 {{ product?.reviewCount || 0 }} 条评价</div>
              </div>
            </div>
            
            <div class="reviews-list" v-if="reviews.length > 0">
              <div v-for="review in reviews" :key="review.id" class="review-item">
                <div class="review-header">
                  <div class="user-info">
                    <img :src="review.userAvatar" :alt="review.userName" class="user-avatar">
                    <span class="user-name">{{ review.userName }}</span>
                  </div>
                  <div class="review-rating">
                    <el-rate v-model="review.rating" disabled size="small"></el-rate>
                    <span class="review-date">{{ formatDate(review.createTime) }}</span>
                  </div>
                </div>
                <div class="review-content">{{ review.content }}</div>
                <div class="review-images" v-if="review.images && review.images.length > 0">
                  <img 
                    v-for="(img, index) in review.images" 
                    :key="index"
                    :src="img" 
                    :alt="`评价图片${index + 1}`"
                    class="review-image"
                    @click="previewImage(img)">
                </div>
              </div>
            </div>
            
            <div v-else class="no-reviews">
              <el-empty description="暂无评价"></el-empty>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 图片预览 -->
    <el-image-viewer
      v-if="showPreview"
      :url-list="previewImages"
      :initial-index="previewIndex"
      @close="showPreview = false">
    </el-image-viewer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import LocalIcon from '@/components/LocalIcon.vue'
import { getProductDetail, getProductReviews } from '@/api/product'
import { addToCart as addToCartApi } from '@/api/cart'
const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

// 响应式数据
const product = ref(null)
const currentImage = ref('')
const selectedSpec = ref('')
const quantity = ref(1)
const activeTab = ref('detail')
const addingToCart = ref(false)
const showPreview = ref(false)
const previewImages = ref([])
const previewIndex = ref(0)
const reviews = ref([])

// 计算属性
const currentStock = computed(() => {
  if (!product.value) return 0
  
  if (selectedSpec.value && product.value.specifications) {
    const spec = product.value.specifications.find(s => s.id === selectedSpec.value)
    return spec ? spec.stock : 0
  }
  
  return product.value.stock || 0
})

const maxQuantity = computed(() => {
  return Math.min(currentStock.value, 999)
})

const currentSpecName = computed(() => {
  if (!selectedSpec.value || !product.value.specifications) return ''
  const spec = product.value.specifications.find(s => s.id === selectedSpec.value)
  return spec ? spec.name : ''
})

// 方法
const loadProductDetail = async () => {
  try {
    const productId = route.params.id
    
    // 优先使用真实API
    try {
      const response = await getProductDetail(productId)
      product.value = response.data
    } catch (apiError) {
      console.error('商品详情API调用失败:', apiError)
      ElMessage.error('商品详情加载失败')
      router.push('/404')
      return
    }
    
    // 设置默认图片和规格
    if (product.value.images && product.value.images.length > 0) {
      currentImage.value = product.value.images[0]
    }
    
    if (product.value.specifications && product.value.specifications.length > 0) {
      selectedSpec.value = product.value.specifications[0].id
    }
    
    // 加载评价数据
    await loadReviews()
    
  } catch (error) {
    ElMessage.error('加载商品详情失败')
    console.error('加载商品详情失败:', error)
  }
}

const loadReviews = async () => {
  try {
    const productId = route.params.id
    
    // 优先使用真实API
    try {
      const response = await getProductReviews(productId)
      reviews.value = response.data
    } catch (apiError) {
      console.error('评价API调用失败:', apiError)
      reviews.value = []
    }
  } catch (error) {
    console.error('加载评价失败:', error)
    reviews.value = []
  }
}

const addToCart = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (currentStock.value <= 0) {
    ElMessage.warning('商品暂时缺货')
    return
  }
  
  try {
    addingToCart.value = true
    
    const cartItem = {
      productId: product.value.id,
      quantity: quantity.value,
      specificationId: selectedSpec.value,
      specifications: currentSpecName.value
    }
    
    // 使用真实API添加到购物车
    try {
      await addToCartApi(cartItem)
      ElMessage.success('已添加到购物车')
      
      // 更新购物车状态
      if (cartStore.addItem) {
        await cartStore.addItem(cartItem)
      }
    } catch (apiError) {
      console.warn('购物车API调用失败:', apiError)
      // 如果API失败，仍然更新本地状态
      if (cartStore.addItem) {
        await cartStore.addItem(cartItem)
      }
      ElMessage.success('已添加到购物车')
    }
    
  } catch (error) {
    ElMessage.error('添加到购物车失败')
    console.error('添加到购物车失败:', error)
  } finally {
    addingToCart.value = false
  }
}

const buyNow = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (currentStock.value <= 0) {
    ElMessage.warning('商品暂时缺货')
    return
  }
  
  // 先添加到购物车，然后跳转到结算页面
  await addToCart()
  router.push('/checkout')
}

const showImagePreview = () => {
  if (product.value.images && product.value.images.length > 0) {
    previewImages.value = product.value.images
    previewIndex.value = product.value.images.indexOf(currentImage.value)
    showPreview.value = true
  }
}

const previewImage = (imageUrl) => {
  previewImages.value = [imageUrl]
  previewIndex.value = 0
  showPreview.value = true
}

const formatDate = (date) => {
  return new Date(date).toLocaleDateString('zh-CN')
}

// 监听规格变化，重置数量
watch(selectedSpec, () => {
  quantity.value = 1
})

// 生命周期
onMounted(() => {
  loadProductDetail()
})
</script>

<style scoped>
.product-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.product-detail {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  margin-bottom: 40px;
}

/* 商品图片区域 */
.product-images {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.main-image {
  width: 100%;
  aspect-ratio: 1;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.main-image:hover img {
  transform: scale(1.05);
}

.thumbnail-list {
  display: flex;
  gap: 10px;
  overflow-x: auto;
}

.thumbnail-item {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  border: 2px solid transparent;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.3s;
}

.thumbnail-item.active {
  border-color: #409eff;
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 商品信息区域 */
.product-info {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.product-header h1 {
  margin: 0 0 10px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.product-subtitle {
  color: #909399;
  font-size: 16px;
}

.price-section {
  display: flex;
  align-items: baseline;
  gap: 15px;
  flex-wrap: wrap;
}

.current-price {
  display: flex;
  align-items: baseline;
  color: #f56c6c;
  font-weight: 600;
}

.currency {
  font-size: 18px;
}

.price {
  font-size: 32px;
}

.original-price {
  color: #909399;
  text-decoration: line-through;
  font-size: 16px;
}

.product-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag-item {
  margin-right: 0;
}

.spec-section h3,
.quantity-section h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.spec-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.spec-group .el-radio-button {
  margin-right: 0;
}

.out-of-stock {
  color: #f56c6c;
  font-size: 12px;
}

.quantity-controls {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stock-info {
  color: #909399;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  gap: 15px;
}

.add-to-cart-btn,
.buy-now-btn {
  flex: 1;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
}

.service-section h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.service-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.service-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

.service-item .el-icon {
  color: #409eff;
}

/* 详情标签页 */
.product-tabs {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.detail-content {
  line-height: 1.6;
  color: #606266;
}

.params-content {
  padding: 20px 0;
}

.reviews-content {
  padding: 20px 0;
}

.reviews-summary {
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.rating-overview {
  display: flex;
  align-items: center;
  gap: 20px;
}

.rating-score {
  display: flex;
  align-items: center;
  gap: 10px;
}

.score {
  font-size: 32px;
  font-weight: 600;
  color: #ff9900;
}

.rating-count {
  color: #909399;
}

.review-item {
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.user-name {
  font-weight: 500;
  color: #303133;
}

.review-rating {
  display: flex;
  align-items: center;
  gap: 10px;
}

.review-date {
  color: #909399;
  font-size: 12px;
}

.review-content {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 10px;
}

.review-images {
  display: flex;
  gap: 10px;
}

.review-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
}

.no-reviews {
  text-align: center;
  padding: 40px 0;
}

/* 本地图标样式 */
.local-icon {
  display: inline-block;
  vertical-align: middle;
  object-fit: contain;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .product-detail {
    gap: 30px;
  }
  
  .product-images {
    width: 45%;
  }
  
  .product-info {
    width: 55%;
  }
}

@media (max-width: 992px) {
  .product-detail {
    flex-direction: column;
    gap: 20px;
  }

  .product-images,
  .product-info {
    width: 100%;
  }

  .main-image img {
    height: 350px;
  }

  .thumbnail-list {
    justify-content: center;
  }

  .action-buttons {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background-color: white;
    padding: 15px;
    box-shadow: 0 -2px 10px rgba(0,0,0,0.1);
    z-index: 1000;
    border-top: 1px solid #e0e0e0;
  }

  .add-to-cart-btn,
  .buy-now-btn {
    flex: 1;
    height: 50px;
  }

  .product-detail-container {
    padding-bottom: 80px; /* 为固定按钮留出空间 */
  }
}

@media (max-width: 768px) {
  .product-detail-container {
    padding: 10px;
  }

  .main-image img {
    height: 300px;
  }

  .thumbnail-list {
    gap: 8px;
  }

  .thumbnail-item {
    width: 60px;
    height: 60px;
  }

  .product-title {
    font-size: 18px;
    line-height: 1.4;
  }

  .current-price .price {
    font-size: 24px;
  }

  .spec-group {
    flex-wrap: wrap;
  }

  .quantity-controls {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .service-list {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .service-item {
    font-size: 12px;
    padding: 8px;
  }

  .detail-tabs {
    margin-top: 10px;
  }

  .reviews-summary {
    padding: 15px;
  }

  .rating-overview {
    flex-direction: column;
    text-align: center;
    gap: 10px;
  }

  .review-item {
    padding: 15px;
  }

  .review-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .review-images {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 480px) {
  .product-detail-container {
    padding: 5px;
  }

  .main-image img {
    height: 250px;
  }

  .thumbnail-item {
    width: 50px;
    height: 50px;
  }

  .product-title {
    font-size: 16px;
  }

  .current-price .price {
    font-size: 20px;
  }

  .service-list {
    grid-template-columns: 1fr;
  }

  .review-images {
    grid-template-columns: repeat(2, 1fr);
  }

  .action-buttons {
    padding: 10px;
  }

  .add-to-cart-btn,
  .buy-now-btn {
    height: 45px;
    font-size: 14px;
  }
}
</style>