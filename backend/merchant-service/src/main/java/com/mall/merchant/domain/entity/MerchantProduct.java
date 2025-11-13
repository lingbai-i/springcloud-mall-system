package com.mall.merchant.domain.entity;

import com.mall.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * 商家商品实体类
 * 存储商家发布的商品信息
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "merchant_product")
public class MerchantProduct extends BaseEntity {

    /**
     * 商家ID
     */
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    /**
     * 商品名称
     */
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    /**
     * 商品SKU编码
     */
    @Column(name = "sku", length = 100)
    private String sku;

    /**
     * 商品分类ID
     */
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    /**
     * 商品品牌
     */
    @Column(name = "brand", length = 100)
    private String brand;

    /**
     * 商品价格
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * 市场价格
     */
    @Column(name = "market_price", precision = 10, scale = 2)
    private BigDecimal marketPrice;

    /**
     * 成本价格
     */
    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    /**
     * 库存数量
     */
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    /**
     * 预警库存
     */
    @Column(name = "warning_stock", nullable = false)
    private Integer warningStock = 10;

    /**
     * 商品主图
     */
    @Column(name = "main_image", length = 200)
    private String mainImage;

    /**
     * 商品图片（多张，逗号分隔）
     */
    @Column(name = "images", length = 1000)
    private String images;

    /**
     * 商品详情
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 商品规格参数（JSON格式）
     */
    @Column(name = "specifications", columnDefinition = "TEXT")
    private String specifications;

    /**
     * 商品属性（JSON格式）
     */
    @Column(name = "attributes", columnDefinition = "TEXT")
    private String attributes;

    /**
     * 商品重量（克）
     */
    @Column(name = "weight")
    private Integer weight;

    /**
     * 商品尺寸（长x宽x高，单位：厘米）
     */
    @Column(name = "dimensions", length = 50)
    private String dimensions;

    /**
     * 商品状态：0-下架，1-上架，2-草稿
     */
    @Column(name = "status", nullable = false)
    private Integer status = 2;

    /**
     * 是否推荐：0-否，1-是
     */
    @Column(name = "is_recommended", nullable = false)
    private Integer isRecommended = 0;

    /**
     * 是否新品：0-否，1-是
     */
    @Column(name = "is_new", nullable = false)
    private Integer isNew = 0;

    /**
     * 是否热销：0-否，1-是
     */
    @Column(name = "is_hot", nullable = false)
    private Integer isHot = 0;

    /**
     * 销售数量
     */
    @Column(name = "sales_count", nullable = false)
    private Integer salesCount = 0;

    /**
     * 浏览次数
     */
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    /**
     * 收藏次数
     */
    @Column(name = "favorite_count", nullable = false)
    private Integer favoriteCount = 0;

    /**
     * 评分（1-5分）
     */
    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating;

    /**
     * 评价数量
     */
    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    /**
     * 排序值
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /**
     * SEO关键词
     */
    @Column(name = "seo_keywords", length = 200)
    private String seoKeywords;

    /**
     * SEO描述
     */
    @Column(name = "seo_description", length = 500)
    private String seoDescription;

    // 业务方法

    /**
     * 获取商品状态文本
     * 
     * @return 状态文本
     */
    public String getStatusText() {
        switch (status) {
            case 0:
                return "下架";
            case 1:
                return "上架";
            case 2:
                return "草稿";
            default:
                return "未知";
        }
    }

    /**
     * 判断是否上架
     * 
     * @return 是否上架
     */
    public boolean isOnSale() {
        return status != null && status == 1;
    }

    /**
     * 判断是否有库存
     * 
     * @return 是否有库存
     */
    public boolean hasStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    /**
     * 判断是否库存不足
     * 
     * @return 是否库存不足
     */
    public boolean isLowStock() {
        return stockQuantity != null && warningStock != null && stockQuantity <= warningStock;
    }

