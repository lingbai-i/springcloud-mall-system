<template>
  <div class="product-detail-container">
    <div class="page-navigation">
      <el-button type="text" @click="goBack" class="back-btn">
        <LocalIcon name="fanhui" :size="16" /><span>返回</span>
      </el-button>
      <el-button type="text" @click="goHome" class="home-btn">
        <LocalIcon name="shouye" :size="16" /><span>首页</span>
      </el-button>
    </div>

    <div class="product-detail" v-if="product">
      <div class="product-images">
        <div class="main-image">
          <ImageMagnifier :src="currentImage" :alt="product.name" :zoom-level="2.5" :lens-size="120" :preview-width="450" :preview-height="450" @click="showImagePreview" />
        </div>
        <div class="thumbnail-list" v-if="product.images && product.images.length > 1">
          <div v-for="(image, index) in product.images" :key="index" class="thumbnail-item" :class="{ active: currentImage === image }" @click="currentImage = image">
            <img :src="image" :alt="`${product.name} ${index + 1}`">
          </div>
        </div>
      </div>

      <div class="product-info">
        <div class="product-header">
          <h1 class="product-title">{{ product.name }}</h1>
          <div class="product-subtitle" v-if="product.subtitle">{{ product.subtitle }}</div>
        </div>
        <div class="price-section">
          <div class="current-price"><span class="currency">¥</span><span class="price">{{ product.price }}</span></div>
          <div class="original-price" v-if="product.originalPrice && product.originalPrice > product.price"><span>原价：¥{{ product.originalPrice }}</span></div>
        </div>
        <div class="spec-section" v-if="product.specifications && product.specifications.length > 0">
          <h3>选择规格</h3>
          <el-radio-group v-model="selectedSpec" class="spec-group">
            <el-radio-button v-for="spec in product.specifications" :key="spec.id" :value="spec.id" :disabled="spec.stock <= 0">{{ spec.name }}</el-radio-button>
          </el-radio-group>
        </div>
        <div class="quantity-section">
          <h3>购买数量</h3>
          <div class="quantity-controls">
            <el-input-number v-model="quantity" :min="1" :max="maxQuantity" size="large" />
            <span class="stock-info">库存：{{ currentStock }}件</span>
          </div>
        </div>
        <div class="action-buttons">
          <el-button type="primary" size="large" :loading="addingToCart" :disabled="currentStock <= 0" @click="addToCart" class="add-to-cart-btn">
            <LocalIcon name="gouwuche" :size="16" />{{ currentStock <= 0 ? '暂时缺货' : '加入购物车' }}
          </el-button>
          <el-button type="danger" size="large" :disabled="currentStock <= 0" @click="buyNow" class="buy-now-btn">立即购买</el-button>
          <el-button 
            :type="isFavorited ? 'warning' : 'default'" 
            size="large" 
            :loading="favoriteLoading"
            @click="toggleFavorite" 
            class="favorite-btn"
          >
            <LocalIcon :name="isFavorited ? 'shoucang-fill' : 'shoucang'" :size="16" />
            {{ isFavorited ? '已收藏' : '收藏' }}
          </el-button>
        </div>
        <div class="service-section">
          <h3>服务保障</h3>
          <div class="service-list">
            <div class="service-item"><LocalIcon name="anquan" :size="16" /><span>正品保证</span></div>
            <div class="service-item"><LocalIcon name="kuaidi" :size="16" /><span>快速配送</span></div>
            <div class="service-item"><LocalIcon name="tuihuan" :size="16" /><span>7天无理由退换</span></div>
            <div class="service-item"><LocalIcon name="kefu" :size="16" /><span>售后服务</span></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 粘性导航栏 -->
    <div class="section-nav" :class="{ 'is-sticky': isNavSticky }" ref="sectionNavRef">
      <div class="nav-inner">
        <div v-for="nav in navItems" :key="nav.id" class="nav-item" :class="{ active: activeSection === nav.id }" @click="scrollToSection(nav.id)">
          {{ nav.label }}<span v-if="nav.id === 'reviews'" class="nav-count">({{ reviewStatistics.totalCount || 0 }})</span>
        </div>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="content-sections" ref="contentSectionsRef">
      <section id="section-reviews" class="content-section" ref="reviewsSectionRef">
        <h2 class="section-title">用户评价</h2>
        <div class="reviews-content" v-if="product" v-loading="reviewLoading">
          <div class="reviews-summary">
            <div class="rating-overview">
              <div class="main-score"><span class="score-number">{{ reviewStatistics.avgRating?.toFixed(1) || '5.0' }}</span><span class="score-label">综合评分</span></div>
              <div class="rating-stats">
                <div class="stat-item"><span class="count">{{ reviewStatistics.totalCount || 0 }}</span><span class="label">全部评价</span></div>
                <div class="stat-item good"><span class="count">{{ reviewStatistics.goodRate || 100 }}%</span><span class="label">好评率</span></div>
              </div>
            </div>
            <div class="rating-filters">
              <el-button :type="reviewRatingType === 'all' ? 'primary' : 'default'" size="small" @click="changeRatingType('all')">全部({{ reviewStatistics.totalCount || 0 }})</el-button>
              <el-button :type="reviewRatingType === 'good' ? 'primary' : 'default'" size="small" @click="changeRatingType('good')">好评({{ reviewStatistics.goodCount || 0 }})</el-button>
              <el-button :type="reviewRatingType === 'bad' ? 'primary' : 'default'" size="small" @click="changeRatingType('bad')">差评({{ reviewStatistics.badCount || 0 }})</el-button>
            </div>
          </div>
          <div class="reviews-list" v-if="reviews.length > 0">
            <div v-for="review in reviews" :key="review.id" class="review-item">
              <div class="review-header">
                <div class="user-info">
                  <img :src="review.userAvatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'" class="user-avatar">
                  <div class="user-details"><span class="user-name">{{ review.userName || '匿名用户' }}</span><el-rate :model-value="review.rating" disabled size="small" /></div>
                </div>
                <span class="review-date">{{ formatDate(review.createTime) }}</span>
              </div>
              <div class="review-content">{{ review.content }}</div>
              <div class="review-images" v-if="review.images && review.images.length > 0">
                <img v-for="(img, idx) in review.images" :key="idx" :src="img" class="review-image" @click="previewImage(img)">
              </div>
            </div>
            <div class="reviews-pagination" v-if="reviewTotal > reviewSize">
              <el-pagination v-model:current-page="reviewPage" :page-size="reviewSize" :total="reviewTotal" layout="prev, pager, next" @current-change="handleReviewPageChange" />
            </div>
          </div>
          <div v-else class="no-reviews"><el-empty description="暂无评价"></el-empty></div>
        </div>
      </section>

      <section id="section-params" class="content-section" ref="paramsSectionRef">
        <h2 class="section-title">规格参数</h2>
        <div class="params-content" v-if="product && product.parameters && product.parameters.length > 0">
          <el-descriptions :column="2" border>
            <el-descriptions-item v-for="param in product.parameters" :key="param.name" :label="param.name">{{ param.value }}</el-descriptions-item>
          </el-descriptions>
        </div>
        <div v-else class="no-params"><el-empty description="暂无规格参数"></el-empty></div>
      </section>

      <section id="section-detail" class="content-section" ref="detailSectionRef">
        <h2 class="section-title">商品详情</h2>
        <div class="detail-content">
          <div v-if="product?.description" v-html="product.description" class="description-text"></div>
          <div v-if="product?.detailImages && product.detailImages.length > 0" class="detail-images">
            <img v-for="(img, idx) in product.detailImages" :key="idx" :src="img" :alt="`商品详情图 ${idx + 1}`" class="detail-image" @click="previewDetailImage(idx)" />
          </div>
          <div v-if="!product?.description && (!product?.detailImages || product.detailImages.length === 0)">
            <el-empty description="暂无详细描述"></el-empty>
          </div>
        </div>
      </section>
    </div>

    <el-image-viewer v-if="showPreview" :url-list="previewImages" :initial-index="previewIndex" @close="showPreview = false" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { useFavoriteStore } from '@/stores/favorite'
