package com.mall.product.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * 性能优化工具类
 * 提供缓存、性能监控、批量处理等优化功能
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Component
public class PerformanceOptimizer {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceOptimizer.class);

    // 性能监控指标
    private final ConcurrentHashMap<String, AtomicLong> performanceMetrics = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> responseTimeMetrics = new ConcurrentHashMap<>();
    
    // 缓存管理
    private final ConcurrentHashMap<String, CacheItem> cache = new ConcurrentHashMap<>();
    private static final long DEFAULT_CACHE_TTL = 300000; // 5分钟

    /**
     * 执行带性能监控的操作
     * 
     * @param operationName 操作名称
     * @param operation 操作逻辑
     * @param <T> 返回类型
     * @return 操作结果
     */
    public <T> T executeWithMonitoring(String operationName, Supplier<T> operation) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 增加调用次数
            performanceMetrics.computeIfAbsent(operationName + "_count", k -> new AtomicLong(0))
                    .incrementAndGet();
            
            // 执行操作
            T result = operation.get();
            
            // 记录成功次数
            performanceMetrics.computeIfAbsent(operationName + "_success", k -> new AtomicLong(0))
                    .incrementAndGet();
            
            return result;
            
        } catch (Exception e) {
            // 记录失败次数
            performanceMetrics.computeIfAbsent(operationName + "_failure", k -> new AtomicLong(0))
                    .incrementAndGet();
            
            logger.error("操作执行失败: {}", operationName, e);
            throw e;
            
        } finally {
            // 记录响应时间
            long responseTime = System.currentTimeMillis() - startTime;
            responseTimeMetrics.computeIfAbsent(operationName + "_response_time", k -> new AtomicLong(0))
                    .addAndGet(responseTime);
            
            // 记录平均响应时间
            long totalCount = performanceMetrics.get(operationName + "_count").get();
            long totalTime = responseTimeMetrics.get(operationName + "_response_time").get();
            long avgTime = totalTime / totalCount;
            
            // 如果响应时间超过500ms，记录警告
            if (responseTime > 500) {
                logger.warn("操作响应时间过长: {} - {}ms (平均: {}ms)", 
                    operationName, responseTime, avgTime);
            }
        }
    }

    /**
     * 获取或设置缓存
     * 
     * @param key 缓存键
     * @param supplier 数据提供者
     * @param ttl 缓存时间（毫秒）
     * @param <T> 数据类型
     * @return 缓存数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrSetCache(String key, Supplier<T> supplier, long ttl) {
        CacheItem item = cache.get(key);
        
        // 检查缓存是否存在且未过期
        if (item != null && !item.isExpired()) {
            logger.debug("缓存命中: {}", key);
            return (T) item.getValue();
        }
        
        // 缓存未命中或已过期，重新获取数据
        logger.debug("缓存未命中，重新获取数据: {}", key);
        T value = supplier.get();
        
        // 存储到缓存
        cache.put(key, new CacheItem(value, System.currentTimeMillis() + ttl));
        
        return value;
    }

    /**
     * 获取或设置缓存（使用默认TTL）
     */
    public <T> T getOrSetCache(String key, Supplier<T> supplier) {
        return getOrSetCache(key, supplier, DEFAULT_CACHE_TTL);
    }

    /**
     * 清除缓存
     * 
     * @param key 缓存键
     */
    public void clearCache(String key) {
        cache.remove(key);
        logger.debug("清除缓存: {}", key);
    }

    /**
     * 清除所有缓存
     */
    public void clearAllCache() {
        cache.clear();
        logger.info("清除所有缓存");
    }

    /**
     * 清除过期缓存
     */
    public void clearExpiredCache() {
        long currentTime = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        logger.debug("清除过期缓存完成");
    }

    /**
     * 获取性能指标
     * 
     * @param operationName 操作名称
     * @return 性能指标
     */
    public PerformanceMetrics getPerformanceMetrics(String operationName) {
        long count = performanceMetrics.getOrDefault(operationName + "_count", new AtomicLong(0)).get();
        long success = performanceMetrics.getOrDefault(operationName + "_success", new AtomicLong(0)).get();
        long failure = performanceMetrics.getOrDefault(operationName + "_failure", new AtomicLong(0)).get();
        long totalTime = responseTimeMetrics.getOrDefault(operationName + "_response_time", new AtomicLong(0)).get();
        
        double successRate = count > 0 ? (double) success / count * 100 : 0;
        double avgResponseTime = count > 0 ? (double) totalTime / count : 0;
        
        return new PerformanceMetrics(operationName, count, success, failure, 
                successRate, avgResponseTime, totalTime);
    }

    /**
     * 获取所有性能指标
     */
    public ConcurrentHashMap<String, PerformanceMetrics> getAllPerformanceMetrics() {
        ConcurrentHashMap<String, PerformanceMetrics> allMetrics = new ConcurrentHashMap<>();
        
        performanceMetrics.keySet().stream()
                .filter(key -> key.endsWith("_count"))
                .map(key -> key.substring(0, key.length() - 6))
                .forEach(operationName -> {
                    allMetrics.put(operationName, getPerformanceMetrics(operationName));
                });
        
        return allMetrics;
    }

    /**
     * 重置性能指标
     */
    public void resetPerformanceMetrics() {
        performanceMetrics.clear();
        responseTimeMetrics.clear();
        logger.info("性能指标已重置");
    }

    /**
     * 批量处理优化
     * 
     * @param items 待处理项目
     * @param processor 处理器
     * @param batchSize 批次大小
     * @param <T> 项目类型
     * @param <R> 结果类型
     * @return 处理结果
     */
    public <T, R> java.util.List<R> batchProcess(java.util.List<T> items, 
                                                 java.util.function.Function<java.util.List<T>, java.util.List<R>> processor,
                                                 int batchSize) {
        java.util.List<R> results = new java.util.ArrayList<>();
        
        for (int i = 0; i < items.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, items.size());
            java.util.List<T> batch = items.subList(i, endIndex);
            
            java.util.List<R> batchResults = processor.apply(batch);
            results.addAll(batchResults);
        }
        
        return results;
    }

    /**
     * 缓存项
     */
    private static class CacheItem {
        private final Object value;
        private final long expireTime;

        public CacheItem(Object value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }

        public Object getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    /**
     * 性能指标数据类
     */
    public static class PerformanceMetrics {
        private final String operationName;
        private final long totalCount;
        private final long successCount;
        private final long failureCount;
        private final double successRate;
        private final double avgResponseTime;
        private final long totalResponseTime;

        public PerformanceMetrics(String operationName, long totalCount, long successCount, 
                                long failureCount, double successRate, double avgResponseTime, 
                                long totalResponseTime) {
            this.operationName = operationName;
            this.totalCount = totalCount;
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.successRate = successRate;
            this.avgResponseTime = avgResponseTime;
            this.totalResponseTime = totalResponseTime;
        }

        // Getters
        public String getOperationName() { return operationName; }
        public long getTotalCount() { return totalCount; }
        public long getSuccessCount() { return successCount; }
        public long getFailureCount() { return failureCount; }
        public double getSuccessRate() { return successRate; }
        public double getAvgResponseTime() { return avgResponseTime; }
        public long getTotalResponseTime() { return totalResponseTime; }

        @Override
        public String toString() {
            return String.format("PerformanceMetrics{operation='%s', total=%d, success=%d, failure=%d, " +
                    "successRate=%.2f%%, avgResponseTime=%.2fms}", 
                    operationName, totalCount, successCount, failureCount, successRate, avgResponseTime);
        }
    }
}