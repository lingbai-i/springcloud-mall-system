package com.mall.merchant.controller;

import com.mall.common.core.domain.R;
import com.mall.merchant.domain.vo.BannerApplicationVO;
import com.mall.merchant.service.BannerApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 轮播图管理员内部API控制器
 * 提供给admin-service通过Feign调用的内部接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-12-28
 */
@Slf4j
@RestController
@RequestMapping("/api/internal/banner")
@RequiredArgsConstructor
@Validated
@Tag(name = "轮播图管理员内部API", description = "提供给admin-service调用的内部接口")
public class BannerAdminController {

    private final BannerApplicationService bannerApplicationService;

    /**
     * 获取待审核申请列表
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 待审核申请列表
     */
    @GetMapping("/pending")
    @Operation(summary = "获取待审核申请列表", description = "管理员获取待审核的轮播图申请列表")
    public R<Page<BannerApplicationVO>> getPendingApplications(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.debug("获取待审核申请列表，页码：{}，大小：{}", page, size);
        Page<BannerApplicationVO> result = bannerApplicationService.getPendingApplications(page, size);
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
    @Operation(summary = "获取所有申请列表", description = "管理员获取所有轮播图申请列表")
    public R<Page<BannerApplicationVO>> getAllApplications(
            @Parameter(description = "状态筛选") @RequestParam(required = false) String status,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.debug("获取所有申请列表，状态：{}，页码：{}，大小：{}", status, page, size);
        Page<BannerApplicationVO> result = bannerApplicationService.getAllApplications(status, startDate, endDate, page, size);
        return R.ok(result);
    }

    /**
     * 获取申请详情
     * 
     * @param id 申请ID
     * @return 申请详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取申请详情", description = "管理员获取轮播图申请详情")
    public R<BannerApplicationVO> getApplicationDetail(
            @Parameter(description = "申请ID") @PathVariable @NotNull Long id) {
        log.debug("获取申请详情：{}", id);
        BannerApplicationVO vo = bannerApplicationService.getApplicationDetailForAdmin(id);
        return R.ok(vo);
    }

    /**
     * 审核通过申请
     * 
     * @param id 申请ID
     * @param adminId 管理员ID
     * @return 操作结果
     */
    @PostMapping("/{id}/approve")
    @Operation(summary = "审核通过申请", description = "管理员审核通过轮播图申请")
    public R<String> approveApplication(
            @Parameter(description = "申请ID") @PathVariable @NotNull Long id,
            @Parameter(description = "管理员ID") @RequestParam @NotNull Long adminId) {
        log.info("管理员 {} 审核通过申请 {}", adminId, id);
        bannerApplicationService.approveApplication(id, adminId);
        return R.ok("审核通过成功", null);
    }

    /**
     * 审核拒绝申请
     * 
     * @param id 申请ID
     * @param adminId 管理员ID
     * @param reason 拒绝原因
     * @return 操作结果
     */
    @PostMapping("/{id}/reject")
    @Operation(summary = "审核拒绝申请", description = "管理员审核拒绝轮播图申请")
    public R<String> rejectApplication(
            @Parameter(description = "申请ID") @PathVariable @NotNull Long id,
            @Parameter(description = "管理员ID") @RequestParam @NotNull Long adminId,
            @Parameter(description = "拒绝原因") @RequestParam @NotBlank String reason) {
        log.info("管理员 {} 审核拒绝申请 {}，原因：{}", adminId, id, reason);
        bannerApplicationService.rejectApplication(id, adminId, reason);
        return R.ok("审核拒绝成功", null);
    }

    /**
     * 获取待审核申请数量
     * 
     * @return 待审核数量
     */
    @GetMapping("/pending-count")
    @Operation(summary = "获取待审核申请数量", description = "获取待审核的轮播图申请数量")
    public R<Long> getPendingCount() {
        log.debug("获取待审核申请数量");
        Long count = bannerApplicationService.getPendingCount();
        return R.ok(count);
    }

    // ==================== 展示端内部API ====================

    /**
     * 获取活跃轮播图列表（用于首页展示）
     * 
     * @param limit 最大返回数量
     * @return 活跃轮播图列表
     */
    @GetMapping("/active")
    @Operation(summary = "获取活跃轮播图列表", description = "获取当前活跃的轮播图列表，用于首页展示")
    public R<java.util.List<BannerApplicationVO>> getActiveBanners(
            @Parameter(description = "最大返回数量") @RequestParam(defaultValue = "5") int limit) {
        log.debug("获取活跃轮播图列表，最大数量：{}", limit);
        java.util.List<BannerApplicationVO> banners = bannerApplicationService.getActiveBanners(limit);
        return R.ok(banners);
    }

    /**
     * 记录轮播图曝光
     * 
     * @param id 轮播图ID
     * @return 操作结果
     */
    @PostMapping("/{id}/impression")
    @Operation(summary = "记录轮播图曝光", description = "记录轮播图的曝光次数")
    public R<String> recordImpression(
            @Parameter(description = "轮播图ID") @PathVariable @NotNull Long id) {
        log.debug("记录轮播图曝光：{}", id);
        bannerApplicationService.recordImpression(id);
        return R.ok("记录成功", null);
    }

    /**
     * 记录轮播图点击
     * 
     * @param id 轮播图ID
     * @return 操作结果
     */
    @PostMapping("/{id}/click")
    @Operation(summary = "记录轮播图点击", description = "记录轮播图的点击次数")
    public R<String> recordClick(
            @Parameter(description = "轮播图ID") @PathVariable @NotNull Long id) {
        log.debug("记录轮播图点击：{}", id);
        bannerApplicationService.recordClick(id);
        return R.ok("记录成功", null);
    }
}
