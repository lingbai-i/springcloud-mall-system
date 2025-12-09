package com.mall.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 商品服务客户端
 * 调用 product-service 获取商品信息和管理库存
 * 
 * @author lingbai
 * @version 2.0
 * @since 2025-01-21
 * 修改日志：V2.0 2025-12-01：从 merchant-service 改为调用 product-service
 */
@FeignClient(name = "product-service", path = "/api")
public interface ProductClient {
    
    /**
     * 根据商品ID获取商品信息
     * 
     * @param productId 商品ID
     * @return 商品信息
     */
    @GetMapping("/{productId}")
    Map<String, Object> getProduct(@PathVariable("productId") Long productId);
    
    /**
     * 批量获取商品信息
     * 
     * @param productIds 商品ID列表
     * @return 商品信息列表
     */
    @PostMapping("/products/batch")
    List<Map<String, Object>> getProductsBatch(@RequestBody List<Long> productIds);
    
    /**
     * 检查商品库存
     * 
     * @param productId 商品ID
     * @param quantity 需要的数量
     * @return 库存是否充足
     */
    @GetMapping("/products/{productId}/stock/check")
    Boolean checkStock(@PathVariable("productId") Long productId, @RequestParam("quantity") Integer quantity);
    
    /**
     * 扣减商品库存
     * 
     * @param stockDeductionRequest 库存扣减请求，包含productId、quantity、orderNo
     * @return 扣减结果
     */
    @PostMapping("/products/stock/deduct")
    Boolean deductStock(@RequestBody Map<String, Object> stockDeductionRequest);
    
    /**
     * 恢复商品库存
     * 
     * @param stockRestoreRequest 库存恢复请求，包含productId、quantity、orderNo
     * @return 恢复结果
     */
    @PostMapping("/products/stock/restore")
    Boolean restoreStock(@RequestBody Map<String, Object> stockRestoreRequest);
}
