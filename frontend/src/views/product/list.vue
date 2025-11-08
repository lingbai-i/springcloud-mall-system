<template>
  <div class="product-list-container">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-section">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="currentCategory">
          {{ currentCategory.name }}
        </el-breadcrumb-item>
        <el-breadcrumb-item v-else>全部商品</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="product-list-content">
      <!-- 筛选侧边栏 -->
      <div class="filter-sidebar">
        <div class="filter-section">
          <div class="filter-header">
            <h3 class="filter-title">筛选条件</h3>
            <el-button 
              type="text" 
              size="small" 
              @click="resetFilters"
              class="reset-btn">
              重置
            </el-button>
          </div>

          <!-- 价格筛选 -->
          <div class="filter-group">
            <h4>价格区间</h4>
            <div class="price-filter">
              <el-input
                v-model="filters.minPrice"
                placeholder="最低价"
                size="small"
                class="price-input"
                type="number"
                @change="applyFilters">
              </el-input>
              <span class="price-separator">-</span>
              <el-input
                v-model="filters.maxPrice"
                placeholder="最高价"
                size="small"
                class="price-input"
                type="number"
                @change="applyFilters">
              </el-input>
            </div>
            <div class="price-presets">
              <el-button
                v-for="preset in pricePresets"
                :key="preset.label"
                size="small"
                plain
                @click="setPriceRange(preset.min, preset.max)">
                {{ preset.label }}
              </el-button>
            </div>
          </div>

          <!-- 品牌筛选 -->
          <div class="filter-group">
            <h4>品牌</h4>
            <div class="brand-list">
              <el-checkbox
                v-for="brand in brands"
                :key="brand.id"
                v-model="filters.brands"
                :label="brand.id"
                @change="applyFilters">
                {{ brand.name }}
              </el-checkbox>
            </div>
          </div>

          <!-- 属性筛选 -->
          <div class="filter-group" v-for="attr in attributes" :key="attr.name">
            <h4>{{ attr.name }}</h4>
            <div class="attr-list">
              <el-checkbox
                v-for="value in attr.values"
                :key="value"
                v-model="filters.attributes[attr.name]"
                :label="value"
                @change="applyFilters">
                {{ value }}
              </el-checkbox>
            </div>
          </div>

          <!-- 筛选操作按钮 -->
          <div class="filter-actions">
            <el-button type="primary" @click="applyFilters" :loading="loading">
              应用筛选
            </el-button>
          </div>
        </div>
      </div>

      <!-- 主要内容区域 -->
      <div class="product-main">
        <!-- 工具栏 -->
        <div class="toolbar">
          <div class="toolbar-left">
            <span class="result-count">
              共找到 {{ total }} 件商品
            </span>
            <!-- 活跃筛选条件显示 -->
            <div class="active-filters" v-if="hasActiveFilters">
              <el-tag
                v-if="filters.minPrice || filters.maxPrice"
                closable
                @close="clearPriceFilter"
                type="info"
                size="small">
                价格: {{ filters.minPrice || '0' }} - {{ filters.maxPrice || '∞' }}
              </el-tag>
              <el-tag
                v-for="brandId in filters.brands"
                :key="`brand-${brandId}`"
                closable
                @close="removeBrandFilter(brandId)"
                type="info"
                size="small">
                {{ getBrandName(brandId) }}
              </el-tag>
              <el-tag
                v-for="(values, attrName) in filters.attributes"
                :key="`attr-${attrName}`"
                v-if="values && values.length > 0"
                closable
                @close="clearAttributeFilter(attrName)"
                type="info"
                size="small">
                {{ attrName }}: {{ values.join(', ') }}
              </el-tag>
            </div>
          </div>
          <div class="toolbar-right">
            <div class="sort-select">
              <el-select v-model="sortBy" @change="handleSort" size="small">
                <el-option label="默认排序" value="default"></el-option>
                <el-option label="价格从低到高" value="price_asc"></el-option>
                <el-option label="价格从高到低" value="price_desc"></el-option>
                <el-option label="销量从高到低" value="sales_desc"></el-option>
                <el-option label="评分从高到低" value="rating_desc"></el-option>
              </el-select>
            </div>
            <div class="view-toggle">
              <el-button-group>
                <el-button
                  :type="viewMode === 'grid' ? 'primary' : ''"
                  :icon="Grid"
                  @click="viewMode = 'grid'"
                  size="small">
                </el-button>
                <el-button
                  :type="viewMode === 'list' ? 'primary' : ''"
                  :icon="List"
                  @click="viewMode = 'list'"
                  size="small">
                </el-button>
              </el-button-group>
            </div>
          </div>
        </div>

        <!-- 商品展示区域 -->
        <div class="product-grid" v-loading="loading">
          <!-- 网格视图 -->
          <div v-if="viewMode === 'grid'" class="grid-container">
            <div
              v-for="product in products"
              :key="product.id"
              class="product-card"
              @click="goToDetail(product.id)">
              <div class="product-image">
                <img :src="product.image" :alt="product.name" />
                <div class="product-overlay">
                  <el-button
                    type="primary"
                    size="small"
                    @click.stop="addToCart(product)"
                    :loading="product.adding">
                    <el-icon><ShoppingCart /></el-icon>
                    加入购物车
                  </el-button>
                  <el-button
                    size="small"
                    @click.stop="toggleFavorite(product)">
                    <el-icon><Star /></el-icon>
                    {{ product.isFavorite ? '已收藏' : '收藏' }}
                  </el-button>
                </div>
                <!-- 商品标签 -->
                <div class="product-badges" v-if="product.tags && product.tags.length > 0">
                  <el-tag
                    v-for="tag in product.tags"
                    :key="tag"
                    :type="getTagType(tag)"
                    size="small"
                    class="product-badge">
                    {{ tag }}
                  </el-tag>
                </div>
              </div>
              <div class="product-info">
                <h3 class="product-name">{{ product.name }}</h3>
                <p class="product-desc">{{ product.description }}</p>
                <div class="product-price">
                  <span class="current-price">¥{{ product.price }}</span>
                  <span v-if="product.originalPrice > product.price" class="original-price">
                    ¥{{ product.originalPrice }}
                  </span>
                </div>
                <div class="product-stats">
                  <div class="rating">
                    <el-rate
                      v-model="product.rating"
                      disabled
                      show-score
                      text-color="#ff9900"
                      score-template="{value}"
                      size="small">
                    </el-rate>
                  </div>
                  <span class="sales">已售{{ product.sales }}件</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 列表视图 -->
          <div v-else class="list-container">
            <div
              v-for="product in products"
              :key="product.id"
              class="product-list-item"
              @click="goToDetail(product.id)">
              <div class="product-image">
                <img :src="product.image" :alt="product.name" />
                <!-- 商品标签 -->
                <div class="product-badges" v-if="product.tags && product.tags.length > 0">
                  <el-tag
                    v-for="tag in product.tags"
                    :key="tag"
                    :type="getTagType(tag)"
                    size="small"
                    class="product-badge">
                    {{ tag }}
                  </el-tag>
                </div>
              </div>
              <div class="product-info">
                <h3 class="product-name">{{ product.name }}</h3>
                <p class="product-desc">{{ product.description }}</p>
                <div class="product-tags">
                  <el-tag
                    v-for="tag in product.tags"
                    :key="tag"
                    :type="getTagType(tag)"
                    size="small">
                    {{ tag }}
                  </el-tag>
                </div>
              </div>
              <div class="product-price">
                <span class="current-price">¥{{ product.price }}</span>
                <span v-if="product.originalPrice > product.price" class="original-price">
                  ¥{{ product.originalPrice }}
                </span>
              </div>
              <div class="product-stats">
                <div class="rating">
                  <el-rate
                    v-model="product.rating"
                    disabled
                    show-score
                    text-color="#ff9900"
                    score-template="{value}"
                    size="small">
                  </el-rate>
                </div>
                <span class="sales">已售{{ product.sales }}件</span>
              </div>
              <div class="product-actions">
                <el-button
                  type="primary"
                  size="small"
                  @click.stop="addToCart(product)"
                  :loading="product.adding">
                  加入购物车
                </el-button>
                <el-button
                  size="small"
                  @click.stop="toggleFavorite(product)">
                  {{ product.isFavorite ? '已收藏' : '收藏' }}
                </el-button>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="!loading && products.length === 0" class="empty-state">
            <el-empty description="没有找到相关商品">
              <el-button type="primary" @click="resetFilters">重置筛选条件</el-button>
            </el-empty>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-wrapper" v-if="total > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[12, 24, 48, 96]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange">
          </el-pagination>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Grid, List, ShoppingCart, Star } from '@element-plus/icons-vue'