import LocalIcon from '@/components/LocalIcon.vue'
import ImageMagnifier from '@/components/ImageMagnifier.vue'
import { getProductDetail, getProductReviews } from '@/api/product'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()
const favoriteStore = useFavoriteStore()

const product = ref(null)
const currentImage = ref('')
const selectedSpec = ref('')
const quantity = ref(1)
const addingToCart = ref(false)
const showPreview = ref(false)
const previewImages = ref([])
const previewIndex = ref(0)
const reviews = ref([])
const isFavorited = ref(false)
const favoriteLoading = ref(false)

const sectionNavRef = ref(null)
const contentSectionsRef = ref(null)
const reviewsSectionRef = ref(null)
const paramsSectionRef = ref(null)
const detailSectionRef = ref(null)
const isNavSticky = ref(false)
const activeSection = ref('reviews')

const navItems = [
  { id: 'reviews', label: '用户评价' },
  { id: 'params', label: '规格参数' },
  { id: 'detail', label: '商品详情' }
]

const reviewStatistics = ref({ totalCount: 0, avgRating: 5.0, goodCount: 0, badCount: 0, goodRate: 100 })
const reviewPage = ref(1)
const reviewSize = ref(10)
const reviewTotal = ref(0)
const reviewRatingType = ref('all')
const reviewLoading = ref(false)

