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
            <span class="value amount">¥{{ order.totalAmount }}</span>
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
      <div class="logistics-info" v-if="order.status >= 3 && logistics.length > 0">
        <h3 class="section-title">
          <el-icon><Truck /></el-icon>
          物流信息
        </h3>
        <div class="logistics-card">
          <div class="logistics-header">
            <div class="courier-info">
              <span class="courier-company">{{ order.courierCompany }}</span>
              <span class="tracking-no">运单号：{{ order.trackingNo }}</span>
              <el-button size="small" text @click="copyTrackingNo">复制</el-button>
            </div>
            <div class="logistics-status">
              <span class="status">{{ getLogisticsStatus(order.logisticsStatus) }}</span>
            </div>
          </div>
          <el-timeline class="logistics-timeline">
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
          <div
            v-for="item in order.items"
            :key="item.id"
            class="product-item"
            @click="goToProduct(item.productId)">
            <div class="product-image">
              <img :src="item.image" :alt="item.name" />
            </div>
            <div class="product-info">
              <h4 class="product-name">{{ item.name }}</h4>
              <p class="product-spec" v-if="item.specification">{{ item.specification }}</p>
              <div class="product-price">
                <span class="price">¥{{ item.price }}</span>
                <span class="quantity">x{{ item.quantity }}</span>
              </div>
            </div>
            <div class="product-actions">
              <el-button
                v-if="canReview(item)"
                type="primary"
                size="small"
                @click.stop="openReviewDialog(item)">
                评价
              </el-button>
              <el-button
                size="small"
                @click.stop="buyAgain(item)">
                再次购买
              </el-button>
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
          <div class="recipient-info">
            <div class="name-phone">
              <span class="name">{{ order.receiverName }}</span>
              <span class="phone">{{ order.receiverPhone }}</span>
            </div>
            <div class="address">{{ order.receiverAddress }}</div>
          </div>
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
            <span class="value">¥{{ order.productAmount }}</span>
          </div>
          <div class="summary-item" v-if="order.discountAmount > 0">
            <span class="label">优惠金额：</span>
            <span class="value discount">-¥{{ order.discountAmount }}</span>
          </div>
          <div class="summary-item">
            <span class="label">运费：</span>
            <span class="value">¥{{ order.shippingFee }}</span>
          </div>
          <div class="summary-item total">
            <span class="label">实付金额：</span>
            <span class="value">¥{{ order.totalAmount }}</span>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="order-actions">
        <el-button
          v-if="canCancel(order.status)"
          type="danger"
          @click="cancelOrder"
          :loading="cancelling">
          取消订单
        </el-button>
        <el-button
          v-if="canPay(order.status)"
          type="primary"
          @click="payOrder">
          立即支付
        </el-button>
        <el-button
          v-if="canConfirm(order.status)"
          type="primary"
          @click="confirmReceive"
          :loading="confirming">
          确认收货
        </el-button>
        <el-button
          v-if="canRefund(order.status)"
          @click="applyRefund">
          <el-icon><RefreshLeft /></el-icon>
          申请退款
        </el-button>
        <el-button @click="contactService">
          <el-icon><Service /></el-icon>
          联系客服
        </el-button>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!loading" class="empty-state">
      <el-empty description="订单不存在或已被删除">
        <el-button type="primary" @click="$router.push('/user/orders')">
          返回订单列表
        </el-button>
      </el-empty>
    </div>

    <!-- 评价对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      title="商品评价"
      width="600px"
      @close="resetReviewForm">
      <div class="review-form" v-if="currentReviewItem">
        <div class="review-product">
          <img :src="currentReviewItem.image" :alt="currentReviewItem.name" />
          <div class="product-info">
            <h4>{{ currentReviewItem.name }}</h4>
            <p v-if="currentReviewItem.specification">{{ currentReviewItem.specification }}</p>
          </div>
        </div>
        <el-form :model="reviewForm" label-width="80px">
          <el-form-item label="评分">
            <el-rate v-model="reviewForm.rating" size="large" show-text />
          </el-form-item>
          <el-form-item label="评价内容">
            <el-input
              v-model="reviewForm.content"
              type="textarea"
              :rows="4"
              placeholder="请分享您的使用感受..."
              maxlength="500"
              show-word-limit />
          </el-form-item>
          <el-form-item label="上传图片">
            <el-upload
              v-model:file-list="reviewForm.images"
              action="#"
              list-type="picture-card"
              :auto-upload="false"
              :limit="5"
              accept="image/*">
              <el-icon><Plus /></el-icon>
            </el-upload>
          </el-form-item>
          <el-form-item label="匿名评价">
            <el-switch v-model="reviewForm.anonymous" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="submittingReview">
          提交评价
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Clock,
  Check,
  Truck,
  Box,
  Location,
  Money,
  Plus,
  CircleCheck,
  Warning,
  Close,
  DocumentCopy,
  List,
  Star,
  ShoppingCart,
  LocationInformation,
  ChatDotRound,
  EditPen,
  CreditCard,
  RefreshLeft,
  Service
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'

