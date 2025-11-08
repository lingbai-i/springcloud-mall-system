import request from '@/utils/request'

/**
 * 订单相关API
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */

// 获取订单列表
export function getOrderList(params) {
  return request({
    url: '/order-service/orders',
    method: 'get',
    params
  })
}

// 获取订单详情
export function getOrderDetail(id) {
  return request({
    url: `/order-service/orders/${id}`,
    method: 'get'
  })
}

// 创建订单
export function createOrder(data) {
  return request({
    url: '/order-service/orders',
    method: 'post',
    data
  })
}

// 取消订单
export function cancelOrder(id, reason) {
  return request({
    url: `/order-service/orders/${id}/cancel`,
    method: 'put',
    data: { reason }
  })
}

// 确认收货
export function confirmReceive(id) {
  return request({
    url: `/order-service/orders/${id}/confirm`,
    method: 'put'
  })
}

// 申请退款
export function applyRefund(id, data) {
  return request({
    url: `/order-service/orders/${id}/refund`,
    method: 'post',
    data
  })
}

// 获取物流信息
export function getLogistics(id) {
  return request({
    url: `/order-service/orders/${id}/logistics`,
    method: 'get'
  })
}

// 订单支付
export function payOrder(id, paymentMethod) {
  return request({
    url: `/order-service/orders/${id}/pay`,
    method: 'post',
    data: { paymentMethod }
  })
}

// 获取订单统计
export function getOrderStats() {
  return request({
    url: '/order-service/orders/stats',
    method: 'get'
  })
}

// 重新购买
export function reorder(id) {
  return request({
    url: `/order-service/orders/${id}/reorder`,
    method: 'post'
  })
}

// 导出orderApi对象，包含所有订单相关API
export const orderApi = {
  getOrderList,
  getOrderDetail,
  createOrder,
  cancelOrder,
  confirmReceive,
  applyRefund,
  getLogistics,
  payOrder,
  getOrderStats,
  reorder
}

export default orderApi