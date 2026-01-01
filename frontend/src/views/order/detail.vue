<template>
  <div class="order-detail-container">
    <div class="order-detail" v-if="order" v-loading="loading">
      <!-- 订单状态头部 -->
      <div class="order-header">
        <div class="order-status-section">
          <div class="status-icon">
            <el-icon :size="48" :color="getStatusColor(order.status)">
              <component :is="getStatusIcon(order.status)" />
            </el-icon>
          </div>
          <div class="status-info">
            <h2 class="status-title">{{ getStatusText(order.status) }}</h2>
            <p class="status-desc">{{ getStatusDescription(order.status) }}</p>
          </div>
        </div>
        <div class="order-basic-info">
          <div class="info-item">
            <span class="label">订单号：</span>
            <span class="value">{{ order.orderNo }}</span>
            <el-button size="small" text @click="copyOrderNo">复制</el-button>
          </div>
          <div class="info-item">
            <span class="label">下单时间：</span>
            <span class="value">{{ formatDate(order.createTime) }}</span>
          </div>
          <div class="info-item">
            <span class="label">订单金额：</span>
            <span class="value amount">{{ order.totalAmount }} 元</span>
          </div>
        </div>
      </div>

      <!-- 订单进度 -->
      <div class="order-progress">
        <h3 class="section-title">订单进度</h3>
        <el-timeline>
          <el-timeline-item
            v-for="(step, index) in orderSteps"
            :key="index"
            :timestamp="step.time"
            :type="step.type"
            :icon="step.icon">
            <div class="timeline-content">
              <h4>{{ step.title }}</h4>
              <p>{{ step.description }}</p>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>

      <!-- 物流信息 -->
      <div class="logistics-info" v-if="hasLogistics">
        <h3 class="section-title">
          <el-icon><Van /></el-icon>
          物流信息
        </h3>
        <div class="logistics-card">
          <div class="logistics-header">
            <span class="courier-company">{{ order.courierCompany }}</span>
            <span class="tracking-no">运单号：{{ order.trackingNo }}</span>
          </div>
          <el-timeline class="logistics-timeline" v-if="logistics.length > 0">
            <el-timeline-item
              v-for="(item, index) in logistics"
              :key="index"
              :timestamp="item.time"
              size="small">
              {{ item.description }}
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>

      <!-- 商品信息 -->
      <div class="order-products">
        <h3 class="section-title">
          <el-icon><Box /></el-icon>
          商品信息
        </h3>
        <div class="products-list">
          <div v-for="item in order.items" :key="item.id" class="product-item">
            <div class="product-image">
              <img :src="item.image" :alt="item.name" />
            </div>
            <div class="product-info">
              <h4 class="product-name">{{ item.name }}</h4>
              <p class="product-spec" v-if="item.specification">{{ item.specification }}</p>
              <div class="product-price">
                <span class="price">{{ item.price }} 元</span>
                <span class="quantity">x{{ item.quantity }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 收货信息 -->
      <div class="delivery-info">
        <h3 class="section-title">
          <el-icon><Location /></el-icon>
          收货信息
        </h3>
        <div class="delivery-card">
          <div class="name-phone">
            <span class="name">{{ order.receiverName }}</span>
            <span class="phone">{{ order.receiverPhone }}</span>
          </div>
          <div class="address">{{ order.receiverAddress }}</div>
        </div>
      </div>

      <!-- 费用明细 -->
      <div class="order-summary">
        <h3 class="section-title">
          <el-icon><Money /></el-icon>
          费用明细
        </h3>
        <div class="summary-card">
          <div class="summary-item">
            <span class="label">商品总价：</span>
            <span class="value">{{ order.productAmount }} 元</span>
          </div>
          <div class="summary-item" v-if="order.discountAmount > 0">
            <span class="label">优惠金额：</span>
            <span class="value discount">-{{ order.discountAmount }} 元</span>
          </div>
          <div class="summary-item">
            <span class="label">运费：</span>
            <span class="value">{{ order.shippingFee }} 元</span>
          </div>
          <div class="summary-item total">
            <span class="label">实付金额：</span>
            <span class="value">{{ order.totalAmount }} 元</span>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="order-actions">
        <el-button v-if="canCancel(order.status)" type="danger" @click="cancelOrder" :loading="cancelling">
          取消订单
        </el-button>
        <el-button v-if="canPay(order.status)" type="primary" @click="payOrder">
          立即支付
        </el-button>
        <el-button v-if="canConfirm(order.status)" type="primary" @click="confirmReceive" :loading="confirming">
          确认收货
        </el-button>
        <el-button v-if="canRefund(order.status)" @click="applyRefund">
          <el-icon><RefreshLeft /></el-icon>
          申请退款
        </el-button>
        <el-button @click="goBack">返回</el-button>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!loading" class="empty-state">
      <el-empty description="订单不存在或已被删除">
        <el-button type="primary" @click="goBack">返回订单列表</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Check, Van, Box, Location, Money, CircleCheck, Warning, Close, RefreshLeft } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const order = ref(null)