// 路由相关
const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const order = ref(null)
const logistics = ref([])
const cancelling = ref(false)
const confirming = ref(false)
const reviewDialogVisible = ref(false)
const currentReviewItem = ref(null)
const submittingReview = ref(false)

// 评价表单
const reviewForm = reactive({
  rating: 5,
  content: '',
  images: [],
  anonymous: false
})

// 订单状态映射
const statusMap = {
  1: { text: '待付款', desc: '请在30分钟内完成支付', color: '#E6A23C', icon: Clock },
  2: { text: '待发货', desc: '商家正在准备您的商品', color: '#409EFF', icon: Box },
  3: { text: '待收货', desc: '商品正在配送中，请耐心等待', color: '#67C23A', icon: Truck },
  4: { text: '待评价', desc: '订单已完成，快来评价一下商品吧', color: '#67C23A', icon: Check },
  5: { text: '已完成', desc: '感谢您的购买', color: '#67C23A', icon: CircleCheck },
  6: { text: '已取消', desc: '订单已取消', color: '#909399', icon: Close },
  7: { text: '退款中', desc: '退款申请处理中', color: '#E6A23C', icon: Warning },
  8: { text: '已退款', desc: '退款已完成', color: '#909399', icon: Check }
}

// 物流状态映射
const logisticsStatusMap = {
  1: '运输中',
  2: '派送中', 
  3: '已签收',
  4: '异常'
}

// 物流状态类型映射
const logisticsStatusTypeMap = {
  1: 'primary',
  2: 'warning',
  3: 'success',
  4: 'danger'
}

// 计算属性
const orderSteps = computed(() => {
  if (!order.value) return []
  
  const steps = [
    {
      title: '订单创建',
      description: '您的订单已创建成功',
      time: formatDate(order.value.createTime),
      type: 'primary',
      icon: Check,
      size: 'large'
    }
  ]
  
  if (order.value.payTime) {
    steps.push({
      title: '支付成功',
      description: '订单支付成功，等待商家发货',
      time: formatDate(order.value.payTime),
      type: 'success',
      icon: Check,
      extra: `支付方式：${order.value.paymentMethod || '在线支付'}`
    })
  }
  
  if (order.value.shipTime) {
    steps.push({
      title: '商品发货',
      description: `商品已发货，快递公司：${order.value.courierCompany}`,
      time: formatDate(order.value.shipTime),
      type: 'success',
      icon: Truck,
      extra: `快递单号：${order.value.trackingNo}`
    })
  }
  
  if (order.value.receiveTime) {
    steps.push({
      title: '确认收货',
      description: '您已确认收货，订单完成',
      time: formatDate(order.value.receiveTime),
      type: 'success',
      icon: CircleCheck,
      size: 'large'
    })
  }
  
  if (order.value.status === 6) {
    steps.push({
      title: '订单取消',
      description: '订单已取消',
      time: formatDate(order.value.cancelTime || order.value.createTime),
      type: 'danger',
      icon: Close
    })
  }
  
  return steps
})

