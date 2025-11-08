package com.mall.product.service;

import com.mall.product.domain.entity.PriceHistory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 价格管理服务接口
 * 提供价格调整、历史查询、审核机制等功能
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
public interface PriceService {
    
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
    PriceAdjustmentResult adjustPrice(Long productId, Long skuId, Double newPrice, Long operatorId, String reason);
    
    /**
     * 批量调价
     * 
     * @param priceAdjustments 价格调整列表
     * @return 批量调价结果
     */
    BatchPriceAdjustmentResult batchAdjustPrice(List<PriceAdjustment> priceAdjustments);
    
    /**
     * 获取价格历史记录
     * 
     * @param productId 商品ID
     * @param skuId SKU ID（可选）
     * @param current 当前页码
     * @param size 页面大小
     * @return 价格历史记录
     */
    Object getPriceHistory(Long productId, Long skuId, Long current, Long size);
    
    /**
     * 获取价格变动统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 价格变动统计
     */
    Object getPriceChangeStatistics(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 价格审核
     * 
     * @param priceHistoryId 价格历史记录ID
     * @param approved 是否通过审核
     * @param auditReason 审核意见
     * @param auditorId 审核员ID
     * @return 审核结果
     */
    PriceAuditResult auditPrice(Long priceHistoryId, Boolean approved, String auditReason, Long auditorId);
    
    /**
     * 获取待审核价格列表
     * 
     * @param current 当前页码
     * @param size 页面大小
     * @return 待审核价格列表
     */
    Object getPendingAuditPrices(Long current, Long size);
    
    /**
     * 创建价格策略
     * 
     * @param priceStrategy 价格策略
     * @return 创建结果
     */
    PriceStrategyResult createPriceStrategy(PriceStrategy priceStrategy);
    
    /**
     * 应用价格策略
     * 
     * @param strategyId 策略ID
     * @param productIds 商品ID列表
     * @param operatorId 操作员ID
     * @return 应用结果
     */
    PriceStrategyResult applyPriceStrategy(Long strategyId, List<Long> productIds, Long operatorId);
    
    /**
     * 获取价格策略列表
     * 
     * @param current 当前页码
     * @param size 页面大小
     * @return 价格策略列表
     */
    Object getPriceStrategies(Long current, Long size);
    
    // ==================== 内部类定义 ====================
    
    /**
     * 价格调整参数
     */
    class PriceAdjustment {
        private Long productId;
        private Long skuId;
        private Double newPrice;
        private Long operatorId;
        private String reason;
        
        // 构造函数
        public PriceAdjustment() {}
        
        public PriceAdjustment(Long productId, Long skuId, Double newPrice, Long operatorId, String reason) {
            this.productId = productId;
            this.skuId = skuId;
            this.newPrice = newPrice;
            this.operatorId = operatorId;
            this.reason = reason;
        }
        
        // Getter和Setter方法
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public Long getSkuId() { return skuId; }
        public void setSkuId(Long skuId) { this.skuId = skuId; }
        
        public Double getNewPrice() { return newPrice; }
        public void setNewPrice(Double newPrice) { this.newPrice = newPrice; }
        
        public Long getOperatorId() { return operatorId; }
        public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    /**
     * 价格调整结果
     */
    class PriceAdjustmentResult {
        private boolean success;
        private String message;
        private Double oldPrice;
        private Double newPrice;
        private Long priceHistoryId;
        
        // 构造函数
        public PriceAdjustmentResult() {}
        
        public PriceAdjustmentResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public PriceAdjustmentResult(boolean success, String message, Double oldPrice, Double newPrice, Long priceHistoryId) {
            this.success = success;
            this.message = message;
            this.oldPrice = oldPrice;
            this.newPrice = newPrice;
            this.priceHistoryId = priceHistoryId;
        }
        
        // Getter和Setter方法
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Double getOldPrice() { return oldPrice; }
        public void setOldPrice(Double oldPrice) { this.oldPrice = oldPrice; }
        
        public Double getNewPrice() { return newPrice; }
        public void setNewPrice(Double newPrice) { this.newPrice = newPrice; }
        
        public Long getPriceHistoryId() { return priceHistoryId; }
        public void setPriceHistoryId(Long priceHistoryId) { this.priceHistoryId = priceHistoryId; }
    }
    
    /**
     * 批量价格调整结果
     */
    class BatchPriceAdjustmentResult {
        private boolean success;
        private String message;
        private int totalCount;
        private int successCount;
        private int failCount;
        private List<PriceAdjustmentResult> results;
        
        // 构造函数
        public BatchPriceAdjustmentResult() {}
        
        public BatchPriceAdjustmentResult(boolean success, String message, int totalCount, int successCount, int failCount) {
            this.success = success;
            this.message = message;
            this.totalCount = totalCount;
            this.successCount = successCount;
            this.failCount = failCount;
        }
        
        // Getter和Setter方法
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        
        public int getFailCount() { return failCount; }
        public void setFailCount(int failCount) { this.failCount = failCount; }
        
        public List<PriceAdjustmentResult> getResults() { return results; }
        public void setResults(List<PriceAdjustmentResult> results) { this.results = results; }
    }
    
    /**
     * 价格审核结果
     */
    class PriceAuditResult {
        private boolean success;
        private String message;
        private Long priceHistoryId;
        private String auditStatus;
        
        // 构造函数
        public PriceAuditResult() {}
        
        public PriceAuditResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public PriceAuditResult(boolean success, String message, Long priceHistoryId, String auditStatus) {
            this.success = success;
            this.message = message;
            this.priceHistoryId = priceHistoryId;
            this.auditStatus = auditStatus;
        }
        
        // Getter和Setter方法
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Long getPriceHistoryId() { return priceHistoryId; }
        public void setPriceHistoryId(Long priceHistoryId) { this.priceHistoryId = priceHistoryId; }
        
        public String getAuditStatus() { return auditStatus; }
        public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    }
    
    /**
     * 价格策略
     */
    class PriceStrategy {
        private Long id;
        private String name;
        private String description;
        private String strategyType; // 1-固定价格 2-百分比调整 3-固定金额调整
        private Double adjustmentValue;
        private String conditions; // JSON格式的条件配置
        private Integer status; // 1-启用 0-禁用
        private Long creatorId;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        
        // 构造函数
        public PriceStrategy() {}
        
        // Getter和Setter方法
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getStrategyType() { return strategyType; }
        public void setStrategyType(String strategyType) { this.strategyType = strategyType; }
        
        public Double getAdjustmentValue() { return adjustmentValue; }
        public void setAdjustmentValue(Double adjustmentValue) { this.adjustmentValue = adjustmentValue; }
        
        public String getConditions() { return conditions; }
        public void setConditions(String conditions) { this.conditions = conditions; }
        
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        
        public Long getCreatorId() { return creatorId; }
        public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
        
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
        
        public LocalDateTime getUpdateTime() { return updateTime; }
        public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    }
    
    /**
     * 价格策略结果
     */
    class PriceStrategyResult {
        private boolean success;
        private String message;
        private Long strategyId;
        private int affectedCount;
        
        // 构造函数
        public PriceStrategyResult() {}
        
        public PriceStrategyResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public PriceStrategyResult(boolean success, String message, Long strategyId, int affectedCount) {
            this.success = success;
            this.message = message;
            this.strategyId = strategyId;
            this.affectedCount = affectedCount;
        }
        
        // Getter和Setter方法
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Long getStrategyId() { return strategyId; }
        public void setStrategyId(Long strategyId) { this.strategyId = strategyId; }
        
        public int getAffectedCount() { return affectedCount; }
        public void setAffectedCount(int affectedCount) { this.affectedCount = affectedCount; }
    }
}