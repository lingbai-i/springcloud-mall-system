/**
 * 商家相关API接口
 * @author lingbai
 * @date 2025-01-27
 */
import request from '@/utils/request'

/**
 * 商家登录
 * @param {Object} data 登录数据
 * @param {string} data.username 用户名
 * @param {string} data.password 密码
 * @param {string} data.captcha 验证码
 * @param {boolean} data.remember 记住登录
 * @returns {Promise} 登录结果
 */
export const merchantLogin = (data) => {
  return request({
    url: '/merchant/auth/login',
    method: 'post',
    data
  })
}

/**
 * 商家登出
 * @returns {Promise} 登出结果
 */
export const merchantLogout = () => {
  return request({
    url: '/merchant/auth/logout',
    method: 'post'
  })
}

/**
 * 获取商家信息
 * @returns {Promise} 商家信息
 */
export const getMerchantInfo = () => {
  return request({
    url: '/merchant/auth/info',
    method: 'get'
  })
}

/**
 * 更新商家信息
 * @param {Object} data 商家信息
 * @returns {Promise} 更新结果
 */
export const updateMerchantInfo = (data) => {
  return request({
    url: '/merchant/profile',
    method: 'put',
    data
  })
}

/**
 * 获取商家统计数据
 * @returns {Promise} 统计数据
 */
export const getMerchantStats = () => {
  return request({
    url: '/merchant/stats',
    method: 'get'
  })
}

/**
 * 获取商家商品列表
 * @param {Object} params 查询参数
 * @param {number} params.page 页码
 * @param {number} params.size 每页大小
 * @param {string} params.keyword 搜索关键词
 * @param {string} params.status 商品状态
 * @param {string} params.category 商品类别
 * @returns {Promise} 商品列表
 */
export const getMerchantProducts = (params) => {
  return request({
    url: '/merchant/products',
    method: 'get',
    params
  })
}

/**
 * 获取商品详情
 * @param {number} productId 商品ID
 * @returns {Promise} 商品详情
 */
export const getMerchantProductDetail = (productId) => {
  return request({
    url: `/merchant/products/${productId}`,
    method: 'get'
  })
}

/**
 * 创建商品
 * @param {Object} data 商品数据
 * @returns {Promise} 创建结果
 */
export const createMerchantProduct = (data) => {
  return request({
    url: '/merchant/products',
    method: 'post',
    data
  })
}

/**
 * 更新商品
 * @param {number} productId 商品ID
 * @param {Object} data 商品数据
 * @returns {Promise} 更新结果
 */
export const updateMerchantProduct = (productId, data) => {
  return request({
    url: `/merchant/products/${productId}`,
    method: 'put',
    data
  })
}

/**
 * 删除商品
 * @param {number} productId 商品ID
 * @returns {Promise} 删除结果
 */
export const deleteMerchantProduct = (productId) => {
  return request({
    url: `/merchant/products/${productId}`,
    method: 'delete'
  })
}

/**
 * 更新商品状态
 * @param {number} productId 商品ID
 * @param {string} status 商品状态 on_sale/off_sale
 * @returns {Promise} 更新结果
 */
export const updateProductStatus = (productId, status) => {
  return request({
    url: `/merchant/products/${productId}/status`,
    method: 'put',
    data: { status }
  })
}

/**
 * 批量上架商品
 * @param {Array} productIds 商品ID列表
 * @returns {Promise} 操作结果
 */
export const batchOnSaleProducts = (productIds) => {
  return request({
    url: '/merchant/products/batch/on-sale',
    method: 'put',
    data: { productIds }
  })
}

/**
 * 批量下架商品
 * @param {Array} productIds 商品ID列表
 * @returns {Promise} 操作结果
 */
export const batchOffSaleProducts = (productIds) => {
  return request({
    url: '/merchant/products/batch/off-sale',
    method: 'put',
    data: { productIds }
  })
}

/**
 * 获取商家订单列表
 * @param {Object} params 查询参数
 * @param {number} params.page 页码
 * @param {number} params.size 每页大小
 * @param {string} params.keyword 搜索关键词
 * @param {string} params.status 订单状态
 * @param {Array} params.dateRange 日期范围
 * @returns {Promise} 订单列表
 */
export const getMerchantOrders = (params) => {
  return request({
    url: '/merchant/orders',
    method: 'get',
    params
  })
}

/**
 * 获取订单详情
 * @param {number} orderId 订单ID
 * @returns {Promise} 订单详情
 */
export const getMerchantOrderDetail = (orderId) => {
  return request({
    url: `/merchant/orders/${orderId}`,
    method: 'get'
  })
}

/**
 * 发货
 * @param {number} orderId 订单ID
 * @param {Object} data 发货数据
 * @param {string} data.expressCompany 快递公司
 * @param {string} data.trackingNumber 快递单号
 * @returns {Promise} 发货结果
 */
export const shipOrder = (orderId, data) => {
  return request({
    url: `/merchant/orders/${orderId}/ship`,
    method: 'put',
    data
  })
}

/**
 * 确认订单
 * @param {number} orderId 订单ID
 * @returns {Promise} 确认结果
 */
export const confirmOrder = (orderId) => {
  return request({
    url: `/merchant/orders/${orderId}/confirm`,
    method: 'put'
  })
}

/**
 * 取消订单
 * @param {number} orderId 订单ID
 * @param {string} reason 取消原因
 * @returns {Promise} 取消结果
 */
export const cancelOrder = (orderId, reason) => {
  return request({
    url: `/merchant/orders/${orderId}/cancel`,
    method: 'put',
    data: { reason }
  })
}

/**
 * 获取销售统计数据
 * @param {Object} params 查询参数
 * @param {string} params.period 统计周期 day/week/month/year
 * @param {string} params.startDate 开始日期
 * @param {string} params.endDate 结束日期
 * @returns {Promise} 统计数据
 */
export const getMerchantSalesStats = (params) => {
  return request({
    url: '/merchant/stats/sales',
    method: 'get',
    params
  })
}

/**
 * 获取订单统计数据
 * @param {Object} params 查询参数
 * @returns {Promise} 统计数据
 */
export const getMerchantOrderStats = (params) => {
  return request({
    url: '/merchant/stats/orders',
    method: 'get',
    params
  })
}

/**
 * 获取商品统计数据
 * @param {Object} params 查询参数
 * @returns {Promise} 统计数据
 */
export const getMerchantProductStats = (params) => {
  return request({
    url: '/merchant/stats/products',
    method: 'get',
    params
  })
}

/**
 * 获取客户统计数据
 * @param {Object} params 查询参数
 * @returns {Promise} 统计数据
 */
export const getMerchantCustomerStats = (params) => {
  return request({
    url: '/merchant/stats/customers',
    method: 'get',
    params
  })
}

/**
 * 获取财务统计数据
 * @param {Object} params 查询参数
 * @returns {Promise} 统计数据
 */
export const getMerchantFinanceStats = (params) => {
  return request({
    url: '/merchant/stats/finance',
    method: 'get',
    params
  })
}

/**
 * 上传商品图片
 * @param {File} file 图片文件
 * @returns {Promise} 上传结果
 */
export const uploadProductImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/merchant/upload/product-image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传商家资质文件
 * @param {File} file 文件
 * @returns {Promise} 上传结果
 */
export const uploadQualificationFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/merchant/upload/qualification',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}