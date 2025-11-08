package com.mall.product.service.impl;

import com.mall.product.service.PriceService;
import com.mall.product.service.StockService;
import com.mall.product.service.AuditLogService;
import com.mall.product.model.*;
import com.mall.product.domain.entity.Product;
import com.mall.product.domain.entity.ProductSku;
import com.mall.product.domain.entity.PriceHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 价格服务实现类
 * 负责价格调整、历史记录、审核、策略管理等功能
 * 支持事务性操作、版本管理、审批流程和完善的日志记录
 */
@Service
public class PriceServiceImpl implements PriceService {
    
    private static final Logger logger = LoggerFactory.getLogger(PriceServiceImpl.class);
    
    @Autowired
    private StockService stockService;
    
    @Autowired
    private AuditLogService auditLogService;
    
    // 模拟数据存储
    private final Map<Long, PriceHistory> priceHistoryMap = new ConcurrentHashMap<>();
    private final Map<Long, PriceStrategy> priceStrategyMap = new ConcurrentHashMap<>();
    private final Map<Long, Product> productMap = new ConcurrentHashMap<>();
    private final Map<Long, ProductSku> skuMap = new ConcurrentHashMap<>();
    private final Map<Long, PriceVersion> priceVersionMap = new ConcurrentHashMap<>();
    private final Map<String, PriceAuditLog> auditLogMap = new ConcurrentHashMap<>();
    
    // ID生成器
    private final AtomicLong priceHistoryIdGenerator = new AtomicLong(1);
    private final AtomicLong strategyIdGenerator = new AtomicLong(1);
    private final AtomicLong versionIdGenerator = new AtomicLong(1);
    
    // 价格操作锁
    private final Map<String, ReentrantLock> priceLocks = new ConcurrentHashMap<>();
    
    public PriceServiceImpl() {
        initMockData();
    }
    
