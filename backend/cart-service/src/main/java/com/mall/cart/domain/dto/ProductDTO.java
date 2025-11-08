package com.mall.cart.domain.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品信息DTO
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
public class ProductDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 商品ID
     */
    private Long id;
    
    /**
     * 商品名称
     */
    private String name;
    
    /**
     * 商品价格
     */
    private BigDecimal price;
    
    /**
     * 商品图片
     */
    private String image;
    
    /**
     * 商品状态 (1:上架 0:下架)
     */
    private Integer status;
    
    /**
     * 库存数量
     */
    private Integer stock;
    
    // 构造函数
    public ProductDTO() {}
    
    public ProductDTO(Long id, String name, BigDecimal price, String image, Integer status, Integer stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.image = image;
        this.status = status;
        this.stock = stock;
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
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getImage() {
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
    
    public Integer getStock() {
        return stock;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
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