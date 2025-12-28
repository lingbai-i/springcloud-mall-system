import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

/**
 * 管理员轮播图审核API
 * 提供轮播图审核列表、审核通过、审核拒绝、待审核数量等接口
 */

/**
 * 获取当前管理员ID
 * @returns {Number|null} 管理员ID
 */
function getAdminId() {
  const userStore = useUserStore()
  return userStore.userInfo?.id || userStore.userId || null
}

// ==================== 审核管理 ====================

/**
 * 获取待审核轮播图列表
 * @param {Object} params 查询参数
 * @param {String} params.status 状态筛选 (PENDING/APPROVED/REJECTED)
 * @param {String} params.startDate 开始日期
 * @param {String} params.endDate 结束日期
 * @param {Number} params.page 页码
 * @param {Number} params.size 每页数量
 * @returns {Promise} 分页审核列表
 */
export function getBannerReviewList(params) {
  return request({
    url: '/admin/banner/list',
    method: 'get',
    params
  })
}

/**
 * 获取轮播图审核详情
 * @param {Number} id 申请ID
 * @returns {Promise} 审核详情
 */
export function getBannerReviewDetail(id) {
  return request({
    url: `/admin/banner/${id}`,
    method: 'get'
  })
}

/**
 * 审核通过轮播图申请
 * @param {Number} id 申请ID
 * @returns {Promise}
 */
export function approveBannerApplication(id) {
  const adminId = getAdminId()
  return request({
    url: `/admin/banner/${id}/approve`,
    method: 'post',
    headers: {
      'X-Admin-Id': adminId
    }
  })
}

/**
 * 审核拒绝轮播图申请
 * @param {Number} id 申请ID
 * @param {Object} data 拒绝数据
 * @param {String} data.reason 拒绝原因
 * @returns {Promise}
 */
export function rejectBannerApplication(id, data) {
  const adminId = getAdminId()
  return request({
    url: `/admin/banner/${id}/reject`,
    method: 'post',
    headers: {
      'X-Admin-Id': adminId
    },
    data
  })
}

/**
 * 获取待审核轮播图数量
 * @returns {Promise} 待审核数量
 */
export function getPendingBannerCount() {
  return request({
    url: '/admin/banner/pending-count',
    method: 'get'
  })
}
