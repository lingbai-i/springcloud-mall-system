import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

/**
 * 商家轮播图投流API
 * 提供轮播图申请提交、列表、详情、取消、统计等接口
 */

// 获取商家ID的辅助函数
function getMerchantId() {
  const userStore = useUserStore()
  return userStore.merchantId || userStore.userInfo?.merchantId || '1'
}

// ==================== 申请管理 ====================

/**
 * 提交轮播图申请
 * @param {Object} data 申请数据
 * @param {String} data.imageUrl 轮播图图片URL
 * @param {String} data.title 标题
 * @param {String} data.description 描述
 * @param {String} data.targetUrl 跳转链接
 * @param {String} data.startDate 展示开始日期 (yyyy-MM-dd)
 * @param {String} data.endDate 展示结束日期 (yyyy-MM-dd)
 * @returns {Promise} 申请ID
 */
export function submitBannerApplication(data) {
  return request({
    url: '/merchant/banner/apply',
    method: 'post',
    data,
    headers: {
      'X-Merchant-Id': getMerchantId()
    }
  })
}

/**
 * 获取轮播图申请列表
 * @param {Object} params 查询参数
 * @param {String} params.status 状态筛选 (PENDING/APPROVED/REJECTED/EXPIRED/CANCELLED)
 * @param {Number} params.page 页码
 * @param {Number} params.size 每页数量
 * @returns {Promise} 分页申请列表
 */
export function getBannerApplicationList(params) {
  return request({
    url: '/merchant/banner/list',
    method: 'get',
    params,
    headers: {
      'X-Merchant-Id': getMerchantId()
    }
  })
}

/**
 * 获取轮播图申请详情
 * @param {Number} id 申请ID
 * @returns {Promise} 申请详情
 */
export function getBannerApplicationDetail(id) {
  return request({
    url: `/merchant/banner/${id}`,
    method: 'get',
    headers: {
      'X-Merchant-Id': getMerchantId()
    }
  })
}

/**
 * 更新轮播图申请
 * @param {Number} id 申请ID
 * @param {Object} data 更新数据
 * @returns {Promise}
 */
export function updateBannerApplication(id, data) {
  return request({
    url: `/merchant/banner/${id}`,
    method: 'put',
    data,
    headers: {
      'X-Merchant-Id': getMerchantId()
    }
  })
}

/**
 * 取消轮播图申请
 * @param {Number} id 申请ID
 * @returns {Promise}
 */
export function cancelBannerApplication(id) {
  return request({
    url: `/merchant/banner/${id}`,
    method: 'delete',
    headers: {
      'X-Merchant-Id': getMerchantId()
    }
  })
}

/**
 * 获取轮播图统计数据
 * @param {Number} id 申请ID
 * @returns {Promise} 统计数据 (曝光量、点击量、点击率)
 */
export function getBannerStatistics(id) {
  return request({
    url: `/merchant/banner/${id}/statistics`,
    method: 'get',
    headers: {
      'X-Merchant-Id': getMerchantId()
    }
  })
}
