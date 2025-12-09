package com.mall.order.entity;

import com.mall.order.enums.OrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体类
 * 表示电商系统中的订单信息，包含订单基本信息、状态、金额等
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "orders")
public class Order {
    
    /**
     * 订单ID - 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 订单号 - 唯一标识
     */
    @Column(name = "order_no", unique = true, nullable = false, length = 32)
    private String orderNo;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 商家ID
     */
    @Column(name = "merchant_id")
    private Long merchantId;
    
    /**
     * 订单状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;
    
    /**
     * 商品总金额（不含运费）
     */
    @Column(name = "product_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal productAmount;
    
    /**
     * 运费
     */
    @Column(name = "freight_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal freightAmount;
    
    /**
     * 优惠金额
     */
    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;
    
    /**
     * 订单总金额
     */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    /**
     * 实付金额
     */
    @Column(name = "pay_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal payAmount;
    
    /**
     * 支付方式
     */
    @Column(name = "pay_method", length = 20)
    private String payMethod;
    
    /**
     * 支付时间
     */
    @Column(name = "pay_time")
    private LocalDateTime payTime;
    
    /**
     * 收货人姓名
     */
    @Column(name = "receiver_name", nullable = false, length = 50)
    private String receiverName;
    
    /**
     * 收货人电话
     */
    @Column(name = "receiver_phone", nullable = false, length = 20)
    private String receiverPhone;
    
    /**
     * 收货地址
     */
    @Column(name = "receiver_address", nullable = false, length = 200)
    private String receiverAddress;
    
    /**
     * 发货时间
     */
    @Column(name = "ship_time")
    private LocalDateTime shipTime;
    
    /**
     * 确认收货时间
     */
    @Column(name = "confirm_time")
    private LocalDateTime confirmTime;
    
    /**
     * 取消时间
     */
    @Column(name = "cancel_time")
    private LocalDateTime cancelTime;
    
    /**
     * 取消原因
     */
    @Column(name = "cancel_reason", length = 200)
    private String cancelReason;
    
    /**
     * 物流公司
     */
    @Column(name = "logistics_company", length = 50)
    private String logisticsCompany;
    
    /**
     * 物流单号
     */
    @Column(name = "logistics_no", length = 50)
    private String logisticsNo;
    
    /**
     * 物流跟踪号
     */
    @Column(name = "tracking_no", length = 50)
    private String trackingNo;
    
    /**
     * 支付ID
     */
    @Column(name = "payment_id", length = 50)
    private String paymentId;
    
    /**
     * 运费（与freightAmount相同，为了兼容性）
     */
    @Column(name = "shipping_fee", precision = 10, scale = 2)
    private BigDecimal shippingFee;
    
    /**
     * 退款时间
     */
    @Column(name = "refund_time")
    private LocalDateTime refundTime;
    
    /**
     * 退款原因
     */
    @Column(name = "refund_reason", length = 200)
    private String refundReason;
    
    /**
     * 退款申请时间
     */
    @Column(name = "refund_apply_time")
    private LocalDateTime refundApplyTime;
    
    /**
     * 订单备注
     */
    @Column(name = "remark", length = 500)
    private String remark;
    
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
     * 订单项列表
     * 使用EAGER加载避免懒加载异常
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> orderItems;
    
    /**
     * 实体创建前的回调方法
     * 设置创建时间和更新时间
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createTime = now;
        this.updateTime = now;
        
        // 如果状态为空，设置默认状态为待付款
        if (this.status == null) {
            this.status = OrderStatus.PENDING;
        }
        
        // 如果运费为空，设置默认值为0
        if (this.freightAmount == null) {
            this.freightAmount = BigDecimal.ZERO;
        }
        
        // 如果优惠金额为空，设置默认值为0
        if (this.discountAmount == null) {
            this.discountAmount = BigDecimal.ZERO;
        }
    }
    
    /**
     * 实体更新前的回调方法
     * 更新修改时间
     */
    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 计算订单总金额
     * 总金额 = 商品金额 + 运费 - 优惠金额
     */
    public void calculateTotalAmount() {
        this.totalAmount = this.productAmount
                .add(this.freightAmount)
                .subtract(this.discountAmount);
        this.payAmount = this.totalAmount;
    }
    
    /**
     * 检查订单是否可以取消
     * 
     * @return 是否可以取消
     */
    public boolean canCancel() {
        return this.status != null && this.status.canCancel();
    }
    
    /**
     * 检查订单是否可以申请退款
     * 
     * @return 是否可以申请退款
     */
    public boolean canRefund() {
        return this.status != null && this.status.canRefund();
    }
    
    /**
     * 检查订单是否可以确认收货
     * 
     * @return 是否可以确认收货
     */
    public boolean canConfirm() {
        return this.status != null && this.status.canConfirm();
    }
    
    /**
     * 获取应付金额（与payAmount相同，为了兼容性）
     * 
     * @return 应付金额
     */
    public BigDecimal getPayableAmount() {
        return this.payAmount;
    }
    
    /**
     * 设置应付金额（与payAmount相同，为了兼容性）
     * 
     * @param payableAmount 应付金额
     */
    public void setPayableAmount(BigDecimal payableAmount) {
        this.payAmount = payableAmount;
    }
}