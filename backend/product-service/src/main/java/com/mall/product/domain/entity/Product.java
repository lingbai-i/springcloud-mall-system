package com.mall.product.domain.entity;

// import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品实体类
 * 
 * @author lingbai
 * @version 1.1
 * @since 2025-01-21
 */
@Data
@EqualsAndHashCode(callSuper = false)  // 修改为false避免循环引用
@JsonIgnoreProperties(ignoreUnknown = true)  // 忽略未知属性
// @TableName("t_product")
public class Product extends BaseEntity {
    
    /**
     * 商品名称
     */
    private String name;
    
    /**
     * 商品描述
     */
    private String description;
    
    /**
     * 商品价格
     */
    private Double price;
    
    /**
     * 商品库存
     */
    private Integer stock;
    
    /**
     * 商品分类ID
     */
    private Long categoryId;
    
    /**
     * 商品品牌ID
     */
    private Long brandId;
    
    /**
     * 商品主图URL
     */
    private String mainImage;
    
    /**
     * 商品详情图片URLs，JSON格式存储
     */
    private String detailImages;
    
    /**
     * 商品状态：0-下架，1-上架
     */
    private Integer status;
    
    /**
     * 商品销量
     */
    private Integer sales;
    
    /**
     * 商品重量（克）
     */
    private Integer weight;
    
    /**
     * 商品规格参数，JSON格式存储
     */
    private String specifications;
    
    /**
     * 商品SEO关键词
     */
    private String keywords;
    
    /**
     * 商品原价（用于显示折扣）
     */
    private Double originalPrice;
    
    /**
     * 商品成本价
     */
    private Double costPrice;
    
    /**
     * 价格版本号
     */
    private Long priceVersion;
    
    /**
     * 商品库存预警值
     */
    private Integer stockWarning;
    
    /**
     * 商品单位（件、个、盒等）
     */
    private String unit;
    
    /**
     * 商品条形码
     */
    private String barcode;
    
    /**
     * 商品型号
     */
    private String model;
    
    /**
     * 是否支持多规格：0-单规格，1-多规格
     */
    private Integer hasSpecs;
    
    /**
     * 商品规格组合（多规格商品的SKU信息，JSON格式）
     */
    private String skuList;
    
    /**
     * 商品属性（颜色、尺寸等规格属性，JSON格式）
     */
    private String attributes;
    
    /**
     * 商品评分（1-5星）
     */
    private Double rating;
    
    /**
     * 评价数量
     */
    private Integer reviewCount;
    
    /**
     * 商品标签（热销、新品、推荐等）
     */
    private String tags;
    
    /**
     * 商品详情描述（富文本）
     */
    private String detailContent;
    
    /**
     * 商品视频URL
     */
    private String videoUrl;
    
    /**
     * 是否虚拟商品：0-实物，1-虚拟
     */
    private Integer isVirtual;
    
    /**
     * 运费模板ID
     */
    private Long freightTemplateId;
    
    /**
     * 商品分类名称（冗余字段，用于显示）
     */
    private String categoryName;
    
    /**
     * 品牌名称（冗余字段，用于显示）
     */
    private String brandName;
}