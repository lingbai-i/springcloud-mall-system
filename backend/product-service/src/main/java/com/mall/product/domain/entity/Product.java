package com.mall.product.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.mall.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品实体类
 * 用于 product-service 统一管理商品数据
 * 
 * @author lingbai
 * @version 2.0
 * @since 2025-01-21
 * 修改日志：V2.0 2025-12-01：添加 merchantId 字段和 MyBatis-Plus 注解，支持数据库持久化
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@TableName("product")
public class Product extends BaseEntity {
    
    /**
     * 商品ID - 主键
     * 覆盖父类的 id 字段，使用 MyBatis-Plus 注解
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 商家ID - 关联商家
     */
    private Long merchantId;
    
    /**
     * 创建时间 - 覆盖父类字段，映射到正确的列名
     */
    @TableField("create_time")
    private java.time.LocalDateTime createTime;
    
    /**
     * 更新时间 - 覆盖父类字段，映射到正确的列名
     */
    @TableField("update_time")
    private java.time.LocalDateTime updateTime;
    
    /**
     * 删除标志 - 覆盖父类字段
     */
    @TableField("deleted")
    private Integer deleted;
    
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
     * 商品品牌ID（数据库中不存在此字段，使用 brandName 代替）
     */
    @TableField(exist = false)
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
     * 商品重量（克）- 数据库中不存在
     */
    @TableField(exist = false)
    private Integer weight;
    
    /**
     * 商品规格参数，JSON格式存储 - 数据库中不存在
     */
    @TableField(exist = false)
    private String specifications;
    
    /**
     * 商品SEO关键词 - 数据库中不存在
     */
    @TableField(exist = false)
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
     * 价格版本号 - 数据库中不存在
     */
    @TableField(exist = false)
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
     * 商品条形码 - 数据库中不存在
     */
    @TableField(exist = false)
    private String barcode;
    
    /**
     * 商品型号 - 数据库中不存在
     */
    @TableField(exist = false)
    private String model;
    
    /**
     * 是否支持多规格：0-单规格，1-多规格 - 数据库中不存在
     */
    @TableField(exist = false)
    private Integer hasSpecs;
    
    /**
     * 商品规格组合（多规格商品的SKU信息，JSON格式）- 数据库中不存在
     */
    @TableField(exist = false)
    private String skuList;
    
    /**
     * 商品属性（颜色、尺寸等规格属性，JSON格式）- 数据库中不存在
     */
    @TableField(exist = false)
    private String attributes;
    
    /**
     * 商品评分（1-5星）- 数据库中不存在
     */
    @TableField(exist = false)
    private Double rating;
    
    /**
     * 评价数量 - 数据库中不存在
     */
    @TableField(exist = false)
    private Integer reviewCount;
    
    /**
     * 商品标签（热销、新品、推荐等）- 数据库中不存在
     */
    @TableField(exist = false)
    private String tags;
    
    /**
     * 商品详情描述（富文本）- 数据库中不存在
     */
    @TableField(exist = false)
    private String detailContent;
    
    /**
     * 商品视频URL - 数据库中不存在
     */
    @TableField(exist = false)
    private String videoUrl;
    
    /**
     * 是否虚拟商品：0-实物，1-虚拟 - 数据库中不存在
     */
    @TableField(exist = false)
    private Integer isVirtual;
    
    /**
     * 运费模板ID - 数据库中不存在
     */
    @TableField(exist = false)
    private Long freightTemplateId;
    
    /**
     * 商品分类名称（冗余字段，用于显示）- 数据库中不存在
     */
    @TableField(exist = false)
    private String categoryName;
    
    /**
     * 品牌名称（冗余字段，用于显示）
     */
    private String brandName;
    
    /**
     * 是否推荐：0-否，1-是
     */
    private Boolean isRecommend;
    
    /**
     * 是否新品：0-否，1-是
     */
    private Boolean isNew;
    
    /**
     * 是否热销：0-否，1-是
     */
    private Boolean isHot;
    
    /**
     * 排序值
     */
    private Integer sortOrder;
    
    /**
     * 创建者 - 数据库中不存在
     */
    @TableField(exist = false)
    private String createBy;
    
    /**
     * 更新者 - 数据库中不存在
     */
    @TableField(exist = false)
    private String updateBy;
    
    /**
     * 版本号 - 数据库中不存在
     */
    @TableField(exist = false)
    private Integer version;
}