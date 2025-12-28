<template>
  <div class="product-detail-container">
    <!-- 返回导航 -->
    <div class="page-navigation">
      <el-button 
        type="text" 
        @click="goBack"
        class="back-btn">
        <LocalIcon name="fanhui" :size="16" />
        <span>返回</span>
      </el-button>
      <el-button 
        type="text" 
        @click="goHome"
        class="home-btn">
        <LocalIcon name="shouye" :size="16" />
        <span>首页</span>
      </el-button>
    </div>

    <div class="product-detail" v-if="product">
      <!-- 商品图片区域 -->
      <div class="product-images">
        <div class="main-image">
          <ImageMagnifier 
            :src="currentImage" 
            :alt="product.name"
            :zoom-level="2.5"
            :lens-size="120"
            :preview-width="450"
            :preview-height="450"
            @click="showImagePreview"
          />
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
          <div class="params-content" v-if="product && product.parameters && product.parameters.length > 0">
            <el-descriptions :column="2" border>
              <el-descriptions-item 
                v-for="param in product.parameters" 
                :key="param.name"
                :label="param.name">
                {{ param.value }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
          <div v-else-if="product" class="no-params">
            <el-empty description="暂无规格参数"></el-empty>
          </div>
          <div v-else class="loading-state">
            <el-skeleton :rows="5" animated />
          </div>
        </el-tab-pane>
        <el-tab-pane label="用户评价" name="reviews">
          <div class="reviews-content" v-if="product" v-loading="reviewLoading">
            <!-- 评价统计区域 -->
            <div class="reviews-summary">
              <div class="rating-overview">
                <div class="rating-score-section">
                  <div class="main-score">
                    <span class="score-number">{{ reviewStatistics.avgRating?.toFixed(1) || '5.0' }}</span>
                    <span class="score-label">综合评分</span>
                  </div>
                  <div class="rating-details">
                    <div class="rating-item">
                      <span class="label">描述相符</span>
                      <el-rate :model-value="reviewStatistics.avgDescriptionRating || 5" disabled size="small" />
                      <span class="value">{{ reviewStatistics.avgDescriptionRating?.toFixed(1) || '5.0' }}</span>
                    </div>
                    <div class="rating-item">
                      <span class="label">卖家服务</span>
                      <el-rate :model-value="reviewStatistics.avgServiceRating || 5" disabled size="small" />
                      <span class="value">{{ reviewStatistics.avgServiceRating?.toFixed(1) || '5.0' }}</span>
                    </div>
                    <div class="rating-item">
                      <span class="label">物流服务</span>
                      <el-rate :model-value="reviewStatistics.avgLogisticsRating || 5" disabled size="small" />
                      <span class="value">{{ reviewStatistics.avgLogisticsRating?.toFixed(1) || '5.0' }}</span>
                    </div>
                  </div>
                </div>
                <div class="rating-stats">
                  <div class="stat-item">
                    <span class="count">{{ reviewStatistics.totalCount || 0 }}</span>
                    <span class="label">全部评价</span>
                  </div>
                  <div class="stat-item good">
                    <span class="count">{{ reviewStatistics.goodRate || 100 }}%</span>
                    <span class="label">好评率</span>
                  </div>
                </div>
              </div>
              
              <!-- 评价筛选标签 -->
              <div class="rating-filters">
                <el-button 
                  :type="reviewRatingType === 'all' ? 'primary' : 'default'"
                  size="small"
                  @click="changeRatingType('all')">
                  全部({{ reviewStatistics.totalCount || 0 }})
                </el-button>
                <el-button 
                  :type="reviewRatingType === 'good' ? 'primary' : 'default'"
                  size="small"
                  @click="changeRatingType('good')">
                  好评({{ reviewStatistics.goodCount || 0 }})
                </el-button>
                <el-button 
                  :type="reviewRatingType === 'medium' ? 'primary' : 'default'"
                  size="small"
                  @click="changeRatingType('medium')">
                  中评({{ reviewStatistics.mediumCount || 0 }})
                </el-button>
                <el-button 
                  :type="reviewRatingType === 'bad' ? 'primary' : 'default'"
                  size="small"
                  @click="changeRatingType('bad')">
                  差评({{ reviewStatistics.badCount || 0 }})
                </el-button>
                <el-button 
                  :type="reviewRatingType === 'withImage' ? 'primary' : 'default'"
                  size="small"
                  @click="changeRatingType('withImage')">
                  有图({{ reviewStatistics.withImageCount || 0 }})
                </el-button>
              </div>
            </div>
            
            <!-- 评价列表 -->
            <div class="reviews-list" v-if="reviews.length > 0">
              <div v-for="review in reviews" :key="review.id" class="review-item">
                <div class="review-header">
                  <div class="user-info">
                    <img 
                      :src="review.userAvatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'" 
                      :alt="review.userName" 
                      class="user-avatar">
                    <div class="user-details">
                      <span class="user-name">{{ review.userName || '匿名用户' }}</span>
                      <div class="review-rating">
                        <el-rate :model-value="review.rating" disabled size="small" />
                      </div>
                    </div>
                  </div>
                  <span class="review-date">{{ formatDate(review.createTime) }}</span>
                </div>
                
                <div class="review-content">{{ review.content }}</div>
                
                <!-- 评价图片 -->
                <div class="review-images" v-if="review.images && review.images.length > 0">
                  <img 
                    v-for="(img, index) in review.images" 
                    :key="index"
                    :src="img" 
                    :alt="`评价图片${index + 1}`"
                    class="review-image"
                    @click="previewImage(img)"
                    @error="handleReviewImageError"
                    loading="lazy">
                </div>
                
                <!-- 多维度评分 -->
                <div class="review-ratings" v-if="review.descriptionRating || review.serviceRating || review.logisticsRating">
                  <span class="rating-tag">描述相符: {{ review.descriptionRating }}星</span>
                  <span class="rating-tag">卖家服务: {{ review.serviceRating }}星</span>
                  <span class="rating-tag">物流服务: {{ review.logisticsRating }}星</span>
                </div>
                
                <!-- 商家回复 -->
                <div class="merchant-reply" v-if="review.merchantReply">
                  <div class="reply-header">
                    <span class="reply-label">商家回复</span>
                    <span class="reply-time">{{ formatDate(review.merchantReplyTime) }}</span>
                  </div>
                  <div class="reply-content">{{ review.merchantReply }}</div>
                </div>
                
                <!-- 操作按钮 -->
                <div class="review-actions">
                  <el-button 
                    type="text" 
                    size="small"
                    :disabled="likedReviewIds.has(review.id)"
                    :class="{ 'is-liked': likedReviewIds.has(review.id) }"
                    @click="handleLikeReview(review)">
                    <el-icon><Star /></el-icon>
                    {{ likedReviewIds.has(review.id) ? '已点赞' : '有用' }}({{ review.likeCount || 0 }})
                  </el-button>
                </div>
              </div>
              
              <!-- 分页 -->
              <div class="reviews-pagination" v-if="reviewTotal > reviewSize">
                <el-pagination
                  v-model:current-page="reviewPage"
                  :page-size="reviewSize"
                  :total="reviewTotal"
                  layout="prev, pager, next"
                  @current-change="handleReviewPageChange"
                />
              </div>
            </div>
            
            <div v-else class="no-reviews">
              <el-empty description="暂无评价，快来抢沙发吧~"></el-empty>
            </div>
          </div>
          <div v-else class="loading-state">
            <el-skeleton :rows="5" animated />
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
import { Star } from '@element-plus/icons-vue'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import LocalIcon from '@/components/LocalIcon.vue'
import ImageMagnifier from '@/components/ImageMagnifier.vue'
import { getProductDetail, getProductReviews, addProductReview, likeReview } from '@/api/product'
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

// 评价相关数据
const reviewStatistics = ref({
  totalCount: 0,
  avgRating: 5.0,
  avgDescriptionRating: 5.0,
  avgServiceRating: 5.0,
  avgLogisticsRating: 5.0,
  goodCount: 0,
  mediumCount: 0,
  badCount: 0,
  withImageCount: 0,
  goodRate: 100
})
const reviewPage = ref(1)
const reviewSize = ref(10)
const reviewTotal = ref(0)
const reviewRatingType = ref('all')
const reviewLoading = ref(false)
// 已点赞的评价ID集合（防止重复点赞）
const likedReviewIds = ref(new Set())

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
    
    // 使用merchant-service的真实API
    try {
      const response = await getProductDetail(productId)
      
      // 数据映射：将后端字段名映射为前端期待的字段名
      const productData = response.data
      
      // 安全解析JSON字段
      let specifications = []
      try {
        if (productData.specifications && typeof productData.specifications === 'string') {
          specifications = JSON.parse(productData.specifications)
        } else if (Array.isArray(productData.specifications)) {
          specifications = productData.specifications
        }
      } catch (e) {
        console.warn('解析商品规格失败:', e)
      }
      
      let parameters = []
      try {
        if (productData.attributes && typeof productData.attributes === 'string') {
          const attrs = JSON.parse(productData.attributes)
          // 将对象转换为数组格式
          if (typeof attrs === 'object' && !Array.isArray(attrs)) {
            parameters = Object.entries(attrs).map(([name, value]) => ({ name, value }))
          } else if (Array.isArray(attrs)) {
            parameters = attrs
          }
        }
      } catch (e) {
        console.warn('解析商品属性失败:', e)
      }
      
      // V2.0 2025-12-02: 适配 product-service 返回的字段名
      // product-service 字段: name, stock, originalPrice, mainImage, detailImages, sales
      // merchant-service 字段: productName, stockQuantity, marketPrice, mainImage, images, salesCount
      product.value = {
        id: productData.id,
        name: productData.name || productData.productName,
        subtitle: productData.seoDescription || '',
        price: productData.price,
        originalPrice: productData.originalPrice || productData.marketPrice,
        stock: productData.stock || productData.stockQuantity || 0,
        image: productData.mainImage,
        images: (productData.detailImages || productData.images) 
          ? (productData.detailImages || productData.images).split(',').filter(img => img) 
          : [productData.mainImage],
        description: productData.description || '暂无详细描述',
        tags: [],
        specifications: specifications,
        parameters: parameters,
        rating: productData.rating ? parseFloat(productData.rating) : 5.0,
        reviewCount: productData.reviewCount || 0,
        sales: productData.sales || productData.salesCount || 0,
        status: productData.status
      }
    } catch (apiError) {
      console.error('商品详情API调用失败:', apiError)
      ElMessage.error('商品详情加载失败')
      // 不要直接跳转404，因为可能是网络问题
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
    reviewLoading.value = true
    const productId = route.params.id
    
    const response = await getProductReviews(productId, {
      page: reviewPage.value,
      size: reviewSize.value,
      ratingType: reviewRatingType.value
    })
    
    if (response.data) {
      // 更新统计信息
      if (response.data.statistics) {
        reviewStatistics.value = {
          ...reviewStatistics.value,
          ...response.data.statistics
        }
      }
      
      // 更新评价列表
      reviews.value = (response.data.list || []).map(review => ({
        ...review,
        // 处理图片字段
        images: review.images ? review.images.split(',').filter(img => img) : []
      }))
      
      reviewTotal.value = response.data.total || 0
    }
  } catch (error) {
    console.warn('加载评价失败:', error)
    reviews.value = []
  } finally {
    reviewLoading.value = false
  }
}

