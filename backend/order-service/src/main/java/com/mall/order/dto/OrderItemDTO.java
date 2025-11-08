package com.mall.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单项数据传输对象
 * 用于订单项信息的传输和展示
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Data
public class OrderItemDTO {
    
    /**
     * 订单项ID
     */
    private Long id;
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 商品名称
     */
    private String productName;
    
    /**
     * 商品图片
     */
    private String productImage;
    
    /**
     * 商品规格
     */
    private String productSpec;
    
    /**
     * 商品单价
     */
    private BigDecimal unitPrice;
    
    /**
     * 购买数量
     */
    private Integer quantity;
    
    /**
     * 小计金额
     */
    private BigDecimal subtotal;
}