// 方法
const loadOrderDetail = async () => {
  loading.value = true
  try {
    const orderId = route.params.id
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 800))
    
    // 模拟订单数据
    order.value = {
      id: orderId,
      orderNo: `ORD${Date.now().toString().slice(-8)}`,
      status: 3,
      logisticsStatus: 1,
      totalAmount: 3299.00,
      productAmount: 3299.00,
      discountAmount: 200.00,
      couponAmount: 100.00,
      shippingFee: 0,
      paymentMethod: '微信支付',
      createTime: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000),
      payTime: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000 + 10 * 60 * 1000),
      shipTime: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000),
      receiveTime: null,
      estimatedDeliveryTime: new Date(Date.now() + 4 * 60 * 60 * 1000),
      receiverName: '张三',
      receiverPhone: '13812345678',
      receiverAddress: '北京市朝阳区建国门外大街1号国贸大厦A座1001室',
      addressTag: '公司',
      deliveryNote: '工作日9:00-18:00配送，请提前电话联系',
      courierCompany: '顺丰速运',
      trackingNo: 'SF1234567890123',
      remark: '请仔细包装，商品易碎',
      items: [
        {
          id: 1,
          productId: 1,
          name: 'iPhone 15 Pro Max 钛金属',
          specification: '深空黑色 256GB 中国大陆版',
          image: 'https://picsum.photos/120/120?random=1',
          price: 2999.00,
          quantity: 1,
          reviewed: false,
          badge: '现货',
          badgeType: 'success',
          tags: ['官方正品', '全国联保']
        },
        {
          id: 2,
          productId: 2,
          name: 'AirPods Pro 第三代',
          specification: '白色 主动降噪',
          image: 'https://picsum.photos/120/120?random=2',
          price: 300.00,
          quantity: 1,
          reviewed: true,
          badge: '赠品',
          badgeType: 'warning',
          tags: ['原装配件']
        }
      ]
    }
    
    // 模拟物流数据
    if (order.value.status >= 3) {
      logistics.value = [
        {
          time: formatDate(new Date(Date.now() - 30 * 60 * 1000)),
          description: '快件正在派送中，派送员：李师傅',
          location: '北京朝阳区建国门营业点'
        },
        {
          time: formatDate(new Date(Date.now() - 2 * 60 * 60 * 1000)),
          description: '快件已到达目的地分拣中心',
          location: '北京朝阳分拣中心'
        },
        {
          time: formatDate(new Date(Date.now() - 6 * 60 * 60 * 1000)),
          description: '快件正在运输途中',
          location: '北京转运中心'
        },
        {
          time: formatDate(new Date(Date.now() - 12 * 60 * 60 * 1000)),
          description: '快件已从上海分拣中心发出',
          location: '上海分拣中心'
        },
        {
          time: formatDate(order.value.shipTime),
          description: '商家已发货，等待快递公司揽收',
          location: '上海市浦东新区'
        }
      ]
    }
  } catch (error) {
    ElMessage.error('加载订单详情失败')
    console.error('Load order detail error:', error)
  } finally {
    loading.value = false
  }
}

const getStatusText = (status) => statusMap[status]?.text || '未知状态'
const getStatusDescription = (status) => statusMap[status]?.desc || ''
const getStatusColor = (status) => statusMap[status]?.color || '#909399'
const getStatusIcon = (status) => statusMap[status]?.icon || Clock
const getLogisticsStatus = (status) => logisticsStatusMap[status] || '未知'
const getLogisticsStatusType = (status) => logisticsStatusTypeMap[status] || 'info'

const formatDate = (date, format = 'YYYY-MM-DD HH:mm:ss') => {
  return dayjs(date).format(format)
}

