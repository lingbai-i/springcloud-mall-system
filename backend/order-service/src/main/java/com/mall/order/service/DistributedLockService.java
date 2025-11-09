package com.mall.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁服务
 * 基于Redis实现分布式锁机制
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockService {
    
    private final StringRedisTemplate redisTemplate;
    
    private static final String LOCK_PREFIX = "lock:";
    private static final Long UNLOCK_SUCCESS = 1L;
    
    /**
     * 获取订单创建锁
     * 
     * @param userId 用户ID
     * @return 锁信息数组 [lockKey, lockValue]
     */
    public String[] getOrderCreateLock(Long userId) {
        String lockKey = LOCK_PREFIX + "order:create:" + userId;
        String lockValue = UUID.randomUUID().toString();
        return new String[]{lockKey, lockValue};
    }
    
    /**
     * 获取订单取消锁
     * 
     * @param orderId 订单ID
     * @return 锁信息数组 [lockKey, lockValue]
     */
    public String[] getOrderCancelLock(Long orderId) {
        String lockKey = LOCK_PREFIX + "order:cancel:" + orderId;
        String lockValue = UUID.randomUUID().toString();
        return new String[]{lockKey, lockValue};
    }
    
    /**
     * 获取订单支付锁
     * 
     * @param orderNo 订单号
     * @return 锁信息数组 [lockKey, lockValue]
     */
    public String[] getOrderPaymentLock(String orderNo) {
        String lockKey = LOCK_PREFIX + "order:pay:" + orderNo;
        String lockValue = UUID.randomUUID().toString();
        return new String[]{lockKey, lockValue};
    }
    
    /**
     * 获取订单状态更新锁
     * 
     * @param orderId 订单ID
     * @return 锁信息数组 [lockKey, lockValue]
     */
    public String[] getOrderStatusLock(Long orderId) {
        String lockKey = LOCK_PREFIX + "order:status:" + orderId;
        String lockValue = UUID.randomUUID().toString();
        return new String[]{lockKey, lockValue};
    }
    
    /**
     * 尝试获取锁
     * 
     * @param lockKey 锁key
     * @param lockValue 锁value（用于释放锁时验证）
     * @param expireSeconds 过期时间（秒）
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, String lockValue, long expireSeconds) {
        try {
            Boolean result = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, lockValue, expireSeconds, TimeUnit.SECONDS);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("获取分布式锁失败: lockKey={}", lockKey, e);
            return false;
        }
    }
    
    /**
     * 释放锁
     * 使用Lua脚本保证原子性
     * 
     * @param lockKey 锁key
     * @param lockValue 锁value
     * @return 是否释放成功
     */
    public boolean unlock(String lockKey, String lockValue) {
        try {
            String luaScript = 
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "    return redis.call('del', KEYS[1]) " +
                "else " +
                "    return 0 " +
                "end";
            
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(luaScript);
            redisScript.setResultType(Long.class);
            
            Long result = redisTemplate.execute(redisScript, 
                    Collections.singletonList(lockKey), lockValue);
            
            return UNLOCK_SUCCESS.equals(result);
        } catch (Exception e) {
            log.error("释放分布式锁失败: lockKey={}", lockKey, e);
            return false;
        }
    }
    
    /**
     * 带锁执行业务逻辑
     * 
     * @param lockKey 锁key
     * @param lockValue 锁value
     * @param expireSeconds 过期时间（秒）
     * @param supplier 业务逻辑
     * @param <T> 返回类型
     * @return 业务逻辑执行结果
     */
    public <T> T executeWithLock(String lockKey, String lockValue, 
                                   long expireSeconds, Supplier<T> supplier) {
        boolean locked = tryLock(lockKey, lockValue, expireSeconds);
        if (!locked) {
            log.warn("获取分布式锁失败，操作被拒绝: lockKey={}", lockKey);
            throw new RuntimeException("操作过于频繁，请稍后重试");
        }
        
        try {
            log.debug("成功获取分布式锁: lockKey={}", lockKey);
            return supplier.get();
        } finally {
            boolean unlocked = unlock(lockKey, lockValue);
            if (unlocked) {
                log.debug("成功释放分布式锁: lockKey={}", lockKey);
            } else {
                log.warn("释放分布式锁失败: lockKey={}", lockKey);
            }
        }
    }
}