const currentStock = computed(() => {
  if (!product.value) return 0
  if (selectedSpec.value && product.value.specifications) {
    const spec = product.value.specifications.find(s => s.id === selectedSpec.value)
    return spec ? spec.stock : 0
  }
  return product.value.stock || 0
})

const maxQuantity = computed(() => Math.min(currentStock.value, 999))

const scrollToSection = (sectionId) => {
  const refs = { reviews: reviewsSectionRef, params: paramsSectionRef, detail: detailSectionRef }
  if (refs[sectionId]?.value) {
    const navHeight = sectionNavRef.value?.offsetHeight || 60
    const top = refs[sectionId].value.getBoundingClientRect().top + window.scrollY - navHeight - 10
    window.scrollTo({ top, behavior: 'smooth' })
    activeSection.value = sectionId
  }
}

const handleScroll = () => {
  if (!sectionNavRef.value || !contentSectionsRef.value) return
  isNavSticky.value = contentSectionsRef.value.getBoundingClientRect().top <= 60
  const navHeight = (sectionNavRef.value?.offsetHeight || 60) + 50
  const sections = [{ id: 'reviews', ref: reviewsSectionRef }, { id: 'params', ref: paramsSectionRef }, { id: 'detail', ref: detailSectionRef }]
  for (let i = sections.length - 1; i >= 0; i--) {
    if (sections[i].ref?.value?.getBoundingClientRect().top <= navHeight) {
      activeSection.value = sections[i].id
      break
    }
  }
}

const loadProductDetail = async () => {
  try {
    const res = await getProductDetail(route.params.id)
    const d = res.data
    let specs = [], params = []
    try { specs = typeof d.specifications === 'string' ? JSON.parse(d.specifications) : (d.specifications || []) } catch {}
    try {
      if (d.attributes && typeof d.attributes === 'string') {
        const attrs = JSON.parse(d.attributes)
        params = Array.isArray(attrs) ? attrs : Object.entries(attrs).map(([name, value]) => ({ name, value }))
      }
    } catch {}
    // 主图：mainImage 可能是逗号分隔的多个URL，取第一个作为封面，全部作为主图轮播
    const mainImages = d.mainImage?.split(',').filter(Boolean) || []
    // 详情图片：detailImages 用于商品详情区域展示
    const detailImages = d.detailImages?.split(',').filter(Boolean) || []
    product.value = {
      id: d.id, name: d.name || d.productName, subtitle: d.seoDescription || '', price: d.price,
      originalPrice: d.originalPrice || d.marketPrice, stock: d.stock || d.stockQuantity || 0,
      image: mainImages[0] || '', images: mainImages.length > 0 ? mainImages : (detailImages.length > 0 ? detailImages : []),
      detailImages: detailImages, // 详情图片单独存储
      description: d.description || '暂无详细描述', specifications: specs, parameters: params
    }
    currentImage.value = product.value.images?.[0] || ''
    if (specs.length) selectedSpec.value = specs[0].id
    await loadReviews()
    // 检查收藏状态
    await checkFavoriteStatus()
  } catch (e) { ElMessage.error('加载商品详情失败') }
}

const checkFavoriteStatus = async () => {
  if (!userStore.isLoggedIn) {
    isFavorited.value = false
    return
  }
  try {
    isFavorited.value = await favoriteStore.checkFavorited(Number(route.params.id))
  } catch {
    isFavorited.value = false
  }
}

const toggleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  try {
    favoriteLoading.value = true
    const result = await favoriteStore.toggleFavorite(Number(route.params.id))
    if (result.success) {
      isFavorited.value = !isFavorited.value
      ElMessage.success(isFavorited.value ? '收藏成功' : '已取消收藏')
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    favoriteLoading.value = false
  }
}

