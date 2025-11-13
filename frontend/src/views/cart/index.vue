<template>
  <div class="cart-container">
    <!-- 返回导航 -->
    <div class="page-navigation">
      <el-button 
        type="text" 
        @click="goBack"
        class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </el-button>
      <el-button 
        type="text" 
        @click="goHome"
        class="home-btn">
        <el-icon><House /></el-icon>
        <span>首页</span>
      </el-button>
    </div>

    <!-- 页面标题 -->
    <div class="page-header">
      <h2>购物车</h2>
      <span class="item-count">共 {{ cartItems.length }} 件商品</span>
    </div>

    <!-- 购物车列表 -->
    <div class="cart-content" v-if="cartItems.length > 0">
      <!-- 表头 -->
      <div class="cart-header">
        <el-checkbox 
          v-model="selectAll" 
          @change="handleSelectAll"
          :indeterminate="isIndeterminate">
          全选
        </el-checkbox>
        <span class="header-item">商品信息</span>
        <span class="header-price">单价</span>
        <span class="header-quantity">数量</span>
        <span class="header-total">小计</span>
        <span class="header-action">操作</span>
      </div>

      <!-- 商品列表 -->
      <div class="cart-items">
        <div 
          v-for="item in cartItems" 
          :key="item.productId" 
          class="cart-item">
          
          <el-checkbox 
            v-model="item.selected" 
            @change="handleItemSelect(item)">
          </el-checkbox>
          
          <!-- 商品信息 -->
          <div class="item-info">
            <img :src="item.productImage" :alt="item.productName" class="product-image">
            <div class="product-details">
              <h4 class="product-name">{{ item.productName }}</h4>
              <p class="product-specs" v-if="item.specifications">{{ item.specifications }}</p>
            </div>
          </div>
          
          <!-- 单价 -->
          <div class="item-price">
            ¥{{ item.price }}
          </div>
          
          <!-- 数量控制 -->
          <div class="item-quantity">
            <el-input-number
              v-model="item.quantity"
              :min="1"
              :max="999"
              @change="handleQuantityChange(item)"
              size="small">
            </el-input-number>
          </div>
          
          <!-- 小计 -->
          <div class="item-total">
            ¥{{ (item.price * item.quantity).toFixed(2) }}
          </div>
          
          <!-- 操作 -->
          <div class="item-actions">
            <el-button 
              type="text" 
              @click="handleRemoveItem(item)"
              class="remove-btn">
              删除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 底部结算栏 -->
      <div class="cart-footer">
        <div class="footer-left">
          <el-button @click="handleClearCart" type="text">清空购物车</el-button>
        </div>
        
        <div class="footer-right">
          <span class="selected-count">
            已选择 {{ selectedItems.length }} 件商品
          </span>
          <span class="total-price">
            合计: ¥{{ totalPrice.toFixed(2) }}
          </span>
          <el-button 
            type="primary" 
            size="large"
            @click="handleCheckout"
            :disabled="selectedItems.length === 0">
            去结算
          </el-button>
        </div>
      </div>
    </div>

    <!-- 空购物车 -->
    <div class="empty-cart" v-else>
      <el-empty description="购物车是空的">
        <el-button type="primary" @click="$router.push('/')">去购物</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, House } from '@element-plus/icons-vue'
import { useCartStore } from '@/stores/cart'
import { useRouter } from 'vue-router'

const cartStore = useCartStore()
const router = useRouter()

// 响应式数据
const cartItems = ref([])
const selectAll = ref(false)

// 计算属性
const selectedItems = computed(() => {
  return cartItems.value.filter(item => item.selected)
})

const totalPrice = computed(() => {
  return selectedItems.value.reduce((total, item) => {
    return total + (item.price * item.quantity)
  }, 0)
})

const isIndeterminate = computed(() => {
  const selectedCount = selectedItems.value.length
  return selectedCount > 0 && selectedCount < cartItems.value.length
})

// 方法
const loadCartItems = async () => {
  try {
    // 使用购物车store加载数据
    await cartStore.fetchCartItems()
    cartItems.value = cartStore.cartItems
    
    // 更新全选状态
    updateSelectAllStatus()
  } catch (error) {
    console.error('加载购物车失败:', error)
    ElMessage.error('加载购物车失败')
  }
}

