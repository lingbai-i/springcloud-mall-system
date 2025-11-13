import request from '@/utils/request'

/**
 * 商家商品管理API
 */

// ==================== 商品基础管理 ====================

/**
 * 创建商品
 * @param {Object} data 商品数据
 */
export function createProduct(data) {
  return request({
    url: '/merchant/products',
    method: 'post',
    data
  })
}

/**
 * 更新商品
 * @param {Number} id 商品ID
 * @param {Object} data 商品数据
 */
export function updateProduct(id, data) {
  return request({
    url: `/merchant/products/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除商品
 * @param {Number} id 商品ID
 * @param {Number} merchantId 商家ID
 */
export function deleteProduct(id, merchantId) {
  return request({
    url: `/merchant/products/${id}`,
    method: 'delete',
    params: { merchantId }
  })
}

/**
 * 获取商品详情
 * @param {Number} id 商品ID
 * @param {Number} merchantId 商家ID（可选）
 */
export function getProductById(id, merchantId) {
  return request({
    url: `/merchant/products/${id}`,
    method: 'get',
    params: { merchantId }
  })
}

/**
 * 获取商品列表
 * @param {Object} params 查询参数
 */
export function getProductList(params) {
  return request({
    url: '/merchant/products/list',
    method: 'get',
    params
  })
}

// ==================== 商品状态管理 ====================

/**
 * 商品上架
 * @param {Number} id 商品ID
 * @param {Number} merchantId 商家ID
 */
export function productOnShelf(id, merchantId) {
  return request({
    url: `/merchant/products/${id}/on-shelf`,
    method: 'post',
    params: { merchantId }
  })
}

/**
 * 商品下架
 * @param {Number} id 商品ID
 * @param {Number} merchantId 商家ID
 */
export function productOffShelf(id, merchantId) {
  return request({
    url: `/merchant/products/${id}/off-shelf`,
    method: 'post',
    params: { merchantId }
  })
}

/**
 * 批量上架
 * @param {Number} merchantId 商家ID
 * @param {Array} productIds 商品ID列表
 */
export function batchOnShelf(merchantId, productIds) {
  return request({
    url: '/merchant/products/batch-on-shelf',
    method: 'post',
    params: { merchantId },
    data: productIds
  })
}

/**
 * 批量下架
 * @param {Number} merchantId 商家ID
 * @param {Array} productIds 商品ID列表
 */
export function batchOffShelf(merchantId, productIds) {
  return request({
    url: '/merchant/products/batch-off-shelf',
    method: 'post',
    params: { merchantId },
    data: productIds
  })
}

/**
 * 批量删除
 * @param {Number} merchantId 商家ID
 * @param {Array} productIds 商品ID列表
 */
export function batchDelete(merchantId, productIds) {
  return request({
    url: '/merchant/products/batch',
    method: 'delete',
    params: { merchantId },
    data: productIds
  })
}

// ==================== 库存管理 ====================

/**
 * 更新商品库存
 * @param {Number} id 商品ID
 * @param {Number} merchantId 商家ID
 * @param {Object} data 库存数据 { stock, lowStockThreshold, reason }
 */
export function updateInventory(id, merchantId, data) {
  return request({
    url: `/merchant/products/${id}/inventory`,
    method: 'put',
    params: { merchantId },
    data
  })
}

/**
 * 批量更新库存
 * @param {Object} data { merchantId, items: [{ productId, stock, lowStockThreshold, reason }] }
 */
export function batchUpdateInventory(data) {
  return request({
    url: '/merchant/products/inventory/batch',
    method: 'put',
    data
  })
}

/**
 * 获取库存预警列表
 * @param {Object} params { merchantId, page, size }
 */
export function getInventoryAlerts(params) {
  return request({
    url: '/merchant/products/inventory/alerts',
    method: 'get',
    params
  })
}

/**
 * 获取库存不足商品
 * @param {Object} params { merchantId, threshold, page, size }
 */
export function getLowStockProducts(params) {
  return request({
    url: '/merchant/products/low-stock',
    method: 'get',
    params
  })
}

// ==================== 商品统计 ====================

/**
 * 获取商品统计信息
 * @param {Number} id 商品ID
 * @param {Number} merchantId 商家ID
 */
export function getProductStatistics(id, merchantId) {
  return request({
    url: `/merchant/products/${id}/statistics`,
    method: 'get',
    params: { merchantId }
  })
}

/**
 * 获取热销商品
 * @param {Object} params { merchantId, limit }
 */
export function getHotSellingProducts(params) {
  return request({
    url: '/merchant/products/hot-selling',
    method: 'get',
    params
  })
}

/**
 * 获取推荐商品
 * @param {Object} params { merchantId, page, size }
 */
export function getRecommendedProducts(params) {
  return request({
    url: '/merchant/products/recommended',
    method: 'get',
    params
  })
}

/**
 * 获取新品商品
 * @param {Object} params { merchantId, page, size }
 */
export function getNewProducts(params) {
  return request({
    url: '/merchant/products/new-products',
    method: 'get',
    params
  })
}

// ==================== 商品设置 ====================

/**
 * 更新商品价格
 * @param {Number} id 商品ID
 * @param {Number} merchantId 商家ID
 * @param {Number} price 新价格
 */
export function updateProductPrice(id, merchantId, price) {
  return request({
    url: `/merchant/products/${id}/price`,
    method: 'put',
    params: { merchantId, price }
  })
}

/**
 * 设置推荐商品
 * @param {Number} id 商品ID
 * @param {Number} merchantId 商家ID
 * @param {Boolean} isRecommended 是否推荐
 */
export function setProductRecommended(id, merchantId, isRecommended) {
  return request({
    url: `/merchant/products/${id}/recommend`,
    method: 'put',
    params: { merchantId, isRecommended }
  })
}

/**
 * 设置新品商品
 * @param {Number} id 商品ID
 * @param {Number} merchantId 商家ID
 * @param {Boolean} isNew 是否新品
 */
export function setProductNew(id, merchantId, isNew) {
  return request({
    url: `/merchant/products/${id}/new`,
    method: 'put',
    params: { merchantId, isNew }
  })
}

/**
 * 导出商品数据
 * @param {Object} params 导出参数
 */
export function exportProducts(params) {
  return request({
    url: '/merchant/products/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
