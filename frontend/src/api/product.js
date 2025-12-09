import request from '@/utils/request'

/**
 * 商品相关API
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */

// 获取商品列表
export function getProductList(params) {
  return request({
    url: '/product-service/products',
    method: 'get',
    params
  })
}

// 获取商品详情
// V2.0 2025-12-02: 迁移到 product-service，商品数据统一从 product-service 获取
export function getProductDetail(id) {
  return request({
    url: `/product-service/${id}`,
    method: 'get'
  })
}

// 获取商品分类
export function getCategories() {
  return request({
    url: '/product-service/categories',
    method: 'get'
  })
}

// 根据分类ID获取商品列表
export function getProductsByCategory(params) {
  return request({
    url: `/product-service/categories/${params.categoryId}/products`,
    method: 'get',
    params: {
      page: params.page,
      size: params.size,
      sortBy: params.sortBy
    }
  })
}

// 获取品牌列表
export function getBrands() {
  return request({
    url: '/product-service/brands',
    method: 'get'
  })
}

// 获取商品属性
export function getProductAttributes(categoryId) {
  return request({
    url: `/product-service/categories/${categoryId}/attributes`,
    method: 'get'
  })
}

// 搜索商品
// TODO: 后续优化 - 统一迁移到product-service
export function searchProducts(params) {
  return request({
    url: '/merchant/products/list',
    method: 'get',
    params: {
      keyword: params.keyword,
      current: params.current || params.page || 1,
      size: params.size || 12
    }
  })
}

// 获取商品评价
// TODO: 临时返回空数据,后续product-service实现评论功能后再调用真实接口
export function getProductReviews(productId, params) {
  // 临时返回空数据
  return Promise.resolve({
    code: 200,
    message: '操作成功',
    data: {
      total: 0,
      list: []
    }
  })
  // return request({
  //   url: `/product-service/products/${productId}/reviews`,
  //   method: 'get',
  //   params
  // })
}

// 添加商品评价
// TODO: 临时返回成功,后续product-service实现评论功能后再调用真实接口
export function addProductReview(productId, data) {
  // 临时返回成功
  return Promise.resolve({
    code: 200,
    message: '评价成功'
  })
  // return request({
  //   url: `/product-service/products/${productId}/reviews`,
  //   method: 'post',
  //   data
  // })
}

// 收藏商品
export function favoriteProduct(productId) {
  return request({
    url: `/product-service/products/${productId}/favorite`,
    method: 'post'
  })
}

// 取消收藏商品
export function unfavoriteProduct(productId) {
  return request({
    url: `/product-service/products/${productId}/favorite`,
    method: 'delete'
  })
}

// 获取用户收藏的商品
export function getFavoriteProducts(params) {
  return request({
    url: '/product-service/products/favorites',
    method: 'get',
    params
  })
}

// 获取热门商品
// TODO: 后续优化 - 统一迁移到product-service
export function getHotProducts(params) {
  return request({
    url: '/merchant/products/hot-selling',
    method: 'get',
    params: {
      limit: params?.limit || 12
    }
  })
}

// 获取推荐商品
// TODO: 后续优化 - 统一迁移到product-service
export function getRecommendProducts(params) {
  return request({
    url: '/merchant/products/recommended',
    method: 'get',
    params: {
      limit: params?.limit || 12
    }
  })
}

// 获取新品商品
export function getNewProducts(params) {
  return request({
    url: '/product-service/products/new',
    method: 'get',
    params
  })
}

// 导出productApi对象，包含所有商品相关API
export const productApi = {
  getProductList,
  getProductDetail,
  getCategories,
  getProductsByCategory,
  getBrands,
  getProductAttributes,
  searchProducts,
  getProductReviews,
  addProductReview,
  favoriteProduct,
  unfavoriteProduct,
  getFavoriteProducts,
  getHotProducts,
  getRecommendProducts,
  getNewProducts
}

export default productApi