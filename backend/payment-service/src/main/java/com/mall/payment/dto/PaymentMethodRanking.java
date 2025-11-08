package com.mall.payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * 支付方式排名DTO
 * 用于封装支付方式排名信息
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodRanking {

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 支付方式名称
     */
    private String paymentMethodName;

    /**
     * 统计值（金额或订单数）
     */
    private BigDecimal value;

    /**
     * 单位（元或笔）
     */
    private String unit;

    /**
     * 排名
     */
    private Integer ranking;

    /**
     * 占比
     */
    private BigDecimal ratio;

    /**
     * 构造函数 - 包含基本排名信息
     * 
     * @param paymentMethod 支付方式
     * @param value 统计值
     * @param unit 单位
     * @param ranking 排名
     */
    public PaymentMethodRanking(String paymentMethod, BigDecimal value, String unit, Integer ranking) {
        this.paymentMethod = paymentMethod;
        this.value = value;
        this.unit = unit;
        this.ranking = ranking;
        this.paymentMethodName = getDisplayName(paymentMethod);
    }

    /**
     * 获取支付方式显示名称
     * 
     * @param paymentMethod 支付方式代码
     * @return 支付方式显示名称
     */
    private String getDisplayName(String paymentMethod) {
        if (paymentMethod == null) {
            return "";
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

    /**
     * 获取格式化的统计值
     * 
     * @return 格式化的统计值字符串
     */
    public String getFormattedValue() {
        if (value == null) {
            return "0" + unit;
        }
        
        if ("元".equals(unit)) {
            return String.format("%.2f%s", value, unit);
        } else {
            return String.format("%.0f%s", value, unit);
        }
    }

    /**
     * 获取格式化的占比
     * 
     * @return 格式化的占比字符串
     */
    public String getFormattedRatio() {
        if (ratio == null) {
            return "0.00%";
        }
        return String.format("%.2f%%", ratio);
    }
}