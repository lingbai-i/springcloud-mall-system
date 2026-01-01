<template>
  <el-dialog
    v-model="visible"
    title="商品详情"
    width="80%"
    :before-close="handleClose">
    <div v-if="product" class="product-detail">
      <!-- 基本信息 -->
      <el-card shadow="never" class="detail-section">
        <template #header>
          <span>基本信息</span>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="product-images">
              <el-image
                :src="getFirstImage(product.mainImage)"
                :preview-src-list="product.images"
                class="main-image"
                fit="cover"
              />
              <div class="image-list">
                <el-image
                  v-for="(image, index) in product.images.slice(0, 4)"
                  :key="index"
                  :src="image"
                  :preview-src-list="product.images"
                  class="thumb-image"
                  fit="cover"
                />
              </div>
            </div>
          </el-col>
          
          <el-col :span="16">
            <div class="product-info">
              <h3 class="product-title">{{ product.name }}</h3>
              
              <div class="info-row">
                <span class="label">商品编号：</span>
                <span class="value">{{ product.sku }}</span>
              </div>
              
              <div class="info-row">
                <span class="label">商品分类：</span>
                <span class="value">{{ getCategoryName(product.category) }}</span>
              </div>
              
              <div class="info-row">
                <span class="label">商品状态：</span>
                <el-tag :type="getStatusType(product.status)" size="small">
                  {{ getStatusText(product.status) }}
                </el-tag>
              </div>
              
              <div class="info-row">
                <span class="label">销售价格：</span>
                <span class="price">¥{{ product.price }}</span>
              </div>
              
              <div class="info-row">
                <span class="label">市场价格：</span>
                <span class="market-price">¥{{ product.marketPrice }}</span>
              </div>
              
              <div class="info-row">
                <span class="label">当前库存：</span>
                <span class="stock" :class="{ 'low-stock': product.stock <= 10 }">
                  {{ product.stock }} 件
                </span>
              </div>
              
              <div class="info-row">
                <span class="label">累计销量：</span>
                <span class="value">{{ product.sales }} 件</span>
              </div>
              
              <div class="info-row">
                <span class="label">商品重量：</span>
                <span class="value">{{ product.weight }} kg</span>
              </div>
              
              <div class="info-row">
                <span class="label">创建时间：</span>
                <span class="value">{{ formatTime(product.createTime) }}</span>
              </div>
              
              <div class="info-row">
                <span class="label">更新时间：</span>
                <span class="value">{{ formatTime(product.updateTime) }}</span>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 详细描述 -->
      <el-card shadow="never" class="detail-section">
        <template #header>
          <span>商品描述</span>
        </template>
        <div class="product-description" v-html="product.description"></div>
      </el-card>

      <!-- 规格参数 -->
      <el-card shadow="never" class="detail-section" v-if="product.specifications">
        <template #header>
          <span>规格参数</span>
        </template>
        <el-table :data="product.specifications" style="width: 100%">
          <el-table-column prop="name" label="参数名称" width="200"></el-table-column>
          <el-table-column prop="value" label="参数值"></el-table-column>
        </el-table>
      </el-card>

      <!-- 销售数据 -->
      <el-card shadow="never" class="detail-section">
        <template #header>
          <span>销售数据</span>
        </template>
        <el-row :gutter="20">
          <el-col :span="6" v-for="stat in salesStats" :key="stat.key">
            <div class="stat-item">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 评价信息 -->
      <el-card shadow="never" class="detail-section">
        <template #header>
          <span>评价信息</span>
        </template>
        <div class="review-summary">
          <div class="rating-overview">
            <div class="overall-rating">
              <span class="rating-score">{{ product.rating }}</span>
              <el-rate
                v-model="product.rating"
                disabled
                show-score
                text-color="#ff9900"
                score-template="{value} 分"
              />
            </div>
            <div class="rating-stats">
              <div class="rating-item" v-for="(count, star) in product.ratingStats" :key="star">
                <span class="star-label">{{ star }}星</span>
                <el-progress
                  :percentage="(count / product.totalReviews) * 100"
                  :show-text="false"
                  :stroke-width="8"
                />
                <span class="count">{{ count }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="recent-reviews" v-if="product.recentReviews">
          <h4>最新评价</h4>
          <div class="review-item" v-for="review in product.recentReviews" :key="review.id">
            <div class="review-header">
              <span class="reviewer">{{ review.username }}</span>
              <el-rate v-model="review.rating" disabled size="small" />
              <span class="review-time">{{ formatTime(review.createTime) }}</span>
            </div>
            <div class="review-content">{{ review.content }}</div>
          </div>
        </div>
      </el-card>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button type="primary" @click="editProduct">编辑商品</el-button>
        <el-button
          :type="product?.status === 'on_sale' ? 'warning' : 'success'"
          @click="toggleStatus">
          {{ product?.status === 'on_sale' ? '下架商品' : '上架商品' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  productId: {
    type: [String, Number],
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'refresh'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const product = ref(null)
const loading = ref(false)

// 模拟商品数据
const mockProduct = {
  id: 1,
  name: 'iPhone 15 Pro Max 256GB 深空黑色',
  sku: 'IP15PM256BK',
  category: 'electronics',
  status: 'on_sale',
  price: '9999.00',
  marketPrice: '10999.00',
  stock: 156,
  sales: 2580,
  weight: 0.221,
  rating: 4.8,
  totalReviews: 1256,
  mainImage: 'https://trae-api-us.mchost.guru/api/ide/v1/text_to_image?prompt=iPhone%2015%20Pro%20Max%20black%20smartphone%20product%20photo&image_size=square',
  images: [
    'https://trae-api-us.mchost.guru/api/ide/v1/text_to_image?prompt=iPhone%2015%20Pro%20Max%20black%20smartphone%20product%20photo&image_size=square',
    'https://trae-api-us.mchost.guru/api/ide/v1/text_to_image?prompt=iPhone%2015%20Pro%20Max%20back%20view%20camera%20system&image_size=square',
    'https://trae-api-us.mchost.guru/api/ide/v1/text_to_image?prompt=iPhone%2015%20Pro%20Max%20side%20view%20titanium%20frame&image_size=square',
    'https://trae-api-us.mchost.guru/api/ide/v1/text_to_image?prompt=iPhone%2015%20Pro%20Max%20screen%20display%20interface&image_size=square'
  ],
  description: `
    <h3>产品特色</h3>
    <p>iPhone 15 Pro Max 采用钛金属设计，搭载 A17 Pro 芯片，配备专业级摄像头系统。</p>
    <h3>主要功能</h3>
    <ul>
      <li>6.7 英寸超视网膜 XDR 显示屏</li>
      <li>A17 Pro 芯片，6 核中央处理器</li>
      <li>专业级摄像头系统，支持 5 倍光学变焦</li>
      <li>钛金属设计，更轻更坚固</li>
      <li>支持 USB-C 接口</li>
    </ul>
  `,
  specifications: [
    { name: '屏幕尺寸', value: '6.7 英寸' },
    { name: '分辨率', value: '2796 x 1290 像素' },
    { name: '处理器', value: 'A17 Pro 芯片' },
    { name: '存储容量', value: '256GB' },
    { name: '后置摄像头', value: '4800万像素主摄 + 1200万像素超广角 + 1200万像素长焦' },
    { name: '前置摄像头', value: '1200万像素' },
    { name: '电池容量', value: '4441mAh' },
    { name: '操作系统', value: 'iOS 17' }
  ],
  ratingStats: {
    5: 856,
    4: 245,
    3: 89,
    2: 34,
    1: 32
  },
  recentReviews: [
    {
      id: 1,
      username: '张***',
      rating: 5,
      content: '手机很不错，拍照效果很好，系统流畅，值得购买！',
      createTime: '2024-01-15 14:30:00'
    },
    {
      id: 2,
      username: '李***',
      rating: 4,
      content: '整体满意，就是价格有点贵，不过质量确实好。',
      createTime: '2024-01-14 16:20:00'
    },
    {
      id: 3,
      username: '王***',
      rating: 5,
      content: '苹果的品质一如既往的好，推荐购买！',
      createTime: '2024-01-13 10:15:00'
    }
  ],
  createTime: '2024-01-10 10:30:00',
  updateTime: '2024-01-15 09:20:00'
}

// 销售统计数据
const salesStats = computed(() => [
  {
    key: 'today_sales',
    label: '今日销量',
    value: '23'
  },
  {
    key: 'week_sales',
    label: '本周销量',
    value: '156'
  },
  {
    key: 'month_sales',
    label: '本月销量',
    value: '678'
  },
  {
    key: 'total_revenue',
    label: '总收入',
    value: '¥258万'
  }
])

// 方法

/**
 * 获取第一张图片URL（处理逗号分隔的多图片URL）
 * @param imageUrl 图片URL字符串，可能包含逗号分隔的多个URL
 * @returns 第一张图片的URL
 */
const getFirstImage = (imageUrl) => {
  if (!imageUrl) return ''
  if (imageUrl.includes(',')) {
    return imageUrl.split(',')[0].trim()
  }
  return imageUrl
}

const getCategoryName = (category) => {
  const categoryMap = {
    'electronics': '数码电子',
    'clothing': '服装鞋帽',
    'home': '家居用品',
    'beauty': '美妆护肤',
    'food': '食品饮料',
    'sports': '运动户外'
  }
  return categoryMap[category] || '未知分类'
}

const getStatusType = (status) => {
  const statusMap = {
    'on_sale': 'success',
    'off_sale': 'info',
    'sold_out': 'warning',
    'pending': 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    'on_sale': '在售',
    'off_sale': '下架',
    'sold_out': '售罄',
    'pending': '待审核'
  }
  return statusMap[status] || '未知'
}

const formatTime = (time) => {
  return new Date(time).toLocaleString('zh-CN')
}

const loadProductDetail = async () => {
  if (!props.productId) return
  
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 这里应该调用真实的API
    // const response = await productApi.getProductDetail(props.productId)
    // product.value = response.data
    
    // 使用模拟数据
    product.value = { ...mockProduct, id: props.productId }
    
  } catch (error) {
    ElMessage.error('加载商品详情失败')
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  visible.value = false
}

const editProduct = () => {
  // 跳转到编辑页面
  console.log('编辑商品:', props.productId)
  handleClose()
}

const toggleStatus = async () => {
  if (!product.value) return
  
  const newStatus = product.value.status === 'on_sale' ? 'off_sale' : 'on_sale'
  const actionText = newStatus === 'on_sale' ? '上架' : '下架'
  
  try {
    await ElMessageBox.confirm(
      `确定要${actionText}该商品吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用API
    // await productApi.updateProductStatus(props.productId, newStatus)
    
    product.value.status = newStatus
    ElMessage.success(`商品${actionText}成功`)
    emit('refresh')
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 监听商品ID变化
watch(() => props.productId, () => {
  if (props.productId && visible.value) {
    loadProductDetail()
  }
}, { immediate: true })

// 监听对话框显示状态
watch(visible, (newVisible) => {
  if (newVisible && props.productId) {
    loadProductDetail()
  }
})
</script>

<style scoped>
.product-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

/* 商品图片 */
.product-images {
  text-align: center;
}

.main-image {
  width: 100%;
  height: 300px;
  border-radius: 8px;
  margin-bottom: 12px;
}

.image-list {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.thumb-image {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.2s;
}

.thumb-image:hover {
  transform: scale(1.1);
}

/* 商品信息 */
.product-info {
  padding-left: 20px;
}

.product-title {
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 600;
  color: #262626;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.label {
  width: 100px;
  color: #8c8c8c;
  font-size: 14px;
}

.value {
  color: #262626;
  font-size: 14px;
}

.price {
  font-size: 18px;
  font-weight: 600;
  color: #f5222d;
}

.market-price {
  color: #8c8c8c;
  text-decoration: line-through;
}

.stock {
  font-weight: 500;
  color: #52c41a;
}

.low-stock {
  color: #faad14;
}

/* 商品描述 */
.product-description {
  line-height: 1.6;
}

.product-description h3 {
  color: #262626;
  margin: 16px 0 8px 0;
}

.product-description ul {
  padding-left: 20px;
}

.product-description li {
  margin-bottom: 4px;
}

/* 销售数据 */
.stat-item {
  text-align: center;
  padding: 20px;
  background-color: #fafafa;
  border-radius: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #8c8c8c;
}

/* 评价信息 */
.review-summary {
  margin-bottom: 20px;
}

.rating-overview {
  display: flex;
  gap: 40px;
  align-items: flex-start;
}

.overall-rating {
  text-align: center;
}

.rating-score {
  display: block;
  font-size: 36px;
  font-weight: 600;
  color: #ff9900;
  margin-bottom: 8px;
}

.rating-stats {
  flex: 1;
  max-width: 300px;
}

.rating-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.star-label {
  width: 40px;
  font-size: 14px;
  color: #8c8c8c;
}

.count {
  width: 40px;
  text-align: right;
  font-size: 14px;
  color: #8c8c8c;
}

.recent-reviews h4 {
  margin: 0 0 16px 0;
  color: #262626;
}

.review-item {
  padding: 16px;
  background-color: #fafafa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.reviewer {
  font-weight: 500;
  color: #262626;
}

.review-time {
  font-size: 12px;
  color: #8c8c8c;
  margin-left: auto;
}

.review-content {
  color: #595959;
  line-height: 1.5;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-detail {
    max-height: 60vh;
  }
  
  .product-info {
    padding-left: 0;
    margin-top: 20px;
  }
  
  .rating-overview {
    flex-direction: column;
    gap: 20px;
  }
  
  .rating-stats {
    max-width: none;
  }
  
  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .label {
    width: auto;
    font-weight: 500;
  }
}
</style>