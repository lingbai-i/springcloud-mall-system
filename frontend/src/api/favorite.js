import request from '@/utils/request'

/**
 * 收藏相关API
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-12-30
 */

/**
 * 添加收藏
 * @param {number} productId 商品ID
 */
export function addFavorite(productId) {
  return request({
    url: `/favorites/${productId}`,
    method: 'post'
  })
}

/**
 * 取消收藏
 * @param {number} productId 商品ID
 */
export function removeFavorite(productId) {
  return request({
    url: `/favorites/${productId}`,
    method: 'delete'
  })
}

/**
 * 批量取消收藏
 * @param {number[]} productIds 商品ID列表
 */
export function batchRemoveFavorites(productIds) {
  return request({
    url: '/favorites/batch',
    method: 'delete',
    data: productIds
  })
}

/**
 * 获取收藏列表
 * @param {Object} params 分页参数
 * @param {number} params.page 页码
 * @param {number} params.size 每页大小
 */
export function getFavorites(params = {}) {
  return request({
    url: '/favorites',
    method: 'get',
    params: {
      page: params.page || 1,
      size: params.size || 10
    }
  })
}

/**
 * 检查收藏状态
 * @param {number} productId 商品ID
 */
export function checkFavoriteStatus(productId) {
  return request({
    url: `/favorites/${productId}/status`,
    method: 'get'
  })
}

/**
 * 获取收藏数量
 */
export function getFavoriteCount() {
  return request({
    url: '/favorites/count',
    method: 'get'
  })
}

export default {
  addFavorite,
  removeFavorite,
  batchRemoveFavorites,
  getFavorites,
  checkFavoriteStatus,
  getFavoriteCount
}
