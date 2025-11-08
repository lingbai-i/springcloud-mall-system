/**
 * 管理员相关API接口
 * @author lingbai
 * @date 2025-01-27
 */
import request from '@/utils/request'

/**
 * 管理员登录
 * @param {Object} data 登录数据
 * @param {string} data.username 用户名
 * @param {string} data.password 密码
 * @param {string} data.captcha 验证码
 * @param {boolean} data.remember 记住登录
 * @returns {Promise} 登录结果
 */
export const adminLogin = (data) => {
  return request({
    url: '/admin/auth/login',
    method: 'post',
    data
  })
}

/**
 * 管理员登出
 * @returns {Promise} 登出结果
 */
export const adminLogout = () => {
  return request({
    url: '/admin/auth/logout',
    method: 'post'
  })
}

/**
 * 获取管理员信息
 * @returns {Promise} 管理员信息
 */
export const getAdminInfo = () => {
  return request({
    url: '/admin/auth/info',
    method: 'get'
  })
}

/**
 * 获取系统统计数据
 * @returns {Promise} 统计数据
 */
export const getSystemStats = () => {
  return request({
    url: '/admin/system/stats',
    method: 'get'
  })
}

/**
 * 获取用户列表
 * @param {Object} params 查询参数
 * @param {number} params.page 页码
 * @param {number} params.size 每页大小
 * @param {string} params.keyword 搜索关键词
 * @param {string} params.status 用户状态
 * @returns {Promise} 用户列表
 */
export const getUserList = (params) => {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

/**
 * 获取用户详情
 * @param {number} userId 用户ID
 * @returns {Promise} 用户详情
 */
export const getUserDetail = (userId) => {
  return request({
    url: `/admin/users/${userId}`,
    method: 'get'
  })
}

/**
 * 禁用用户
 * @param {number} userId 用户ID
 * @returns {Promise} 操作结果
 */
export const disableUser = (userId) => {
  return request({
    url: `/admin/users/${userId}/disable`,
    method: 'put'
  })
}

/**
 * 启用用户
 * @param {number} userId 用户ID
 * @returns {Promise} 操作结果
 */
export const enableUser = (userId) => {
  return request({
    url: `/admin/users/${userId}/enable`,
    method: 'put'
  })
}

/**
 * 删除用户
 * @param {number} userId 用户ID
 * @returns {Promise} 操作结果
 */
export const deleteUser = (userId) => {
  return request({
    url: `/admin/users/${userId}`,
    method: 'delete'
  })
}

/**
 * 获取商家列表
 * @param {Object} params 查询参数
 * @param {number} params.page 页码
 * @param {number} params.size 每页大小
 * @param {string} params.keyword 搜索关键词
 * @param {string} params.status 商家状态
 * @param {string} params.category 商家类别
 * @param {Array} params.dateRange 日期范围
 * @returns {Promise} 商家列表
 */
export const getMerchantList = (params) => {
  return request({
    url: '/admin/merchants',
    method: 'get',
    params
  })
}

/**
 * 获取商家详情
 * @param {number} merchantId 商家ID
 * @returns {Promise} 商家详情
 */
export const getMerchantDetail = (merchantId) => {
  return request({
    url: `/admin/merchants/${merchantId}`,
    method: 'get'
  })
}

/**
 * 审核商家
 * @param {number} merchantId 商家ID
 * @param {Object} data 审核数据
 * @param {string} data.result 审核结果 approved/rejected
 * @param {string} data.reason 审核原因
 * @param {Array} data.permissions 权限列表
 * @returns {Promise} 操作结果
 */
export const approveMerchant = (merchantId, data) => {
  return request({
    url: `/admin/merchants/${merchantId}/approve`,
    method: 'put',
    data
  })
}

/**
 * 禁用商家
 * @param {number} merchantId 商家ID
 * @returns {Promise} 操作结果
 */
export const disableMerchant = (merchantId) => {
  return request({
    url: `/admin/merchants/${merchantId}/disable`,
    method: 'put'
  })
}

/**
 * 启用商家
 * @param {number} merchantId 商家ID
 * @returns {Promise} 操作结果
 */
export const enableMerchant = (merchantId) => {
  return request({
    url: `/admin/merchants/${merchantId}/enable`,
    method: 'put'
  })
}

/**
 * 删除商家
 * @param {number} merchantId 商家ID
 * @returns {Promise} 操作结果
 */
export const deleteMerchant = (merchantId) => {
  return request({
    url: `/admin/merchants/${merchantId}`,
    method: 'delete'
  })
}

/**
 * 导出商家数据
 * @param {Object} params 导出参数
 * @returns {Promise} 导出结果
 */
export const exportMerchants = (params) => {
  return request({
    url: '/admin/merchants/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

/**
 * 获取订单统计数据
 * @param {Object} params 查询参数
 * @param {string} params.period 统计周期 day/week/month/year
 * @param {string} params.startDate 开始日期
 * @param {string} params.endDate 结束日期
 * @returns {Promise} 统计数据
 */
export const getOrderStats = (params) => {
  return request({
    url: '/admin/orders/stats',
    method: 'get',
    params
  })
}

/**
 * 获取销售统计数据
 * @param {Object} params 查询参数
 * @returns {Promise} 统计数据
 */
export const getSalesStats = (params) => {
  return request({
    url: '/admin/sales/stats',
    method: 'get',
    params
  })
}

/**
 * 获取用户统计数据
 * @param {Object} params 查询参数
 * @returns {Promise} 统计数据
 */
export const getUserStats = (params) => {
  return request({
    url: '/admin/users/stats',
    method: 'get',
    params
  })
}

/**
 * 获取商品统计数据
 * @param {Object} params 查询参数
 * @returns {Promise} 统计数据
 */
export const getProductStats = (params) => {
  return request({
    url: '/admin/products/stats',
    method: 'get',
    params
  })
}

/**
 * 获取财务统计数据
 * @param {Object} params 查询参数
 * @returns {Promise} 统计数据
 */
export const getFinanceStats = (params) => {
  return request({
    url: '/admin/finance/stats',
    method: 'get',
    params
  })
}