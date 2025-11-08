package com.mall.cart.client;

import com.mall.common.core.domain.R;
import com.mall.cart.domain.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 商品服务客户端
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@FeignClient(name = "product-service", path = "/api/products")
public interface ProductClient {
    
    /**
     * 根据商品ID查询商品信息
     * 
     * @param productId 商品ID
     * @return 商品信息
     */
    @GetMapping("/{id}")
    R<ProductDTO> getProductById(@PathVariable("id") Long productId);
}