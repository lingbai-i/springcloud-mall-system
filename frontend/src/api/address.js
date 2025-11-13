import request from '@/utils/request'

/**
 * 收货地址相关API
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */

// 获取收货地址列表
export function getAddressList() {
  return request({
    url: '/user-service/addresses',
    method: 'get'
  })
}

// 获取地址详情
export function getAddressDetail(id) {
  return request({
    url: `/user-service/addresses/${id}`,
    method: 'get'
  })
}

// 添加收货地址
export function addAddress(data) {
  return request({
    url: '/user-service/addresses',
    method: 'post',
    data
  })
}

// 更新收货地址
export function updateAddress(id, data) {
  return request({
    url: `/user-service/addresses/${id}`,
    method: 'put',
    data
  })
}

// 删除收货地址
export function deleteAddress(id) {
  return request({
    url: `/user-service/addresses/${id}`,
    method: 'delete'
  })
}

// 设置默认地址
export function setDefaultAddress(id) {
  return request({
    url: `/user-service/addresses/${id}/default`,
    method: 'put'
  })
}

// 导出addressApi对象
export const addressApi = {
  getAddressList,
  getAddressDetail,
  addAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress
}

export default addressApi
