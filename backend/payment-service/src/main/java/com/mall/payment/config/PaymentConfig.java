package com.mall.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付配置类
 * 管理支付相关的配置参数，包括第三方支付平台配置、手续费配置等
 * 
 * <p>配置结构：</p>
 * <ul>
 *   <li>支付宝配置（AlipayConfig）：应用ID、密钥、网关地址等</li>
 *   <li>微信支付配置（WechatPayConfig）：应用ID、商户号、证书路径等</li>
 *   <li>银行卡支付配置（BankCardConfig）：商户号、支持银行列表等</li>
 *   <li>余额支付配置（BalanceConfig）：金额限制、密码要求等</li>
 *   <li>通用配置（GeneralConfig）：超时时间、重试次数、通知配置等</li>
 *   <li>手续费配置（FeeConfig）：各支付方式的手续费率</li>
 * </ul>
 * 
 * <p>配置来源：</p>
 * <ul>
 *   <li>配置文件：application.yml中的payment前缀配置</li>
 *   <li>环境变量：支持通过环境变量覆盖配置</li>
 *   <li>配置中心：支持从配置中心动态获取配置</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>
 * payment:
 *   alipay:
 *     app-id: 2021001234567890
 *     private-key: MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC...
 *     enabled: true
 *   general:
 *     default-timeout-minutes: 30
 *     max-retry-count: 3
 * </pre>
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * 修改日志：
 * V1.2 2025-11-01：完善Javadoc注释，增加配置结构和使用示例说明
 * V1.1 2024-12-15：增加手续费配置和通用配置
 * V1.0 2024-12-01：初始版本，定义基本支付平台配置
 */
@Configuration
@ConfigurationProperties(prefix = "payment")
public class PaymentConfig {

    /**
     * 支付宝配置
     */
    private AlipayConfig alipay = new AlipayConfig();

    /**
     * 微信支付配置
     */
    private WechatPayConfig wechatPay = new WechatPayConfig();

    /**
     * 银行卡支付配置
     */
    private BankCardConfig bankCard = new BankCardConfig();

    /**
     * 余额支付配置
     */
    private BalanceConfig balance = new BalanceConfig();

    /**
     * 通用配置
     */
    private GeneralConfig general = new GeneralConfig();

    /**
     * 手续费配置
     */
    private FeeConfig fee = new FeeConfig();

    // ==================== Getter和Setter方法 ====================

    public AlipayConfig getAlipay() {
        return alipay;
    }

    public void setAlipay(AlipayConfig alipay) {
        this.alipay = alipay;
    }

    public WechatPayConfig getWechatPay() {
        return wechatPay;
    }

    public void setWechatPay(WechatPayConfig wechatPay) {
        this.wechatPay = wechatPay;
    }

    public BankCardConfig getBankCard() {
        return bankCard;
    }

    public void setBankCard(BankCardConfig bankCard) {
        this.bankCard = bankCard;
    }

    public BalanceConfig getBalance() {
        return balance;
    }

    public void setBalance(BalanceConfig balance) {
        this.balance = balance;
    }

    public GeneralConfig getGeneral() {
        return general;
    }

    public void setGeneral(GeneralConfig general) {
        this.general = general;
    }

    public FeeConfig getFee() {
        return fee;
    }

    public void setFee(FeeConfig fee) {
        this.fee = fee;
    }

    // ==================== 内部配置类 ====================

    /**
     * 支付宝配置
     */
    public static class AlipayConfig {
        /**
         * 应用ID
         */
        private String appId;

        /**
         * 商户私钥
         */
        private String privateKey;

        /**
         * 支付宝公钥
         */
        private String alipayPublicKey;

        /**
         * 签名类型
         */
        private String signType = "RSA2";

        /**
         * 字符编码
         */
        private String charset = "UTF-8";

        /**
         * 数据格式
         */
        private String format = "json";

        /**
         * 网关地址
         */
        private String gatewayUrl = "https://openapi.alipay.com/gateway.do";

        /**
         * 是否启用
         */
        private boolean enabled = true;

        /**
         * 支付超时时间（分钟）
         */
        private int timeoutMinutes = 30;

