package com.mall.payment.service.impl;

import com.mall.payment.enums.PaymentMethod;
import com.mall.payment.enums.RefundStatus;
import com.mall.payment.service.RefundChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * 退款渠道服务实现类
 * 实现与第三方支付平台退款相关的接口方法，处理不同支付渠道的退款操作
 * 注意：这是一个模拟实现，实际项目中需要集成真实的第三方支付SDK
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@Service
public class RefundChannelServiceImpl implements RefundChannelService {

    private static final Logger logger = LoggerFactory.getLogger(RefundChannelServiceImpl.class);

    // 模拟的第三方支付配置
    private static final Map<PaymentMethod, Map<String, String>> CHANNEL_CONFIGS = new HashMap<>();
    
    // 模拟的退款手续费率配置
    private static final Map<PaymentMethod, BigDecimal> REFUND_FEE_RATES = new HashMap<>();

    static {
        // 初始化支付宝配置
        Map<String, String> alipayConfig = new HashMap<>();
        alipayConfig.put("appId", "2021000000000000");
        alipayConfig.put("privateKey", "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC...");
        alipayConfig.put("publicKey", "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...");
        alipayConfig.put("gatewayUrl", "https://openapi.alipay.com/gateway.do");
        CHANNEL_CONFIGS.put(PaymentMethod.ALIPAY, alipayConfig);

        // 初始化微信支付配置
        Map<String, String> wechatConfig = new HashMap<>();
        wechatConfig.put("appId", "wx1234567890123456");
        wechatConfig.put("mchId", "1234567890");
        wechatConfig.put("apiKey", "abcdefghijklmnopqrstuvwxyz123456");
        wechatConfig.put("gatewayUrl", "https://api.mch.weixin.qq.com");
        CHANNEL_CONFIGS.put(PaymentMethod.WECHAT, wechatConfig);

        // 初始化银行卡配置
        Map<String, String> bankConfig = new HashMap<>();
        bankConfig.put("merchantId", "BANK123456");
        bankConfig.put("secretKey", "bank_secret_key_123456");
        bankConfig.put("gatewayUrl", "https://gateway.bank.com");
        CHANNEL_CONFIGS.put(PaymentMethod.BANK_CARD, bankConfig);

        // 初始化退款手续费率
        REFUND_FEE_RATES.put(PaymentMethod.ALIPAY, new BigDecimal("0.006"));      // 0.6%
        REFUND_FEE_RATES.put(PaymentMethod.WECHAT, new BigDecimal("0.006"));  // 0.6%
        REFUND_FEE_RATES.put(PaymentMethod.BANK_CARD, new BigDecimal("0.01"));    // 1.0%
        REFUND_FEE_RATES.put(PaymentMethod.BALANCE, BigDecimal.ZERO);             // 0%
        REFUND_FEE_RATES.put(PaymentMethod.CREDIT_CARD, new BigDecimal("0.015")); // 1.5%
        REFUND_FEE_RATES.put(PaymentMethod.PAYPAL, new BigDecimal("0.035"));      // 3.5%
        REFUND_FEE_RATES.put(PaymentMethod.CRYPTO, new BigDecimal("0.02")); // 2.0%
    }