const loadReviews = async () => {
  try {
    reviewLoading.value = true
    const res = await getProductReviews(route.params.id, { page: reviewPage.value, size: reviewSize.value, ratingType: reviewRatingType.value })
    if (res.data) {
      if (res.data.statistics) reviewStatistics.value = { ...reviewStatistics.value, ...res.data.statistics }
      reviews.value = (res.data.list || []).map(r => ({ ...r, images: r.images?.split(',').filter(Boolean) || [] }))
      reviewTotal.value = res.data.total || 0
    }
  } catch { reviews.value = [] } finally { reviewLoading.value = false }
}

const changeRatingType = (type) => { reviewRatingType.value = type; reviewPage.value = 1; loadReviews() }
const handleReviewPageChange = (page) => { reviewPage.value = page; loadReviews() }

const addToCart = async () => {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); router.push('/login'); return }
  if (currentStock.value <= 0) { ElMessage.warning('商品暂时缺货'); return }
  try {
    addingToCart.value = true
    const success = await cartStore.addToCart({ productId: product.value.id, quantity: quantity.value, specificationId: selectedSpec.value })
    ElMessage[success ? 'success' : 'error'](success ? '已添加到购物车' : '添加购物车失败')
  } catch { ElMessage.error('添加到购物车失败') } finally { addingToCart.value = false }
}

const buyNow = async () => {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); router.push('/login'); return }
  if (currentStock.value <= 0) { ElMessage.warning('商品暂时缺货'); return }
  
  // 直接购买：不加入购物车，通过路由state传递商品信息
  const buyNowItem = {
    productId: product.value.id,
    productName: product.value.name,
    productImage: currentImage.value || product.value.image,
    price: product.value.price,
    quantity: quantity.value,
    specifications: selectedSpec.value ? product.value.specifications?.find(s => s.id === selectedSpec.value)?.name || '' : ''
  }
  
  router.push({
    path: '/checkout',
    query: { mode: 'buyNow' },
    state: { buyNowItem }
  })
}

const showImagePreview = () => {
  if (product.value?.images?.length) {
    previewImages.value = product.value.images
    previewIndex.value = product.value.images.indexOf(currentImage.value)
    showPreview.value = true
  }
}

const previewImage = (url) => { previewImages.value = [url]; previewIndex.value = 0; showPreview.value = true }
const previewDetailImage = (idx) => {
  if (product.value?.detailImages?.length) {
    previewImages.value = product.value.detailImages
    previewIndex.value = idx
    showPreview.value = true
  }
}
const formatDate = (d) => new Date(d).toLocaleDateString('zh-CN')
const goBack = () => router.back()
const goHome = () => router.push('/')

watch(selectedSpec, () => { quantity.value = 1 })

onMounted(() => {
  loadProductDetail()
  window.addEventListener('scroll', handleScroll, { passive: true })
  nextTick(handleScroll)
})

onUnmounted(() => window.removeEventListener('scroll', handleScroll))
</script>

