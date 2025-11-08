package com.mall.merchant.domain.entity;

import com.mall.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商家订单实体类
 * 存储商家相关的订单信息
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "merchant_order")
public class MerchantOrder extends BaseEntity {

    /**
     * 订单号
     */
    @Column(name = "order_no", nullable = false, unique = true, length = 50)
    private String orderNo;

    /**
     * 商家ID
     */
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

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
     * 商品图片
     */
    @Column(name = "product_image", length = 200)
    private String productImage;

    /**
     * 商品规格
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
     * 订单总金额
     */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /**
     * 实付金额
     */
    @Column(name = "paid_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal paidAmount;

    /**
     * 优惠金额
     */
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    /**
     * 运费
     */
    @Column(name = "shipping_fee", precision = 10, scale = 2)
    private BigDecimal shippingFee;

    /**
     * 订单状态：1-待付款，2-待发货，3-待收货，4-待评价，5-已完成，6-已取消，7-退款中，8-已退款
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 支付方式：1-微信支付，2-支付宝，3-银行卡
     */
    @Column(name = "payment_method")
    private Integer paymentMethod;

    /**
     * 支付时间
     */
    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    /**
     * 发货时间
     */
    @Column(name = "ship_time")
    private LocalDateTime shipTime;

    /**
     * 收货时间
     */
    @Column(name = "receive_time")
    private LocalDateTime receiveTime;

    /**
     * 完成时间
     */
    @Column(name = "finish_time")
    private LocalDateTime finishTime;

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
     * 收货人姓名
     */
    @Column(name = "receiver_name", nullable = false, length = 50)
    private String receiverName;

    /**
     * 收货人手机号
     */
    @Column(name = "receiver_phone", nullable = false, length = 20)
    private String receiverPhone;

    /**
     * 收货地址
     */
    @Column(name = "receiver_address", nullable = false, length = 200)
    private String receiverAddress;

    /**
     * 省份
     */
    @Column(name = "receiver_province", length = 50)
    private String receiverProvince;

    /**
     * 城市
     */
    @Column(name = "receiver_city", length = 50)
    private String receiverCity;

    /**
     * 区县
     */
    @Column(name = "receiver_district", length = 50)
    private String receiverDistrict;

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
     * 用户备注
     */
    @Column(name = "user_remark", length = 200)
    private String userRemark;

    /**
     * 商家备注
     */
    @Column(name = "merchant_remark", length = 200)
    private String merchantRemark;

    /**
     * 评价状态：0-未评价，1-已评价
     */
    @Column(name = "review_status", nullable = false)
    private Integer reviewStatus = 0;

    /**
     * 评价时间
     */
    @Column(name = "review_time")
    private LocalDateTime reviewTime;

    /**
     * 退款状态：0-无退款，1-退款中，2-退款成功，3-退款失败
     */
    @Column(name = "refund_status", nullable = false)
    private Integer refundStatus = 0;

    /**
     * 退款金额
     */
    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    @Column(name = "refund_reason", length = 200)
    private String refundReason;

    /**
     * 退款时间
     */
    @Column(name = "refund_time")
    private LocalDateTime refundTime;

    // 业务方法

    /**
     * 获取商家ID
     * 
     * @return 商家ID
     */
    public Long getMerchantId() {
        return merchantId;
    }

    /**
     * 获取订单状态
     * 
     * @return 订单状态
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 获取订单状态文本描述
     * 
     * @return 状态文本
     */
    public String getStatusText() {
        switch (status) {
            case 1:
                return "待付款";
            case 2:
                return "待发货";
            case 3:
                return "待收货";
            case 4:
                return "待评价";
            case 5:
                return "已完成";
            case 6:
                return "已取消";
            case 7:
                return "退款中";
            case 8:
                return "已退款";
            default:
                return "未知";
        }
    }

    /**
     * 获取支付方式文本
     * 
     * @return 支付方式文本
     */
    public String getPaymentMethodText() {
        if (paymentMethod == null) {
            return "未支付";
        }
        switch (paymentMethod) {
            case 1:
                return "微信支付";
            case 2:
                return "支付宝";
            case 3:
                return "银行卡";
            default:
                return "其他";
        }
    }

    /**
     * 判断是否可以发货
     * 
     * @return 是否可以发货
     */
    public boolean canShip() {
        return status != null && status == 2;
    }

    /**
     * 判断是否可以取消
     * 
     * @return 是否可以取消
     */
    public boolean canCancel() {
        return status != null && (status == 1 || status == 2);
    }

    /**
     * 判断是否可以退款
     * 
     * @return 是否可以退款
     */
    public boolean canRefund() {
        return status != null && (status == 2 || status == 3 || status == 4 || status == 5)
                && refundStatus != null && refundStatus == 0;
    }

    /**
     * 判断是否已支付
     * 
     * @return 是否已支付
     */
    public boolean isPaid() {
        return status != null && status > 1;
    }

    /**
     * 判断是否已完成
     * 
     * @return 是否已完成
     */
    public boolean isCompleted() {
        return status != null && status == 5;
    }

    /**
     * 判断是否已取消
     * 
     * @return 是否已取消
     */
    public boolean isCancelled() {
        return status != null && status == 6;
    }

    /**
     * 设置退款时间
     * 
     * @param refundTime 退款时间
     */
    public void setRefundTime(LocalDateTime refundTime) {
        this.refundTime = refundTime;
    }

    /**
     * 获取总金额
     * 
     * @return 总金额
     */
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    /**
     * 获取已支付金额
     * 
     * @return 已支付金额
     */
    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    /**
     * 获取支付方式
     * 
     * @return 支付方式
     */
    public Integer getPaymentMethod() {
        return paymentMethod;
    }

    /**
     * 获取发货时间
     * 
     * @return 发货时间
     */
    public LocalDateTime getShipTime() {
        return shipTime;
    }

    /**
     * 获取完成时间
     * 
     * @return 完成时间
     */
    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    /**
     * 获取物流公司
     * 
     * @return 物流公司
     */
    public String getLogisticsCompany() {
        return logisticsCompany;
    }

    /**
     * 获取物流单号
     * 
     * @return 物流单号
     */
    public String getLogisticsNo() {
        return logisticsNo;
    }

    /**
     * 设置退款原因
     * 
     * @param refundReason 退款原因
     */
    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }

