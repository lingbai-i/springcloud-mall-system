package com.mall.cart.domain.entity;

import com.mall.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 购物车项实体类
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CartItem extends BaseEntity {
    
    /** 用户ID */
    private Long userId;
    
    /** 商品ID */
    private Long productId;
    
    /** 商品名称 */
    private String productName;
    
    /** 商品图片 */
    private String productImage;
    
    /** 商品价格 */
    private BigDecimal price;
    
    /** 购买数量 */
    private Integer quantity;
    
    /** 是否选中 */
    private Boolean selected;
    
    /** 商品规格 */
    private String specifications;
    
    // Getter methods for Lambda expressions
    public Long getUserId() { return userId; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getProductImage() { return productImage; }
    public BigDecimal getPrice() { return price; }
    public Integer getQuantity() { return quantity; }
    public Boolean getSelected() { return selected; }
    public String getSpecifications() { return specifications; }
    
    // Setter methods
    public void setUserId(Long userId) { this.userId = userId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setProductImage(String productImage) { this.productImage = productImage; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setSelected(Boolean selected) { this.selected = selected; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
}