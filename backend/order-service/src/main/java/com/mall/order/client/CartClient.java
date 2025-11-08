package com.mall.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 购物车服务客户端
 * 用于调用购物车服务的相关接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@FeignClient(name = "cart-service", path = "/api/cart")
public interface CartClient {
    
    /**
     * 获取用户购物车信息
     * 
     * @param userId 用户ID
     * @return 购物车信息
     */
    @GetMapping("/{userId}")
    Map<String, Object> getCart(@PathVariable("userId") Long userId);
    
    /**
     * 获取用户选中的购物车项
     * 
     * @param userId 用户ID
     * @param selected 是否选中
     * @return 选中的购物车项列表
     */
    @GetMapping("/{userId}/items")
    List<Map<String, Object>> getCartItems(@PathVariable("userId") Long userId, 
                                          @RequestParam(value = "selected", defaultValue = "true") Boolean selected);
    
    /**
     * 清空用户购物车中的指定商品
     * 
     * @param userId 用户ID
     * @param productIds 商品ID列表
     * @return 清空结果
     */
    @DeleteMapping("/{userId}/items")
    Boolean clearCartItems(@PathVariable("userId") Long userId, @RequestParam("productIds") List<Long> productIds);
    
    /**
     * 清空用户购物车中的选中商品
     * 
     * @param userId 用户ID
     * @return 清空结果
     */
    @DeleteMapping("/{userId}/selected")
    Boolean clearSelectedItems(@PathVariable("userId") Long userId);
}