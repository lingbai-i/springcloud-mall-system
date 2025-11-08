package com.mall.product.domain.dto;

import com.mall.product.domain.entity.Product;
import com.mall.product.domain.entity.ProductSku;
import lombok.Data;

import java.util.List;

/**
 * 商品详情DTO
 * 包含商品基本信息和SKU列表
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Data
public class ProductDetailDto {
    
    /**
     * 商品基本信息
     */
    private Product product;
    
    /**
     * 商品SKU列表
     */
    private List<ProductSku> skuList;
    
    /**
     * 商品总库存
     */
    private Integer totalStock;
    
    /**
     * 最低价格
     */
    private Double minPrice;
    
    /**
     * 最高价格
     */
    private Double maxPrice;
    
    /**
     * 商品分类名称
     */
    private String categoryName;
    
    /**
     * 商品品牌名称
     */
    private String brandName;
    
    /**
     * 商品评价统计
     */
    private ProductRatingDto rating;
    
    @Data
    public static class ProductRatingDto {
        /**
         * 平均评分
         */
        private Double averageRating;
        
        /**
         * 评价总数
         */
        private Integer totalReviews;
        
        /**
         * 好评率
         */
        private Double positiveRate;
    }
}
    