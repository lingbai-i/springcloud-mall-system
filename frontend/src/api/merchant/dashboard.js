import request from '@/utils/request'

/**
 * 商家仪表盘API
 * 提供仪表盘页面所需的各项统计数据接口
 */

// ==================== 统计数据 ====================

/**
 * 获取商家总览统计数据
 * @param {Number} merchantId 商家ID
 * @returns {Promise} 总览统计数据
 */
export function getOverviewStatistics(merchantId) {
  return request({
    url: '/merchant/statistics/overview',
    method: 'get',
    params: { merchantId }
  })
}

/**
 * 获取今日统计数据
 * @param {Number} merchantId 商家ID
 * @returns {Promise} 今日统计数据
 */
export function getTodayStatistics(merchantId) {
  return request({
    url: '/merchant/statistics/today',
    method: 'get',
    params: { merchantId }
  })
}

/**
 * 获取昨日统计数据
 * @param {Number} merchantId 商家ID
 * @returns {Promise} 昨日统计数据
 */
export function getYesterdayStatistics(merchantId) {
  return request({
    url: '/merchant/statistics/yesterday',
    method: 'get',
    params: { merchantId }
  })
}

/**
 * 获取销售趋势数据（近期日统计）
 * @param {Number} merchantId 商家ID
 * @param {Number} days 天数（7/30/90）
 * @returns {Promise} 销售趋势数据列表
 */
export function getSalesTrend(merchantId, days = 7) {
  return request({
    url: '/merchant/statistics/recent-daily',
    method: 'get',
    params: { merchantId, days }
  })
}

/**
 * 获取关键指标数据
 * @param {Number} merchantId 商家ID
 * @returns {Promise} 关键指标数据（转化率、客单价等）
 */
export function getKeyMetrics(merchantId) {
  return request({
    url: '/merchant/statistics/key-metrics',
    method: 'get',
    params: { merchantId }
  })
}


// ==================== 订单数据 ====================

/**
 * 获取最近订单列表
 * @param {Number} merchantId 商家ID
 * @param {Number} limit 数量限制，默认10
 * @returns {Promise} 最近订单列表
 */
export function getRecentOrders(merchantId, limit = 10) {
  return request({
    url: '/merchant/order/recent',
    method: 'get',
    params: { merchantId, limit }
  })
}

/**
 * 获取订单状态统计
 * @param {Number} merchantId 商家ID
 * @returns {Promise} 各状态订单数量统计
 */
export function getOrderStatusStatistics(merchantId) {
  return request({
    url: '/merchant/order/status-statistics',
    method: 'get',
    params: { merchantId }
  })
}

/**
 * 获取热销商品统计
 * @param {Number} merchantId 商家ID
 * @param {Number} limit 数量限制，默认10
 * @returns {Promise} 热销商品列表
 */
export function getHotProducts(merchantId, limit = 10) {
  return request({
    url: '/merchant/order/hot-products',
    method: 'get',
    params: { merchantId, limit }
  })
}

/**
 * 获取每日销售统计
 * @param {Number} merchantId 商家ID
 * @param {String} startDate 开始日期 (yyyy-MM-dd)
 * @param {String} endDate 结束日期 (yyyy-MM-dd)
 * @returns {Promise} 每日销售统计列表
 */
export function getDailySalesStatistics(merchantId, startDate, endDate) {
  return request({
    url: '/merchant/order/daily-sales',
    method: 'get',
    params: { merchantId, startTime: startDate, endTime: endDate }
  })
}