const handleSelectAll = async () => {
  try {
    // 使用购物车store批量更新选中状态
    await cartStore.toggleAllSelected(selectAll.value)
    // 更新本地数据
    cartItems.value.forEach(item => {
      item.selected = selectAll.value
    })
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleItemSelect = async (item) => {
  try {
    // 使用购物车store更新选中状态
    await cartStore.toggleItemSelected(item.productId)
    updateSelectAllStatus()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const updateSelectAllStatus = () => {
  const selectedCount = selectedItems.value.length
  const totalCount = cartItems.value.length
  
  if (selectedCount === 0) {
    selectAll.value = false
  } else if (selectedCount === totalCount) {
    selectAll.value = true
  }
}

const handleQuantityChange = async (item) => {
  try {
    // 使用购物车store更新数量
    await cartStore.updateQuantity(item.productId, item.quantity)
    // 重新加载购物车数据以保持同步
    await loadCartItems()
  } catch (error) {
    ElMessage.error('更新数量失败')
  }
}

const handleRemoveItem = async (item) => {
  try {
    await ElMessageBox.confirm('确定要删除这件商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 使用购物车store删除商品
    await cartStore.removeItem(item.productId)
    // 重新加载购物车数据
    await loadCartItems()
    updateSelectAllStatus()
  } catch (error) {
    // 用户取消删除或删除失败
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleClearCart = async () => {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 使用购物车store清空购物车
    await cartStore.clearCart()
    cartItems.value = []
    selectAll.value = false
  } catch (error) {
    // 用户取消清空或清空失败
    if (error !== 'cancel') {
      ElMessage.error('清空失败')
    }
  }
}

const handleCheckout = () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }
  
  // 跳转到结算页面
  router.push('/checkout')
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 返回首页
const goHome = () => {
  router.push('/')
}

// 生命周期
onMounted(() => {
  loadCartItems()
})
</script>

<style scoped>
.cart-container {
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

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.item-count {
  color: #909399;
  font-size: 14px;
}

.cart-header {
  display: grid;
  grid-template-columns: 50px 1fr 120px 150px 120px 80px;
  align-items: center;
  padding: 15px 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 10px;
  font-weight: 500;
  color: #606266;
}

.cart-item {
  display: grid;
  grid-template-columns: 50px 1fr 120px 150px 120px 80px;
  align-items: center;
  padding: 20px;
  background-color: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 10px;
  transition: box-shadow 0.3s;
}

.cart-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.item-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.product-details {
  flex: 1;
}

.product-name {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #303133;
  font-weight: 500;
}

.product-specs {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.item-price {
  font-size: 16px;
  color: #e6a23c;
  font-weight: 500;
}

.item-total {
  font-size: 16px;
  color: #f56c6c;
  font-weight: 600;
}

.remove-btn {
  color: #f56c6c;
}

.remove-btn:hover {
  color: #f78989;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background-color: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-top: 20px;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.selected-count {
  color: #606266;
}

.total-price {
  font-size: 18px;
  color: #f56c6c;
  font-weight: 600;
}

.empty-cart {
  text-align: center;
  padding: 60px 20px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .cart-container {
    padding: 15px;
  }
  
  .cart-header {
    padding: 0 15px;
  }
  
  .cart-item {
    padding: 15px;
  }
}

@media (max-width: 992px) {
  .cart-header {
    display: none;
  }

  .cart-item {
    flex-direction: column;
    padding: 20px 15px;
    gap: 15px;
    border-radius: 8px;
    margin-bottom: 15px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  }

  .cart-item-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }

  .item-info {
    width: 100%;
    margin-bottom: 15px;
  }

  .product-image {
    width: 80px;
    height: 80px;
  }

  .product-name {
    font-size: 16px;
    margin-bottom: 5px;
  }

  .product-specs {
    font-size: 12px;
    color: #999;
  }

  .mobile-item-controls {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    padding: 10px 0;
    border-top: 1px solid #f0f0f0;
  }

  .mobile-price-quantity {
    display: flex;
    align-items: center;
    gap: 20px;
  }

  .mobile-price {
    font-size: 18px;
    font-weight: bold;
    color: #e3101e;
  }

  .mobile-total {
    font-size: 16px;
    font-weight: bold;
    color: #333;
  }

  .item-price,
  .item-quantity,
  .item-total,
  .item-actions {
    width: auto;
    text-align: left;
  }

  .cart-footer {
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

  .footer-right {
    flex-direction: row;
    align-items: center;
    gap: 15px;
  }

  .cart-content {
    padding-bottom: 80px; /* 为固定底栏留出空间 */
  }
}

@media (max-width: 768px) {
  .cart-container {
    padding: 10px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
    margin-bottom: 15px;
  }

  .page-header h2 {
    font-size: 20px;
    margin: 0;
  }

  .item-count {
    font-size: 14px;
  }

  .cart-item {
    padding: 15px 10px;
  }

  .product-image {
    width: 70px;
    height: 70px;
  }

  .product-name {
    font-size: 14px;
    line-height: 1.4;
  }

  .mobile-price {
    font-size: 16px;
  }

  .mobile-total {
    font-size: 14px;
  }

  .cart-footer {
    padding: 10px 15px;
  }

  .footer-right {
    flex-direction: column;
    gap: 10px;
    align-items: flex-end;
  }

  .selected-count,
  .total-price {
    font-size: 14px;
  }

  .total-price {
    font-size: 16px;
    font-weight: bold;
  }
}

@media (max-width: 480px) {
  .cart-container {
    padding: 5px;
  }

  .cart-item {
    padding: 10px;
  }

  .product-image {
    width: 60px;
    height: 60px;
  }

  .product-name {
    font-size: 13px;
  }

  .mobile-price-quantity {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .footer-right {
    width: 100%;
  }

  .footer-right .el-button {
    width: 100%;
    margin-top: 10px;
  }
}
</style>