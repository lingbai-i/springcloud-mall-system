<template>
  <div class="orders-page">
    <div class="page-header">
      <h1>我的订单</h1>
    </div>

    <!-- 订单状态筛选 -->
    <div class="order-tabs">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部订单" name="all" />
        <el-tab-pane label="待付款" name="pending" />
        <el-tab-pane label="待发货" name="paid" />
        <el-tab-pane label="待收货" name="shipped" />
        <el-tab-pane label="已完成" name="completed" />
        <el-tab-pane label="已取消" name="cancelled" />
      </el-tabs>
    </div>

    <!-- 订单列表 -->
    <div v-loading="loading" class="orders-list">
      <div
        v-for="order in orders"
        :key="order.id"
        class="order-card"
      >
        <!-- 订单头部 -->
        <div class="order-header">
          <div class="order-info">
            <span class="order-number">订单号：{{ order.orderNumber }}</span>
            <span class="order-date">{{ order.createTime }}</span>
          </div>
          <div class="order-status">
            <el-tag :type="getStatusType(order.status)">
              {{ getStatusText(order.status) }}
            </el-tag>
          </div>
        </div>

        <!-- 订单商品 -->
        <div class="order-items">
          <div
            v-for="item in order.items"
            :key="item.id"
            class="order-item"
          >
            <div class="item-image">
              <img :src="item.image" :alt="item.name" />
            </div>
            <div class="item-info">
              <h4 class="item-name">{{ item.name }}</h4>
              <p class="item-spec">{{ item.specification }}</p>
              <div class="item-price-qty">
                <span class="item-price">¥{{ item.price }}</span>
                <span class="item-qty">x{{ item.quantity }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 订单金额 -->
        <div class="order-amount">
          <div class="amount-detail">
            <span>商品金额：¥{{ order.itemsAmount }}</span>
            <span>运费：¥{{ order.shippingFee }}</span>
            <span class="total-amount">实付款：¥{{ order.totalAmount }}</span>
          </div>
        </div>

        <!-- 订单操作 -->
        <div class="order-actions">
          <el-button
            v-if="order.status === 'pending'"
            type="primary"
            @click="payOrder(order)"
          >
            立即付款
          </el-button>
          <el-button
            v-if="order.status === 'pending'"
            @click="cancelOrder(order.id)"
          >
            取消订单
          </el-button>
          <el-button
            v-if="order.status === 'shipped'"
            type="primary"
            @click="confirmReceive(order.id)"
          >
            确认收货
          </el-button>
          <el-button
            v-if="order.status === 'completed'"
            @click="evaluateOrder(order.id)"
          >
            评价
          </el-button>
          <el-button @click="viewOrderDetail(order.id)">
            查看详情
          </el-button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && orders.length === 0" description="暂无订单" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const activeTab = ref('all')
const orders = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

/**
 * 映射后端订单状态到前端状态
 */
const mapOrderStatus = (status: string) => {
  const statusMap = {
    'PENDING': 'pending',
    'PAID': 'paid',
    'SHIPPED': 'shipped',
    'COMPLETED': 'completed',
    'CANCELLED': 'cancelled',
    'REFUNDING': 'refunding',
    'REFUNDED': 'refunded'
  }
  return statusMap[status] || status?.toLowerCase() || 'pending'
}

/**
 * 获取订单状态类型
 */
const getStatusType = (status: string) => {
  const statusMap = {
    pending: 'warning',
    paid: 'info',
    shipped: 'primary',
    completed: 'success',
    cancelled: 'danger'
  }
  return statusMap[status] || 'info'
}

/**
 * 获取订单状态文本
 */
const getStatusText = (status: string) => {
  const statusMap = {
    pending: '待付款',
    paid: '待发货',
    shipped: '待收货',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || '未知状态'
}

/**
 * 获取订单列表
 */
const fetchOrders = async () => {
  loading.value = true
  try {
    // 获取用户ID（从用户store或localStorage）
    const userId = userStore.userId || localStorage.getItem('userId')
    if (!userId) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }
    console.log('获取订单列表，用户ID:', userId)
    
    // 调用真实API，添加必需的userId参数
    const response = await fetch(`/api/order-service/orders?userId=${userId}&page=${currentPage.value - 1}&size=${pageSize.value}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    if (!response.ok) {
      throw new Error('获取订单失败')
    }
    
    const result = await response.json()
    
    // 后端返回的数据结构：{ success: true, data: { content: [], totalElements: 0, ... } }
    if (result.success) {
      // 转换后端数据格式为前端期望的格式
      const rawOrders = result.data.content || []
      orders.value = rawOrders.map(order => ({
        id: order.id,
        orderNumber: order.orderNo,
        createTime: order.createTime,
        status: mapOrderStatus(order.status),
        itemsAmount: order.productAmount,
        shippingFee: order.shippingFee || order.freightAmount || 0,
        totalAmount: order.totalAmount,
        items: (order.orderItems || []).map(item => ({
          id: item.id,
          name: item.productName,
          image: item.productImage,
          specification: item.productSpec,
          price: item.productPrice,
          quantity: item.quantity
        }))
      }))
      total.value = result.data.totalElements || 0
    } else {
      throw new Error(result.message || '获取订单失败')
    }
  } catch (error) {
    console.error('Fetch orders error:', error)
    ElMessage.error('获取订单失败')
    orders.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/**
 * 根据状态筛选订单
 */
const filterOrdersByStatus = () => {
  if (activeTab.value === 'all') {
    fetchOrders()
  } else {
    // 可以在API中添加状态筛选参数
    fetchOrders()
  }
}

/**
 * 标签页切换
 */
const handleTabChange = (tab: string) => {
  activeTab.value = tab
  currentPage.value = 1
  filterOrdersByStatus()
}

/**
 * 页码改变
 */
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchOrders()
}

/**
 * 支付订单
 */
const payOrder = (order: any) => {
  ElMessage.info('跳转到支付页面...')
  // 这里应该跳转到支付页面
  router.push(`/payment/${order.id}`)
}

/**
 * 取消订单
 */
const cancelOrder = async (orderId: number) => {
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 模拟取消订单
    const order = orders.value.find(o => o.id === orderId)
    if (order) {
      order.status = 'cancelled'
      ElMessage.success('订单取消成功')
    }
  } catch (error) {
    // 用户取消操作
  }
}

/**
 * 确认收货
 */
const confirmReceive = async (orderId: number) => {
  try {
    await ElMessageBox.confirm('确定已收到商品吗？', '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    // 模拟确认收货
    const order = orders.value.find(o => o.id === orderId)
    if (order) {
      order.status = 'completed'
      ElMessage.success('收货确认成功')
    }
  } catch (error) {
    // 用户取消操作
  }
}

/**
 * 评价订单
 */
const evaluateOrder = (orderId: number) => {
  ElMessage.info('跳转到评价页面...')
  // 这里应该跳转到评价页面
  router.push(`/evaluate/${orderId}`)
}

/**
 * 查看订单详情
 */
const viewOrderDetail = (orderId: number) => {
  router.push(`/order/${orderId}`)
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.orders-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.order-tabs {
  margin-bottom: 20px;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: white;
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #ebeef5;
}

.order-info {
  display: flex;
  gap: 20px;
  align-items: center;
}

.order-number {
  font-weight: 500;
  color: #303133;
}

.order-date {
  color: #909399;
  font-size: 14px;
}

.order-items {
  padding: 20px;
}

.order-item {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}

.order-item:last-child {
  margin-bottom: 0;
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
}

.item-name {
  margin: 0 0 5px 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.item-spec {
  margin: 0 0 10px 0;
  color: #909399;
  font-size: 14px;
}

.item-price-qty {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-price {
  color: #e74c3c;
  font-weight: 500;
}

.item-qty {
  color: #909399;
}

.order-amount {
  padding: 15px 20px;
  background: #f8f9fa;
  border-top: 1px solid #ebeef5;
}

.amount-detail {
  display: flex;
  justify-content: flex-end;
  gap: 20px;
  font-size: 14px;
}

.total-amount {
  color: #e74c3c;
  font-weight: 500;
  font-size: 16px;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 15px 20px;
  border-top: 1px solid #ebeef5;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .orders-page {
    padding: 15px;
  }
  
  .order-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
  
  .order-info {
    flex-direction: column;
    gap: 5px;
    align-items: flex-start;
  }
  
  .amount-detail {
    flex-direction: column;
    gap: 5px;
    align-items: flex-end;
  }
  
  .order-actions {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>