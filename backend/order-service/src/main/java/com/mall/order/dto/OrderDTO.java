package com.mall.order.dto;

import com.mall.order.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单数据传输对象
 * 用于订单信息的传输和展示
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Data
public class OrderDTO {
    
    /**
     * 订单ID
     */
    private Long id;
    
    /**
     * 订单号
     */
    private String orderNo;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 订单状态
     */
    private OrderStatus status;
    
    /**
     * 订单状态描述
     */
    private String statusDesc;
    
    /**
     * 商品总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 运费
     */
    private BigDecimal shippingFee;
    
    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;
    
    /**
     * 应付金额
     */
    private BigDecimal payableAmount;
    
    /**
     * 收货人姓名
     */
    private String receiverName;
    
    /**
     * 收货人电话
     */
    private String receiverPhone;
    
    /**
     * 收货地址
     */
    private String receiverAddress;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 支付ID
     */
    private String paymentId;
    
    /**
     * 物流公司
     */
    private String logisticsCompany;
    
    /**
     * 物流单号
     */
    private String trackingNo;
    
    /**
     * 取消原因
     */
    private String cancelReason;
    
    /**
     * 退款原因
     */
    private String refundReason;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 支付时间
     */
    private LocalDateTime payTime;
    
    /**
     * 发货时间
     */
    private LocalDateTime shipTime;
    
    /**
     * 确认收货时间
     */
    private LocalDateTime confirmTime;
    
    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;
    
    /**
     * 退款时间
     */
    private LocalDateTime refundTime;
    
    /**
     * 订单项列表
     */
    private List<OrderItemDTO> orderItems;
}