package com.mall.payment.enums;

import lombok.Getter;

/**
 * 支付方式枚举
 * 定义系统支持的各种支付方式，包括第三方支付和银行卡支付
 * 
 * <p>支付方式分类：</p>
 * <ul>
 *   <li>第三方支付：ALIPAY（支付宝）、WECHAT（微信支付）、PAYPAL（PayPal）</li>
 *   <li>银行卡支付：BANK_CARD（储蓄卡）、CREDIT_CARD（信用卡）</li>
 *   <li>账户支付：BALANCE（余额支付）</li>
 *   <li>数字货币：CRYPTO（数字货币支付）</li>
 * </ul>
 * 
 * <p>功能特性：</p>
 * <ul>
 *   <li>扫码支付：支付宝、微信支付</li>
 *   <li>退款支持：除数字货币外均支持</li>
 *   <li>手续费：不同支付方式手续费率不同</li>
 *   <li>启用状态：部分支付方式可能暂未启用</li>
 * </ul>
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * 修改日志：
 * V1.2 2025-11-01：完善Javadoc注释，增加支付方式分类和功能特性说明
 * V1.1 2024-12-20：新增数字货币支付方式
 * V1.0 2024-12-01：初始版本，定义基本支付方式
 */
@Getter
public enum PaymentMethod {
    
    /**
     * 支付宝支付 - 使用支付宝进行在线支付
     */
    ALIPAY("ALIPAY", "支付宝", "alipay.png", true),
    
    /**
     * 微信支付 - 使用微信进行在线支付
     */
    WECHAT("WECHAT", "微信支付", "wechat.png", true),
    
    /**
     * 银行卡支付 - 使用银行卡进行在线支付
     */
    BANK_CARD("BANK_CARD", "银行卡", "bank_card.png", true),
    
    /**
     * 余额支付 - 使用账户余额进行支付
     */
    BALANCE("BALANCE", "余额支付", "balance.png", true),
    
    /**
     * 信用卡支付 - 使用信用卡进行支付
     */
    CREDIT_CARD("CREDIT_CARD", "信用卡", "credit_card.png", false),
    
    /**
     * PayPal支付 - 使用PayPal进行国际支付
     */
    PAYPAL("PAYPAL", "PayPal", "paypal.png", false),
    
    /**
     * 数字货币支付 - 使用数字货币进行支付
     */
    CRYPTO("CRYPTO", "数字货币", "crypto.png", false);

    /**
     * 支付方式代码
     */
    private final String code;
    
    /**
     * 支付方式名称
     */
    private final String name;
    
    /**
     * 支付方式图标
     */
    private final String icon;
    
    /**
     * 是否启用
     */
    private final boolean enabled;
    
    /**
     * 构造函数
     * 
     * @param code 支付方式代码
     * @param name 支付方式名称
     * @param icon 支付方式图标
     * @param enabled 是否启用
     */
    PaymentMethod(String code, String name, String icon, boolean enabled) {
        this.code = code;
        this.name = name;
        this.icon = icon;
        this.enabled = enabled;
    }


    /**
     * 获取支付方式代码
     * 
     * @return 支付方式代码
     */
    public String getCode() {
        return this.code;
    }

    /**
     * 获取支付方式名称
     * 
     * @return 支付方式名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 获取显示名称
     * 
     * @return 显示名称
     */
    public String getDisplayName() {
        return this.name;
    }

    /**
     * 检查是否启用
     * 
     * @return 是否启用
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * 根据代码获取支付方式
     * 
     * @param code 支付方式代码
     * @return 支付方式枚举，如果未找到则返回null
     */
    public static PaymentMethod fromCode(String code) {
        for (PaymentMethod method : values()) {
            if (method.getCode().equals(code)) {
                return method;
            }
        }
        return null;
    }

    /**
     * 获取所有启用的支付方式
     * 
     * @return 启用的支付方式数组
     */
    public static PaymentMethod[] getEnabledMethods() {
        return java.util.Arrays.stream(PaymentMethod.values())
                .filter(PaymentMethod::isEnabled)
                .toArray(PaymentMethod[]::new);
    }

    /**
     * 判断是否为第三方支付
     * 
     * @return 如果是第三方支付返回true，否则返回false
     */
    public boolean isThirdParty() {
        return this == ALIPAY || this == WECHAT || this == PAYPAL;
    }

    /**
     * 判断是否支持扫码支付
     * 
     * @return 如果支持扫码支付返回true，否则返回false
     */
    public boolean supportQrCode() {
        return this == ALIPAY || this == WECHAT;
    }

    /**
     * 判断是否支持退款
     * 
     * @return 如果支持退款返回true，否则返回false
     */
    public boolean supportRefund() {
        return this != CRYPTO; // 数字货币暂不支持退款
    }

    /**
     * 获取支付手续费率（百分比）
     * 
     * @return 手续费率
     */
    public double getFeeRate() {
        switch (this) {
            case ALIPAY:
            case WECHAT:
                return 0.6; // 0.6%
            case BANK_CARD:
            case CREDIT_CARD:
                return 0.8; // 0.8%
            case BALANCE:
                return 0.0; // 余额支付无手续费
            case PAYPAL:
                return 2.9; // 2.9%
            case CRYPTO:
                return 1.0; // 1.0%
            default:
                return 0.0;
        }
    }
}