const formatPhone = (phone) => {
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const getCourierLogo = (company) => {
  // 模拟快递公司logo
  const logos = {
    '顺丰速运': 'https://picsum.photos/40/40?random=sf',
    '圆通速递': 'https://picsum.photos/40/40?random=yt',
    '中通快递': 'https://picsum.photos/40/40?random=zt',
    '韵达速递': 'https://picsum.photos/40/40?random=yd'
  }
  return logos[company] || 'https://picsum.photos/40/40?random=default'
}

const copyOrderNo = () => {
  navigator.clipboard.writeText(order.value.orderNo)
  ElMessage.success('订单号已复制到剪贴板')
}

const copyTrackingNo = () => {
  navigator.clipboard.writeText(order.value.trackingNo)
  ElMessage.success('运单号已复制到剪贴板')
}

const canCancel = (status) => status === 1 || status === 2
const canPay = (status) => status === 1
const canConfirm = (status) => status === 3
const canRefund = (status) => status === 2 || status === 3 || status === 4
const canReview = (item) => order.value.status >= 4 && !item.reviewed

const cancelOrder = async () => {
  try {
    await ElMessageBox.confirm(
      '取消订单后无法恢复，确定要取消这个订单吗？',
      '取消订单',
      {
        type: 'warning',
        confirmButtonText: '确定取消',
        cancelButtonText: '我再想想'
      }
    )
    
    cancelling.value = true
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    order.value.status = 6
    order.value.cancelTime = new Date()
    ElMessage.success('订单已取消')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消订单失败，请稍后重试')
    }
  } finally {
    cancelling.value = false
  }
}

const payOrder = () => {
  router.push(`/payment?orderId=${order.value.id}&amount=${order.value.totalAmount}`)
}

const confirmReceive = async () => {
  try {
    await ElMessageBox.confirm(
      '确认收货后订单将完成，无法撤销，请确认已收到商品且无质量问题',
      '确认收货',
      {
        type: 'warning',
        confirmButtonText: '确认收货',
        cancelButtonText: '我再检查一下'
      }
    )
    
    confirming.value = true
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    order.value.status = 4
    order.value.receiveTime = new Date()
    ElMessage.success('确认收货成功，感谢您的购买！')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('确认收货失败，请稍后重试')
    }
  } finally {
    confirming.value = false
  }
}

const applyRefund = () => {
  router.push(`/user/refund/apply?orderId=${order.value.id}`)
}

const contactService = () => {
  ElMessage.info('正在为您转接客服，请稍候...')
  // 这里可以集成在线客服系统
}

const goToProduct = (productId) => {
  router.push(`/product/${productId}`)
}

const buyAgain = (item) => {
  ElMessage.success(`${item.name} 已加入购物车`)
  // 这里可以调用加入购物车的API
}

const openReviewDialog = (item) => {
  currentReviewItem.value = item
  reviewDialogVisible.value = true
}

const resetReviewForm = () => {
  Object.assign(reviewForm, {
    rating: 5,
    content: '',
    images: [],
    anonymous: false
  })
  currentReviewItem.value = null
}

const submitReview = async () => {
  if (!reviewForm.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }
  
  submittingReview.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // 标记为已评价
    currentReviewItem.value.reviewed = true
    
    ElMessage.success('评价提交成功，感谢您的反馈！')
    reviewDialogVisible.value = false
  } catch (error) {
    ElMessage.error('评价提交失败，请稍后重试')
  } finally {
    submittingReview.value = false
  }
}

// 生命周期
onMounted(() => {
  loadOrderDetail()
})
</script>

