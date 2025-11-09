/**
 * V1.1 2025-11-08T08:52:37+08:00 修改日志：
 * - 启用 withCredentials 支持基于 Cookie 的会话与 CSRF 防护
 * - 对 POST/PUT/PATCH/DELETE 注入 CSRF 令牌头（X-CSRF-TOKEN / X-XSRF-TOKEN）
 * - 统一日志接口替换为轻量封装（后续可替换为 Winston）
 * @author lingbai
 * @description Axios 请求/响应拦截器与安全策略统一配置
 */
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'
// 轻量日志封装：前端环境无 Winston，使用 console 模拟多级日志
// 若后续接入 Winston 前端版本，可在此替换实现
import * as logger from '@/utils/logger'

/**
 * Axios 实例创建
 * 说明：
 * - 启用 withCredentials 以支持后端基于 Cookie 的会话/CSRF 防护
 * - 统一 JSON Content-Type
 * @author lingbai
 * @version 1.1 2025-11-08T08:46:54+08:00：增加 CSRF 头注入与统一日志
 */
const service = axios.create({
  baseURL: '/api',  // 使用Vite代理，会被转发到http://localhost:8082
  timeout: 15000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    logger.debug('[HTTP] 请求拦截开始', { url: config.url, method: config.method })
    
    // 定义不需要token的路径
    const publicPaths = [
      '/users/register',
      '/users/login',
      '/sms/send'
    ]
    
    // 检查当前请求是否为公开路径
    const isPublicPath = publicPaths.some(path => config.url?.includes(path))
    
    // 只在非公开路径且有token时添加Authorization头
    if (!isPublicPath && userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    
    // CSRF 防护：对变更类请求方法注入 CSRF 令牌头（后端建议名：X-CSRF-TOKEN 或 X-XSRF-TOKEN）
    const unsafeMethods = ['post', 'put', 'patch', 'delete']
    if (config.method && unsafeMethods.includes(config.method.toLowerCase())) {
      const csrfToken = getCsrfTokenFromCookie()
      if (csrfToken) {
        // 同步两种常见头名称，提升后端兼容性
        config.headers['X-CSRF-TOKEN'] = csrfToken
        config.headers['X-XSRF-TOKEN'] = csrfToken
        logger.debug('[HTTP] 注入 CSRF 令牌头', { url: config.url })
      }
      // 注意：当前CSRF功能未在后端启用，无需警告
    }

    // 添加请求ID用于追踪
    config.headers['X-Request-ID'] = generateRequestId()
    logger.info('[HTTP] 已添加请求ID与认证头（如适用）', { url: config.url })
    
    return config
  },
  error => {
    logger.error('Request error', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const { data } = response
    logger.debug('[HTTP] 响应收到', { url: response.config?.url, status: response.status })
    
    // 如果是文件下载，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    
    // 统一处理响应
    // 处理标准格式 {code: 200, data: ...}
    if (data.code === 200) {
      logger.info('[HTTP] 标准响应成功 code=200', { url: response.config?.url })
      return data
    } else if (data.code === 401) {
      // token过期或无效
      handleTokenExpired()
      return Promise.reject(new Error(data.message || '登录已过期'))
    } 
    // 处理后端success格式 {success: true/false, message: ...}
    else if (data.hasOwnProperty('success')) {
      if (data.success === true) {
        logger.info('[HTTP] success=true 响应成功', { url: response.config?.url })
        return data
      } else {
        // success为false时显示错误消息
        ElMessage.error(data.message || '请求失败')
        return Promise.reject(new Error(data.message || '请求失败'))
      }
    } 
    // 处理特殊格式：{code: undefined, message: '成功消息'}
    else if (data.message && typeof data.message === 'string') {
      const successKeywords = ['成功', '完成', 'success', 'Success', 'SUCCESS']
      const isSuccess = successKeywords.some(keyword => data.message.includes(keyword))
      
      if (isSuccess) {
        // 将成功消息转换为标准格式
        logger.info('[HTTP] 非标准成功消息转换为标准格式', { url: response.config?.url })
        return {
          code: 200,
          message: data.message,
          data: data.data || null,
          success: true
        }
      } else {
        // 包含错误消息
        ElMessage.error(data.message || '请求失败')
        return Promise.reject(new Error(data.message || '请求失败'))
      }
    }
    else {
      // 其他错误
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message || '请求失败'))
    }
  },
  error => {
    logger.error('Response error', error)
    
    if (error.response) {
      const { status, data, config } = error.response
      
      // 定义非关键API列表（失败时不弹出“登录过期”提示）
      const nonCriticalPaths = [
        '/cart-service',  // 购物车服务
        '/product-service',  // 商品服务
        '/order-service'  // 订单服务
      ]
      
      const isNonCriticalApi = nonCriticalPaths.some(path => 
        config?.url?.includes(path)
      )
      
      switch (status) {
        case 400:
          ElMessage.error(data.message || '请求参数错误')
          break
        case 401:
          // 只对关键API（如用户服务）弹出登录过期提示
          if (!isNonCriticalApi) {
            handleTokenExpired()
          } else {
            logger.debug('非关键API返回401，静默处理', { url: config?.url })
          }
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        case 502:
          ElMessage.error('网关错误')
          break
        case 503:
          ElMessage.error('服务不可用')
          break
        case 504:
          ElMessage.error('网关超时')
          break
        default:
          ElMessage.error(data.message || `请求失败 (${status})`)
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试')
    } else if (error.message === 'Network Error') {
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      ElMessage.error(error.message || '请求失败')
    }
    
    return Promise.reject(error)
  }
)

// 处理token过期
function handleTokenExpired() {
  const userStore = useUserStore()
  
  ElMessageBox.confirm(
    '登录状态已过期，请重新登录',
    '系统提示',
    {
      confirmButtonText: '重新登录',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    userStore.userLogout()
    router.push('/auth/login')
  }).catch(() => {
    // 用户取消，不做处理
  })
}

// 生成请求ID
function generateRequestId() {
  return Date.now().toString(36) + Math.random().toString(36).substr(2)
}

/**
 * 从 Cookie 中提取 CSRF 令牌
 * 支持常见命名：XSRF-TOKEN / csrfToken / _csrf
 * @return {string|undefined} CSRF 令牌值
 * @author lingbai
 * @version 1.0 2025-11-08T08:46:54+08:00：新增
 */
function getCsrfTokenFromCookie() {
  try {
    const cookie = document.cookie || ''
    const map = Object.fromEntries(
      cookie.split(';')
        .map(kv => kv.trim())
        .filter(Boolean)
        .map(kv => {
          const idx = kv.indexOf('=')
          const key = idx > -1 ? kv.slice(0, idx) : kv
          const val = idx > -1 ? decodeURIComponent(kv.slice(idx + 1)) : ''
          return [key, val]
        })
    )
    return map['XSRF-TOKEN'] || map['csrfToken'] || map['_csrf']
  } catch (e) {
    logger.warn('解析 CSRF Cookie 失败', e)
    return undefined
  }
}

// 上传文件
export function uploadFile(file, onProgress) {
  const formData = new FormData()
  formData.append('file', file)
  
  return service({
    url: '/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress) {
        const percentCompleted = Math.round(
          (progressEvent.loaded * 100) / progressEvent.total
        )
        onProgress(percentCompleted)
      }
    }
  })
}

// 下载文件
export function downloadFile(url, filename) {
  return service({
    url,
    method: 'get',
    responseType: 'blob'
  }).then(response => {
    const blob = new Blob([response.data])
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = filename || 'download'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(downloadUrl)
  })
}

export default service
