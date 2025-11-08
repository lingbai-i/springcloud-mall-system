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
    url: '/user-service/auth/me',
    method: 'get'
  })
}

// 更新用户资料
export function updateUserProfile(data) {
  return request({
    url: '/user-service/user/profile',
    method: 'put',
    data
  })
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/user-service/user/password',
    method: 'put',
    data
  })
}

// 上传头像
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/user-service/user/avatar',
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
    url: '/user-service/user/checkUsernameUnique',
    method: 'get',
    params: { username }
  })
}

// 检查手机号唯一性
export function checkPhoneUnique(phone) {
  return request({
    url: '/user-service/user/checkPhoneUnique',
    method: 'get',
    params: { phone }
  })
}

// 检查邮箱唯一性
export function checkEmailUnique(email) {
  return request({
    url: '/user-service/user/checkEmailUnique',
    method: 'get',
    params: { email }
  })
}

// 获取用户统计信息
export function getUserStats() {
  return request({
    url: '/user-service/user/stats',
    method: 'get'
  })
}

// 获取用户操作日志
export function getUserLogs(params) {
  return request({
    url: '/user-service/user/logs',
    method: 'get',
    params
  })
}

// 绑定手机号
export function bindPhone(data) {
  return request({
    url: '/user-service/user/bind/phone',
    method: 'post',
    data
  })
}

// 绑定邮箱
export function bindEmail(data) {
  return request({
    url: '/user-service/user/bind/email',
    method: 'post',
    data
  })
}

// 解绑手机号
export function unbindPhone(data) {
  return request({
    url: '/user-service/user/unbind/phone',
    method: 'post',
    data
  })
}

// 解绑邮箱
export function unbindEmail(data) {
  return request({
    url: '/user-service/user/unbind/email',
    method: 'post',
    data
  })
}

// 注销账号
export function deleteAccount(data) {
  return request({
    url: '/user-service/user/delete',
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