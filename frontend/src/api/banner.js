import request from '@/utils/request'

/**
 * 用户端轮播图展示API
 * 提供活跃轮播图列表、点击记录、曝光记录等接口
 */

// ==================== 轮播图展示 ====================

/**
 * 获取活跃轮播图列表
 * @returns {Promise} 活跃轮播图列表
 */
export function getActiveBanners() {
  return request({
    url: '/banner/active',
    method: 'get'
  })
}

/**
 * 记录轮播图点击
 * @param {Number} bannerId 轮播图ID
 * @returns {Promise}
 */
export function recordBannerClick(bannerId) {
  return request({
    url: `/banner/${bannerId}/click`,
    method: 'post'
  })
}

/**
 * 记录轮播图曝光
 * @param {Number} bannerId 轮播图ID
 * @returns {Promise}
 */
export function recordBannerImpression(bannerId) {
  return request({
    url: `/banner/${bannerId}/impression`,
    method: 'post'
  })
}