    /**
     * 价格调整（支持事务和版本管理）
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public PriceAdjustmentResult adjustPrice(Long productId, Long skuId, Double newPrice, Long operatorId, String reason) {
        logger.info("价格调整 - 商品ID: {}, SKU ID: {}, 新价格: {}, 操作员: {}, 原因: {}", 
            productId, skuId, newPrice, operatorId, reason);
        
        // 参数验证
        if (productId == null) {
            logger.error("价格调整失败：商品ID不能为空");
            return new PriceAdjustmentResult(false, "商品ID不能为空");
        }
        
        if (newPrice == null || newPrice <= 0) {
            logger.error("价格调整失败：价格无效 - 价格: {}", newPrice);
            return new PriceAdjustmentResult(false, "价格必须大于0");
        }
        
        if (operatorId == null) {
            logger.error("价格调整失败：操作员ID不能为空");
            return new PriceAdjustmentResult(false, "操作员ID不能为空");
        }
        
        if (reason == null || reason.trim().isEmpty()) {
            logger.error("价格调整失败：调价原因不能为空");
            return new PriceAdjustmentResult(false, "调价原因不能为空");
        }
        
        String lockKey = productId + "_" + (skuId != null ? skuId : "0");
        ReentrantLock lock = priceLocks.computeIfAbsent(lockKey, k -> new ReentrantLock());
        
        lock.lock();
        try {
            // 获取当前价格和版本信息
            Double oldPrice;
            Long currentVersion;
            
            if (skuId != null) {
                ProductSku sku = skuMap.get(skuId);
                if (sku == null) {
                    logger.error("价格调整失败：SKU不存在 - SKU ID: {}", skuId);
                    return new PriceAdjustmentResult(false, "SKU不存在");
                }
                
                if (!productId.equals(sku.getProductId())) {
                    logger.error("价格调整失败：SKU不属于指定商品 - 商品ID: {}, SKU ID: {}", productId, skuId);
                    return new PriceAdjustmentResult(false, "SKU不属于指定商品");
                }
                
                oldPrice = sku.getPrice();
                currentVersion = sku.getPriceVersion() != null ? sku.getPriceVersion() : 1L;
            } else {
                Product product = productMap.get(productId);
                if (product == null) {
                    logger.error("价格调整失败：商品不存在 - 商品ID: {}", productId);
                    return new PriceAdjustmentResult(false, "商品不存在");
                }
                
                oldPrice = product.getPrice();
                currentVersion = product.getPriceVersion() != null ? product.getPriceVersion() : 1L;
            }
            
            // 检查价格是否有变化
            if (Objects.equals(oldPrice, newPrice)) {
                logger.warn("价格调整跳过：新价格与当前价格相同 - 价格: {}", newPrice);
                return new PriceAdjustmentResult(false, "新价格与当前价格相同");
            }
            
            // 创建新版本
            Long newVersion = currentVersion + 1;
            PriceVersion priceVersion = createPriceVersion(productId, skuId, oldPrice, newPrice, 
                newVersion, operatorId, reason);
            
            // 创建价格历史记录
            Long historyId = priceHistoryIdGenerator.getAndIncrement();
            PriceHistory priceHistory = new PriceHistory();
            priceHistory.setId(historyId);
            priceHistory.setProductId(productId);
            priceHistory.setSkuId(skuId);
            priceHistory.setOldPrice(oldPrice);
            priceHistory.setNewPrice(newPrice);
            priceHistory.setChangeReason(reason);
            priceHistory.setOperatorId(operatorId);
            priceHistory.setAuditStatus("PENDING");
            priceHistory.setCreateTime(LocalDateTime.now());
            priceHistory.setPriceVersion(newVersion);
            
            priceHistoryMap.put(historyId, priceHistory);
            
            // 更新价格和版本（需要审批的情况下暂不更新实际价格）
            boolean requiresApproval = requiresApproval(oldPrice, newPrice);
            if (!requiresApproval) {
                // 不需要审批，直接更新价格
                updateActualPrice(productId, skuId, newPrice, newVersion);
                priceHistory.setAuditStatus("AUTO_APPROVED");
                priceHistory.setAuditorId(operatorId);
                priceHistory.setAuditTime(LocalDateTime.now());
                priceHistory.setAuditReason("自动审批");
                
                // 同步库存数据
                syncWithInventory(productId, skuId, oldPrice, newPrice, operatorId);
                
                logger.info("价格调整成功（自动审批） - 商品ID: {}, SKU ID: {}, 旧价格: {}, 新价格: {}, 版本: {}", 
                    productId, skuId, oldPrice, newPrice, newVersion);
            } else {
                logger.info("价格调整提交审批 - 商品ID: {}, SKU ID: {}, 旧价格: {}, 新价格: {}, 版本: {}", 
                    productId, skuId, oldPrice, newPrice, newVersion);
            }
            
            // 记录审计日志
            recordAuditLog("PRICE_ADJUST", productId, skuId, operatorId, 
                String.format("价格调整：%.2f -> %.2f，原因：%s", oldPrice, newPrice, reason));
            
            return new PriceAdjustmentResult(true, 
                requiresApproval ? "价格调整已提交审批" : "价格调整成功", 
                oldPrice, newPrice, historyId);
                
        } catch (Exception e) {
            logger.error("价格调整异常 - 商品ID: {}, SKU ID: {}, 新价格: {}", productId, skuId, newPrice, e);
            return new PriceAdjustmentResult(false, "价格调整异常: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public BatchPriceAdjustmentResult batchAdjustPrice(List<PriceAdjustment> priceAdjustments) {
        if (priceAdjustments == null || priceAdjustments.isEmpty()) {
            return new BatchPriceAdjustmentResult(false, "调价列表不能为空", 0, 0, 0);
        }
        
        int totalCount = priceAdjustments.size();
        int successCount = 0;
        int failCount = 0;
        List<PriceAdjustmentResult> results = new ArrayList<>();
        
        for (PriceAdjustment adjustment : priceAdjustments) {
            PriceAdjustmentResult result = adjustPrice(
                adjustment.getProductId(),
                adjustment.getSkuId(),
                adjustment.getNewPrice(),
                adjustment.getOperatorId(),
                adjustment.getReason()
            );
            
            results.add(result);
            if (result.isSuccess()) {
                successCount++;
            } else {
                failCount++;
            }
        }
        
        BatchPriceAdjustmentResult batchResult = new BatchPriceAdjustmentResult(
            successCount > 0,
            String.format("批量调价完成，成功：%d，失败：%d", successCount, failCount),
            totalCount,
            successCount,
            failCount
        );
        batchResult.setResults(results);
        
        return batchResult;
    }
    
    @Override
    public Object getPriceHistory(Long productId, Long skuId, Long current, Long size) {
        List<PriceHistory> allHistory = priceHistoryMap.values().stream()
            .filter(history -> {
                boolean match = true;
                if (productId != null) {
                    match = productId.equals(history.getProductId());
                }
                if (skuId != null && match) {
                    match = skuId.equals(history.getSkuId());
                }
                return match;
            })
            .sorted((h1, h2) -> h2.getCreateTime().compareTo(h1.getCreateTime()))
            .collect(Collectors.toList());
        
        // 分页处理
        int start = (int) ((current - 1) * size);
        int end = Math.min(start + size.intValue(), allHistory.size());
        List<PriceHistory> pageData = allHistory.subList(start, end);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageData);
        result.put("total", allHistory.size());
        result.put("current", current);
        result.put("size", size);
        result.put("pages", (allHistory.size() + size - 1) / size);
        
        return result;
    }
    
    @Override
    public Object getPriceChangeStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        List<PriceHistory> filteredHistory = priceHistoryMap.values().stream()
            .filter(history -> {
                LocalDateTime createTime = history.getCreateTime();
                return createTime.isAfter(startTime) && createTime.isBefore(endTime);
            })
            .collect(Collectors.toList());
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalAdjustments", filteredHistory.size());
        statistics.put("increaseCount", filteredHistory.stream()
            .mapToLong(h -> h.getNewPrice() > h.getOldPrice() ? 1 : 0).sum());
        statistics.put("decreaseCount", filteredHistory.stream()
            .mapToLong(h -> h.getNewPrice() < h.getOldPrice() ? 1 : 0).sum());
        statistics.put("averageChangeAmount", filteredHistory.stream()
            .mapToDouble(h -> Math.abs(h.getNewPrice() - h.getOldPrice())).average().orElse(0.0));
        statistics.put("maxPriceIncrease", filteredHistory.stream()
            .filter(h -> h.getNewPrice() > h.getOldPrice())
            .mapToDouble(h -> h.getNewPrice() - h.getOldPrice()).max().orElse(0.0));
        statistics.put("maxPriceDecrease", filteredHistory.stream()
            .filter(h -> h.getNewPrice() < h.getOldPrice())
            .mapToDouble(h -> h.getOldPrice() - h.getNewPrice()).max().orElse(0.0));
        
        return statistics;
    }
    
    /**
     * 创建价格版本记录
     */
    private PriceVersion createPriceVersion(Long productId, Long skuId, Double oldPrice, 
                                          Double newPrice, Long version, Long operatorId, String reason) {
        Long versionId = versionIdGenerator.getAndIncrement();
        PriceVersion priceVersion = new PriceVersion();
        priceVersion.setId(versionId);
        priceVersion.setProductId(productId);
        priceVersion.setSkuId(skuId);
        priceVersion.setVersion(version);
        priceVersion.setOldPrice(oldPrice);
        priceVersion.setNewPrice(newPrice);
        priceVersion.setChangeReason(reason);
        priceVersion.setOperatorId(operatorId);
        priceVersion.setCreateTime(LocalDateTime.now());
        priceVersion.setStatus("ACTIVE");
        
        priceVersionMap.put(versionId, priceVersion);
        return priceVersion;
    }
    