<style scoped>
.product-detail-container { max-width: 1200px; margin: 0 auto; padding: 20px; }
.page-navigation { display: flex; gap: 10px; margin-bottom: 20px; padding-bottom: 15px; border-bottom: 1px solid #ebeef5; }
.back-btn, .home-btn { display: flex; align-items: center; gap: 5px; padding: 8px 16px; color: #606266; }
.back-btn:hover, .home-btn:hover { color: #409eff; background-color: #ecf5ff; }
.product-detail { display: grid; grid-template-columns: 1fr 1fr; gap: 40px; margin-bottom: 40px; }
.product-images { display: flex; flex-direction: column; gap: 15px; }
.main-image { width: 100%; aspect-ratio: 1; border: 1px solid #ebeef5; border-radius: 8px; overflow: visible; }
.thumbnail-list { display: flex; gap: 10px; overflow-x: auto; }
.thumbnail-item { width: 80px; height: 80px; border: 2px solid transparent; border-radius: 6px; overflow: hidden; cursor: pointer; }
.thumbnail-item.active { border-color: #409eff; }
.thumbnail-item img { width: 100%; height: 100%; object-fit: cover; }
.product-info { display: flex; flex-direction: column; gap: 25px; }
.product-header h1 { margin: 0 0 10px; font-size: 24px; font-weight: 600; color: #303133; }
.product-subtitle { color: #909399; font-size: 16px; }
.price-section { display: flex; align-items: baseline; gap: 15px; }
.current-price { display: flex; align-items: baseline; color: #f56c6c; font-weight: 600; }
.currency { font-size: 18px; }
.price { font-size: 32px; }
.original-price { color: #909399; text-decoration: line-through; }
.spec-section h3, .quantity-section h3, .service-section h3 { margin: 0 0 15px; font-size: 16px; font-weight: 600; color: #303133; }
.spec-group { display: flex; flex-wrap: wrap; gap: 10px; }
.quantity-controls { display: flex; align-items: center; gap: 15px; }
.stock-info { color: #909399; font-size: 14px; }
.action-buttons { display: flex; gap: 15px; }
.add-to-cart-btn, .buy-now-btn { flex: 1; height: 50px; font-size: 16px; font-weight: 600; }
.favorite-btn { height: 50px; font-size: 16px; font-weight: 600; min-width: 100px; }
.service-list { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; }
.service-item { display: flex; align-items: center; gap: 8px; color: #606266; font-size: 14px; }

.section-nav { background: #fff; border-radius: 8px; margin-bottom: 20px; box-shadow: 0 2px 12px rgba(0,0,0,0.1); z-index: 100; }
.section-nav.is-sticky { position: sticky; top: 0; border-radius: 0; margin: 0 -20px 20px; padding: 0 20px; }
.nav-inner { display: flex; }
.nav-item { padding: 16px 30px; font-size: 16px; font-weight: 500; color: #606266; cursor: pointer; border-bottom: 3px solid transparent; transition: all 0.3s; }
.nav-item:hover { color: #409eff; background-color: #f5f7fa; }
.nav-item.active { color: #409eff; border-bottom-color: #409eff; background-color: #ecf5ff; }
.nav-count { font-size: 12px; color: #909399; margin-left: 4px; }

.content-sections { display: flex; flex-direction: column; gap: 30px; }
.content-section { background: #fff; border-radius: 8px; padding: 25px; box-shadow: 0 2px 12px rgba(0,0,0,0.1); }
.section-title { margin: 0 0 20px; font-size: 20px; font-weight: 600; color: #303133; padding-bottom: 15px; border-bottom: 2px solid #409eff; display: inline-block; }
.detail-content { line-height: 1.8; color: #606266; }
.detail-content :deep(img) { max-width: 100%; height: auto; }
.detail-content .description-text { margin-bottom: 20px; }
.detail-content .detail-images { display: flex; flex-direction: column; align-items: center; gap: 15px; }
.detail-content .detail-image { max-width: 100%; height: auto; cursor: pointer; border-radius: 8px; transition: transform 0.3s; }
.detail-content .detail-image:hover { transform: scale(1.02); }
.params-content { padding: 10px 0; }
.no-params, .no-reviews { text-align: center; padding: 40px 0; }

.reviews-summary { margin-bottom: 30px; padding: 20px; background: #f8f9fa; border-radius: 8px; }
.rating-overview { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.main-score { display: flex; flex-direction: column; align-items: center; }
.score-number { font-size: 48px; font-weight: 600; color: #ff9900; }
.score-label { font-size: 14px; color: #909399; margin-top: 8px; }
.rating-stats { display: flex; gap: 30px; }
.stat-item { display: flex; flex-direction: column; align-items: center; }
.stat-item .count { font-size: 28px; font-weight: 600; color: #303133; }
.stat-item.good .count { color: #67c23a; }
.stat-item .label { font-size: 12px; color: #909399; margin-top: 5px; }
.rating-filters { display: flex; gap: 10px; flex-wrap: wrap; }

.reviews-list { margin-top: 20px; }
.review-item { padding: 20px 0; border-bottom: 1px solid #ebeef5; }
.review-item:last-child { border-bottom: none; }
.review-header { display: flex; justify-content: space-between; margin-bottom: 12px; }
.user-info { display: flex; align-items: center; gap: 12px; }
.user-avatar { width: 40px; height: 40px; border-radius: 50%; object-fit: cover; }
.user-details { display: flex; flex-direction: column; gap: 4px; }
.user-name { font-weight: 500; color: #303133; font-size: 14px; }
.review-date { color: #909399; font-size: 12px; }
.review-content { color: #606266; line-height: 1.6; margin-bottom: 12px; }
.review-images { display: flex; gap: 10px; flex-wrap: wrap; }
.review-image { width: 80px; height: 80px; object-fit: cover; border-radius: 4px; cursor: pointer; }
.reviews-pagination { display: flex; justify-content: center; margin-top: 20px; }

@media (max-width: 768px) {
  .product-detail { grid-template-columns: 1fr; gap: 20px; }
  .section-nav.is-sticky { margin: 0 -15px 20px; padding: 0 15px; }
  .nav-item { padding: 12px 20px; font-size: 14px; }
  .rating-overview { flex-direction: column; gap: 20px; }
  .action-buttons { flex-direction: column; }
}
</style>
