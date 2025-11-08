package com.mall.payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 月统计数据DTO
 * 用于封装每月支付统计信息
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyStats {

    /**
     * 统计月份（月初日期）
     */
    private LocalDate month;

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
     * 退款订单数
     */
    private Long refundOrderCount;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 支付成功率
     */
    private BigDecimal paymentSuccessRate;

    /**
     * 平均支付金额
     */
    private BigDecimal averagePaymentAmount;

    /**
     * 环比增长率
     */
    private BigDecimal growthRate;

    /**
     * 构造函数 - 包含基本统计信息
     * 
     * @param month 统计月份
     * @param paymentOrderCount 支付订单数
     * @param paymentSuccessCount 支付成功数
     * @param paymentAmount 支付金额
     * @param paymentSuccessAmount 支付成功金额
     */
    public MonthlyStats(LocalDate month, Long paymentOrderCount, Long paymentSuccessCount, 
                       BigDecimal paymentAmount, BigDecimal paymentSuccessAmount) {
        this.month = month;
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
}