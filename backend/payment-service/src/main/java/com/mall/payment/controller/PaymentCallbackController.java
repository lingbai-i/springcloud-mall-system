package com.mall.payment.controller;

import com.mall.payment.dto.PaymentCallbackInfo;
import com.mall.payment.dto.response.PaymentOrderResponse;
import com.mall.payment.enums.PaymentMethod;
import com.mall.payment.enums.PaymentStatus;
import com.mall.payment.enums.RefundStatus;
import com.mall.payment.service.PaymentChannelService;
import com.mall.payment.service.PaymentService;
import com.mall.payment.service.RefundChannelService;
import com.mall.payment.service.RefundService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付回调处理控制器
 * 处理来自第三方支付平台的支付和退款回调通知
 * 
 * <p>主要功能包括：</p>
 * <ul>
 *   <li>支付回调处理：处理支付宝、微信、银行卡支付成功/失败通知</li>
 *   <li>退款回调处理：处理各支付渠道的退款成功/失败通知</li>
 *   <li>签名验证：验证回调请求的合法性和完整性</li>
 *   <li>数据解析：解析不同支付平台的回调数据格式</li>
 *   <li>状态同步：将第三方支付状态同步到本地订单</li>
 * </ul>
 * 
 * <p>支持的支付渠道：</p>
 * <ul>
 *   <li>支付宝：处理支付宝异步通知</li>
 *   <li>微信支付：处理微信支付异步通知（XML格式）</li>
 *   <li>银行卡支付：处理银行卡支付异步通知</li>
 * </ul>
 * 
 * <p>回调处理流程：</p>
 * <ol>
 *   <li>接收第三方平台回调请求</li>
 *   <li>提取回调参数和签名信息</li>
 *   <li>验证回调签名的有效性</li>
 *   <li>解析回调数据获取订单状态</li>
 *   <li>查找对应的本地支付订单</li>
 *   <li>更新订单状态并处理业务逻辑</li>
 *   <li>返回符合第三方要求的响应格式</li>
 * </ol>
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * <p>修改日志：</p>
 * <ul>
 *   <li>V1.0 2024-12-01：初始版本，实现基础回调处理功能</li>
 *   <li>V1.1 2025-01-20：增加退款回调处理和签名验证增强</li>
 *   <li>V1.2 2025-11-01：完善Javadoc注释，优化异常处理</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/callback")
