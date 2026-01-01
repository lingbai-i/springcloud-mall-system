import request from '@/utils/request'

/**
 * 搜索相关API
 * 
 * @author lingbai
 * @version 2.0
 * @since 2025-12-31
 */

/**
 * 搜索商品（完整筛选参数）
 * @param {Object} params 搜索参数
 * @param {string} params.keyword - 搜索关键词
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页大小
 * @param {number} params.categoryId - 分类ID
 * @param {string} params.brand - 品牌
 * @param {number} params.minPrice - 最低价格
 * @param {number} params.maxPrice - 最高价格
 * @param {string} params.sortBy - 排序方式：price_asc, price_desc, sales, newest
 */
export function searchProducts(params) {
  // 构建查询参数
  const queryParams = {
    keyword: params.keyword || '',
    page: params.page || 1,
    size: params.size || 12,
    status: 1 // 只搜索上架商品
  }
  
  // 可选参数
  if (params.categoryId) queryParams.categoryId = params.categoryId
  if (params.brand) queryParams.brand = params.brand
  if (params.minPrice !== undefined && params.minPrice !== null) queryParams.minPrice = params.minPrice
  if (params.maxPrice !== undefined && params.maxPrice !== null) queryParams.maxPrice = params.maxPrice
  
  // 排序参数 - 后端支持的排序字段
  if (params.sortBy && params.sortBy !== 'default') {
    // 映射前端排序值到后端字段
    const sortMapping = {
      'sales': 'salesCount,desc',
      'price_asc': 'price,asc',
      'price_desc': 'price,desc',
      'newest': 'createTime,desc'
    }
    if (sortMapping[params.sortBy]) {
      const [field, order] = sortMapping[params.sortBy].split(',')
      queryParams.sortField = field
      queryParams.sortOrder = order
    }
  }
  
  return request({
    url: '/merchant/products/list',
    method: 'get',
    params: queryParams
  })
}

/**
 * 获取热门搜索关键词
 * 基于热销商品生成热门搜索词
 */
export function getHotSearchKeywords() {
  return request({
    url: '/merchant/products/hot-selling',
    method: 'get',
    params: { limit: 10 }
  })
}

/**
 * 获取搜索建议（基于商品名称模糊匹配）
 * @param {string} keyword 输入的关键词
 */
export function getSearchSuggestions(keyword) {
  if (!keyword || keyword.trim().length < 1) {
    return Promise.resolve({ code: 200, data: [] })
  }
  
  return request({
    url: '/merchant/products/list',
    method: 'get',
    params: {
      keyword: keyword.trim(),
      page: 1,
      size: 8,
      status: 1
    }
  })
}

/**
 * 获取商品分类列表
 */
export function getCategories() {
  return request({
    url: '/product-service/categories',
    method: 'get'
  })
}

/**
 * 获取品牌列表
 */
export function getBrands() {
  return request({
    url: '/product-service/brands',
    method: 'get'
  })
}

// ==================== 搜索历史管理（本地存储）====================

const SEARCH_HISTORY_KEY = 'mall_search_history'
const MAX_HISTORY_COUNT = 10

/**
 * 获取搜索历史
 * @returns {string[]} 搜索历史数组
 */
export function getSearchHistory() {
  try {
    const history = localStorage.getItem(SEARCH_HISTORY_KEY)
    return history ? JSON.parse(history) : []
  } catch (e) {
    console.error('获取搜索历史失败', e)
    return []
  }
}

/**
 * 添加搜索历史
 * @param {string} keyword 搜索关键词
 */
export function addSearchHistory(keyword) {
  if (!keyword || !keyword.trim()) return
  
  try {
    let history = getSearchHistory()
    const trimmedKeyword = keyword.trim()
    
    // 移除重复项
    history = history.filter(item => item !== trimmedKeyword)
    
    // 添加到头部
    history.unshift(trimmedKeyword)
    
    // 限制数量
    if (history.length > MAX_HISTORY_COUNT) {
      history = history.slice(0, MAX_HISTORY_COUNT)
    }
    
    localStorage.setItem(SEARCH_HISTORY_KEY, JSON.stringify(history))
  } catch (e) {
    console.error('保存搜索历史失败', e)
  }
}

/**
 * 删除单条搜索历史
 * @param {string} keyword 要删除的关键词
 */
export function removeSearchHistory(keyword) {
  try {
    let history = getSearchHistory()
    history = history.filter(item => item !== keyword)
    localStorage.setItem(SEARCH_HISTORY_KEY, JSON.stringify(history))
  } catch (e) {
    console.error('删除搜索历史失败', e)
  }
}

/**
 * 清空搜索历史
 */
export function clearSearchHistory() {
  try {
    localStorage.removeItem(SEARCH_HISTORY_KEY)
  } catch (e) {
    console.error('清空搜索历史失败', e)
  }
}

export default {
  searchProducts,
  getHotSearchKeywords,
  getSearchSuggestions,
  getCategories,
  getBrands,
  getSearchHistory,
  addSearchHistory,
  removeSearchHistory,
  clearSearchHistory
}



