/**
 * 商家相关API
 * 
 * @author system
 * @since 2025-11-11
 */
import request from '@/utils/request'

/**
 * 商家登录
 */
export function merchantLogin(data) {
  return request({
    url: '/merchant/login',
    method: 'post',
    params: data  // 后端使用 @RequestParam ，所以用 params
  })
}

/**
 * 提交商家入驻申请
 */
export function submitMerchantApplication(data) {
  return request({
    url: '/merchants/apply',
    method: 'post',
    data
  })
}

/**
 * 查询申请详情
 */
export function getApplicationDetail(id) {
  return request({
    url: `/merchants/applications/${id}`,
    method: 'get'
  })
}

/**
 * 获取申请统计
 */
export function getApplicationStats() {
  return request({
    url: '/merchants/applications/stats',
    method: 'get'
  })
}

/**
 * 管理员获取申请列表
 */
export function getApplicationList(params) {
  return request({
    url: '/admin/merchants/applications',
    method: 'get',
    params
  })
}

/**
 * 管理员审批申请
 */
export function approveApplication(id, data) {
  return request({
    url: `/admin/merchants/applications/${id}/approve`,
    method: 'put',
    data
  })
}
