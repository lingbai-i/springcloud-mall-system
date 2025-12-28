package com.mall.merchant.controller;

import com.mall.common.core.domain.R;
import com.mall.merchant.domain.dto.BannerApplicationDTO;
import com.mall.merchant.domain.vo.BannerApplicationVO;
import com.mall.merchant.domain.vo.BannerStatisticsVO;
import com.mall.merchant.service.BannerApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 轮播图申请管理控制器
 * 提供商家轮播图投流申请的提交、查询、更新、取消等功能的REST API接口
 * 
 * @author system
 * @version 1.0
 * @since 2025-12-28
 */
@Slf4j
@RestController
@RequestMapping("/api/merchant/banner")
@RequiredArgsConstructor
@Validated
@Tag(name = "轮播图申请管理", description = "商家轮播图投流申请的提交、查询、更新、取消等功能")
public class BannerApplicationController {

    private final BannerApplicationService bannerApplicationService;

    /**
     * 提交轮播图申请
     * 商家提交新的轮播图投流申请
     * 
     * @param dto 申请信息
     * @param merchantId 商家ID（从请求头或Token中获取）
     * @return 申请ID
     */
    @PostMapping("/apply")
    @Operation(summary = "提交轮播图申请", description = "商家提交新的轮播图投流申请")
    public R<Long> createApplication(
            @Valid @RequestBody BannerApplicationDTO dto,
            @Parameter(description = "商家ID") @RequestHeader("X-Merchant-Id") @NotNull Long merchantId) {
        log.info("商家 {} 提交轮播图申请，标题：{}", merchantId, dto.getTitle());
        Long applicationId = bannerApplicationService.createApplication(dto, merchantId);
        return R.ok("申请提交成功", applicationId);
    }

    /**
     * 更新轮播图申请
     * 更新待审核或已拒绝状态的申请
     * 
     * @param id 申请ID
     * @param dto 更新信息
     * @param merchantId 商家ID
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新轮播图申请", description = "更新待审核或已拒绝状态的申请")
    public R<String> updateApplication(
            @Parameter(description = "申请ID") @PathVariable @NotNull Long id,
            @Valid @RequestBody BannerApplicationDTO dto,
            @Parameter(description = "商家ID") @RequestHeader("X-Merchant-Id") @NotNull Long merchantId) {
        log.info("商家 {} 更新轮播图申请 {}", merchantId, id);
        bannerApplicationService.updateApplication(id, dto, merchantId);
        return R.ok("申请更新成功", null);
    }


    /**
     * 取消轮播图申请
     * 取消待审核状态的申请
     * 
     * @param id 申请ID
     * @param merchantId 商家ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "取消轮播图申请", description = "取消待审核状态的申请")
    public R<String> cancelApplication(
            @Parameter(description = "申请ID") @PathVariable @NotNull Long id,
            @Parameter(description = "商家ID") @RequestHeader("X-Merchant-Id") @NotNull Long merchantId) {
        log.info("商家 {} 取消轮播图申请 {}", merchantId, id);
        bannerApplicationService.cancelApplication(id, merchantId);
        return R.ok("申请已取消", null);
    }

    /**
     * 获取轮播图申请详情
     * 获取指定申请的详细信息
     * 
     * @param id 申请ID
     * @param merchantId 商家ID
     * @return 申请详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取轮播图申请详情", description = "获取指定申请的详细信息")
    public R<BannerApplicationVO> getApplicationDetail(
            @Parameter(description = "申请ID") @PathVariable @NotNull Long id,
            @Parameter(description = "商家ID") @RequestHeader("X-Merchant-Id") @NotNull Long merchantId) {
        log.debug("商家 {} 查询轮播图申请详情 {}", merchantId, id);
        BannerApplicationVO vo = bannerApplicationService.getApplicationDetail(id, merchantId);
        return R.ok(vo);
    }

    /**
     * 获取轮播图申请列表
     * 分页查询商家的轮播图申请列表
     * 
     * @param merchantId 商家ID
     * @param status 状态筛选（可选）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 申请列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取轮播图申请列表", description = "分页查询商家的轮播图申请列表")
    public R<Page<BannerApplicationVO>> getApplicationList(
            @Parameter(description = "商家ID") @RequestHeader("X-Merchant-Id") @NotNull Long merchantId,
            @Parameter(description = "状态筛选") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.debug("商家 {} 查询轮播图申请列表，状态：{}，页码：{}，大小：{}", merchantId, status, page, size);
        Page<BannerApplicationVO> result = bannerApplicationService.getApplicationList(merchantId, status, page, size);
        return R.ok(result);
    }

    /**
     * 获取轮播图统计数据
     * 获取指定申请的曝光、点击等统计数据
     * 
     * @param id 申请ID
     * @param merchantId 商家ID
     * @return 统计数据
     */
    @GetMapping("/{id}/statistics")
    @Operation(summary = "获取轮播图统计数据", description = "获取指定申请的曝光、点击等统计数据")
    public R<BannerStatisticsVO> getStatistics(
            @Parameter(description = "申请ID") @PathVariable @NotNull Long id,
            @Parameter(description = "商家ID") @RequestHeader("X-Merchant-Id") @NotNull Long merchantId) {
        log.debug("商家 {} 查询轮播图统计数据 {}", merchantId, id);
        BannerStatisticsVO vo = bannerApplicationService.getStatistics(id, merchantId);
        return R.ok(vo);
    }

    /**
     * 获取申请数量统计
     * 获取商家各状态的申请数量
     * 
     * @param merchantId 商家ID
     * @param status 状态筛选（可选）
     * @return 申请数量
     */
    @GetMapping("/count")
    @Operation(summary = "获取申请数量统计", description = "获取商家各状态的申请数量")
    public R<Long> getApplicationCount(
            @Parameter(description = "商家ID") @RequestHeader("X-Merchant-Id") @NotNull Long merchantId,
            @Parameter(description = "状态筛选") @RequestParam(required = false) String status) {
        log.debug("商家 {} 查询申请数量，状态：{}", merchantId, status);
        Long count = bannerApplicationService.countApplications(merchantId, status);
        return R.ok(count);
    }
}
