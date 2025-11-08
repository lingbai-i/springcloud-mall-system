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

/**
 * 退款记录实体类
 * 记录每次退款操作的详细信息，包括退款请求、响应、状态变更等
 * 一个退款订单可能有多条退款记录（重试、部分退款等场景）
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "refund_records", indexes = {
    @Index(name = "idx_refund_order_id", columnList = "refundOrderId"),
    @Index(name = "idx_third_party_refund_no", columnList = "thirdPartyRefundNo"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@EntityListeners(AuditingEntityListener.class)
public class RefundRecord {

    /**
     * 退款记录ID - 主键，使用UUID生成
     */
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", length = 36)
    private String id;

    /**
     * 退款订单ID - 关联的退款订单
     */
    @Column(name = "refund_order_id", nullable = false, length = 36)
    private String refundOrderId;

    /**
     * 退款订单对象 - 多对一关系
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refund_order_id", insertable = false, updatable = false)
    private RefundOrder refundOrder;

    /**
     * 退款金额 - 本次退款操作的金额
     */
    @Column(name = "refund_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal refundAmount;

    /**
     * 退款状态 - 本次退款操作的状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RefundStatus status = RefundStatus.PROCESSING;

    /**
     * 第三方退款单号 - 支付渠道返回的退款流水号
     */
    @Column(name = "third_party_refund_no", length = 64)
    private String thirdPartyRefundNo;

    /**
     * 退款渠道 - 具体的退款渠道标识
     */
    @Column(name = "refund_channel", length = 50)
    private String refundChannel;

    /**
     * 退款请求参数 - JSON格式存储发送给第三方的请求参数
     */
    @Column(name = "request_params", columnDefinition = "TEXT")
    private String requestParams;

    /**
     * 退款响应数据 - JSON格式存储第三方返回的响应数据
     */
    @Column(name = "response_data", columnDefinition = "TEXT")
    private String responseData;

    /**
     * 退款完成时间 - 第三方确认退款成功的时间
     */
    @Column(name = "refund_time")
    private LocalDateTime refundTime;

    /**
     * 实际退款金额 - 实际到账的退款金额
     */
    @Column(name = "actual_refund_amount", precision = 15, scale = 2)
    private BigDecimal actualRefundAmount;

    /**
     * 退款手续费 - 本次退款产生的手续费
     */
    @Column(name = "refund_fee", precision = 15, scale = 2)
    private BigDecimal refundFee;

    /**
     * 错误代码 - 退款失败时的错误代码
     */
    @Column(name = "error_code", length = 50)
    private String errorCode;

    /**
     * 错误信息 - 退款失败时的详细错误信息
     */
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    /**
     * 重试次数 - 当前记录的重试次数
     */
    @Column(name = "retry_count")
    private Integer retryCount = 0;

    /**
     * 操作人ID - 执行退款操作的用户ID（系统或管理员）
     */
    @Column(name = "operator_id", length = 36)
    private String operatorId;

    /**
     * 操作人类型 - 1:系统自动 2:管理员手动 3:用户申请
     */
    @Column(name = "operator_type")
    private Integer operatorType = 1;

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
     * 判断退款记录是否成功
     * 
     * @return 如果退款成功返回true，否则返回false
     */
    public boolean isSuccess() {
        return status == RefundStatus.SUCCESS;
    }

    /**
     * 判断退款记录是否失败
     * 
     * @return 如果退款失败返回true，否则返回false
     */
    public boolean isFailed() {
        return status == RefundStatus.FAILED;
    }

    /**
     * 判断退款记录是否处于处理中状态
     * 
     * @return 如果正在处理返回true，否则返回false
     */
    public boolean isProcessing() {
        return status == RefundStatus.PROCESSING;
    }

    /**
     * 判断是否可以重试
     * 只有失败状态且重试次数未超限的记录才可以重试
     * 
     * @param maxRetryCount 最大重试次数
     * @return 如果可以重试返回true，否则返回false
     */
    public boolean canRetry(int maxRetryCount) {
        return isFailed() && (retryCount == null || retryCount < maxRetryCount);
    }

    /**
     * 更新退款状态
     * 同时更新相关的时间字段
     * 
     * @param newStatus 新的退款状态
     */
    public void updateStatus(RefundStatus newStatus) {
        this.status = newStatus;
        if (newStatus == RefundStatus.SUCCESS && this.refundTime == null) {
            this.refundTime = LocalDateTime.now();
        }
    }

    /**
     * 设置退款失败信息
     * 
     * @param errorCode 错误代码
     * @param errorMessage 错误信息
     */
    public void setFailureInfo(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.status = RefundStatus.FAILED;
    }

    /**
     * 设置退款成功信息
     * 
     * @param actualAmount 实际退款金额
     * @param thirdPartyRefundNo 第三方退款单号
     */
    public void setSuccessInfo(BigDecimal actualAmount, String thirdPartyRefundNo) {
        this.actualRefundAmount = actualAmount;
        this.thirdPartyRefundNo = thirdPartyRefundNo;
        this.refundTime = LocalDateTime.now();
        this.status = RefundStatus.SUCCESS;
    }

    /**
     * 增加重试次数
     */
    public void incrementRetryCount() {
        this.retryCount = (this.retryCount == null ? 0 : this.retryCount) + 1;
    }

    /**
     * 计算退款耗时（毫秒）
     * 从创建到退款完成的时间差
     * 
     * @return 退款耗时，如果未完成退款返回null
     */
    public Long getRefundDuration() {
        if (refundTime == null || createdAt == null) {
            return null;
        }
        return java.time.Duration.between(createdAt, refundTime).toMillis();
    }

    /**
     * 判断是否为系统自动退款
     * 
     * @return 如果是系统自动退款返回true，否则返回false
     */
    public boolean isSystemAutoRefund() {
        return operatorType != null && operatorType == 1;
    }

    /**
     * 判断是否为管理员手动退款
     * 
     * @return 如果是管理员手动退款返回true，否则返回false
     */
    public boolean isManualRefund() {
        return operatorType != null && operatorType == 2;
    }

    /**
     * 判断是否为用户申请退款
     * 
     * @return 如果是用户申请退款返回true，否则返回false
     */
    public boolean isUserAppliedRefund() {
        return operatorType != null && operatorType == 3;
    }

    // Additional getter and setter methods for Lombok compatibility
    public String getRefundOrderId() {
        return refundOrderId;
    }

    public void setRefundOrderId(String refundOrderId) {
        this.refundOrderId = refundOrderId;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public void setStatus(RefundStatus status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getThirdPartyRefundNo() {
        return thirdPartyRefundNo;
    }

    public void setThirdPartyRefundNo(String thirdPartyRefundNo) {
        this.thirdPartyRefundNo = thirdPartyRefundNo;
    }

    public String getRefundChannel() {
        return refundChannel;
    }

    public void setRefundChannel(String refundChannel) {
        this.refundChannel = refundChannel;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public LocalDateTime getRefundTime() {
        return refundTime;
    }

    public void setRefundTime(LocalDateTime refundTime) {
        this.refundTime = refundTime;
    }

    public BigDecimal getActualRefundAmount() {
        return actualRefundAmount;
    }

    public void setActualRefundAmount(BigDecimal actualRefundAmount) {
        this.actualRefundAmount = actualRefundAmount;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public Integer getOperatorType() {
        return operatorType;
    }

    public void setOperatorType(Integer operatorType) {
        this.operatorType = operatorType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}