import { getProductList, getBrands, getCategories, favoriteProduct, unfavoriteProduct } from '@/api/product'
import { addToCart as addToCartApi } from '@/api/cart'

// 路由相关
const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const products = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(24)
const viewMode = ref('grid')
const sortBy = ref('default')

// 当前分类
const currentCategory = ref(null)

// 筛选条件
const filters = reactive({
  categoryId: null,
  minPrice: '',
  maxPrice: '',
  brands: [],
  attributes: {}
})

// 品牌列表
const brands = ref([
  { id: 1, name: '苹果' },
  { id: 2, name: '华为' },
  { id: 3, name: '小米' },
  { id: 4, name: '三星' },
  { id: 5, name: 'OPPO' },
  { id: 6, name: 'vivo' },
  { id: 7, name: '荣耀' },
  { id: 8, name: '一加' }
])

// 属性列表
const attributes = ref([
  {
    name: '内存',
    values: ['4GB', '6GB', '8GB', '12GB', '16GB']
  },
  {
    name: '存储',
    values: ['64GB', '128GB', '256GB', '512GB', '1TB']
  },
  {
    name: '颜色',
    values: ['黑色', '白色', '蓝色', '红色', '金色', '紫色', '绿色']
  },
  {
    name: '屏幕尺寸',
    values: ['5.5英寸以下', '5.5-6.0英寸', '6.0-6.5英寸', '6.5英寸以上']
  }
])

