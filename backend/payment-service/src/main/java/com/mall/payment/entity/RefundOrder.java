package com.mall.payment.entity;

import com.mall.payment.enums.RefundStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 退款订单实体类
 * 存储退款申请的基本信息，包括退款金额、退款原因、审核状态等
 * 
 * <p>数据表结构：</p>
 * <ul>
 *   <li>表名：refund_orders</li>
 *   <li>主键：id（UUID）</li>
 *   <li>索引：payment_order_id、refund_no、status、created_at</li>
 * </ul>
 * 
 * <p>业务关系：</p>
 * <ul>
 *   <li>多个退款订单对应一个支付订单（支持部分退款）</li>
 *   <li>一个退款订单可以有多条退款记录（重试场景）</li>
 *   <li>退款订单与支付订单是多对一关系</li>
 * </ul>
 * 
 * <p>退款流程：</p>
 * <ul>
 *   <li>1. 用户申请退款（PENDING）</li>
 *   <li>2. 系统或人工审核（REVIEWING → APPROVED/REJECTED）</li>
 *   <li>3. 处理退款（PROCESSING）</li>
 *   <li>4. 退款完成（SUCCESS）或失败（FAILED）</li>
 * </ul>
 * 
 * <p>退款类型：</p>
 * <ul>
 *   <li>用户申请（USER_APPLIED）：需要审核流程</li>
 *   <li>系统自动（SYSTEM_AUTO）：无需审核</li>
 *   <li>客服处理（CUSTOMER_SERVICE）：人工处理</li>
 *   <li>风控退款（RISK_CONTROL）：优先处理</li>
 * </ul>
 * 
 * <p>审计功能：</p>
 * <ul>
 *   <li>自动记录创建时间（created_at）</li>
 *   <li>自动记录更新时间（updated_at）</li>
 *   <li>记录审核人、处理人等操作轨迹</li>
 * </ul>
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * 修改日志：
 * V1.2 2025-11-01：完善Javadoc注释，增加数据表结构和退款流程说明
 * V1.1 2024-12-18：增加人工审核和异常处理流程
 * V1.0 2024-12-01：初始版本，定义基本退款订单结构
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "refund_orders", indexes = {
    @Index(name = "idx_payment_order_id", columnList = "paymentOrderId"),
    @Index(name = "idx_refund_no", columnList = "refundNo"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@EntityListeners(AuditingEntityListener.class)
public class RefundOrder {

    /**
     * 退款订单ID - 主键，使用UUID生成
     */
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", length = 36)
    private String id;

    /**
     * 退款单号 - 业务退款单号，用于对外展示
     */
    @Column(name = "refund_no", nullable = false, unique = true, length = 64)
    private String refundNo;

    /**
     * 支付订单ID - 关联的支付订单
     */
    @Column(name = "payment_order_id", nullable = false, length = 36)
    private String paymentOrderId;

    /**
     * 支付订单对象 - 多对一关系
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_order_id", insertable = false, updatable = false)
    private PaymentOrder paymentOrder;

    /**
     * 用户ID - 申请退款的用户
     */
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    /**
     * 退款金额 - 申请退款的金额
     */
    @Column(name = "refund_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal refundAmount;

    /**
     * 退款原因 - 用户填写的退款原因
     */
    @Column(name = "refund_reason", nullable = false, length = 500)
    private String refundReason;

    /**
     * 退款类型 - 1:用户申请 2:系统自动 3:客服处理
     */
    @Column(name = "refund_type", nullable = false)
    private Integer refundType = 1;

    /**
     * 退款状态 - 使用枚举类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RefundStatus status = RefundStatus.PENDING_REVIEW;

    /**
     * 审核人ID - 审核退款申请的管理员ID
     */
    @Column(name = "reviewer_id", length = 36)
    private String reviewerId;

    /**
     * 审核时间 - 审核完成的时间
     */
    @Column(name = "review_time")
    private LocalDateTime reviewTime;

    /**
     * 审核备注 - 审核时的备注信息
     */
    @Column(name = "review_remark", length = 500)
    private String reviewRemark;

    /**
     * 处理人ID - 实际处理退款的操作员ID
     */
    @Column(name = "processor_id", length = 36)
    private String processorId;

    /**
     * 处理时间 - 开始处理退款的时间
     */
    @Column(name = "process_time")
    private LocalDateTime processTime;

    /**
     * 退款完成时间 - 退款成功的时间
     */
    @Column(name = "refund_time")
    private LocalDateTime refundTime;

    /**
     * 实际退款金额 - 最终实际退款的金额（可能扣除手续费等）
     */
    @Column(name = "actual_refund_amount", precision = 15, scale = 2)
    private BigDecimal actualRefundAmount;

    /**
     * 退款手续费 - 退款产生的手续费
     */
    @Column(name = "refund_fee", precision = 15, scale = 2)
    private BigDecimal refundFee;

    /**
     * 第三方退款单号 - 支付渠道返回的退款单号
     */
    @Column(name = "third_party_refund_no", length = 64)
    private String thirdPartyRefundNo;

    /**
     * 退款渠道响应数据 - JSON格式存储第三方返回的原始数据
     */
    @Column(name = "channel_response", columnDefinition = "TEXT")
    private String channelResponse;

    /**
     * 失败原因 - 退款失败时的错误信息
     */
    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    /**
     * 重试次数 - 退款失败后的重试次数
     */
    @Column(name = "retry_count")
    private Integer retryCount = 0;

    /**
     * 预计到账时间 - 退款预计到账的时间
     */
    @Column(name = "expected_arrival_time")
    private LocalDateTime expectedArrivalTime;

    /**
     * 退款凭证 - 退款凭证文件路径或URL
     */
    @Column(name = "refund_voucher", length = 500)
    private String refundVoucher;

    /**
     * 备注信息 - 额外的备注说明
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 创建时间 - 自动设置
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间 - 自动更新
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 退款记录列表 - 一对多关系
     */
    @OneToMany(mappedBy = "refundOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RefundRecord> refundRecords = new ArrayList<>();

    /**
     * 判断退款订单是否成功
     * 
     * @return 如果退款成功返回true，否则返回false
     */
    public boolean isSuccess() {
        return status.isSuccess();
    }

    /**
     * 判断退款订单是否失败
     * 
     * @return 如果退款失败返回true，否则返回false
     */
    public boolean isFailed() {
        return status == RefundStatus.FAILED;
    }

    /**
     * 判断是否可以取消
     * 只有待审核和审核中状态的退款订单才可以取消
     * 
     * @return 如果可以取消返回true，否则返回false
     */
    public boolean canCancel() {
        return status.canCancel();
    }

    /**
     * 判断是否需要人工处理
     * 
     * @return 如果需要人工处理返回true，否则返回false
     */
    public boolean needsManualProcess() {
        return status.needManualProcess();
    }

    /**
     * 判断是否处于终态
     * 
     * @return 如果处于终态返回true，否则返回false
     */
    public boolean isFinalStatus() {
        return status.isFinalStatus();
    }

    /**
     * 更新退款状态
     * 同时更新相关的时间字段
     * 
     * @param newStatus 新的退款状态
     */
    public void updateStatus(RefundStatus newStatus) {
        this.status = newStatus;
        
        switch (newStatus) {
            case REVIEWING:
                if (this.reviewTime == null) {
                    this.reviewTime = LocalDateTime.now();
                }
                break;
            case PROCESSING:
                if (this.processTime == null) {
                    this.processTime = LocalDateTime.now();
                }
                break;
            case SUCCESS:
                if (this.refundTime == null) {
                    this.refundTime = LocalDateTime.now();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 审核通过
     * 
     * @param reviewerId 审核人ID
     * @param reviewRemark 审核备注
     */
    public void approve(String reviewerId, String reviewRemark) {
        this.reviewerId = reviewerId;
        this.reviewRemark = reviewRemark;
        this.reviewTime = LocalDateTime.now();
        this.status = RefundStatus.APPROVED;
    }

    /**
     * 审核拒绝
     * 
     * @param reviewerId 审核人ID
     * @param reviewRemark 审核备注
     */
    public void reject(String reviewerId, String reviewRemark) {
        this.reviewerId = reviewerId;
        this.reviewRemark = reviewRemark;
        this.reviewTime = LocalDateTime.now();
        this.status = RefundStatus.REJECTED;
    }

    /**
     * 开始处理退款
     * 
     * @param processorId 处理人ID
     */
    public void startProcess(String processorId) {
        this.processorId = processorId;
        this.processTime = LocalDateTime.now();
        this.status = RefundStatus.PROCESSING;
    }

    /**
     * 退款成功
     * 
     * @param actualAmount 实际退款金额
     * @param thirdPartyRefundNo 第三方退款单号
     */
    public void success(BigDecimal actualAmount, String thirdPartyRefundNo) {
        this.actualRefundAmount = actualAmount;
        this.thirdPartyRefundNo = thirdPartyRefundNo;
        this.refundTime = LocalDateTime.now();
        this.status = RefundStatus.SUCCESS;
    }

    /**
     * 退款失败
     * 
     * @param failureReason 失败原因
     */
    public void fail(String failureReason) {
        this.failureReason = failureReason;
        this.status = RefundStatus.FAILED;
    }

    /**
     * 增加重试次数
     */
    public void incrementRetryCount() {
        this.retryCount = (this.retryCount == null ? 0 : this.retryCount) + 1;
    }

    /**
     * 计算退款处理耗时（小时）
     * 从申请到完成的时间差
     * 
     * @return 处理耗时，如果未完成返回null
     */
    public Long getProcessDurationHours() {
        if (refundTime == null || createdAt == null) {
            return null;
        }
        return java.time.Duration.between(createdAt, refundTime).toHours();
    }

    // Additional getter and setter methods for Lombok compatibility
    public RefundStatus getStatus() {
        return status;
    }

    public void setStatus(RefundStatus status) {
        this.status = status;
    }

    public LocalDateTime getProcessTime() {
        return processTime;
    }

    public void setProcessTime(LocalDateTime processTime) {
        this.processTime = processTime;
    }

    public PaymentOrder getPaymentOrder() {
        return paymentOrder;
    }

    public void setPaymentOrder(PaymentOrder paymentOrder) {
        this.paymentOrder = paymentOrder;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getRefundReason() {
        return refundReason;
    }

    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }

    public String getRefundNo() {
        return refundNo;
    }

    public void setRefundNo(String refundNo) {
        this.refundNo = refundNo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getThirdPartyRefundNo() {
        return thirdPartyRefundNo;
    }

    public void setThirdPartyRefundNo(String thirdPartyRefundNo) {
        this.thirdPartyRefundNo = thirdPartyRefundNo;
    }

    public BigDecimal getActualRefundAmount() {
        return actualRefundAmount;
    }

    public void setActualRefundAmount(BigDecimal actualRefundAmount) {
        this.actualRefundAmount = actualRefundAmount;
    }

    public LocalDateTime getRefundTime() {
        return refundTime;
    }

    public void setRefundTime(LocalDateTime refundTime) {
        this.refundTime = refundTime;
    }

    public String getChannelResponse() {
        return channelResponse;
    }

    public void setChannelResponse(String channelResponse) {
        this.channelResponse = channelResponse;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public LocalDateTime getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(LocalDateTime reviewTime) {
        this.reviewTime = reviewTime;
    }

    public String getReviewRemark() {
        return reviewRemark;
    }

    public void setReviewRemark(String reviewRemark) {
        this.reviewRemark = reviewRemark;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPaymentOrderId() {
        return paymentOrderId;
    }

    public void setPaymentOrderId(String paymentOrderId) {
        this.paymentOrderId = paymentOrderId;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getRefundType() {
        return refundType;
    }

    public void setRefundType(Integer refundType) {
        this.refundType = refundType;
    }
}