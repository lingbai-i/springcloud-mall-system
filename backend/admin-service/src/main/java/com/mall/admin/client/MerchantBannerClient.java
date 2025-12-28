package com.mall.admin.client;

import com.mall.admin.domain.vo.BannerReviewVO;
import com.mall.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 商家服务轮播图Feign Client
 * 用于调用merchant-service的轮播图内部API
 * 
 * @author system
 * @version 1.0
 * @since 2025-12-28
 */
@FeignClient(name = "merchant-service", path = "/api/internal/banner", contextId = "merchantBannerClient")
public interface MerchantBannerClient {

    /**
     * 获取待审核申请列表
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 待审核申请列表
     */
    @GetMapping("/pending")
    R<Page<BannerReviewVO>> getPendingApplications(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size);

    /**
     * 获取所有申请列表
     * 
     * @param status 状态筛选（可选）
     * @param startDate 开始日期筛选（可选）
     * @param endDate 结束日期筛选（可选）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 申请列表
     */
    @GetMapping("/list")
    R<Page<BannerReviewVO>> getAllApplications(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size);

    /**
     * 获取申请详情
     * 
     * @param id 申请ID
     * @return 申请详情
     */
    @GetMapping("/{id}")
    R<BannerReviewVO> getApplicationDetail(@PathVariable("id") Long id);

    /**
     * 审核通过申请
     * 
     * @param id 申请ID
     * @param adminId 管理员ID
     * @return 操作结果
     */
    @PostMapping("/{id}/approve")
    R<String> approveApplication(
            @PathVariable("id") Long id,
            @RequestParam("adminId") Long adminId);

    /**
     * 审核拒绝申请
     * 
     * @param id 申请ID
     * @param adminId 管理员ID
     * @param reason 拒绝原因
     * @return 操作结果
     */
    @PostMapping("/{id}/reject")
    R<String> rejectApplication(
            @PathVariable("id") Long id,
            @RequestParam("adminId") Long adminId,
            @RequestParam("reason") String reason);

    /**
     * 获取待审核申请数量
     * 
     * @return 待审核数量
     */
    @GetMapping("/pending-count")
    R<Long> getPendingCount();
}