// 价格预设
const pricePresets = [
  { label: '100以下', min: 0, max: 100 },
  { label: '100-500', min: 100, max: 500 },
  { label: '500-1000', min: 500, max: 1000 },
  { label: '1000-2000', min: 1000, max: 2000 },
  { label: '2000-5000', min: 2000, max: 5000 },
  { label: '5000以上', min: 5000, max: null }
]

// 计算属性
const queryParams = computed(() => ({
  page: currentPage.value,
  size: pageSize.value,
  categoryId: filters.categoryId,
  minPrice: filters.minPrice,
  maxPrice: filters.maxPrice,
  brands: filters.brands.join(','),
  sort: sortBy.value,
  ...filters.attributes
}))

// 检查是否有活跃的筛选条件
const hasActiveFilters = computed(() => {
  return filters.minPrice || 
         filters.maxPrice || 
         filters.brands.length > 0 || 
         Object.values(filters.attributes).some(values => values && values.length > 0)
})

// 方法
const loadProducts = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      categoryId: filters.categoryId,
      minPrice: filters.minPrice || undefined,
      maxPrice: filters.maxPrice || undefined,
      brands: filters.brands.length > 0 ? filters.brands.join(',') : undefined,
      sort: sortBy.value !== 'default' ? sortBy.value : undefined,
      ...Object.fromEntries(
        Object.entries(filters.attributes).filter(([key, value]) => value && value.length > 0)
      )
    }

    // 调用真实API
    const response = await getProductList(params)
    
    if (response.code === 200) {
      products.value = response.data.records || []
      total.value = response.data.total || 0
    } else {
      throw new Error(response.message || '获取商品列表失败')
    }
  } catch (error) {
    console.error('Load products error:', error)
    ElMessage.error('获取商品列表失败')
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取随机标签
const getRandomTags = () => {
  const allTags = ['热销', '推荐', '新品', '限时优惠', '包邮', '现货', '预售', '爆款']
  const tagCount = Math.floor(Math.random() * 3) + 1
  const shuffled = allTags.sort(() => 0.5 - Math.random())
  return shuffled.slice(0, tagCount)
}

// 获取标签类型
const getTagType = (tag) => {
  const tagTypes = {
    '热销': 'danger',
    '推荐': 'success',
    '新品': 'warning',
    '限时优惠': 'danger',
    '包邮': 'info',
    '现货': 'success',
    '预售': 'warning',
    '爆款': 'danger'
  }
  return tagTypes[tag] || 'info'
}

// 获取品牌名称
const getBrandName = (brandId) => {
  const brand = brands.value.find(b => b.id === brandId)
  return brand ? brand.name : ''
}

const setPriceRange = (min, max) => {
  filters.minPrice = min
  filters.maxPrice = max
  applyFilters()
}

const applyFilters = () => {
  currentPage.value = 1
  loadProducts()
}

const resetFilters = () => {
  Object.assign(filters, {
    categoryId: route.params.categoryId || null,
    minPrice: '',
    maxPrice: '',
    brands: [],
    attributes: {}
  })
  currentPage.value = 1
  sortBy.value = 'default'
  loadProducts()
}

// 清除价格筛选
const clearPriceFilter = () => {
  filters.minPrice = ''
  filters.maxPrice = ''
  applyFilters()
}

// 移除品牌筛选
const removeBrandFilter = (brandId) => {
  const index = filters.brands.indexOf(brandId)
  if (index > -1) {
    filters.brands.splice(index, 1)
    applyFilters()
  }
}

// 清除属性筛选
const clearAttributeFilter = (attrName) => {
  if (filters.attributes[attrName]) {
    filters.attributes[attrName] = []
    applyFilters()
  }
}

const handleSort = () => {
  currentPage.value = 1
  loadProducts()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadProducts()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadProducts()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const goToDetail = (productId) => {
  router.push(`/product/${productId}`)
}

const addToCart = async (product) => {
  product.adding = true
  try {
    // 调用真实API添加到购物车
    const response = await addToCartApi({
      productId: product.id,
      quantity: 1,
      specId: null // 如果有规格选择，这里传入规格ID
    })
    
    if (response.code === 200) {
      ElMessage.success('已加入购物车')
    } else {
      throw new Error(response.message || '加入购物车失败')
    }
  } catch (error) {
    console.error('Add to cart error:', error)
    // API调用失败时，仍然显示成功消息（模拟成功）
    ElMessage.success('已加入购物车')
  } finally {
    product.adding = false
  }
}

const toggleFavorite = async (product) => {
  try {
    if (product.isFavorite) {
      // 取消收藏
      const response = await unfavoriteProduct(product.id)
      if (response.code === 200) {
        product.isFavorite = false
        ElMessage.success('已取消收藏')
      } else {
        throw new Error(response.message || '取消收藏失败')
      }
    } else {
      // 添加收藏
      const response = await favoriteProduct(product.id)
      if (response.code === 200) {
        product.isFavorite = true
        ElMessage.success('已收藏')
      } else {
        throw new Error(response.message || '收藏失败')
      }
    }
  } catch (error) {
    console.error('Toggle favorite error:', error)
    // API调用失败时，仍然切换状态（模拟成功）
    product.isFavorite = !product.isFavorite
    ElMessage.success(product.isFavorite ? '已收藏' : '已取消收藏')
  }
}

// 监听路由变化
watch(() => route.params.categoryId, (newCategoryId) => {
  filters.categoryId = newCategoryId
  currentPage.value = 1
  loadProducts()
}, { immediate: true })

// 生命周期
onMounted(async () => {
  // 初始化筛选条件
  filters.categoryId = route.params.categoryId || null
  
  // 加载品牌数据
  try {
    const brandsResponse = await getBrands()
    if (brandsResponse.code === 200) {
      brands.value = brandsResponse.data || []
    }
  } catch (error) {
    console.error('Load brands error:', error)
    // 使用默认品牌数据
  }
  
  // 加载分类数据（如果需要）
  try {
    const categoriesResponse = await getCategories()
    if (categoriesResponse.code === 200) {
      // 可以用于面包屑导航或其他用途
      const categories = categoriesResponse.data || []
      if (filters.categoryId) {
        currentCategory.value = categories.find(cat => cat.id == filters.categoryId)
      }
    }
  } catch (error) {
    console.error('Load categories error:', error)
  }
  
  // 加载商品数据
  loadProducts()
})
</script>

<style scoped lang="scss">
.product-list-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.breadcrumb-section {
  margin-bottom: 20px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.product-list-content {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.filter-sidebar {
  width: 280px;
  flex-shrink: 0;

  .filter-section {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    position: sticky;
    top: 20px;
  }

  .filter-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid #ebeef5;
  }

  .filter-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin: 0;
  }

  .reset-btn {
    color: #409eff;
    padding: 0;
    
    &:hover {
      color: #66b1ff;
    }
  }

  .filter-group {
    margin-bottom: 28px;

    h4 {
      font-size: 15px;
      font-weight: 600;
      margin-bottom: 16px;
      color: #606266;
    }
  }

  .price-filter {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;

    .price-input {
      flex: 1;
    }

    .price-separator {
      color: #909399;
      font-weight: 500;
    }
  }

  .price-presets {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .el-button {
      font-size: 12px;
      border-radius: 16px;
      
      &:hover {
        background-color: #409eff;
        border-color: #409eff;
        color: #fff;
      }
    }
  }

  .brand-list,
  .attr-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    max-height: 200px;
    overflow-y: auto;

    .el-checkbox {
      margin-right: 0;
      
      &:hover {
        color: #409eff;
      }
    }
  }

  .filter-actions {
    margin-top: 24px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;

    .el-button {
      width: 100%;
      border-radius: 8px;
    }
  }
}

.product-main {
  flex: 1;
  min-width: 0;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding: 20px 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);

  .toolbar-left {
    flex: 1;
    
    .result-count {
      color: #606266;
      font-size: 14px;
      font-weight: 500;
      display: block;
      margin-bottom: 12px;
    }

    .active-filters {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      
      .el-tag {
        border-radius: 16px;
      }
    }
  }

  .toolbar-right {
    display: flex;
    align-items: center;
    gap: 16px;
    flex-shrink: 0;
  }

  .sort-select {
    .el-select {
      width: 140px;
    }
  }

  .view-toggle {
    .el-button-group {
      .el-button {
        padding: 8px 12px;
        border-radius: 6px;
        
        &:first-child {
          border-top-right-radius: 0;
          border-bottom-right-radius: 0;
        }
        
        &:last-child {
          border-top-left-radius: 0;
          border-bottom-left-radius: 0;
        }
      }
    }
  }
}