    /**
     * 处理退款申请
     * 根据不同的支付方式调用相应的第三方退款接口
     */
    @Override
    public RefundResult processRefund(PaymentMethod paymentMethod, String thirdPartyOrderNo, 
                                     String refundOrderId, BigDecimal refundAmount, String refundReason) {
        logger.info("开始处理退款申请，支付方式: {}, 第三方订单号: {}, 退款订单ID: {}, 退款金额: {}", 
                   paymentMethod, thirdPartyOrderNo, refundOrderId, refundAmount);

        // 参数验证
        validateRefundParams(paymentMethod, thirdPartyOrderNo, refundOrderId, refundAmount);

        try {
            // 根据支付方式选择处理逻辑
            switch (paymentMethod) {
                case ALIPAY:
                    return processAlipayRefund(thirdPartyOrderNo, refundOrderId, refundAmount, refundReason);
                case WECHAT:
                    return processWechatRefund(thirdPartyOrderNo, refundOrderId, refundAmount, refundReason);
                case BANK_CARD:
                    return processBankCardRefund(thirdPartyOrderNo, refundOrderId, refundAmount, refundReason);
                case BALANCE:
                    return processBalanceRefund(thirdPartyOrderNo, refundOrderId, refundAmount, refundReason);
                case CREDIT_CARD:
                    return processCreditCardRefund(thirdPartyOrderNo, refundOrderId, refundAmount, refundReason);
                case PAYPAL:
                    return processPaypalRefund(thirdPartyOrderNo, refundOrderId, refundAmount, refundReason);
                case CRYPTO:
                    return processDigitalCurrencyRefund(thirdPartyOrderNo, refundOrderId, refundAmount, refundReason);
                default:
                    throw new IllegalArgumentException("不支持的支付方式: " + paymentMethod);
            }
        } catch (Exception e) {
            logger.error("处理退款申请异常，支付方式: {}, 退款订单ID: {}", paymentMethod, refundOrderId, e);
            return RefundResult.failure("系统异常: " + e.getMessage(), null);
        }
    }

    /**
     * 查询退款状态
     */
    @Override
    public RefundQueryResult queryRefundStatus(PaymentMethod paymentMethod, String thirdPartyRefundNo) {
        logger.debug("查询退款状态，支付方式: {}, 第三方退款单号: {}", paymentMethod, thirdPartyRefundNo);

        if (!StringUtils.hasText(thirdPartyRefundNo)) {
            throw new IllegalArgumentException("第三方退款单号不能为空");
        }

        try {
            // 模拟查询第三方退款状态
            // 实际实现中需要调用相应的第三方API
            Random random = new Random();
            int statusCode = random.nextInt(100);
            
            if (statusCode < 80) {
                return new RefundQueryResult(RefundStatus.SUCCESS, null, null, 
                    "{\"status\":\"SUCCESS\",\"refund_no\":\"" + thirdPartyRefundNo + "\"}");
            } else if (statusCode < 90) {
                return new RefundQueryResult(RefundStatus.PROCESSING, null, null, 
                    "{\"status\":\"PROCESSING\",\"refund_no\":\"" + thirdPartyRefundNo + "\"}");
            } else {
                return new RefundQueryResult(RefundStatus.FAILED, null, "查询失败", 
                    "{\"status\":\"FAILED\",\"refund_no\":\"" + thirdPartyRefundNo + "\"}");
            }
        } catch (Exception e) {
            logger.error("查询退款状态异常，支付方式: {}, 第三方退款单号: {}", paymentMethod, thirdPartyRefundNo, e);
            return new RefundQueryResult(RefundStatus.FAILED, null, "查询退款状态失败: " + e.getMessage(), null);
        }
    }

    /**
     * 验证退款回调签名
     */
    @Override
    public boolean verifyRefundCallback(PaymentMethod paymentMethod, String callbackData, String signature) {
        logger.debug("验证退款回调签名，支付方式: {}", paymentMethod);

        if (!StringUtils.hasText(callbackData) || !StringUtils.hasText(signature)) {
            return false;
        }

        try {
            // 根据支付方式选择验证逻辑
            switch (paymentMethod) {
                case ALIPAY:
                    return verifyAlipayRefundCallback(callbackData, signature);
                case WECHAT:
                    return verifyWechatRefundCallback(callbackData, signature);
                case BANK_CARD:
                    return verifyBankCardRefundCallback(callbackData, signature);
                default:
                    // 其他支付方式的签名验证
                    return verifyGenericRefundCallback(paymentMethod, callbackData, signature);
            }
        } catch (Exception e) {
            logger.error("验证退款回调签名异常，支付方式: {}", paymentMethod, e);
            return false;
        }
    }

