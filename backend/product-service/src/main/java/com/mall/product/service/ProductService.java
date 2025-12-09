package com.mall.product.service;

import com.mall.product.domain.entity.Product;
import com.mall.product.domain.entity.ProductSku;
import com.mall.product.domain.entity.PriceHistory;
import com.mall.product.domain.entity.StockLog;
import com.mall.product.domain.dto.ProductDetailDto;
import com.mall.product.domain.dto.ProductQueryDto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品服务接口
 * 提供完整的商品管理功能，包括商品信息管理、库存管理、价格管理等
 * 
 * @author lingbai
 * @version 2.0
 * @since 2025-01-21
 */
public interface ProductService {
    
    // ==================== 商品基础管理 ====================
    
    /**
     * 根据分类ID分页查询商品
     * 
     * @param current 当前页码
     * @param size 每页大小
     * @param categoryId 分类ID
     * @return 商品分页数据
     */
    Object getProductsByCategoryId(Long current, Long size, Long categoryId);
    
    /**
     * 根据分类ID分页查询商品（支持商家筛选）
     * 
     * @param current 当前页码
     * @param size 每页大小
     * @param categoryId 分类ID（可选）
     * @param merchantId 商家ID（可选）
     * @return 商品分页数据
     */
    Object getProductsByCategoryId(Long current, Long size, Long categoryId, Long merchantId);
    
    /**
     * 搜索商品
     * 
     * @param current 当前页码
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @return 商品分页数据
     */
    Object searchProducts(Long current, Long size, String keyword);
    
    /**
     * 搜索商品（支持商家筛选）
     * 
     * @param current 当前页码
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @param merchantId 商家ID（可选）
     * @return 商品分页数据
     */
    Object searchProducts(Long current, Long size, String keyword, Long merchantId);
    
    /**
     * 根据商家ID分页查询商品
     * 
     * @param current 当前页码
     * @param size 每页大小
     * @param merchantId 商家ID
     * @return 商品分页数据
     */
    Object getProductsByMerchantId(Long current, Long size, Long merchantId);
    
    /**
     * 获取热销商品列表
     * 
     * @param limit 限制数量
     * @return 热销商品列表
     */
    List<Product> getHotProducts(Integer limit);
    
    /**
     * 获取推荐商品列表
     * 
     * @param limit 限制数量
     * @return 推荐商品列表
     */
    List<Product> getRecommendProducts(Integer limit);
    
    /**
     * 根据ID获取商品详情
     * 
     * @param id 商品ID
     * @return 商品详情
     */
    Product getProductById(Long id);
    
    /**
     * 根据ID列表批量获取商品
     * 
     * @param ids 商品ID列表
     * @return 商品列表
     */
    List<Product> getProductsByIds(List<Long> ids);
    
    /**
     * 获取商品详细信息（包含SKU列表）
     * 
     * @param id 商品ID
     * @return 商品详细信息
     */
    ProductDetailDto getProductDetail(Long id);
    
    /**
     * 高级商品搜索
     * 
     * @param queryDto 查询条件
     * @param current 当前页码
     * @param size 每页大小
     * @return 商品分页数据
     */
    Object advancedSearchProducts(ProductQueryDto queryDto, Long current, Long size);
    
    /**
     * 新增商品
     * 
     * @param product 商品信息
     * @return 是否成功
     */
    boolean addProduct(Product product);
    
    /**
     * 更新商品信息
     * 
     * @param product 商品信息
     * @return 是否成功
     */
    boolean updateProduct(Product product);
    
    /**
     * 删除商品
     * 
     * @param id 商品ID
     * @return 是否成功
     */
    boolean deleteProduct(Long id);
    
    /**
     * 批量删除商品
     * 
     * @param ids 商品ID列表
     * @return 是否成功
     */
    boolean batchDeleteProducts(List<Long> ids);
    
    /**
     * 更新商品状态
     * 
     * @param id 商品ID
     * @param status 状态（0-下架，1-上架）
     * @return 是否成功
     */
    boolean updateProductStatus(Long id, Integer status);
    
    // ==================== 多规格商品管理 ====================
    
    /**
     * 获取商品的SKU列表
     * 
     * @param productId 商品ID
     * @return SKU列表
     */
    List<ProductSku> getProductSkus(Long productId);
    
    /**
     * 新增商品SKU
     * 
     * @param sku SKU信息
     * @return 是否成功
     */
    boolean addProductSku(ProductSku sku);
    
