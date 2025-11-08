package com.mall.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁服务
 * 基于Redis实现的分布式锁，用于防止订单重复操作
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 * 
 * 修改日志：
 * V1.0 2025-01-21：初始版本，实现基于Redis的分布式锁功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 锁的前缀
     */
    private static final String LOCK_PREFIX = "order:lock:";

    /**
     * 默认锁超时时间（秒）
     */
    private static final long DEFAULT_TIMEOUT = 30L;

    /**
     * 释放锁的Lua脚本
     * 确保只有持有锁的线程才能释放锁
     */
    private static final String UNLOCK_SCRIPT = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

    /**
     * 获取分布式锁
     * 
     * @param lockKey 锁的键
     * @param lockValue 锁的值（用于标识持有锁的线程）
     * @param timeout 锁超时时间（秒）
     * @return 是否成功获取锁
     */
    public boolean tryLock(String lockKey, String lockValue, long timeout) {
        try {
            String key = LOCK_PREFIX + lockKey;
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, lockValue, timeout, TimeUnit.SECONDS);
            
            boolean success = Boolean.TRUE.equals(result);
            log.debug("尝试获取分布式锁: key={}, value={}, timeout={}s, success={}", 
                    key, lockValue, timeout, success);
            
            return success;
        } catch (Exception e) {
            log.error("获取分布式锁异常: key={}, error={}", lockKey, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取分布式锁（使用默认超时时间）
     * 
     * @param lockKey 锁的键
     * @param lockValue 锁的值
     * @return 是否成功获取锁
     */
    public boolean tryLock(String lockKey, String lockValue) {
        return tryLock(lockKey, lockValue, DEFAULT_TIMEOUT);
    }

    /**
     * 释放分布式锁
     * 
     * @param lockKey 锁的键
     * @param lockValue 锁的值（必须与获取锁时的值一致）
     * @return 是否成功释放锁
     */
    public boolean unlock(String lockKey, String lockValue) {
        try {
            String key = LOCK_PREFIX + lockKey;
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(UNLOCK_SCRIPT);
            script.setResultType(Long.class);
            
            Long result = redisTemplate.execute(script, Collections.singletonList(key), lockValue);
            boolean success = Long.valueOf(1L).equals(result);
            
            log.debug("释放分布式锁: key={}, value={}, success={}", key, lockValue, success);
            
            return success;
        } catch (Exception e) {
            log.error("释放分布式锁异常: key={}, error={}", lockKey, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取订单创建锁
     * 防止用户重复创建订单
     * 
     * @param userId 用户ID
     * @return 锁的键值对，[0]为key，[1]为value
     */
    public String[] getOrderCreateLock(Long userId) {
        String lockKey = "create_order:" + userId;
        String lockValue = Thread.currentThread().getName() + ":" + System.currentTimeMillis();
        return new String[]{lockKey, lockValue};
    }

    /**
     * 获取订单状态更新锁
     * 防止订单状态并发更新
     * 
     * @param orderId 订单ID
     * @return 锁的键值对，[0]为key，[1]为value
     */
    public String[] getOrderStatusLock(Long orderId) {
        String lockKey = "update_status:" + orderId;
        String lockValue = Thread.currentThread().getName() + ":" + System.currentTimeMillis();
        return new String[]{lockKey, lockValue};
    }

    /**
     * 获取订单支付锁
     * 防止订单重复支付
     * 
     * @param orderNo 订单号
     * @return 锁的键值对，[0]为key，[1]为value
     */
    public String[] getOrderPaymentLock(String orderNo) {
        String lockKey = "payment:" + orderNo;
        String lockValue = Thread.currentThread().getName() + ":" + System.currentTimeMillis();
        return new String[]{lockKey, lockValue};
    }

    /**
     * 获取订单取消锁
     * 防止订单重复取消
     * 
     * @param orderId 订单ID
     * @return 锁的键值对，[0]为key，[1]为value
     */
    public String[] getOrderCancelLock(Long orderId) {
        String lockKey = "cancel:" + orderId;
        String lockValue = Thread.currentThread().getName() + ":" + System.currentTimeMillis();
        return new String[]{lockKey, lockValue};
    }

    /**
     * 获取库存扣减锁
     * 防止库存并发扣减
     * 
     * @param productId 商品ID
     * @return 锁的键值对，[0]为key，[1]为value
     */
    public String[] getInventoryLock(Long productId) {
        String lockKey = "inventory:" + productId;
        String lockValue = Thread.currentThread().getName() + ":" + System.currentTimeMillis();
        return new String[]{lockKey, lockValue};
    }

    /**
     * 执行带锁的操作
     * 
     * @param lockKey 锁的键
     * @param lockValue 锁的值
     * @param timeout 锁超时时间（秒）
     * @param action 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     * @throws RuntimeException 如果获取锁失败或操作执行失败
     */
    public <T> T executeWithLock(String lockKey, String lockValue, long timeout, LockAction<T> action) {
        if (!tryLock(lockKey, lockValue, timeout)) {
            throw new RuntimeException("获取分布式锁失败: " + lockKey);
        }
        
        try {
            log.debug("成功获取分布式锁，开始执行操作: key={}", lockKey);
            return action.execute();
        } finally {
            unlock(lockKey, lockValue);
            log.debug("释放分布式锁: key={}", lockKey);
        }
    }

    /**
     * 执行带锁的操作（使用默认超时时间）
     * 
     * @param lockKey 锁的键
     * @param lockValue 锁的值
     * @param action 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     */
    public <T> T executeWithLock(String lockKey, String lockValue, LockAction<T> action) {
        return executeWithLock(lockKey, lockValue, DEFAULT_TIMEOUT, action);
    }

    /**
     * 锁操作接口
     * 
     * @param <T> 返回值类型
     */
    @FunctionalInterface
    public interface LockAction<T> {
        /**
         * 执行操作
         * 
         * @return 操作结果
         * @throws Exception 操作异常
         */
        T execute() throws Exception;
    }
}