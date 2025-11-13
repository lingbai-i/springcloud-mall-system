import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

/**
 * 获取当前用户ID
 */
const getUserId = () => {
  const userStore = useUserStore()
  return userStore.userId || 1  // 开发模式默认使用 userId=1
}

// 获取购物车列表
export function getCartList() {
  return request({
    url: '/cart-service/cart/list',
    method: 'get',
    params: { userId: getUserId() }
  })
}

// 添加商品到购物车
export function addToCart(data) {
  return request({
    url: '/cart-service/cart/add',
    method: 'post',
    params: {
      userId: getUserId(),
      productId: data.productId,
      quantity: data.quantity,
      specifications: data.specifications
    }
  })
}

// 更新购物车商品数量
export function updateCartItem(data) {
  return request({
    url: '/cart-service/cart/update',
    method: 'put',
    params: {
      userId: getUserId(),
      productId: data.productId,
      quantity: data.quantity
    }
  })
}

// 删除购物车商品
export function removeCartItem(productId) {
  return request({
    url: '/cart-service/cart/remove',
    method: 'delete',
    params: { 
      userId: getUserId(),
      productId 
    }
  })
}

// 清空购物车
export function clearCart() {
  return request({
    url: '/cart-service/cart/clear',
    method: 'delete',
    params: { userId: getUserId() }
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
    url: '/cart-service/cart/select',
    method: 'put',
    params: { 
      userId: getUserId(),
      productId, 
      selected 
    }
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