    /**
     * 增加销售数量
     * 
     * @param quantity 销售数量
     */
    public void increaseSales(Integer quantity) {
        if (quantity != null && quantity > 0) {
            this.salesCount = (this.salesCount == null ? 0 : this.salesCount) + quantity;
        }
    }

    /**
     * 减少库存
     * 
     * @param quantity 减少数量
     * @return 是否成功
     */
    public boolean decreaseStock(Integer quantity) {
        if (quantity != null && quantity > 0 && hasStock() && stockQuantity >= quantity) {
            this.stockQuantity -= quantity;
            return true;
        }
        return false;
    }

    /**
     * 增加库存
     * 
     * @param quantity 增加数量
     */
    public void increaseStock(Integer quantity) {
        if (quantity != null && quantity > 0) {
            this.stockQuantity += quantity;
        }
    }

    /**
     * 设置评分
     * 
     * @param rating 评分
     */
    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    /**
     * 设置是否推荐
     * 
     * @param isRecommended 是否推荐
     */
    public void setIsRecommended(Integer isRecommended) {
        this.isRecommended = isRecommended;
    }

    /**
     * 设置是否新品
     * 
     * @param isNew 是否新品
     */
    public void setIsNew(Integer isNew) {
        this.isNew = isNew;
    }

    /**
     * 设置是否热销
     * 
     * @param isHot 是否热销
     */
    public void setIsHot(Integer isHot) {
        this.isHot = isHot;
    }

    /**
     * 设置排序值
     * 
     * @param sortOrder 排序值
     */
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * 获取商家ID
     * 
     * @return 商家ID
     */
    public Long getMerchantId() {
        return this.merchantId;
    }

    /**
     * 获取商品名称
     * 
     * @return 商品名称
     */
    public String getProductName() {
        return this.productName;
    }

    /**
     * 获取分类ID
     * 
     * @return 分类ID
     */
    public Long getCategoryId() {
        return this.categoryId;
    }

    /**
     * 获取品牌
     * 
     * @return 品牌
     */
    public String getBrand() {
        return this.brand;
    }

    /**
     * 获取价格
     * 
     * @return 价格
     */
    public BigDecimal getPrice() {
        return this.price;
    }

    /**
     * 获取市场价
     * 
     * @return 市场价
     */
    public BigDecimal getMarketPrice() {
        return this.marketPrice;
    }

    /**
     * 获取成本价
     * 
     * @return 成本价
     */
    public BigDecimal getCostPrice() {
        return this.costPrice;
    }

    /**
     * 获取库存数量
     * 
     * @return 库存数量
     */
    public Integer getStockQuantity() {
        return this.stockQuantity;
    }

    /**
     * 获取预警库存
     * 
     * @return 预警库存
     */
    public Integer getWarningStock() {
        return this.warningStock;
    }

    /**
     * 获取主图
     * 
     * @return 主图
     */
    public String getMainImage() {
        return this.mainImage;
    }

    /**
     * 获取图片列表
     * 
     * @return 图片列表
     */
    public String getImages() {
        return this.images;
    }

    /**
     * 获取描述
     * 
     * @return 描述
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * 获取规格
     * 
     * @return 规格
     */
    public String getSpecifications() {
        return this.specifications;
    }

    /**
     * 获取属性
     * 
     * @return 属性
     */
    public String getAttributes() {
        return this.attributes;
    }

    /**
     * 获取重量
     * 
     * @return 重量
     */
    public Integer getWeight() {
        return this.weight;
    }

    /**
     * 获取尺寸
     * 
     * @return 尺寸
     */
    public String getDimensions() {
        return this.dimensions;
    }

    /**
     * 获取状态
     * 
     * @return 状态
     */
    public Integer getStatus() {
        return this.status;
    }

    /**
     * 获取是否推荐
     * 
     * @return 是否推荐
     */
    public Integer getIsRecommended() {
        return this.isRecommended;
    }

