package com.mall.admin.controller;

import com.mall.admin.domain.dto.BannerRejectDTO;
import com.mall.admin.domain.vo.BannerReviewDetailVO;
import com.mall.admin.domain.vo.BannerReviewVO;
import com.mall.admin.service.BannerReviewService;
import com.mall.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 轮播图审核控制器
 * 提供管理员轮播图审核相关的REST API接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-12-28
 */
@Slf4j
@RestController
@RequestMapping("/admin/banner")
@RequiredArgsConstructor
@Validated
public class BannerReviewController {

    private final BannerReviewService bannerReviewService;

    /**
     * 获取待审核申请列表
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 待审核申请列表
     */
    @GetMapping("/pending")
    public R<Page<BannerReviewVO>> getPendingList(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.debug("获取待审核申请列表，页码：{}，大小：{}", page, size);
        Page<BannerReviewVO> result = bannerReviewService.getPendingList(page, size);
        return R.ok(result);
    }

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
    public R<Page<BannerReviewVO>> getAllApplications(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.debug("获取所有申请列表，状态：{}，页码：{}，大小：{}", status, page, size);
        Page<BannerReviewVO> result = bannerReviewService.getAllApplications(status, startDate, endDate, page, size);
        return R.ok(result);
    }

    /**
     * 获取申请详情
     * 
     * @param id 申请ID
     * @return 申请详情
     */
    @GetMapping("/{id}")
    public R<BannerReviewDetailVO> getApplicationDetail(@PathVariable @NotNull Long id) {
        log.debug("获取申请详情：{}", id);
        BannerReviewDetailVO vo = bannerReviewService.getApplicationDetail(id);
        return R.ok(vo);
    }

    /**
     * 审核通过申请
     * 
     * @param id 申请ID
     * @param adminId 管理员ID（从请求头或Token中获取）
     * @return 操作结果
     */
    @PostMapping("/{id}/approve")
    public R<String> approveApplication(
            @PathVariable @NotNull Long id,
            @RequestHeader("X-Admin-Id") @NotNull Long adminId) {
        log.info("管理员 {} 审核通过申请 {}", adminId, id);
        bannerReviewService.approveApplication(id, adminId);
        return R.ok("审核通过成功", null);
    }

    /**
     * 审核拒绝申请
     * 
     * @param id 申请ID
     * @param adminId 管理员ID（从请求头或Token中获取）
     * @param dto 拒绝请求（包含拒绝原因）
     * @return 操作结果
     */
    @PostMapping("/{id}/reject")
    public R<String> rejectApplication(
            @PathVariable @NotNull Long id,
            @RequestHeader("X-Admin-Id") @NotNull Long adminId,
            @Valid @RequestBody BannerRejectDTO dto) {
        log.info("管理员 {} 审核拒绝申请 {}，原因：{}", adminId, id, dto.getReason());
        bannerReviewService.rejectApplication(id, adminId, dto.getReason());
        return R.ok("审核拒绝成功", null);
    }

    /**
     * 获取待审核申请数量
     * 
     * @return 待审核数量
     */
    @GetMapping("/pending-count")
    public R<Long> getPendingCount() {
        log.debug("获取待审核申请数量");
        Long count = bannerReviewService.getPendingCount();
        return R.ok(count);
    }
}