const logistics = ref([])
const cancelling = ref(false)
const confirming = ref(false)

// 订单状态映射
// 数字状态来自 merchant-service 的 MerchantOrderServiceImpl.convertStatusToInt 方法
// 映射关系：PENDING→1, PAID→2, SHIPPED→3, COMPLETED→5, CANCELLED→6, REFUNDING→7, REFUNDED→8
const statusMap = {
  0: { text: '待付款', desc: '请在10分钟内完成支付', color: '#E6A23C', icon: Clock },
  1: { text: '待付款', desc: '请在10分钟内完成支付', color: '#E6A23C', icon: Clock },
  2: { text: '待发货', desc: '商家正在准备您的商品', color: '#409EFF', icon: Box },
  3: { text: '已发货', desc: '商品正在配送中', color: '#67C23A', icon: Van },
  4: { text: '待评价', desc: '订单已完成', color: '#67C23A', icon: Check },
  5: { text: '已完成', desc: '感谢您的购买', color: '#67C23A', icon: CircleCheck },
  6: { text: '已取消', desc: '订单已取消', color: '#909399', icon: Close },
  7: { text: '待退款', desc: '退款申请处理中', color: '#E6A23C', icon: Warning },
  8: { text: '已退款', desc: '退款已完成', color: '#909399', icon: Check },
  'PENDING': { text: '待付款', desc: '请在10分钟内完成支付', color: '#E6A23C', icon: Clock },
  'PAID': { text: '待发货', desc: '商家正在准备您的商品', color: '#409EFF', icon: Box },
  'SHIPPED': { text: '已发货', desc: '商品正在配送中', color: '#67C23A', icon: Van },
  'COMPLETED': { text: '已完成', desc: '感谢您的购买', color: '#67C23A', icon: CircleCheck },
  'CANCELLED': { text: '已取消', desc: '订单已取消', color: '#909399', icon: Close },
  'REFUNDING': { text: '待退款', desc: '退款申请处理中', color: '#E6A23C', icon: Warning },
  'REFUNDED': { text: '已退款', desc: '退款已完成', color: '#909399', icon: Check }
}

const hasLogistics = computed(() => {
  const s = order.value?.status
  return (s === 'SHIPPED' || s === 3) && order.value?.trackingNo
})

const orderSteps = computed(() => {
  if (!order.value) return []
  const steps = [
    { title: '订单创建', description: '订单已创建成功', time: formatDate(order.value.createTime), type: 'primary', icon: Check }
  ]
  if (order.value.payTime) {
    steps.push({ title: '支付成功', description: '订单支付成功', time: formatDate(order.value.payTime), type: 'success', icon: Check })
  }
  if (order.value.shipTime) {
    steps.push({ title: '商品发货', description: '商品已发货', time: formatDate(order.value.shipTime), type: 'success', icon: Van })
  }
  if (order.value.receiveTime) {
    steps.push({ title: '确认收货', description: '已确认收货', time: formatDate(order.value.receiveTime), type: 'success', icon: CircleCheck })
  }
  if (order.value.status === 6 || order.value.status === 'CANCELLED') {
    steps.push({ title: '订单取消', description: '订单已取消', time: formatDate(order.value.cancelTime), type: 'danger', icon: Close })
  }
  return steps
})

