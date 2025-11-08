package com.mall.cart.service;

import com.mall.cart.domain.entity.CartItem;
import com.mall.common.core.domain.R;

import java.util.List;

/**
 * 购物车服务接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
public interface CartService {
    
    /**
     * 添加商品到购物车
     * 
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 数量
     * @param specifications 规格
     * @return 操作结果
     */
    R<Void> addToCart(Long userId, Long productId, Integer quantity, String specifications);
    
    /**
     * 更新购物车商品数量
     * 
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 新数量
     * @return 操作结果
     */
    R<Void> updateQuantity(Long userId, Long productId, Integer quantity);
    
    /**
     * 从购物车删除商品
     * 
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 操作结果
     */
    R<Void> removeFromCart(Long userId, Long productId);
    
    /**
     * 获取用户购物车列表
     * 
     * @param userId 用户ID
     * @return 购物车商品列表
     */
    R<List<CartItem>> getCartItems(Long userId);
    
    /**
     * 清空购物车
     * 
     * @param userId 用户ID
     * @return 操作结果
     */
    R<Void> clearCart(Long userId);
    
    /**
     * 选中/取消选中购物车商品
     * 
     * @param userId 用户ID
     * @param productId 商品ID
     * @param selected 是否选中
     * @return 操作结果
     */
    R<Void> selectItem(Long userId, Long productId, Boolean selected);
    
    /**
     * 获取购物车商品数量
     * 
     * @param userId 用户ID
     * @return 购物车商品数量
     */
    R<Integer> getCartCount(Long userId);
}