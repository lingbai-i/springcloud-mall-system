<template>
  <div class="favorites-container">
    <el-card class="favorites-card">
      <template #header>
        <div class="card-header">
          <span>我的收藏</span>
          <el-button v-if="selectedIds.length > 0" type="danger" @click="batchDelete">
            批量删除 ({{ selectedIds.length }})
          </el-button>
        </div>
      </template>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <!-- 收藏商品列表 -->
      <div v-else-if="favoritesList.length > 0" class="favorites-list">
        <div
          v-for="item in favoritesList"
          :key="item.id"
          class="favorite-item"
        >
          <el-checkbox
            v-model="selectedIds"
            :value="item.productId"
            class="item-checkbox"
          />
          <div class="item-image" @click="goToProduct(item.productId)">
            <img :src="getFirstImage(item.productImage) || '/placeholder.png'" :alt="item.productName" />
          </div>
          <div class="item-info" @click="goToProduct(item.productId)">
            <h3 class="product-name">{{ item.productName }}</h3>
            <p class="product-desc">{{ item.productDesc || '暂无描述' }}</p>
            <div class="product-price">
              <span class="current-price">¥{{ formatPrice(item.productPrice) }}</span>
              <span v-if="item.originalPrice && item.originalPrice > item.productPrice" class="original-price">
                ¥{{ formatPrice(item.originalPrice) }}
              </span>
            </div>
          </div>
          <div class="item-actions">
            <el-button type="primary" @click="addToCart(item)" :loading="addingToCart === item.productId">
              加入购物车
            </el-button>
            <el-button type="danger" text @click="removeItem(item.productId)">移除</el-button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-else description="暂无收藏商品">
        <el-button type="primary" @click="goToHome">去逛逛</el-button>
      </el-empty>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useFavoriteStore } from '@/stores/favorite'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const favoriteStore = useFavoriteStore()
const cartStore = useCartStore()

const selectedIds = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const addingToCart = ref(null)

// 计算属性
const favoritesList = computed(() => favoriteStore.favorites)
const total = computed(() => favoriteStore.pagination.total)
const loading = computed(() => favoriteStore.loading)

onMounted(() => {
  loadFavorites()
})

const loadFavorites = async () => {
  try {
    const result = await favoriteStore.loadFavorites(currentPage.value, pageSize.value)
    if (!result.success) {
      ElMessage.error(result.message || '加载收藏列表失败')
    }
  } catch (error) {
    ElMessage.error('加载收藏列表失败')
  }
}

const goToProduct = (productId) => {
  router.push(`/product/${productId}`)
}

const goToHome = () => {
  router.push('/')
}

const addToCart = async (item) => {
  try {
    addingToCart.value = item.productId
    await cartStore.addToCart({
      productId: item.productId,
      quantity: 1
    })
    ElMessage.success('已加入购物车')
  } catch (error) {
    ElMessage.error(error.message || '加入购物车失败')
  } finally {
    addingToCart.value = null
  }
}

const removeItem = async (productId) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const result = await favoriteStore.removeFavorite(productId)
    if (result.success) {
      ElMessage.success('已取消收藏')
      // 如果当前页没有数据了，回到上一页
      if (favoritesList.value.length === 0 && currentPage.value > 1) {
        currentPage.value--
        loadFavorites()
      }
    } else {
      ElMessage.error(result.message || '取消收藏失败')
    }
  } catch (error) {
    // 用户取消
  }
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要取消选中的 ${selectedIds.value.length} 个收藏吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const result = await favoriteStore.batchRemoveFavorites(selectedIds.value)
    if (result.success) {
      selectedIds.value = []
      ElMessage.success('已取消收藏')
      // 如果当前页没有数据了，回到上一页
      if (favoritesList.value.length === 0 && currentPage.value > 1) {
        currentPage.value--
        loadFavorites()
      }
    } else {
      ElMessage.error(result.message || '批量取消收藏失败')
    }
  } catch (error) {
    // 用户取消
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  selectedIds.value = []
  loadFavorites()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  selectedIds.value = []
  loadFavorites()
}

const formatPrice = (price) => {
  if (price === null || price === undefined) return '0.00'
  return Number(price).toFixed(2)
}

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
</script>

<style scoped>
.favorites-container {
  padding: 20px;
}

.favorites-card {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.loading-container {
  padding: 20px;
}

.favorites-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.favorite-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 8px;
  transition: all 0.3s;
}

.favorite-item:hover {
  border-color: #409EFF;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
}

.item-checkbox {
  flex-shrink: 0;
}

.item-image {
  width: 120px;
  height: 120px;
  flex-shrink: 0;
  cursor: pointer;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.item-info {
  flex: 1;
  cursor: pointer;
}

.product-name {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #303133;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.current-price {
  font-size: 20px;
  color: #F56C6C;
  font-weight: 600;
}

.original-price {
  font-size: 14px;
  color: #909399;
  text-decoration: line-through;
}

.item-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex-shrink: 0;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .favorite-item {
    flex-direction: column;
  }
  
  .item-image {
    width: 100%;
  }
  
  .item-actions {
    flex-direction: row;
    width: 100%;
  }
  
  .item-actions .el-button {
    flex: 1;
  }
}
</style>
