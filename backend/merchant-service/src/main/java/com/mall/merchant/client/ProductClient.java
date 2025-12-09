package com.mall.merchant.client;

import com.mall.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品服务 Feign 客户端
 * 调用 product-service 进行商品管理操作
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-12-01
 */
@FeignClient(name = "product-service", path = "/api")
public interface ProductClient {
    
    // ==================== 商品 CRUD ====================
    
    /**
     * 创建商品
     * 
     * @param product 商品信息
     * @return 创建结果
     */
    @PostMapping("/products")
    R<String> createProduct(@RequestBody Map<String, Object> product);
    
    /**
     * 更新商品
     * 
     * @param product 商品信息
     * @return 更新结果
     */
    @PutMapping("/products")
    R<String> updateProduct(@RequestBody Map<String, Object> product);
    
    /**
     * 删除商品
     * 
     * @param id 商品ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    R<String> deleteProduct(@PathVariable("id") Long id);
    
    /**
     * 根据ID获取商品
     * 
     * @param id 商品ID
     * @return 商品信息
     */
    @GetMapping("/{id}")
    R<Map<String, Object>> getProductById(@PathVariable("id") Long id);

    
    /**
     * 分页查询商品列表
     * 
     * @param current 当前页码
     * @param size 每页大小
     * @param categoryId 分类ID（可选）
     * @param merchantId 商家ID（可选）
     * @return 商品分页数据
     */
    @GetMapping("/products")
    R<Object> getProducts(@RequestParam(value = "current", defaultValue = "1") Long current,
                          @RequestParam(value = "size", defaultValue = "10") Long size,
                          @RequestParam(value = "categoryId", required = false) Long categoryId,
                          @RequestParam(value = "merchantId", required = false) Long merchantId);
    
    /**
     * 根据商家ID查询商品列表
     * 
     * @param merchantId 商家ID
     * @param current 当前页码
     * @param size 每页大小
     * @return 商品分页数据
     */
    @GetMapping("/products/merchant/{merchantId}")
    R<Object> getProductsByMerchantId(@PathVariable("merchantId") Long merchantId,
                                       @RequestParam(value = "current", defaultValue = "1") Long current,
                                       @RequestParam(value = "size", defaultValue = "10") Long size);
    
    /**
     * 搜索商品
     * 
     * @param current 当前页码
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @param merchantId 商家ID（可选）
     * @return 搜索结果
     */
    @GetMapping("/search")
    R<Object> searchProducts(@RequestParam(value = "current", defaultValue = "1") Long current,
                             @RequestParam(value = "size", defaultValue = "10") Long size,
                             @RequestParam("keyword") String keyword,
                             @RequestParam(value = "merchantId", required = false) Long merchantId);
    
    // ==================== 商品归属验证 ====================
    
    /**
     * 验证商品归属
     * 
     * @param productId 商品ID
     * @param merchantId 商家ID
     * @return 是否属于该商家
     */
    @GetMapping("/products/{productId}/ownership")
    R<Boolean> checkProductOwnership(@PathVariable("productId") Long productId,
                                     @RequestParam("merchantId") Long merchantId);
    
    // ==================== 商品状态管理 ====================
    
    /**
     * 更新商品状态（上架/下架）
     * 
     * @param id 商品ID
     * @param status 状态（0-下架，1-上架）
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    R<String> updateProductStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status);
    
    // ==================== 统计接口 ====================
    
    /**
     * 获取商品统计数据
     * 
     * @param merchantId 商家ID（可选，为空则统计全平台）
     * @return 统计数据
     */
    @GetMapping("/statistics")
    R<Map<String, Object>> getStatistics(@RequestParam(value = "merchantId", required = false) Long merchantId);
    
    /**
     * 获取销售统计数据
     * 
     * @param merchantId 商家ID（可选）
     * @return 销售统计数据
     */
    @GetMapping("/statistics/sales")
    R<Map<String, Object>> getSalesStatistics(@RequestParam(value = "merchantId", required = false) Long merchantId);
    
    // ==================== 库存预警 ====================
    
    /**
     * 获取库存预警商品列表
     * 
     * @return 库存预警商品列表
     */
    @GetMapping("/stock/warning")
    R<List<Map<String, Object>>> getStockWarningProducts();
    
    /**
     * 获取库存预警商品列表（支持商家筛选）
     * 
     * @param merchantId 商家ID（可选）
     * @return 库存预警商品列表
     */
    @GetMapping("/stock/warning/merchant")
    R<List<Map<String, Object>>> getStockWarningProductsByMerchant(
            @RequestParam(value = "merchantId", required = false) Long merchantId);
    
    // ==================== 热销商品 ====================
    
    /**
     * 获取热销商品列表
     * 
     * @param limit 限制数量
     * @return 热销商品列表
     */
    @GetMapping("/hot")
    R<List<Map<String, Object>>> getHotProducts(@RequestParam(value = "limit", defaultValue = "10") Integer limit);
    
    /**
     * 获取热销商品列表（支持商家筛选）
     * 
     * @param merchantId 商家ID（可选）
     * @param limit 限制数量
     * @return 热销商品列表
     */
    @GetMapping("/hot/merchant")
    R<List<Map<String, Object>>> getHotProductsByMerchant(
            @RequestParam(value = "merchantId", required = false) Long merchantId,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit);
    
    // ==================== 库存管理 ====================
    
    /**
     * 更新商品库存
     * 
     * @param productId 商品ID
     * @param quantity 新库存数量
     * @return 更新结果
     */
    @PutMapping("/{productId}/stock")
    R<String> updateStock(@PathVariable("productId") Long productId, @RequestParam("quantity") Integer quantity);
    
    // ==================== 价格管理 ====================
    
    /**
     * 更新商品价格
     * 
     * @param productId 商品ID
     * @param newPrice 新价格
     * @param reason 变更原因
     * @param operatorId 操作人ID
     * @return 更新结果
     */
    @PutMapping("/{productId}/price")
    R<String> updateProductPrice(@PathVariable("productId") Long productId,
                                 @RequestParam("newPrice") Double newPrice,
                                 @RequestParam(value = "reason", required = false) String reason,
                                 @RequestParam(value = "operatorId", required = false) Long operatorId);
}
