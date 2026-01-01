import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { 
  addFavorite as addFavoriteApi, 
  removeFavorite as removeFavoriteApi,
  batchRemoveFavorites as batchRemoveFavoritesApi,
  getFavorites as getFavoritesApi,
  checkFavoriteStatus,
  getFavoriteCount as getFavoriteCountApi
} from '@/api/favorite'

/**
 * 收藏状态管理
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-12-30
 */
export const useFavoriteStore = defineStore('favorite', () => {
  // 收藏列表
  const favorites = ref([])
  // 收藏总数
  const favoriteCount = ref(0)
  // 已收藏的商品ID集合（用于快速检查）
  const favoriteIds = ref(new Set())
  // 分页信息
  const pagination = ref({
    current: 1,
    size: 10,
    total: 0,
    pages: 0
  })
  // 加载状态
  const loading = ref(false)

  /**
   * 检查商品是否已收藏
   */
  function isFavorited(productId) {
    return favoriteIds.value.has(productId)
  }

  /**
   * 添加收藏
   */
  async function addFavorite(productId) {
    try {
      const res = await addFavoriteApi(productId)
      if (res.success) {
        favoriteIds.value.add(productId)
        favoriteCount.value++
        return { success: true }
      }
      return { success: false, message: res.message }
    } catch (error) {
      console.error('添加收藏失败:', error)
      return { success: false, message: error.message || '添加收藏失败' }
    }
  }

  /**
   * 取消收藏
   */
  async function removeFavorite(productId) {
    try {
      const res = await removeFavoriteApi(productId)
      if (res.success) {
        favoriteIds.value.delete(productId)
        favoriteCount.value = Math.max(0, favoriteCount.value - 1)
        // 从列表中移除
        favorites.value = favorites.value.filter(item => item.productId !== productId)
        return { success: true }
      }
      return { success: false, message: res.message }
    } catch (error) {
      console.error('取消收藏失败:', error)
      return { success: false, message: error.message || '取消收藏失败' }
    }
  }

  /**
   * 批量取消收藏
   */
  async function batchRemoveFavorites(productIds) {
    try {
      const res = await batchRemoveFavoritesApi(productIds)
      if (res.success) {
        productIds.forEach(id => favoriteIds.value.delete(id))
        favoriteCount.value = Math.max(0, favoriteCount.value - productIds.length)
        // 从列表中移除
        favorites.value = favorites.value.filter(item => !productIds.includes(item.productId))
        return { success: true }
      }
      return { success: false, message: res.message }
    } catch (error) {
      console.error('批量取消收藏失败:', error)
      return { success: false, message: error.message || '批量取消收藏失败' }
    }
  }

  /**
   * 切换收藏状态
   */
  async function toggleFavorite(productId) {
    if (isFavorited(productId)) {
      return await removeFavorite(productId)
    } else {
      return await addFavorite(productId)
    }
  }

  /**
   * 加载收藏列表
   */
  async function loadFavorites(page = 1, size = 10) {
    loading.value = true
    try {
      const res = await getFavoritesApi({ page, size })
      if (res.success && res.data) {
        favorites.value = res.data.records || []
        pagination.value = {
          current: res.data.current || page,
          size: res.data.size || size,
          total: res.data.total || 0,
          pages: res.data.pages || 0
        }
        favoriteCount.value = res.data.total || 0
        // 更新收藏ID集合
        favoriteIds.value = new Set(favorites.value.map(item => item.productId))
        return { success: true, data: res.data }
      }
      return { success: false, message: res.message }
    } catch (error) {
      console.error('加载收藏列表失败:', error)
      return { success: false, message: error.message || '加载收藏列表失败' }
    } finally {
      loading.value = false
    }
  }

  /**
   * 检查单个商品的收藏状态
   */
  async function checkFavorited(productId) {
    try {
      const res = await checkFavoriteStatus(productId)
      if (res.success) {
        if (res.data) {
          favoriteIds.value.add(productId)
        } else {
          favoriteIds.value.delete(productId)
        }
        return res.data
      }
      return false
    } catch (error) {
      console.error('检查收藏状态失败:', error)
      return false
    }
  }

  /**
   * 获取收藏数量
   */
  async function fetchFavoriteCount() {
    try {
      const res = await getFavoriteCountApi()
      if (res.success) {
        favoriteCount.value = res.data || 0
        return favoriteCount.value
      }
      return 0
    } catch (error) {
      console.error('获取收藏数量失败:', error)
      return 0
    }
  }

  /**
   * 清空本地收藏状态（用于登出时）
   */
  function clearFavorites() {
    favorites.value = []
    favoriteCount.value = 0
    favoriteIds.value = new Set()
    pagination.value = {
      current: 1,
      size: 10,
      total: 0,
      pages: 0
    }
  }

  return {
    // 状态
    favorites,
    favoriteCount,
    favoriteIds,
    pagination,
    loading,
    // 计算属性
    isFavorited,
    // 方法
    addFavorite,
    removeFavorite,
    batchRemoveFavorites,
    toggleFavorite,
    loadFavorites,
    checkFavorited,
    fetchFavoriteCount,
    clearFavorites
  }
})
