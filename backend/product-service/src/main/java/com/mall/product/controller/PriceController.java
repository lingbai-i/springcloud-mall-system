package com.mall.product.controller;

import com.mall.product.service.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 价格管理控制器
 * 提供价格调整、历史查询、审核机制、策略管理等API接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@RestController
@RequestMapping("/price")
public class PriceController {
    
    @Autowired
    private PriceService priceService;
    
    /**
     * 单个商品调价
     * 
     * @param productId 商品ID
     * @param skuId SKU ID（可选）
     * @param newPrice 新价格
     * @param operatorId 操作员ID
     * @param reason 调价原因
     * @return 调价结果
     */
    @PostMapping("/adjust")
    public PriceService.PriceAdjustmentResult adjustPrice(
            @RequestParam Long productId,
            @RequestParam(required = false) Long skuId,
            @RequestParam Double newPrice,
            @RequestParam Long operatorId,
            @RequestParam String reason) {
        return priceService.adjustPrice(productId, skuId, newPrice, operatorId, reason);
    }
    
    /**
     * 批量调价
     * 
     * @param priceAdjustments 价格调整列表
     * @return 批量调价结果
     */
    @PostMapping("/batch/adjust")
    public PriceService.BatchPriceAdjustmentResult batchAdjustPrice(
            @RequestBody List<PriceService.PriceAdjustment> priceAdjustments) {
        return priceService.batchAdjustPrice(priceAdjustments);
    }
    
    /**
     * 获取价格历史记录
     * 
     * @param productId 商品ID
     * @param skuId SKU ID（可选）
     * @param current 当前页码
     * @param size 页面大小
     * @return 价格历史记录
     */
    @GetMapping("/history")
    public Object getPriceHistory(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long skuId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        return priceService.getPriceHistory(productId, skuId, current, size);
    }
    
    /**
     * 获取价格变动统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 价格变动统计
     */
    @GetMapping("/statistics")
    public Object getPriceChangeStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return priceService.getPriceChangeStatistics(startTime, endTime);
    }
    
    /**
     * 价格审核
     * 
     * @param priceHistoryId 价格历史记录ID
     * @param approved 是否通过审核
     * @param auditReason 审核意见
     * @param auditorId 审核员ID
     * @return 审核结果
     */
    @PostMapping("/audit")
    public PriceService.PriceAuditResult auditPrice(
            @RequestParam Long priceHistoryId,
            @RequestParam Boolean approved,
            @RequestParam String auditReason,
            @RequestParam Long auditorId) {
        return priceService.auditPrice(priceHistoryId, approved, auditReason, auditorId);
    }
    
    /**
     * 获取待审核价格列表
     * 
     * @param current 当前页码
     * @param size 页面大小
     * @return 待审核价格列表
     */
    @GetMapping("/audit/pending")
    public Object getPendingAuditPrices(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        return priceService.getPendingAuditPrices(current, size);
    }
    
    /**
     * 创建价格策略
     * 
     * @param priceStrategy 价格策略
     * @return 创建结果
     */
    @PostMapping("/strategy")
    public PriceService.PriceStrategyResult createPriceStrategy(
            @RequestBody PriceService.PriceStrategy priceStrategy) {
        return priceService.createPriceStrategy(priceStrategy);
    }
    
    /**
     * 应用价格策略
     * 
     * @param strategyId 策略ID
     * @param productIds 商品ID列表
     * @param operatorId 操作员ID
     * @return 应用结果
     */
    @PostMapping("/strategy/{strategyId}/apply")
    public PriceService.PriceStrategyResult applyPriceStrategy(
            @PathVariable Long strategyId,
            @RequestBody List<Long> productIds,
            @RequestParam Long operatorId) {
        return priceService.applyPriceStrategy(strategyId, productIds, operatorId);
    }
    
    /**
     * 获取价格策略列表
     * 
     * @param current 当前页码
     * @param size 页面大小
     * @return 价格策略列表
     */
    @GetMapping("/strategy")
    public Object getPriceStrategies(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        return priceService.getPriceStrategies(current, size);
    }
}