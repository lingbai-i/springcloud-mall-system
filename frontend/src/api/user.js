import request from '@/utils/request'

/**
 * 用户信息管理相关API
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-10-21
 */

// 获取用户详细信息
export function getUserProfile() {
  return request({
    url: '/users/profile',
    method: 'get'
  })
}

// 更新用户资料
export function updateUserProfile(data) {
  return request({
    url: '/users/profile',
    method: 'put',
    data
  })
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/users/change-password',
    method: 'put',
    data
  })
}

// 上传头像
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/users/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 检查用户名唯一性
export function checkUsernameUnique(username) {
  return request({
    url: '/users/check-username',
    method: 'get',
    params: { username }
  })
}

// 检查手机号唯一性
export function checkPhoneUnique(phone) {
  return request({
    url: '/users/check-phone',
    method: 'get',
    params: { phone }
  })
}

// 检查邮箱唯一性
export function checkEmailUnique(email) {
  return request({
    url: '/users/check-email',
    method: 'get',
    params: { email }
  })
}

// 获取用户统计信息
export function getUserStats() {
  return request({
    url: '/users/stats',
    method: 'get'
  })
}

// 获取用户操作日志
export function getUserLogs(params) {
  return request({
    url: '/users/logs',
    method: 'get',
    params
  })
}

// 绑定手机号
export function bindPhone(data) {
  return request({
    url: '/users/bind/phone',
    method: 'post',
    data
  })
}

// 绑定邮箱
export function bindEmail(data) {
  return request({
    url: '/users/bind/email',
    method: 'post',
    data
  })
}

// 解绑手机号
export function unbindPhone(data) {
  return request({
    url: '/users/unbind/phone',
    method: 'post',
    data
  })
}

// 解绑邮箱
export function unbindEmail(data) {
  return request({
    url: '/users/unbind/email',
    method: 'post',
    data
  })
}

// 注销账号
export function deleteAccount(data) {
  return request({
    url: '/users/delete',
    method: 'post',
    data
  })
}

// 导出userApi对象，包含所有用户相关API
export const userApi = {
  getUserProfile,
  updateUserProfile,
  changePassword,
  uploadAvatar,
  checkUsernameUnique,
  checkPhoneUnique,
  checkEmailUnique,
  getUserStats,
  getUserLogs,
  bindPhone,
  bindEmail,
  unbindPhone,
  unbindEmail,
  deleteAccount,
  // 兼容性方法
  getUserInfo: getUserProfile,
  updateProfile: updateUserProfile
}