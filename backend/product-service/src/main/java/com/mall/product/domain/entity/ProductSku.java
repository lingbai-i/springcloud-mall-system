package com.mall.product.domain.entity;

import com.mall.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * 商品SKU实体类
 * 用于支持多规格商品管理
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductSku extends BaseEntity {
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * SKU编码
     */
    private String skuCode;
    
    /**
     * SKU名称
     */
    private String skuName;
    
    /**
     * SKU价格
     */
    private Double price;
    
    /**
     * SKU原价
     */
    private Double originalPrice;
    
    /**
     * SKU成本价
     */
    private Double costPrice;
    
    /**
     * 价格版本号
     */
    private Long priceVersion;
    
    /**
     * SKU库存
     */
    private Integer stock;
    
    /**
     * SKU库存预警值
     */
    private Integer stockWarning;
    
    /**
     * SKU重量（克）
     */
    private Integer weight;
    
    /**
     * SKU条形码
     */
    private String barcode;
    
    /**
     * SKU图片URL
     */
    private String image;
    
    /**
     * SKU规格属性（JSON格式，如：{"颜色":"红色","尺寸":"L"}）
     */
    private String specValues;
    
    /**
     * SKU状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * SKU销量
     */
    private Integer sales;
    
    /**
     * 排序值
     */
    private Integer sort;
    
    /**
     * 商品名称（冗余字段，用于显示）
     */
    private String productName;
}