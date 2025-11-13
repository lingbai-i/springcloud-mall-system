package com.mall.cart.client;

import com.mall.common.core.domain.R;
import com.mall.cart.domain.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 商品服务客户端
 * TODO: 临时调用merchant-service,后续统一迁移到product-service
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@FeignClient(name = "merchant-service", path = "/merchant/products")
public interface ProductClient {

    /**
     * 根据商品ID查询商品信息
     * 
     * @param productId 商品ID
     * @return 商品信息
     */
    @GetMapping("/{productId}")
    R<ProductDTO> getProductById(@PathVariable("productId") Long productId);
}