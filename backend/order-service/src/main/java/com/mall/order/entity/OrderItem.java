package com.mall.order.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单项实体类
 * 表示订单中的具体商品项，包含商品信息、数量、价格等
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "order_items")
public class OrderItem {
    
    /**
     * 订单项ID - 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 订单ID - 外键
     */
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    /**
     * 商品ID
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    /**
     * 商品名称
     */
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;
    
    /**
     * 商品图片URL
     */
    @Column(name = "product_image", length = 500)
    private String productImage;
    
    /**
     * 商品规格（如颜色、尺寸等）
     */
    @Column(name = "product_spec", length = 200)
    private String productSpec;
    
    /**
     * 商品单价
     */
    @Column(name = "product_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal productPrice;
    
    /**
     * 购买数量
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    /**
     * 小计金额（单价 × 数量）
     */
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
    
    /**
     * 关联的订单实体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;
    
    /**
     * 实体创建前的回调方法
     * 设置创建时间和更新时间
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createTime = now;
        this.updateTime = now;
        
        // 计算小计金额
        calculateSubtotal();
    }
    
    /**
     * 实体更新前的回调方法
     * 更新修改时间
     */
    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
        
        // 重新计算小计金额
        calculateSubtotal();
    }
    
    /**
     * 计算小计金额
     * 小计 = 单价 × 数量
     */
    public void calculateSubtotal() {
        if (this.productPrice != null && this.quantity != null) {
            this.subtotal = this.productPrice.multiply(BigDecimal.valueOf(this.quantity));
        }
    }
}