    /**
     * 更新商品SKU
     * 
     * @param sku SKU信息
     * @return 是否成功
     */
    boolean updateProductSku(ProductSku sku);
    
    /**
     * 删除商品SKU
     * 
     * @param skuId SKU ID
     * @return 是否成功
     */
    boolean deleteProductSku(Long skuId);
    
    /**
     * 批量保存商品SKU
     * 
     * @param productId 商品ID
     * @param skus SKU列表
     * @return 是否成功
     */
    boolean batchSaveProductSkus(Long productId, List<ProductSku> skus);
    
    // ==================== 库存管理 ====================
    
    /**
     * 更新商品库存
     * 
     * @param productId 商品ID
     * @param quantity 库存变化数量
     * @return 是否更新成功
     */
    boolean updateStock(Long productId, Integer quantity);
    
    /**
     * 更新SKU库存
     * 
     * @param skuId SKU ID
     * @param quantity 库存变化数量
     * @return 是否更新成功
     */
    boolean updateSkuStock(Long skuId, Integer quantity);
    
    /**
     * 批量更新商品库存
     * 
     * @param stockUpdates 库存更新列表
     * @return 是否全部更新成功
     */
    boolean batchUpdateStock(List<StockUpdate> stockUpdates);
    
    /**
     * 获取库存预警商品列表
     * 
     * @return 库存预警商品列表
     */
    List<Product> getStockWarningProducts();
    
    /**
     * 获取库存变更日志
     * 
     * @param productId 商品ID
     * @param current 当前页码
     * @param size 每页大小
     * @return 库存变更日志分页数据
     */
    Object getStockLogs(Long productId, Long current, Long size);
    
    // ==================== 价格管理 ====================
    
    /**
     * 更新商品价格
     * 
     * @param productId 商品ID
     * @param newPrice 新价格
     * @param reason 变更原因
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    boolean updateProductPrice(Long productId, Double newPrice, String reason, Long operatorId);
    
    /**
     * 更新SKU价格
     * 
     * @param skuId SKU ID
     * @param newPrice 新价格
     * @param reason 变更原因
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    boolean updateSkuPrice(Long skuId, Double newPrice, String reason, Long operatorId);
    
    /**
     * 批量调价
     * 
     * @param priceUpdates 价格更新列表
     * @return 是否成功
     */
    boolean batchUpdatePrices(List<PriceUpdate> priceUpdates);
    
    /**
     * 获取价格变更历史
     * 
     * @param productId 商品ID
     * @param current 当前页码
     * @param size 每页大小
     * @return 价格变更历史分页数据
     */
    Object getPriceHistory(Long productId, Long current, Long size);
    
    // ==================== 内部类定义 ====================
    
    /**
     * 库存更新内部类
     */
    class StockUpdate {
        private Long productId;
        private Long skuId;
        private Integer quantity;
        private String reason;
        private Long operatorId;
        
        public StockUpdate() {}
        
        public StockUpdate(Long productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
        
        public StockUpdate(Long productId, Long skuId, Integer quantity, String reason, Long operatorId) {
            this.productId = productId;
            this.skuId = skuId;
            this.quantity = quantity;
            this.reason = reason;
            this.operatorId = operatorId;
        }
        
        // Getters and Setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Long getSkuId() { return skuId; }
        public void setSkuId(Long skuId) { this.skuId = skuId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public Long getOperatorId() { return operatorId; }
        public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    }
    
    /**
     * 价格更新内部类
     */
    class PriceUpdate {
        private Long productId;
        private Long skuId;
        private Double newPrice;
        private String reason;
        private Long operatorId;
        
        public PriceUpdate() {}
        
        public PriceUpdate(Long productId, Double newPrice, String reason, Long operatorId) {
            this.productId = productId;
            this.newPrice = newPrice;
            this.reason = reason;
            this.operatorId = operatorId;
        }
        
        public PriceUpdate(Long productId, Long skuId, Double newPrice, String reason, Long operatorId) {
            this.productId = productId;
            this.skuId = skuId;
            this.newPrice = newPrice;
            this.reason = reason;
            this.operatorId = operatorId;
        }
        
        // Getters and Setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Long getSkuId() { return skuId; }
        public void setSkuId(Long skuId) { this.skuId = skuId; }
        public Double getNewPrice() { return newPrice; }
        public void setNewPrice(Double newPrice) { this.newPrice = newPrice; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public Long getOperatorId() { return operatorId; }
        public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    }
}