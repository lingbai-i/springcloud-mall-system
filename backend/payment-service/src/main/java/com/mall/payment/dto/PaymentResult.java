package com.mall.payment.dto;

import com.mall.payment.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付结果DTO
 * 用于封装支付处理的结果信息
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@Data
public class PaymentResult {

    /**
     * 支付是否成功
     */
    private boolean success;

    /**
     * 支付状态
     */
    private PaymentStatus status;

    /**
     * 第三方订单号
     */
    private String thirdPartyOrderNo;

    /**
     * 支付URL或二维码内容
     */
    private String paymentUrl;

    /**
     * 实际支付金额
     */
    private BigDecimal actualAmount;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 原始响应数据
     */
    private String rawData;

    /**
     * 构造函数
     */
    public PaymentResult() {}

    /**
     * 成功结果构造函数
     * 
     * @param status 支付状态
     * @param thirdPartyOrderNo 第三方订单号
     * @param paymentUrl 支付URL
     * @param actualAmount 实际支付金额
     */
    public PaymentResult(PaymentStatus status, String thirdPartyOrderNo, String paymentUrl, BigDecimal actualAmount) {
        this.success = true;
        this.status = status;
        this.thirdPartyOrderNo = thirdPartyOrderNo;
        this.paymentUrl = paymentUrl;
        this.actualAmount = actualAmount;
    }

    /**
     * 失败结果构造函数
     * 
     * @param errorCode 错误代码
     * @param errorMessage 错误信息
     */
    public PaymentResult(String errorCode, String errorMessage) {
        this.success = false;
        this.status = PaymentStatus.FAILED;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * 创建成功结果
     * 
     * @param status 支付状态
     * @param thirdPartyOrderNo 第三方订单号
     * @param paymentUrl 支付URL
     * @param actualAmount 实际支付金额
     * @return 支付结果
     */
    public static PaymentResult success(PaymentStatus status, String thirdPartyOrderNo, String paymentUrl, BigDecimal actualAmount) {
        return new PaymentResult(status, thirdPartyOrderNo, paymentUrl, actualAmount);
    }

    /**
     * 创建失败结果
     * 
     * @param errorCode 错误代码
     * @param errorMessage 错误信息
     * @return 支付结果
     */
    public static PaymentResult failure(String errorCode, String errorMessage) {
        return new PaymentResult(errorCode, errorMessage);
    }

    /**
     * 检查是否为成功状态
     * 
     * @return 是否成功
     */
    public boolean isSuccess() {
        return success && status != null && status != PaymentStatus.FAILED;
    }

    /**
     * 获取失败原因（兼容方法）
     * @return 错误信息
     */
    public String getFailureReason() {
        return errorMessage;
    }

    /**
     * 获取第三方订单号
     * @return 第三方订单号
     */
    public String getThirdPartyOrderNo() {
        return thirdPartyOrderNo;
    }

    @Override
    public String toString() {
        return "PaymentResult{" +
                "success=" + success +
                ", status=" + status +
                ", thirdPartyOrderNo='" + thirdPartyOrderNo + '\'' +
                ", paymentUrl='" + paymentUrl + '\'' +
                ", actualAmount=" + actualAmount +
                ", payTime=" + payTime +
                ", errorMessage='" + errorMessage + '\'' +
                ", errorCode='" + errorCode + '\'' +
                '}';
    }
}