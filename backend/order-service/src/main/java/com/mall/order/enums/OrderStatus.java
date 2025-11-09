package com.mall.order.enums;

/**
 * 订单状态枚举
 * 定义订单在整个生命周期中的各种状态
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
public enum OrderStatus {
    
    /**
     * 待付款 - 订单已创建，等待用户付款
     */
    PENDING("pending", "待付款"),
    
    /**
     * 待付款别名（兼容旧代码）
     */
    PENDING_PAYMENT("pending", "待付款"),
    
    /**
     * 已付款 - 用户已完成付款，等待商家发货
     */
    PAID("paid", "已付款"),
    
    /**
     * 已发货 - 商家已发货，商品在配送途中
     */
    SHIPPED("shipped", "已发货"),
    
    /**
     * 已完成 - 用户已确认收货，订单完成
     */
    COMPLETED("completed", "已完成"),
    
    /**
     * 已取消 - 订单被取消（用户取消或系统自动取消）
     */
    CANCELLED("cancelled", "已取消"),
    
    /**
     * 退款中 - 用户申请退款，正在处理中
     */
    REFUNDING("refunding", "退款中"),
    
    /**
     * 退款待处理 - 退款申请已提交，等待处理
     */
    REFUND_PENDING("refund_pending", "退款待处理"),
    
    /**
     * 已退款 - 退款已完成
     */
    REFUNDED("refunded", "已退款");
    
    /**
     * 状态代码
     */
    private final String code;
    
    /**
     * 状态描述
     */
    private final String description;
    
    /**
     * 构造函数
     * 
     * @param code 状态代码
     * @param description 状态描述
     */
    OrderStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 获取状态代码
     * 
     * @return 状态代码
     */
    public String getCode() {
        return code;
    }
    
    /**
     * 获取状态描述
     * 
     * @return 状态描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取订单状态
     * 
     * @param code 状态代码
     * @return 订单状态枚举，如果未找到则返回null
     */
    public static OrderStatus fromCode(String code) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * 检查是否可以取消订单
     * 只有待付款和已付款状态的订单可以取消
     * 
     * @return 是否可以取消
     */
    public boolean canCancel() {
        return this == PENDING || this == PAID;
    }
    
    /**
     * 检查是否可以申请退款
     * 已付款、已发货、已完成状态的订单可以申请退款
     * 
     * @return 是否可以申请退款
     */
    public boolean canRefund() {
        return this == PAID || this == SHIPPED || this == COMPLETED;
    }
    
    /**
     * 检查是否可以确认收货
     * 只有已发货状态的订单可以确认收货
     * 
     * @return 是否可以确认收货
     */
    public boolean canConfirm() {
        return this == SHIPPED;
    }
}