package com.mall.product.service;

import com.mall.product.domain.entity.Product;
import com.mall.product.domain.entity.ProductSku;
import com.mall.product.domain.entity.StockLog;

import java.util.List;

/**
 * 库存管理服务接口
 * 提供库存监控、预警、扣减回滚等功能
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
public interface StockService {
    
    /**
     * 实时库存监控
     * 
     * @return 库存监控数据
     */
    Object getStockMonitorData();
    
    /**
     * 获取库存预警商品列表
     * 
     * @param warningLevel 预警级别：1-低库存，2-缺货
     * @return 预警商品列表
     */
    List<Product> getStockWarningProducts(Integer warningLevel);
    
    /**
     * 库存扣减（支持事务回滚）
     * 
     * @param productId 商品ID
     * @param skuId SKU ID（可选）
     * @param quantity 扣减数量
     * @param orderNo 订单号
     * @param operatorId 操作人ID
     * @return 扣减结果
     */
    StockOperationResult deductStock(Long productId, Long skuId, Integer quantity, String orderNo, Long operatorId);
    
    /**
     * 库存回滚
     * 
     * @param productId 商品ID
     * @param skuId SKU ID（可选）
     * @param quantity 回滚数量
     * @param orderNo 订单号
     * @param operatorId 操作人ID
     * @return 回滚结果
     */
    StockOperationResult rollbackStock(Long productId, Long skuId, Integer quantity, String orderNo, Long operatorId);
    
    /**
     * 批量库存扣减
     * 
     * @param stockOperations 库存操作列表
     * @return 批量操作结果
     */
    BatchStockOperationResult batchDeductStock(List<StockOperation> stockOperations);
    
    /**
     * 批量库存回滚
     * 
     * @param stockOperations 库存操作列表
     * @return 批量操作结果
     */
    BatchStockOperationResult batchRollbackStock(List<StockOperation> stockOperations);
    
    /**
     * 获取库存变更日志
     * 
     * @param productId 商品ID
     * @param skuId SKU ID（可选）
     * @param current 当前页码
     * @param size 每页大小
     * @return 库存变更日志分页数据
     */
    Object getStockLogs(Long productId, Long skuId, Long current, Long size);
    
    /**
     * 库存盘点
     * 
     * @param productId 商品ID
     * @param skuId SKU ID（可选）
     * @param actualStock 实际库存
     * @param operatorId 操作人ID
     * @param reason 盘点原因
     * @return 盘点结果
     */
    StockOperationResult stockTaking(Long productId, Long skuId, Integer actualStock, Long operatorId, String reason);
    
    /**
     * 库存操作类
     */
    class StockOperation {
        private Long productId;
        private Long skuId;
        private Integer quantity;
        private String orderNo;
        private Long operatorId;
        private String reason;
        
        // 构造函数
        public StockOperation() {}
        
        public StockOperation(Long productId, Integer quantity, String orderNo, Long operatorId) {
            this.productId = productId;
            this.quantity = quantity;
            this.orderNo = orderNo;
            this.operatorId = operatorId;
        }
        
        public StockOperation(Long productId, Long skuId, Integer quantity, String orderNo, Long operatorId, String reason) {
            this.productId = productId;
            this.skuId = skuId;
            this.quantity = quantity;
            this.orderNo = orderNo;
            this.operatorId = operatorId;
            this.reason = reason;
        }
        
        // Getter和Setter方法
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public Long getSkuId() { return skuId; }
        public void setSkuId(Long skuId) { this.skuId = skuId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public String getOrderNo() { return orderNo; }
        public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
        
        public Long getOperatorId() { return operatorId; }
        public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    /**
     * 库存操作结果类
     */
    class StockOperationResult {
        private boolean success;
        private String message;
        private Integer beforeStock;
        private Integer afterStock;
        private Long logId;
        
        public StockOperationResult() {}
        
        public StockOperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public StockOperationResult(boolean success, String message, Integer beforeStock, Integer afterStock, Long logId) {
            this.success = success;
            this.message = message;
            this.beforeStock = beforeStock;
            this.afterStock = afterStock;
            this.logId = logId;
        }
        
        // Getter和Setter方法
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Integer getBeforeStock() { return beforeStock; }
        public void setBeforeStock(Integer beforeStock) { this.beforeStock = beforeStock; }
        
        public Integer getAfterStock() { return afterStock; }
        public void setAfterStock(Integer afterStock) { this.afterStock = afterStock; }
        
        public Long getLogId() { return logId; }
        public void setLogId(Long logId) { this.logId = logId; }
    }
    
    /**
     * 批量库存操作结果类
     */
    class BatchStockOperationResult {
        private boolean success;
        private String message;
        private int totalCount;
        private int successCount;
        private int failCount;
        private List<StockOperationResult> results;
        
        public BatchStockOperationResult() {}
        
        public BatchStockOperationResult(boolean success, String message, int totalCount, int successCount, int failCount) {
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
        
        public List<StockOperationResult> getResults() { return results; }
        public void setResults(List<StockOperationResult> results) { this.results = results; }
    }
}