    /**
     * 设置商家备注
     * 
     * @param merchantRemark 商家备注
     */
    public void setMerchantRemark(String merchantRemark) {
        this.merchantRemark = merchantRemark;
    }

    /**
     * 设置物流公司
     * 
     * @param logisticsCompany 物流公司
     */
    public void setLogisticsCompany(String logisticsCompany) {
        this.logisticsCompany = logisticsCompany;
    }

    /**
     * 设置物流单号
     * 
     * @param logisticsNo 物流单号
     */
    public void setLogisticsNo(String logisticsNo) {
        this.logisticsNo = logisticsNo;
    }

    /**
     * 设置退款状态
     * 
     * @param refundStatus 退款状态
     */
    public void setRefundStatus(Integer refundStatus) {
        this.refundStatus = refundStatus;
    }

    /**
     * 设置退款金额
     * 
     * @param refundAmount 退款金额
     */
    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    /**
     * 设置订单状态
     * 
     * @param status 订单状态
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 设置收货时间
     * 
     * @param receiveTime 收货时间
     */
    public void setReceiveTime(LocalDateTime receiveTime) {
        this.receiveTime = receiveTime;
    }

    /**
     * 设置取消时间
     * 
     * @param cancelTime 取消时间
     */
    public void setCancelTime(LocalDateTime cancelTime) {
        this.cancelTime = cancelTime;
    }

    /**
     * 设置发货时间
     * 
     * @param shipTime 发货时间
     */
    public void setShipTime(LocalDateTime shipTime) {
        this.shipTime = shipTime;
    }

    /**
     * 获取订单号
     * 设计说明：显式提供 getter 以在缺失 Lombok 处理器或编译配置异常时仍可用。
     * 文档生成时间：2025-11-05T20:11:47+08:00
     *
     * @author lingbai
     * @return 订单号
     */
    public String getOrderNo() {
        return orderNo;
    }
}
