package com.mall.product.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 商品查询DTO
 * 用于商品搜索和筛选条件
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Data
public class ProductQueryDto {
    
    /**
     * 商品名称关键词
     */
    private String keyword;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 品牌ID
     */
    private Long brandId;
    
    /**
     * 最低价格
     */
    private Double minPrice;
    
    /**
     * 最高价格
     */
    private Double maxPrice;
    
    /**
     * 商品状态：0-下架，1-上架
     */
    private Integer status;
    
    /**
     * 是否有库存：true-有库存，false-无库存
     */
    private Boolean hasStock;
    
    /**
     * 排序字段：price-价格，sales-销量，createTime-创建时间
     */
    private String sortField;
    
    /**
     * 排序方向：asc-升序，desc-降序
     */
    private String sortOrder;
    
    /**
     * 商品标签
     */
    private List<String> tags;
    
    /**
     * 商品属性筛选
     */
    private List<AttributeFilter> attributes;
    
    @Data
    public static class AttributeFilter {
        /**
         * 属性名称
         */
        private String name;
        
        /**
         * 属性值
         */
        private String value;
    }
}