package com.mall.product.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁工具类
 * 支持锁的自动过期、安全释放和本地锁降级
 * 
 * @author lingbai
 * @since 2025-01-21
 */
@Component
public class RedisDistributedLock {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisDistributedLock.class);
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    // 锁的前缀
    private static final String LOCK_PREFIX = "distributed_lock:";
    
    // 默认锁过期时间（秒）
    private static final int DEFAULT_EXPIRE_TIME = 30;
    
    // 默认获取锁的超时时间（毫秒）
    private static final long DEFAULT_TIMEOUT = 5000;
    
    // Lua脚本：安全释放锁
    private static final String UNLOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('del', KEYS[1]) " +
        "else " +
        "    return 0 " +
        "end";
    
    /**
     * 尝试获取分布式锁
     * 
     * @param lockKey 锁的键
     * @return 锁对象，如果获取失败返回null
     */
    public DistributedLockResult tryLock(String lockKey) {
        return tryLock(lockKey, DEFAULT_EXPIRE_TIME, DEFAULT_TIMEOUT);
    }
    
    /**
     * 尝试获取分布式锁
     * 
     * @param lockKey 锁的键名
     * @param expireSeconds 锁的过期时间（秒）
     * @param timeoutMs 获取锁的超时时间（毫秒）
     * @return 锁结果对象
     */
    public DistributedLockResult tryLock(String lockKey, int expireSeconds, long timeoutMs) {
        // 如果Redis不可用，直接使用本地锁
        if (redisTemplate == null) {
            logger.warn("Redis不可用，使用本地锁降级 - 锁键: {}", lockKey);
            return tryLocalLock(lockKey, timeoutMs);
        }
        
        String lockValue = java.util.UUID.randomUUID().toString();
        String fullLockKey = LOCK_PREFIX + lockKey;
        
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeoutMs;
        
        try {
            while (System.currentTimeMillis() < endTime) {
                // 尝试设置锁，如果键不存在则设置成功
                Boolean success = redisTemplate.opsForValue().setIfAbsent(
                    fullLockKey, lockValue, expireSeconds, TimeUnit.SECONDS);
                
                if (Boolean.TRUE.equals(success)) {
                    logger.debug("获取分布式锁成功 - 锁键: {}, 锁值: {}", fullLockKey, lockValue);
                    return new DistributedLockResult(true, fullLockKey, lockValue, null);
                }
                
                // 短暂等待后重试
                Thread.sleep(50);
            }
            
            // 超时未获取到锁，尝试本地锁降级
            logger.warn("Redis分布式锁获取超时，降级使用本地锁 - 锁键: {}", lockKey);
            return tryLocalLock(lockKey, timeoutMs);
            
        } catch (Exception e) {
            logger.error("获取Redis分布式锁异常，降级使用本地锁 - 锁键: {}", lockKey, e);
            return tryLocalLock(lockKey, timeoutMs);
        }
    }
    
    /**
     * 释放分布式锁
     * 使用Lua脚本确保原子性操作
     * 
     * @param lockResult 锁结果对象
     */
    public void unlock(DistributedLockResult lockResult) {
        if (lockResult == null || !lockResult.isLocked()) {
            return;
        }
        
        // 如果是本地锁降级，释放本地锁
        if (lockResult.getLocalLock() != null) {
            releaseLocalLock(lockResult.getLockKey());
            return;
        }
        
        // 如果Redis不可用，跳过释放
        if (redisTemplate == null) {
            logger.warn("Redis不可用，跳过锁释放 - 锁键: {}", lockResult.getLockKey());
            return;
        }
        
        try {
            // 使用Lua脚本确保只有锁的持有者才能释放锁
            String script = 
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "    return redis.call('del', KEYS[1]) " +
                "else " +
                "    return 0 " +
                "end";
            
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(script);
            redisScript.setResultType(Long.class);
            
            Long result = redisTemplate.execute(redisScript, 
                Collections.singletonList(lockResult.getLockKey()), 
                lockResult.getLockValue());
            
            if (result != null && result == 1) {
                logger.debug("成功释放分布式锁 - 锁键: {}", lockResult.getLockKey());
            } else {
                logger.warn("释放分布式锁失败，锁可能已过期或被其他线程释放 - 锁键: {}", 
                    lockResult.getLockKey());
            }
            
        } catch (Exception e) {
            logger.error("释放分布式锁异常 - 锁键: {}", lockResult.getLockKey(), e);
        }
    }
    
    // 本地锁映射，用于降级处理
    private final ConcurrentHashMap<String, ReentrantLock> localLocks = new ConcurrentHashMap<>();
    
    /**
     * 尝试获取本地锁（降级处理）
     * 
     * @param lockKey 锁键
     * @param timeoutMs 超时时间（毫秒）
     * @return 锁结果对象
     */
    private DistributedLockResult tryLocalLock(String lockKey, long timeoutMs) {
        ReentrantLock localLock = localLocks.computeIfAbsent(lockKey, k -> new ReentrantLock());
        
        try {
            boolean acquired = localLock.tryLock(timeoutMs, TimeUnit.MILLISECONDS);
            if (acquired) {
                logger.debug("获取本地锁成功 - 锁键: {}", lockKey);
                return new DistributedLockResult(true, lockKey, "local_lock", localLock);
            } else {
                logger.warn("获取本地锁超时 - 锁键: {}", lockKey);
                return new DistributedLockResult(false, lockKey, null, null);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("获取本地锁被中断 - 锁键: {}", lockKey);
            return new DistributedLockResult(false, lockKey, null, null);
        }
    }
    
    /**
     * 释放本地锁
     * 
     * @param lockKey 锁键
     */
    private void releaseLocalLock(String lockKey) {
        ReentrantLock localLock = localLocks.get(lockKey);
        if (localLock != null && localLock.isHeldByCurrentThread()) {
            localLock.unlock();
            logger.debug("释放本地锁成功 - 锁键: {}", lockKey);
        }
    }
    
    /**
     * 分布式锁结果对象
     */
    public static class DistributedLockResult {
        private final boolean locked;
        private final String lockKey;
        private final String lockValue;
        private final ReentrantLock localLock;
        
        public DistributedLockResult(boolean locked, String lockKey, String lockValue, ReentrantLock localLock) {
            this.locked = locked;
            this.lockKey = lockKey;
            this.lockValue = lockValue;
            this.localLock = localLock;
        }
        
        public boolean isLocked() {
            return locked;
        }
        
        public String getLockKey() {
            return lockKey;
        }
        
        public String getLockValue() {
            return lockValue;
        }
        
        public ReentrantLock getLocalLock() {
            return localLock;
        }
        
        @Override
        public String toString() {
            return String.format("DistributedLockResult{locked=%s, lockKey='%s', lockValue='%s', localLock=%s}", 
                locked, lockKey, lockValue, localLock != null ? "present" : "null");
        }
    }
}