    /**
     * 判断是否需要审批
     */
    private boolean requiresApproval(Double oldPrice, Double newPrice) {
        if (oldPrice == null || newPrice == null) {
            return true;
        }
        
        // 价格变动超过20%需要审批
        double changeRate = Math.abs(newPrice - oldPrice) / oldPrice;
        return changeRate > 0.2;
    }
    
    /**
     * 更新实际价格
     */
    private void updateActualPrice(Long productId, Long skuId, Double newPrice, Long version) {
        if (skuId != null) {
            ProductSku sku = skuMap.get(skuId);
            if (sku != null) {
                sku.setPrice(newPrice);
                sku.setPriceVersion(version);
                sku.setUpdateTime(LocalDateTime.now());
            }
        } else {
            Product product = productMap.get(productId);
            if (product != null) {
                product.setPrice(newPrice);
                product.setPriceVersion(version);
                product.setUpdateTime(LocalDateTime.now());
            }
        }
    }
    
    /**
     * 与库存数据同步
     */
    private void syncWithInventory(Long productId, Long skuId, Double oldPrice, Double newPrice, Long operatorId) {
        try {
            // 这里可以调用库存服务的相关方法来同步价格变更信息
            logger.info("价格变更同步到库存系统 - 商品ID: {}, SKU ID: {}, 旧价格: {}, 新价格: {}", 
                productId, skuId, oldPrice, newPrice);
            
            // 如果有库存相关的价格计算逻辑，可以在这里处理
            // 例如：更新库存价值、重新计算成本等
            
        } catch (Exception e) {
            logger.error("价格变更同步到库存系统失败", e);
        }
    }
    
