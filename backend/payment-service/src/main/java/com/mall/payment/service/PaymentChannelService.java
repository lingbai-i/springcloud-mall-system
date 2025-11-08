package com.mall.payment.service;

import com.mall.payment.dto.PaymentCallbackInfo;
import com.mall.payment.entity.PaymentOrder;
import com.mall.payment.enums.PaymentMethod;
import com.mall.payment.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付渠道服务接口
 * 
 * 定义与第三方支付平台交互的核心方法，负责处理具体的支付渠道对接逻辑。
 * 该接口封装了不同支付方式的统一操作，包括：
 * - 支付订单的创建和发起
 * - 支付状态的查询和同步
 * - 支付订单的取消和退款
 * - 支付回调的验证和解析
 * - 签名验证和数据安全处理
 * 
 * 支持的支付渠道：
 * - 支付宝（Alipay）：网页支付、手机支付、扫码支付
 * - 微信支付（WeChat Pay）：公众号支付、小程序支付、扫码支付
 * - 银行卡支付：网银支付、快捷支付
 * - 余额支付：内部账户余额扣款
 * 
 * 设计原则：
 * - 统一接口：不同支付渠道提供统一的调用接口
 * - 安全可靠：严格的签名验证和数据加密
 * - 异常处理：完善的错误处理和重试机制
 * - 可扩展性：支持新增支付渠道的快速接入
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * 修改日志：
 * V1.2 2025-11-01：完善Javadoc注释，添加详细的方法说明和设计原则
 * V1.1 2025-01-01：添加支付回调验证和解析功能
 * V1.0 2024-12-01：初始版本，基础支付渠道功能
 */
public interface PaymentChannelService {

    /**
     * 发起支付
     * 
     * 调用第三方支付平台接口，创建支付订单并返回支付信息。
     * 该方法会根据支付订单中的支付方式，选择对应的支付渠道进行处理：
     * - 支付宝：生成支付宝支付URL或表单
     * - 微信支付：生成微信支付二维码或调起支付参数
     * - 银行卡：生成网银支付跳转链接
     * - 余额支付：直接进行余额扣款处理
     * 
     * 处理流程：
     * 1. 验证支付订单信息的完整性
     * 2. 构建第三方支付平台所需的请求参数
     * 3. 生成签名并调用第三方接口
     * 4. 解析响应结果并返回支付信息
     * 
     * @param paymentOrder 支付订单信息，包含订单金额、支付方式、商户信息等
     * @return 支付结果字符串，根据支付方式不同可能是支付URL、二维码内容、表单HTML等
     * @throws IllegalArgumentException 当支付订单信息无效时抛出
     * @throws RuntimeException 当调用第三方接口失败或网络异常时抛出
     * @since 2025-11-01
     */
    String initiatePayment(PaymentOrder paymentOrder);

    /**
     * 查询支付状态
     * 
     * 主动查询第三方支付平台的订单状态，用于支付状态同步和对账。
     * 该方法会调用对应支付渠道的查询接口，获取最新的支付状态信息。
     * 
     * 查询场景：
     * - 定时任务同步支付状态
     * - 用户主动查询支付结果
     * - 支付超时后的状态确认
     * - 异常情况下的状态核实
     * 
     * @param paymentOrder 支付订单信息，包含第三方订单号等查询必需信息
     * @return 第三方平台返回的支付状态枚举值
     * @throws IllegalArgumentException 当支付订单信息不足以进行查询时抛出
     * @throws RuntimeException 当调用第三方查询接口失败时抛出
     * @since 2025-11-01
     */
    PaymentStatus queryPaymentStatus(PaymentOrder paymentOrder);

    /**
     * 取消支付
     * 调用第三方支付平台接口取消支付订单
     * 
     * @param paymentOrder 支付订单信息
     * @return 取消结果，true表示取消成功，false表示取消失败
     * @throws RuntimeException 当调用第三方接口失败时抛出
     */
    boolean cancelPayment(PaymentOrder paymentOrder);

    /**
     * 验证支付回调签名
     * 验证第三方支付平台回调请求的签名是否有效
     * 
     * @param paymentOrder 支付订单信息
     * @param callbackData 回调数据
     * @param signature 签名
     * @return 验证结果，true表示签名有效，false表示签名无效
     */
    boolean verifyCallbackSignature(PaymentOrder paymentOrder, String callbackData, String signature);

    /**
     * 解析支付回调数据
     * 解析第三方支付平台的回调数据，提取关键信息
     * 
     * @param paymentOrder 支付订单信息
     * @param callbackData 回调数据
     * @return 解析后的回调信息
     */
    PaymentCallbackInfo parseCallbackData(PaymentOrder paymentOrder, String callbackData);

    /**
     * 处理支付请求
     * 
     * @param paymentMethod 支付方式
     * @param amount 支付金额
     * @param orderId 订单ID
     * @param returnUrl 返回URL
     * @param notifyUrl 通知URL
     * @return 支付结果
     */
    com.mall.payment.dto.PaymentResult processPayment(PaymentMethod paymentMethod, BigDecimal amount, 
                                                     String orderId, String returnUrl, String notifyUrl);

    /**
     * 验证支付回调
     * 验证第三方支付平台回调请求的有效性
     * 
     * @param paymentMethod 支付方式
     * @param params 回调参数
     * @return 验证结果，true表示验证通过，false表示验证失败
     */
    boolean verifyPaymentCallback(PaymentMethod paymentMethod, Map<String, String> params);

    /**
     * 解析支付回调数据
     * 解析第三方支付平台的回调数据，提取关键信息
     * 
     * @param paymentMethod 支付方式
     * @param params 回调参数
     * @return 解析后的回调信息
     */
    PaymentCallbackInfo parsePaymentCallback(PaymentMethod paymentMethod, Map<String, String> params);

    /**
     * 查询支付状态
     * 主动查询第三方支付平台的支付状态
     * 
     * @param paymentMethod 支付方式
     * @param thirdPartyOrderNo 第三方订单号
     * @return 支付查询结果
     */
    PaymentQueryResult queryPaymentStatus(PaymentMethod paymentMethod, String thirdPartyOrderNo);

    /**
     * 支付查询结果内部类
     */
    class PaymentQueryResult {
        private PaymentStatus status;           // 支付状态
        private java.math.BigDecimal actualAmount;    // 实际支付金额
        private String failureReason;           // 失败原因
        private String rawData;                 // 原始响应数据

        // 构造函数
        public PaymentQueryResult() {}

        public PaymentQueryResult(PaymentStatus status, java.math.BigDecimal actualAmount, 
                                String failureReason, String rawData) {
            this.status = status;
            this.actualAmount = actualAmount;
            this.failureReason = failureReason;
            this.rawData = rawData;
        }

        // Getter和Setter方法
        public PaymentStatus getStatus() { return status; }
        public void setStatus(PaymentStatus status) { this.status = status; }

        public java.math.BigDecimal getActualAmount() { return actualAmount; }
        public void setActualAmount(java.math.BigDecimal actualAmount) { this.actualAmount = actualAmount; }

        public String getFailureReason() { return failureReason; }
        public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

        public String getRawData() { return rawData; }
        public void setRawData(String rawData) { this.rawData = rawData; }

        @Override
        public String toString() {
            return "PaymentQueryResult{" +
                    "status=" + status +
                    ", actualAmount=" + actualAmount +
                    ", failureReason='" + failureReason + '\'' +
                    ", rawData='" + rawData + '\'' +
                    '}';
        }
    }
}