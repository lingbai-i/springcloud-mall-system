package com.mall.product.service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存补偿服务接口
 * 提供分布式事务场景下的库存补偿机制
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
public interface StockCompensationService {
    
    /**
     * 创建库存补偿记录
     * 
     * @param productId 商品ID
     * @param skuId SKU ID（可选）
     * @param quantity 数量
     * @param orderNo 订单号
     * @param operationType 操作类型（DEDUCT/ROLLBACK）
     * @param operatorId 操作人ID
     * @return 补偿记录ID
     */
    Long createCompensationRecord(Long productId, Long skuId, Integer quantity, 
                                 String orderNo, String operationType, Long operatorId);
    
    /**
     * 执行库存补偿
     * 
     * @param compensationId 补偿记录ID
     * @return 补偿结果
     */
    CompensationResult executeCompensation(Long compensationId);
    
    /**
     * 批量执行库存补偿
     * 
     * @param compensationIds 补偿记录ID列表
     * @return 批量补偿结果
     */
    BatchCompensationResult batchExecuteCompensation(List<Long> compensationIds);
    
    /**
     * 查询待补偿记录
     * 
     * @param status 补偿状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 待补偿记录列表
     */
    List<StockCompensationRecord> getPendingCompensations(String status, 
                                                         LocalDateTime startTime, 
                                                         LocalDateTime endTime);
    
    /**
     * 取消补偿记录
     * 
     * @param compensationId 补偿记录ID
     * @param reason 取消原因
     * @return 是否成功
     */
    boolean cancelCompensation(Long compensationId, String reason);
    
    /**
     * 库存补偿记录
     */
    class StockCompensationRecord {
        private Long id;
        private Long productId;
        private Long skuId;
        private Integer quantity;
        private String orderNo;
        private String operationType;
        private String status; // PENDING, SUCCESS, FAILED, CANCELLED
        private String failReason;
        private Integer retryCount;
        private Integer maxRetryCount;
        private Long operatorId;
        private LocalDateTime createTime;
        private LocalDateTime executeTime;
        private LocalDateTime updateTime;
        
        // 构造函数
        public StockCompensationRecord() {}
        
        // Getter和Setter方法
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public Long getSkuId() { return skuId; }
        public void setSkuId(Long skuId) { this.skuId = skuId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public String getOrderNo() { return orderNo; }
        public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
        
        public String getOperationType() { return operationType; }
        public void setOperationType(String operationType) { this.operationType = operationType; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getFailReason() { return failReason; }
        public void setFailReason(String failReason) { this.failReason = failReason; }
        
        public Integer getRetryCount() { return retryCount; }
        public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
        
        public Integer getMaxRetryCount() { return maxRetryCount; }
        public void setMaxRetryCount(Integer maxRetryCount) { this.maxRetryCount = maxRetryCount; }
        
        public Long getOperatorId() { return operatorId; }
        public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
        
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
        
        public LocalDateTime getExecuteTime() { return executeTime; }
        public void setExecuteTime(LocalDateTime executeTime) { this.executeTime = executeTime; }
        
        public LocalDateTime getUpdateTime() { return updateTime; }
        public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    }
    
    /**
     * 补偿结果
     */
    class CompensationResult {
        private boolean success;
        private String message;
        private Long compensationId;
        private String status;
        private LocalDateTime executeTime;
        
        public CompensationResult() {}
        
        public CompensationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public CompensationResult(boolean success, String message, Long compensationId, String status) {
            this.success = success;
            this.message = message;
            this.compensationId = compensationId;
            this.status = status;
            this.executeTime = LocalDateTime.now();
        }
        
        // Getter和Setter方法
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Long getCompensationId() { return compensationId; }
        public void setCompensationId(Long compensationId) { this.compensationId = compensationId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public LocalDateTime getExecuteTime() { return executeTime; }
        public void setExecuteTime(LocalDateTime executeTime) { this.executeTime = executeTime; }
    }
    
    /**
     * 批量补偿结果
     */
    class BatchCompensationResult {
        private boolean success;
        private String message;
        private int totalCount;
        private int successCount;
        private int failCount;
        private List<CompensationResult> results;
        
        public BatchCompensationResult() {}
        
        public BatchCompensationResult(boolean success, String message, int totalCount, 
                                     int successCount, int failCount) {
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
        
        public List<CompensationResult> getResults() { return results; }
        public void setResults(List<CompensationResult> results) { this.results = results; }
    }
}