    /**
     * 记录审计日志
     */
    private void recordAuditLog(String action, Long productId, Long skuId, Long operatorId, String description) {
        try {
            // 使用新的审计日志服务
            if (auditLogService != null) {
                auditLogService.recordOperationLog(
                    action,
                    skuId != null ? "ProductSku" : "Product",
                    skuId != null ? skuId : productId,
                    operatorId,
                    "价格管理员", // 实际应用中应从用户上下文获取
                    description,
                    null, // 旧值，实际应用中应传入具体的旧价格
                    null, // 新值，实际应用中应传入具体的新价格
                    "127.0.0.1", // 实际应用中应获取真实IP
                    "PriceService" // 用户代理
                );
                
                // 记录业务日志
                Map<String, Object> extendInfo = new HashMap<>();
                extendInfo.put("productId", productId);
                extendInfo.put("skuId", skuId);
                extendInfo.put("action", action);
                
                auditLogService.recordBusinessLog(
                    "PRICE_MANAGEMENT",
                    productId,
                    operatorId,
                    "价格管理员",
                    action,
                    "SUCCESS",
                    0L, // 执行时长，实际应用中应计算
                    extendInfo
                );
            }
            
            // 保留原有的日志记录逻辑作为备份
            String logId = System.currentTimeMillis() + "_" + operatorId;
            PriceAuditLog auditLog = new PriceAuditLog();
            auditLog.setId(logId);
            auditLog.setAction(action);
            auditLog.setProductId(productId);
            auditLog.setSkuId(skuId);
            auditLog.setOperatorId(operatorId);
            auditLog.setDescription(description);
            auditLog.setCreateTime(LocalDateTime.now());
            auditLog.setIpAddress("127.0.0.1"); // 实际应用中应获取真实IP
            
            auditLogMap.put(logId, auditLog);
            logger.debug("记录审计日志 - 操作: {}, 描述: {}", action, description);
        } catch (Exception e) {
            logger.error("记录审计日志失败", e);
            
            // 记录异常日志
            if (auditLogService != null) {
                auditLogService.recordExceptionLog(
                    "AUDIT_LOG_ERROR",
                    skuId != null ? "ProductSku" : "Product",
                    skuId != null ? skuId : productId,
                    operatorId,
                    "记录审计日志失败: " + e.getMessage(),
                    e.getStackTrace() != null ? Arrays.toString(e.getStackTrace()) : "",
                    String.format("action=%s, productId=%s, skuId=%s", action, productId, skuId)
                );
            }
        }
    }
    
