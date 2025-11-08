package com.mall.payment.dto;

import com.mall.payment.enums.PaymentMethod;
import com.mall.payment.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付回调信息DTO
 * 用于封装第三方支付平台的回调数据
 * 
 * <p>回调数据说明：</p>
 * <ul>
 *   <li>订单信息：支付订单ID、第三方订单号、第三方交易号</li>
 *   <li>支付信息：支付方式、支付状态、支付金额、实际支付金额</li>
 *   <li>费用信息：手续费金额</li>
 *   <li>时间信息：支付完成时间、回调时间</li>
 *   <li>用户信息：买家账号、买家用户ID</li>
 *   <li>异常信息：失败原因、错误代码</li>
 *   <li>验证信息：回调验证结果、原始回调数据</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>接收支付宝支付回调通知</li>
 *   <li>接收微信支付回调通知</li>
 *   <li>接收银行卡支付回调通知</li>
 *   <li>统一处理各种支付平台的回调数据</li>
 * </ul>
 * 
 * <p>处理流程：</p>
 * <ul>
 *   <li>1. 接收第三方平台回调请求</li>
 *   <li>2. 验证回调数据的签名和完整性</li>
 *   <li>3. 解析回调数据并封装为此DTO</li>
 *   <li>4. 更新本地支付订单状态</li>
 *   <li>5. 返回确认响应给第三方平台</li>
 * </ul>
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * 修改日志：
 * V1.2 2025-11-01：完善Javadoc注释，增加回调数据说明和处理流程
 * V1.1 2024-12-12：增加回调验证字段和原始数据保存
 * V1.0 2024-12-01：初始版本，定义基本回调数据结构
 */
public class PaymentCallbackInfo {

    /**
     * 支付订单ID
     */
    private String paymentOrderId;

    /**
     * 第三方支付订单号
     */
    private String thirdPartyOrderNo;

    /**
     * 第三方交易号
     */
    private String thirdPartyTransactionNo;

    /**
     * 支付方式
     */
    private PaymentMethod paymentMethod;

    /**
     * 支付状态
     */
    private PaymentStatus status;

    /**
     * 支付状态（字符串形式，用于第三方平台）
     */
    private String paymentStatus;

    /**
     * 支付金额
     */
    private BigDecimal paymentAmount;

    /**
     * 实际支付金额
     */
    private BigDecimal actualAmount;

    /**
     * 手续费
     */
    private BigDecimal feeAmount;

    /**
     * 支付完成时间
     */
    private LocalDateTime paymentTime;

    /**
     * 回调时间
     */
    private LocalDateTime callbackTime;

    /**
     * 买家账号
     */
    private String buyerAccount;

    /**
     * 买家用户ID
     */
    private String buyerUserId;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 原始回调数据
     */
    private String rawData;

    /**
     * 回调验证是否成功
     */
    private Boolean verified;

    /**
     * 默认构造函数
     */
    public PaymentCallbackInfo() {
        this.callbackTime = LocalDateTime.now();
        this.verified = false;
    }

    /**
     * 构造函数
     * 
     * @param thirdPartyOrderNo 第三方订单号
     * @param status 支付状态
     * @param actualAmount 实际支付金额
     */
    public PaymentCallbackInfo(String thirdPartyOrderNo, PaymentStatus status, BigDecimal actualAmount) {
        this();
        this.thirdPartyOrderNo = thirdPartyOrderNo;
        this.status = status;
        this.actualAmount = actualAmount;
    }

    /**
     * 判断是否支付成功
     * 
     * @return true-支付成功，false-支付未成功
     */
    public boolean isPaymentSuccess() {
        return PaymentStatus.SUCCESS.equals(this.status);
    }

    /**
     * 判断是否支付失败
     * 
     * @return true-支付失败，false-支付未失败
     */
    public boolean isPaymentFailed() {
        return PaymentStatus.FAILED.equals(this.status);
    }

    /**
     * 判断是否支付中
     * 
     * @return true-支付中，false-非支付中状态
     */
    public boolean isPaymentPending() {
        return PaymentStatus.PENDING.equals(this.status);
    }

    /**
     * 判断回调验证是否成功
     * 
     * @return true-验证成功，false-验证失败
     */
    public boolean isVerified() {
        return Boolean.TRUE.equals(this.verified);
    }

    /**
     * 设置验证成功
     */
    public void setVerified() {
        this.verified = true;
    }

    /**
     * 设置验证失败
     * 
     * @param reason 失败原因
     */
    public void setVerifyFailed(String reason) {
        this.verified = false;
        this.failureReason = reason;
    }

    // Getter and Setter methods

    public String getPaymentOrderId() {
        return paymentOrderId;
    }

    public void setPaymentOrderId(String paymentOrderId) {
        this.paymentOrderId = paymentOrderId;
    }

    public String getThirdPartyOrderNo() {
        return thirdPartyOrderNo;
    }

    public void setThirdPartyOrderNo(String thirdPartyOrderNo) {
        this.thirdPartyOrderNo = thirdPartyOrderNo;
    }

    public String getThirdPartyTransactionNo() {
        return thirdPartyTransactionNo;
    }

    public void setThirdPartyTransactionNo(String thirdPartyTransactionNo) {
        this.thirdPartyTransactionNo = thirdPartyTransactionNo;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public BigDecimal getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    public LocalDateTime getCallbackTime() {
        return callbackTime;
    }

    public void setCallbackTime(LocalDateTime callbackTime) {
        this.callbackTime = callbackTime;
    }

    public String getBuyerAccount() {
        return buyerAccount;
    }

    public void setBuyerAccount(String buyerAccount) {
        this.buyerAccount = buyerAccount;
    }

    public String getBuyerUserId() {
        return buyerUserId;
    }

    public void setBuyerUserId(String buyerUserId) {
        this.buyerUserId = buyerUserId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    @Override
    public String toString() {
        return "PaymentCallbackInfo{" +
                "paymentOrderId='" + paymentOrderId + '\'' +
                ", thirdPartyOrderNo='" + thirdPartyOrderNo + '\'' +
                ", thirdPartyTransactionNo='" + thirdPartyTransactionNo + '\'' +
                ", paymentMethod=" + paymentMethod +
                ", status=" + status +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", paymentAmount=" + paymentAmount +
                ", actualAmount=" + actualAmount +
                ", feeAmount=" + feeAmount +
                ", paymentTime=" + paymentTime +
                ", callbackTime=" + callbackTime +
                ", buyerAccount='" + buyerAccount + '\'' +
                ", buyerUserId='" + buyerUserId + '\'' +
                ", failureReason='" + failureReason + '\'' +
                ", verified=" + verified +
                '}';
    }
}