// 切换评价类型
const changeRatingType = (type) => {
  reviewRatingType.value = type
  reviewPage.value = 1
  loadReviews()
}

// 评价分页变化
const handleReviewPageChange = (page) => {
  reviewPage.value = page
  loadReviews()
}

// 点赞评价
const handleLikeReview = async (review) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  // 检查是否已点赞
  if (likedReviewIds.value.has(review.id)) {
    ElMessage.warning('您已经点赞过了')
    return
  }
  
  try {
    await likeReview(review.id, userStore.userInfo?.id)
    review.likeCount = (review.likeCount || 0) + 1
    // 记录已点赞
    likedReviewIds.value.add(review.id)
    // 保存到localStorage
    saveLikedReviews()
    ElMessage.success('点赞成功')
  } catch (error) {
    if (error.response?.data?.msg?.includes('已点赞')) {
      likedReviewIds.value.add(review.id)
      saveLikedReviews()
      ElMessage.warning('您已经点赞过了')
    } else {
      ElMessage.error('点赞失败')
    }
  }
}

// 保存已点赞记录到localStorage
const saveLikedReviews = () => {
  const userId = userStore.userInfo?.id
  if (userId) {
    localStorage.setItem(`liked_reviews_${userId}`, JSON.stringify([...likedReviewIds.value]))
  }
}

