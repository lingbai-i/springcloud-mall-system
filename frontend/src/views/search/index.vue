<template>
  <div class="search-page">
    <!-- 搜索头部 -->
    <div class="search-header">
      <el-input
        v-model="searchQuery"
        placeholder="搜索商品..."
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch" type="primary">
            <LocalIcon name="sousuo" :size="16" />
          </el-button>
        </template>
      </el-input>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <div class="filter-item">
        <span class="filter-label">分类：</span>
        <el-select v-model="filters.category" placeholder="选择分类" @change="handleSearch">
          <el-option label="全部" value="" />
          <el-option label="数码电子" value="electronics" />
          <el-option label="服装鞋帽" value="clothing" />
          <el-option label="家居用品" value="home" />
          <el-option label="图书音像" value="books" />
        </el-select>
      </div>
      
      <div class="filter-item">
        <span class="filter-label">价格：</span>
        <el-select v-model="filters.priceRange" placeholder="价格区间" @change="handleSearch">
          <el-option label="全部" value="" />
          <el-option label="0-100" value="0-100" />
          <el-option label="100-500" value="100-500" />
          <el-option label="500-1000" value="500-1000" />
          <el-option label="1000以上" value="1000+" />
        </el-select>
      </div>
      
      <div class="filter-item">
        <span class="filter-label">排序：</span>
        <el-select v-model="filters.sortBy" placeholder="排序方式" @change="handleSearch">
          <el-option label="综合排序" value="default" />
          <el-option label="价格从低到高" value="price_asc" />
          <el-option label="价格从高到低" value="price_desc" />
          <el-option label="销量优先" value="sales" />
          <el-option label="最新上架" value="newest" />
        </el-select>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div class="search-results">
      <div class="result-header">
        <span class="result-count">找到 {{ total }} 件商品</span>
      </div>
      
      <div v-loading="loading" class="product-grid">
        <div
          v-for="product in products"
          :key="product.id"
          class="product-card"
          @click="goToProduct(product.id)"
        >
          <div class="product-image">
            <img :src="product.image" :alt="product.name" />
          </div>
          <div class="product-info">
            <h3 class="product-name">{{ product.name }}</h3>
            <p class="product-desc">{{ product.description }}</p>
            <div class="product-price">
              <span class="current-price">¥{{ product.price }}</span>
              <span v-if="product.originalPrice" class="original-price">
                ¥{{ product.originalPrice }}
              </span>
            </div>
            <div class="product-stats">
              <span class="sales">已售 {{ product.sales }}</span>
              <span class="rating">
                <el-rate
                  v-model="product.rating"
                  disabled
                  show-score
                  text-color="#ff9900"
                  score-template="{value}"
                />
              </span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[12, 24, 48]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    
    <!-- 空状态 -->
    <el-empty v-if="!loading && products.length === 0" description="没有找到相关商品" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import LocalIcon from '@/components/LocalIcon.vue'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const searchQuery = ref('')
const products = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

// 筛选条件
const filters = reactive({
  category: '',
  priceRange: '',
  sortBy: 'default'
})

/**
 * 执行搜索
 * @author lingbai
 * @since 2025-01-27
 */
const handleSearch = async () => {
  loading.value = true
  
  try {
    // 调用真实搜索API
    const response = await fetch('/api/products/search', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        keyword: searchQuery.value,
        category: filters.category,
        priceRange: filters.priceRange,
        sortBy: filters.sortBy,
        page: currentPage.value,
        size: pageSize.value
      })
    })
    
    if (!response.ok) {
      throw new Error('搜索请求失败')
    }
    
    const result = await response.json()
    
    if (result.code === 200) {
      products.value = result.data.records || []
      total.value = result.data.total || 0
    } else {
      throw new Error(result.message || '搜索失败')
    }
    
  } catch (error) {
    console.error('Search error:', error)
    ElMessage.error('搜索失败，请重试')
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/**
 * 页面大小改变处理
 * @param size 新的页面大小
 * @author lingbai
 * @since 2025-01-27
 */
const handleSizeChange = (size: number) => {
  pageSize.value = size
  handleSearch()
}

/**
 * 当前页改变处理
 * @param page 新的页码
 * @author lingbai
 * @since 2025-01-27
 */
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  handleSearch()
}

/**
 * 跳转到商品详情页
 * @param productId 商品ID
 * @author lingbai
 * @since 2025-01-27
 */
const goToProduct = (productId: number) => {
  router.push(`/product/${productId}`)
}

// 监听路由参数变化
watch(() => route.query.q, (newQuery) => {
  if (newQuery) {
    searchQuery.value = newQuery as string
    handleSearch()
  }
}, { immediate: true })

onMounted(() => {
  // 如果URL中有搜索参数，自动搜索
  const query = route.query.q as string
  if (query) {
    searchQuery.value = query
  }
  handleSearch()
})
</script>

<style scoped>
.search-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.search-header {
  margin-bottom: 20px;
}

.search-input {
  max-width: 600px;
}

.filter-section {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f5f5;
  border-radius: 8px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-weight: 500;
  color: #666;
  white-space: nowrap;
}

.search-results {
  margin-top: 20px;
}

.result-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.result-count {
  color: #666;
  font-size: 14px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.product-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
}

.product-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.product-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  padding: 15px;
}

.product-name {
  font-size: 16px;
  font-weight: 500;
  margin: 0 0 8px 0;
  color: #333;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-desc {
  font-size: 14px;
  color: #666;
  margin: 0 0 10px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-price {
  margin-bottom: 10px;
}

.current-price {
  font-size: 18px;
  font-weight: bold;
  color: #e74c3c;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
  margin-left: 8px;
}

.product-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #999;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

/* 本地图标样式 */
.local-icon {
  display: inline-block;
  vertical-align: middle;
  object-fit: contain;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-page {
    padding: 15px;
  }
  
  .filter-section {
    flex-direction: column;
    gap: 10px;
  }
  
  .filter-item {
    justify-content: space-between;
  }
  
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 15px;
  }
}
</style>