        // Getter和Setter方法
        public String getAppId() { return appId; }
        public void setAppId(String appId) { this.appId = appId; }
        public String getPrivateKey() { return privateKey; }
        public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
        public String getAlipayPublicKey() { return alipayPublicKey; }
        public void setAlipayPublicKey(String alipayPublicKey) { this.alipayPublicKey = alipayPublicKey; }
        public String getSignType() { return signType; }
        public void setSignType(String signType) { this.signType = signType; }
        public String getCharset() { return charset; }
        public void setCharset(String charset) { this.charset = charset; }
        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }
        public String getGatewayUrl() { return gatewayUrl; }
        public void setGatewayUrl(String gatewayUrl) { this.gatewayUrl = gatewayUrl; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getTimeoutMinutes() { return timeoutMinutes; }
        public void setTimeoutMinutes(int timeoutMinutes) { this.timeoutMinutes = timeoutMinutes; }
    }

    /**
     * 微信支付配置
     */
    public static class WechatPayConfig {
        /**
         * 应用ID
         */
        private String appId;

        /**
         * 商户号
         */
        private String mchId;

        /**
         * 商户密钥
         */
        private String mchKey;

        /**
         * API证书路径
         */
        private String certPath;

        /**
         * API密钥路径
         */
        private String keyPath;

        /**
         * 网关地址
         */
        private String gatewayUrl = "https://api.mch.weixin.qq.com";

        /**
         * 是否启用
         */
        private boolean enabled = true;

        /**
         * 支付超时时间（分钟）
         */
        private int timeoutMinutes = 30;

        // Getter和Setter方法
        public String getAppId() { return appId; }
        public void setAppId(String appId) { this.appId = appId; }
        public String getMchId() { return mchId; }
        public void setMchId(String mchId) { this.mchId = mchId; }
        public String getMchKey() { return mchKey; }
        public void setMchKey(String mchKey) { this.mchKey = mchKey; }
        public String getCertPath() { return certPath; }
        public void setCertPath(String certPath) { this.certPath = certPath; }
        public String getKeyPath() { return keyPath; }
        public void setKeyPath(String keyPath) { this.keyPath = keyPath; }
        public String getGatewayUrl() { return gatewayUrl; }
        public void setGatewayUrl(String gatewayUrl) { this.gatewayUrl = gatewayUrl; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getTimeoutMinutes() { return timeoutMinutes; }
        public void setTimeoutMinutes(int timeoutMinutes) { this.timeoutMinutes = timeoutMinutes; }
    }

    /**
     * 银行卡支付配置
     */
    public static class BankCardConfig {
        /**
         * 商户号
         */
        private String merchantId;

        /**
         * 商户密钥
         */
        private String merchantKey;

        /**
         * 网关地址
         */
        private String gatewayUrl;

        /**
         * 是否启用
         */
        private boolean enabled = true;

        /**
         * 支付超时时间（分钟）
         */
        private int timeoutMinutes = 30;

        /**
         * 支持的银行列表
         */
        private Map<String, String> supportedBanks;

        // Getter和Setter方法
        public String getMerchantId() { return merchantId; }
        public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
        public String getMerchantKey() { return merchantKey; }
        public void setMerchantKey(String merchantKey) { this.merchantKey = merchantKey; }
        public String getGatewayUrl() { return gatewayUrl; }
        public void setGatewayUrl(String gatewayUrl) { this.gatewayUrl = gatewayUrl; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getTimeoutMinutes() { return timeoutMinutes; }
        public void setTimeoutMinutes(int timeoutMinutes) { this.timeoutMinutes = timeoutMinutes; }
        public Map<String, String> getSupportedBanks() { return supportedBanks; }
        public void setSupportedBanks(Map<String, String> supportedBanks) { this.supportedBanks = supportedBanks; }
    }

    /**
     * 余额支付配置
     */
    public static class BalanceConfig {
        /**
         * 是否启用
         */
        private boolean enabled = true;

        /**
         * 最小支付金额
         */
        private BigDecimal minAmount = new BigDecimal("0.01");

        /**
         * 最大支付金额
         */
        private BigDecimal maxAmount = new BigDecimal("50000.00");

        /**
         * 是否需要支付密码
         */
        private boolean requirePaymentPassword = true;

        // Getter和Setter方法
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public BigDecimal getMinAmount() { return minAmount; }
        public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
        public BigDecimal getMaxAmount() { return maxAmount; }
        public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }
        public boolean isRequirePaymentPassword() { return requirePaymentPassword; }
        public void setRequirePaymentPassword(boolean requirePaymentPassword) { this.requirePaymentPassword = requirePaymentPassword; }
    }

    /**
     * 通用配置
     */
    public static class GeneralConfig {
        /**
         * 默认支付超时时间（分钟）
         */
        private int defaultTimeoutMinutes = 30;

        /**
         * 最大重试次数
         */
        private int maxRetryCount = 3;

        /**
         * 重试间隔时间（秒）
         */
        private int retryIntervalSeconds = 60;

