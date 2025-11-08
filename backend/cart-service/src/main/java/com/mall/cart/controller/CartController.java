package com.mall.cart.controller;

import com.mall.cart.domain.entity.CartItem;
import com.mall.cart.service.CartService;
import com.mall.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 购物车控制器
 * 
 * @author lingbai
 * @version 1.1
 * @since 2025-01-21
 * 
 * 修改日志：
 * V1.1 2025-01-21：添加JWT认证支持，从令牌中获取用户ID
 */
@RestController
@RequestMapping("/api/cart")
@Validated
@Tag(name = "购物车管理", description = "购物车相关接口")
public class CartController {
    
    private static final Logger log = LoggerFactory.getLogger(CartController.class);
    
    @Autowired
    private CartService cartService;

    /**
     * 获取当前认证用户的ID
     * 
     * @return 用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        throw new RuntimeException("用户未认证或认证信息无效");
    }

    /**
     * 添加商品到购物车
     */
    @Operation(summary = "添加商品到购物车")
    @PostMapping("/add")
    public R<Void> addToCart(
            @Parameter(description = "商品ID") @RequestParam @NotNull Long productId,
            @Parameter(description = "数量") @RequestParam @NotNull @Min(1) Integer quantity,
            @Parameter(description = "规格") @RequestParam(required = false) String specifications) {
        
        Long userId = getCurrentUserId();
        log.info("添加商品到购物车请求: userId={}, productId={}, quantity={}", userId, productId, quantity);
        return cartService.addToCart(userId, productId, quantity, specifications);
    }

    /**
     * 更新购物车商品数量
     */
    @Operation(summary = "更新购物车商品数量")
    @PutMapping("/update")
    public R<Void> updateQuantity(
            @Parameter(description = "商品ID") @RequestParam @NotNull Long productId,
            @Parameter(description = "新数量") @RequestParam @NotNull @Min(0) Integer quantity) {
        
        Long userId = getCurrentUserId();
        log.info("更新购物车商品数量请求: userId={}, productId={}, quantity={}", userId, productId, quantity);
        return cartService.updateQuantity(userId, productId, quantity);
    }

    /**
     * 从购物车删除商品
     */
    @Operation(summary = "从购物车删除商品")
    @DeleteMapping("/remove")
    public R<Void> removeFromCart(
            @Parameter(description = "商品ID") @RequestParam @NotNull Long productId) {
        
        Long userId = getCurrentUserId();
        log.info("从购物车删除商品请求: userId={}, productId={}", userId, productId);
        return cartService.removeFromCart(userId, productId);
    }

    /**
     * 获取用户购物车列表
     */
    @Operation(summary = "获取用户购物车列表")
    @GetMapping("/list")
    public R<List<CartItem>> getCartItems() {
        
        Long userId = getCurrentUserId();
        log.info("获取购物车列表请求: userId={}", userId);
        return cartService.getCartItems(userId);
    }

    /**
     * 清空购物车
     */
    @Operation(summary = "清空购物车")
    @DeleteMapping("/clear")
    public R<Void> clearCart() {
        
        Long userId = getCurrentUserId();
        log.info("清空购物车请求: userId={}", userId);
        return cartService.clearCart(userId);
    }

    /**
     * 选中/取消选中购物车商品
     */
    @Operation(summary = "选中/取消选中购物车商品")
    @PutMapping("/select")
    public R<Void> selectItem(
            @Parameter(description = "商品ID") @RequestParam @NotNull Long productId,
            @Parameter(description = "是否选中") @RequestParam @NotNull Boolean selected) {
        
        Long userId = getCurrentUserId();
        log.info("选中购物车商品请求: userId={}, productId={}, selected={}", userId, productId, selected);
        return cartService.selectItem(userId, productId, selected);
    }

    /**
     * 获取购物车商品数量
     */
    @Operation(summary = "获取购物车商品数量")
    @GetMapping("/count")
    public R<Integer> getCartCount() {
        
        Long userId = getCurrentUserId();
        log.info("获取购物车商品数量请求: userId={}", userId);
        return cartService.getCartCount(userId);
    }
}