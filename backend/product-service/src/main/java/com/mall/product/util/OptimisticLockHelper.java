package com.mall.product.util;

import com.mall.common.core.domain.BaseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 乐观锁辅助工具类
 * 提供基于版本号的乐观锁机制，用于解决并发更新问题
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Component
public class OptimisticLockHelper {

    private static final Logger logger = LoggerFactory.getLogger(OptimisticLockHelper.class);
    
    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 3;
    
    /**
     * 重试间隔时间（毫秒）
     */
    private static final long RETRY_INTERVAL_MS = 10;
    
    /**
     * 实体版本缓存，用于模拟数据库中的版本控制
     * 在实际项目中，这应该通过数据库的乐观锁机制实现
     */
    private final ConcurrentHashMap<String, Integer> versionCache = new ConcurrentHashMap<>();

    /**
     * 执行带乐观锁的更新操作
     * 
     * @param entity 要更新的实体对象
     * @param updateFunction 更新操作函数
     * @param <T> 实体类型，必须继承BaseEntity
     * @return 更新结果
     */
    public <T extends BaseEntity> OptimisticLockResult executeWithOptimisticLock(
            T entity, Function<T, Boolean> updateFunction) {
        return executeWithOptimisticLock(entity, updateFunction, MAX_RETRY_COUNT, RETRY_INTERVAL_MS);
    }

    /**
     * 执行带乐观锁的更新操作（带重试参数）
     * 
     * @param entity 要更新的实体对象
     * @param updateFunction 更新操作函数
     * @param maxRetryCount 最大重试次数
     * @param retryIntervalMs 重试间隔毫秒
     * @param <T> 实体类型，必须继承BaseEntity
     * @return 更新结果
     */
    public <T extends BaseEntity> OptimisticLockResult executeWithOptimisticLock(
            T entity, Function<T, Boolean> updateFunction, int maxRetryCount, long retryIntervalMs) {
        
        if (entity == null) {
            return new OptimisticLockResult(false, "实体对象不能为空", 0);
        }
        
        String entityKey = generateEntityKey(entity);
        int retryCount = 0;
        
        while (retryCount <= maxRetryCount) {
            try {
                // 获取当前版本号
                Integer currentVersion = getCurrentVersion(entityKey, entity);
                
                // 检查版本是否匹配
                if (!isVersionMatched(entity, currentVersion)) {
                    logger.warn("乐观锁版本不匹配 - 实体: {}, 期望版本: {}, 当前版本: {}", 
                        entityKey, entity.getVersion(), currentVersion);
                    
                    if (retryCount >= maxRetryCount) {
                        return new OptimisticLockResult(false, "版本冲突，重试次数已达上限", retryCount);
                    }
                    
                    retryCount++;
                    Thread.sleep(retryIntervalMs);
                    continue;
                }
                
                // 执行更新操作
                Boolean updateResult = updateFunction.apply(entity);
                if (updateResult == null || !updateResult) {
                    return new OptimisticLockResult(false, "更新操作执行失败", retryCount);
                }
                
                // 更新版本号
                Integer newVersion = currentVersion + 1;
                entity.setVersion(newVersion);
                versionCache.put(entityKey, newVersion);
                
                logger.debug("乐观锁更新成功 - 实体: {}, 版本: {} -> {}, 重试次数: {}", 
                    entityKey, currentVersion, newVersion, retryCount);
                
                return new OptimisticLockResult(true, "更新成功", retryCount);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("乐观锁更新被中断 - 实体: {}", entityKey, e);
                return new OptimisticLockResult(false, "更新操作被中断", retryCount);
            } catch (Exception e) {
                logger.error("乐观锁更新异常 - 实体: {}", entityKey, e);
                return new OptimisticLockResult(false, "更新操作异常: " + e.getMessage(), retryCount);
            }
        }
        
        return new OptimisticLockResult(false, "达到最大重试次数", retryCount);
    }

    /**
     * 生成实体缓存键
     */
    private String generateEntityKey(BaseEntity entity) {
        return entity.getClass().getSimpleName() + "_" + entity.getId();
    }

    /**
     * 获取当前版本号
     * 优先从缓存获取，如果缓存中没有则使用实体的版本号
     */
    private Integer getCurrentVersion(String entityKey, BaseEntity entity) {
        Integer cachedVersion = versionCache.get(entityKey);
        if (cachedVersion != null) {
            return cachedVersion;
        } else {
            // 如果缓存中没有，使用实体当前版本号并缓存
            Integer entityVersion = entity.getVersion() != null ? entity.getVersion() : 0;
            versionCache.put(entityKey, entityVersion);
            return entityVersion;
        }
    }

    /**
     * 检查版本是否匹配
     */
    private boolean isVersionMatched(BaseEntity entity, Integer currentVersion) {
        Integer entityVersion = entity.getVersion() != null ? entity.getVersion() : 0;
        return entityVersion.equals(currentVersion);
    }

    /**
     * 清除指定实体的版本缓存
     */
    public void clearVersionCache(BaseEntity entity) {
        if (entity != null) {
            String entityKey = generateEntityKey(entity);
            versionCache.remove(entityKey);
            logger.debug("已清除实体版本缓存 - 实体: {}", entityKey);
        }
    }

    /**
     * 清除所有版本缓存
     */
    public void clearAllVersionCache() {
        versionCache.clear();
        logger.info("已清除所有实体版本缓存");
    }

    /**
     * 乐观锁操作结果类
     */
    public static class OptimisticLockResult {
        private final boolean success;
        private final String message;
        private final int retryCount;
        
        public OptimisticLockResult(boolean success, String message, int retryCount) {
            this.success = success;
            this.message = message;
            this.retryCount = retryCount;
        }
        
        public boolean isSuccess() { 
            return success; 
        }
        
        public String getMessage() { 
            return message; 
        }
        
        public String getErrorMessage() { 
            return message; 
        }
        
        public int getRetryCount() { 
            return retryCount; 
        }
    }
}