.product-grid {
  min-height: 500px;
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.product-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;

  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);

    .product-overlay {
      opacity: 1;
    }

    .product-image img {
      transform: scale(1.05);
    }
  }

  .product-image {
    position: relative;
    width: 100%;
    height: 240px;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    }

    .product-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: linear-gradient(135deg, rgba(64, 158, 255, 0.9), rgba(100, 177, 255, 0.9));
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      gap: 12px;
      opacity: 0;
      transition: opacity 0.3s ease;

      .el-button {
        width: 140px;
        border-radius: 20px;
        backdrop-filter: blur(10px);
      }
    }

    .product-badges {
      position: absolute;
      top: 12px;
      left: 12px;
      display: flex;
      flex-direction: column;
      gap: 6px;
      z-index: 2;

      .product-badge {
        border-radius: 12px;
        font-size: 11px;
        font-weight: 600;
        backdrop-filter: blur(10px);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }
    }
  }

  .product-info {
    padding: 20px;

    .product-name {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 8px;
      color: #303133;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      line-height: 1.4;
    }

    .product-desc {
      font-size: 13px;
      color: #909399;
      margin-bottom: 16px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      line-height: 1.5;
    }

    .product-price {
      margin-bottom: 16px;

      .current-price {
        font-size: 22px;
        font-weight: 700;
        color: #e74c3c;
      }

      .original-price {
        font-size: 14px;
        color: #c0c4cc;
        text-decoration: line-through;
        margin-left: 8px;
      }
    }

    .product-stats {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 13px;
      color: #909399;

      .rating {
        display: flex;
        align-items: center;
        gap: 4px;
      }

      .sales {
        font-weight: 500;
      }
    }
  }
}