    /**
     * 价格审核（增强版）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PriceAuditResult auditPrice(Long priceHistoryId, Boolean approved, String auditReason, Long auditorId) {
        logger.info("价格审核 - 历史记录ID: {}, 审核结果: {}, 审核员: {}", priceHistoryId, approved, auditorId);
        
        // 参数验证
        if (priceHistoryId == null) {
            logger.error("价格审核失败：历史记录ID不能为空");
            return new PriceAuditResult(false, "历史记录ID不能为空");
        }
        
        if (approved == null) {
            logger.error("价格审核失败：审核结果不能为空");
            return new PriceAuditResult(false, "审核结果不能为空");
        }
        
        if (auditorId == null) {
            logger.error("价格审核失败：审核员ID不能为空");
            return new PriceAuditResult(false, "审核员ID不能为空");
        }
        
        PriceHistory priceHistory = priceHistoryMap.get(priceHistoryId);
        if (priceHistory == null) {
            logger.error("价格审核失败：价格历史记录不存在 - ID: {}", priceHistoryId);
            return new PriceAuditResult(false, "价格历史记录不存在");
        }
        
        if (!"PENDING".equals(priceHistory.getAuditStatus())) {
            logger.warn("价格审核跳过：该记录已经审核过了 - 状态: {}", priceHistory.getAuditStatus());
            return new PriceAuditResult(false, "该记录已经审核过了");
        }
        
        String lockKey = priceHistory.getProductId() + "_" + 
            (priceHistory.getSkuId() != null ? priceHistory.getSkuId() : "0");
        ReentrantLock lock = priceLocks.computeIfAbsent(lockKey, k -> new ReentrantLock());
        
        lock.lock();
        try {
            String auditStatus = approved ? "APPROVED" : "REJECTED";
            priceHistory.setAuditStatus(auditStatus);
            priceHistory.setAuditorId(auditorId);
            priceHistory.setAuditReason(auditReason);
            priceHistory.setAuditTime(LocalDateTime.now());
            
            if (approved) {
                // 审核通过，更新实际价格
                updateActualPrice(priceHistory.getProductId(), priceHistory.getSkuId(), 
                    priceHistory.getNewPrice(), priceHistory.getPriceVersion());
                
                // 同步库存数据
                syncWithInventory(priceHistory.getProductId(), priceHistory.getSkuId(), 
                    priceHistory.getOldPrice(), priceHistory.getNewPrice(), auditorId);
                
                logger.info("价格审核通过 - 商品ID: {}, SKU ID: {}, 新价格: {}", 
                    priceHistory.getProductId(), priceHistory.getSkuId(), priceHistory.getNewPrice());
            } else {
                // 审核不通过，记录拒绝原因
                logger.info("价格审核拒绝 - 商品ID: {}, SKU ID: {}, 拒绝原因: {}", 
                    priceHistory.getProductId(), priceHistory.getSkuId(), auditReason);
            }
            
            // 记录审计日志
            recordAuditLog("PRICE_AUDIT", priceHistory.getProductId(), priceHistory.getSkuId(), 
                auditorId, String.format("价格审核%s，原因：%s", approved ? "通过" : "拒绝", auditReason));
            
            return new PriceAuditResult(true, "审核完成", priceHistoryId, auditStatus);
            
        } catch (Exception e) {
            logger.error("价格审核异常 - 历史记录ID: {}", priceHistoryId, e);
            return new PriceAuditResult(false, "审核异常: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * 获取价格版本历史
     */
    public Object getPriceVersionHistory(Long productId, Long skuId, Long current, Long size) {
        logger.info("获取价格版本历史 - 商品ID: {}, SKU ID: {}", productId, skuId);
        
        List<PriceVersion> allVersions = priceVersionMap.values().stream()
            .filter(version -> {
                boolean match = true;
                if (productId != null) {
                    match = productId.equals(version.getProductId());
                }
                if (skuId != null && match) {
                    match = skuId.equals(version.getSkuId());
                }
                return match;
            })
            .sorted((v1, v2) -> v2.getVersion().compareTo(v1.getVersion()))
            .collect(Collectors.toList());
        
        // 分页处理
        int start = (int) ((current - 1) * size);
        int end = Math.min(start + size.intValue(), allVersions.size());
        List<PriceVersion> pageData = allVersions.subList(start, end);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageData);
        result.put("total", allVersions.size());
        result.put("current", current);
        result.put("size", size);
        result.put("pages", (allVersions.size() + size - 1) / size);
        