    /**
     * 解析退款回调数据
     */
    @Override
    public RefundCallbackInfo parseRefundCallback(PaymentMethod paymentMethod, String callbackData) {
        logger.debug("解析退款回调数据，支付方式: {}", paymentMethod);

        if (!StringUtils.hasText(callbackData)) {
            throw new IllegalArgumentException("回调数据不能为空");
        }

        try {
            // 根据支付方式选择解析逻辑
            switch (paymentMethod) {
                case ALIPAY:
                    return parseAlipayRefundCallback(callbackData);
                case WECHAT:
                    return parseWechatRefundCallback(callbackData);
                case BANK_CARD:
                    return parseBankCardRefundCallback(callbackData);
                default:
                    return parseGenericRefundCallback(paymentMethod, callbackData);
            }
        } catch (Exception e) {
            logger.error("解析退款回调数据异常，支付方式: {}", paymentMethod, e);
            throw new IllegalArgumentException("回调数据格式无效", e);
        }
    }

    /**
     * 取消退款申请
     */
    @Override
    public boolean cancelRefund(PaymentMethod paymentMethod, String thirdPartyRefundNo, String cancelReason) {
        logger.info("取消退款申请，支付方式: {}, 第三方退款单号: {}, 取消原因: {}", 
                   paymentMethod, thirdPartyRefundNo, cancelReason);

        if (!supportsRefundCancel(paymentMethod)) {
            logger.warn("支付方式不支持取消退款，支付方式: {}", paymentMethod);
            return false;
        }

        try {
            // 模拟调用第三方取消退款接口
            // 实际实现中需要调用相应的第三方API
            Random random = new Random();
            boolean success = random.nextBoolean();
            
            logger.info("取消退款申请结果: {}, 支付方式: {}, 第三方退款单号: {}", 
                       success ? "成功" : "失败", paymentMethod, thirdPartyRefundNo);
            
            return success;
        } catch (Exception e) {
            logger.error("取消退款申请异常，支付方式: {}, 第三方退款单号: {}", paymentMethod, thirdPartyRefundNo, e);
            return false;
        }
    }

    /**
     * 获取退款手续费
     */
    @Override
    public BigDecimal getRefundFee(PaymentMethod paymentMethod, BigDecimal refundAmount) {
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal feeRate = REFUND_FEE_RATES.getOrDefault(paymentMethod, BigDecimal.ZERO);
        BigDecimal fee = refundAmount.multiply(feeRate);
        
        // 保留2位小数，向上取整
        return fee.setScale(2, BigDecimal.ROUND_UP);
    }