    /**
     * 获取是否新品
     * 
     * @return 是否新品
     */
    public Integer getIsNew() {
        return this.isNew;
    }

    /**
     * 获取是否热销
     * 
     * @return 是否热销
     */
    public Integer getIsHot() {
        return this.isHot;
    }

    /**
     * 获取销量
     * 
     * @return 销量
     */
    public Integer getSalesCount() {
        return this.salesCount;
    }

    /**
     * 获取浏览量
     * 
     * @return 浏览量
     */
    public Integer getViewCount() {
        return this.viewCount;
    }

    /**
     * 获取收藏量
     * 
     * @return 收藏量
     */
    public Integer getFavoriteCount() {
        return this.favoriteCount;
    }

    /**
     * 获取评分
     * 
     * @return 评分
     */
    public BigDecimal getRating() {
        return this.rating;
    }

    /**
     * 获取评价数量
     * 
     * @return 评价数量
     */
    public Integer getReviewCount() {
        return this.reviewCount;
    }

    /**
     * 获取排序值
     * 
     * @return 排序值
     */
    public Integer getSortOrder() {
        return this.sortOrder;
    }

    /**
     * 获取SEO关键词
     * 
     * @return SEO关键词
     */
    public String getSeoKeywords() {
        return this.seoKeywords;
    }

    /**
     * 获取SEO描述
     * 
     * @return SEO描述
     */
    public String getSeoDescription() {
        return this.seoDescription;
    }

    /**
     * 设置商品名称
     * 
     * @param productName 商品名称
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * 设置分类ID
     * 
     * @param categoryId 分类ID
     */
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * 设置品牌
     * 
     * @param brand 品牌
     */
    public void setBrand(String brand) {
        this.brand = brand;
    }

    /**
     * 设置价格
     * 
     * @param price 价格
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * 设置市场价
     * 
     * @param marketPrice 市场价
     */
    public void setMarketPrice(BigDecimal marketPrice) {
        this.marketPrice = marketPrice;
    }

    /**
     * 设置成本价
     * 
     * @param costPrice 成本价
     */
    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    /**
     * 设置库存数量
     * 
     * @param stockQuantity 库存数量
     */
    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    /**
     * 设置预警库存
     * 
     * @param warningStock 预警库存
     */
    public void setWarningStock(Integer warningStock) {
        this.warningStock = warningStock;
    }

    /**
     * 设置主图
     * 
     * @param mainImage 主图
     */
    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    /**
     * 设置图片列表
     * 
     * @param images 图片列表
     */
    public void setImages(String images) {
        this.images = images;
    }

    /**
     * 设置描述
     * 
     * @param description 描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 设置规格
     * 
     * @param specifications 规格
     */
    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    /**
     * 设置属性
     * 
     * @param attributes 属性
     */
    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    /**
     * 设置重量
     * 
     * @param weight 重量
     */
    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    /**
     * 设置尺寸
     * 
     * @param dimensions 尺寸
     */
    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    /**
     * 设置状态
     * 
     * @param status 状态
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 设置销量
     * 
     * @param salesCount 销量
     */
    public void setSalesCount(Integer salesCount) {
        this.salesCount = salesCount;
    }

    /**
     * 设置浏览量
     * 
     * @param viewCount 浏览量
     */
    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    /**
     * 设置收藏量
     * 
     * @param favoriteCount 收藏量
     */
    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    /**
     * 设置评价数量
     * 
     * @param reviewCount 评价数量
     */
    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    /**
     * 设置SEO关键词
     * 
     * @param seoKeywords SEO关键词
     */
    public void setSeoKeywords(String seoKeywords) {
        this.seoKeywords = seoKeywords;
    }

    /**
     * 设置SEO描述
     * 
     * @param seoDescription SEO描述
     */
    public void setSeoDescription(String seoDescription) {
        this.seoDescription = seoDescription;
    }

    /**
     * 设置商家ID
     * 
     * @param merchantId 商家ID
     */
    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }
}