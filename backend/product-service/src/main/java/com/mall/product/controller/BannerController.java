package com.mall.product.controller;

import com.mall.common.core.domain.R;
import com.mall.product.domain.vo.ActiveBannerVO;
import com.mall.product.service.BannerDisplayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 轮播图展示控制器
 * 提供首页轮播图展示相关的REST API接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-12-28
 */
@Slf4j
@RestController
@RequestMapping("/api/banner")
@RequiredArgsConstructor
@Validated
@Tag(name = "轮播图展示", description = "首页轮播图展示相关接口")
public class BannerController {

    private final BannerDisplayService bannerDisplayService;

    /**
     * 获取活跃轮播图列表
     * 
     * @param limit 最大返回数量（默认5，最大10）
     * @return 活跃轮播图列表
     */
    @GetMapping("/active")
    @Operation(summary = "获取活跃轮播图列表", description = "获取当前活跃的轮播图列表，用于首页展示")
    public R<List<ActiveBannerVO>> getActiveBanners(
            @Parameter(description = "最大返回数量") 
            @RequestParam(defaultValue = "5") @Min(1) @Max(10) int limit) {
        log.debug("获取活跃轮播图列表，最大数量：{}", limit);
        List<ActiveBannerVO> banners = bannerDisplayService.getActiveBanners(limit);
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
        bannerDisplayService.recordImpression(id);
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
        bannerDisplayService.recordClick(id);
        return R.ok("记录成功", null);
    }

    /**
     * 刷新轮播图缓存（管理接口）
     * 
     * @return 操作结果
     */
    @PostMapping("/refresh-cache")
    @Operation(summary = "刷新轮播图缓存", description = "清除轮播图缓存，强制从数据库重新加载")
    public R<String> refreshCache() {
        log.info("刷新轮播图缓存");
        bannerDisplayService.refreshCache();
        return R.ok("缓存已刷新", null);
    }
}