        return result;
    }
    
    /**
     * 获取审计日志
     */
    public Object getAuditLogs(Long productId, Long skuId, String action, Long current, Long size) {
        logger.info("获取审计日志 - 商品ID: {}, SKU ID: {}, 操作: {}", productId, skuId, action);
        
        List<PriceAuditLog> allLogs = auditLogMap.values().stream()
            .filter(log -> {
                boolean match = true;
                if (productId != null) {
                    match = productId.equals(log.getProductId());
                }
                if (skuId != null && match) {
                    match = skuId.equals(log.getSkuId());
                }
                if (action != null && match) {
                    match = action.equals(log.getAction());
                }
                return match;
            })
            .sorted((l1, l2) -> l2.getCreateTime().compareTo(l1.getCreateTime()))
            .collect(Collectors.toList());
        
        // 分页处理
        int start = (int) ((current - 1) * size);
        int end = Math.min(start + size.intValue(), allLogs.size());
        List<PriceAuditLog> pageData = allLogs.subList(start, end);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageData);
        result.put("total", allLogs.size());
        result.put("current", current);
        result.put("size", size);
        result.put("pages", (allLogs.size() + size - 1) / size);
        
        return result;
    }
    
    @Override
    public PriceStrategyResult createPriceStrategy(PriceStrategy priceStrategy) {
        if (priceStrategy == null || priceStrategy.getName() == null || priceStrategy.getName().trim().isEmpty()) {
            return new PriceStrategyResult(false, "策略名称不能为空");
        }
        
        Long strategyId = strategyIdGenerator.getAndIncrement();
        priceStrategy.setId(strategyId);
        priceStrategy.setCreateTime(LocalDateTime.now());
        priceStrategy.setUpdateTime(LocalDateTime.now());
        priceStrategy.setStatus(1); // 默认启用
        
        priceStrategyMap.put(strategyId, priceStrategy);
        
        return new PriceStrategyResult(true, "价格策略创建成功", strategyId, 0);
    }
    
    @Override
    public PriceStrategyResult applyPriceStrategy(Long strategyId, List<Long> productIds, Long operatorId) {
        PriceStrategy strategy = priceStrategyMap.get(strategyId);
        if (strategy == null) {
            return new PriceStrategyResult(false, "价格策略不存在");
        }
        
        if (strategy.getStatus() != 1) {
            return new PriceStrategyResult(false, "价格策略已禁用");
        }
        
        if (productIds == null || productIds.isEmpty()) {
            return new PriceStrategyResult(false, "商品列表不能为空");
        }
        
        int affectedCount = 0;
        String strategyType = strategy.getStrategyType();
        Double adjustmentValue = strategy.getAdjustmentValue();
        
        for (Long productId : productIds) {
            Product product = productMap.get(productId);
            if (product == null) continue;
            
            Double oldPrice = product.getPrice();
            Double newPrice = calculateNewPrice(oldPrice, strategyType, adjustmentValue);
            
            if (newPrice > 0) {
                // 应用价格策略
                PriceAdjustmentResult result = adjustPrice(
                    productId, null, newPrice, operatorId, 
                    "应用价格策略：" + strategy.getName()
                );
                
                if (result.isSuccess()) {
                    affectedCount++;
                }
            }
        }
        
        return new PriceStrategyResult(true, "价格策略应用完成", strategyId, affectedCount);
    }
    
    @Override
    public Object getPriceStrategies(Long current, Long size) {
        List<PriceStrategy> allStrategies = new ArrayList<>(priceStrategyMap.values());
        allStrategies.sort((s1, s2) -> s2.getCreateTime().compareTo(s1.getCreateTime()));
        
        // 分页处理
        int start = (int) ((current - 1) * size);
        int end = Math.min(start + size.intValue(), allStrategies.size());
        List<PriceStrategy> pageData = allStrategies.subList(start, end);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageData);
        result.put("total", allStrategies.size());
        result.put("current", current);
        result.put("size", size);
        result.put("pages", (allStrategies.size() + size - 1) / size);
        
        return result;
    }
    
    /**
     * 根据策略类型计算新价格
     */
    private Double calculateNewPrice(Double oldPrice, String strategyType, Double adjustmentValue) {
        if (oldPrice == null || adjustmentValue == null) {
            return oldPrice;
        }
        
        switch (strategyType) {
            case "1": // 固定价格
                return adjustmentValue;
            case "2": // 百分比调整
                return oldPrice * (1 + adjustmentValue / 100);
            case "3": // 固定金额调整
                return oldPrice + adjustmentValue;
            default:
                return oldPrice;
        }
    }
    
    /**
     * 初始化模拟数据
     */
    private void initMockData() {
        // 初始化商品数据
        for (int i = 1; i <= 10; i++) {
            Product product = new Product();
            product.setId((long) i);
            product.setName("商品" + i);
            product.setPrice(100.0 + i * 10);
            product.setStatus(1);
            product.setCreateTime(LocalDateTime.now().minusDays(i));
            product.setUpdateTime(LocalDateTime.now().minusDays(i));
            productMap.put((long) i, product);
            
            // 为每个商品创建2个SKU
            for (int j = 1; j <= 2; j++) {
                ProductSku sku = new ProductSku();
                sku.setId((long) (i * 10 + j));
                sku.setProductId((long) i);
                sku.setSkuCode("SKU" + i + "-" + j);
                sku.setPrice(product.getPrice() + j * 5);
                sku.setStock(100 + j * 10);
                sku.setStatus(1);
                sku.setCreateTime(LocalDateTime.now().minusDays(i));
                sku.setUpdateTime(LocalDateTime.now().minusDays(i));
                skuMap.put(sku.getId(), sku);
            }
        }
        
        // 初始化价格历史数据
        for (int i = 1; i <= 5; i++) {
            PriceHistory history = new PriceHistory();
            history.setId((long) i);
            history.setProductId((long) i);
            history.setOldPrice(100.0 + i * 10);
            history.setNewPrice(110.0 + i * 10);
            history.setChangeReason("市场调价");
            history.setOperatorId(1L);
            history.setAuditStatus(i % 2 == 0 ? "APPROVED" : "PENDING");
            history.setCreateTime(LocalDateTime.now().minusDays(i));
            priceHistoryMap.put((long) i, history);
        }
        
        // 初始化价格策略数据
        PriceStrategy strategy1 = new PriceStrategy();
        strategy1.setId(1L);
        strategy1.setName("春季促销策略");
        strategy1.setDescription("春季商品统一降价10%");
        strategy1.setStrategyType("2");
        strategy1.setAdjustmentValue(-10.0);
        strategy1.setStatus(1);
        strategy1.setCreatorId(1L);
        strategy1.setCreateTime(LocalDateTime.now().minusDays(7));
        strategy1.setUpdateTime(LocalDateTime.now().minusDays(7));
        priceStrategyMap.put(1L, strategy1);
        
        PriceStrategy strategy2 = new PriceStrategy();
        strategy2.setId(2L);
        strategy2.setName("高端商品调价策略");
        strategy2.setDescription("高端商品统一涨价50元");
        strategy2.setStrategyType("3");
        strategy2.setAdjustmentValue(50.0);
        strategy2.setStatus(1);
        strategy2.setCreatorId(1L);
        strategy2.setCreateTime(LocalDateTime.now().minusDays(5));
        strategy2.setUpdateTime(LocalDateTime.now().minusDays(5));
        priceStrategyMap.put(2L, strategy2);
        
        // 更新ID生成器
        priceHistoryIdGenerator.set(6);
        strategyIdGenerator.set(3);
    }

    @Override
    public Object getPendingAuditPrices(Long current, Long size) {
        logger.info("获取待审核价格列表 - 页码: {}, 大小: {}", current, size);
        
        try {
            // 参数验证
            if (current == null || current < 1) {
                current = 1L;
            }
            if (size == null || size < 1) {
                size = 10L;
            }
            
            // 获取所有待审核的价格历史记录
            List<PriceHistory> pendingPrices = priceHistoryMap.values().stream()
                .filter(history -> "PENDING".equals(history.getAuditStatus()))
                .sorted(Comparator.comparing(PriceHistory::getCreateTime).reversed())
                .collect(Collectors.toList());
            
            // 分页处理
            int total = pendingPrices.size();
            int start = (int) ((current - 1) * size);
            int end = Math.min(start + size.intValue(), total);
            
            List<PriceHistory> pagedPrices = new ArrayList<>();
            if (start < total) {
                pagedPrices = pendingPrices.subList(start, end);
            }
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("records", pagedPrices);
            result.put("total", total);
            result.put("current", current);
            result.put("size", size);
            result.put("pages", (total + size.intValue() - 1) / size.intValue());
            
            logger.info("获取待审核价格列表成功 - 总数: {}, 当前页: {}", total, current);
            return result;
            
        } catch (Exception e) {
            logger.error("获取待审核价格列表失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("records", new ArrayList<>());
            errorResult.put("total", 0);
            errorResult.put("current", current);
            errorResult.put("size", size);
            errorResult.put("pages", 0);
            return errorResult;
        }
    }
}