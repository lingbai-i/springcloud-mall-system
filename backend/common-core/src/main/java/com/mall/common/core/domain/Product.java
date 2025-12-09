package com.mall.common.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 统一商品实体类
 * 整合product-service和merchant-service的商品模型，消除数据不一致问题
 * 
 * 设计原则:
 * 1. 使用BigDecimal处理所有金额字段，避免精度丢失
 * 2. 统一字段命名规范，提高可维护性
 * 3. 提供完整的业务方法，封装领域逻辑
 * 4. 支持JPA和MyBatis-Plus双ORM框架
 * 
 * @author lingbai
 * @version 2.0 - 统一版本
 * @since 2025-11-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "products")
public class Product extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== 基本信息 ====================

    /**
     * 商品名称
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * 商品SKU编码
     */
    @Column(name = "sku", length = 100)
    private String sku;

    /**
     * 商品描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 商品详情内容（富文本）
     */
    @Column(name = "detail_content", columnDefinition = "TEXT")
    private String detailContent;

    // ==================== 分类和品牌 ====================

    /**
     * 商品分类ID
     */
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    /**
     * 商品分类名称（冗余字段，用于显示）
     */
    @Column(name = "category_name", length = 100)
    private String categoryName;

    /**
     * 品牌名称
     */
    @Column(name = "brand", length = 100)
    private String brand;

    /**
     * 品牌ID（可选，用于品牌关联）
     */
    @Column(name = "brand_id")
    private Long brandId;

    // ==================== 商家信息 ====================

    /**
     * 商家ID
     */
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    // ==================== 价格信息（使用BigDecimal确保精度） ====================

    /**
     * 商品价格
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * 市场价格/原价
     */
    @Column(name = "market_price", precision = 10, scale = 2)
    private BigDecimal marketPrice;

    /**
     * 成本价格（仅商家可见）
     */
    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    /**
     * 价格版本号（用于乐观锁控制价格变更）
     */
    @Column(name = "price_version")
    private Long priceVersion;

    // ==================== 库存信息 ====================

    /**
     * 库存数量
     */
    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    /**
     * 预警库存
     */
    @Column(name = "stock_warning", nullable = false)
    private Integer stockWarning = 10;

    /**
     * 商品单位（件、个、盒等）
     */
    @Column(name = "unit", length = 20)
    private String unit;

    // ==================== 图片和媒体 ====================

    /**
     * 商品主图URL
     */
    @Column(name = "main_image", length = 255)
    private String mainImage;

    /**
     * 商品图片列表（JSON格式）
     */
    @Column(name = "images", length = 1000)
    private String images;

    /**
     * 商品详情图片（JSON格式）
     */
    @Column(name = "detail_images", length = 1000)
    private String detailImages;

    /**
     * 商品视频URL
     */
    @Column(name = "video_url", length = 255)
    private String videoUrl;

    // ==================== 规格和属性 ====================

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
     * 是否支持多规格：0-单规格，1-多规格
     */
    @Column(name = "has_specs")
    private Integer hasSpecs = 0;

    /**
     * SKU列表（JSON格式）
     */
    @Column(name = "sku_list", columnDefinition = "TEXT")
    private String skuList;

    // ==================== 物流信息 ====================

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
     * 是否虚拟商品：0-实物，1-虚拟
     */
    @Column(name = "is_virtual")
    private Integer isVirtual = 0;

    /**
     * 运费模板ID
     */
    @Column(name = "freight_template_id")
    private Long freightTemplateId;

    /**
     * 商品条形码
     */
    @Column(name = "barcode", length = 100)
    private String barcode;

    /**
     * 商品型号
     */
    @Column(name = "model", length = 100)
    private String model;

    // ==================== 状态和标签 ====================

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
     * 商品标签（热销、新品、推荐等，逗号分隔）
     */
    @Column(name = "tags", length = 200)
    private String tags;

    // ==================== 销售和统计 ====================

    /**
     * 销售数量
     */
    @Column(name = "sales", nullable = false)
    private Integer sales = 0;

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

    // ==================== SEO和排序 ====================

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

    // ==================== 业务方法 ====================

    /**
     * 获取商品状态文本
     * 
     * @return 状态文本
     */
    public String getStatusText() {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "下架";
            case 1 -> "上架";
            case 2 -> "草稿";
            default -> "未知";
        };
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
        return stock != null && stock > 0;
    }

    /**
     * 判断是否库存不足
     * 
     * @return 是否库存不足
     */
    public boolean isLowStock() {
        return stock != null && stockWarning != null && stock <= stockWarning;
    }

    /**
     * 增加销售数量
     * 
     * @param quantity 销售数量
     */
    public void increaseSales(Integer quantity) {
        if (quantity != null && quantity > 0) {
            this.sales = (this.sales == null ? 0 : this.sales) + quantity;
        }
    }

    /**
     * 减少库存
     * 
     * @param quantity 减少数量
     * @return 是否成功
     */
    public boolean decreaseStock(Integer quantity) {
        if (quantity != null && quantity > 0 && hasStock() && stock >= quantity) {
            this.stock -= quantity;
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
            this.stock = (this.stock == null ? 0 : this.stock) + quantity;
        }
    }

    /**
     * 增加浏览量
     */
    public void increaseViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    /**
     * 增加收藏数
     */
    public void increaseFavoriteCount() {
        this.favoriteCount = (this.favoriteCount == null ? 0 : this.favoriteCount) + 1;
    }

    /**
     * 减少收藏数
     */
    public void decreaseFavoriteCount() {
        if (this.favoriteCount != null && this.favoriteCount > 0) {
            this.favoriteCount--;
        }
    }

    /**
     * 计算折扣
     * 
     * @return 折扣（例如：8.5表示85折）
     */
    public BigDecimal getDiscount() {
        if (price != null && marketPrice != null && marketPrice.compareTo(BigDecimal.ZERO) > 0) {
            return price.divide(marketPrice, 2, BigDecimal.ROUND_HALF_UP)
                       .multiply(BigDecimal.TEN);
        }
        return null;
    }

    /**
     * 判断是否有折扣
     * 
     * @return 是否有折扣
     */
    public boolean hasDiscount() {
        return marketPrice != null && price != null && 
               marketPrice.compareTo(price) > 0;
    }
}
