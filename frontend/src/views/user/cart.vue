<template>
  <div class="user-cart-container">
    <el-card class="cart-card">
      <template #header>
        <div class="card-header">
          <span>我的购物车</span>
          <LocalIcon name="gouwuche" :size="20" color="#409EFF" />
        </div>
      </template>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <!-- 购物车列表 -->
      <div v-else-if="cartItems.length > 0" class="cart-content">
        <!-- 全选 -->
        <div class="cart-header">
          <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
          <span class="header-product">商品信息</span>
          <span class="header-price">单价</span>
          <span class="header-quantity">数量</span>
          <span class="header-subtotal">小计</span>
          <span class="header-action">操作</span>
        </div>

        <!-- 商品列表 -->
        <div class="cart-list">
          <div v-for="item in cartItems" :key="item.id" class="cart-item">
            <el-checkbox v-model="item.selected" @change="handleItemSelect" />
            <div class="item-image" @click="goToProduct(item.productId)">
              <img :src="getFirstImage(item.productImage) || '/placeholder.png'" :alt="item.productName" />
            </div>
            <div class="item-info" @click="goToProduct(item.productId)">
              <h3 class="product-name">{{ item.productName }}</h3>
              <p class="product-spec" v-if="item.specificationName">规格：{{ item.specificationName }}</p>
            </div>
            <div class="item-price">
              <span class="current-price">¥{{ formatPrice(item.price) }}</span>
            </div>
            <div class="item-quantity">
              <el-input-number 
                v-model="item.quantity" 
                :min="1" 
                :max="item.stock || 99"
                size="small"
                @change="(val) => handleQuantityChange(item, val)"
              />
            </div>
            <div class="item-subtotal">
              <span class="subtotal-price">¥{{ formatPrice(item.price * item.quantity) }}</span>
            </div>
            <div class="item-action">
              <el-button type="danger" text @click="removeItem(item.productId)">删除</el-button>
            </div>
          </div>
        </div>

        <!-- 结算栏 -->
        <div class="cart-footer">
          <div class="footer-left">
            <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
            <el-button type="danger" text @click="removeSelected" :disabled="selectedCount === 0">
              删除选中 ({{ selectedCount }})
            </el-button>
          </div>
          <div class="footer-right">
            <div class="total-info">
              <span>已选 <em>{{ selectedCount }}</em> 件商品</span>
              <span class="total-price">合计：<em>¥{{ formatPrice(totalPrice) }}</em></span>
            </div>
            <el-button type="danger" size="large" :disabled="selectedCount === 0" @click="goToCheckout">
              去结算
            </el-button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-else description="购物车是空的">
        <el-button type="primary" @click="goToHome">去逛逛</el-button>
      </el-empty>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import LocalIcon from '@/components/LocalIcon.vue'

const router = useRouter()
const cartStore = useCartStore()

// 获取第一张图片URL（处理逗号分隔的多图片URL）
const getFirstImage = (imageUrl) => {
  if (!imageUrl) return ''
  if (imageUrl.includes(',')) {
    return imageUrl.split(',')[0].trim()
  }
  return imageUrl
}

const loading = ref(false)

// 计算属性
const cartItems = computed(() => cartStore.cartItems || [])
const cartCount = computed(() => cartStore.totalCount || 0)
const selectedCount = computed(() => cartItems.value.filter(item => item.selected).length)
const totalPrice = computed(() => {
  return cartItems.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + (item.price * item.quantity), 0)
})
const selectAll = computed({
  get: () => cartItems.value.length > 0 && cartItems.value.every(item => item.selected),
  set: (val) => handleSelectAll(val)
})

onMounted(async () => {
  loading.value = true
  try {
    await cartStore.fetchCartItems()
  } catch (error) {
    console.error('加载购物车失败:', error)
  } finally {
    loading.value = false
  }
})

const handleSelectAll = (val) => {
  cartItems.value.forEach(item => {
    item.selected = val
  })
}

const handleItemSelect = () => {
  // 触发响应式更新
}

const handleQuantityChange = async (item, newQuantity) => {
  try {
    await cartStore.updateQuantity(item.productId, newQuantity)
  } catch (error) {
    ElMessage.error('更新数量失败')
    // 恢复原数量
    await cartStore.fetchCartItems()
  }
}

const removeItem = async (productId) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await cartStore.removeFromCart(productId)
    ElMessage.success('已删除')
  } catch (error) {
    // 用户取消
  }
}

const removeSelected = async () => {
  const selectedItemsList = cartItems.value.filter(item => item.selected)
  if (selectedItemsList.length === 0) return

  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedItemsList.length} 件商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    for (const item of selectedItemsList) {
      await cartStore.removeFromCart(item.productId)
    }
    ElMessage.success('已删除选中商品')
  } catch (error) {
    // 用户取消
  }
}

const goToProduct = (productId) => {
  router.push(`/product/${productId}`)
}

const goToHome = () => {
  router.push('/')
}

const goToCheckout = () => {
  const selectedItems = cartItems.value.filter(item => item.selected)
  if (selectedItems.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }
  router.push('/checkout')
}

const formatPrice = (price) => {
  if (price === null || price === undefined) return '0.00'
  return Number(price).toFixed(2)
}
</script>

<style scoped>
.user-cart-container {
  padding: 20px;
}

.cart-card {
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

.cart-content {
  display: flex;
  flex-direction: column;
}

.cart-header {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
  font-weight: 500;
  color: #606266;
}

.cart-header .el-checkbox {
  margin-right: 15px;
}

.header-product { flex: 1; }
.header-price { width: 100px; text-align: center; }
.header-quantity { width: 120px; text-align: center; }
.header-subtotal { width: 100px; text-align: center; }
.header-action { width: 80px; text-align: center; }

.cart-list {
  display: flex;
  flex-direction: column;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.cart-item .el-checkbox {
  margin-right: 15px;
}

.item-image {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
  cursor: pointer;
  margin-right: 15px;
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
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-spec {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.item-price {
  width: 100px;
  text-align: center;
}

.current-price {
  color: #F56C6C;
  font-weight: 500;
}

.item-quantity {
  width: 120px;
  display: flex;
  justify-content: center;
}

.item-subtotal {
  width: 100px;
  text-align: center;
}

.subtotal-price {
  color: #F56C6C;
  font-weight: 600;
  font-size: 16px;
}

.item-action {
  width: 80px;
  text-align: center;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  margin-top: 10px;
  border-top: 2px solid #eee;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 30px;
}

.total-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 5px;
}

.total-info em {
  font-style: normal;
  color: #F56C6C;
  font-weight: 600;
}

.total-price em {
  font-size: 24px;
}

@media (max-width: 768px) {
  .cart-header {
    display: none;
  }
  
  .cart-item {
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .item-info {
    width: calc(100% - 110px);
  }
  
  .item-price,
  .item-quantity,
  .item-subtotal,
  .item-action {
    width: auto;
  }
  
  .cart-footer {
    flex-direction: column;
    gap: 15px;
  }
  
  .footer-right {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