// 加载已点赞记录
const loadLikedReviews = () => {
  const userId = userStore.userInfo?.id
  if (userId) {
    const saved = localStorage.getItem(`liked_reviews_${userId}`)
    if (saved) {
      try {
        likedReviewIds.value = new Set(JSON.parse(saved))
      } catch (e) {
        likedReviewIds.value = new Set()
      }
    }
  }
}

// 处理评价图片加载错误
const handleReviewImageError = (event) => {
  event.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iODAiIGhlaWdodD0iODAiIHZpZXdCb3g9IjAgMCA4MCA4MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iODAiIGhlaWdodD0iODAiIGZpbGw9IiNGNUY1RjUiLz48cGF0aCBkPSJNMzIgMjhIMjhWNTJINTJWNDhINTZWMjRIMzJWMjhaIiBmaWxsPSIjRTBFMEUwIi8+PHBhdGggZD0iTTM2IDM2QzM3LjEwNDYgMzYgMzggMzUuMTA0NiAzOCAzNEMzOCAzMi44OTU0IDM3LjEwNDYgMzIgMzYgMzJDMzQuODk1NCAzMiAzNCAzMi44OTU0IDM0IDM0QzM0IDM1LjEwNDYgMzQuODk1NCAzNiAzNiAzNloiIGZpbGw9IiNFMEUwRTAiLz48cGF0aCBkPSJNMzIgNDRMNDAgMzZMNDQgNDBMNTIgMzJWNDhIMzJWNDRaIiBmaWxsPSIjRTBFMEUwIi8+PC9zdmc+'
  event.target.classList.add('image-error')
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
    
    // 使用store添加到购物车（store内部会调用API并更新状态）
    const success = await cartStore.addToCart(cartItem)
    if (success) {
      ElMessage.success('已添加到购物车')
    } else {
      ElMessage.error('添加购物车失败')
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

// 返回上一页
const goBack = () => {
  router.back()
}

// 返回首页
const goHome = () => {
  router.push('/')
}

// 监听规格变化，重置数量
watch(selectedSpec, () => {
  quantity.value = 1
})

// 生命周期
onMounted(() => {
  loadLikedReviews()
  loadProductDetail()
})
</script>

<style scoped>
.product-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* 页面导航 */
.page-navigation {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.back-btn,
.home-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
  color: #606266;
  font-size: 14px;
  transition: all 0.3s;
}

.back-btn:hover,
.home-btn:hover {
  color: #409eff;
  background-color: #ecf5ff;
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
  overflow: visible;
  position: relative;
}

/* 放大镜组件内的图片样式 */
.main-image :deep(.magnifier-image) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

/* 放大镜预览区域位置调整 */
.main-image :deep(.magnifier-preview) {
  border-radius: 8px;
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
  justify-content: space-between;
  align-items: flex-start;
  gap: 30px;
  margin-bottom: 20px;
}

.rating-score-section {
  display: flex;
  gap: 40px;
}

.main-score {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 20px;
}

.score-number {
  font-size: 48px;
  font-weight: 600;
  color: #ff9900;
  line-height: 1;
}

.score-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.rating-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rating-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rating-item .label {
  font-size: 14px;
  color: #606266;
  width: 70px;
}

.rating-item .value {
  font-size: 14px;
  color: #ff9900;
  font-weight: 500;
}

.rating-stats {
  display: flex;
  gap: 30px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 20px;
}

.stat-item .count {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.stat-item.good .count {
  color: #67c23a;
}

.stat-item .label {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.rating-filters {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
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
  align-items: flex-start;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
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
  margin-bottom: 12px;
  font-size: 14px;
}

.review-images {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.review-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.2s;
}

.review-image:hover {
  transform: scale(1.05);
}

.review-image.image-error {
  background-color: #f5f5f5;
  object-fit: contain;
}

/* 已点赞按钮样式 */
.review-actions .is-liked {
  color: #ff9900 !important;
  cursor: not-allowed;
}

.review-actions .is-liked:hover {
  color: #ff9900 !important;
}

.review-ratings {
  display: flex;
  gap: 15px;
  margin-bottom: 12px;
}

.rating-tag {
  font-size: 12px;
  color: #909399;
  background: #f5f7fa;
  padding: 4px 8px;
  border-radius: 4px;
}

.merchant-reply {
  background: #fef0f0;
  border-radius: 8px;
  padding: 12px;
  margin-top: 12px;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.reply-label {
  font-size: 12px;
  color: #f56c6c;
  font-weight: 500;
}

.reply-time {
  font-size: 12px;
  color: #909399;
}

.reply-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.review-actions {
  margin-top: 12px;
}

.reviews-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.no-reviews {
  text-align: center;
  padding: 40px 0;
}

.no-params {
  text-align: center;
  padding: 40px 0;
}

.loading-state {
  padding: 20px;
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

  .main-image :deep(.magnifier-image) {
    height: 350px;
  }
  
  /* 平板上隐藏放大镜预览，改用点击预览 */
  .main-image :deep(.magnifier-preview) {
    display: none;
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

  .main-image :deep(.magnifier-image) {
    height: 300px;
  }
  
  /* 移动端隐藏放大镜，改用点击预览 */
  .main-image :deep(.magnifier-lens),
  .main-image :deep(.magnifier-preview) {
    display: none;
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

  .main-image :deep(.magnifier-image) {
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