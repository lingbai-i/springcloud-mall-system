package com.mall.payment.dto.response;

import com.mall.payment.enums.PaymentMethod;
import com.mall.payment.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付订单响应DTO
 * 用于返回支付订单的详细信息给前端或其他服务
 * 
 * <p>响应数据说明：</p>
 * <ul>
 *   <li>基本信息：订单ID、业务订单ID、用户ID、支付金额等</li>
 *   <li>支付信息：支付方式、支付状态、第三方订单号、支付完成时间等</li>
 *   <li>金额信息：支付金额、实际支付金额、手续费等</li>
 *   <li>时间信息：创建时间、过期时间、支付完成时间等</li>
 *   <li>回调信息：返回URL、通知URL等</li>
 *   <li>扩展信息：失败原因、重试次数、客户端信息等</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>支付订单创建后返回给前端</li>
 *   <li>支付订单查询接口的响应</li>
 *   <li>支付状态变更通知</li>
 *   <li>支付订单列表展示</li>
 * </ul>
 * 
 * <p>注意事项：</p>
 * <ul>
 *   <li>敏感信息（如第三方密钥）不会包含在响应中</li>
 *   <li>金额字段使用BigDecimal确保精度</li>
 *   <li>时间字段使用LocalDateTime格式</li>
 *   <li>枚举字段同时提供枚举值和描述文本</li>
 * </ul>
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * 修改日志：
 * V1.2 2025-11-01：完善Javadoc注释，增加响应数据说明和使用场景
 * V1.1 2024-12-15：增加客户端信息和设备信息字段
 * V1.0 2024-12-01：初始版本，定义基本响应字段
 */
@Data
public class PaymentOrderResponse {

    /**
     * 支付订单ID
     */
    private String id;

    /**
     * 业务订单ID
     */
    private String orderId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付方式
     */
    private PaymentMethod paymentMethod;

    /**
     * 支付方式描述
     */
    private String paymentMethodDesc;

    /**
     * 支付状态
     */
    private PaymentStatus status;

    /**
     * 支付状态描述
     */
    private String statusDesc;

    /**
     * 支付描述
     */
    private String description;

    /**
     * 支付成功后的返回URL
     */
    private String returnUrl;

    /**
     * 异步通知URL
     */
    private String notifyUrl;

    /**
     * 支付过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 第三方支付订单号
     */
    private String thirdPartyOrderNo;

    /**
     * 支付完成时间
     */
    private LocalDateTime payTime;

    /**
     * 实际支付金额
     */
    private BigDecimal actualAmount;

    /**
     * 手续费
     */
    private BigDecimal feeAmount;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 支付记录列表
     */
    private List<PaymentRecordResponse> paymentRecords;

    /**
     * 退款订单列表
     */
    private List<RefundOrderResponse> refundOrders;

    /**
     * 是否已过期
     */
    private Boolean expired;

    /**
     * 是否可以支付
     */
    private Boolean canPay;

    /**
     * 是否可以取消
     */
    private Boolean canCancel;

    /**
     * 是否可以退款
     */
    private Boolean canRefund;

    /**
     * 可退款金额
     */
    private BigDecimal refundableAmount;

    /**
     * 支付二维码URL（扫码支付时使用）
     */
    private String qrCodeUrl;

    /**
     * 支付跳转URL（网页支付时使用）
     */
    private String payUrl;

    /**
     * 扩展信息
     */
    private String extInfo;

    /**
     * 获取格式化的支付金额
     * 
     * @return 格式化后的支付金额字符串
     */
    public String getFormattedAmount() {
        return amount != null ? "¥" + amount.setScale(2, BigDecimal.ROUND_HALF_UP).toString() : "¥0.00";
    }

    /**
     * 获取格式化的实际支付金额
     * 
     * @return 格式化后的实际支付金额字符串
     */
    public String getFormattedActualAmount() {
        return actualAmount != null ? "¥" + actualAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toString() : "¥0.00";
    }

