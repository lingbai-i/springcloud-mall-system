package com.mall.payment.dto.response;

import com.mall.payment.enums.PaymentMethod;
import com.mall.payment.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录响应DTO
 * 用于返回支付记录的详细信息给前端或其他服务
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@Data
public class PaymentRecordResponse {

    /**
     * 支付记录ID
     */
    private String id;

    /**
     * 支付订单ID
     */
    private String paymentOrderId;

    /**
     * 支付方式
     */
    private PaymentMethod paymentMethod;

    /**
     * 支付方式描述
     */
    private String paymentMethodDesc;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付状态
     */
    private PaymentStatus status;

    /**
     * 支付状态描述
     */
    private String statusDesc;

    /**
     * 第三方交易号
     */
    private String thirdPartyTradeNo;

    /**
     * 支付渠道
     */
    private String paymentChannel;

    /**
     * 支付完成时间
     */
    private LocalDateTime payTime;

    /**
     * 手续费
     */
    private BigDecimal feeAmount;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 设备信息
     */
    private String deviceInfo;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 支付耗时（毫秒）
     */
    private Long paymentDuration;

    /**
     * 获取格式化的支付金额
     * 
     * @return 格式化后的支付金额字符串
     */
    public String getFormattedAmount() {
        return amount != null ? "¥" + amount.setScale(2, BigDecimal.ROUND_HALF_UP).toString() : "¥0.00";
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
     * 获取格式化的支付耗时
     * 
     * @return 格式化后的支付耗时字符串
     */
    public String getFormattedPaymentDuration() {
        if (paymentDuration == null) {
            return "未完成";
        }
        
        if (paymentDuration < 1000) {
            return paymentDuration + "毫秒";
        } else if (paymentDuration < 60000) {
            return (paymentDuration / 1000) + "秒";
        } else {
            return (paymentDuration / 60000) + "分钟";
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
     * 判断是否有错误信息
     * 
     * @return 如果有错误信息返回true，否则返回false
     */
    public boolean hasError() {
        return errorCode != null && !errorCode.trim().isEmpty();
    }

    /**
     * 获取完整的错误信息
     * 
     * @return 包含错误代码和错误信息的完整描述
     */
    public String getFullErrorMessage() {
        if (!hasError()) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        if (errorCode != null) {
            sb.append("[").append(errorCode).append("]");
        }
        if (errorMessage != null) {
            sb.append(" ").append(errorMessage);
        }
        
        return sb.toString().trim();
    }

    /**
     * 判断是否可以重试
     * 
     * @param maxRetryCount 最大重试次数
     * @return 如果可以重试返回true，否则返回false
     */
    public boolean canRetry(int maxRetryCount) {
        return isFailed() && (retryCount == null || retryCount < maxRetryCount);
    }

    /**
     * 获取支付渠道显示名称
     * 
     * @return 支付渠道的显示名称
     */
    public String getPaymentChannelDisplayName() {
        if (paymentChannel == null) {
            return paymentMethod != null ? paymentMethod.getDisplayName() : "未知";
        }
        
        // 根据支付渠道返回友好的显示名称
        switch (paymentChannel.toLowerCase()) {
            case "alipay":
                return "支付宝";
            case "wechat":
                return "微信支付";
            case "unionpay":
                return "银联支付";
            case "balance":
                return "余额支付";
            default:
                return paymentChannel;
        }
    }

    /**
     * 获取设备类型
     * 
     * @return 设备类型（PC、Mobile、Unknown）
     */
    public String getDeviceType() {
        if (userAgent == null) {
            return "Unknown";
        }
        
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "Mobile";
        } else if (ua.contains("windows") || ua.contains("mac") || ua.contains("linux")) {
            return "PC";
        } else {
            return "Unknown";
        }
    }
}