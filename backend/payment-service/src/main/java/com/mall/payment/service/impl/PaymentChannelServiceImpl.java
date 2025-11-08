package com.mall.payment.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.payment.dto.PaymentCallbackInfo;
import com.mall.payment.dto.PaymentQueryResult;
import com.mall.payment.entity.PaymentOrder;
import com.mall.payment.enums.PaymentMethod;
import com.mall.payment.enums.PaymentStatus;
import com.mall.payment.service.PaymentChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.TreeMap;

/**
 * 支付渠道服务实现类
 * 实现与第三方支付平台的交互逻辑，包括支付宝、微信支付等
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>支付发起：调用各支付平台API发起支付</li>
 *   <li>状态查询：主动查询第三方支付状态</li>
 *   <li>回调验证：验证支付平台回调签名</li>
 *   <li>数据解析：解析不同平台的回调数据格式</li>
 * </ul>
 * 
 * <p>支持的支付渠道：</p>
 * <ul>
 *   <li>支付宝：网页支付、手机支付、扫码支付</li>
 *   <li>微信支付：公众号支付、小程序支付、扫码支付</li>
 *   <li>银行卡支付：网银支付、快捷支付</li>
 *   <li>余额支付：内部账户余额扣款</li>
 * </ul>
 * 
 * <p>注意事项：</p>
 * <ul>
 *   <li>当前为模拟实现，生产环境需集成真实的第三方SDK</li>
 *   <li>签名验证使用模拟算法，实际需要使用平台提供的验签方法</li>
 *   <li>支付结果为模拟数据，实际需要调用真实的支付接口</li>
 * </ul>
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * <p>修改日志：</p>
 * <ul>
 *   <li>V1.0 2024-12-01：初始版本，实现基础支付渠道功能</li>
 *   <li>V1.1 2025-01-15：增加状态查询和回调验证功能</li>
 *   <li>V1.2 2025-11-01：完善Javadoc注释，优化错误处理</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentChannelServiceImpl implements PaymentChannelService {

    private static final Logger log = LoggerFactory.getLogger(PaymentChannelServiceImpl.class);
    
    private final ObjectMapper objectMapper;

    // 模拟的第三方支付平台配置
    private static final String ALIPAY_APP_ID = "2021000000000000";
    private static final String ALIPAY_PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC...";
    private static final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...";
    private static final String ALIPAY_GATEWAY = "https://openapi.alipay.com/gateway.do";
    
    private static final String WECHAT_APP_ID = "wx1234567890abcdef";
    private static final String WECHAT_MCH_ID = "1234567890";
    private static final String WECHAT_API_KEY = "abcdefghijklmnopqrstuvwxyz123456";
    private static final String WECHAT_GATEWAY = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    /**
     * 发起支付
     * 
     * @param paymentOrder 支付订单信息
     * @return 支付结果
     */
    @Override
    public String initiatePayment(PaymentOrder paymentOrder) {
        log.info("发起第三方支付，订单ID: {}, 支付方式: {}", paymentOrder.getId(), paymentOrder.getPaymentMethod());

        try {
            switch (paymentOrder.getPaymentMethod()) {
                case ALIPAY:
                    return initiateAlipayPayment(paymentOrder);
                case WECHAT:
                    return initiateWechatPayment(paymentOrder);
                case BANK_CARD:
                    return initiateBankCardPayment(paymentOrder);
                case BALANCE:
                    return initiateBalancePayment(paymentOrder);
                default:
                    throw new UnsupportedOperationException("不支持的支付方式: " + paymentOrder.getPaymentMethod());
            }
        } catch (Exception e) {
            log.error("发起第三方支付失败，订单ID: {}", paymentOrder.getId(), e);
            throw new RuntimeException("发起支付失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询支付状态
     * 
     * @param paymentOrder 支付订单信息
     * @return 支付状态
     */
    @Override
    public PaymentStatus queryPaymentStatus(PaymentOrder paymentOrder) {
        log.debug("查询第三方支付状态，订单ID: {}, 支付方式: {}", paymentOrder.getId(), paymentOrder.getPaymentMethod());

        try {
            switch (paymentOrder.getPaymentMethod()) {
                case ALIPAY:
                    return queryAlipayStatus(paymentOrder);
                case WECHAT:
                    return queryWechatStatus(paymentOrder);
                case BANK_CARD:
                    return queryBankCardStatus(paymentOrder);
                case BALANCE:
                    return queryBalanceStatus(paymentOrder);
                default:
                    log.warn("不支持的支付方式状态查询: {}", paymentOrder.getPaymentMethod());
                    return paymentOrder.getStatus();
            }
        } catch (Exception e) {
            log.error("查询第三方支付状态失败，订单ID: {}", paymentOrder.getId(), e);
            return paymentOrder.getStatus();
        }
    }

    /**
     * 取消支付
     * 
     * @param paymentOrder 支付订单信息
     * @return 取消结果
     */
    @Override
    public boolean cancelPayment(PaymentOrder paymentOrder) {
        log.info("取消第三方支付，订单ID: {}, 支付方式: {}", paymentOrder.getId(), paymentOrder.getPaymentMethod());

        try {
            switch (paymentOrder.getPaymentMethod()) {
                case ALIPAY:
                    return cancelAlipayPayment(paymentOrder);
                case WECHAT:
                    return cancelWechatPayment(paymentOrder);
                case BANK_CARD:
                    return cancelBankCardPayment(paymentOrder);
                case BALANCE:
                    return cancelBalancePayment(paymentOrder);
                default:
                    log.warn("不支持的支付方式取消: {}", paymentOrder.getPaymentMethod());
                    return false;
            }
        } catch (Exception e) {
            log.error("取消第三方支付失败，订单ID: {}", paymentOrder.getId(), e);
            return false;
        }
    }

    /**
     * 验证支付回调签名
     * 
     * @param paymentOrder 支付订单信息
     * @param callbackData 回调数据
     * @param signature 签名
     * @return 验证结果
     */
    @Override
    public boolean verifyCallbackSignature(PaymentOrder paymentOrder, String callbackData, String signature) {
        log.debug("验证支付回调签名，订单ID: {}, 支付方式: {}", paymentOrder.getId(), paymentOrder.getPaymentMethod());

        try {
            switch (paymentOrder.getPaymentMethod()) {
                case ALIPAY:
                    return verifyAlipaySignature(callbackData, signature);
                case WECHAT:
                    return verifyWechatSignature(callbackData, signature);
                case BANK_CARD:
                    return verifyBankCardSignature(callbackData, signature);
                case BALANCE:
                    return verifyBalanceSignature(callbackData, signature);
                default:
                    log.warn("不支持的支付方式签名验证: {}", paymentOrder.getPaymentMethod());
                    return false;
            }
        } catch (Exception e) {
            log.error("验证支付回调签名失败，订单ID: {}", paymentOrder.getId(), e);
            return false;
        }
    }

    /**
     * 解析支付回调数据
     * 
     * @param paymentOrder 支付订单信息
     * @param callbackData 回调数据
     * @return 解析后的回调信息
     */
    @Override
    public PaymentCallbackInfo parseCallbackData(PaymentOrder paymentOrder, String callbackData) {
        log.debug("解析支付回调数据，订单ID: {}, 支付方式: {}", paymentOrder.getId(), paymentOrder.getPaymentMethod());

        try {
            switch (paymentOrder.getPaymentMethod()) {
                case ALIPAY:
                    return parseAlipayCallback(callbackData);
                case WECHAT:
                    return parseWechatCallback(callbackData);
                case BANK_CARD:
                    return parseBankCardCallback(callbackData);
                case BALANCE:
                    return parseBalanceCallback(callbackData);
                default:
                    log.warn("不支持的支付方式回调解析: {}", paymentOrder.getPaymentMethod());
                    return new PaymentCallbackInfo();
            }
        } catch (Exception e) {
            log.error("解析支付回调数据失败，订单ID: {}", paymentOrder.getId(), e);
            return new PaymentCallbackInfo();
        }
    }

    // ==================== 支付宝支付相关方法 ====================

    /**
     * 发起支付宝支付
     */
    private String initiateAlipayPayment(PaymentOrder paymentOrder) {
        log.info("发起支付宝支付，订单ID: {}", paymentOrder.getId());

        // 构建支付宝支付参数
        Map<String, String> params = new HashMap<>();
        params.put("app_id", ALIPAY_APP_ID);
        params.put("method", "alipay.trade.page.pay");
        params.put("charset", "UTF-8");
        params.put("sign_type", "RSA2");
        params.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        params.put("version", "1.0");
        params.put("notify_url", paymentOrder.getNotifyUrl());
        params.put("return_url", paymentOrder.getReturnUrl());

        // 业务参数
        Map<String, Object> bizContent = new HashMap<>();
        bizContent.put("out_trade_no", paymentOrder.getId());
        bizContent.put("total_amount", paymentOrder.getAmount().toString());
        bizContent.put("subject", paymentOrder.getDescription());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

        try {
            params.put("biz_content", objectMapper.writeValueAsString(bizContent));
            
            // 生成签名（这里简化处理，实际应该使用RSA签名）
            String sign = generateSign(params, ALIPAY_PRIVATE_KEY);
            params.put("sign", sign);

            // 构建支付URL
            StringBuilder payUrl = new StringBuilder(ALIPAY_GATEWAY).append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                payUrl.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }

            String result = payUrl.toString();
            log.info("支付宝支付URL生成成功，订单ID: {}", paymentOrder.getId());
            return result;

        } catch (Exception e) {
            log.error("发起支付宝支付失败，订单ID: {}", paymentOrder.getId(), e);
            throw new RuntimeException("发起支付宝支付失败", e);
        }
    }

    /**
     * 查询支付宝支付状态
     */
    private PaymentStatus queryAlipayStatus(PaymentOrder paymentOrder) {
        log.debug("查询支付宝支付状态，订单ID: {}", paymentOrder.getId());

        // 模拟查询逻辑
        // 实际应该调用支付宝的alipay.trade.query接口
        
        // 这里简化处理，随机返回状态用于演示
        if (StringUtils.hasText(paymentOrder.getThirdPartyOrderNo())) {
            // 如果有第三方订单号，说明支付可能已完成
            return PaymentStatus.SUCCESS;
        } else {
            // 否则保持原状态
            return paymentOrder.getStatus();
        }
    }

    /**
     * 取消支付宝支付
     */
    private boolean cancelAlipayPayment(PaymentOrder paymentOrder) {
        log.info("取消支付宝支付，订单ID: {}", paymentOrder.getId());

        // 模拟取消逻辑
        // 实际应该调用支付宝的alipay.trade.cancel接口
        
        try {
            // 这里简化处理，直接返回成功
            log.info("支付宝支付取消成功，订单ID: {}", paymentOrder.getId());
            return true;
        } catch (Exception e) {
            log.error("取消支付宝支付失败，订单ID: {}", paymentOrder.getId(), e);
            return false;
        }
    }

    /**
     * 验证支付宝签名
     */
    private boolean verifyAlipaySignature(String data, String signature) {
        // 实际应该使用支付宝公钥验证RSA签名
        // 这里简化处理
        return StringUtils.hasText(signature);
    }

    /**
     * 解析支付宝回调数据
     */
    private PaymentCallbackInfo parseAlipayCallback(String callbackData) {
        try {
            // 解析支付宝回调参数
            Map<String, Object> params = objectMapper.readValue(callbackData, Map.class);
            
            PaymentCallbackInfo info = new PaymentCallbackInfo();
            info.setThirdPartyOrderNo((String) params.get("trade_no"));
            info.setActualAmount(new BigDecimal((String) params.get("total_amount")));
            info.setRawData(callbackData);
            
            // 根据交易状态设置支付状态
            String tradeStatus = (String) params.get("trade_status");
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                info.setStatus(PaymentStatus.SUCCESS);
            } else if ("TRADE_CLOSED".equals(tradeStatus)) {
                info.setStatus(PaymentStatus.CANCELLED);
            } else {
                info.setStatus(PaymentStatus.FAILED);
                info.setFailureReason("支付宝交易状态异常: " + tradeStatus);
            }
            
            return info;
        } catch (Exception e) {
            log.error("解析支付宝回调数据失败", e);
            return new PaymentCallbackInfo();
        }
    }

    // ==================== 微信支付相关方法 ====================

    /**
     * 发起微信支付
     */
    private String initiateWechatPayment(PaymentOrder paymentOrder) {
        log.info("发起微信支付，订单ID: {}", paymentOrder.getId());

        // 构建微信支付参数
        Map<String, String> params = new TreeMap<>();
        params.put("appid", WECHAT_APP_ID);
        params.put("mch_id", WECHAT_MCH_ID);
        params.put("nonce_str", generateNonceStr());
        params.put("body", paymentOrder.getDescription());
        params.put("out_trade_no", paymentOrder.getId());
        params.put("total_fee", paymentOrder.getAmount().multiply(new BigDecimal("100")).intValue() + ""); // 转换为分
        params.put("spbill_create_ip", "127.0.0.1");
        params.put("notify_url", paymentOrder.getNotifyUrl());
        params.put("trade_type", "NATIVE"); // 扫码支付

        // 生成签名
        String sign = generateWechatSign(params, WECHAT_API_KEY);
        params.put("sign", sign);

        try {
            // 构建XML请求
            String xmlRequest = buildWechatXmlRequest(params);
            
            // 这里应该发送HTTP请求到微信支付接口
            // 简化处理，直接返回模拟的二维码内容
            String qrCodeUrl = "weixin://wxpay/bizpayurl?pr=" + generateNonceStr();
            
            log.info("微信支付二维码生成成功，订单ID: {}", paymentOrder.getId());
            return qrCodeUrl;

        } catch (Exception e) {
            log.error("发起微信支付失败，订单ID: {}", paymentOrder.getId(), e);
            throw new RuntimeException("发起微信支付失败", e);
        }
    }

    /**
     * 查询微信支付状态
     */
    private PaymentStatus queryWechatStatus(PaymentOrder paymentOrder) {
        log.debug("查询微信支付状态，订单ID: {}", paymentOrder.getId());

        // 模拟查询逻辑
        // 实际应该调用微信的orderquery接口
        
        if (StringUtils.hasText(paymentOrder.getThirdPartyOrderNo())) {
            return PaymentStatus.SUCCESS;
        } else {
            return paymentOrder.getStatus();
        }
    }

    /**
     * 取消微信支付
     */
    private boolean cancelWechatPayment(PaymentOrder paymentOrder) {
        log.info("取消微信支付，订单ID: {}", paymentOrder.getId());

        // 模拟取消逻辑
        // 实际应该调用微信的closeorder接口
        
        try {
            log.info("微信支付取消成功，订单ID: {}", paymentOrder.getId());
            return true;
        } catch (Exception e) {
            log.error("取消微信支付失败，订单ID: {}", paymentOrder.getId(), e);
            return false;
        }
    }

    /**
     * 验证微信签名
     */
    private boolean verifyWechatSignature(String data, String signature) {
        // 实际应该验证微信的MD5签名
        // 这里简化处理
        return StringUtils.hasText(signature);
    }

    /**
     * 解析微信回调数据
     */
    private PaymentCallbackInfo parseWechatCallback(String callbackData) {
        try {
            // 解析微信XML回调数据
            // 这里简化处理，实际应该解析XML
            
            PaymentCallbackInfo info = new PaymentCallbackInfo();
            info.setThirdPartyOrderNo("wx" + System.currentTimeMillis());
            info.setStatus(PaymentStatus.SUCCESS);
            info.setRawData(callbackData);
            
            return info;
        } catch (Exception e) {
            log.error("解析微信回调数据失败", e);
            return new PaymentCallbackInfo();
        }
    }

    // ==================== 银行卡支付相关方法 ====================

    /**
     * 发起银行卡支付
     */
    private String initiateBankCardPayment(PaymentOrder paymentOrder) {
        log.info("发起银行卡支付，订单ID: {}", paymentOrder.getId());

        // 模拟银行卡支付
        // 实际应该集成银联或其他银行卡支付接口
        
        String paymentUrl = "https://payment.bank.com/pay?orderId=" + paymentOrder.getId() + 
                           "&amount=" + paymentOrder.getAmount();
        
        log.info("银行卡支付URL生成成功，订单ID: {}", paymentOrder.getId());
        return paymentUrl;
    }

    /**
     * 查询银行卡支付状态
     */
    private PaymentStatus queryBankCardStatus(PaymentOrder paymentOrder) {
        log.debug("查询银行卡支付状态，订单ID: {}", paymentOrder.getId());
        
        // 模拟查询逻辑
        return paymentOrder.getStatus();
    }

    /**
     * 取消银行卡支付
     */
    private boolean cancelBankCardPayment(PaymentOrder paymentOrder) {
        log.info("取消银行卡支付，订单ID: {}", paymentOrder.getId());
        
        // 模拟取消逻辑
        return true;
    }

    /**
     * 验证银行卡签名
     */
    private boolean verifyBankCardSignature(String data, String signature) {
        return StringUtils.hasText(signature);
    }

    /**
     * 解析银行卡回调数据
     */
    private PaymentCallbackInfo parseBankCardCallback(String callbackData) {
        PaymentCallbackInfo info = new PaymentCallbackInfo();
        info.setStatus(PaymentStatus.SUCCESS);
        info.setRawData(callbackData);
        return info;
    }

    // ==================== 余额支付相关方法 ====================

    /**
     * 发起余额支付
     */
    private String initiateBalancePayment(PaymentOrder paymentOrder) {
        log.info("发起余额支付，订单ID: {}", paymentOrder.getId());

        // 余额支付通常是同步的，直接返回成功结果
        // 实际应该检查用户余额是否充足，然后扣减余额
        
        return "BALANCE_PAY_SUCCESS";
    }

    /**
     * 查询余额支付状态
     */
    private PaymentStatus queryBalanceStatus(PaymentOrder paymentOrder) {
        log.debug("查询余额支付状态，订单ID: {}", paymentOrder.getId());
        
        // 余额支付通常是同步的，状态不会改变
        return paymentOrder.getStatus();
    }

    /**
     * 取消余额支付
     */
    private boolean cancelBalancePayment(PaymentOrder paymentOrder) {
        log.info("取消余额支付，订单ID: {}", paymentOrder.getId());
        
        // 余额支付取消需要退回余额
        return true;
    }

    /**
     * 验证余额支付签名
     */
    private boolean verifyBalanceSignature(String data, String signature) {
        // 余额支付是内部处理，不需要验证外部签名
        return true;
    }

    /**
     * 解析余额支付回调数据
     */
    private PaymentCallbackInfo parseBalanceCallback(String callbackData) {
        PaymentCallbackInfo info = new PaymentCallbackInfo();
        info.setStatus(PaymentStatus.SUCCESS);
        info.setRawData(callbackData);
        return info;
    }

    /**
     * 处理支付宝支付
     */
    private com.mall.payment.dto.PaymentResult processAlipayPayment(BigDecimal amount, String orderId, String returnUrl, String notifyUrl) {
        try {
            // 模拟支付宝支付处理
            String thirdPartyOrderNo = "alipay_" + System.currentTimeMillis();
            String paymentUrl = "https://openapi.alipay.com/gateway.do?order_id=" + orderId;
            
            return com.mall.payment.dto.PaymentResult.success(PaymentStatus.PROCESSING, thirdPartyOrderNo, paymentUrl, amount);
        } catch (Exception e) {
            return com.mall.payment.dto.PaymentResult.failure("ALIPAY_ERROR", "支付宝支付处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理微信支付
     */
    private com.mall.payment.dto.PaymentResult processWechatPayment(BigDecimal amount, String orderId, String returnUrl, String notifyUrl) {
        try {
            // 模拟微信支付处理
            String thirdPartyOrderNo = "wechat_" + System.currentTimeMillis();
            String paymentUrl = "weixin://wxpay/bizpayurl?pr=" + orderId;
            
            return com.mall.payment.dto.PaymentResult.success(PaymentStatus.PROCESSING, thirdPartyOrderNo, paymentUrl, amount);
        } catch (Exception e) {
            return com.mall.payment.dto.PaymentResult.failure("WECHAT_ERROR", "微信支付处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理银行卡支付
     */
    private com.mall.payment.dto.PaymentResult processBankPayment(BigDecimal amount, String orderId, String returnUrl, String notifyUrl) {
        try {
            // 模拟银行卡支付处理
            String thirdPartyOrderNo = "bank_" + System.currentTimeMillis();
            String paymentUrl = "https://bank.example.com/pay?order_id=" + orderId;
            
            return com.mall.payment.dto.PaymentResult.success(PaymentStatus.PROCESSING, thirdPartyOrderNo, paymentUrl, amount);
        } catch (Exception e) {
            return com.mall.payment.dto.PaymentResult.failure("BANK_ERROR", "银行卡支付处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理余额支付
     */
    private com.mall.payment.dto.PaymentResult processBalancePayment(BigDecimal amount, String orderId, String returnUrl, String notifyUrl) {
        try {
            // 模拟余额支付处理（通常是同步的）
            String thirdPartyOrderNo = "balance_" + System.currentTimeMillis();
            
            // 余额支付通常是即时成功或失败
            return com.mall.payment.dto.PaymentResult.success(PaymentStatus.SUCCESS, thirdPartyOrderNo, null, amount);
        } catch (Exception e) {
            return com.mall.payment.dto.PaymentResult.failure("BALANCE_ERROR", "余额支付处理失败: " + e.getMessage());
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 生成签名（简化实现）
     */
    private String generateSign(Map<String, String> params, String privateKey) {
        // 实际应该使用RSA私钥签名
        // 这里简化处理，使用MD5
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!"sign".equals(entry.getKey()) && StringUtils.hasText(entry.getValue())) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(sb.toString().getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("生成签名失败", e);
            return "";
        }
    }

    /**
     * 生成微信签名
     */
    private String generateWechatSign(Map<String, String> params, String apiKey) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (StringUtils.hasText(entry.getValue())) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        sb.append("key=").append(apiKey);
        
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(sb.toString().getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString().toUpperCase();
        } catch (Exception e) {
            log.error("生成微信签名失败", e);
            return "";
        }
    }

    /**
     * 生成随机字符串
     */
    private String generateNonceStr() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 构建微信XML请求
     */
    private String buildWechatXmlRequest(Map<String, String> params) {
        StringBuilder xml = new StringBuilder("<xml>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            xml.append("<").append(entry.getKey()).append(">")
               .append(entry.getValue())
               .append("</").append(entry.getKey()).append(">");
        }
        xml.append("</xml>");
        return xml.toString();
    }

    /**
     * 查询支付状态
     * 主动查询第三方支付平台的支付状态
     * 
     * @param paymentMethod 支付方式
     * @param thirdPartyOrderNo 第三方订单号
     * @return 支付查询结果
     * @author lingbai
     * @since V1.0 2025-01-27: 新增查询支付状态功能
     */
    /**
     * 验证支付回调
     * 验证第三方支付平台回调请求的有效性
     * 
     * @param paymentMethod 支付方式
     * @param params 回调参数
     * @return 验证结果，true表示验证通过，false表示验证失败
     */
    @Override
    public boolean verifyPaymentCallback(PaymentMethod paymentMethod, Map<String, String> params) {
        log.info("验证支付回调，支付方式: {}", paymentMethod);
        
        try {
            switch (paymentMethod) {
                case ALIPAY:
                    return verifyAlipayCallback(params);
                case WECHAT:
                    return verifyWechatCallback(params);
                case BANK_CARD:
                    return verifyBankCardCallback(params);
                case BALANCE:
                    return verifyBalanceCallback(params);
                default:
                    log.warn("不支持的支付方式: {}", paymentMethod);
                    return false;
            }
        } catch (Exception e) {
            log.error("验证支付回调异常，支付方式: {}", paymentMethod, e);
            return false;
        }
    }

    /**
     * 解析支付回调数据
     * 解析第三方支付平台的回调数据，提取关键信息
     * 
     * @param paymentMethod 支付方式
     * @param params 回调参数
     * @return 解析后的回调信息
     */
    @Override
    public PaymentCallbackInfo parsePaymentCallback(PaymentMethod paymentMethod, Map<String, String> params) {
        log.info("解析支付回调数据，支付方式: {}", paymentMethod);
        
        try {
            switch (paymentMethod) {
                case ALIPAY:
                    return parseAlipayCallbackParams(params);
                case WECHAT:
                    return parseWechatCallbackParams(params);
                case BANK_CARD:
                    return parseBankCardCallbackParams(params);
                case BALANCE:
                    return parseBalanceCallbackParams(params);
                default:
                    log.warn("不支持的支付方式: {}", paymentMethod);
                    return new PaymentCallbackInfo();
            }
        } catch (Exception e) {
            log.error("解析支付回调数据异常，支付方式: {}", paymentMethod, e);
            PaymentCallbackInfo errorInfo = new PaymentCallbackInfo();
            errorInfo.setStatus(PaymentStatus.FAILED);
            errorInfo.setFailureReason("回调数据解析失败: " + e.getMessage());
            return errorInfo;
        }
    }

    @Override
    public com.mall.payment.dto.PaymentResult processPayment(PaymentMethod paymentMethod, BigDecimal amount, 
                                                           String orderId, String returnUrl, String notifyUrl) {
        log.info("处理支付请求，支付方式: {}, 金额: {}, 订单ID: {}", paymentMethod, amount, orderId);
        
        try {
            switch (paymentMethod) {
                case ALIPAY:
                    return processAlipayPayment(amount, orderId, returnUrl, notifyUrl);
                case WECHAT:
                    return processWechatPayment(amount, orderId, returnUrl, notifyUrl);
                case BANK_CARD:
                    return processBankPayment(amount, orderId, returnUrl, notifyUrl);
                case BALANCE:
                    return processBalancePayment(amount, orderId, returnUrl, notifyUrl);
                default:
                    return com.mall.payment.dto.PaymentResult.failure("UNSUPPORTED_METHOD", "不支持的支付方式: " + paymentMethod);
            }
        } catch (Exception e) {
            log.error("处理支付请求失败，支付方式: {}, 错误: {}", paymentMethod, e.getMessage(), e);
            return com.mall.payment.dto.PaymentResult.failure("PAYMENT_ERROR", "支付处理失败: " + e.getMessage());
        }
    }

    @Override
    public PaymentQueryResult queryPaymentStatus(PaymentMethod paymentMethod, String thirdPartyOrderNo) {
        log.info("查询支付状态，支付方式: {}, 第三方订单号: {}", paymentMethod, thirdPartyOrderNo);
        
        try {
            switch (paymentMethod) {
                case ALIPAY:
                    return queryAlipayStatus(thirdPartyOrderNo);
                case WECHAT:
                    return queryWechatStatus(thirdPartyOrderNo);
                case BANK_CARD:
                    return queryBankCardStatus(thirdPartyOrderNo);
                default:
                    log.warn("不支持的支付方式: {}", paymentMethod);
                    return new PaymentQueryResult(PaymentStatus.FAILED, null, 
                                                "不支持的支付方式", null);
            }
        } catch (Exception e) {
            log.error("查询支付状态异常，支付方式: {}, 第三方订单号: {}, 错误: {}", 
                     paymentMethod, thirdPartyOrderNo, e.getMessage(), e);
            return new PaymentQueryResult(PaymentStatus.FAILED, null, 
                                        "查询支付状态异常: " + e.getMessage(), null);
        }
    }

    /**
     * 查询支付宝支付状态
     */
    private PaymentQueryResult queryAlipayStatus(String thirdPartyOrderNo) {
        log.debug("查询支付宝支付状态，第三方订单号: {}", thirdPartyOrderNo);
        
        try {
            // 构建查询参数
            Map<String, String> params = new HashMap<>();
            params.put("app_id", ALIPAY_APP_ID);
            params.put("method", "alipay.trade.query");
            params.put("charset", "UTF-8");
            params.put("sign_type", "RSA2");
            params.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            params.put("version", "1.0");

            // 业务参数
            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", thirdPartyOrderNo);
            params.put("biz_content", objectMapper.writeValueAsString(bizContent));

            // 生成签名
            String sign = generateSign(params, ALIPAY_PRIVATE_KEY);
            params.put("sign", sign);

            // 发送查询请求（这里简化处理，实际应该发送HTTP请求）
            // 模拟查询结果
            PaymentStatus status = PaymentStatus.SUCCESS; // 实际应该解析响应
            BigDecimal actualAmount = new BigDecimal("100.00"); // 实际应该从响应中获取
            
            log.info("支付宝支付状态查询成功，第三方订单号: {}, 状态: {}", thirdPartyOrderNo, status);
            return new PaymentQueryResult(status, actualAmount, null, "支付宝查询响应数据");
            
        } catch (Exception e) {
            log.error("查询支付宝支付状态失败，第三方订单号: {}, 错误: {}", thirdPartyOrderNo, e.getMessage(), e);
            return new PaymentQueryResult(PaymentStatus.FAILED, null, 
                                        "查询支付宝状态失败: " + e.getMessage(), null);
        }
    }

    /**
     * 查询微信支付状态
     */
    private PaymentQueryResult queryWechatStatus(String thirdPartyOrderNo) {
        log.debug("查询微信支付状态，第三方订单号: {}", thirdPartyOrderNo);
        
        try {
            // 构建查询参数
            Map<String, String> params = new HashMap<>();
            params.put("appid", WECHAT_APP_ID);
            params.put("mch_id", WECHAT_MCH_ID);
            params.put("out_trade_no", thirdPartyOrderNo);
            params.put("nonce_str", generateNonceStr());

            // 生成签名
            String sign = generateWechatSign(params, WECHAT_API_KEY);
            params.put("sign", sign);

            // 构建XML请求
            String xmlRequest = buildWechatXmlRequest(params);

            // 发送查询请求（这里简化处理，实际应该发送HTTP请求）
            // 模拟查询结果
            PaymentStatus status = PaymentStatus.SUCCESS; // 实际应该解析响应
            BigDecimal actualAmount = new BigDecimal("100.00"); // 实际应该从响应中获取
            
            log.info("微信支付状态查询成功，第三方订单号: {}, 状态: {}", thirdPartyOrderNo, status);
            return new PaymentQueryResult(status, actualAmount, null, "微信查询响应数据");
            
        } catch (Exception e) {
            log.error("查询微信支付状态失败，第三方订单号: {}, 错误: {}", thirdPartyOrderNo, e.getMessage(), e);
            return new PaymentQueryResult(PaymentStatus.FAILED, null, 
                                        "查询微信状态失败: " + e.getMessage(), null);
        }
    }

    /**
     * 查询银行卡支付状态
     */
    private PaymentQueryResult queryBankCardStatus(String thirdPartyOrderNo) {
        log.debug("查询银行卡支付状态，第三方订单号: {}", thirdPartyOrderNo);
        
        try {
            // 构建查询参数（这里简化处理，实际应该根据具体银行接口）
            Map<String, String> params = new HashMap<>();
            params.put("merchantId", "merchant123");
            params.put("orderNo", thirdPartyOrderNo);
            params.put("timestamp", String.valueOf(System.currentTimeMillis()));

            // 生成签名
            String sign = generateSign(params, "bankPrivateKey");
            params.put("sign", sign);

            // 发送查询请求（这里简化处理，实际应该发送HTTP请求）
            // 模拟查询结果
            PaymentStatus status = PaymentStatus.SUCCESS; // 实际应该解析响应
            BigDecimal actualAmount = new BigDecimal("100.00"); // 实际应该从响应中获取
            
            log.info("银行卡支付状态查询成功，第三方订单号: {}, 状态: {}", thirdPartyOrderNo, status);
            return new PaymentQueryResult(status, actualAmount, null, "银行卡查询响应数据");
            
        } catch (Exception e) {
            log.error("查询银行卡支付状态失败，第三方订单号: {}, 错误: {}", thirdPartyOrderNo, e.getMessage(), e);
            return new PaymentQueryResult(PaymentStatus.FAILED, null, 
                                        "查询银行卡状态失败: " + e.getMessage(), null);
        }
    }

    /**
     * 验证支付宝回调参数
     */
    private boolean verifyAlipayCallback(Map<String, String> params) {
        try {
            String sign = params.get("sign");
            if (!StringUtils.hasText(sign)) {
                return false;
            }
            
            // 模拟支付宝签名验证
            // 实际实现中需要使用支付宝SDK进行签名验证
            return true;
        } catch (Exception e) {
            log.error("验证支付宝回调异常", e);
            return false;
        }
    }

    /**
     * 验证微信支付回调参数
     */
    private boolean verifyWechatCallback(Map<String, String> params) {
        try {
            String sign = params.get("sign");
            if (!StringUtils.hasText(sign)) {
                return false;
            }
            
            // 模拟微信支付签名验证
            // 实际实现中需要使用微信支付SDK进行签名验证
            return true;
        } catch (Exception e) {
            log.error("验证微信支付回调异常", e);
            return false;
        }
    }

    /**
     * 验证银行卡回调参数
     */
    private boolean verifyBankCardCallback(Map<String, String> params) {
        try {
            String signature = params.get("signature");
            if (!StringUtils.hasText(signature)) {
                return false;
            }
            
            // 模拟银行卡签名验证
            return true;
        } catch (Exception e) {
            log.error("验证银行卡回调异常", e);
            return false;
        }
    }

    /**
     * 验证余额支付回调参数
     */
    private boolean verifyBalanceCallback(Map<String, String> params) {
        try {
            // 余额支付通常不需要复杂的签名验证
            return params.containsKey("order_no") && params.containsKey("status");
        } catch (Exception e) {
            log.error("验证余额支付回调异常", e);
            return false;
        }
    }

    /**
     * 解析支付宝回调参数
     */
    private PaymentCallbackInfo parseAlipayCallbackParams(Map<String, String> params) {
        PaymentCallbackInfo info = new PaymentCallbackInfo();
        
        try {
            info.setThirdPartyOrderNo(params.get("out_trade_no"));
            
            String tradeStatus = params.get("trade_status");
            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                info.setStatus(PaymentStatus.SUCCESS);
            } else if ("TRADE_CLOSED".equals(tradeStatus)) {
                info.setStatus(PaymentStatus.FAILED);
            } else {
                info.setStatus(PaymentStatus.PENDING);
            }
            
            String totalAmount = params.get("total_amount");
            if (StringUtils.hasText(totalAmount)) {
                info.setActualAmount(new BigDecimal(totalAmount));
            }
            
            info.setRawData(params.toString());
            
        } catch (Exception e) {
            log.error("解析支付宝回调参数异常", e);
            info.setStatus(PaymentStatus.FAILED);
            info.setFailureReason("解析回调参数失败: " + e.getMessage());
        }
        
        return info;
    }

    /**
     * 解析微信支付回调参数
     */
    private PaymentCallbackInfo parseWechatCallbackParams(Map<String, String> params) {
        PaymentCallbackInfo info = new PaymentCallbackInfo();
        
        try {
            info.setThirdPartyOrderNo(params.get("out_trade_no"));
            
            String resultCode = params.get("result_code");
            if ("SUCCESS".equals(resultCode)) {
                info.setStatus(PaymentStatus.SUCCESS);
            } else {
                info.setStatus(PaymentStatus.FAILED);
                info.setFailureReason(params.get("err_code_des"));
            }
            
            String totalFee = params.get("total_fee");
            if (StringUtils.hasText(totalFee)) {
                // 微信支付金额单位是分，需要转换为元
                info.setActualAmount(new BigDecimal(totalFee).divide(new BigDecimal("100")));
            }
            
            info.setRawData(params.toString());
            
        } catch (Exception e) {
            log.error("解析微信支付回调参数异常", e);
            info.setStatus(PaymentStatus.FAILED);
            info.setFailureReason("解析回调参数失败: " + e.getMessage());
        }
        
        return info;
    }

    /**
     * 解析银行卡回调参数
     */
    private PaymentCallbackInfo parseBankCardCallbackParams(Map<String, String> params) {
        PaymentCallbackInfo info = new PaymentCallbackInfo();
        
        try {
            info.setThirdPartyOrderNo(params.get("order_no"));
            
            String status = params.get("status");
            if ("SUCCESS".equals(status)) {
                info.setStatus(PaymentStatus.SUCCESS);
            } else if ("FAILED".equals(status)) {
                info.setStatus(PaymentStatus.FAILED);
                info.setFailureReason(params.get("error_msg"));
            } else {
                info.setStatus(PaymentStatus.PENDING);
            }
            
            String amount = params.get("amount");
            if (StringUtils.hasText(amount)) {
                info.setActualAmount(new BigDecimal(amount));
            }
            
            info.setRawData(params.toString());
            
        } catch (Exception e) {
            log.error("解析银行卡回调参数异常", e);
            info.setStatus(PaymentStatus.FAILED);
            info.setFailureReason("解析回调参数失败: " + e.getMessage());
        }
        
        return info;
    }

    /**
     * 解析余额支付回调参数
     */
    private PaymentCallbackInfo parseBalanceCallbackParams(Map<String, String> params) {
        PaymentCallbackInfo info = new PaymentCallbackInfo();
        
        try {
            info.setThirdPartyOrderNo(params.get("order_no"));
            
            String status = params.get("status");
            if ("SUCCESS".equals(status)) {
                info.setStatus(PaymentStatus.SUCCESS);
            } else if ("FAILED".equals(status)) {
                info.setStatus(PaymentStatus.FAILED);
                info.setFailureReason(params.get("error_msg"));
            } else {
                info.setStatus(PaymentStatus.PENDING);
            }
            
            String amount = params.get("amount");
            if (StringUtils.hasText(amount)) {
                info.setActualAmount(new BigDecimal(amount));
            }
            
            info.setRawData(params.toString());
            
        } catch (Exception e) {
            log.error("解析余额支付回调参数异常", e);
            info.setStatus(PaymentStatus.FAILED);
            info.setFailureReason("解析回调参数失败: " + e.getMessage());
        }
        
        return info;
    }
}