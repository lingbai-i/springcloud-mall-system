import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { register } from '@/api/auth'
import { getUserProfile } from '@/api/user'
import * as logger from '@/utils/logger'

/**
 * 用户状态管理
 * 管理用户登录状态、用户信息等
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-10-21
 */
export const useUserStore = defineStore('user', () => {
  // 状态 - 优先从持久化存储读取，兼容旧版本的直接 localStorage 存储
  const token = ref(localStorage.getItem('token') || '')
  const userInfoStr = localStorage.getItem('userInfo')
  const userInfo = ref(userInfoStr ? JSON.parse(userInfoStr) : {})
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value.username || '')
  const avatar = computed(() => userInfo.value.avatar || '')
  const userId = computed(() => userInfo.value.id || userInfo.value.userId || null)
  const isAdmin = computed(() => 
    userInfo.value.role === 'admin' || 
    userInfo.value.username === 'admin' ||
    userInfo.value.isAdmin === true
  )
  const isMerchant = computed(() => 
    userInfo.value.role === 'merchant' ||
    userInfo.value.isMerchant === true
  )
  
  /**
   * 登录
   * @param {Object} loginData - 登录数据
   */
  const login = async (loginData) => {
    try {
      // 调用auth API
      const { login } = await import('@/api/auth')
      const result = await login(loginData)
      
      // 后端返回格式: {success: true, data: {accessToken, expiresIn, userInfo}}
      if (!result.success && result.code !== 200) {
        throw { code: result.code, message: result.message }
      }
      
      // 从data中提取token和userInfo
      const { accessToken, userInfo: userData } = result.data
      
      token.value = accessToken
      userInfo.value = userData
      
      // 持久化存储
      localStorage.setItem('token', accessToken)
      localStorage.setItem('userInfo', JSON.stringify(userData))
      
      return result.data
    } catch (error) {
      throw error
    }
  }
  
  /**
   * 用户注册
   * 注册成功后自动建立用户会话，实现无缝登录体验
   * 
   * @param {Object} registerData - 注册数据
   * @returns {Promise<boolean>} 注册结果
   * @author lingbai
   * @version 1.1 2025-01-27：修改以支持注册后自动登录功能
   */
  const userRegister = async (registerData) => {
    try {
      const response = await register(registerData)
      
      // 检查注册是否成功
      const isSuccess = response.code === 200 || 
                       response.success === true || 
                       (response.message && response.message.includes('成功'))
      
      if (!isSuccess) {
        throw { code: response.code, message: response.message }
      }
      
      // 注册成功后，检查是否返回了登录信息（新的响应格式）
      if (response.data && response.data.token && response.data.userInfo) {
        // 自动建立用户会话
        token.value = response.data.token
        userInfo.value = response.data.userInfo
        
        // 持久化存储到本地
        localStorage.setItem('token', response.data.token)
        localStorage.setItem('userInfo', JSON.stringify(response.data.userInfo))
        
        console.log('注册成功并自动登录，用户会话已建立:', response.data.userInfo.username)
      } else {
        console.log('注册成功，但未返回登录信息，需要手动登录')
      }
      
      return true
    } catch (error) {
      console.error('用户注册失败:', error)
      throw error
    }
  }

  /**
   * 退出登录
   */
  const logout = () => {
    token.value = ''
    userInfo.value = {}
    
    // 清除本地存储
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }
  
  // logout的别名，与前端其他部分保持兼容
  const userLogout = logout
  
  /**
   * 更新用户信息
   * @param {Object} newUserInfo - 新的用户信息
   */
  const updateUserInfo = (newUserInfo) => {
    userInfo.value = { ...userInfo.value, ...newUserInfo }
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  /**
   * 获取并刷新当前登录用户信息
   *
   * 修改日志：
   * V1.1 2025-11-09T20:51:07+08:00：新增 fetchUserInfo 方法，统一从后端拉取用户信息并写入本地存储；加入结构化日志与异常分支，避免会话不同步导致的 UI 偏差。
   * @author lingbai
   * @returns {Promise<Object>} 最新的用户信息对象
   * @throws {Error} 当服务端返回异常或会话无效时抛出错误
   */
  const fetchUserInfo = async () => {
    // 防御性：无 token 不发起网络请求，避免无效后端调用
    if (!token.value) {
      logger.warn('fetchUserInfo 被调用，但当前不存在有效 token，已跳过网络请求')
      return userInfo.value
    }
    try {
      logger.info('开始拉取后端用户信息 /users/profile')
      const resp = await getUserProfile()
      // 后端约定：{ success: boolean, data: UserInfoResponse }
      if (resp && (resp.success === true || resp.code === 200) && resp.data) {
        userInfo.value = { ...(userInfo.value || {}), ...(resp.data || {}) }
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        logger.info('用户信息拉取成功并写入本地存储')
        return userInfo.value
      }
      const message = (resp && resp.message) || '获取用户信息失败'
      logger.error('后端返回失败，无法刷新用户信息', { message })
      throw new Error(message)
    } catch (error) {
      // 错误分支：可能为 401、网络异常或后端错误
      logger.error('拉取用户信息过程中出现异常', error)
      throw error
    }
  }
  
  /**
   * 初始化用户状态
   * 从本地存储恢复用户状态
   */
  const initUserState = () => {
    const savedToken = localStorage.getItem('token')
    const savedUserInfo = localStorage.getItem('userInfo')
    
    if (savedToken) {
      token.value = savedToken
      console.log('从 localStorage 恢复 token:', savedToken.substring(0, 20) + '...')
    }
    
    if (savedUserInfo) {
      try {
        userInfo.value = JSON.parse(savedUserInfo)
        console.log('从 localStorage 恢复用户信息:', userInfo.value.username)
      } catch (error) {
        console.error('解析用户信息失败:', error)
        userInfo.value = {}
      }
    }
    
    // 打印当前状态用于调试
    console.log('用户状态初始化完成:', { 
      hasToken: !!token.value, 
      username: userInfo.value.username,
      isLoggedIn: isLoggedIn.value 
    })
  }
  
  return {
    // 状态
    token,
    userInfo,
    
    // 计算属性
    isLoggedIn,
    username,
    avatar,
    userId,
    isAdmin,
    isMerchant,
    
    // 方法
    login,
    userRegister,
    logout,
    userLogout,
    updateUserInfo,
    fetchUserInfo,
    initUserState
  }
}, {
  persist: {
    key: 'user-store',
    storage: localStorage,
    paths: ['token', 'userInfo']
  }
})
