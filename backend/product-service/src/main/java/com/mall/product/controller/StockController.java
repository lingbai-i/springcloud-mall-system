package com.mall.product.controller;

import com.mall.product.domain.entity.Product;
import com.mall.product.service.StockService;
import com.mall.product.service.StockService.BatchStockOperationResult;
import com.mall.product.service.StockService.StockOperation;
import com.mall.product.service.StockService.StockOperationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存管理控制器
 * 提供库存监控、预警、扣减回滚等API接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@RestController
@RequestMapping("/stock")
public class StockController {
    
    private static final Logger logger = LoggerFactory.getLogger(StockController.class);
    
    @Autowired
    private StockService stockService;
    
    /**
     * 获取实时库存监控数据
     * 
     * @return 库存监控数据
     */
    @GetMapping("/monitor")
    public Object getStockMonitorData() {
        logger.info("获取实时库存监控数据");
        
        try {
            Object monitorData = stockService.getStockMonitorData();
            return createSuccessResponse(monitorData);
        } catch (Exception e) {
            logger.error("获取库存监控数据失败", e);
            return createErrorResponse("获取库存监控数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取库存预警商品列表
     * 
     * @param warningLevel 预警级别 1-低库存 2-缺货
     * @return 预警商品列表
     */
    @GetMapping("/warning")
    public Object getStockWarningProducts(@RequestParam(defaultValue = "1") Integer warningLevel) {
        logger.info("获取库存预警商品列表 - 预警级别: {}", warningLevel);
        
        try {
            List<Product> warningProducts = stockService.getStockWarningProducts(warningLevel);
            return createSuccessResponse(warningProducts);
        } catch (Exception e) {
            logger.error("获取库存预警商品列表失败", e);
            return createErrorResponse("获取库存预警商品列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 库存扣减
     * 
     * @param productId 商品ID
     * @param skuId SKU ID（可选）
     * @param quantity 扣减数量
     * @param orderNo 订单号
     * @param operatorId 操作员ID
     * @return 扣减结果
     */
    @PostMapping("/deduct")
    public Object deductStock(@RequestParam Long productId,
                             @RequestParam(required = false) Long skuId,
                             @RequestParam Integer quantity,
                             @RequestParam String orderNo,
                             @RequestParam(defaultValue = "1") Long operatorId) {
        logger.info("库存扣减 - 商品ID: {}, SKU ID: {}, 数量: {}, 订单号: {}", productId, skuId, quantity, orderNo);
        
        try {
            StockOperationResult result = stockService.deductStock(productId, skuId, quantity, orderNo, operatorId);
            
            if (result.isSuccess()) {
                return createSuccessResponse(result, "库存扣减成功");
            } else {
                return createErrorResponse(result.getMessage());
            }
        } catch (Exception e) {
            logger.error("库存扣减失败", e);
            return createErrorResponse("库存扣减失败: " + e.getMessage());
        }
    }
    
    /**
     * 库存回滚
     * 
     * @param productId 商品ID
     * @param skuId SKU ID（可选）
     * @param quantity 回滚数量
     * @param orderNo 订单号
     * @param operatorId 操作员ID
     * @return 回滚结果
     */
    @PostMapping("/rollback")
    public Object rollbackStock(@RequestParam Long productId,
                               @RequestParam(required = false) Long skuId,
                               @RequestParam Integer quantity,
                               @RequestParam String orderNo,
                               @RequestParam(defaultValue = "1") Long operatorId) {
        logger.info("库存回滚 - 商品ID: {}, SKU ID: {}, 数量: {}, 订单号: {}", productId, skuId, quantity, orderNo);
        
        try {
            StockOperationResult result = stockService.rollbackStock(productId, skuId, quantity, orderNo, operatorId);
            
            if (result.isSuccess()) {
                return createSuccessResponse(result, "库存回滚成功");
            } else {
                return createErrorResponse(result.getMessage());
            }
        } catch (Exception e) {
            logger.error("库存回滚失败", e);
            return createErrorResponse("库存回滚失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量库存扣减
     * 
     * @param stockOperations 库存操作列表
     * @return 批量扣减结果
     */
    @PostMapping("/batch/deduct")
    public Object batchDeductStock(@RequestBody List<StockOperation> stockOperations) {
        logger.info("批量库存扣减 - 操作数量: {}", stockOperations.size());
        
        try {
            BatchStockOperationResult result = stockService.batchDeductStock(stockOperations);
            
            if (result.isSuccess()) {
                return createSuccessResponse(result, "批量库存扣减成功");
            } else {
                return createErrorResponse(result.getMessage());
            }
        } catch (Exception e) {
            logger.error("批量库存扣减失败", e);
            return createErrorResponse("批量库存扣减失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量库存回滚
     * 
     * @param stockOperations 库存操作列表
     * @return 批量回滚结果
     */
    @PostMapping("/batch/rollback")
    public Object batchRollbackStock(@RequestBody List<StockOperation> stockOperations) {
        logger.info("批量库存回滚 - 操作数量: {}", stockOperations.size());
        
        try {
            BatchStockOperationResult result = stockService.batchRollbackStock(stockOperations);
            
            if (result.isSuccess()) {
                return createSuccessResponse(result, "批量库存回滚成功");
            } else {
                return createErrorResponse(result.getMessage());
            }
        } catch (Exception e) {
            logger.error("批量库存回滚失败", e);
            return createErrorResponse("批量库存回滚失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取库存变更日志
     * 
     * @param productId 商品ID（可选）
     * @param skuId SKU ID（可选）
     * @param current 当前页码
     * @param size 页面大小
     * @return 库存变更日志
     */
    @GetMapping("/logs")
    public Object getStockLogs(@RequestParam(required = false) Long productId,
                              @RequestParam(required = false) Long skuId,
                              @RequestParam(defaultValue = "1") Long current,
                              @RequestParam(defaultValue = "10") Long size) {
        logger.info("获取库存变更日志 - 商品ID: {}, SKU ID: {}, 页码: {}, 大小: {}", productId, skuId, current, size);
        
        try {
            Object stockLogs = stockService.getStockLogs(productId, skuId, current, size);
            return createSuccessResponse(stockLogs);
        } catch (Exception e) {
            logger.error("获取库存变更日志失败", e);
            return createErrorResponse("获取库存变更日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 库存盘点
     * 
     * @param productId 商品ID
     * @param skuId SKU ID（可选）
     * @param actualStock 实际库存
     * @param operatorId 操作员ID
     * @param reason 盘点原因
     * @return 盘点结果
     */
    @PostMapping("/taking")
    public Object stockTaking(@RequestParam Long productId,
                             @RequestParam(required = false) Long skuId,
                             @RequestParam Integer actualStock,
                             @RequestParam(defaultValue = "1") Long operatorId,
                             @RequestParam(required = false) String reason) {
        logger.info("库存盘点 - 商品ID: {}, SKU ID: {}, 实际库存: {}, 原因: {}", productId, skuId, actualStock, reason);
        
        try {
            StockOperationResult result = stockService.stockTaking(productId, skuId, actualStock, operatorId, reason);
            
            if (result.isSuccess()) {
                return createSuccessResponse(result, "库存盘点成功");
            } else {
                return createErrorResponse(result.getMessage());
            }
        } catch (Exception e) {
            logger.error("库存盘点失败", e);
            return createErrorResponse("库存盘点失败: " + e.getMessage());
        }
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 创建成功响应
     */
    private Map<String, Object> createSuccessResponse(Object data) {
        return createSuccessResponse(data, "操作成功");
    }
    
    /**
     * 创建成功响应
     */
    private Map<String, Object> createSuccessResponse(Object data, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", message);
        response.put("data", data);
        response.put("success", true);
        return response;
    }
    
    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 500);
        response.put("message", message);
        response.put("data", null);
        response.put("success", false);
        return response;
    }
}