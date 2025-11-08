package com.mall.cart.service;

import com.mall.cart.domain.entity.CartItem;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 购物车数据同步服务接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
public interface CartSyncService {
    
    /**
     * 同步用户购物车数据到Redis
     * 
     * @param userId 用户ID
     * @param cartItems 购物车商品列表
     * @return 同步是否成功
     */
    boolean syncCartToRedis(Long userId, List<CartItem> cartItems);
    
    /**
     * 从Redis同步用户购物车数据
     * 
     * @param userId 用户ID
     * @return 购物车商品列表
     */
    List<CartItem> syncCartFromRedis(Long userId);
    
    /**
     * 清理过期的购物车数据
     * 
     * @param userId 用户ID
     * @return 清理是否成功
     */
    boolean cleanExpiredCartData(Long userId);
    
    /**
     * 批量同步多个用户的购物车数据
     * 
     * @param userIds 用户ID列表
     * @return 同步成功的用户数量
     */
    int batchSyncCartData(List<Long> userIds);
    
    /**
     * 检查购物车数据一致性
     * 
     * @param userId 用户ID
     * @return 数据是否一致
     */
    boolean checkCartDataConsistency(Long userId);
    
    /**
     * 修复购物车数据不一致问题
     * 
     * @param userId 用户ID
     * @return 修复是否成功
     */
    boolean repairCartDataInconsistency(Long userId);
}