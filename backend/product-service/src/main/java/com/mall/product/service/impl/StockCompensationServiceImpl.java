package com.mall.product.service.impl;

import com.mall.product.service.StockCompensationService;
import com.mall.product.service.StockService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 库存补偿服务实现类
 * 提供分布式事务场景下的库存补偿机制
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Service
public class StockCompensationServiceImpl implements StockCompensationService {
    
    private static final Logger logger = LoggerFactory.getLogger(StockCompensationServiceImpl.class);
    
    @Autowired
    private StockService stockService;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    // 异步执行器
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(5);
    
    // 网络异常处理器
    private final Map<String, Integer> networkRetryCount = new ConcurrentHashMap<>();
    
    // 模拟数据存储
    private static final Map<Long, StockCompensationRecord> COMPENSATION_CACHE = new ConcurrentHashMap<>();
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);
    
    // 最大重试次数
    private static final int MAX_RETRY_COUNT = 3;
    private static final int NETWORK_MAX_RETRY = 5;
    private static final long NETWORK_RETRY_DELAY = 2000L; // 2秒
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCompensationRecord(Long productId, Long skuId, Integer quantity, 
                                       String orderNo, String operationType, Long operatorId) {
        logger.info("创建库存补偿记录 - 商品ID: {}, SKU ID: {}, 数量: {}, 订单号: {}, 操作类型: {}", 
            productId, skuId, quantity, orderNo, operationType);
        
        try {
            // 参数验证
            if (productId == null) {
                throw new IllegalArgumentException("商品ID不能为空");
            }
            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("数量必须大于0");
            }
            if (orderNo == null || orderNo.trim().isEmpty()) {
                throw new IllegalArgumentException("订单号不能为空");
            }
            if (operationType == null || (!operationType.equals("DEDUCT") && !operationType.equals("ROLLBACK"))) {
                throw new IllegalArgumentException("操作类型必须是DEDUCT或ROLLBACK");
            }
            
            Long compensationId = ID_GENERATOR.getAndIncrement();
            
            StockCompensationRecord record = new StockCompensationRecord();
            record.setId(compensationId);
            record.setProductId(productId);
            record.setSkuId(skuId);
            record.setQuantity(quantity);
            record.setOrderNo(orderNo);
            record.setOperationType(operationType);
            record.setStatus("PENDING");
            record.setRetryCount(0);
            record.setMaxRetryCount(MAX_RETRY_COUNT);
            record.setOperatorId(operatorId);
            record.setCreateTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());
            
            COMPENSATION_CACHE.put(compensationId, record);
            
            logger.info("库存补偿记录创建成功 - 补偿ID: {}", compensationId);
            return compensationId;
            
        } catch (Exception e) {
            logger.error("创建库存补偿记录失败", e);
            throw e;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompensationResult executeCompensation(Long compensationId) {
        logger.info("执行库存补偿 - 补偿ID: {}", compensationId);
        
        try {
            // 参数验证
            if (compensationId == null) {
                return new CompensationResult(false, "补偿ID不能为空");
            }
            
            StockCompensationRecord record = COMPENSATION_CACHE.get(compensationId);
            if (record == null) {
                return new CompensationResult(false, "补偿记录不存在");
            }
            
            // 检查补偿状态
            if ("SUCCESS".equals(record.getStatus())) {
                return new CompensationResult(true, "补偿已完成", compensationId, "SUCCESS");
            }
            
            if ("CANCELLED".equals(record.getStatus())) {
                return new CompensationResult(false, "补偿已取消", compensationId, "CANCELLED");
            }
            
            // 检查重试次数
            if (record.getRetryCount() >= record.getMaxRetryCount()) {
                record.setStatus("FAILED");
                record.setFailReason("超过最大重试次数");
                record.setUpdateTime(LocalDateTime.now());
                return new CompensationResult(false, "补偿失败：超过最大重试次数", compensationId, "FAILED");
            }
            
            // 增加重试次数
            record.setRetryCount(record.getRetryCount() + 1);
            record.setUpdateTime(LocalDateTime.now());
            
            // 执行库存操作
            StockService.StockOperationResult stockResult;
            if ("DEDUCT".equals(record.getOperationType())) {
                stockResult = stockService.deductStock(
                    record.getProductId(),
                    record.getSkuId(),
                    record.getQuantity(),
                    record.getOrderNo(),
                    record.getOperatorId()
                );
            } else if ("ROLLBACK".equals(record.getOperationType())) {
                stockResult = stockService.rollbackStock(
                    record.getProductId(),
                    record.getSkuId(),
                    record.getQuantity(),
                    record.getOrderNo(),
                    record.getOperatorId()
                );
            } else {
                record.setStatus("FAILED");
                record.setFailReason("未知的操作类型: " + record.getOperationType());
                record.setUpdateTime(LocalDateTime.now());
                return new CompensationResult(false, "补偿失败：未知的操作类型", compensationId, "FAILED");
            }
            
            // 更新补偿记录状态
            if (stockResult.isSuccess()) {
                record.setStatus("SUCCESS");
                record.setExecuteTime(LocalDateTime.now());
                record.setUpdateTime(LocalDateTime.now());
                
                logger.info("库存补偿执行成功 - 补偿ID: {}, 操作类型: {}", compensationId, record.getOperationType());
                return new CompensationResult(true, "补偿成功", compensationId, "SUCCESS");
            } else {
                record.setFailReason(stockResult.getMessage());
                record.setUpdateTime(LocalDateTime.now());
                
                // 如果还有重试机会，保持PENDING状态
                if (record.getRetryCount() < record.getMaxRetryCount()) {
                    logger.warn("库存补偿执行失败，将重试 - 补偿ID: {}, 失败原因: {}, 重试次数: {}/{}", 
                        compensationId, stockResult.getMessage(), record.getRetryCount(), record.getMaxRetryCount());
                    return new CompensationResult(false, "补偿失败，将重试: " + stockResult.getMessage(), compensationId, "PENDING");
                } else {
                    record.setStatus("FAILED");
                    logger.error("库存补偿执行失败，已达最大重试次数 - 补偿ID: {}, 失败原因: {}", 
                        compensationId, stockResult.getMessage());
                    return new CompensationResult(false, "补偿失败: " + stockResult.getMessage(), compensationId, "FAILED");
                }
            }
            
        } catch (Exception e) {
            logger.error("执行库存补偿异常 - 补偿ID: {}", compensationId, e);
            
            // 更新记录状态
            StockCompensationRecord record = COMPENSATION_CACHE.get(compensationId);
            if (record != null) {
                record.setFailReason("执行异常: " + e.getMessage());
                record.setUpdateTime(LocalDateTime.now());
                
                if (record.getRetryCount() >= record.getMaxRetryCount()) {
                    record.setStatus("FAILED");
                }
            }
            
            return new CompensationResult(false, "补偿异常: " + e.getMessage(), compensationId, "FAILED");
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchCompensationResult batchExecuteCompensation(List<Long> compensationIds) {
        logger.info("批量执行库存补偿 - 补偿数量: {}", compensationIds != null ? compensationIds.size() : 0);
        
        try {
            // 参数验证
            if (compensationIds == null || compensationIds.isEmpty()) {
                return new BatchCompensationResult(false, "补偿ID列表不能为空", 0, 0, 0);
            }
            
            List<CompensationResult> results = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;
            
            for (Long compensationId : compensationIds) {
                try {
                    if (compensationId == null) {
                        results.add(new CompensationResult(false, "补偿ID为空"));
                        failCount++;
                        continue;
                    }
                    
                    CompensationResult result = executeCompensation(compensationId);
                    results.add(result);
                    
                    if (result.isSuccess()) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    logger.error("批量补偿执行失败 - 补偿ID: {}", compensationId, e);
                    results.add(new CompensationResult(false, "补偿异常: " + e.getMessage()));
                    failCount++;
                }
            }
            
            BatchCompensationResult batchResult = new BatchCompensationResult(
                failCount == 0,
                String.format("批量补偿完成，成功: %d, 失败: %d", successCount, failCount),
                compensationIds.size(),
                successCount,
                failCount
            );
            batchResult.setResults(results);
            
            logger.info("批量库存补偿完成 - 总数: {}, 成功: {}, 失败: {}", 
                compensationIds.size(), successCount, failCount);
            
            return batchResult;
            
        } catch (Exception e) {
            logger.error("批量执行库存补偿异常", e);
            return new BatchCompensationResult(false, "批量补偿异常: " + e.getMessage(), 0, 0, 0);
        }
    }
    
    @Override
    public List<StockCompensationRecord> getPendingCompensations(String status, 
                                                               LocalDateTime startTime, 
                                                               LocalDateTime endTime) {
        logger.info("查询待补偿记录 - 状态: {}, 开始时间: {}, 结束时间: {}", status, startTime, endTime);
        
        try {
            return COMPENSATION_CACHE.values().stream()
                .filter(record -> {
                    if (record == null) {
                        return false;
                    }
                    
                    // 状态过滤
                    if (status != null && !status.equals(record.getStatus())) {
                        return false;
                    }
                    
                    // 时间范围过滤
                    if (startTime != null && record.getCreateTime() != null && 
                        record.getCreateTime().isBefore(startTime)) {
                        return false;
                    }
                    
                    if (endTime != null && record.getCreateTime() != null && 
                        record.getCreateTime().isAfter(endTime)) {
                        return false;
                    }
                    
                    return true;
                })
                .sorted(Comparator.comparing(StockCompensationRecord::getCreateTime).reversed())
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            logger.error("查询待补偿记录失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelCompensation(Long compensationId, String reason) {
        logger.info("取消补偿记录 - 补偿ID: {}, 原因: {}", compensationId, reason);
        
        try {
            // 参数验证
            if (compensationId == null) {
                logger.error("取消补偿失败：补偿ID不能为空");
                return false;
            }
            
            StockCompensationRecord record = COMPENSATION_CACHE.get(compensationId);
            if (record == null) {
                logger.error("取消补偿失败：补偿记录不存在 - 补偿ID: {}", compensationId);
                return false;
            }
            
            // 检查当前状态
            if ("SUCCESS".equals(record.getStatus())) {
                logger.warn("补偿记录已成功，无法取消 - 补偿ID: {}", compensationId);
                return false;
            }
            
            if ("CANCELLED".equals(record.getStatus())) {
                logger.warn("补偿记录已取消 - 补偿ID: {}", compensationId);
                return true;
            }
            
            // 更新状态
            record.setStatus("CANCELLED");
            record.setFailReason(reason != null ? reason : "手动取消");
            record.setUpdateTime(LocalDateTime.now());
            
            logger.info("补偿记录取消成功 - 补偿ID: {}", compensationId);
            return true;
            
        } catch (Exception e) {
            logger.error("取消补偿记录异常 - 补偿ID: {}", compensationId, e);
            return false;
        }
    }
    
    /**
     * 异步处理待补偿记录
     * 定时任务会调用此方法处理PENDING状态的补偿记录
     */
    @Async
    public void processPendingCompensations() {
        logger.info("开始处理待补偿记录");
        
        try {
            List<StockCompensationRecord> pendingRecords = getPendingCompensations("PENDING", null, null);
            
            if (pendingRecords.isEmpty()) {
                logger.debug("没有待处理的补偿记录");
                return;
            }
            
            logger.info("发现 {} 条待处理的补偿记录", pendingRecords.size());
            
            for (StockCompensationRecord record : pendingRecords) {
                try {
                    // 检查是否超过创建时间阈值（例如：创建超过1小时的记录才处理）
                    if (record.getCreateTime().isBefore(LocalDateTime.now().minusHours(1))) {
                        executeCompensation(record.getId());
                    }
                } catch (Exception e) {
                    logger.error("处理补偿记录异常 - 补偿ID: {}", record.getId(), e);
                }
            }
            
        } catch (Exception e) {
            logger.error("处理待补偿记录异常", e);
        }
    }
    
    /**
     * 网络异常处理方法
     * 处理网络连接超时、连接中断等异常情况
     */
    public CompensationResult handleNetworkException(Exception ex, Long compensationId) {
        logger.error("网络异常处理 - 补偿ID: {}, 异常: {}", compensationId, ex.getMessage());
        
        String key = "compensation_" + compensationId;
        int retryCount = networkRetryCount.getOrDefault(key, 0);
        
        if (retryCount < NETWORK_MAX_RETRY) {
            // 增加重试计数
            networkRetryCount.put(key, retryCount + 1);
            
            // 延迟重试
            scheduledExecutor.schedule(() -> {
                try {
                    logger.info("网络异常重试 - 补偿ID: {}, 重试次数: {}", compensationId, retryCount + 1);
                    executeCompensation(compensationId);
                } catch (Exception e) {
                    logger.error("网络异常重试失败 - 补偿ID: {}", compensationId, e);
                }
            }, NETWORK_RETRY_DELAY, TimeUnit.MILLISECONDS);
            
            return new CompensationResult(false, "网络异常，已安排重试", compensationId, "RETRYING");
        } else {
            // 超过最大重试次数，标记为失败
            networkRetryCount.remove(key);
            
            StockCompensationRecord record = COMPENSATION_CACHE.get(compensationId);
            if (record != null) {
                record.setStatus("FAILED");
                record.setFailReason("网络异常，超过最大重试次数: " + ex.getMessage());
                record.setUpdateTime(LocalDateTime.now());
            }
            
            return new CompensationResult(false, "网络异常，超过最大重试次数", compensationId, "FAILED");
        }
    }
    
    /**
     * 定时清理过期的补偿记录
     * 每小时执行一次，清理超过24小时的已完成或已取消记录
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void cleanupExpiredRecords() {
        logger.info("开始清理过期的补偿记录");
        
        try {
            LocalDateTime expireTime = LocalDateTime.now().minusHours(24);
            List<Long> expiredIds = new ArrayList<>();
            
            for (Map.Entry<Long, StockCompensationRecord> entry : COMPENSATION_CACHE.entrySet()) {
                StockCompensationRecord record = entry.getValue();
                if (record != null && record.getUpdateTime() != null && 
                    record.getUpdateTime().isBefore(expireTime) &&
                    ("SUCCESS".equals(record.getStatus()) || "CANCELLED".equals(record.getStatus()))) {
                    expiredIds.add(entry.getKey());
                }
            }
            
            for (Long id : expiredIds) {
                COMPENSATION_CACHE.remove(id);
                networkRetryCount.remove("compensation_" + id);
            }
            
            if (!expiredIds.isEmpty()) {
                logger.info("清理过期补偿记录完成，清理数量: {}", expiredIds.size());
            }
            
        } catch (Exception e) {
            logger.error("清理过期补偿记录异常", e);
        }
    }
    
    /**
     * 异步执行补偿操作
     * 用于处理大批量补偿操作，避免阻塞主线程
     */
    @Async
    public CompletableFuture<CompensationResult> executeCompensationAsync(Long compensationId) {
        logger.info("异步执行库存补偿 - 补偿ID: {}", compensationId);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeCompensation(compensationId);
            } catch (Exception e) {
                logger.error("异步执行补偿异常 - 补偿ID: {}", compensationId, e);
                return new CompensationResult(false, "异步执行异常: " + e.getMessage(), compensationId, "FAILED");
            }
        });
    }
    
    /**
     * 数据库操作失败回滚处理
     * 当数据库操作失败时，执行相应的回滚操作
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void handleDatabaseRollback(Long compensationId, String errorMessage) {
        logger.warn("处理数据库回滚 - 补偿ID: {}, 错误信息: {}", compensationId, errorMessage);
        
        try {
            StockCompensationRecord record = COMPENSATION_CACHE.get(compensationId);
            if (record != null) {
                // 如果是扣减操作失败，创建回滚补偿记录
                if ("DEDUCT".equals(record.getOperationType())) {
                    Long rollbackId = createCompensationRecord(
                        record.getProductId(),
                        record.getSkuId(),
                        record.getQuantity(),
                        record.getOrderNo(),
                        "ROLLBACK",
                        record.getOperatorId()
                    );
                    
                    logger.info("创建回滚补偿记录 - 原补偿ID: {}, 回滚补偿ID: {}", compensationId, rollbackId);
                }
                
                // 更新原记录状态
                record.setStatus("FAILED");
                record.setFailReason("数据库操作失败: " + errorMessage);
                record.setUpdateTime(LocalDateTime.now());
            }
            
        } catch (Exception e) {
            logger.error("处理数据库回滚异常 - 补偿ID: {}", compensationId, e);
        }
    }
}