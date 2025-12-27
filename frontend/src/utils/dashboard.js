/**
 * 仪表盘工具函数
 * 
 * @author system
 * @date 2025-12-25
 */

/**
 * 订单状态映射
 */
export const ORDER_STATUS_MAP = {
  PENDING_PAYMENT: '待付款',
  PENDING: '待付款',
  PAID: '待发货',
  SHIPPED: '已发货',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  REFUND_PENDING: '退款中',
  REFUNDED: '已退款'
}

/**
 * 订单状态类型映射（Element Plus Tag类型）
 */
export const ORDER_STATUS_TYPE_MAP = {
  PENDING_PAYMENT: 'warning',
  PENDING: 'warning',
  PAID: 'primary',
  SHIPPED: 'info',
  COMPLETED: 'success',
  CANCELLED: 'danger',
  REFUND_PENDING: 'warning',
  REFUNDED: 'info'
}

/**
 * 计算趋势百分比
 * 
 * Property 2: 趋势百分比计算正确
 * *For any* current period value C and previous period value P where P > 0,
 * the trend percentage SHALL be calculated as ((C - P) / P) * 100
 * **Validates: Requirements 1.5**
 * 
 * @param {number} today 今日值
 * @param {number} yesterday 昨日值
 * @returns {number} 趋势百分比
 */
export function calculateTrend(today, yesterday) {
  // 处理无效输入
  const todayValue = Number(today) || 0
  const yesterdayValue = Number(yesterday) || 0
  
  if (yesterdayValue === 0) {
    return todayValue > 0 ? 100 : 0
  }
  
  return Math.round(((todayValue - yesterdayValue) / yesterdayValue) * 100)
}

/**
 * 格式化金额
 * 
 * @param {number} amount 金额
 * @param {string} prefix 前缀，默认为 ¥
 * @returns {string} 格式化后的金额字符串
 */
export function formatAmount(amount, prefix = '¥') {
  const value = Number(amount)
  if (isNaN(value)) {
    return `${prefix}0.00`
  }
  return `${prefix}${value.toFixed(2)}`
}

/**
 * 格式化百分比
 * 
 * @param {number} value 小数值（如 0.5 表示 50%）
 * @param {number} decimals 小数位数，默认为 1
 * @returns {string} 格式化后的百分比字符串
 */
export function formatPercentage(value, decimals = 1) {
  const num = Number(value)
  if (isNaN(num)) {
    return '0%'
  }
  return `${(num * 100).toFixed(decimals)}%`
}

/**
 * 获取周期对应的天数
 * 
 * Property 3: 销售趋势周期选择
 * *For any* selected period (7days, 30days, 90days),
 * the dashboard SHALL call the sales trend API with the corresponding days parameter
 * **Validates: Requirements 2.2, 2.3, 2.4**
 * 
 * @param {string} period 周期字符串
 * @returns {number} 天数
 */
export function getPeriodDays(period) {
  const periodMap = {
    '7days': 7,
    '7d': 7,
    '30days': 30,
    '30d': 30,
    '90days': 90,
    '90d': 90
  }
  return periodMap[period] || 7
}

/**
 * 获取订单状态文本
 * 
 * Property 4: 最近订单数据完整性
 * *For any* order in the recent orders list,
 * the displayed data SHALL include status text
 * **Validates: Requirements 4.1, 4.2**
 * 
 * @param {string} status 订单状态
 * @returns {string} 状态文本
 */
export function getOrderStatusText(status) {
  return ORDER_STATUS_MAP[status] || '未知'
}

/**
 * 获取订单状态类型（Element Plus Tag类型）
 * 
 * @param {string} status 订单状态
 * @returns {string} 状态类型
 */
export function getOrderStatusType(status) {
  return ORDER_STATUS_TYPE_MAP[status] || 'info'
}

/**
 * 格式化日期时间
 * 
 * Property 7: 错误处理降级
 * *For any* invalid datetime input, formatDateTime SHALL return empty string
 * **Validates: Requirements 1.6, 7.2, 7.3**
 * 
 * @param {string} datetime 日期时间字符串
 * @returns {string} 格式化后的日期时间
 */
export function formatDateTime(datetime) {
  if (!datetime || typeof datetime !== 'string') {
    return ''
  }
  
  try {
    const date = new Date(datetime)
    if (isNaN(date.getTime())) {
      return ''
    }
    
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  } catch (e) {
    return ''
  }
}

/**
 * 格式化短日期（MM-dd）
 * 
 * @param {string} date 日期字符串
 * @returns {string} 格式化后的短日期
 */
export function formatShortDate(date) {
  if (!date || typeof date !== 'string') {
    return ''
  }
  
  // 尝试提取 MM-dd 部分
  const match = date.match(/\d{4}-(\d{2}-\d{2})/)
  return match ? match[1] : ''
}

/**
 * 计算交易额
 * 
 * Property 6: 交易额计算正确
 * *For any* set of orders, the total transaction amount SHALL equal
 * the sum of payable amounts from all completed orders
 * **Validates: Requirements 6.1, 6.2**
 * 
 * @param {Array} orders 订单列表
 * @returns {number} 总交易额
 */
export function calculateTransactionAmount(orders) {
  if (!Array.isArray(orders)) {
    return 0
  }
  
  return orders
    .filter(order => order && order.status === 'COMPLETED')
    .reduce((sum, order) => {
      const amount = Number(order.payableAmount) || 0
      return sum + amount
    }, 0)
}
