package com.mall.product.service.impl;

import com.mall.product.domain.entity.Product;
import com.mall.product.domain.entity.ProductSku;
import com.mall.product.domain.entity.StockLog;
import com.mall.product.service.StockService;
import com.mall.product.service.AuditLogService;
import com.mall.product.util.RedisDistributedLock;
import com.mall.product.util.OptimisticLockHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.Date;

/**
 * 库存管理服务实现类
 * 提供库存监控、预警、扣减回滚等功能
 * 支持事务性操作和分布式事务补偿机制
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Service
public class StockServiceImpl implements StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockServiceImpl.class);

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private RedisDistributedLock distributedLock;

    @Autowired
    private OptimisticLockHelper optimisticLockHelper;

    // 模拟数据存储
    private static final Map<Long, Product> PRODUCT_CACHE = new ConcurrentHashMap<>();
    private static final Map<Long, List<ProductSku>> SKU_CACHE = new ConcurrentHashMap<>();
    private static final List<StockLog> STOCK_LOG_CACHE = Collections.synchronizedList(new ArrayList<>());

    // 库存操作锁，防止并发问题
    private static final Map<String, ReentrantLock> STOCK_LOCKS = new ConcurrentHashMap<>();

    // 事务回滚记录，用于分布式事务补偿
    private static final Map<String, TransactionRollbackRecord> TRANSACTION_ROLLBACK_CACHE = new ConcurrentHashMap<>();

    static {
        // 初始化模拟数据
        initMockData();
    }

    /**
     * 实时库存监控
     */
    @Override
    public Object getStockMonitorData() {
        logger.info("获取实时库存监控数据");

        try {
            Map<String, Object> monitorData = new HashMap<>();

            // 统计总商品数
            int totalProducts = PRODUCT_CACHE.size();

            // 统计库存预警商品数
            long lowStockCount = PRODUCT_CACHE.values().stream()
                    .filter(p -> p.getStock() <= p.getStockWarning())
                    .count();

            // 统计缺货商品数
            long outOfStockCount = PRODUCT_CACHE.values().stream()
                    .filter(p -> p.getStock() <= 0)
                    .count();

            // 计算库存总值
            double totalStockValue = PRODUCT_CACHE.values().stream()
                    .mapToDouble(p -> p.getStock() * p.getPrice())
                    .sum();

            // 最近24小时库存变动
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            long recentStockChanges = STOCK_LOG_CACHE.stream()
                    .filter(log -> log.getCreateTime().isAfter(yesterday))
                    .count();

            monitorData.put("totalProducts", totalProducts);
            monitorData.put("lowStockCount", lowStockCount);
            monitorData.put("outOfStockCount", outOfStockCount);
            monitorData.put("totalStockValue", totalStockValue);
            monitorData.put("recentStockChanges", recentStockChanges);
            monitorData.put("updateTime", LocalDateTime.now());

            return monitorData;
        } catch (Exception e) {
            logger.error("获取库存监控数据失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 获取库存预警商品列表
     */
    @Override
    public List<Product> getStockWarningProducts(Integer warningLevel) {
        logger.info("获取库存预警商品列表 - 预警级别: {}", warningLevel);

        try {
            // 参数验证
            if (warningLevel == null) {
                logger.warn("预警级别参数为空");
                return new ArrayList<>();
            }

            return PRODUCT_CACHE.values().stream()
                    .filter(product -> {
                        // 空值检查
                        if (product == null || product.getStock() == null || product.getStockWarning() == null) {
                            logger.warn("商品数据不完整，跳过处理 - 商品ID: {}", product != null ? product.getId() : "null");
                            return false;
                        }

                        if (warningLevel == 1) {
                            // 低库存：库存小于等于预警值但大于0
                            return product.getStock() <= product.getStockWarning() && product.getStock() > 0;
                        } else if (warningLevel == 2) {
                            // 缺货：库存为0
                            return product.getStock() <= 0;
                        }
                        return false;
                    })
                    .sorted(Comparator.comparing(Product::getStock))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取库存预警商品列表失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 库存扣减（支持事务回滚）
     */
    /**
     * 库存扣减（支持事务回滚）
     * 增强参数验证和异常处理，防止空指针异常
     * 
     * @param productId  商品ID
     * @param skuId      SKU ID（可为null，表示操作商品库存）
     * @param quantity   扣减数量
     * @param orderNo    订单号
     * @param operatorId 操作员ID
     * @return 库存操作结果
     * @author lingbai
     * @since 2025-01-21
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public StockOperationResult deductStock(Long productId, Long skuId, Integer quantity, String orderNo,
            Long operatorId) {
        logger.info("库存扣减 - 商品ID: {}, SKU ID: {}, 数量: {}, 订单号: {}", productId, skuId, quantity, orderNo);

        // 增强参数验证，防止空指针异常
        if (productId == null) {
            logger.error("库存扣减失败：商品ID不能为空");
            return new StockOperationResult(false, "商品ID不能为空");
        }

        if (quantity == null || quantity <= 0) {
            logger.error("库存扣减失败：扣减数量无效 - 数量: {}", quantity);
            return new StockOperationResult(false, "扣减数量必须大于0");
        }

        if (orderNo == null || orderNo.trim().isEmpty()) {
            logger.error("库存扣减失败：订单号不能为空");
            return new StockOperationResult(false, "订单号不能为空");
        }

        if (operatorId == null) {
            logger.warn("操作员ID为空，使用默认值");
            operatorId = 0L; // 使用默认操作员ID
        }

        // 使用Redis分布式锁，提高并发安全性
        String lockKey = "stock_deduct_" + productId + "_" + (skuId != null ? skuId : "0");
        RedisDistributedLock.DistributedLockResult lockResult = distributedLock.tryLock(lockKey, 30, 5000);

        if (!lockResult.isLocked()) {
            logger.error("获取库存扣减锁失败 - 商品ID: {}, SKU ID: {}", productId, skuId);
            return new StockOperationResult(false, "系统繁忙，请稍后重试");
        }

        try {
            // 记录事务操作，用于回滚
            String transactionId = getTransactionId();
            if (transactionId != null) {
                recordTransactionOperation(transactionId, productId, skuId, quantity, orderNo, "DEDUCT", operatorId);
            }

            StockOperationResult result;
            if (skuId != null) {
                // SKU库存扣减
                result = deductSkuStock(productId, skuId, quantity, orderNo, operatorId);
            } else {
                // 商品库存扣减
                result = deductProductStock(productId, quantity, orderNo, operatorId);
            }

            // 如果扣减失败，清理事务记录
            if (!result.isSuccess() && transactionId != null) {
                removeTransactionOperation(transactionId, productId, skuId, orderNo);
            }

            logger.info("库存扣减完成 - 商品ID: {}, SKU ID: {}, 结果: {}",
                    productId, skuId, result.isSuccess() ? "成功" : "失败");
            return result;

        } catch (Exception e) {
            logger.error("库存扣减异常 - 商品ID: {}, SKU ID: {}, 数量: {}", productId, skuId, quantity, e);

            // 异常时清理事务记录
            String transactionId = getTransactionId();
            if (transactionId != null) {
                removeTransactionOperation(transactionId, productId, skuId, orderNo);
            }

            return new StockOperationResult(false, "库存扣减异常: " + e.getMessage());
        } finally {
            // 释放分布式锁
            distributedLock.unlock(lockResult);
        }
    }

    /**
     * 库存回滚
     * 增强参数验证和异常处理，防止空指针异常
     * 
     * @param productId  商品ID
     * @param skuId      SKU ID（可为null，表示操作商品库存）
     * @param quantity   回滚数量
     * @param orderNo    订单号
     * @param operatorId 操作员ID
     * @return 库存操作结果
     * @author lingbai
     * @since 2025-01-21
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public StockOperationResult rollbackStock(Long productId, Long skuId, Integer quantity, String orderNo,
            Long operatorId) {
        logger.info("库存回滚 - 商品ID: {}, SKU ID: {}, 数量: {}, 订单号: {}", productId, skuId, quantity, orderNo);

        // 增强参数验证，防止空指针异常
        if (productId == null) {
            logger.error("库存回滚失败：商品ID不能为空");
            return new StockOperationResult(false, "商品ID不能为空");
        }

        if (quantity == null || quantity <= 0) {
            logger.error("库存回滚失败：回滚数量无效 - 数量: {}", quantity);
            return new StockOperationResult(false, "回滚数量必须大于0");
        }

        if (orderNo == null || orderNo.trim().isEmpty()) {
            logger.error("库存回滚失败：订单号不能为空");
            return new StockOperationResult(false, "订单号不能为空");
        }

        if (operatorId == null) {
            logger.warn("操作员ID为空，使用默认值");
            operatorId = 0L; // 使用默认操作员ID
        }

        // 使用Redis分布式锁，保证回滚操作的原子性
        String lockKey = "stock_rollback_" + productId + "_" + (skuId != null ? skuId : "0");
        RedisDistributedLock.DistributedLockResult lockResult = distributedLock.tryLock(lockKey, 30, 5000);

        if (!lockResult.isLocked()) {
            logger.error("获取库存回滚锁失败 - 商品ID: {}, SKU ID: {}", productId, skuId);
            return new StockOperationResult(false, "系统繁忙，请稍后重试");
        }

        try {
            // 记录事务操作，用于补偿
            String transactionId = getTransactionId();
            if (transactionId != null) {
                recordTransactionOperation(transactionId, productId, skuId, quantity, orderNo, "ROLLBACK", operatorId);
            }

            StockOperationResult result;
            if (skuId != null) {
                // SKU库存回滚
                result = rollbackSkuStock(productId, skuId, quantity, orderNo, operatorId);
            } else {
                // 商品库存回滚
                result = rollbackProductStock(productId, quantity, orderNo, operatorId);
            }

            // 如果回滚失败，清理事务记录
            if (!result.isSuccess() && transactionId != null) {
                removeTransactionOperation(transactionId, productId, skuId, orderNo);
            }

            logger.info("库存回滚完成 - 商品ID: {}, SKU ID: {}, 结果: {}",
                    productId, skuId, result.isSuccess() ? "成功" : "失败");
            return result;

        } catch (Exception e) {
            logger.error("库存回滚异常 - 商品ID: {}, SKU ID: {}, 数量: {}", productId, skuId, quantity, e);

            // 异常时清理事务记录
            String transactionId = getTransactionId();
            if (transactionId != null) {
                removeTransactionOperation(transactionId, productId, skuId, orderNo);
            }

            return new StockOperationResult(false, "库存回滚异常: " + e.getMessage());
        } finally {
            // 释放分布式锁
            distributedLock.unlock(lockResult);
        }
    }

    /**
     * 批量库存扣减（支持事务）
     * 增强异常处理和空值检查，确保事务一致性
     * 
     * @param stockOperations 库存操作列表
     * @return 批量操作结果
     * @author lingbai
     * @since 2025-01-21
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public BatchStockOperationResult batchDeductStock(List<StockOperation> stockOperations) {
        logger.info("批量库存扣减 - 操作数量: {}", stockOperations != null ? stockOperations.size() : 0);

        // 增强参数验证
        if (stockOperations == null || stockOperations.isEmpty()) {
            logger.error("批量库存扣减失败：操作列表为空");
            return new BatchStockOperationResult(false, "操作列表不能为空", 0, 0, 0);
        }

        // 预检查所有操作的有效性
        for (int i = 0; i < stockOperations.size(); i++) {
            StockOperation operation = stockOperations.get(i);
            if (operation == null) {
                logger.error("批量库存扣减失败：第{}个操作对象为空", i + 1);
                return new BatchStockOperationResult(false,
                        String.format("第%d个操作对象为空", i + 1), 0, 0, stockOperations.size());
            }

            if (operation.getProductId() == null) {
                logger.error("批量库存扣减失败：第{}个操作的商品ID为空", i + 1);
                return new BatchStockOperationResult(false,
                        String.format("第%d个操作的商品ID为空", i + 1), 0, 0, stockOperations.size());
            }

            if (operation.getQuantity() == null || operation.getQuantity() <= 0) {
                logger.error("批量库存扣减失败：第{}个操作的数量无效 - 数量: {}", i + 1, operation.getQuantity());
                return new BatchStockOperationResult(false,
                        String.format("第%d个操作的数量无效", i + 1), 0, 0, stockOperations.size());
            }

            if (operation.getOrderNo() == null || operation.getOrderNo().trim().isEmpty()) {
                logger.error("批量库存扣减失败：第{}个操作的订单号为空", i + 1);
                return new BatchStockOperationResult(false,
                        String.format("第%d个操作的订单号为空", i + 1), 0, 0, stockOperations.size());
            }
        }

        List<StockOperationResult> results = new ArrayList<>();
        List<StockOperation> successOperations = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;
        String batchId = "BATCH_" + System.currentTimeMillis();

        String transactionId = getTransactionId();

        logger.info("开始批量库存扣减 - 批次ID: {}, 操作数量: {}", batchId, stockOperations.size());

        try {
            for (int i = 0; i < stockOperations.size(); i++) {
                StockOperation operation = stockOperations.get(i);
                try {
                    // 验证操作对象
                    if (operation == null) {
                        logger.warn("跳过空的库存操作对象");
                        results.add(new StockOperationResult(false, "操作对象为空"));
                        failCount++;
                        continue;
                    }

                    logger.debug("执行第{}个库存扣减操作 - 商品ID: {}, SKU ID: {}, 数量: {}",
                            i + 1, operation.getProductId(), operation.getSkuId(), operation.getQuantity());

                    StockOperationResult result = deductStock(
                            operation.getProductId(),
                            operation.getSkuId(),
                            operation.getQuantity(),
                            operation.getOrderNo(),
                            operation.getOperatorId());
                    results.add(result);

                    if (result.isSuccess()) {
                        successOperations.add(operation);
                        successCount++;
                        logger.debug("第{}个库存扣减操作成功", i + 1);
                    } else {
                        failCount++;
                        // 如果有失败的操作，回滚之前成功的操作
                        if (!successOperations.isEmpty()) {
                            logger.warn("批量库存扣减中断 - 批次ID: {}, 第{}个操作失败: {}",
                                    batchId, i + 1, result.getMessage());
                            rollbackSuccessOperations(successOperations);
                        }
                        break; // 遇到失败就停止后续操作
                    }
                } catch (Exception e) {
                    logger.error("批量库存扣减失败 - 商品ID: {}",
                            operation != null ? operation.getProductId() : "null", e);
                    results.add(new StockOperationResult(false, "扣减失败: " + e.getMessage()));
                    failCount++;

                    // 回滚之前成功的操作
                    if (!successOperations.isEmpty()) {
                        rollbackSuccessOperations(successOperations);
                    }
                    break;
                }
            }

            BatchStockOperationResult batchResult = new BatchStockOperationResult(
                    failCount == 0,
                    String.format("批量扣减完成，成功: %d, 失败: %d", successCount, failCount),
                    stockOperations.size(),
                    successCount,
                    failCount);
            batchResult.setResults(results);

            logger.info("批量库存扣减完成 - 批次ID: {}, 成功操作数: {}, 失败操作数: {}",
                    batchId, successCount, failCount);

            return batchResult;

        } catch (Exception e) {
            logger.error("批量库存扣减异常 - 批次ID: {}", batchId, e);

            // 异常时回滚所有成功的操作
            if (!successOperations.isEmpty()) {
                rollbackSuccessOperations(successOperations);
            }

            return new BatchStockOperationResult(false, "批量扣减异常: " + e.getMessage(), 0, 0, stockOperations.size());
        }
    }

    /**
     * 批量库存回滚
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchStockOperationResult batchRollbackStock(List<StockOperation> stockOperations) {
        logger.info("批量库存回滚 - 操作数量: {}", stockOperations.size());

        List<StockOperationResult> results = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        for (StockOperation operation : stockOperations) {
            try {
                StockOperationResult result = rollbackStock(
                        operation.getProductId(),
                        operation.getSkuId(),
                        operation.getQuantity(),
                        operation.getOrderNo(),
                        operation.getOperatorId());
                results.add(result);

                if (result.isSuccess()) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                logger.error("批量库存回滚失败 - 商品ID: {}", operation.getProductId(), e);
                results.add(new StockOperationResult(false, "回滚失败: " + e.getMessage()));
                failCount++;
            }
        }

        BatchStockOperationResult batchResult = new BatchStockOperationResult(
                failCount == 0,
                String.format("批量回滚完成，成功: %d, 失败: %d", successCount, failCount),
                stockOperations.size(),
                successCount,
                failCount);
        batchResult.setResults(results);

        return batchResult;
    }

    /**
     * 获取库存变更日志
     */
    @Override
    public Object getStockLogs(Long productId, Long skuId, Long current, Long size) {
        logger.info("获取库存变更日志 - 商品ID: {}, SKU ID: {}, 页码: {}, 大小: {}", productId, skuId, current, size);

        try {
            List<StockLog> filteredLogs = STOCK_LOG_CACHE.stream()
                    .filter(log -> {
                        if (productId != null && !productId.equals(log.getProductId())) {
                            return false;
                        }
                        if (skuId != null && !skuId.equals(log.getSkuId())) {
                            return false;
                        }
                        return true;
                    })
                    .sorted(Comparator.comparing(StockLog::getCreateTime).reversed())
                    .collect(Collectors.toList());

            // 分页处理
            int start = (int) ((current - 1) * size);
            int end = Math.min(start + size.intValue(), filteredLogs.size());
            List<StockLog> pageLogs = filteredLogs.subList(start, end);

            return createPageData(pageLogs, filteredLogs.size(), current, size);
        } catch (Exception e) {
            logger.error("获取库存变更日志失败", e);
            return createEmptyPageData(current, size);
        }
    }

    /**
     * 库存盘点
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockOperationResult stockTaking(Long productId, Long skuId, Integer actualStock, Long operatorId,
            String reason) {
        logger.info("库存盘点 - 商品ID: {}, SKU ID: {}, 实际库存: {}, 原因: {}", productId, skuId, actualStock, reason);

        String lockKey = productId + "_" + (skuId != null ? skuId : "0");
        ReentrantLock lock = STOCK_LOCKS.computeIfAbsent(lockKey, k -> new ReentrantLock());

        lock.lock();
        try {
            if (skuId != null) {
                // SKU库存盘点
                return stockTakingSku(productId, skuId, actualStock, operatorId, reason);
            } else {
                // 商品库存盘点
                return stockTakingProduct(productId, actualStock, operatorId, reason);
            }
        } finally {
            lock.unlock();
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 商品库存扣减（增强空值检查）
     * 
     * @param productId  商品ID
     * @param quantity   扣减数量
     * @param orderNo    订单号
     * @param operatorId 操作员ID
     * @return 库存操作结果
     * @author lingbai
     * @since 2025-01-21
     */
    private StockOperationResult deductProductStock(Long productId, Integer quantity, String orderNo, Long operatorId) {
        // 增强参数验证
        if (productId == null) {
            logger.error("商品库存扣减失败：商品ID不能为空");
            return new StockOperationResult(false, "商品ID不能为空");
        }

        if (quantity == null || quantity <= 0) {
            logger.error("商品库存扣减失败：扣减数量无效 - 数量: {}", quantity);
            return new StockOperationResult(false, "扣减数量必须大于0");
        }

        Product product = PRODUCT_CACHE.get(productId);
        if (product == null) {
            logger.error("商品不存在 - 商品ID: {}", productId);
            return new StockOperationResult(false, "商品不存在");
        }

        // 检查商品库存字段，防止空指针异常
        if (product.getStock() == null) {
            logger.error("商品库存数据异常 - 商品ID: {}, 库存为null", productId);
            // 初始化库存为0
            product.setStock(0);
            logger.warn("已将商品库存初始化为0 - 商品ID: {}", productId);
        }

        // 检查库存是否足够
        if (product.getStock() < quantity) {
            logger.warn("商品库存不足 - 商品ID: {}, 当前库存: {}, 需要扣减: {}",
                    productId, product.getStock(), quantity);
            return new StockOperationResult(false, "库存不足，当前库存: " + product.getStock());
        }

        // 使用乐观锁机制进行库存扣减
        Integer oldStock = product.getStock();

        // 使用OptimisticLockHelper执行乐观锁更新
        OptimisticLockHelper.OptimisticLockResult lockResult = optimisticLockHelper.executeWithOptimisticLock(
                product,
                (p) -> {
                    // 再次检查库存是否足够（防止并发情况下库存变化）
                    if (p.getStock() < quantity) {
                        logger.warn("乐观锁更新时库存不足 - 商品ID: {}, 当前库存: {}, 需要扣减: {}",
                                productId, p.getStock(), quantity);
                        return false;
                    }

                    // 执行库存扣减
                    Integer newStock = p.getStock() - quantity;
                    p.setStock(newStock);

                    logger.debug("乐观锁库存扣减 - 商品ID: {}, 原库存: {}, 新库存: {}",
                            productId, p.getStock() + quantity, newStock);
                    return true;
                });

        if (!lockResult.isSuccess()) {
            logger.error("商品库存扣减失败 - 商品ID: {}, 原因: {}", productId, lockResult.getMessage());
            return new StockOperationResult(false, lockResult.getMessage());
        }

        Integer newStock = product.getStock();

        // 记录库存变更日志
        String productName = product.getName() != null ? product.getName() : "未知商品";
        Long logId = recordStockLog(productId, null, oldStock, newStock, -quantity, "2", "订单扣减",
                orderNo, "系统", productName, null);

        logger.info("商品库存扣减成功 - 商品ID: {}, 原库存: {}, 新库存: {}, 版本: {}",
                productId, oldStock, newStock, product.getVersion());
        return new StockOperationResult(true, "扣减成功", oldStock, newStock, logId);
    }

    /**
     * SKU库存扣减（增强空值检查）
     * 
     * @param productId  商品ID
     * @param skuId      SKU ID
     * @param quantity   扣减数量
     * @param orderNo    订单号
     * @param operatorId 操作员ID
     * @return 库存操作结果
     * @author lingbai
     * @since 2025-01-21
     */
    private StockOperationResult deductSkuStock(Long productId, Long skuId, Integer quantity, String orderNo,
            Long operatorId) {
        // 增强参数验证
        if (productId == null) {
            logger.error("SKU库存扣减失败：商品ID不能为空");
            return new StockOperationResult(false, "商品ID不能为空");
        }

        if (skuId == null) {
            logger.error("SKU库存扣减失败：SKU ID不能为空");
            return new StockOperationResult(false, "SKU ID不能为空");
        }

        if (quantity == null || quantity <= 0) {
            logger.error("SKU库存扣减失败：扣减数量无效 - 数量: {}", quantity);
            return new StockOperationResult(false, "扣减数量必须大于0");
        }

        List<ProductSku> skuList = SKU_CACHE.get(productId);
        if (skuList == null || skuList.isEmpty()) {
            logger.error("商品SKU列表不存在 - 商品ID: {}", productId);
            return new StockOperationResult(false, "商品SKU不存在");
        }

        ProductSku sku = skuList.stream()
                .filter(s -> s != null && skuId.equals(s.getId()))
                .findFirst()
                .orElse(null);

        if (sku == null) {
            logger.error("SKU不存在 - 商品ID: {}, SKU ID: {}", productId, skuId);
            return new StockOperationResult(false, "SKU不存在");
        }

        // 检查SKU库存字段，防止空指针异常
        if (sku.getStock() == null) {
            logger.error("SKU库存数据异常 - 商品ID: {}, SKU ID: {}, 库存为null", productId, skuId);
            // 初始化库存为0
            sku.setStock(0);
            logger.warn("已将SKU库存初始化为0 - 商品ID: {}, SKU ID: {}", productId, skuId);
        }

        // 检查库存是否足够
        if (sku.getStock() < quantity) {
            logger.warn("SKU库存不足 - 商品ID: {}, SKU ID: {}, 当前库存: {}, 需要扣减: {}",
                    productId, skuId, sku.getStock(), quantity);
            return new StockOperationResult(false, "SKU库存不足，当前库存: " + sku.getStock());
        }

        // 使用乐观锁机制进行库存扣减
        Integer oldStock = sku.getStock();

        // 使用OptimisticLockHelper执行乐观锁更新
        OptimisticLockHelper.OptimisticLockResult lockResult = optimisticLockHelper.executeWithOptimisticLock(
                sku,
                (s) -> {
                    // 再次检查库存是否足够（防止并发情况下库存变化）
                    if (s.getStock() < quantity) {
                        logger.warn("乐观锁更新时SKU库存不足 - 商品ID: {}, 当前库存: {}, 需要扣减: {}",
                                productId, skuId, s.getStock(), quantity);
                        return false;
                    }

                    // 执行库存扣减
                    Integer newStock = s.getStock() - quantity;
                    s.setStock(newStock);

                    logger.debug("乐观锁SKU库存扣减 - 商品ID: {}, SKU ID: {}, 原库存: {}, 新库存: {}",
                            productId, skuId, s.getStock() + quantity, newStock);
                    return true;
                });

        if (!lockResult.isSuccess()) {
            logger.error("SKU库存扣减失败 - 商品ID: {}, SKU ID: {}, 原因: {}", productId, skuId, lockResult.getMessage());
            return new StockOperationResult(false, lockResult.getMessage());
        }

        Integer newStock = sku.getStock();

        // 记录库存变更日志
        String productName = sku.getProductName() != null ? sku.getProductName() : "未知商品";
        String skuName = sku.getSkuName() != null ? sku.getSkuName() : "未知SKU";
        Long logId = recordStockLog(productId, skuId, oldStock, newStock, -quantity, "2", "订单扣减",
                orderNo, "系统", productName, skuName);

        logger.info("SKU库存扣减成功 - 商品ID: {}, SKU ID: {}, 原库存: {}, 新库存: {}, 版本: {}",
                productId, skuId, oldStock, newStock, sku.getVersion());
        return new StockOperationResult(true, "扣减成功", oldStock, newStock, logId);
    }

    /**
     * 商品库存回滚（增强空值检查）
     * 
     * @param productId  商品ID
     * @param quantity   回滚数量
     * @param orderNo    订单号
     * @param operatorId 操作员ID
     * @return 库存操作结果
     * @author lingbai
     * @since 2025-01-21
     */
    private StockOperationResult rollbackProductStock(Long productId, Integer quantity, String orderNo,
            Long operatorId) {
        // 增强参数验证
        if (productId == null) {
            logger.error("商品库存回滚失败：商品ID不能为空");
            return new StockOperationResult(false, "商品ID不能为空");
        }

        if (quantity == null || quantity <= 0) {
            logger.error("商品库存回滚失败：回滚数量无效 - 数量: {}", quantity);
            return new StockOperationResult(false, "回滚数量必须大于0");
        }

        Product product = PRODUCT_CACHE.get(productId);
        if (product == null) {
            logger.error("商品不存在 - 商品ID: {}", productId);
            return new StockOperationResult(false, "商品不存在");
        }

        // 检查商品库存字段，防止空指针异常
        if (product.getStock() == null) {
            logger.error("商品库存数据异常 - 商品ID: {}, 库存为null", productId);
            // 初始化库存为0
            product.setStock(0);
            logger.warn("已将商品库存初始化为0 - 商品ID: {}", productId);
        }

        // 使用乐观锁机制进行库存回滚
        Integer oldStock = product.getStock();

        // 使用OptimisticLockHelper执行乐观锁更新
        OptimisticLockHelper.OptimisticLockResult lockResult = optimisticLockHelper.executeWithOptimisticLock(
                product,
                (p) -> {
                    // 执行库存回滚
                    Integer newStock = p.getStock() + quantity;
                    p.setStock(newStock);

                    logger.debug("乐观锁库存回滚 - 商品ID: {}, 原库存: {}, 新库存: {}",
                            productId, p.getStock() - quantity, newStock);
                    return true;
                });

        if (!lockResult.isSuccess()) {
            logger.error("商品库存回滚失败 - 商品ID: {}, 原因: {}", productId, lockResult.getMessage());
            return new StockOperationResult(false, lockResult.getMessage());
        }

        Integer newStock = product.getStock();

        // 记录库存变更日志
        String productName = product.getName() != null ? product.getName() : "未知商品";
        Long logId = recordStockLog(productId, null, oldStock, newStock, quantity, "1", "订单回滚",
                orderNo, "系统", productName, null);

        logger.info("商品库存回滚成功 - 商品ID: {}, 原库存: {}, 新库存: {}, 版本: {}",
                productId, oldStock, newStock, product.getVersion());
        return new StockOperationResult(true, "回滚成功", oldStock, newStock, logId);
    }

    /**
     * SKU库存回滚（增强空值检查）
     * 
     * @param productId  商品ID
     * @param skuId      SKU ID
     * @param quantity   回滚数量
     * @param orderNo    订单号
     * @param operatorId 操作员ID
     * @return 库存操作结果
     * @author lingbai
     * @since 2025-01-21
     */
    private StockOperationResult rollbackSkuStock(Long productId, Long skuId, Integer quantity, String orderNo,
            Long operatorId) {
        // 增强参数验证
        if (productId == null) {
            logger.error("SKU库存回滚失败：商品ID不能为空");
            return new StockOperationResult(false, "商品ID不能为空");
        }

        if (skuId == null) {
            logger.error("SKU库存回滚失败：SKU ID不能为空");
            return new StockOperationResult(false, "SKU ID不能为空");
        }

        if (quantity == null || quantity <= 0) {
            logger.error("SKU库存回滚失败：回滚数量无效 - 数量: {}", quantity);
            return new StockOperationResult(false, "回滚数量必须大于0");
        }

        List<ProductSku> skuList = SKU_CACHE.get(productId);
        if (skuList == null || skuList.isEmpty()) {
            logger.error("商品SKU列表不存在 - 商品ID: {}", productId);
            return new StockOperationResult(false, "商品SKU不存在");
        }

        ProductSku sku = skuList.stream()
                .filter(s -> s != null && skuId.equals(s.getId()))
                .findFirst()
                .orElse(null);

        if (sku == null) {
            logger.error("SKU不存在 - 商品ID: {}, SKU ID: {}", productId, skuId);
            return new StockOperationResult(false, "SKU不存在");
        }

        // 检查SKU库存字段，防止空指针异常
        if (sku.getStock() == null) {
            logger.error("SKU库存数据异常 - 商品ID: {}, SKU ID: {}, 库存为null", productId, skuId);
            // 初始化库存为0
            sku.setStock(0);
            logger.warn("已将SKU库存初始化为0 - 商品ID: {}, SKU ID: {}", productId, skuId);
        }

        // 使用乐观锁辅助工具进行库存回滚
        Integer oldStock = sku.getStock();
        Integer newStock = oldStock + quantity;

        // 使用乐观锁机制进行库存回滚
        OptimisticLockHelper.OptimisticLockResult lockResult = optimisticLockHelper.executeWithOptimisticLock(
                sku,
                entity -> {
                    entity.setStock(entity.getStock() + quantity);
                    return true;
                },
                3, // 最大重试次数
                100 // 重试间隔毫秒
        );

        if (!lockResult.isSuccess()) {
            logger.warn("SKU库存回滚失败：{} - 商品ID: {}, SKU ID: {}", lockResult.getErrorMessage(), productId, skuId);
            return new StockOperationResult(false, lockResult.getErrorMessage());
        }

        // 记录库存变更日志
        String productName = sku.getProductName() != null ? sku.getProductName() : "未知商品";
        String skuName = sku.getSkuName() != null ? sku.getSkuName() : "未知SKU";
        Long logId = recordStockLog(productId, skuId, oldStock, sku.getStock(), quantity, "1", "订单回滚",
                orderNo, "系统", productName, skuName);

        logger.info("SKU库存回滚成功 - 商品ID: {}, SKU ID: {}, 原库存: {}, 新库存: {}, 重试次数: {}",
                productId, skuId, oldStock, sku.getStock(), lockResult.getRetryCount());
        return new StockOperationResult(true, "回滚成功", oldStock, sku.getStock(), logId);
    }

    private StockOperationResult stockTakingProduct(Long productId, Integer actualStock, Long operatorId,
            String reason) {
        Product product = PRODUCT_CACHE.get(productId);
        if (product == null) {
            return new StockOperationResult(false, "商品不存在");
        }

        Integer oldStock = product.getStock();
        Integer changeQuantity = actualStock - oldStock;
        product.setStock(actualStock);

        // 记录库存变更日志
        Long logId = recordStockLog(productId, null, oldStock, actualStock, changeQuantity, "4",
                reason != null ? reason : "库存盘点", null, "管理员", product.getName(), null);

        return new StockOperationResult(true, "盘点成功", oldStock, actualStock, logId);
    }

    private StockOperationResult stockTakingSku(Long productId, Long skuId, Integer actualStock, Long operatorId,
            String reason) {
        List<ProductSku> skuList = SKU_CACHE.get(productId);
        if (skuList == null) {
            return new StockOperationResult(false, "商品SKU不存在");
        }

        ProductSku sku = skuList.stream()
                .filter(s -> skuId.equals(s.getId()))
                .findFirst()
                .orElse(null);

        if (sku == null) {
            return new StockOperationResult(false, "SKU不存在");
        }

        Integer oldStock = sku.getStock();
        Integer changeQuantity = actualStock - oldStock;
        sku.setStock(actualStock);

        // 记录库存变更日志
        Long logId = recordStockLog(productId, skuId, oldStock, actualStock, changeQuantity, "4",
                reason != null ? reason : "库存盘点", null, "管理员", sku.getProductName(), sku.getSkuName());

        return new StockOperationResult(true, "盘点成功", oldStock, actualStock, logId);
    }

    private Long recordStockLog(Long productId, Long skuId, Integer oldStock, Integer newStock,
            Integer changeQuantity, String changeType, String reason,
            String relatedOrderNo, String operatorName, String productName, String skuName) {
        try {
            StockLog stockLog = new StockLog();
            stockLog.setId(System.currentTimeMillis());
            stockLog.setProductId(productId);
            stockLog.setSkuId(skuId);
            stockLog.setOldStock(oldStock);
            stockLog.setNewStock(newStock);
            stockLog.setChangeQuantity(changeQuantity);
            stockLog.setChangeType(changeType);
            stockLog.setReason(reason);
            stockLog.setRelatedOrderNo(relatedOrderNo);
            stockLog.setOperatorId(1L);
            stockLog.setOperatorName(operatorName);
            stockLog.setProductName(productName);
            stockLog.setSkuName(skuName);
            stockLog.setCreateTime(LocalDateTime.now());

            STOCK_LOG_CACHE.add(stockLog);

            // 记录到审计日志系统
            if (auditLogService != null) {
                String action = getStockActionByChangeType(changeType);
                String description = String.format("库存变更 - %s: %s -> %s (变化: %+d), 原因: %s",
                        productName != null ? productName : "商品ID:" + productId,
                        oldStock, newStock, changeQuantity, reason);

                auditLogService.recordOperationLog(
                        action,
                        skuId != null ? "ProductSku" : "Product",
                        skuId != null ? skuId : productId,
                        1L, // 操作人ID，实际应用中应从上下文获取
                        operatorName != null ? operatorName : "系统",
                        description,
                        oldStock != null ? oldStock.toString() : null,
                        newStock != null ? newStock.toString() : null,
                        "127.0.0.1", // 实际应用中应获取真实IP
                        "StockService" // 用户代理
                );

                // 记录业务日志
                Map<String, Object> extendInfo = new HashMap<>();
                extendInfo.put("productId", productId);
                extendInfo.put("skuId", skuId);
                extendInfo.put("changeQuantity", changeQuantity);
                extendInfo.put("changeType", changeType);
                extendInfo.put("relatedOrderNo", relatedOrderNo);

                auditLogService.recordBusinessLog(
                        "STOCK_MANAGEMENT",
                        stockLog.getId(),
                        1L, // 操作人ID
                        operatorName != null ? operatorName : "系统",
                        action,
                        "SUCCESS",
                        0L, // 执行时长，实际应用中应计算
                        extendInfo);
            }

            return stockLog.getId();
        } catch (Exception e) {
            logger.error("记录库存日志失败 - 商品ID: {}, SKU ID: {}", productId, skuId, e);

            // 记录异常日志
            if (auditLogService != null) {
                auditLogService.recordExceptionLog(
                        "STOCK_LOG_ERROR",
                        skuId != null ? "ProductSku" : "Product",
                        skuId != null ? skuId : productId,
                        1L, // 操作人ID
                        "记录库存日志失败: " + e.getMessage(),
                        e.getStackTrace() != null ? Arrays.toString(e.getStackTrace()) : "",
                        String.format("productId=%s, skuId=%s, changeQuantity=%s", productId, skuId, changeQuantity));
            }

            return null;
        }
    }

    /**
     * 根据变更类型获取操作动作
     */
    private String getStockActionByChangeType(String changeType) {
        if (changeType == null)
            return "STOCK_UNKNOWN";

        switch (changeType) {
            case "1":
                return "STOCK_INCREASE";
            case "2":
                return "STOCK_DECREASE";
            case "3":
                return "STOCK_TRANSFER";
            case "4":
                return "STOCK_INVENTORY";
            default:
                return "STOCK_OTHER";
        }
    }

    private Map<String, Object> createPageData(List<?> records, int total, Long current, Long size) {
        Map<String, Object> pageData = new HashMap<>();
        pageData.put("records", records);
        pageData.put("total", total);
        pageData.put("current", current);
        pageData.put("size", size);
        pageData.put("pages", (int) Math.ceil((double) total / size));
        return pageData;
    }

    private Map<String, Object> createEmptyPageData(Long current, Long size) {
        return createPageData(new ArrayList<>(), 0, current, size);
    }

    /**
     * 初始化模拟数据
     * 注意：已禁用，不再初始化虚拟测试数据
     * 修复SKU ID映射问题，确保测试用例能正确匹配数据
     * 
     * @author lingbai
     * @since 2025-01-21
     */
    private static void initMockData() {
        logger.info("模拟数据初始化已禁用，系统将使用真实数据库数据");
        // 以下代码已注释，不再初始化虚拟测试数据
        /*
         * logger.info("开始初始化库存服务模拟数据");
         * 
         * // 初始化商品数据
         * for (int i = 1; i <= 10; i++) {
         * Product product = new Product();
         * product.setId((long) i);
         * product.setName("测试商品" + i);
         * product.setDescription("这是测试商品" + i + "的描述");
         * product.setPrice(99.99 + i * 10);
         * product.setStock(100 + i * 10); // 确保有足够的库存用于测试
         * product.setStockWarning(20);
         * product.setCategoryId((long) (i % 3 + 1));
         * product.setBrandId((long) (i % 2 + 1));
         * product.setStatus(1);
         * product.setSales(i * 5);
         * product.setCreateTime(LocalDateTime.now().minusDays(i));
         * product.setUpdateTime(LocalDateTime.now());
         * 
         * PRODUCT_CACHE.put(product.getId(), product);
         * 
         * // 为每个商品创建2个SKU，修复SKU ID映射问题
         * List<ProductSku> skuList = new ArrayList<>();
         * for (int j = 1; j <= 2; j++) {
         * ProductSku sku = new ProductSku();
         * // 修复：使用与测试用例匹配的SKU ID规则
         * // 商品1的SKU ID: 11, 12; 商品2的SKU ID: 21, 22; 以此类推
         * sku.setId((long) (i * 10 + j));
         * sku.setProductId(product.getId());
         * sku.setSkuCode("SKU" + i + "0" + j);
         * sku.setSkuName(product.getName() + "-规格" + j);
         * sku.setPrice(product.getPrice() + j * 5);
         * sku.setStock(50 + j * 10); // 确保有足够的库存用于测试
         * sku.setStockWarning(10);
         * sku.setStatus(1);
         * sku.setProductName(product.getName());
         * sku.setCreateTime(LocalDateTime.now());
         * sku.setUpdateTime(LocalDateTime.now());
         * 
         * skuList.add(sku);
         * logger.debug("创建SKU - 商品ID: {}, SKU ID: {}, SKU名称: {}, 库存: {}",
         * product.getId(), sku.getId(), sku.getSkuName(), sku.getStock());
         * }
         * SKU_CACHE.put(product.getId(), skuList);
         * }
         * 
         * logger.info("库存服务模拟数据初始化完成 - 商品数量: {}, SKU总数: {}",
         * PRODUCT_CACHE.size(),
         * SKU_CACHE.values().stream().mapToInt(List::size).sum());
         */
    }

    /**
     * 获取当前事务ID
     */
    private String getTransactionId() {
        try {
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                return TransactionSynchronizationManager.getCurrentTransactionName();
            }
        } catch (Exception e) {
            logger.warn("获取事务ID失败", e);
        }
        return null;
    }

    /**
     * 记录事务操作，用于回滚补偿
     */
    private void recordTransactionOperation(String transactionId, Long productId, Long skuId,
            Integer quantity, String orderNo, String operation, Long operatorId) {
        try {
            String key = transactionId + "_" + orderNo;
            TransactionRollbackRecord record = new TransactionRollbackRecord();
            record.setTransactionId(transactionId);
            record.setProductId(productId);
            record.setSkuId(skuId);
            record.setQuantity(quantity);
            record.setOrderNo(orderNo);
            record.setOperation(operation);
            record.setOperatorId(operatorId);
            record.setCreateTime(new Date());

            TRANSACTION_ROLLBACK_CACHE.put(key, record);
            logger.debug("记录事务操作 - 事务ID: {}, 操作: {}, 订单号: {}", transactionId, operation, orderNo);
        } catch (Exception e) {
            logger.error("记录事务操作失败", e);
        }
    }

    /**
     * 移除事务操作记录
     */
    private void removeTransactionOperation(String transactionId, Long productId, Long skuId, String orderNo) {
        try {
            String key = transactionId + "_" + orderNo;
            TRANSACTION_ROLLBACK_CACHE.remove(key);
            logger.debug("移除事务操作记录 - 事务ID: {}, 订单号: {}", transactionId, orderNo);
        } catch (Exception e) {
            logger.error("移除事务操作记录失败", e);
        }
    }

    /**
     * 回滚成功的操作
     * 
     * @param successOperations 需要回滚的成功操作列表
     * @param batchId           批次ID，用于日志追踪
     * @author lingbai
     * @since 2025-01-21
     */
    private void rollbackSuccessfulOperations(List<StockOperation> successOperations, String batchId) {
        if (successOperations == null || successOperations.isEmpty()) {
            logger.info("无需回滚操作 - 批次ID: {}", batchId);
            return;
        }

        logger.info("开始回滚成功的操作 - 批次ID: {}, 数量: {}", batchId, successOperations.size());

        int rollbackSuccessCount = 0;
        int rollbackFailCount = 0;

        // 逆序回滚，确保操作顺序的一致性
        for (int i = successOperations.size() - 1; i >= 0; i--) {
            StockOperation operation = successOperations.get(i);
            try {
                logger.debug("回滚第{}个操作 - 批次ID: {}, 商品ID: {}, SKU ID: {}",
                        i + 1, batchId, operation.getProductId(), operation.getSkuId());

                StockOperationResult rollbackResult = rollbackStock(
                        operation.getProductId(),
                        operation.getSkuId(),
                        operation.getQuantity(),
                        operation.getOrderNo(),
                        operation.getOperatorId());

                if (rollbackResult.isSuccess()) {
                    rollbackSuccessCount++;
                    logger.debug("回滚第{}个操作成功 - 批次ID: {}", i + 1, batchId);
                } else {
                    rollbackFailCount++;
                    logger.error("回滚第{}个操作失败 - 批次ID: {}, 商品ID: {}, SKU ID: {}, 原因: {}",
                            i + 1, batchId, operation.getProductId(), operation.getSkuId(),
                            rollbackResult.getMessage());
                }
            } catch (Exception e) {
                rollbackFailCount++;
                logger.error("回滚第{}个操作异常 - 批次ID: {}, 商品ID: {}, SKU ID: {}",
                        i + 1, batchId, operation.getProductId(), operation.getSkuId(), e);
            }
        }

        logger.info("回滚操作完成 - 批次ID: {}, 成功: {}, 失败: {}",
                batchId, rollbackSuccessCount, rollbackFailCount);
    }

    /**
     * 回滚成功的操作（兼容旧版本）
     * 
     * @param successOperations 需要回滚的成功操作列表
     */
    private void rollbackSuccessOperations(List<StockOperation> successOperations) {
        rollbackSuccessfulOperations(successOperations, "LEGACY_" + System.currentTimeMillis());
    }

    /**
     * 事务回滚记录内部类
     */
    private static class TransactionRollbackRecord {
        private String transactionId;
        private Long productId;
        private Long skuId;
        private Integer quantity;
        private String orderNo;
        private String operation;
        private Long operatorId;
        private Date createTime;

        // Getters and Setters
        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Long getSkuId() {
            return skuId;
        }

        public void setSkuId(Long skuId) {
            this.skuId = skuId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public Long getOperatorId() {
            return operatorId;
        }

        public void setOperatorId(Long operatorId) {
            this.operatorId = operatorId;
        }

        public Date getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Date createTime) {
            this.createTime = createTime;
        }
    }
}