    /**
     * 获取格式化的手续费
     * 
     * @return 格式化后的手续费字符串
     */
    public String getFormattedFeeAmount() {
        return feeAmount != null ? "¥" + feeAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toString() : "¥0.00";
    }

    /**
     * 获取格式化的可退款金额
     * 
     * @return 格式化后的可退款金额字符串
     */
    public String getFormattedRefundableAmount() {
        return refundableAmount != null ? "¥" + refundableAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toString() : "¥0.00";
    }

    /**
     * 获取支付耗时（毫秒）
     * 
     * @return 支付耗时，如果未完成支付返回null
     */
    public Long getPaymentDuration() {
        if (payTime == null || createdAt == null) {
            return null;
        }
        return java.time.Duration.between(createdAt, payTime).toMillis();
    }

    /**
     * 获取格式化的支付耗时
     * 
     * @return 格式化后的支付耗时字符串
     */
    public String getFormattedPaymentDuration() {
        Long duration = getPaymentDuration();
        if (duration == null) {
            return "未完成";
        }
        
        if (duration < 1000) {
            return duration + "毫秒";
        } else if (duration < 60000) {
            return (duration / 1000) + "秒";
        } else {
            return (duration / 60000) + "分钟";
        }
    }

    /**
     * 判断是否为成功状态
     * 
     * @return 如果支付成功返回true，否则返回false
     */
    public boolean isSuccess() {
        return status == PaymentStatus.SUCCESS;
    }

    /**
     * 判断是否为失败状态
     * 
     * @return 如果支付失败返回true，否则返回false
     */
    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }

    /**
     * 判断是否为处理中状态
     * 
     * @return 如果正在处理返回true，否则返回false
     */
    public boolean isProcessing() {
        return status == PaymentStatus.PROCESSING;
    }

    /**
     * 判断是否为待支付状态
     * 
     * @return 如果待支付返回true，否则返回false
     */
    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }

    // Getter methods for fields that Lombok is not generating
    public String getId() {
        return id;
    }

    public String getPaymentOrderId() {
        return id; // 支付订单ID就是id字段
    }

    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    /**
     * 获取剩余支付时间（分钟）
     * 
     * @return 剩余支付时间，如果已过期返回0
     */
    public long getRemainingMinutes() {
        if (expireTime == null) {
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expireTime)) {
            return 0;
        }
        
        return java.time.Duration.between(now, expireTime).toMinutes();
    }

    /**
     * 获取格式化的剩余支付时间
     * 
     * @return 格式化后的剩余支付时间字符串
     */
    public String getFormattedRemainingTime() {
        long minutes = getRemainingMinutes();
        if (minutes <= 0) {
            return "已过期";
        }
        
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        
        if (hours > 0) {
            return String.format("%d小时%d分钟", hours, remainingMinutes);
        } else {
            return String.format("%d分钟", remainingMinutes);
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setPaymentMethodDesc(String paymentMethodDesc) {
        this.paymentMethodDesc = paymentMethodDesc;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public void setThirdPartyOrderNo(String thirdPartyOrderNo) {
        this.thirdPartyOrderNo = thirdPartyOrderNo;
    }

    public void setPayTime(LocalDateTime payTime) {
        this.payTime = payTime;
    }

    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setPaymentRecords(List<PaymentRecordResponse> paymentRecords) {
        this.paymentRecords = paymentRecords;
    }

    public void setRefundOrders(List<RefundOrderResponse> refundOrders) {
        this.refundOrders = refundOrders;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public void setCanPay(Boolean canPay) {
        this.canPay = canPay;
    }

    public void setCanCancel(Boolean canCancel) {
        this.canCancel = canCancel;
    }

    public void setCanRefund(Boolean canRefund) {
        this.canRefund = canRefund;
    }

    public void setRefundableAmount(BigDecimal refundableAmount) {
        this.refundableAmount = refundableAmount;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public void setPayUrl(String payUrl) {
        this.payUrl = payUrl;
    }

    public void setExtInfo(String extInfo) {
        this.extInfo = extInfo;
    }
}