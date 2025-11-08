package com.mall.product.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 异常处理工具类
 * 提供统一的异常处理、重试机制、异常统计等功能
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Component
public class ExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    // 异常统计
    private final ConcurrentHashMap<String, AtomicLong> exceptionStats = new ConcurrentHashMap<>();
    
    // 重试配置
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final long DEFAULT_RETRY_DELAY = 1000; // 1秒

    /**
     * 执行带异常处理的操作
     * 
     * @param operationName 操作名称
     * @param operation 操作逻辑
     * @param <T> 返回类型
     * @return 操作结果
     */
    public <T> T executeWithExceptionHandling(String operationName, Supplier<T> operation) {
        try {
            return operation.get();
        } catch (Exception e) {
            handleException(operationName, e);
            throw new RuntimeException("操作执行失败: " + operationName, e);
        }
    }

    /**
     * 执行带重试机制的操作
     * 
     * @param operationName 操作名称
     * @param operation 操作逻辑
     * @param maxRetries 最大重试次数
     * @param retryDelay 重试延迟（毫秒）
     * @param <T> 返回类型
     * @return 操作结果
     */
    public <T> T executeWithRetry(String operationName, Supplier<T> operation, 
                                  int maxRetries, long retryDelay) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 1) {
                    Thread.sleep(retryDelay);
                }
                
                return operation.get();
                
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("操作被中断: " + operationName, ie);
            } catch (Exception e) {
                lastException = e;
                handleException(operationName + "_retry_" + attempt, e);
                
                if (attempt == maxRetries) {
                    logger.error("操作重试失败，已达到最大重试次数: {} ({}次)", operationName, maxRetries);
                    break;
                }
                
                if (!isRetryableException(e)) {
                    logger.error("遇到不可重试异常，停止重试: {}", operationName);
                    break;
                }
            }
        }
        
        throw new RuntimeException("操作执行失败: " + operationName, lastException);
    }

    /**
     * 执行带重试机制的操作（使用默认配置）
     */
    public <T> T executeWithRetry(String operationName, Supplier<T> operation) {
        return executeWithRetry(operationName, operation, DEFAULT_MAX_RETRIES, DEFAULT_RETRY_DELAY);
    }

    /**
     * 安全执行操作，返回默认值而不抛出异常
     * 
     * @param operationName 操作名称
     * @param operation 操作逻辑
     * @param defaultValue 默认值
     * @param <T> 返回类型
     * @return 操作结果或默认值
     */
    public <T> T executeSafely(String operationName, Supplier<T> operation, T defaultValue) {
        try {
            return operation.get();
        } catch (Exception e) {
            handleException(operationName, e);
            logger.warn("操作执行失败，返回默认值: {} - {}", operationName, e.getMessage());
            return defaultValue;
        }
    }

    /**
     * 执行带异常转换的操作
     * 
     * @param operationName 操作名称
     * @param operation 操作逻辑
     * @param exceptionMapper 异常转换器
     * @param <T> 返回类型
     * @return 操作结果
     */
    public <T> T executeWithExceptionMapping(String operationName, Supplier<T> operation,
                                           Function<Exception, RuntimeException> exceptionMapper) {
        try {
            return operation.get();
        } catch (Exception e) {
            handleException(operationName, e);
            throw exceptionMapper.apply(e);
        }
    }

    /**
     * 处理异常
     * 
     * @param operationName 操作名称
     * @param exception 异常
     */
    private void handleException(String operationName, Exception exception) {
        // 记录异常统计
        String exceptionType = exception.getClass().getSimpleName();
        exceptionStats.computeIfAbsent(operationName + "_" + exceptionType, k -> new AtomicLong(0))
                .incrementAndGet();
        
        // 记录总异常数
        exceptionStats.computeIfAbsent(operationName + "_total", k -> new AtomicLong(0))
                .incrementAndGet();
        
        // 记录异常日志
        logger.error("操作异常: {} - {}: {}", operationName, exceptionType, exception.getMessage());
        
        // 如果是严重异常，记录详细堆栈
        if (isCriticalException(exception)) {
            logger.error("严重异常详情:", exception);
        }
    }

    /**
     * 判断是否为可重试异常
     * 
     * @param exception 异常
     * @return 是否可重试
     */
    private boolean isRetryableException(Exception exception) {
        // 网络相关异常通常可以重试
        if (exception instanceof java.net.SocketTimeoutException ||
            exception instanceof java.net.ConnectException ||
            exception instanceof java.io.IOException) {
            return true;
        }
        
        // 数据库连接异常可以重试
        if (exception.getClass().getName().contains("SQLException") ||
            exception.getClass().getName().contains("DataAccessException")) {
            return true;
        }
        
        // 业务逻辑异常通常不应重试
        if (exception instanceof IllegalArgumentException ||
            exception instanceof IllegalStateException ||
            exception instanceof NullPointerException) {
            return false;
        }
        
        return true; // 默认可重试
    }

    /**
     * 判断是否为严重异常
     * 
     * @param exception 异常
     * @return 是否严重
     */
    private boolean isCriticalException(Exception exception) {
        return exception.getClass().getName().contains("OutOfMemoryError") ||
               exception.getClass().getName().contains("StackOverflowError") ||
               exception instanceof SecurityException ||
               exception.getClass().getName().contains("DataIntegrityViolationException");
    }

    /**
     * 获取异常统计
     * 
     * @param operationName 操作名称
     * @return 异常统计
     */
    public ExceptionStatistics getExceptionStatistics(String operationName) {
        long totalExceptions = exceptionStats.getOrDefault(operationName + "_total", new AtomicLong(0)).get();
        
        ConcurrentHashMap<String, Long> exceptionsByType = new ConcurrentHashMap<>();
        exceptionStats.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(operationName + "_") && 
                               !entry.getKey().equals(operationName + "_total"))
                .forEach(entry -> {
                    String exceptionType = entry.getKey().substring((operationName + "_").length());
                    exceptionsByType.put(exceptionType, entry.getValue().get());
                });
        
        return new ExceptionStatistics(operationName, totalExceptions, exceptionsByType);
    }

    /**
     * 获取所有异常统计
     */
    public ConcurrentHashMap<String, ExceptionStatistics> getAllExceptionStatistics() {
        ConcurrentHashMap<String, ExceptionStatistics> allStats = new ConcurrentHashMap<>();
        
        exceptionStats.keySet().stream()
                .filter(key -> key.endsWith("_total"))
                .map(key -> key.substring(0, key.length() - 6))
                .forEach(operationName -> {
                    allStats.put(operationName, getExceptionStatistics(operationName));
                });
        
        return allStats;
    }

    /**
     * 重置异常统计
     */
    public void resetExceptionStatistics() {
        exceptionStats.clear();
        logger.info("异常统计已重置");
    }

    /**
     * 异常统计数据类
     */
    public static class ExceptionStatistics {
        private final String operationName;
        private final long totalExceptions;
        private final ConcurrentHashMap<String, Long> exceptionsByType;

        public ExceptionStatistics(String operationName, long totalExceptions, 
                                 ConcurrentHashMap<String, Long> exceptionsByType) {
            this.operationName = operationName;
            this.totalExceptions = totalExceptions;
            this.exceptionsByType = exceptionsByType;
        }

        // Getters
        public String getOperationName() { return operationName; }
        public long getTotalExceptions() { return totalExceptions; }
        public ConcurrentHashMap<String, Long> getExceptionsByType() { return exceptionsByType; }

        @Override
        public String toString() {
            return String.format("ExceptionStatistics{operation='%s', total=%d, byType=%s}", 
                    operationName, totalExceptions, exceptionsByType);
        }
    }
}