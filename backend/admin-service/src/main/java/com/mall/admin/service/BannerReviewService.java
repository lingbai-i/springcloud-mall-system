package com.mall.admin.service;

import com.mall.admin.domain.vo.BannerReviewDetailVO;
import com.mall.admin.domain.vo.BannerReviewVO;
import org.springframework.data.domain.Page;

/**
 * 轮播图审核服务接口
 * 提供管理员端轮播图审核相关的业务操作
 * 
 * @author system
 * @version 1.0
 * @since 2025-12-28
 */
public interface BannerReviewService {

    /**
     * 获取待审核申请列表
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 待审核申请分页列表
     */
    Page<BannerReviewVO> getPendingList(int page, int size);

    /**
     * 获取所有申请列表
     * 
     * @param status 状态筛选（可选）
     * @param startDate 开始日期筛选（可选）
     * @param endDate 结束日期筛选（可选）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 申请分页列表
     */
    Page<BannerReviewVO> getAllApplications(String status, String startDate, String endDate, int page, int size);

    /**
     * 获取申请详情
     * 
     * @param id 申请ID
     * @return 申请详情
     */
    BannerReviewDetailVO getApplicationDetail(Long id);

    /**
     * 审核通过申请
     * 
     * @param id 申请ID
     * @param adminId 管理员ID
     */
    void approveApplication(Long id, Long adminId);

    /**
     * 审核拒绝申请
     * 
     * @param id 申请ID
     * @param adminId 管理员ID
     * @param reason 拒绝原因
     */
    void rejectApplication(Long id, Long adminId, String reason);

    /**
     * 获取待审核申请数量
     * 
     * @return 待审核数量
     */
    Long getPendingCount();
}
