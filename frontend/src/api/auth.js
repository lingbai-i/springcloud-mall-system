/**
 * @author lingbai
 * @description 认证与短信相关 API 封装
 * 修改日志：
 * V1.1 2025-11-08T09:13:29+08:00：增强 sendSmsCode 支持对象/位置参数两种调用方式，并加入结构化日志，避免参数形状错误导致服务端解析失败。
 */
import request from '@/utils/request'
import * as logger from '@/utils/logger'

// 用户注册
export const register = (data) => {
  return request({
    url: '/user-service/auth/register',
    method: 'post',
    data
  })
}

// 用户登录
export const login = (data) => {
  return request({
    url: '/user-service/auth/login',
    method: 'post',
    data
  })
}

// 刷新token
export const refreshToken = (data) => {
  return request({
    url: '/user-service/auth/refresh',
    method: 'post',
    data
  })
}

// 验证token
export const verifyToken = (token) => {
  return request({
    url: '/user-service/auth/validate',
    method: 'get',
    headers: { Authorization: `Bearer ${token}` }
  })
}

// 用户登出
export const logout = () => {
  return request({
    url: '/user-service/auth/logout',
    method: 'post'
  })
}

// 发送验证码
/**
 * 发送短信验证码
 * 设计说明：
 * - 兼容两种入参：
 *   1) 位置参数：`sendSmsCode('13800000000', 'LOGIN')`
 *   2) 对象参数：`sendSmsCode({ phoneNumber: '13800000000', purpose: 'LOGIN' })`
 * - 统一结构化日志，便于与网络层拦截器日志关联追踪。
 * @param {string|{phoneNumber:string,purpose?:string}} arg1 - 位置或对象形式的手机号与用途
 * @param {string} [purpose='LOGIN'] - 验证码用途（位置参数形式时生效），如 LOGIN/REGISTER
 * @return {Promise<any>} Axios 响应 Promise
 * @author lingbai
 * @version 1.1 2025-11-08T09:13:29+08:00：新增对象参数兼容与日志
 */
export function sendSmsCode(arg1, purpose = 'LOGIN') {
  // 构造请求负载，兼容对象/位置参数两种调用方式
  const payload = typeof arg1 === 'string'
    ? { phoneNumber: arg1, purpose: purpose || 'LOGIN' }
    : { phoneNumber: arg1?.phoneNumber, purpose: arg1?.purpose || 'LOGIN' }

  // 简单防御性校验（前端亦有表单校验，这里仅兜底）
  if (!payload.phoneNumber || typeof payload.phoneNumber !== 'string') {
    const err = new Error('缺少或非法的手机号参数')
    logger.error('[AUTH] 发送短信验证码参数错误', err)
    return Promise.reject(err)
  }

  // 结构化日志记录调用意图
  logger.debug('[AUTH] 发送短信验证码请求', { purpose: payload.purpose, phoneMasked: payload.phoneNumber.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') })

  // 通过网关调用SMS服务
  return request({
    url: '/sms/send',
    method: 'post',
    data: payload
  })
}

// 验证码登录（未注册自动注册）
export function loginBySms(data) {
  return request({
    url: '/user-service/auth/sms-login',
    method: 'post',
    data
  })
}
