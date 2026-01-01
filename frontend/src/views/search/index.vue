<template>
  <div class="search-page">
    <!-- 顶部导航栏 -->
    <div class="top-nav">
      <button class="back-home-btn" @click="goHome">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/>
        </svg>
        返回首页
      </button>
      <div class="nav-breadcrumb">
        <span @click="goHome">首页</span>
        <span class="separator">/</span>
        <span class="current">搜索结果</span>
      </div>
    </div>

    <!-- 搜索头部区域 -->
    <div class="search-header">
      <div class="search-box-wrapper">
        <div class="search-box" :class="{ 'is-focused': isSearchFocused }">
          <span class="search-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
            </svg>
          </span>
          <input
            ref="searchInputRef"
            v-model="searchQuery"
            type="text"
            class="search-input"
            placeholder="搜索商品、品牌、分类..."
            @focus="handleSearchFocus"
            @blur="handleSearchBlur"
            @input="handleSearchInput"
            @keyup.enter="handleSearch"
          />
          <button v-if="searchQuery" class="clear-btn" @click="clearSearch">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.47 2 2 6.47 2 12s4.47 10 10 10 10-4.47 10-10S17.53 2 12 2zm5 13.59L15.59 17 12 13.41 8.41 17 7 15.59 10.59 12 7 8.41 8.41 7 12 10.59 15.59 7 17 8.41 13.41 12 17 15.59z"/></svg>
          </button>
          <button class="search-btn" @click="handleSearch">搜索</button>
        </div>

        <!-- 搜索下拉面板 -->
        <div v-show="showSearchPanel" class="search-panel">
          <!-- 搜索历史 -->
          <div v-if="searchHistory.length > 0" class="panel-section">
            <div class="panel-header">
              <span class="panel-title">搜索历史</span>
              <button class="panel-action" @click="handleClearHistory">清空</button>
            </div>
            <div class="tag-list">
              <span v-for="item in searchHistory" :key="item" class="tag tag--history" @click="handleTagClick(item)">
                {{ item }}
                <svg class="tag-close" viewBox="0 0 24 24" @click.stop="handleRemoveHistory(item)">
                  <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
                </svg>
              </span>
            </div>
          </div>

          <!-- 热门搜索 -->
          <div v-if="hotKeywords.length > 0" class="panel-section">
            <div class="panel-header">
              <span class="panel-title">🔥 热门搜索</span>
            </div>
            <div class="tag-list">
              <span v-for="(item, index) in hotKeywords" :key="item.id || index" class="tag tag--hot" :class="{ 'tag--top': index < 3 }" @click="handleTagClick(item.productName || item)">
                <span v-if="index < 3" class="tag-rank">{{ index + 1 }}</span>
                {{ item.productName || item }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选条件栏 -->
    <div class="filter-bar">
      <div class="filter-tabs">
        <button v-for="sort in sortOptions" :key="sort.value" class="filter-tab" :class="{ active: filters.sortBy === sort.value }" @click="handleSortChange(sort.value)">
          {{ sort.label }}
        </button>
      </div>

      <div class="filter-controls">
        <el-select v-model="filters.categoryId" placeholder="全部分类" clearable class="filter-select" @change="handleFilterChange">
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
        </el-select>

        <el-popover placement="bottom" :width="280" trigger="click">
          <template #reference>
            <button class="price-filter-btn">
              {{ priceFilterText }}
              <svg viewBox="0 0 24 24"><path d="M7 10l5 5 5-5z"/></svg>
            </button>
          </template>
          <div class="price-filter-panel">
            <div class="price-inputs">
              <el-input-number v-model="filters.minPrice" :min="0" :controls="false" placeholder="最低价" />
              <span class="price-separator">-</span>
              <el-input-number v-model="filters.maxPrice" :min="0" :controls="false" placeholder="最高价" />
            </div>
            <div class="price-shortcuts">
              <button v-for="range in priceRanges" :key="range.label" @click="setPriceRange(range)">{{ range.label }}</button>
            </div>
            <div class="price-actions">
              <el-button size="small" @click="clearPriceFilter">重置</el-button>
              <el-button size="small" type="primary" @click="applyPriceFilter">确定</el-button>
            </div>
          </div>
        </el-popover>
      </div>
    </div>

    <!-- 搜索结果信息 -->
    <div class="result-info">
      <span class="result-count">共找到 <em>{{ total }}</em> 件商品</span>
      <div v-if="hasActiveFilters" class="active-filters">
        <span class="filter-tag" v-if="searchQuery">关键词: {{ searchQuery }}
          <svg @click="clearSearch" viewBox="0 0 24 24"><path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/></svg>
        </span>
        <button class="clear-all-btn" @click="clearAllFilters">清除全部</button>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="product-section">
      <div v-if="loading" class="loading-skeleton">
        <div v-for="i in 12" :key="i" class="skeleton-card">
          <div class="skeleton-image"></div>
          <div class="skeleton-content">
            <div class="skeleton-title"></div>
            <div class="skeleton-price"></div>
          </div>
        </div>
      </div>

      <div v-else-if="products.length > 0" class="product-grid">
        <ProductCard v-for="product in products" :key="product.id" :product="product" @click="goToProduct" @favorite="handleFavorite" />
      </div>

      <div v-else class="empty-state">
        <div class="empty-icon">🔍</div>
        <h3 class="empty-title">没有找到相关商品</h3>
        <p class="empty-desc">换个关键词试试</p>
        <button class="empty-action" @click="clearAllFilters">清除筛选条件</button>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-section">
      <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[12, 24, 36]" :total="total" layout="total, sizes, prev, pager, next" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProductCard from '@/components/ProductCard.vue'
import { searchProducts, getHotSearchKeywords, getCategories, getSearchHistory, addSearchHistory, removeSearchHistory, clearSearchHistory } from '@/api/search'

const route = useRoute()
const router = useRouter()

// 搜索相关
const searchInputRef = ref(null)
const searchQuery = ref('')
const isSearchFocused = ref(false)
const showSearchPanel = ref(false)
const searchHistory = ref([])
const hotKeywords = ref([])
const suggestions = ref([])

// 商品数据
const loading = ref(false)
const products = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const categories = ref([])

// 筛选条件
const filters = reactive({
  categoryId: null,
  minPrice: null,
  maxPrice: null,
  sortBy: 'default'
})

// 排序选项
const sortOptions = [
  { label: '综合', value: 'default' },
  { label: '销量', value: 'sales' },
  { label: '价格', value: 'price_asc' },
  { label: '最新', value: 'newest' }
]

// 价格区间快捷选项
const priceRanges = [
  { label: '0-100', min: 0, max: 100 },
  { label: '100-500', min: 100, max: 500 },
  { label: '500-1000', min: 500, max: 1000 },
  { label: '1000-5000', min: 1000, max: 5000 },
  { label: '5000以上', min: 5000, max: null }
]

// 计算属性
const priceFilterText = computed(() => {
  if (filters.minPrice && filters.maxPrice) return `¥${filters.minPrice}-${filters.maxPrice}`
  if (filters.minPrice) return `¥${filters.minPrice}以上`
  if (filters.maxPrice) return `¥${filters.maxPrice}以下`
  return '价格'
})

const hasActiveFilters = computed(() => {
  return searchQuery.value || filters.categoryId || filters.minPrice || filters.maxPrice
})

// 搜索相关方法
const handleSearchFocus = () => {
  isSearchFocused.value = true
  showSearchPanel.value = true
  loadSearchHistory()
}

const handleSearchBlur = () => {
  isSearchFocused.value = false
  setTimeout(() => { showSearchPanel.value = false }, 200)
}

const handleSearchInput = () => {
  // 可以在这里实现搜索建议
}

const handleSearch = async () => {
  if (searchQuery.value.trim()) {
    addSearchHistory(searchQuery.value.trim())
    loadSearchHistory()
  }
  showSearchPanel.value = false
  currentPage.value = 1
  await fetchProducts()
}

const clearSearch = () => {
  searchQuery.value = ''
  handleSearch()
}

const handleTagClick = (keyword) => {
  searchQuery.value = keyword
  handleSearch()
}

const handleRemoveHistory = (keyword) => {
  removeSearchHistory(keyword)
  loadSearchHistory()
}

const handleClearHistory = () => {
  clearSearchHistory()
  searchHistory.value = []
}

const loadSearchHistory = () => {
  searchHistory.value = getSearchHistory()
}

// 筛选相关方法
const handleSortChange = (value) => {
  filters.sortBy = value
  currentPage.value = 1
  fetchProducts()
}

const handleFilterChange = () => {
  currentPage.value = 1
  fetchProducts()
}

const setPriceRange = (range) => {
  filters.minPrice = range.min
  filters.maxPrice = range.max
}

const clearPriceFilter = () => {
  filters.minPrice = null
  filters.maxPrice = null
}

const applyPriceFilter = () => {
  handleFilterChange()
}

const clearAllFilters = () => {
  searchQuery.value = ''
  filters.categoryId = null
  filters.minPrice = null
  filters.maxPrice = null
  filters.sortBy = 'default'
  currentPage.value = 1
  fetchProducts()
}

// 分页
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchProducts()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchProducts()
}

