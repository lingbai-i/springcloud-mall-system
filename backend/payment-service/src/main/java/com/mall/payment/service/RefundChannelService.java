package com.mall.payment.service;

import com.mall.payment.enums.PaymentMethod;
import com.mall.payment.enums.RefundStatus;

import java.math.BigDecimal;

/**
 * 退款渠道服务接口
 * 定义与第三方支付平台退款相关的接口方法，用于处理不同支付渠道的退款操作
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
public interface RefundChannelService {

    /**
     * 处理退款申请
     * 调用第三方支付平台的退款接口，发起退款流程
     * 
     * @param paymentMethod 支付方式
     * @param thirdPartyOrderNo 第三方支付订单号
     * @param refundOrderId 退款订单ID
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 退款处理结果
     * @throws IllegalArgumentException 当参数无效时抛出
     * @throws RuntimeException 当调用第三方接口失败时抛出
     */
    RefundResult processRefund(PaymentMethod paymentMethod, String thirdPartyOrderNo, 
                              String refundOrderId, BigDecimal refundAmount, String refundReason);



    /**
     * 验证退款回调签名
     * 验证第三方支付平台退款回调的签名是否有效
     * 
     * @param paymentMethod 支付方式
     * @param callbackData 回调数据
     * @param signature 签名
     * @return 签名验证结果
     */
    boolean verifyRefundCallback(PaymentMethod paymentMethod, String callbackData, String signature);

    /**
     * 解析退款回调数据
     * 解析第三方支付平台的退款回调数据，提取关键信息
     * 
     * @param paymentMethod 支付方式
     * @param callbackData 回调数据
     * @return 解析后的回调信息
     * @throws IllegalArgumentException 当回调数据格式无效时抛出
     */
    RefundCallbackInfo parseRefundCallback(PaymentMethod paymentMethod, String callbackData);

    /**
     * 取消退款申请
     * 调用第三方支付平台接口取消退款申请（如果支持）
     * 
     * @param paymentMethod 支付方式
     * @param thirdPartyRefundNo 第三方退款单号
     * @param cancelReason 取消原因
     * @return 取消结果
     */
    boolean cancelRefund(PaymentMethod paymentMethod, String thirdPartyRefundNo, String cancelReason);

    /**
     * 获取退款手续费
     * 计算指定支付方式和金额的退款手续费
     * 
     * @param paymentMethod 支付方式
     * @param refundAmount 退款金额
     * @return 退款手续费
     */
    BigDecimal getRefundFee(PaymentMethod paymentMethod, BigDecimal refundAmount);

    /**
     * 检查是否支持退款
     * 检查指定支付方式是否支持退款操作
     * 
     * @param paymentMethod 支付方式
     * @return 是否支持退款
     */
    boolean supportsRefund(PaymentMethod paymentMethod);

    /**
     * 退款处理结果内部类
     * 封装第三方支付平台退款处理的结果信息
     */
    class RefundResult {
        private boolean success;                    // 是否成功
        private String thirdPartyRefundNo;         // 第三方退款单号
        private BigDecimal actualRefundAmount;     // 实际退款金额
        private String failureReason;              // 失败原因
        private String channelResponse;            // 第三方平台响应数据

        // 构造函数
        public RefundResult(boolean success, String thirdPartyRefundNo, BigDecimal actualRefundAmount, 
                           String failureReason, String channelResponse) {
            this.success = success;
            this.thirdPartyRefundNo = thirdPartyRefundNo;
            this.actualRefundAmount = actualRefundAmount;
            this.failureReason = failureReason;
            this.channelResponse = channelResponse;
        }

        // 成功结果的静态工厂方法
        public static RefundResult success(String thirdPartyRefundNo, BigDecimal actualRefundAmount, String channelResponse) {
            return new RefundResult(true, thirdPartyRefundNo, actualRefundAmount, null, channelResponse);
        }

        // 失败结果的静态工厂方法
        public static RefundResult failure(String failureReason, String channelResponse) {
            return new RefundResult(false, null, null, failureReason, channelResponse);
        }

        // Getter方法
        public boolean isSuccess() { return success; }
        public String getThirdPartyRefundNo() { return thirdPartyRefundNo; }
        public BigDecimal getActualRefundAmount() { return actualRefundAmount; }
        public String getFailureReason() { return failureReason; }
        public String getChannelResponse() { return channelResponse; }

        // Setter方法
        public void setSuccess(boolean success) { this.success = success; }
        public void setThirdPartyRefundNo(String thirdPartyRefundNo) { this.thirdPartyRefundNo = thirdPartyRefundNo; }
        public void setActualRefundAmount(BigDecimal actualRefundAmount) { this.actualRefundAmount = actualRefundAmount; }
        public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
        public void setChannelResponse(String channelResponse) { this.channelResponse = channelResponse; }
    }

    /**
     * 退款回调信息内部类
     * 封装第三方支付平台退款回调的关键信息
     */
    class RefundCallbackInfo {
        private String refundOrderId;              // 退款订单ID
        private String thirdPartyRefundNo;         // 第三方退款单号
        private RefundStatus refundStatus;         // 退款状态
        private BigDecimal actualRefundAmount;     // 实际退款金额
        private String failureReason;              // 失败原因
        private String originalData;               // 原始回调数据

        // 构造函数
        public RefundCallbackInfo(String refundOrderId, String thirdPartyRefundNo, RefundStatus refundStatus,
                                 BigDecimal actualRefundAmount, String failureReason, String originalData) {
            this.refundOrderId = refundOrderId;
            this.thirdPartyRefundNo = thirdPartyRefundNo;
            this.refundStatus = refundStatus;
            this.actualRefundAmount = actualRefundAmount;
            this.failureReason = failureReason;
            this.originalData = originalData;
        }

        // Getter方法
        public String getRefundOrderId() { return refundOrderId; }
        public String getThirdPartyRefundNo() { return thirdPartyRefundNo; }
        public RefundStatus getRefundStatus() { return refundStatus; }
        public BigDecimal getActualRefundAmount() { return actualRefundAmount; }
        public String getFailureReason() { return failureReason; }
        public String getOriginalData() { return originalData; }

        // Setter方法
        public void setRefundOrderId(String refundOrderId) { this.refundOrderId = refundOrderId; }
        public void setThirdPartyRefundNo(String thirdPartyRefundNo) { this.thirdPartyRefundNo = thirdPartyRefundNo; }
        public void setRefundStatus(RefundStatus refundStatus) { this.refundStatus = refundStatus; }
        public void setActualRefundAmount(BigDecimal actualRefundAmount) { this.actualRefundAmount = actualRefundAmount; }
        public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
        public void setOriginalData(String originalData) { this.originalData = originalData; }
    }

    /**
     * 查询退款状态
     * 主动查询第三方支付平台的退款状态
     * 
     * @param paymentMethod 支付方式
     * @param thirdPartyRefundNo 第三方退款单号
     * @return 退款查询结果
     */
    RefundQueryResult queryRefundStatus(PaymentMethod paymentMethod, String thirdPartyRefundNo);

    /**
     * 退款查询结果内部类
     */
    class RefundQueryResult {
        private RefundStatus status;                // 退款状态
        private BigDecimal actualRefundAmount;      // 实际退款金额
        private String failureReason;               // 失败原因
        private String rawData;                     // 原始响应数据

        // 构造函数
        public RefundQueryResult() {}

        public RefundQueryResult(RefundStatus status, BigDecimal actualRefundAmount, 
                               String failureReason, String rawData) {
            this.status = status;
            this.actualRefundAmount = actualRefundAmount;
            this.failureReason = failureReason;
            this.rawData = rawData;
        }

        // Getter和Setter方法
        public RefundStatus getStatus() { return status; }
        public void setStatus(RefundStatus status) { this.status = status; }

        public BigDecimal getActualRefundAmount() { return actualRefundAmount; }
        public void setActualRefundAmount(BigDecimal actualRefundAmount) { this.actualRefundAmount = actualRefundAmount; }

        public String getFailureReason() { return failureReason; }
        public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

        public String getRawData() { return rawData; }
        public void setRawData(String rawData) { this.rawData = rawData; }

        @Override
        public String toString() {
            return "RefundQueryResult{" +
                    "status=" + status +
                    ", actualRefundAmount=" + actualRefundAmount +
                    ", failureReason='" + failureReason + '\'' +
                    ", rawData='" + rawData + '\'' +
                    '}';
        }
    }
}