        /**
         * 订单过期检查间隔（分钟）
         */
        private int expireCheckIntervalMinutes = 5;

        /**
         * 是否启用异步通知
         */
        private boolean enableAsyncNotification = true;

        /**
         * 异步通知重试次数
         */
        private int notificationRetryCount = 5;

        /**
         * 异步通知超时时间（秒）
         */
        private int notificationTimeoutSeconds = 30;

        // Getter和Setter方法
        public int getDefaultTimeoutMinutes() { return defaultTimeoutMinutes; }
        public void setDefaultTimeoutMinutes(int defaultTimeoutMinutes) { this.defaultTimeoutMinutes = defaultTimeoutMinutes; }
        public int getMaxRetryCount() { return maxRetryCount; }
        public void setMaxRetryCount(int maxRetryCount) { this.maxRetryCount = maxRetryCount; }
        public int getRetryIntervalSeconds() { return retryIntervalSeconds; }
        public void setRetryIntervalSeconds(int retryIntervalSeconds) { this.retryIntervalSeconds = retryIntervalSeconds; }
        public int getExpireCheckIntervalMinutes() { return expireCheckIntervalMinutes; }
        public void setExpireCheckIntervalMinutes(int expireCheckIntervalMinutes) { this.expireCheckIntervalMinutes = expireCheckIntervalMinutes; }
        public boolean isEnableAsyncNotification() { return enableAsyncNotification; }
        public void setEnableAsyncNotification(boolean enableAsyncNotification) { this.enableAsyncNotification = enableAsyncNotification; }
        public int getNotificationRetryCount() { return notificationRetryCount; }
        public void setNotificationRetryCount(int notificationRetryCount) { this.notificationRetryCount = notificationRetryCount; }
        public int getNotificationTimeoutSeconds() { return notificationTimeoutSeconds; }
        public void setNotificationTimeoutSeconds(int notificationTimeoutSeconds) { this.notificationTimeoutSeconds = notificationTimeoutSeconds; }
    }

    /**
     * 手续费配置
     */
    public static class FeeConfig {
        /**
         * 支付宝手续费率
         */
        private BigDecimal alipayFeeRate = new BigDecimal("0.006");

        /**
         * 微信支付手续费率
         */
        private BigDecimal wechatPayFeeRate = new BigDecimal("0.006");

        /**
         * 银行卡支付手续费率
         */
        private BigDecimal bankCardFeeRate = new BigDecimal("0.008");

        /**
         * 信用卡支付手续费率
         */
        private BigDecimal creditCardFeeRate = new BigDecimal("0.010");

        /**
         * 余额支付手续费率
         */
        private BigDecimal balanceFeeRate = BigDecimal.ZERO;

        /**
         * 最小手续费
         */
        private BigDecimal minFee = new BigDecimal("0.01");

        /**
         * 最大手续费
         */
        private BigDecimal maxFee = new BigDecimal("100.00");

        /**
         * 是否向用户收取手续费
         */
        private boolean chargeToUser = false;

        // Getter和Setter方法
        public BigDecimal getAlipayFeeRate() { return alipayFeeRate; }
        public void setAlipayFeeRate(BigDecimal alipayFeeRate) { this.alipayFeeRate = alipayFeeRate; }
        public BigDecimal getWechatPayFeeRate() { return wechatPayFeeRate; }
        public void setWechatPayFeeRate(BigDecimal wechatPayFeeRate) { this.wechatPayFeeRate = wechatPayFeeRate; }
        public BigDecimal getBankCardFeeRate() { return bankCardFeeRate; }
        public void setBankCardFeeRate(BigDecimal bankCardFeeRate) { this.bankCardFeeRate = bankCardFeeRate; }
        public BigDecimal getCreditCardFeeRate() { return creditCardFeeRate; }
        public void setCreditCardFeeRate(BigDecimal creditCardFeeRate) { this.creditCardFeeRate = creditCardFeeRate; }
        public BigDecimal getBalanceFeeRate() { return balanceFeeRate; }
        public void setBalanceFeeRate(BigDecimal balanceFeeRate) { this.balanceFeeRate = balanceFeeRate; }
        public BigDecimal getMinFee() { return minFee; }
        public void setMinFee(BigDecimal minFee) { this.minFee = minFee; }
        public BigDecimal getMaxFee() { return maxFee; }
        public void setMaxFee(BigDecimal maxFee) { this.maxFee = maxFee; }
        public boolean isChargeToUser() { return chargeToUser; }
        public void setChargeToUser(boolean chargeToUser) { this.chargeToUser = chargeToUser; }
    }
}