<style scoped lang="scss">
.order-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.order-detail {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.order-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 32px;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    width: 200px;
    height: 200px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 50%;
    transform: translate(50%, -50%);
  }

  .order-status-section {
    display: flex;
    align-items: center;
    gap: 24px;
    margin-bottom: 32px;
    position: relative;
    z-index: 1;

    .status-icon {
      width: 80px;
      height: 80px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      backdrop-filter: blur(10px);
    }

    .status-info {
      flex: 1;

      .status-title {
        font-size: 28px;
        font-weight: 700;
        margin: 0 0 8px 0;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }

      .status-desc {
        font-size: 16px;
        margin: 0 0 12px 0;
        opacity: 0.9;
        line-height: 1.5;
      }

      .estimated-time {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 14px;
        background: rgba(255, 255, 255, 0.2);
        padding: 8px 16px;
        border-radius: 20px;
        backdrop-filter: blur(10px);
        width: fit-content;
      }
    }
  }

  .order-basic-info {
    position: relative;
    z-index: 1;

    .info-row {
      display: flex;
      gap: 40px;
      margin-bottom: 16px;

      &:last-child {
        margin-bottom: 0;
      }
    }

    .info-item {
      display: flex;
      flex-direction: column;
      gap: 4px;

      .label {
        font-size: 14px;
        opacity: 0.8;
        font-weight: 500;
      }

      .value {
        font-size: 16px;
        font-weight: 600;

        &.amount {
          font-size: 20px;
          color: #FFD700;
        }
      }

      .value-with-action {
        display: flex;
        align-items: center;
        gap: 8px;

        .copy-btn {
          color: rgba(255, 255, 255, 0.8);
          padding: 4px;

          &:hover {
            color: #fff;
            background: rgba(255, 255, 255, 0.2);
          }
        }
      }
    }
  }
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 20px 0;
  padding: 0 0 12px 0;
  border-bottom: 2px solid #f0f2f5;
}

.order-progress {
  padding: 32px;
  border-bottom: 1px solid #f0f2f5;

  .progress-timeline {
    .timeline-content {
      .step-title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 4px 0;
      }

      .step-desc {
        font-size: 14px;
        color: #606266;
        margin: 0 0 8px 0;
        line-height: 1.5;
      }

      .step-extra {
        font-size: 13px;
        color: #909399;
        background: #f5f7fa;
        padding: 6px 12px;
        border-radius: 6px;
        display: inline-block;
      }
    }
  }
}

