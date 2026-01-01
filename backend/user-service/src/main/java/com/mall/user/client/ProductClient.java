package com.mall.user.client;

import com.mall.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商品服务 Feign 客户端
 * 用于收藏功能调用 product-service 获取商品信息和更新收藏计数
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-12-10
 */
@FeignClient(name = "product-service", path = "/api", contextId = "userProductClient")
public interface ProductClient {

    /**
     * 根据ID获取商品信息
     * 
     * @param id 商品ID
     * @return 商品信息
     */
    @GetMapping("/{id}")
    R<Map<String, Object>> getProductById(@PathVariable("id") Long id);

    /**
     * 增加商品收藏计数
     * 
     * @param id 商品ID
     * @return 操作结果
     */
    @PostMapping("/{id}/favorite-count/increase")
    R<Void> increaseFavoriteCount(@PathVariable("id") Long id);

    /**
     * 减少商品收藏计数
     * 
     * @param id 商品ID
     * @return 操作结果
     */
    @PostMapping("/{id}/favorite-count/decrease")
    R<Void> decreaseFavoriteCount(@PathVariable("id") Long id);
}