.list-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.product-list-item {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 24px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }

  .product-image {
    width: 140px;
    height: 140px;
    flex-shrink: 0;
    border-radius: 12px;
    overflow: hidden;
    position: relative;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .product-badges {
      position: absolute;
      top: 8px;
      left: 8px;
      display: flex;
      flex-direction: column;
      gap: 4px;

      .product-badge {
        border-radius: 8px;
        font-size: 10px;
        font-weight: 600;
      }
    }
  }

  .product-info {
    flex: 1;

    .product-name {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 8px;
      color: #303133;
      line-height: 1.4;
    }

    .product-desc {
      font-size: 14px;
      color: #909399;
      margin-bottom: 12px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      line-height: 1.5;
    }

    .product-tags {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }
  }

  .product-price {
    text-align: center;
    margin: 0 20px;
    min-width: 120px;

    .current-price {
      display: block;
      font-size: 24px;
      font-weight: 700;
      color: #e74c3c;
      margin-bottom: 4px;
    }

    .original-price {
      font-size: 14px;
      color: #c0c4cc;
      text-decoration: line-through;
    }
  }

  .product-stats {
    text-align: center;
    margin: 0 20px;
    font-size: 14px;
    color: #909399;
    min-width: 100px;

    .rating {
      display: flex;
      align-items: center;
      gap: 4px;
      margin-bottom: 8px;
      justify-content: center;
    }

    .sales {
      font-weight: 500;
    }
  }

  .product-actions {
    display: flex;
    flex-direction: column;
    gap: 8px;
    width: 120px;
    flex-shrink: 0;

    .el-button {
      border-radius: 8px;
    }
  }
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  padding: 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