.logistics-info {
  padding: 32px;
  border-bottom: 1px solid #f0f2f5;

  .logistics-card {
    background: #f8fafc;
    border-radius: 12px;
    padding: 24px;
    border: 1px solid #e4e7ed;

    .logistics-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
      padding-bottom: 16px;
      border-bottom: 1px solid #e4e7ed;

      .courier-info {
        .company-info {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 8px;

          .courier-logo {
            width: 40px;
            height: 40px;
            border-radius: 8px;
            object-fit: cover;
          }

          .courier-company {
            font-size: 16px;
            font-weight: 600;
            color: #303133;
          }
        }

        .tracking-info {
          display: flex;
          align-items: center;
          gap: 8px;

          .tracking-no {
            font-size: 14px;
            color: #606266;
            font-family: 'Courier New', monospace;
          }

          .copy-btn {
            color: #409eff;
            padding: 2px;

            &:hover {
              background: #ecf5ff;
            }
          }
        }
      }
    }

    .logistics-timeline {
      .logistics-item {
        .logistics-desc {
          font-size: 14px;
          color: #303133;
          margin-bottom: 4px;
        }

        .logistics-location {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
}

.order-products {
  padding: 32px;
  border-bottom: 1px solid #f0f2f5;

  .products-list {
    .product-item {
      display: flex;
      align-items: center;
      gap: 20px;
      padding: 20px;
      background: #fafbfc;
      border-radius: 12px;
      margin-bottom: 16px;
      cursor: pointer;
      transition: all 0.3s ease;
      border: 1px solid #e4e7ed;

      &:hover {
        background: #f0f9ff;
        border-color: #409eff;
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
      }

      &:last-child {
        margin-bottom: 0;
      }

      .product-image {
        width: 120px;
        height: 120px;
        flex-shrink: 0;
        border-radius: 8px;
        overflow: hidden;
        position: relative;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .product-badge {
          position: absolute;
          top: 8px;
          left: 8px;
        }
      }

      .product-info {
        flex: 1;

        .product-name {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin: 0 0 8px 0;
          line-height: 1.4;
        }

        .product-spec {
          font-size: 14px;
          color: #606266;
          margin: 0 0 12px 0;
        }

        .product-tags {
          display: flex;
          gap: 8px;
          margin-bottom: 12px;
        }

        .product-price {
          display: flex;
          align-items: center;
          gap: 16px;

          .price {
            font-size: 18px;
            font-weight: 600;
            color: #e74c3c;
          }

          .quantity {
            font-size: 14px;
            color: #606266;
          }

          .subtotal {
            font-size: 14px;
            color: #909399;
          }
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
  }
}

.delivery-info {
  padding: 32px;
  border-bottom: 1px solid #f0f2f5;

  .delivery-card {
    background: #f8fafc;
    border-radius: 12px;
    padding: 24px;
    border: 1px solid #e4e7ed;

    .recipient-info {
      .recipient-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;

        .name-phone {
          display: flex;
          align-items: center;
          gap: 16px;

          .name {
            font-size: 16px;
            font-weight: 600;
            color: #303133;
          }

          .phone {
            font-size: 14px;
            color: #606266;
          }
        }
      }

      .address {
        display: flex;
        align-items: flex-start;
        gap: 8px;
        font-size: 14px;
        color: #303133;
        line-height: 1.6;
        margin-bottom: 12px;
      }

      .delivery-note {
        display: flex;
        align-items: flex-start;
        gap: 8px;
        font-size: 14px;
        color: #606266;
        background: #fff;
        padding: 12px;
        border-radius: 8px;

        .note-label {
          font-weight: 500;
          color: #303133;
        }
      }
    }
  }
}

.order-summary {
  padding: 32px;
  border-bottom: 1px solid #f0f2f5;

  .summary-card {
    background: #f8fafc;
    border-radius: 12px;
    padding: 24px;
    border: 1px solid #e4e7ed;

    .summary-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #e4e7ed;

      &:last-child {
        border-bottom: none;
      }

      &.total {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        background: #fff;
        margin: 12px -12px -12px -12px;
        padding: 16px 12px;
        border-radius: 8px;
      }

      .label {
        font-size: 14px;
        color: #606266;
      }

      .value {
        font-size: 14px;
        color: #303133;
        font-weight: 500;

        &.discount {
          color: #e74c3c;
        }
      }
    }
  }
}

.order-notes {
  padding: 32px;
  border-bottom: 1px solid #f0f2f5;

  .notes-card {
    background: #f8fafc;
    border-radius: 12px;
    padding: 20px;
    border: 1px solid #e4e7ed;

    p {
      font-size: 14px;
      color: #606266;
      line-height: 1.6;
      margin: 0;
    }
  }
}

.order-actions {
  padding: 32px;

  .action-buttons {
    display: flex;
    gap: 16px;
    justify-content: flex-end;

    .el-button {
      border-radius: 8px;
      padding: 12px 24px;

      &.el-button--large {
        padding: 16px 32px;
        font-size: 16px;
      }
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
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.review-form {
  .review-product {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px;
    background: #f5f7fa;
    border-radius: 8px;
    margin-bottom: 24px;

    img {
      width: 60px;
      height: 60px;
      border-radius: 6px;
      object-fit: cover;
    }

    .product-info {
      h4 {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 4px 0;
      }

      p {
        font-size: 14px;
        color: #606266;
        margin: 0;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .order-detail-container {
    padding: 16px;
  }

  .order-header {
    padding: 24px 20px;

    .order-status-section {
      flex-direction: column;
      text-align: center;
      gap: 16px;

      .status-icon {
        width: 60px;
        height: 60px;
      }

      .status-info {
        .status-title {
          font-size: 24px;
        }
      }
    }

    .order-basic-info {
      .info-row {
        flex-direction: column;
        gap: 16px;
      }
    }
  }

  .order-progress,
  .logistics-info,
  .order-products,
  .delivery-info,
  .order-summary,
  .order-notes,
  .order-actions {
    padding: 24px 20px;
  }

  .logistics-info {
    .logistics-card {
      .logistics-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;
      }
    }
  }

  .order-products {
    .products-list {
      .product-item {
        flex-direction: column;
        text-align: center;
        gap: 16px;

        .product-image {
          width: 100px;
          height: 100px;
        }

        .product-actions {
          width: 100%;
          flex-direction: row;
          gap: 12px;
        }
      }
    }
  }

  .order-actions {
    .action-buttons {
      flex-direction: column;

      .el-button {
        width: 100%;
      }
    }
  }
}
</style>