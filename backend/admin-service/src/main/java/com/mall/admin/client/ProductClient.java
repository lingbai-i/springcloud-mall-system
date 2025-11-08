package com.mall.admin.client;

import com.mall.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 商品服务客户端
 * 用于调用商品服务的相关接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@FeignClient(name = "product-service", path = "/api/products")
public interface ProductClient {
    
    /**
     * 获取商品统计数据
     * 
     * @param params 查询参数
     * @return 统计数据
     */
    @GetMapping("/stats")
    R<Map<String, Object>> getProductStats(@RequestParam Map<String, Object> params);
}