    /**
     * 检查是否支持退款
     */
    @Override
    public boolean supportsRefund(PaymentMethod paymentMethod) {
        // 大部分支付方式都支持退款，除了一些特殊情况
        switch (paymentMethod) {
            case ALIPAY:
            case WECHAT:
            case BANK_CARD:
            case BALANCE:
            case CREDIT_CARD:
            case PAYPAL:
                return true;
            case CRYPTO:
                // 数字货币退款可能有特殊限制
                return true;
            default:
                return false;
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证退款参数
     */
    private void validateRefundParams(PaymentMethod paymentMethod, String thirdPartyOrderNo, 
                                     String refundOrderId, BigDecimal refundAmount) {
        if (paymentMethod == null) {
            throw new IllegalArgumentException("支付方式不能为空");
        }
        if (!StringUtils.hasText(thirdPartyOrderNo)) {
            throw new IllegalArgumentException("第三方订单号不能为空");
        }
        if (!StringUtils.hasText(refundOrderId)) {
            throw new IllegalArgumentException("退款订单ID不能为空");
        }
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("退款金额必须大于0");
        }
        if (!supportsRefund(paymentMethod)) {
            throw new IllegalArgumentException("支付方式不支持退款: " + paymentMethod);
        }
    }

    /**
     * 处理支付宝退款
     */
    private RefundResult processAlipayRefund(String thirdPartyOrderNo, String refundOrderId, 
                                           BigDecimal refundAmount, String refundReason) {
        logger.info("处理支付宝退款，第三方订单号: {}, 退款金额: {}", thirdPartyOrderNo, refundAmount);

        try {
            // 模拟调用支付宝退款接口
            // 实际实现中需要使用支付宝SDK
            String thirdPartyRefundNo = "ALIPAY_RF_" + System.currentTimeMillis();
            
            // 模拟退款结果
            Random random = new Random();
            boolean success = random.nextInt(100) < 85; // 85%成功率
            
            if (success) {
                // 计算实际退款金额（扣除手续费）
                BigDecimal refundFee = getRefundFee(PaymentMethod.ALIPAY, refundAmount);
                BigDecimal actualRefundAmount = refundAmount.subtract(refundFee);
                
                String channelResponse = String.format(
                    "{\"code\":\"10000\",\"msg\":\"Success\",\"refund_no\":\"%s\",\"refund_amount\":\"%s\"}",
                    thirdPartyRefundNo, actualRefundAmount
                );
                
                return RefundResult.success(thirdPartyRefundNo, actualRefundAmount, channelResponse);
            } else {
                String channelResponse = "{\"code\":\"40004\",\"msg\":\"Business Failed\",\"sub_msg\":\"退款失败\"}";
                return RefundResult.failure("支付宝退款失败", channelResponse);
            }
        } catch (Exception e) {
            logger.error("支付宝退款异常", e);
            return RefundResult.failure("支付宝退款异常: " + e.getMessage(), null);
        }
    }

    /**
     * 处理微信支付退款
     */
    private RefundResult processWechatRefund(String thirdPartyOrderNo, String refundOrderId, 
                                           BigDecimal refundAmount, String refundReason) {
        logger.info("处理微信支付退款，第三方订单号: {}, 退款金额: {}", thirdPartyOrderNo, refundAmount);

        try {
            // 模拟调用微信支付退款接口
            String thirdPartyRefundNo = "WECHAT_RF_" + System.currentTimeMillis();
            
            Random random = new Random();
            boolean success = random.nextInt(100) < 80; // 80%成功率
            
            if (success) {
                BigDecimal refundFee = getRefundFee(PaymentMethod.WECHAT, refundAmount);
                BigDecimal actualRefundAmount = refundAmount.subtract(refundFee);
                
                String channelResponse = String.format(
                    "<xml><return_code>SUCCESS</return_code><refund_id>%s</refund_id><refund_fee>%s</refund_fee></xml>",
                    thirdPartyRefundNo, actualRefundAmount.multiply(new BigDecimal("100")).intValue()
                );
                
                return RefundResult.success(thirdPartyRefundNo, actualRefundAmount, channelResponse);
            } else {
                String channelResponse = "<xml><return_code>FAIL</return_code><return_msg>退款失败</return_msg></xml>";
                return RefundResult.failure("微信支付退款失败", channelResponse);
            }
        } catch (Exception e) {
            logger.error("微信支付退款异常", e);
            return RefundResult.failure("微信支付退款异常: " + e.getMessage(), null);
        }
    }

    /**
     * 处理银行卡退款
     */
    private RefundResult processBankCardRefund(String thirdPartyOrderNo, String refundOrderId, 
                                             BigDecimal refundAmount, String refundReason) {
        logger.info("处理银行卡退款，第三方订单号: {}, 退款金额: {}", thirdPartyOrderNo, refundAmount);

        try {
            String thirdPartyRefundNo = "BANK_RF_" + System.currentTimeMillis();
            
            Random random = new Random();
            boolean success = random.nextInt(100) < 75; // 75%成功率
            
            if (success) {
                BigDecimal refundFee = getRefundFee(PaymentMethod.BANK_CARD, refundAmount);
                BigDecimal actualRefundAmount = refundAmount.subtract(refundFee);
                
                String channelResponse = String.format(
                    "{\"status\":\"SUCCESS\",\"refund_no\":\"%s\",\"amount\":\"%s\"}",
                    thirdPartyRefundNo, actualRefundAmount
                );
                
                return RefundResult.success(thirdPartyRefundNo, actualRefundAmount, channelResponse);
            } else {
                String channelResponse = "{\"status\":\"FAILED\",\"message\":\"银行卡退款失败\"}";
                return RefundResult.failure("银行卡退款失败", channelResponse);
            }
        } catch (Exception e) {
            logger.error("银行卡退款异常", e);
            return RefundResult.failure("银行卡退款异常: " + e.getMessage(), null);
        }
    }

    /**
     * 处理余额退款
     */
    private RefundResult processBalanceRefund(String thirdPartyOrderNo, String refundOrderId, 
                                            BigDecimal refundAmount, String refundReason) {
        logger.info("处理余额退款，第三方订单号: {}, 退款金额: {}", thirdPartyOrderNo, refundAmount);

        try {
            // 余额退款通常是即时到账的
            String thirdPartyRefundNo = "BALANCE_RF_" + System.currentTimeMillis();
            
            // 余额退款无手续费
            String channelResponse = String.format(
                "{\"status\":\"SUCCESS\",\"refund_no\":\"%s\",\"amount\":\"%s\",\"type\":\"BALANCE\"}",
                thirdPartyRefundNo, refundAmount
            );
            
            return RefundResult.success(thirdPartyRefundNo, refundAmount, channelResponse);
        } catch (Exception e) {
            logger.error("余额退款异常", e);
            return RefundResult.failure("余额退款异常: " + e.getMessage(), null);
        }
    }

    /**
     * 处理信用卡退款
     */
    private RefundResult processCreditCardRefund(String thirdPartyOrderNo, String refundOrderId, 
                                               BigDecimal refundAmount, String refundReason) {
        logger.info("处理信用卡退款，第三方订单号: {}, 退款金额: {}", thirdPartyOrderNo, refundAmount);

        try {
            String thirdPartyRefundNo = "CREDIT_RF_" + System.currentTimeMillis();
            
            Random random = new Random();
            boolean success = random.nextInt(100) < 70; // 70%成功率
            
            if (success) {
                BigDecimal refundFee = getRefundFee(PaymentMethod.CREDIT_CARD, refundAmount);
                BigDecimal actualRefundAmount = refundAmount.subtract(refundFee);
                
                String channelResponse = String.format(
                    "{\"status\":\"SUCCESS\",\"refund_no\":\"%s\",\"amount\":\"%s\",\"type\":\"CREDIT_CARD\"}",
                    thirdPartyRefundNo, actualRefundAmount
                );
                
                return RefundResult.success(thirdPartyRefundNo, actualRefundAmount, channelResponse);
            } else {
                String channelResponse = "{\"status\":\"FAILED\",\"message\":\"信用卡退款失败\"}";
                return RefundResult.failure("信用卡退款失败", channelResponse);
            }
        } catch (Exception e) {
            logger.error("信用卡退款异常", e);
            return RefundResult.failure("信用卡退款异常: " + e.getMessage(), null);
        }
    }

    /**
     * 处理PayPal退款
     */
    private RefundResult processPaypalRefund(String thirdPartyOrderNo, String refundOrderId, 
                                           BigDecimal refundAmount, String refundReason) {
        logger.info("处理PayPal退款，第三方订单号: {}, 退款金额: {}", thirdPartyOrderNo, refundAmount);

        try {
            String thirdPartyRefundNo = "PAYPAL_RF_" + System.currentTimeMillis();
            
            Random random = new Random();
            boolean success = random.nextInt(100) < 85; // 85%成功率
            
            if (success) {
                BigDecimal refundFee = getRefundFee(PaymentMethod.PAYPAL, refundAmount);
                BigDecimal actualRefundAmount = refundAmount.subtract(refundFee);
                
                String channelResponse = String.format(
                    "{\"status\":\"COMPLETED\",\"id\":\"%s\",\"amount\":{\"value\":\"%s\",\"currency\":\"USD\"}}",
                    thirdPartyRefundNo, actualRefundAmount
                );
                
                return RefundResult.success(thirdPartyRefundNo, actualRefundAmount, channelResponse);
            } else {
                String channelResponse = "{\"status\":\"FAILED\",\"message\":\"PayPal refund failed\"}";
                return RefundResult.failure("PayPal退款失败", channelResponse);
            }
        } catch (Exception e) {
            logger.error("PayPal退款异常", e);
            return RefundResult.failure("PayPal退款异常: " + e.getMessage(), null);
        }
    }

    /**
     * 处理数字货币退款
     */
    private RefundResult processDigitalCurrencyRefund(String thirdPartyOrderNo, String refundOrderId, 
                                                    BigDecimal refundAmount, String refundReason) {
        logger.info("处理数字货币退款，第三方订单号: {}, 退款金额: {}", thirdPartyOrderNo, refundAmount);

        try {
            String thirdPartyRefundNo = "CRYPTO_RF_" + System.currentTimeMillis();
            
            Random random = new Random();
            boolean success = random.nextInt(100) < 60; // 60%成功率（数字货币退款相对复杂）
            
            if (success) {
                BigDecimal refundFee = getRefundFee(PaymentMethod.CRYPTO, refundAmount);
                BigDecimal actualRefundAmount = refundAmount.subtract(refundFee);
                
                String channelResponse = String.format(
                    "{\"status\":\"SUCCESS\",\"txid\":\"%s\",\"amount\":\"%s\",\"currency\":\"BTC\"}",
                    thirdPartyRefundNo, actualRefundAmount
                );
                
                return RefundResult.success(thirdPartyRefundNo, actualRefundAmount, channelResponse);
            } else {
                String channelResponse = "{\"status\":\"FAILED\",\"message\":\"数字货币退款失败\"}";
                return RefundResult.failure("数字货币退款失败", channelResponse);
            }
        } catch (Exception e) {
            logger.error("数字货币退款异常", e);
            return RefundResult.failure("数字货币退款异常: " + e.getMessage(), null);
        }
    }

    /**
     * 验证支付宝退款回调签名
     */
    private boolean verifyAlipayRefundCallback(String callbackData, String signature) {
        try {
            // 模拟支付宝签名验证
            String expectedSignature = generateMockSignature("ALIPAY", callbackData);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            logger.error("验证支付宝退款回调签名异常", e);
            return false;
        }
    }

    /**
     * 验证微信支付退款回调签名
     */
    private boolean verifyWechatRefundCallback(String callbackData, String signature) {
        try {
            // 模拟微信支付签名验证
            String expectedSignature = generateMockSignature("WECHAT", callbackData);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            logger.error("验证微信支付退款回调签名异常", e);
            return false;
        }
    }

    /**
     * 验证银行卡退款回调签名
     */
    private boolean verifyBankCardRefundCallback(String callbackData, String signature) {
        try {
            // 模拟银行卡签名验证
            String expectedSignature = generateMockSignature("BANK", callbackData);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            logger.error("验证银行卡退款回调签名异常", e);
            return false;
        }
    }

    /**
     * 验证通用退款回调签名
     */
    private boolean verifyGenericRefundCallback(PaymentMethod paymentMethod, String callbackData, String signature) {
        try {
            String expectedSignature = generateMockSignature(paymentMethod.name(), callbackData);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            logger.error("验证通用退款回调签名异常，支付方式: {}", paymentMethod, e);
            return false;
        }
    }

    /**
     * 解析支付宝退款回调
     */
    private RefundCallbackInfo parseAlipayRefundCallback(String callbackData) {
        // 模拟解析支付宝退款回调数据
        String refundOrderId = "RF" + System.currentTimeMillis();
        String thirdPartyRefundNo = "ALIPAY_RF_" + System.currentTimeMillis();
        RefundStatus refundStatus = RefundStatus.SUCCESS;
        BigDecimal actualRefundAmount = new BigDecimal("100.00");
        
        return new RefundCallbackInfo(refundOrderId, thirdPartyRefundNo, refundStatus, 
                                     actualRefundAmount, null, callbackData);
    }

    /**
     * 解析微信支付退款回调
     */
    private RefundCallbackInfo parseWechatRefundCallback(String callbackData) {
        // 模拟解析微信支付退款回调数据
        String refundOrderId = "RF" + System.currentTimeMillis();
        String thirdPartyRefundNo = "WECHAT_RF_" + System.currentTimeMillis();
        RefundStatus refundStatus = RefundStatus.SUCCESS;
        BigDecimal actualRefundAmount = new BigDecimal("100.00");
        
        return new RefundCallbackInfo(refundOrderId, thirdPartyRefundNo, refundStatus, 
                                     actualRefundAmount, null, callbackData);
    }

    /**
     * 解析银行卡退款回调
     */
    private RefundCallbackInfo parseBankCardRefundCallback(String callbackData) {
        // 模拟解析银行卡退款回调数据
        String refundOrderId = "RF" + System.currentTimeMillis();
        String thirdPartyRefundNo = "BANK_RF_" + System.currentTimeMillis();
        RefundStatus refundStatus = RefundStatus.SUCCESS;
        BigDecimal actualRefundAmount = new BigDecimal("100.00");
        
        return new RefundCallbackInfo(refundOrderId, thirdPartyRefundNo, refundStatus, 
                                     actualRefundAmount, null, callbackData);
    }

    /**
     * 解析通用退款回调
     */
    private RefundCallbackInfo parseGenericRefundCallback(PaymentMethod paymentMethod, String callbackData) {
        // 模拟解析通用退款回调数据
        String refundOrderId = "RF" + System.currentTimeMillis();
        String thirdPartyRefundNo = paymentMethod.name() + "_RF_" + System.currentTimeMillis();
        RefundStatus refundStatus = RefundStatus.SUCCESS;
        BigDecimal actualRefundAmount = new BigDecimal("100.00");
        
        return new RefundCallbackInfo(refundOrderId, thirdPartyRefundNo, refundStatus, 
                                     actualRefundAmount, null, callbackData);
    }

    /**
     * 检查是否支持取消退款
     */
    private boolean supportsRefundCancel(PaymentMethod paymentMethod) {
        // 大部分支付方式在退款处理中状态时支持取消
        switch (paymentMethod) {
            case ALIPAY:
            case WECHAT:
            case BANK_CARD:
                return true;
            case BALANCE:
                // 余额退款通常是即时的，不支持取消
                return false;
            default:
                return false;
        }
    }

    /**
     * 生成模拟签名
     */
    private String generateMockSignature(String prefix, String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String signData = prefix + "_" + data + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            byte[] digest = md.digest(signData.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().toUpperCase();
        } catch (Exception e) {
            return UUID.randomUUID().toString().replace("-", "").toUpperCase();
        }
    }



    /**
     * 查询支付宝退款状态
     */
    private RefundQueryResult queryAlipayRefundStatus(String thirdPartyRefundId) {
        logger.info("查询支付宝退款状态，第三方退款ID: {}", thirdPartyRefundId);
        
        try {
            // 模拟支付宝退款查询API调用
            Random random = new Random();
            int statusCode = random.nextInt(100);
            
            if (statusCode < 80) {
                // 80%概率退款成功
                return new RefundQueryResult(RefundStatus.SUCCESS, null, null, 
                    "{\"status\":\"SUCCESS\",\"refund_id\":\"" + thirdPartyRefundId + "\"}");
            } else if (statusCode < 95) {
                // 15%概率退款处理中
                return new RefundQueryResult(RefundStatus.PROCESSING, null, null, 
                    "{\"status\":\"PROCESSING\",\"refund_id\":\"" + thirdPartyRefundId + "\"}");
            } else {
                // 5%概率退款失败
                return new RefundQueryResult(RefundStatus.FAILED, null, "支付宝退款失败", 
                    "{\"status\":\"FAILED\",\"refund_id\":\"" + thirdPartyRefundId + "\"}");
            }
        } catch (Exception e) {
            logger.error("查询支付宝退款状态异常", e);
            return new RefundQueryResult(RefundStatus.FAILED, null, "查询支付宝退款状态异常: " + e.getMessage(), null);
        }
    }

    /**
     * 查询微信退款状态
     */
    private RefundQueryResult queryWechatRefundStatus(String thirdPartyRefundId) {
        logger.info("查询微信退款状态，第三方退款ID: {}", thirdPartyRefundId);
        
        try {
            // 模拟微信退款查询API调用
            Random random = new Random();
            int statusCode = random.nextInt(100);
            
            if (statusCode < 75) {
                // 75%概率退款成功
                return new RefundQueryResult(RefundStatus.SUCCESS, null, null, 
                    "<xml><return_code>SUCCESS</return_code><refund_id>" + thirdPartyRefundId + "</refund_id></xml>");
            } else if (statusCode < 90) {
                // 15%概率退款处理中
                return new RefundQueryResult(RefundStatus.PROCESSING, null, null, 
                    "<xml><return_code>PROCESSING</return_code><refund_id>" + thirdPartyRefundId + "</refund_id></xml>");
            } else {
                // 10%概率退款失败
                return new RefundQueryResult(RefundStatus.FAILED, null, "微信退款失败", 
                    "<xml><return_code>FAIL</return_code><refund_id>" + thirdPartyRefundId + "</refund_id></xml>");
            }
        } catch (Exception e) {
            logger.error("查询微信退款状态异常", e);
            return new RefundQueryResult(RefundStatus.FAILED, null, "查询微信退款状态异常: " + e.getMessage(), null);
        }
    }

    /**
     * 查询银行卡退款状态
     */
    private RefundQueryResult queryBankCardRefundStatus(String thirdPartyRefundId) {
        logger.info("查询银行卡退款状态，第三方退款ID: {}", thirdPartyRefundId);
        
        try {
            // 模拟银行卡退款查询API调用
            Random random = new Random();
            int statusCode = random.nextInt(100);
            
            if (statusCode < 70) {
                // 70%概率退款成功
                return new RefundQueryResult(RefundStatus.SUCCESS, null, null, 
                    "{\"status\":\"SUCCESS\",\"refund_id\":\"" + thirdPartyRefundId + "\",\"type\":\"BANK_CARD\"}");
            } else if (statusCode < 85) {
                // 15%概率退款处理中
                return new RefundQueryResult(RefundStatus.PROCESSING, null, null, 
                    "{\"status\":\"PROCESSING\",\"refund_id\":\"" + thirdPartyRefundId + "\",\"type\":\"BANK_CARD\"}");
            } else {
                // 15%概率退款失败
                return new RefundQueryResult(RefundStatus.FAILED, null, "银行卡退款失败", 
                    "{\"status\":\"FAILED\",\"refund_id\":\"" + thirdPartyRefundId + "\",\"type\":\"BANK_CARD\"}");
            }
        } catch (Exception e) {
            logger.error("查询银行卡退款状态异常", e);
            return new RefundQueryResult(RefundStatus.FAILED, null, "查询银行卡退款状态异常: " + e.getMessage(), null);
        }
    }

    /**
     * 查询余额退款状态
     */
    private RefundQueryResult queryBalanceRefundStatus(String thirdPartyRefundId) {
        logger.info("查询余额退款状态，第三方退款ID: {}", thirdPartyRefundId);
        
        try {
            // 余额退款通常是即时成功的
            return new RefundQueryResult(RefundStatus.SUCCESS, null, null, 
                "{\"status\":\"SUCCESS\",\"refund_id\":\"" + thirdPartyRefundId + "\",\"type\":\"BALANCE\"}");
        } catch (Exception e) {
            logger.error("查询余额退款状态异常", e);
            return new RefundQueryResult(RefundStatus.FAILED, null, "查询余额退款状态异常: " + e.getMessage(), null);
        }
    }

    /**
     * 查询信用卡退款状态
     */
    private RefundQueryResult queryCreditCardRefundStatus(String thirdPartyRefundId) {
        logger.info("查询信用卡退款状态，第三方退款ID: {}", thirdPartyRefundId);
        
        try {
            // 模拟信用卡退款查询API调用
            Random random = new Random();
            int statusCode = random.nextInt(100);
            
            if (statusCode < 65) {
                // 65%概率退款成功
                return new RefundQueryResult(RefundStatus.SUCCESS, null, null, 
                    "{\"status\":\"SUCCESS\",\"refund_id\":\"" + thirdPartyRefundId + "\",\"type\":\"CREDIT_CARD\"}");
            } else if (statusCode < 80) {
                // 15%概率退款处理中
                return new RefundQueryResult(RefundStatus.PROCESSING, null, null, 
                    "{\"status\":\"PROCESSING\",\"refund_id\":\"" + thirdPartyRefundId + "\",\"type\":\"CREDIT_CARD\"}");
            } else {
                // 20%概率退款失败
                return new RefundQueryResult(RefundStatus.FAILED, null, "信用卡退款失败", 
                    "{\"status\":\"FAILED\",\"refund_id\":\"" + thirdPartyRefundId + "\",\"type\":\"CREDIT_CARD\"}");
            }
        } catch (Exception e) {
            logger.error("查询信用卡退款状态异常", e);
            return new RefundQueryResult(RefundStatus.FAILED, null, "查询信用卡退款状态异常: " + e.getMessage(), null);
        }
    }
}