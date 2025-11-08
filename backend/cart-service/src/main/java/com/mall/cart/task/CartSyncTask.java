package com.mall.cart.task;

import com.mall.cart.service.CartSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 购物车数据同步定时任务
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Component
public class CartSyncTask {
    
    private static final Logger log = LoggerFactory.getLogger(CartSyncTask.class);
    
    private static final String CART_KEY_PATTERN = "cart:user:*";
    
    @Autowired
    private CartSyncService cartSyncService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 每小时清理过期的购物车数据
     * 定时任务：每小时的第0分钟执行
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanExpiredCartData() {
        log.info("开始执行购物车过期数据清理任务");
        
        try {
            // 获取所有购物车键
            Set<String> cartKeys = redisTemplate.keys(CART_KEY_PATTERN);
            if (cartKeys == null || cartKeys.isEmpty()) {
                log.info("没有找到购物车数据，跳过清理任务");
                return;
            }
            
            int cleanedCount = 0;
            for (String cartKey : cartKeys) {
                try {
                    // 从键中提取用户ID
                    String userIdStr = cartKey.replace("cart:user:", "");
                    Long userId = Long.parseLong(userIdStr);
                    
                    // 清理过期数据
                    if (cartSyncService.cleanExpiredCartData(userId)) {
                        cleanedCount++;
                    }
                } catch (NumberFormatException e) {
                    log.warn("无效的购物车键格式: {}", cartKey);
                } catch (Exception e) {
                    log.error("清理购物车数据失败，键: {}", cartKey, e);
                }
            }
            
            log.info("购物车过期数据清理任务完成，处理键数: {}, 清理成功: {}", cartKeys.size(), cleanedCount);
        } catch (Exception e) {
            log.error("执行购物车过期数据清理任务失败", e);
        }
    }
    
    /**
     * 每6小时检查购物车数据一致性
     * 定时任务：每天的0点、6点、12点、18点执行
     */
    @Scheduled(cron = "0 0 0,6,12,18 * * ?")
    public void checkCartDataConsistency() {
        log.info("开始执行购物车数据一致性检查任务");
        
        try {
            // 获取所有购物车键
            Set<String> cartKeys = redisTemplate.keys(CART_KEY_PATTERN);
            if (cartKeys == null || cartKeys.isEmpty()) {
                log.info("没有找到购物车数据，跳过一致性检查任务");
                return;
            }
            
            int totalCount = 0;
            int inconsistentCount = 0;
            int repairedCount = 0;
            
            for (String cartKey : cartKeys) {
                try {
                    // 从键中提取用户ID
                    String userIdStr = cartKey.replace("cart:user:", "");
                    Long userId = Long.parseLong(userIdStr);
                    
                    totalCount++;
                    
                    // 检查数据一致性
                    if (!cartSyncService.checkCartDataConsistency(userId)) {
                        inconsistentCount++;
                        log.warn("发现用户{}的购物车数据不一致", userId);
                        
                        // 尝试修复数据不一致问题
                        if (cartSyncService.repairCartDataInconsistency(userId)) {
                            repairedCount++;
                            log.info("成功修复用户{}的购物车数据不一致问题", userId);
                        } else {
                            log.error("修复用户{}的购物车数据不一致问题失败", userId);
                        }
                    }
                } catch (NumberFormatException e) {
                    log.warn("无效的购物车键格式: {}", cartKey);
                } catch (Exception e) {
                    log.error("检查购物车数据一致性失败，键: {}", cartKey, e);
                }
            }
            
            log.info("购物车数据一致性检查任务完成，总数: {}, 不一致: {}, 修复成功: {}", 
                    totalCount, inconsistentCount, repairedCount);
        } catch (Exception e) {
            log.error("执行购物车数据一致性检查任务失败", e);
        }
    }
    
    /**
     * 每天凌晨2点批量刷新购物车数据过期时间
     * 定时任务：每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void batchRefreshCartExpireTime() {
        log.info("开始执行购物车数据过期时间批量刷新任务");
        
        try {
            // 获取所有购物车键
            Set<String> cartKeys = redisTemplate.keys(CART_KEY_PATTERN);
            if (cartKeys == null || cartKeys.isEmpty()) {
                log.info("没有找到购物车数据，跳过过期时间刷新任务");
                return;
            }
            
            List<Long> userIds = new ArrayList<>();
            for (String cartKey : cartKeys) {
                try {
                    // 从键中提取用户ID
                    String userIdStr = cartKey.replace("cart:user:", "");
                    Long userId = Long.parseLong(userIdStr);
                    userIds.add(userId);
                } catch (NumberFormatException e) {
                    log.warn("无效的购物车键格式: {}", cartKey);
                }
            }
            
            if (!userIds.isEmpty()) {
                int successCount = cartSyncService.batchSyncCartData(userIds);
                log.info("购物车数据过期时间批量刷新任务完成，总数: {}, 成功: {}", userIds.size(), successCount);
            }
        } catch (Exception e) {
            log.error("执行购物车数据过期时间批量刷新任务失败", e);
        }
    }
}