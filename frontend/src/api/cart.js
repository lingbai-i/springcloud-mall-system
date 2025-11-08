import request from '@/utils/request'

/**
 * 购物车相关API
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */

// 获取购物车列表
export function getCartList() {
  return request({
    url: '/cart-service/cart',
    method: 'get'
  })
}

// 添加商品到购物车
export function addToCart(data) {
  return request({
    url: '/cart-service/cart/add',
    method: 'post',
    data
  })
}

// 更新购物车商品数量
export function updateCartItem(data) {
  return request({
    url: '/cart-service/cart/update',
    method: 'put',
    data
  })
}

// 删除购物车商品
export function removeCartItem(productId) {
  return request({
    url: `/cart-service/cart/remove/${productId}`,
    method: 'delete'
  })
}

// 清空购物车
export function clearCart() {
  return request({
    url: '/cart-service/cart/clear',
    method: 'delete'
  })
}

// 获取购物车商品数量
export function getCartCount(userId) {
  return request({
    url: '/cart-service/cart/count',
    method: 'get',
    params: { userId }
  })
}

// 批量删除购物车商品
export function batchRemoveCartItems(productIds) {
  return request({
    url: '/cart-service/cart/batch-remove',
    method: 'delete',
    data: { productIds }
  })
}

// 选中/取消选中购物车商品
export function toggleCartItemSelect(productId, selected) {
  return request({
    url: `/cart-service/cart/select/${productId}`,
    method: 'put',
    data: { selected }
  })
}

// 全选/取消全选购物车商品
export function toggleCartSelectAll(selected) {
  return request({
    url: '/cart-service/cart/select-all',
    method: 'put',
    data: { selected }
  })
}

// 导出cartApi对象，包含所有购物车相关API
export const cartApi = {
  getCartList,
  addToCart,
  updateCartItem,
  removeCartItem,
  clearCart,
  getCartCount,
  batchRemoveCartItems,
  toggleCartItemSelect,
  toggleCartSelectAll
}

export default cartApi