package com.mall.cart.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.cart.domain.entity.CartItem;
import com.mall.cart.service.CartSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 购物车数据同步服务实现类
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Service
public class CartSyncServiceImpl implements CartSyncService {
    
    private static final Logger log = LoggerFactory.getLogger(CartSyncServiceImpl.class);
    
    private static final String CART_KEY_PREFIX = "cart:user:";
    private static final String CART_BACKUP_KEY_PREFIX = "cart:backup:user:";
    private static final long CART_EXPIRE_TIME = 7 * 24 * 60 * 60; // 7天过期时间
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public boolean syncCartToRedis(Long userId, List<CartItem> cartItems) {
        if (userId == null) {
            log.warn("同步购物车到Redis失败: 用户ID为空");
            return false;
        }
        
        try {
            String cartKey = CART_KEY_PREFIX + userId;
            String backupKey = CART_BACKUP_KEY_PREFIX + userId;
            
            // 先备份当前数据
            Object currentData = redisTemplate.opsForValue().get(cartKey);
            if (currentData != null) {
                redisTemplate.opsForValue().set(backupKey, currentData, CART_EXPIRE_TIME, TimeUnit.SECONDS);
                log.debug("已备份用户{}的购物车数据", userId);
            }
            
            // 同步新数据
            if (CollectionUtils.isEmpty(cartItems)) {
                redisTemplate.delete(cartKey);
                log.info("用户{}的购物车为空，已清除Redis数据", userId);
            } else {
                String cartJson = objectMapper.writeValueAsString(cartItems);
                redisTemplate.opsForValue().set(cartKey, cartJson, CART_EXPIRE_TIME, TimeUnit.SECONDS);
                log.info("成功同步用户{}的购物车数据到Redis，商品数量: {}", userId, cartItems.size());
            }
            
            return true;
        } catch (Exception e) {
            log.error("同步用户{}的购物车数据到Redis失败", userId, e);
            return false;
        }
    }
    
    @Override
    public List<CartItem> syncCartFromRedis(Long userId) {
        if (userId == null) {
            log.warn("从Redis同步购物车失败: 用户ID为空");
            return new ArrayList<>();
        }
        
        try {
            String cartKey = CART_KEY_PREFIX + userId;
            Object cartData = redisTemplate.opsForValue().get(cartKey);
            
            if (cartData == null) {
                log.debug("用户{}的购物车在Redis中不存在", userId);
                return new ArrayList<>();
            }
            
            String cartJson = cartData.toString();
            List<CartItem> cartItems = objectMapper.readValue(cartJson, new TypeReference<List<CartItem>>() {});
            
            log.info("成功从Redis同步用户{}的购物车数据，商品数量: {}", userId, cartItems.size());
            return cartItems;
        } catch (Exception e) {
            log.error("从Redis同步用户{}的购物车数据失败", userId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public boolean cleanExpiredCartData(Long userId) {
        if (userId == null) {
            log.warn("清理过期购物车数据失败: 用户ID为空");
            return false;
        }
        
        try {
            String cartKey = CART_KEY_PREFIX + userId;
            String backupKey = CART_BACKUP_KEY_PREFIX + userId;
            
            // 检查数据是否过期
            Long ttl = redisTemplate.getExpire(cartKey);
            if (ttl != null && ttl <= 0) {
                // 数据已过期，删除相关键
                redisTemplate.delete(cartKey);
                redisTemplate.delete(backupKey);
                log.info("已清理用户{}的过期购物车数据", userId);
                return true;
            }
            
            log.debug("用户{}的购物车数据未过期，TTL: {}秒", userId, ttl);
            return true;
        } catch (Exception e) {
            log.error("清理用户{}的过期购物车数据失败", userId, e);
            return false;
        }
    }
    
    @Override
    public int batchSyncCartData(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            log.warn("批量同步购物车数据失败: 用户ID列表为空");
            return 0;
        }
        
        int successCount = 0;
        for (Long userId : userIds) {
            try {
                // 从Redis获取当前数据并重新设置过期时间
                String cartKey = CART_KEY_PREFIX + userId;
                Object cartData = redisTemplate.opsForValue().get(cartKey);
                
                if (cartData != null) {
                    redisTemplate.expire(cartKey, CART_EXPIRE_TIME, TimeUnit.SECONDS);
                    successCount++;
                    log.debug("成功刷新用户{}的购物车数据过期时间", userId);
                }
            } catch (Exception e) {
                log.error("批量同步用户{}的购物车数据失败", userId, e);
            }
        }
        
        log.info("批量同步购物车数据完成，成功: {}/{}", successCount, userIds.size());
        return successCount;
    }
    
    @Override
    public boolean checkCartDataConsistency(Long userId) {
        if (userId == null) {
            log.warn("检查购物车数据一致性失败: 用户ID为空");
            return false;
        }
        
        try {
            String cartKey = CART_KEY_PREFIX + userId;
            String backupKey = CART_BACKUP_KEY_PREFIX + userId;
            
            Object currentData = redisTemplate.opsForValue().get(cartKey);
            Object backupData = redisTemplate.opsForValue().get(backupKey);
            
            // 如果都为空，认为是一致的
            if (currentData == null && backupData == null) {
                log.debug("用户{}的购物车数据一致（都为空）", userId);
                return true;
            }
            
            // 如果其中一个为空，认为不一致
            if (currentData == null || backupData == null) {
                log.warn("用户{}的购物车数据不一致（其中一个为空）", userId);
                return false;
            }
            
            // 比较数据内容
            boolean isConsistent = currentData.toString().equals(backupData.toString());
            if (isConsistent) {
                log.debug("用户{}的购物车数据一致", userId);
            } else {
                log.warn("用户{}的购物车数据不一致", userId);
            }
            
            return isConsistent;
        } catch (Exception e) {
            log.error("检查用户{}的购物车数据一致性失败", userId, e);
            return false;
        }
    }
    
    @Override
    public boolean repairCartDataInconsistency(Long userId) {
        if (userId == null) {
            log.warn("修复购物车数据不一致失败: 用户ID为空");
            return false;
        }
        
        try {
            String cartKey = CART_KEY_PREFIX + userId;
            String backupKey = CART_BACKUP_KEY_PREFIX + userId;
            
            Object currentData = redisTemplate.opsForValue().get(cartKey);
            Object backupData = redisTemplate.opsForValue().get(backupKey);
            
            // 如果当前数据存在，使用当前数据
            if (currentData != null) {
                redisTemplate.opsForValue().set(backupKey, currentData, CART_EXPIRE_TIME, TimeUnit.SECONDS);
                log.info("使用当前数据修复用户{}的购物车数据不一致问题", userId);
                return true;
            }
            
            // 如果当前数据不存在但备份数据存在，恢复备份数据
            if (backupData != null) {
                redisTemplate.opsForValue().set(cartKey, backupData, CART_EXPIRE_TIME, TimeUnit.SECONDS);
                log.info("使用备份数据修复用户{}的购物车数据不一致问题", userId);
                return true;
            }
            
            // 如果都不存在，清理相关键
            redisTemplate.delete(cartKey);
            redisTemplate.delete(backupKey);
            log.info("清理用户{}的空购物车数据", userId);
            return true;
            
        } catch (Exception e) {
            log.error("修复用户{}的购物车数据不一致失败", userId, e);
            return false;
        }
    }
}