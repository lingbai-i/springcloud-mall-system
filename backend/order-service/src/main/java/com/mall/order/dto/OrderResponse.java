package com.mall.order.dto;

import com.mall.order.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单响应DTO
 * 用于返回订单详细信息给前端
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 * 
 * 修改日志：
 * V1.0 2025-01-21：初始版本，包含订单基本信息和订单项列表
 */
@Data
public class OrderResponse {
    
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
    private String statusDescription;
    
    /**
     * 商品总金额
     */
    private BigDecimal productAmount;
    
    /**
     * 运费
     */
    private BigDecimal freightAmount;
    
    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;
    
    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 实付金额
     */
    private BigDecimal payAmount;
    
    /**
     * 支付方式
     */
    private String payMethod;
    
    /**
     * 支付时间
     */
    private LocalDateTime payTime;
    
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
     * 取消原因
     */
    private String cancelReason;
    
    /**
     * 物流公司
     */
    private String logisticsCompany;
    
    /**
     * 物流单号
     */
    private String logisticsNo;
    
    /**
     * 物流跟踪号
     */
    private String trackingNo;
    
    /**
     * 退款时间
     */
    private LocalDateTime refundTime;
    
    /**
     * 退款原因
     */
    private String refundReason;
    
    /**
     * 订单备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 订单项列表
     */
    private List<OrderItemResponse> orderItems;
    
    /**
     * 是否可以取消
     */
    private Boolean canCancel;
    
    /**
     * 是否可以申请退款
     */
    private Boolean canRefund;
    
    /**
     * 是否可以确认收货
     */
    private Boolean canConfirm;
    
    /**
     * 订单项响应DTO
     */
    @Data
    public static class OrderItemResponse {
        
        /**
         * 订单项ID
         */
        private Long id;
        
        /**
         * 商品ID
         */
        private Long productId;
        
        /**
         * 商品名称
         */
        private String productName;
        
        /**
         * 商品图片URL
         */
        private String productImage;
        
        /**
         * 商品规格
         */
        private String productSpec;
        
        /**
         * 商品单价
         */
        private BigDecimal productPrice;
        
        /**
         * 购买数量
         */
        private Integer quantity;
        
        /**
         * 小计金额
         */
        private BigDecimal subtotal;
    }
}