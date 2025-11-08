<template>
  <div class="category-page">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>{{ categoryName }}</el-breadcrumb-item>
    </el-breadcrumb>
    
    <!-- 分类筛选和排序 -->
    <div class="filter-bar">
      <div class="filter-options">
        <el-button-group>
          <el-button 
            :type="sortBy === 'default' ? 'primary' : ''"
            @click="handleSort('default')"
          >
            综合
          </el-button>
          <el-button 
            :type="sortBy === 'sales' ? 'primary' : ''"
            @click="handleSort('sales')"
          >
            销量
          </el-button>
          <el-button 
            :type="sortBy === 'price' ? 'primary' : ''"
            @click="handleSort('price')"
          >
            价格 <LocalIcon name="paixu" :size="14" />
          </el-button>
          <el-button 
            :type="sortBy === 'time' ? 'primary' : ''"
            @click="handleSort('time')"
          >
            新品
          </el-button>
        </el-button-group>
      </div>
      
      <div class="view-mode">
        <el-button-group>
          <el-button 
            :type="viewMode === 'grid' ? 'primary' : ''"
            @click="viewMode = 'grid'"
          >
            <LocalIcon name="wangge" :size="16" />
          </el-button>
          <el-button 
            :type="viewMode === 'list' ? 'primary' : ''"
            @click="viewMode = 'list'"
          >
            <LocalIcon name="liebiao" :size="16" />
          </el-button>
        </el-button-group>
      </div>
    </div>
    
    <!-- 商品列表 -->
    <div class="product-container" v-loading="loading">
      <!-- 网格视图 -->
      <div v-if="viewMode === 'grid'" class="product-grid">
        <div 
          v-for="product in productList" 
          :key="product.id"
          class="product-card"
          @click="goToProduct(product.id)"
        >
          <div class="product-image">
            <img :src="product.mainImage" :alt="product.name" />
            <div class="product-actions">
              <el-button 
                type="primary" 
                size="small"
                @click.stop="addToCart(product)"
              >
                加入购物车
              </el-button>
            </div>
          </div>
          <div class="product-info">
            <h3 class="product-name">{{ product.name }}</h3>
            <p class="product-desc">{{ product.description }}</p>
            <div class="product-price">
              <span class="current-price">¥{{ product.price }}</span>
              <span class="sales">已售{{ product.sales }}件</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 列表视图 -->
      <div v-else class="product-list">
        <div 
          v-for="product in productList" 
          :key="product.id"
          class="product-item"
          @click="goToProduct(product.id)"
        >
          <div class="item-image">
            <img :src="product.mainImage" :alt="product.name" />
          </div>
          <div class="item-info">
            <h3 class="item-name">{{ product.name }}</h3>
            <p class="item-desc">{{ product.description }}</p>
            <div class="item-specs">
              <span v-for="spec in parseSpecs(product.specifications)" :key="spec">
                {{ spec }}
              </span>
            </div>
          </div>
          <div class="item-price">
            <div class="price">¥{{ product.price }}</div>
            <div class="sales">已售{{ product.sales }}件</div>
            <el-button 
              type="primary" 
              size="small"
              @click.stop="addToCart(product)"
            >
              加入购物车
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <el-empty v-if="!loading && productList.length === 0" description="暂无商品" />
    </div>
    
    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 48, 96]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { getProductsByCategory } from '@/api/product'
import { ElMessage } from 'element-plus'
import LocalIcon from '@/components/LocalIcon.vue'
import { Sort, Grid, List } from '@element-plus/icons-vue'

/**
 * 商品分类页面组件
 * 展示指定分类下的商品列表，支持筛选、排序和分页
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

// 响应式数据
const loading = ref(false)
const productList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const sortBy = ref('default')
const viewMode = ref('grid')
const categoryName = ref('')

/**
 * 获取商品列表
 * @author lingbai
 * @since 2025-01-27
 */
const fetchProducts = async () => {
  loading.value = true
  try {
    const params = {
      categoryId: route.params.id,
      page: currentPage.value,
      size: pageSize.value,
      sortBy: sortBy.value
    }
    
    const response = await getProductsByCategory(params)
    productList.value = response.data.list
    total.value = response.data.total
    categoryName.value = response.data.categoryName
  } catch (error) {
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 处理排序
 * @param type 排序类型
 * @author lingbai
 * @since 2025-01-27
 */
const handleSort = (type) => {
  sortBy.value = type
  currentPage.value = 1
  fetchProducts()
}

/**
 * 处理页面大小改变
 * @param size 新的页面大小
 * @author lingbai
 * @since 2025-01-27
 */
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchProducts()
}

/**
 * 处理当前页改变
 * @param page 新的页码
 * @author lingbai
 * @since 2025-01-27
 */
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchProducts()
}

/**
 * 跳转到商品详情
 * @param productId 商品ID
 * @author lingbai
 * @since 2025-01-27
 */
const goToProduct = (productId) => {
  router.push(`/product/${productId}`)
}

/**
 * 添加到购物车
 * @param product 商品信息
 * @author lingbai
 * @since 2025-01-27
 */
const addToCart = (product) => {
  cartStore.addItem({
    id: product.id,
    name: product.name,
    price: product.price,
    image: product.mainImage,
    quantity: 1
  })
  ElMessage.success('已添加到购物车')
}

/**
 * 解析商品规格
 * @param specs 规格字符串
 * @returns 规格数组
 * @author lingbai
 * @since 2025-01-27
 */
const parseSpecs = (specs) => {
  if (!specs) return []
  return specs.split(',').slice(0, 3) // 只显示前3个规格
}

// 监听路由参数变化
watch(() => route.params.id, () => {
  currentPage.value = 1
  fetchProducts()
}, { immediate: true })

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.category-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.breadcrumb {
  margin-bottom: 20px;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.product-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

.product-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0,0,0,0.7);
  padding: 10px;
  transform: translateY(100%);
  transition: transform 0.3s;
}

.product-card:hover .product-actions {
  transform: translateY(0);
}

.product-info {
  padding: 15px;
}

.product-name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.current-price {
  color: #e74c3c;
  font-size: 18px;
  font-weight: bold;
}

.sales {
  color: #999;
  font-size: 12px;
}

.product-list {
  margin-bottom: 30px;
}

.product-item {
  display: flex;
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 15px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  cursor: pointer;
  transition: box-shadow 0.3s;
}

.product-item:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.item-image {
  width: 120px;
  height: 120px;
  margin-right: 20px;
  flex-shrink: 0;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.item-info {
  flex: 1;
  margin-right: 20px;
}

.item-name {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 10px;
}

.item-desc {
  color: #666;
  margin-bottom: 10px;
  line-height: 1.5;
}

.item-specs {
  display: flex;
  gap: 10px;
}

.item-specs span {
  background: #f5f5f5;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.item-price {
  text-align: right;
  min-width: 120px;
}

.item-price .price {
  color: #e74c3c;
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 5px;
}

.item-price .sales {
  color: #999;
  font-size: 12px;
  margin-bottom: 10px;
}

.pagination {
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
  .category-page {
    padding: 10px;
  }
  
  .filter-bar {
    flex-direction: column;
    gap: 15px;
  }
  
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 10px;
  }
  
  .product-item {
    flex-direction: column;
    text-align: center;
  }
  
  .item-image {
    width: 100%;
    height: 150px;
    margin-right: 0;
    margin-bottom: 10px;
  }
  
  .item-info {
    margin-right: 0;
  }
}
</style>