const loadOrderDetail = async () => {
  loading.value = true
  try {
    const orderId = route.params.id
    const userId = localStorage.getItem('userId')
    console.log('加载订单详情, orderId:', orderId, 'userId:', userId)
    
    const response = await fetch(`/api/order-service/orders/${orderId}?userId=${userId}`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    })
    const result = await response.json()
    console.log('订单详情API响应:', result)
    
    if (result.success && result.data) {
      const data = result.data
      order.value = {
        id: data.id,
        orderNo: data.orderNo,
        status: data.status,
        totalAmount: data.totalAmount || 0,
        productAmount: data.productAmount || data.totalAmount || 0,
        discountAmount: data.discountAmount || 0,
        shippingFee: data.shippingFee || 0,
        createTime: data.createTime,
        payTime: data.payTime,
        shipTime: data.shipTime,
        receiveTime: data.confirmTime,
        cancelTime: data.cancelTime,
        receiverName: data.receiverName,
        receiverPhone: data.receiverPhone,
        receiverAddress: data.receiverAddress,
        courierCompany: data.logisticsCompany,
        trackingNo: data.logisticsNo || data.trackingNo,
        items: (data.orderItems || []).map(item => ({
          id: item.id,
          productId: item.productId,
          name: item.productName,
          specification: item.productSpec,
          image: item.productImage ? item.productImage.split(',')[0] : 'https://via.placeholder.com/120',
          price: item.productPrice,
          quantity: item.quantity
        }))
      }
    }
  } catch (error) {
    ElMessage.error('加载订单详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const getStatusText = (status) => statusMap[status]?.text || '未知'
const getStatusDescription = (status) => statusMap[status]?.desc || ''
const getStatusColor = (status) => statusMap[status]?.color || '#909399'
const getStatusIcon = (status) => statusMap[status]?.icon || Clock
const formatDate = (date) => date ? dayjs(date).format('YYYY-MM-DD HH:mm:ss') : '-'

const canCancel = (status) => status === 1 || status === 2 || status === 'PENDING' || status === 'PAID'
const canPay = (status) => status === 1 || status === 'PENDING'
const canConfirm = (status) => status === 3 || status === 'SHIPPED'
const canRefund = (status) => ['PAID', 'SHIPPED', 'COMPLETED', 2, 3, 4].includes(status)

const copyOrderNo = () => {
  navigator.clipboard.writeText(order.value.orderNo)
  ElMessage.success('订单号已复制')
}

const cancelOrder = async () => {
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？', '取消订单', { type: 'warning' })
    cancelling.value = true
    const userId = localStorage.getItem('userId')
    const response = await fetch(`/api/order-service/orders/${order.value.id}/cancel?userId=${userId}&reason=用户主动取消`, {
      method: 'PUT',
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    })
    const result = await response.json()
    if (result.success || result.code === 200) {
      order.value.status = 'CANCELLED'
      ElMessage.success('订单已取消')
    } else {
      throw new Error(result.message)
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(error.message || '取消失败')
  } finally {
    cancelling.value = false
  }
}

const payOrder = () => router.push(`/payment/${order.value.id}`)

const confirmReceive = async () => {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', { type: 'warning' })
    confirming.value = true
    const userId = localStorage.getItem('userId')
    const response = await fetch(`/api/order-service/orders/${order.value.id}/confirm?userId=${userId}`, {
      method: 'PUT',
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    })
    const result = await response.json()
    if (result.success || result.code === 200) {
      order.value.status = 'COMPLETED'
      ElMessage.success('确认收货成功')
    } else {
      throw new Error(result.message)
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(error.message || '确认失败')
  } finally {
    confirming.value = false
  }
}

const applyRefund = () => ElMessage.info('退款功能开发中')
const goBack = () => router.back()

onMounted(() => loadOrderDetail())
</script>


<style scoped>
.order-detail-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.order-detail {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.order-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 24px;
}

.order-status-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.status-icon {
  width: 64px;
  height: 64px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.status-desc {
  font-size: 14px;
  margin: 0;
  opacity: 0.9;
}

.order-basic-info {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
}

.info-item .amount {
  font-size: 18px;
  font-weight: 600;
  color: #FFD700;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.order-progress,
.logistics-info,
.order-products,
.delivery-info,
.order-summary {
  padding: 24px;
  border-bottom: 1px solid #f0f2f5;
}

.logistics-card,
.delivery-card,
.summary-card {
  background: #f8fafc;
  border-radius: 8px;
  padding: 16px;
}

.logistics-header {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  font-size: 14px;
}

.courier-company {
  font-weight: 600;
  color: #303133;
}

.tracking-no {
  color: #606266;
}

.products-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.product-item {
  display: flex;
  gap: 16px;
  padding: 12px;
  background: #fafbfc;
  border-radius: 8px;
}

.product-image {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
  border-radius: 6px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 8px 0;
}

.product-spec {
  font-size: 12px;
  color: #909399;
  margin: 0 0 8px 0;
}

.product-price {
  display: flex;
  gap: 12px;
  font-size: 14px;
}

.product-price .price {
  color: #e74c3c;
  font-weight: 500;
}

.product-price .quantity {
  color: #909399;
}

.name-phone {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
}

.name-phone .name {
  font-weight: 600;
  color: #303133;
}

.name-phone .phone {
  color: #606266;
}

.address {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
}

.summary-item .label {
  color: #606266;
}

.summary-item .value {
  color: #303133;
}

.summary-item .discount {
  color: #e74c3c;
}

.summary-item.total {
  font-size: 16px;
  font-weight: 600;
  border-top: 1px solid #ebeef5;
  margin-top: 8px;
  padding-top: 12px;
}

.order-actions {
  padding: 24px;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  background: #fff;
  border-radius: 12px;
}

.timeline-content h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  color: #303133;
}

.timeline-content p {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

@media (max-width: 768px) {
  .order-detail-container {
    padding: 12px;
  }
  
  .order-basic-info {
    flex-direction: column;
    gap: 12px;
  }
  
  .order-actions {
    flex-direction: column;
  }
  
  .order-actions .el-button {
    width: 100%;
  }
}
</style>