public class PaymentCallbackController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentCallbackController.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentChannelService paymentChannelService;

    @Autowired
    private RefundService refundService;

    @Autowired
    private RefundChannelService refundChannelService;

    /**
     * 支付宝支付回调处理
     * 处理支付宝支付成功或失败的异步通知
     * 
     * <p>处理流程：</p>
     * <ol>
     *   <li>提取支付宝回调参数</li>
     *   <li>验证支付宝回调签名</li>
     *   <li>解析回调数据获取支付状态</li>
     *   <li>根据第三方订单号查找本地支付订单</li>
     *   <li>根据支付状态更新订单并处理业务逻辑</li>
     *   <li>返回支付宝要求的响应格式</li>
     * </ol>
     * 
     * <p>响应格式：</p>
     * <ul>
     *   <li>成功：返回"success"字符串</li>
     *   <li>失败：返回"fail"字符串</li>
     * </ul>
     * 
     * @param request HTTP请求对象，包含支付宝回调的所有参数和签名信息
     * @return ResponseEntity包装的处理结果，"success"表示处理成功，"fail"表示处理失败
     * @throws RuntimeException 当系统异常时抛出
     * @since 2025-11-01
     */
    @PostMapping("/alipay/payment")
    public ResponseEntity<String> handleAlipayPaymentCallback(HttpServletRequest request) {
        
        logger.info("收到支付宝支付回调通知");

        try {
            // 获取回调参数
            Map<String, String> params = extractCallbackParams(request);
            logger.debug("支付宝支付回调参数: {}", params);

            // 验证回调签名
            boolean signValid = paymentChannelService.verifyPaymentCallback(PaymentMethod.ALIPAY, params);
            if (!signValid) {
                logger.warn("支付宝支付回调签名验证失败");
                return ResponseEntity.badRequest().body("fail");
            }

            // 解析回调数据
            PaymentCallbackInfo callbackInfo = 
                paymentChannelService.parsePaymentCallback(PaymentMethod.ALIPAY, params);

            if (callbackInfo == null) {
                logger.warn("支付宝支付回调数据解析失败");
                return ResponseEntity.badRequest().body("fail");
            }

            // 通过第三方订单号查找支付订单
            PaymentOrderResponse paymentOrder = paymentService.getPaymentOrderByThirdPartyOrderNo(callbackInfo.getThirdPartyOrderNo());
            if (paymentOrder == null) {
                logger.warn("未找到对应的支付订单，第三方订单号: {}", callbackInfo.getThirdPartyOrderNo());
                return ResponseEntity.badRequest().body("fail");
            }

            // 处理支付回调
            boolean processed = false;
            if (callbackInfo.getStatus() == PaymentStatus.SUCCESS) {
                // 支付成功
                processed = paymentService.handlePaymentSuccess(
                    paymentOrder.getId(),
                    callbackInfo.getThirdPartyOrderNo(),
                    callbackInfo.getActualAmount(),
                    callbackInfo.getRawData()
                );
                logger.info("处理支付宝支付成功回调，支付订单ID: {}, 第三方订单号: {}, 处理结果: {}", 
                           paymentOrder.getId(), callbackInfo.getThirdPartyOrderNo(), processed ? "成功" : "失败");
            } else {
                // 支付失败
                processed = paymentService.handlePaymentFailure(
                    paymentOrder.getId(),
                    callbackInfo.getFailureReason(),
                    callbackInfo.getRawData()
                );
                logger.info("处理支付宝支付失败回调，支付订单ID: {}, 第三方订单号: {}, 失败原因: {}, 处理结果: {}", 
                           paymentOrder.getId(), callbackInfo.getThirdPartyOrderNo(), callbackInfo.getFailureReason(), 
                           processed ? "成功" : "失败");
            }

            return processed ? ResponseEntity.ok("success") : ResponseEntity.badRequest().body("fail");

        } catch (Exception e) {
            logger.error("处理支付宝支付回调异常", e);
            return ResponseEntity.internalServerError().body("fail");
        }
    }

    /**
     * 微信支付回调处理
     * 处理微信支付成功或失败的异步通知
     * 
     * @param request HTTP请求对象，包含回调参数
     * @return 回调处理结果，返回微信要求的XML格式响应
     */
    @PostMapping("/wechat/payment")
    public ResponseEntity<String> handleWechatPaymentCallback(HttpServletRequest request) {
        
        logger.info("收到微信支付回调通知");

        try {
            // 获取回调参数（微信支付回调是XML格式）
            Map<String, String> params = extractCallbackParams(request);
            logger.debug("微信支付回调参数: {}", params);

            // 验证回调签名
            boolean signValid = paymentChannelService.verifyPaymentCallback(PaymentMethod.WECHAT, params);
            if (!signValid) {
                logger.warn("微信支付回调签名验证失败");
                return ResponseEntity.badRequest().body(buildWechatFailResponse("签名验证失败"));
            }

            // 解析回调数据
            PaymentCallbackInfo callbackInfo = 
                paymentChannelService.parsePaymentCallback(PaymentMethod.WECHAT, params);

            if (callbackInfo == null) {
                logger.warn("微信支付回调数据解析失败");
                return ResponseEntity.badRequest().body(buildWechatFailResponse("数据解析失败"));
            }

            // 通过第三方订单号查找支付订单
            PaymentOrderResponse paymentOrder = paymentService.getPaymentOrderByThirdPartyOrderNo(callbackInfo.getThirdPartyOrderNo());
            if (paymentOrder == null) {
                logger.warn("未找到对应的支付订单，第三方订单号: {}", callbackInfo.getThirdPartyOrderNo());
                return ResponseEntity.badRequest().body(buildWechatFailResponse("订单不存在"));
            }

            // 处理支付回调
            boolean processed = false;
            if ("SUCCESS".equals(callbackInfo.getPaymentStatus())) {
                // 支付成功
                processed = paymentService.handlePaymentSuccess(
                    paymentOrder.getId(),
                    callbackInfo.getThirdPartyOrderNo(),
                    callbackInfo.getActualAmount(),
                    callbackInfo.getRawData()
                );
                logger.info("处理微信支付成功回调，支付订单ID: {}, 第三方订单号: {}, 处理结果: {}", 
                           paymentOrder.getId(), callbackInfo.getThirdPartyOrderNo(), processed ? "成功" : "失败");
            } else {
                // 支付失败
                processed = paymentService.handlePaymentFailure(
                    paymentOrder.getId(),
                    callbackInfo.getFailureReason(),
                    callbackInfo.getRawData()
                );
                logger.info("处理微信支付失败回调，支付订单ID: {}, 第三方订单号: {}, 失败原因: {}, 处理结果: {}", 
                           paymentOrder.getId(), callbackInfo.getThirdPartyOrderNo(), callbackInfo.getFailureReason(), 
                           processed ? "成功" : "失败");
            }

            return processed ? 
                ResponseEntity.ok(buildWechatSuccessResponse()) : 
                ResponseEntity.badRequest().body(buildWechatFailResponse("处理失败"));

        } catch (Exception e) {
            logger.error("处理微信支付回调异常", e);
            return ResponseEntity.internalServerError().body(buildWechatFailResponse("系统异常"));
        }
    }

    /**
     * 银行卡支付回调处理
     * 处理银行卡支付成功或失败的异步通知
     * 
     * @param request HTTP请求对象，包含回调参数
     * @return 回调处理结果
     */
    @PostMapping("/bank/payment")
    public ResponseEntity<String> handleBankPaymentCallback(HttpServletRequest request) {
        
        logger.info("收到银行卡支付回调通知");

        try {
            // 获取回调参数
            Map<String, String> params = extractCallbackParams(request);
            logger.debug("银行卡支付回调参数: {}", params);

            // 验证回调签名
            boolean signValid = paymentChannelService.verifyPaymentCallback(PaymentMethod.BANK_CARD, params);
            if (!signValid) {
                logger.warn("银行卡支付回调签名验证失败");
                return ResponseEntity.badRequest().body("FAIL");
            }

            // 解析回调数据
            PaymentCallbackInfo callbackInfo = 
                paymentChannelService.parsePaymentCallback(PaymentMethod.BANK_CARD, params);

            if (callbackInfo == null) {
                logger.warn("银行卡支付回调数据解析失败");
                return ResponseEntity.badRequest().body("FAIL");
            }

            // 通过第三方订单号查找支付订单
            PaymentOrderResponse paymentOrder = paymentService.getPaymentOrderByThirdPartyOrderNo(callbackInfo.getThirdPartyOrderNo());
            if (paymentOrder == null) {
                logger.warn("未找到对应的支付订单，第三方订单号: {}", callbackInfo.getThirdPartyOrderNo());
                return ResponseEntity.badRequest().body("FAIL");
            }

            // 处理支付回调
            boolean processed = false;
            if (PaymentStatus.SUCCESS.equals(callbackInfo.getStatus())) {
                // 支付成功（银行返回码00表示成功）
                processed = paymentService.handlePaymentSuccess(
                    paymentOrder.getId(),
                    callbackInfo.getThirdPartyOrderNo(),
                    callbackInfo.getActualAmount(),
                    callbackInfo.getRawData()
                );
                logger.info("处理银行卡支付成功回调，支付订单ID: {}, 第三方订单号: {}, 处理结果: {}", 
                           paymentOrder.getId(), callbackInfo.getThirdPartyOrderNo(), processed ? "成功" : "失败");
            } else {
                // 支付失败
                processed = paymentService.handlePaymentFailure(
                    paymentOrder.getId(),
                    callbackInfo.getFailureReason(),
                    callbackInfo.getRawData()
                );
                logger.info("处理银行卡支付失败回调，支付订单ID: {}, 第三方订单号: {}, 失败原因: {}, 处理结果: {}", 
                           paymentOrder.getId(), callbackInfo.getThirdPartyOrderNo(), callbackInfo.getFailureReason(), 
                           processed ? "成功" : "失败");
            }

            return processed ? ResponseEntity.ok("SUCCESS") : ResponseEntity.badRequest().body("FAIL");

        } catch (Exception e) {
            logger.error("处理银行卡支付回调异常", e);
            return ResponseEntity.internalServerError().body("FAIL");
        }
    }

    /**
     * 支付宝退款回调处理
     * 处理支付宝退款成功或失败的异步通知
     * 
     * @param request HTTP请求对象，包含回调参数
     * @return 回调处理结果
     */
    @PostMapping("/alipay/refund")
    public ResponseEntity<String> handleAlipayRefundCallback(HttpServletRequest request) {
        
        logger.info("收到支付宝退款回调通知");

        try {
            // 获取回调参数
            Map<String, String> params = extractCallbackParams(request);
            logger.debug("支付宝退款回调参数: {}", params);

            // 验证回调签名
            String callbackData = params.toString();
            String signature = params.get("sign");
            boolean signValid = refundChannelService.verifyRefundCallback(PaymentMethod.ALIPAY, callbackData, signature);
            if (!signValid) {
                logger.warn("支付宝退款回调签名验证失败");
                return ResponseEntity.badRequest().body("fail");
            }

            // 解析回调数据
            RefundChannelService.RefundCallbackInfo callbackInfo = 
                refundChannelService.parseRefundCallback(PaymentMethod.ALIPAY, callbackData);

            if (callbackInfo == null) {
                logger.warn("支付宝退款回调数据解析失败");
                return ResponseEntity.badRequest().body("fail");
            }

            // 处理退款回调
            boolean processed = false;
            if (callbackInfo.getRefundStatus() == RefundStatus.SUCCESS) {
                // 退款成功
                processed = refundService.handleRefundSuccess(
                    callbackInfo.getRefundOrderId(),
                    callbackInfo.getThirdPartyRefundNo(),
                    callbackInfo.getActualRefundAmount(),
                    callbackInfo.getOriginalData()
                );
                logger.info("处理支付宝退款成功回调，退款订单ID: {}, 处理结果: {}", 
                           callbackInfo.getRefundOrderId(), processed ? "成功" : "失败");
            } else {
                // 退款失败
                processed = refundService.handleRefundFailure(
                    callbackInfo.getRefundOrderId(),
                    callbackInfo.getFailureReason(),
                    callbackInfo.getOriginalData()
                );
                logger.info("处理支付宝退款失败回调，退款订单ID: {}, 失败原因: {}, 处理结果: {}", 
                           callbackInfo.getRefundOrderId(), callbackInfo.getFailureReason(), 
                           processed ? "成功" : "失败");
            }

            return processed ? ResponseEntity.ok("success") : ResponseEntity.badRequest().body("fail");

        } catch (Exception e) {
            logger.error("处理支付宝退款回调异常", e);
            return ResponseEntity.internalServerError().body("fail");
        }
    }

    /**
     * 微信退款回调处理
     * 处理微信退款成功或失败的异步通知
     * 
     * @param request HTTP请求对象，包含回调参数
     * @return 回调处理结果
     */
    @PostMapping("/wechat/refund")
    public ResponseEntity<String> handleWechatRefundCallback(HttpServletRequest request) {
        
        logger.info("收到微信退款回调通知");

        try {
            // 获取回调参数
            Map<String, String> params = extractCallbackParams(request);
            logger.debug("微信退款回调参数: {}", params);

            // 验证回调签名
            String callbackData = params.toString();
            String signature = params.get("sign");
            boolean signValid = refundChannelService.verifyRefundCallback(PaymentMethod.WECHAT, callbackData, signature);
            if (!signValid) {
                logger.warn("微信退款回调签名验证失败");
                return ResponseEntity.badRequest().body(buildWechatFailResponse("签名验证失败"));
            }

            // 解析回调数据
            RefundChannelService.RefundCallbackInfo callbackInfo = refundChannelService.parseRefundCallback(PaymentMethod.WECHAT, callbackData);

            if (callbackInfo == null) {
                logger.warn("微信退款回调数据解析失败");
                return ResponseEntity.badRequest().body(buildWechatFailResponse("数据解析失败"));
            }

            // 处理退款回调
            boolean processed = false;
            if (callbackInfo.getRefundStatus() == RefundStatus.SUCCESS) {
                // 退款成功
                processed = refundService.handleRefundSuccess(
                    callbackInfo.getRefundOrderId(),
                    callbackInfo.getThirdPartyRefundNo(),
                    callbackInfo.getActualRefundAmount(),
                    callbackInfo.getOriginalData()
                );
                logger.info("处理微信退款成功回调，退款订单ID: {}, 处理结果: {}", 
                           callbackInfo.getRefundOrderId(), processed ? "成功" : "失败");
            } else {
                // 退款失败
                processed = refundService.handleRefundFailure(
                    callbackInfo.getRefundOrderId(),
                    callbackInfo.getFailureReason(),
                    callbackInfo.getOriginalData()
                );
                logger.info("处理微信退款失败回调，退款订单ID: {}, 失败原因: {}, 处理结果: {}", 
                           callbackInfo.getRefundOrderId(), callbackInfo.getFailureReason(), 
                           processed ? "成功" : "失败");
            }

            return processed ? 
                ResponseEntity.ok(buildWechatSuccessResponse()) : 
                ResponseEntity.badRequest().body(buildWechatFailResponse("处理失败"));

        } catch (Exception e) {
            logger.error("处理微信退款回调异常", e);
            return ResponseEntity.internalServerError().body(buildWechatFailResponse("系统异常"));
        }
    }

    /**
     * 银行卡退款回调处理
     * 处理银行卡退款成功或失败的异步通知
     * 
     * @param request HTTP请求对象，包含回调参数
     * @return 回调处理结果
     */
    @PostMapping("/bank/refund")
    public ResponseEntity<String> handleBankRefundCallback(HttpServletRequest request) {
        
        logger.info("收到银行卡退款回调通知");

        try {
            // 获取回调参数
            Map<String, String> params = extractCallbackParams(request);
            logger.debug("银行卡退款回调参数: {}", params);

            // 验证回调签名
            String callbackData = params.toString();
            String signature = params.get("signature");
            boolean signValid = refundChannelService.verifyRefundCallback(PaymentMethod.BANK_CARD, callbackData, signature);
            if (!signValid) {
                logger.warn("银行卡退款回调签名验证失败");
                return ResponseEntity.badRequest().body("FAIL");
            }

            // 解析回调数据
            RefundChannelService.RefundCallbackInfo callbackInfo = 
                refundChannelService.parseRefundCallback(PaymentMethod.BANK_CARD, callbackData);

            if (callbackInfo == null) {
                logger.warn("银行卡退款回调数据解析失败");
                return ResponseEntity.badRequest().body("FAIL");
            }

            // 处理退款回调
            boolean processed = false;
            if (callbackInfo.getRefundStatus() == RefundStatus.SUCCESS) {
                // 退款成功
                processed = refundService.handleRefundSuccess(
                    callbackInfo.getRefundOrderId(),
                    callbackInfo.getThirdPartyRefundNo(),
                    callbackInfo.getActualRefundAmount(),
                    callbackInfo.getOriginalData()
                );
                logger.info("处理银行卡退款成功回调，退款订单ID: {}, 处理结果: {}", 
                           callbackInfo.getRefundOrderId(), processed ? "成功" : "失败");
            } else {
                // 退款失败
                processed = refundService.handleRefundFailure(
                    callbackInfo.getRefundOrderId(),
                    callbackInfo.getFailureReason(),
                    callbackInfo.getOriginalData()
                );
                logger.info("处理银行卡退款失败回调，退款订单ID: {}, 失败原因: {}, 处理结果: {}", 
                           callbackInfo.getRefundOrderId(), callbackInfo.getFailureReason(), 
                           processed ? "成功" : "失败");
            }

            return processed ? ResponseEntity.ok("SUCCESS") : ResponseEntity.badRequest().body("FAIL");

        } catch (Exception e) {
            logger.error("处理银行卡退款回调异常", e);
            return ResponseEntity.internalServerError().body("FAIL");
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 从HTTP请求中提取回调参数
     * 支持表单参数和JSON参数的提取
     * 
     * @param request HTTP请求对象
     * @return 参数映射
     */
    private Map<String, String> extractCallbackParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        
        // 提取请求参数
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = request.getParameter(paramName);
            params.put(paramName, paramValue);
        }
        
        // 提取请求头（某些支付平台会在请求头中传递参数）
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            // 只提取特定的业务相关头部
            if (headerName.toLowerCase().startsWith("x-") || 
                headerName.toLowerCase().contains("sign") ||
                headerName.toLowerCase().contains("timestamp")) {
                params.put(headerName, headerValue);
            }
        }
        
        return params;
    }

    /**
     * 构建微信支付成功响应XML
     * 
     * @return 微信要求的成功响应格式
     */
    private String buildWechatSuccessResponse() {
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    }

    /**
     * 构建微信支付失败响应XML
     * 
     * @param errorMsg 错误信息
     * @return 微信要求的失败响应格式
     */
    private String buildWechatFailResponse(String errorMsg) {
        return String.format("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[%s]]></return_msg></xml>", 
                           errorMsg);
    }
}