<template>
  <div class="payment-container">
    <div class="payment-card">
      <div class="payment-header">
        <h2>订单支付</h2>
        <p class="order-no">订单号：{{ orderInfo.orderNo }}</p>
      </div>

      <!-- 支付倒计时 -->
      <div class="payment-countdown" v-if="!orderExpired && countdown > 0">
        <el-icon class="countdown-icon"><Clock /></el-icon>
        <span class="countdown-text">
          请在 <span class="countdown-time">{{ formatCountdown }}</span> 内完成支付，超时订单将自动取消
        </span>
      </div>
      
      <!-- 订单已过期提示 -->
      <div class="payment-expired" v-if="orderExpired">
        <el-icon class="expired-icon"><WarningFilled /></el-icon>
        <span class="expired-text">订单已超时，请重新下单</span>
      </div>

      <div class="payment-amount">
        <span class="label">支付金额</span>
        <span class="amount">¥{{ orderInfo.totalAmount }}</span>
      </div>

      <div class="payment-methods">
        <h3>选择支付方式</h3>
        <div class="method-list">
          <div 
            class="method-item" 
            :class="{ active: selectedMethod === 'alipay' }"
            @click="selectedMethod = 'alipay'">
            <div class="method-icon alipay-icon">支</div>
            <span>支付宝</span>
          </div>
          <div 
            class="method-item"
            :class="{ active: selectedMethod === 'wechat' }"
            @click="selectedMethod = 'wechat'">
            <div class="method-icon wechat-icon">微</div>
            <span>微信支付</span>
          </div>
        </div>
      </div>

      <div class="payment-actions">
        <el-button 
          type="primary" 
          size="large" 
          @click="handlePay" 
          :loading="paying"
          :disabled="orderExpired">
          {{ orderExpired ? '订单已超时' : '确认支付' }}
        </el-button>
        <el-button size="large" @click="goBack">返回</el-button>
      </div>

      <div class="payment-tips">
        <p>提示：这是模拟支付，点击确认支付将直接完成支付</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock, WarningFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const orderId = ref(route.params.id)
const orderInfo = ref({
  orderNo: '',
  totalAmount: 0,
  createTime: null
})
const selectedMethod = ref('alipay')
const paying = ref(false)

// 支付超时时间（10分钟，单位：毫秒）
const PAYMENT_TIMEOUT_MS = 10 * 60 * 1000

// 倒计时相关
const countdown = ref(0)
const orderExpired = ref(false)
let countdownTimer = null

/**
 * 格式化倒计时显示
 * @returns {string} 格式化后的时间字符串 (mm:ss)
 */
const formatCountdown = computed(() => {
  const minutes = Math.floor(countdown.value / 60)
  const seconds = countdown.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

/**
 * 启动倒计时
 * @param {string} createTime - 订单创建时间
 */
const startCountdown = (createTime) => {
  if (!createTime) return
  
  // 计算剩余时间
  const orderTime = new Date(createTime).getTime()
  const expireTime = orderTime + PAYMENT_TIMEOUT_MS
  const now = Date.now()
  const remainingMs = expireTime - now
  
  if (remainingMs <= 0) {
    // 订单已超时
    orderExpired.value = true
    countdown.value = 0
    return
  }
  
  // 设置初始倒计时（秒）
  countdown.value = Math.floor(remainingMs / 1000)
  
  // 启动定时器
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      // 倒计时结束，订单超时
      clearInterval(countdownTimer)
      orderExpired.value = true
      ElMessage.warning('订单已超时，请重新下单')
    }
  }, 1000)
}

/**
 * 停止倒计时
 */
const stopCountdown = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

// 获取订单信息
const fetchOrderInfo = async () => {
  try {
    const userId = userStore.userId || localStorage.getItem('userId')
    const response = await fetch(`/api/order-service/orders/${orderId.value}?userId=${userId}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    const result = await response.json()
    if (result.success && result.data) {
      const data = result.data
      orderInfo.value = {
        orderNo: data.orderNo,
        totalAmount: data.totalAmount || data.payAmount || 0,
        createTime: data.createTime
      }
      
      // 检查订单状态，只有待支付状态才启动倒计时
      if (data.status === 'PENDING') {
        startCountdown(data.createTime)
      } else if (data.status === 'CANCELLED') {
        orderExpired.value = true
      }
    }
  } catch (error) {
    console.error('获取订单信息失败:', error)
  }
}

// 模拟支付
const handlePay = async () => {
  paying.value = true
  try {
    const userId = userStore.userId || localStorage.getItem('userId')
    // 调用支付接口
    const response = await fetch(`/api/order-service/orders/${orderId.value}/pay?userId=${userId}&paymentMethod=${selectedMethod.value}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    const result = await response.json()
    console.log('支付结果:', result)
    
    // 检查支付是否成功（后端返回的data中包含success字段）
    if (result.success || (result.data && result.data.success)) {
      ElMessage.success('支付成功！')
      // 延迟跳转，让用户看到成功提示
      setTimeout(() => {
        router.push('/user/orders')
      }, 1000)
    } else {
      ElMessage.error(result.message || '支付失败')
    }
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error('支付失败，请重试')
  } finally {
    paying.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchOrderInfo()
})

// 组件卸载时清理定时器
onUnmounted(() => {
  stopCountdown()
})
</script>

<style scoped>
.payment-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 40px 20px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.payment-card {
  background: white;
  border-radius: 12px;
  padding: 40px;
  width: 100%;
  max-width: 500px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.payment-header {
  text-align: center;
  margin-bottom: 30px;
}

.payment-header h2 {
  margin: 0 0 10px 0;
  color: #303133;
}

.order-no {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

/* 支付倒计时样式 */
.payment-countdown {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 16px;
  margin: 20px 0;
  background: #fdf6ec;
  border: 1px solid #faecd8;
  border-radius: 6px;
  color: #e6a23c;
}

.countdown-icon {
  font-size: 18px;
  margin-right: 8px;
}

.countdown-text {
  font-size: 14px;
}

.countdown-time {
  font-weight: 600;
  color: #f56c6c;
  font-size: 16px;
  margin: 0 4px;
}

/* 订单过期样式 */
.payment-expired {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 16px;
  margin: 20px 0;
  background: #fef0f0;
  border: 1px solid #fde2e2;
  border-radius: 6px;
  color: #f56c6c;
}

.expired-icon {
  font-size: 18px;
  margin-right: 8px;
}

.expired-text {
  font-size: 14px;
  font-weight: 500;
}

.payment-amount {
  text-align: center;
  padding: 30px 0;
  border-top: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
}

.payment-amount .label {
  display: block;
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}

.payment-amount .amount {
  font-size: 36px;
  font-weight: 600;
  color: #f56c6c;
}

.payment-methods {
  padding: 30px 0;
}

.payment-methods h3 {
  margin: 0 0 20px 0;
  font-size: 16px;
  color: #303133;
}

.method-list {
  display: flex;
  gap: 20px;
}

.method-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  border: 2px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.method-item:hover {
  border-color: #409eff;
}

.method-item.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.method-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
  color: white;
  margin-bottom: 10px;
}

.alipay-icon {
  background: #1677ff;
}

.wechat-icon {
  background: #07c160;
}

.payment-actions {
  display: flex;
  gap: 15px;
  margin-top: 30px;
}

.payment-actions .el-button {
  flex: 1;
}

.payment-tips {
  margin-top: 20px;
  text-align: center;
}

.payment-tips p {
  color: #909399;
  font-size: 12px;
  margin: 0;
}
</style>
