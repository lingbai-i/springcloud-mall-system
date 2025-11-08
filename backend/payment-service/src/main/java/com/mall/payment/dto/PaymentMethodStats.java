package com.mall.payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * 支付方式统计数据DTO
 * 用于封装各支付方式的统计信息
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodStats {

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 支付方式名称
     */
    private String paymentMethodName;

    /**
     * 支付订单数
     */
    private Long paymentOrderCount;

    /**
     * 支付成功数
     */
    private Long paymentSuccessCount;

    /**
     * 支付金额
     */
    private BigDecimal paymentAmount;

    /**
     * 支付成功金额
     */
    private BigDecimal paymentSuccessAmount;

    /**
     * 支付成功率
     */
    private BigDecimal paymentSuccessRate;

    /**
     * 平均支付金额
     */
    private BigDecimal averagePaymentAmount;

    /**
     * 占比（按金额）
     */
    private BigDecimal amountRatio;

    /**
     * 占比（按订单数）
     */
    private BigDecimal orderRatio;

    /**
     * 构造函数 - 包含基本统计信息
     * 
     * @param paymentMethod 支付方式
     * @param paymentMethodName 支付方式名称
     * @param paymentOrderCount 支付订单数
     * @param paymentSuccessCount 支付成功数
     * @param paymentAmount 支付金额
     * @param paymentSuccessAmount 支付成功金额
     */
    public PaymentMethodStats(String paymentMethod, String paymentMethodName, Long paymentOrderCount, 
                             Long paymentSuccessCount, BigDecimal paymentAmount, BigDecimal paymentSuccessAmount) {
        this.paymentMethod = paymentMethod;
        this.paymentMethodName = paymentMethodName;
        this.paymentOrderCount = paymentOrderCount;
        this.paymentSuccessCount = paymentSuccessCount;
        this.paymentAmount = paymentAmount;
        this.paymentSuccessAmount = paymentSuccessAmount;
        
        // 计算成功率
        if (paymentOrderCount != null && paymentOrderCount > 0) {
            this.paymentSuccessRate = BigDecimal.valueOf(paymentSuccessCount)
                    .divide(BigDecimal.valueOf(paymentOrderCount), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            this.paymentSuccessRate = BigDecimal.ZERO;
        }
        
        // 计算平均支付金额
        if (paymentOrderCount != null && paymentOrderCount > 0 && paymentAmount != null) {
            this.averagePaymentAmount = paymentAmount.divide(BigDecimal.valueOf(paymentOrderCount), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            this.averagePaymentAmount = BigDecimal.ZERO;
        }
    }

    /**
     * 获取支付方式显示名称
     * 
     * @return 支付方式显示名称
     */
    public String getDisplayName() {
        if (paymentMethodName != null && !paymentMethodName.isEmpty()) {
            return paymentMethodName;
        }
        
        switch (paymentMethod) {
            case "ALIPAY":
                return "支付宝";
            case "WECHAT":
                return "微信支付";
            case "BANK_CARD":
                return "银行卡";
            case "BALANCE":
                return "余额支付";
            default:
                return paymentMethod;
        }
    }
}