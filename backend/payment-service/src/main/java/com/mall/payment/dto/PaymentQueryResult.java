package com.mall.payment.dto;

import com.mall.payment.enums.PaymentMethod;
import com.mall.payment.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付查询结果DTO
 * 用于封装第三方支付平台的查询结果数据
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
public class PaymentQueryResult {

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
    private PaymentStatus paymentStatus;

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
    private BigDecimal fee;

    /**
     * 支付完成时间
     */
    private LocalDateTime paymentTime;

    /**
     * 查询时间
     */
    private LocalDateTime queryTime;

    /**
     * 查询是否成功
     */
    private Boolean querySuccess;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 买家账号
     */
    private String buyerAccount;

    /**
     * 买家用户ID
     */
    private String buyerUserId;

    /**
     * 默认构造函数
     */
    public PaymentQueryResult() {
        this.queryTime = LocalDateTime.now();
        this.querySuccess = false;
    }

    /**
     * 构造函数
     * 
     * @param paymentOrderId 支付订单ID
     * @param thirdPartyOrderNo 第三方支付订单号
     * @param paymentStatus 支付状态
     */
    public PaymentQueryResult(String paymentOrderId, String thirdPartyOrderNo, PaymentStatus paymentStatus) {
        this();
        this.paymentOrderId = paymentOrderId;
        this.thirdPartyOrderNo = thirdPartyOrderNo;
        this.paymentStatus = paymentStatus;
    }

    /**
     * 判断是否支付成功
     * 
     * @return 是否支付成功
     */
    public boolean isPaymentSuccess() {
        return PaymentStatus.SUCCESS.equals(paymentStatus);
    }

    /**
     * 判断是否支付失败
     * 
     * @return 是否支付失败
     */
    public boolean isPaymentFailed() {
        return PaymentStatus.FAILED.equals(paymentStatus);
    }

    /**
     * 判断是否支付中
     * 
     * @return 是否支付中
     */
    public boolean isPaymentPending() {
        return PaymentStatus.PENDING.equals(paymentStatus);
    }

    /**
     * 判断查询是否成功
     * 
     * @return 查询是否成功
     */
    public boolean isQuerySuccess() {
        return Boolean.TRUE.equals(querySuccess);
    }

    /**
     * 设置查询成功
     */
    public void setQuerySuccess() {
        this.querySuccess = true;
    }

    /**
     * 设置查询失败
     * 
     * @param errorCode 错误代码
     * @param errorMessage 错误信息
     */
    public void setQueryFailed(String errorCode, String errorMessage) {
        this.querySuccess = false;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
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

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
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

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    public LocalDateTime getQueryTime() {
        return queryTime;
    }

    public void setQueryTime(LocalDateTime queryTime) {
        this.queryTime = queryTime;
    }

    public Boolean getQuerySuccess() {
        return querySuccess;
    }

    public void setQuerySuccess(Boolean querySuccess) {
        this.querySuccess = querySuccess;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
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

    @Override
    public String toString() {
        return "PaymentQueryResult{" +
                "paymentOrderId='" + paymentOrderId + '\'' +
                ", thirdPartyOrderNo='" + thirdPartyOrderNo + '\'' +
                ", thirdPartyTransactionNo='" + thirdPartyTransactionNo + '\'' +
                ", paymentMethod=" + paymentMethod +
                ", paymentStatus=" + paymentStatus +
                ", paymentAmount=" + paymentAmount +
                ", actualAmount=" + actualAmount +
                ", fee=" + fee +
                ", paymentTime=" + paymentTime +
                ", queryTime=" + queryTime +
                ", querySuccess=" + querySuccess +
                ", errorMessage='" + errorMessage + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", buyerAccount='" + buyerAccount + '\'' +
                ", buyerUserId='" + buyerUserId + '\'' +
                '}';
    }
}