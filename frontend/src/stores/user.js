import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { register } from '@/api/auth'
import { getUserProfile } from '@/api/user'
import * as logger from '@/utils/logger'

/**
 * 用户状态管理
 * 管理用户登录状态、用户信息等
 * 
 * 注意：此 store 使用 Pinia persist 插件进行持久化
 * 持久化 key 为 'user-store'，包含 token 和 userInfo
 * 同时为了兼容性，也会手动同步到 'token'、'userInfo'、'merchantId' 等独立 key
 * 
 * @author lingbai
 * @version 1.1
 * @since 2025-10-21
 */
export const useUserStore = defineStore('user', () => {
  // 状态 - Pinia persist 会自动从 'user-store' key 恢复
  // 初始值设为空，让 Pinia persist 处理恢复逻辑
  const token = ref('')
  const userInfo = ref({})
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value.username || '')
  const avatar = computed(() => userInfo.value.avatar || '')
  const userId = computed(() => userInfo.value.id || userInfo.value.userId || null)
  const merchantId = computed(() => userInfo.value.merchantId || null)
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
      let result
      
      // 根据登录类型调用不同API
      if (loginData.type === 'merchant') {
        // 商家登录
        const { merchantLogin } = await import('@/api/merchant')
        result = await merchantLogin({
          username: loginData.username,
          password: loginData.password
        })
      } else if (loginData.type === 'admin') {
        // 管理员登录
        const { adminLogin } = await import('@/api/admin')
        result = await adminLogin(loginData)
      } else {
        // 普通用户登录
        const { login: userLogin } = await import('@/api/auth')
        result = await userLogin(loginData)
      }
      
      // 后端返回格式: {code: 200, success: true, message: '', data: {accessToken/token, userInfo, ...}}
      if (!result.success || (result.code !== 200 && result.code !== undefined)) {
        throw { code: result.code, message: result.message || '登录失败' }
      }
      
      // 从 data 中提取 token 和 userInfo
      // 兼容不同的字段名：accessToken/token，adminInfo/userInfo
      const tokenValue = result.data.accessToken || result.data.token
      const userData = result.data.adminInfo || result.data.userInfo || result.data
      
      // 如果是商家登录，确保 userInfo 中包含商家标识
      if (loginData.type === 'merchant') {
        userData.role = userData.role || 'merchant'
        userData.isMerchant = true
        // 确保merchantId被保存（可能在不同字段中）
        const merchantIdValue = userData.merchantId || userData.id
        console.log('商家登录 - merchantId:', merchantIdValue, 'userData:', userData)
        if (merchantIdValue) {
          userData.merchantId = merchantIdValue
          // 同时保存到localStorage以便直接访问
          localStorage.setItem('merchantId', String(merchantIdValue))
        }
      } else if (loginData.type === 'admin') {
        userData.role = userData.role || 'admin'
        userData.isAdmin = true
      }
      
      // 如果是普通用户登录，保存userId
      if (userData.id) {
        localStorage.setItem('userId', String(userData.id))
      }
      
      token.value = tokenValue
      userInfo.value = userData
      
      // 持久化存储
      localStorage.setItem('token', tokenValue)
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
    
    // 清除本地存储 - 包括所有独立的 key
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('merchantId')
    localStorage.removeItem('userId')
    // 同时清除 Pinia persist 的 key
    localStorage.removeItem('user-store')
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
   * V1.2 2025-12-25：根据用户角色调用不同的接口，避免管理员/商家信息被普通用户信息覆盖
   * V1.3 2025-12-28：增强商家角色判断逻辑，同时检查 localStorage 中的 merchantId，避免页面刷新后商家信息被覆盖
   * V1.4 2025-12-28：修复普通用户首页刷新后显示手机号的问题，增加 forceRefresh 参数支持强制刷新
   * @author lingbai
   * @param {boolean} forceRefresh - 是否强制刷新，忽略角色检查
   * @returns {Promise<Object>} 最新的用户信息对象
   * @throws {Error} 当服务端返回异常或会话无效时抛出错误
   */
  const fetchUserInfo = async (forceRefresh = false) => {
    // 防御性：无 token 不发起网络请求，避免无效后端调用
    if (!token.value) {
      logger.warn('fetchUserInfo 被调用，但当前不存在有效 token，已跳过网络请求')
      return userInfo.value
    }
    
    // 如果是管理员，调用管理员信息接口（除非强制刷新）
    if (!forceRefresh && (userInfo.value.isAdmin || userInfo.value.role === 'admin')) {
      logger.info('当前用户是管理员，跳过普通用户信息刷新')
      return userInfo.value
    }
    
    // 如果是商家，调用商家信息接口（除非强制刷新）
    // 增强判断：同时检查 localStorage 中的 merchantId，避免页面刷新后状态丢失
    // 注意：只有当 userInfo 中明确标记为商家时才跳过，避免普通用户首页被误判
    if (!forceRefresh && (userInfo.value.isMerchant === true || userInfo.value.role === 'merchant')) {
      const savedMerchantId = localStorage.getItem('merchantId')
      logger.info('当前用户是商家，跳过普通用户信息刷新', { 
        isMerchant: userInfo.value.isMerchant, 
        role: userInfo.value.role,
        savedMerchantId 
      })
      // 如果 userInfo 中缺少商家标识，从 localStorage 恢复
      if (savedMerchantId && !userInfo.value.merchantId) {
        userInfo.value = { 
          ...userInfo.value, 
          merchantId: parseInt(savedMerchantId, 10),
          isMerchant: true,
          role: 'merchant'
        }
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        logger.info('已从 localStorage 恢复商家标识')
      }
      return userInfo.value
    }
    
    try {
      logger.info('开始拉取后端用户信息 /users/profile')
      const resp = await getUserProfile()
      // 后端约定：{ success: boolean, data: UserInfoResponse }
      if (resp && (resp.success === true || resp.code === 200) && resp.data) {
        // 保留原有的角色信息，只更新用户基本信息
        const preservedFields = {
          isMerchant: userInfo.value.isMerchant,
          isAdmin: userInfo.value.isAdmin,
          role: userInfo.value.role,
          merchantId: userInfo.value.merchantId,
          shopName: userInfo.value.shopName,
          logo: userInfo.value.logo
        }
        
        // 合并新数据，但保留角色相关字段
        userInfo.value = { 
          ...(userInfo.value || {}), 
          ...(resp.data || {}),
          // 如果原来有角色信息，保留它们
          ...(preservedFields.isMerchant !== undefined ? { isMerchant: preservedFields.isMerchant } : {}),
          ...(preservedFields.isAdmin !== undefined ? { isAdmin: preservedFields.isAdmin } : {}),
          ...(preservedFields.role ? { role: preservedFields.role } : {}),
          ...(preservedFields.merchantId ? { merchantId: preservedFields.merchantId } : {}),
          ...(preservedFields.shopName ? { shopName: preservedFields.shopName } : {}),
          ...(preservedFields.logo ? { logo: preservedFields.logo } : {})
        }
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        logger.info('用户信息拉取成功并写入本地存储', { nickname: userInfo.value.nickname, username: userInfo.value.username })
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
   * 注意：Pinia persist 插件会自动从 'user-store' key 恢复状态
   * 此方法主要用于同步独立的 localStorage keys 到 Pinia 状态
   */
  const initUserState = () => {
    // Pinia persist 已经自动恢复了 token 和 userInfo
    // 这里只需要确保独立的 localStorage keys 与 Pinia 状态同步
    
    // 如果 Pinia 状态为空，尝试从独立 keys 恢复
    if (!token.value) {
      const savedToken = localStorage.getItem('token')
      if (savedToken) {
        token.value = savedToken
        console.log('从独立 localStorage key 恢复 token')
      }
    }
    
    if (!userInfo.value || Object.keys(userInfo.value).length === 0) {
      const savedUserInfo = localStorage.getItem('userInfo')
      if (savedUserInfo) {
        try {
          userInfo.value = JSON.parse(savedUserInfo)
          console.log('从独立 localStorage key 恢复 userInfo')
        } catch (error) {
          console.error('解析 userInfo 失败:', error)
        }
      }
    }
    
    // 如果 userInfo 中没有 merchantId，尝试从独立 key 恢复
    // 同时确保商家标识 (isMerchant, role) 也被正确设置
    const savedMerchantId = localStorage.getItem('merchantId')
    if (savedMerchantId) {
      const merchantIdNum = parseInt(savedMerchantId, 10)
      if (userInfo.value) {
        // 确保 merchantId 和商家标识都被正确设置
        if (!userInfo.value.merchantId || !userInfo.value.isMerchant) {
          userInfo.value = { 
            ...userInfo.value, 
            merchantId: merchantIdNum,
            isMerchant: true,
            role: userInfo.value.role || 'merchant'
          }
          console.log('从独立 localStorage key 恢复 merchantId 和商家标识:', savedMerchantId)
        }
      }
    }
    
    // 同步 Pinia 状态到独立 keys（确保一致性）
    if (token.value) {
      localStorage.setItem('token', token.value)
    }
    if (userInfo.value && Object.keys(userInfo.value).length > 0) {
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      if (userInfo.value.merchantId) {
        localStorage.setItem('merchantId', String(userInfo.value.merchantId))
      }
      if (userInfo.value.id) {
        localStorage.setItem('userId', String(userInfo.value.id))
      }
    }
    
    // 打印当前状态用于调试
    console.log('用户状态初始化完成:', { 
      hasToken: !!token.value, 
      username: userInfo.value?.username,
      shopName: userInfo.value?.shopName,
      merchantId: userInfo.value?.merchantId,
      isLoggedIn: isLoggedIn.value,
      isMerchant: isMerchant.value,
      role: userInfo.value?.role
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
    merchantId,
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
