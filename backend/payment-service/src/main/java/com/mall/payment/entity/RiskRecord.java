package com.mall.payment.entity;

import com.mall.payment.enums.PaymentMethod;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 风控记录实体类
 * 记录支付风控检查的结果和处理过程
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@Entity
@Table(name = "risk_records")
public class RiskRecord {

    /**
     * 记录ID
     */
    @Id
    @Column(name = "record_id", length = 64)
    private String recordId;

    /**
     * 支付订单ID
     */
    @Column(name = "payment_order_id", length = 64, nullable = false)
    private String paymentOrderId;

    /**
     * 业务订单ID
     */
    @Column(name = "business_order_id", length = 64)
    private String businessOrderId;

    /**
     * 用户ID
     */
    @Column(name = "user_id", length = 64, nullable = false)
    private String userId;

    /**
     * 支付方式
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20, nullable = false)
    private PaymentMethod paymentMethod;

    /**
     * 支付金额
     */
    @Column(name = "payment_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal paymentAmount;

    /**
     * 客户端IP地址
     */
    @Column(name = "client_ip", length = 45)
    private String clientIp;

    /**
     * 用户代理
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 设备指纹
     */
    @Column(name = "device_fingerprint", length = 128)
    private String deviceFingerprint;

    /**
     * 触发的规则ID列表（逗号分隔）
     */
    @Column(name = "triggered_rules", length = 1000)
    private String triggeredRules;

    /**
     * 风险评分
     */
    @Column(name = "risk_score", precision = 5, scale = 2)
    private BigDecimal riskScore;

    /**
     * 风险等级
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 10, nullable = false)
    private RiskRule.RiskLevel riskLevel;

    /**
     * 处理结果
     * PASSED: 通过
     * BLOCKED: 阻止
     * MANUAL_REVIEW: 人工审核
     * WARNING: 警告通过
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 20, nullable = false)
    private RiskResult result;

    /**
     * 处理动作
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 20, nullable = false)
    private RiskRule.RiskAction action;

    /**
     * 风控原因
     */
    @Column(name = "reason", length = 1000)
    private String reason;

    /**
     * 详细信息（JSON格式）
     */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    /**
     * 处理时间（毫秒）
     */
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    /**
     * 是否误报
     */
    @Column(name = "is_false_positive")
    private Boolean isFalsePositive = false;

    /**
     * 审核状态（仅当result为MANUAL_REVIEW时有效）
     * PENDING: 待审核
     * APPROVED: 审核通过
     * REJECTED: 审核拒绝
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", length = 20)
    private ReviewStatus reviewStatus;

    /**
     * 审核人
     */
    @Column(name = "reviewer", length = 64)
    private String reviewer;

    /**
     * 审核时间
     */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    /**
     * 审核备注
     */
    @Column(name = "review_comment", length = 500)
    private String reviewComment;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== 构造函数 ====================

    public RiskRecord() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== JPA生命周期回调 ====================

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== 业务方法 ====================

    /**
     * 检查是否需要人工审核
     * 
     * @return 是否需要人工审核
     */
    public boolean requiresManualReview() {
        return result == RiskResult.MANUAL_REVIEW;
    }

    /**
     * 检查是否已完成审核
     * 
     * @return 是否已完成审核
     */
    public boolean isReviewCompleted() {
        return reviewStatus != null && reviewStatus != ReviewStatus.PENDING;
    }

    /**
     * 检查审核是否通过
     * 
     * @return 审核是否通过
     */
    public boolean isReviewApproved() {
        return reviewStatus == ReviewStatus.APPROVED;
    }

    /**
     * 检查是否为高风险交易
     * 
     * @return 是否为高风险交易
     */
    public boolean isHighRisk() {
        return riskLevel == RiskRule.RiskLevel.HIGH || riskLevel == RiskRule.RiskLevel.CRITICAL;
    }

    /**
     * 标记为误报
     * 
     * @param reviewer 标记人
     * @param comment 备注
     */
    public void markAsFalsePositive(String reviewer, String comment) {
        this.isFalsePositive = true;
        this.reviewer = reviewer;
        this.reviewComment = comment;
        this.reviewedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 完成人工审核
     * 
     * @param status 审核状态
     * @param reviewer 审核人
     * @param comment 审核备注
     */
    public void completeReview(ReviewStatus status, String reviewer, String comment) {
        this.reviewStatus = status;
        this.reviewer = reviewer;
        this.reviewComment = comment;
        this.reviewedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== Getter和Setter方法 ====================

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getPaymentOrderId() {
        return paymentOrderId;
    }

    public void setPaymentOrderId(String paymentOrderId) {
        this.paymentOrderId = paymentOrderId;
    }

    public String getBusinessOrderId() {
        return businessOrderId;
    }

    public void setBusinessOrderId(String businessOrderId) {
        this.businessOrderId = businessOrderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }

    public void setDeviceFingerprint(String deviceFingerprint) {
        this.deviceFingerprint = deviceFingerprint;
    }

    public String getTriggeredRules() {
        return triggeredRules;
    }

    public void setTriggeredRules(String triggeredRules) {
        this.triggeredRules = triggeredRules;
    }

    public BigDecimal getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(BigDecimal riskScore) {
        this.riskScore = riskScore;
    }

    public RiskRule.RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskRule.RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public RiskResult getResult() {
        return result;
    }

    public void setResult(RiskResult result) {
        this.result = result;
    }

    public RiskRule.RiskAction getAction() {
        return action;
    }

    public void setAction(RiskRule.RiskAction action) {
        this.action = action;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public Boolean getIsFalsePositive() {
        return isFalsePositive;
    }

    public void setIsFalsePositive(Boolean isFalsePositive) {
        this.isFalsePositive = isFalsePositive;
    }

    public ReviewStatus getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(ReviewStatus reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ==================== 枚举定义 ====================

    /**
     * 风控结果枚举
     */
    public enum RiskResult {
        /**
         * 通过
         */
        PASSED,
        
        /**
         * 阻止
         */
        BLOCKED,
        
        /**
         * 人工审核
         */
        MANUAL_REVIEW,
        
        /**
         * 警告通过
         */
        WARNING
    }

    /**
     * 审核状态枚举
     */
    public enum ReviewStatus {
        /**
         * 待审核
         */
        PENDING,
        
        /**
         * 审核通过
         */
        APPROVED,
        
        /**
         * 审核拒绝
         */
        REJECTED
    }
}