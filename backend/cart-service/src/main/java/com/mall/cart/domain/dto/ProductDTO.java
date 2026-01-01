package com.mall.cart.domain.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品信息DTO
 * 
 * @author lingbai
 * @version 1.1
 * @since 2025-01-21
 * 
 *        V1.1: 添加字段别名，兼容 merchant-service 返回的 productName 和 mainImage 字段
 */
public class ProductDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long id;

    /**
     * 商品名称
     * 兼容 merchant-service 的 productName 字段
     */
    @JsonAlias({ "productName", "name" })
    private String name;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 商品图片
     * 兼容 merchant-service 的 mainImage 字段
     */
    @JsonAlias({ "mainImage", "image" })
    private String image;

    /**
     * 商品主图 (merchant-service字段)
     */
    private String mainImage;

    /**
     * 商品状态 (1:上架 0:下架)
     */
    private Integer status;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 库存数量 (merchant-service字段)
     */
    @JsonAlias({ "stockQuantity", "stock" })
    private Integer stockQuantity;

    // 构造函数
    public ProductDTO() {
    }

    public ProductDTO(Long id, String name, BigDecimal price, String image, Integer status, Integer stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.image = image;
        this.mainImage = image;
        this.status = status;
        this.stock = stock;
        this.stockQuantity = stock;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 兼容 merchant-service 的 productName 字段
     */
    public void setProductName(String productName) {
        this.name = productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImage() {
        // 如果图片包含逗号，只取第一张
        if (image != null && image.contains(",")) {
            return image.split(",")[0].trim();
        }
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
        // 同步设置image字段
        if (this.image == null) {
            this.image = mainImage;
        }
    }

    public Integer getStock() {
        // 优先返回stockQuantity,兼容merchant-service
        return stockQuantity != null ? stockQuantity : stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
        this.stockQuantity = stock;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
        // 同步设置stock字段
        if (this.stock == null) {
            this.stock = stockQuantity;
        }
    }

    @Override
    public String toString() {
        return "ProductDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", image='" + image + '\'' +
                ", status=" + status +
                ", stock=" + stock +
                '}';
    }
}