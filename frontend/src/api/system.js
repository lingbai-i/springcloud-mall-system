/**
 * 系统相关API
 * @author lingbai
 * @version 1.0
 * @since 2025-01-01
 */
import request from '@/utils/request'

/**
 * 获取服务器时间
 * 公开接口，无需认证
 * 
 * @returns {Promise} 服务器时间信息
 */
export function getServerTime() {
  return request({
    url: '/users/system/time',
    method: 'get'
  })
}