// 导航操作
const goHome = () => {
  router.push('/')
}

// 商品操作
const goToProduct = (product) => {
  router.push(`/product/${product.id}`)
}

const handleFavorite = ({ product, isFavorited }) => {
  console.log('收藏状态变化:', product.id, isFavorited)
}

// 获取商品列表
const fetchProducts = async () => {
  loading.value = true
  try {
    const response = await searchProducts({
      keyword: searchQuery.value,
      page: currentPage.value,
      size: pageSize.value,
      categoryId: filters.categoryId,
      minPrice: filters.minPrice,
      maxPrice: filters.maxPrice,
      sortBy: filters.sortBy
    })

    if (response.code === 200 && response.data) {
      let rawProducts = response.data.records || response.data || []
      total.value = response.data.total || rawProducts.length

      // 映射字段
      let mappedProducts = rawProducts.map(item => {
        let imageUrl = item.mainImage || item.image || ''
        if (imageUrl && imageUrl.includes(',')) {
          imageUrl = imageUrl.split(',')[0].trim()
        }
        return {
          ...item,
          name: item.productName || item.name,
          image: imageUrl,
          sales: item.salesCount ?? item.sales ?? 0,
          originalPrice: item.marketPrice || item.originalPrice
        }
      })

      // 计算关键词相关性分数
      const keyword = searchQuery.value ? searchQuery.value.toLowerCase().trim() : ''
      if (keyword) {
        mappedProducts = mappedProducts.map(item => {
          const name = (item.name || '').toLowerCase()
          const desc = (item.description || '').toLowerCase()
          const brand = (item.brand || '').toLowerCase()
          
          let relevanceScore = 0
          // 名称完全匹配（最高分）
          if (name === keyword) relevanceScore += 100
          // 名称以关键词开头
          else if (name.startsWith(keyword)) relevanceScore += 80
          // 名称包含关键词
          else if (name.includes(keyword)) relevanceScore += 60
          // 品牌匹配
          if (brand.includes(keyword)) relevanceScore += 30
          // 描述包含关键词
          if (desc.includes(keyword)) relevanceScore += 20
          
          return { ...item, _relevanceScore: relevanceScore }
        })
      }

      // 排序：先按相关性，再按用户选择的排序方式
      mappedProducts.sort((a, b) => {
        // 有关键词时，先按相关性排序
        if (keyword) {
          const relevanceDiff = (b._relevanceScore || 0) - (a._relevanceScore || 0)
          if (relevanceDiff !== 0) return relevanceDiff
        }
        
        // 相关性相同时，按用户选择的排序方式
        if (filters.sortBy === 'sales') {
          return (b.sales || 0) - (a.sales || 0)
        } else if (filters.sortBy === 'price_asc') {
          return (a.price || 0) - (b.price || 0)
        } else if (filters.sortBy === 'price_desc') {
          return (b.price || 0) - (a.price || 0)
        } else if (filters.sortBy === 'newest') {
          return new Date(b.createTime || 0) - new Date(a.createTime || 0)
        }
        // 默认按销量排序
        return (b.sales || 0) - (a.sales || 0)
      })

      products.value = mappedProducts
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败，请重试')
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取热门搜索
const fetchHotKeywords = async () => {
  try {
    const response = await getHotSearchKeywords()
    if (response.code === 200 && response.data) {
      hotKeywords.value = response.data.slice(0, 10)
    }
  } catch (error) {
    console.error('获取热门搜索失败:', error)
  }
}

// 获取分类
const fetchCategories = async () => {
  try {
    const response = await getCategories()
    if (response.code === 200 && response.data) {
      categories.value = response.data
    }
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

// 监听路由参数
watch(() => route.query.q, (newQuery) => {
  if (newQuery) {
    searchQuery.value = newQuery
    handleSearch()
  }
}, { immediate: true })

onMounted(() => {
  const query = route.query.q
  if (query) searchQuery.value = query
  
  loadSearchHistory()
  fetchHotKeywords()
  fetchCategories()
  fetchProducts()
})
</script>

<style scoped>
.search-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  min-height: 100vh;
  background: #f5f7fa;
}

/* 顶部导航 */
.top-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.back-home-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: #fff;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.back-home-btn:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(64,158,255,0.4);
}

.back-home-btn svg {
  width: 16px;
  height: 16px;
}

.nav-breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #909399;
}

.nav-breadcrumb span {
  cursor: pointer;
}

.nav-breadcrumb span:hover:not(.separator):not(.current) {
  color: #409eff;
}

.nav-breadcrumb .separator {
  cursor: default;
}

.nav-breadcrumb .current {
  color: #303133;
  cursor: default;
}

/* 搜索头部 */
.search-header {
  margin-bottom: 20px;
}

.search-box-wrapper {
  position: relative;
  max-width: 680px;
  margin: 0 auto;
}

.search-box {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 24px;
  padding: 4px 4px 4px 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  border: 2px solid transparent;
  transition: all 0.3s;
}

.search-box.is-focused {
  border-color: #409eff;
  box-shadow: 0 4px 20px rgba(64,158,255,0.2);
}

.search-icon {
  width: 20px;
  height: 20px;
  color: #909399;
  margin-right: 8px;
}

.search-icon svg {
  width: 100%;
  height: 100%;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  color: #303133;
  background: transparent;
}

.search-input::placeholder {
  color: #c0c4cc;
}

.clear-btn {
  width: 20px;
  height: 20px;
  padding: 0;
  border: none;
  background: none;
  cursor: pointer;
  color: #c0c4cc;
  margin-right: 8px;
}

.clear-btn:hover {
  color: #909399;
}

.clear-btn svg {
  width: 100%;
  height: 100%;
}

.search-btn {
  padding: 10px 24px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: #fff;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.search-btn:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(64,158,255,0.4);
}

/* 搜索面板 */
.search-panel {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.12);
  padding: 16px;
  z-index: 1000;
}

.panel-section {
  margin-bottom: 16px;
}

.panel-section:last-child {
  margin-bottom: 0;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.panel-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.panel-action {
  font-size: 12px;
  color: #909399;
  background: none;
  border: none;
  cursor: pointer;
}

.panel-action:hover {
  color: #409eff;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  background: #f4f4f5;
  border-radius: 16px;
  font-size: 12px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
}

.tag:hover {
  background: #e9e9eb;
}

.tag--hot {
  background: #fff5f5;
  color: #f56c6c;
}

.tag--top {
  background: linear-gradient(135deg, #ff6b6b 0%, #feca57 100%);
  color: #fff;
}

.tag-rank {
  margin-right: 4px;
  font-weight: 600;
}

.tag-close {
  width: 14px;
  height: 14px;
  margin-left: 4px;
  opacity: 0.6;
}

.tag-close:hover {
  opacity: 1;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 12px 20px;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.filter-tabs {
  display: flex;
  gap: 8px;
}

.filter-tab {
  padding: 8px 16px;
  border: none;
  background: none;
  font-size: 14px;
  color: #606266;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;
}

.filter-tab:hover {
  background: #f5f7fa;
}

.filter-tab.active {
  background: #ecf5ff;
  color: #409eff;
  font-weight: 500;
}

.filter-controls {
  display: flex;
  gap: 12px;
}

.filter-select {
  width: 140px;
}

.price-filter-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  color: #606266;
  cursor: pointer;
}

.price-filter-btn svg {
  width: 16px;
  height: 16px;
}

.price-filter-panel {
  padding: 8px 0;
}

.price-inputs {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.price-separator {
  color: #c0c4cc;
}

.price-shortcuts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.price-shortcuts button {
  padding: 4px 12px;
  background: #f4f4f5;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
  cursor: pointer;
}

.price-shortcuts button:hover {
  background: #e9e9eb;
}

.price-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

/* 结果信息 */
.result-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 4px;
}

.result-count {
  font-size: 14px;
  color: #909399;
}

.result-count em {
  font-style: normal;
  font-weight: 600;
  color: #409eff;
}

.active-filters {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: #ecf5ff;
  border-radius: 4px;
  font-size: 12px;
  color: #409eff;
}

.filter-tag svg {
  width: 14px;
  height: 14px;
  cursor: pointer;
  opacity: 0.6;
}

.filter-tag svg:hover {
  opacity: 1;
}

.clear-all-btn {
  padding: 4px 10px;
  background: none;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 12px;
  color: #909399;
  cursor: pointer;
}

.clear-all-btn:hover {
  border-color: #409eff;
  color: #409eff;
}

/* 商品网格 */
.product-section {
  min-height: 400px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

/* 骨架屏 */
.loading-skeleton {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.skeleton-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.skeleton-image {
  width: 100%;
  padding-top: 100%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-content {
  padding: 12px;
}

.skeleton-title {
  height: 16px;
  background: #f0f0f0;
  border-radius: 4px;
  margin-bottom: 8px;
}

.skeleton-price {
  height: 20px;
  width: 60%;
  background: #f0f0f0;
  border-radius: 4px;
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-title {
  font-size: 18px;
  color: #303133;
  margin: 0 0 8px 0;
}

.empty-desc {
  font-size: 14px;
  color: #909399;
  margin: 0 0 24px 0;
}

.empty-action {
  padding: 10px 24px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
}

.empty-action:hover {
  background: #66b1ff;
}

/* 分页 */
.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}

/* 响应式 */
@media (max-width: 768px) {
  .search-page {
    padding: 12px;
  }

  .filter-bar {
    flex-direction: column;
    gap: 12px;
  }

  .filter-tabs {
    width: 100%;
    justify-content: space-between;
  }

  .filter-controls {
    width: 100%;
    justify-content: space-between;
  }

  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}
</style>