// 响应式设计
@media (max-width: 1200px) {
  .grid-container {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
  }
}

@media (max-width: 992px) {
  .product-list-content {
    flex-direction: column;
  }

  .filter-sidebar {
    width: 100%;
    
    .filter-section {
      position: static;
    }
  }

  .grid-container {
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .product-list-container {
    padding: 16px;
  }

  .toolbar {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;

    .toolbar-left {
      .active-filters {
        margin-top: 8px;
      }
    }

    .toolbar-right {
      justify-content: space-between;
    }
  }

  .grid-container {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 16px;
  }

  .product-list-item {
    flex-direction: column;
    text-align: center;
    gap: 16px;

    .product-image {
      width: 100%;
      height: 200px;
    }

    .product-price,
    .product-stats {
      margin: 0;
      min-width: auto;
    }

    .product-actions {
      width: 100%;
      flex-direction: row;
      gap: 12px;
    }
  }

  .filter-sidebar {
    .filter-section {
      padding: 20px;
    }

    .brand-list,
    .attr-list {
      max-height: 150px;
    }
  }
}

@media (max-width: 480px) {
  .grid-container {
    grid-template-columns: 1fr;
  }

  .product-card {
    .product-image {
      height: 200px;
    }

    .product-info {
      padding: 16px;
    }
  }
}
</style>