package com.mall.payment.dto.response;

import com.mall.payment.enums.RefundStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 退款订单响应DTO
 * 用于返回退款订单的详细信息给前端或其他服务
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@Data
public class RefundOrderResponse {

    /**
     * 退款订单ID
     */
    private String id;

    /**
     * 退款单号
     */
    private String refundNo;

    /**
     * 支付订单ID
     */
    private String paymentOrderId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 退款类型
     */
    private Integer refundType;

    /**
     * 退款类型描述
     */
    private String refundTypeDesc;

    /**
     * 退款状态
     */
    private RefundStatus status;

    /**
     * 退款状态描述
     */
    private String statusDesc;

    /**
     * 审核人ID
     */
    private String reviewerId;

    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 审核备注
     */
    private String reviewRemark;

    /**
     * 处理人ID
     */
    private String processorId;

    /**
     * 处理时间
     */
    private LocalDateTime processTime;

    /**
     * 退款完成时间
     */
    private LocalDateTime refundTime;

    /**
     * 实际退款金额
     */
    private BigDecimal actualRefundAmount;

    /**
     * 退款手续费
     */
    private BigDecimal refundFee;

    /**
     * 第三方退款单号
     */
    private String thirdPartyRefundNo;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 预计到账时间
     */
    private LocalDateTime expectedArrivalTime;

    /**
     * 退款凭证
     */
    private String refundVoucher;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 退款记录列表
     */
    private List<RefundRecordResponse> refundRecords;

    /**
     * 是否可以取消
     */
    private Boolean canCancel;

    /**
     * 是否需要人工处理
     */
    private Boolean needsManualProcess;

    /**
     * 是否处于终态
     */
    private Boolean finalStatus;

    /**
     * 处理耗时（小时）
     */
    private Long processDurationHours;

    /**
     * 获取格式化的退款金额
     * 
     * @return 格式化后的退款金额字符串
     */
    public String getFormattedRefundAmount() {
        return refundAmount != null ? "¥" + refundAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toString() : "¥0.00";
    }

    /**
     * 获取格式化的实际退款金额
     * 
     * @return 格式化后的实际退款金额字符串
     */
    public String getFormattedActualRefundAmount() {
        return actualRefundAmount != null ? "¥" + actualRefundAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toString() : "¥0.00";
    }

    /**
     * 获取格式化的退款手续费
     * 
     * @return 格式化后的退款手续费字符串
     */
    public String getFormattedRefundFee() {
        return refundFee != null ? "¥" + refundFee.setScale(2, BigDecimal.ROUND_HALF_UP).toString() : "¥0.00";
    }

    /**
     * 获取退款类型描述
     * 
     * @return 退款类型的中文描述
     */
    public String getRefundTypeDescription() {
        if (refundType == null) {
            return "未知";
        }
        switch (refundType) {
            case 1:
                return "用户申请";
            case 2:
                return "系统自动";
            case 3:
                return "客服处理";
            default:
                return "未知";
        }
    }

    /**
     * 获取格式化的处理耗时
     * 
     * @return 格式化后的处理耗时字符串
     */
    public String getFormattedProcessDuration() {
        if (processDurationHours == null) {
            return "未完成";
        }
        
        if (processDurationHours < 24) {
            return processDurationHours + "小时";
        } else {
            long days = processDurationHours / 24;
            long hours = processDurationHours % 24;
            return days + "天" + hours + "小时";
        }
    }

    /**
     * 判断是否为成功状态
     * 
     * @return 如果退款成功返回true，否则返回false
     */
    public boolean isSuccess() {
        return status != null && status.isSuccess();
    }

    /**
     * 判断是否为失败状态
     * 
     * @return 如果退款失败返回true，否则返回false
     */
    public boolean isFailed() {
        return status == RefundStatus.FAILED;
    }

    /**
     * 判断是否为处理中状态
     * 
     * @return 如果正在处理返回true，否则返回false
     */
    public boolean isProcessing() {
        return status == RefundStatus.PROCESSING;
    }

    /**
     * 判断是否为待审核状态
     * 
     * @return 如果待审核返回true，否则返回false
     */
    public boolean isPendingReview() {
        return status == RefundStatus.PENDING_REVIEW;
    }

    /**
     * 判断是否已审核通过
     * 
     * @return 如果已审核通过返回true，否则返回false
     */
    public boolean isApproved() {
        return status == RefundStatus.APPROVED;
    }

    /**
     * 判断是否已审核拒绝
     * 
     * @return 如果已审核拒绝返回true，否则返回false
     */
    public boolean isRejected() {
        return status == RefundStatus.REJECTED;
    }

    /**
     * 判断是否为用户申请退款
     * 
     * @return 如果是用户申请返回true，否则返回false
     */
    public boolean isUserApplied() {
        return refundType != null && refundType == 1;
    }

    /**
     * 判断是否为系统自动退款
     * 
     * @return 如果是系统自动返回true，否则返回false
     */
    public boolean isSystemAuto() {
        return refundType != null && refundType == 2;
    }

    /**
     * 判断是否为客服处理退款
     * 
     * @return 如果是客服处理返回true，否则返回false
     */
    public boolean isCustomerService() {
        return refundType != null && refundType == 3;
    }

    /**
     * 获取退款进度百分比
     * 
     * @return 退款进度百分比（0-100）
     */
    public int getProgressPercentage() {
        if (status == null) {
            return 0;
        }
        
        switch (status) {
            case PENDING_REVIEW:
                return 10;
            case REVIEWING:
                return 20;
            case APPROVED:
                return 40;
            case PROCESSING:
                return 70;
            case SUCCESS:
                return 100;
            case REJECTED:
            case FAILED:
            case CANCELLED:
                return 0;
            default:
                return 0;
        }
    }

    /**
     * 获取下一步操作提示
     * 
     * @return 下一步操作的提示信息
     */
    public String getNextActionHint() {
        if (status == null) {
            return "状态未知";
        }
        
        switch (status) {
            case PENDING_REVIEW:
                return "等待审核";
            case REVIEWING:
                return "审核中，请耐心等待";
            case APPROVED:
                return "审核通过，等待处理";
            case PROCESSING:
                return "退款处理中";
            case SUCCESS:
                return "退款已完成";
            case REJECTED:
                return "审核未通过：" + (reviewRemark != null ? reviewRemark : "请联系客服");
            case FAILED:
                return "退款失败：" + (failureReason != null ? failureReason : "请联系客服");
            case CANCELLED:
                return "退款已取消";
            default:
                return "请联系客服";
        }
    }

    /**
     * 获取预计到账时间描述
     * 
     * @return 预计到账时间的描述
     */
    public String getExpectedArrivalDescription() {
        if (expectedArrivalTime == null) {
            return "未知";
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expectedArrivalTime)) {
            return "应已到账";
        }
        
        long hours = java.time.Duration.between(now, expectedArrivalTime).toHours();
        if (hours < 24) {
            return hours + "小时内";
        } else {
            long days = hours / 24;
            return days + "天内";
        }
    }

    // Getter methods for fields that Lombok is not generating
    public String getId() {
        return id;
    }

    public String getRefundNo() {
        return refundNo;
    }

    public String getPaymentOrderId() {
        return paymentOrderId;
    }

    public String getUserId() {
        return userId;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public String getRefundReason() {
        return refundReason;
    }

    public Integer getRefundType() {
        return refundType;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRefundNo(String refundNo) {
        this.refundNo = refundNo;
    }

    public void setPaymentOrderId(String paymentOrderId) {
        this.paymentOrderId = paymentOrderId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }

    public void setRefundType(Integer refundType) {
        this.refundType = refundType;
    }

    public void setStatus(RefundStatus status) {
        this.status = status;
    }

    public void setRefundTypeDesc(String refundTypeDesc) {
        this.refundTypeDesc = refundTypeDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public void setReviewTime(LocalDateTime reviewTime) {
        this.reviewTime = reviewTime;
    }

    public void setReviewRemark(String reviewRemark) {
        this.reviewRemark = reviewRemark;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    public void setProcessTime(LocalDateTime processTime) {
        this.processTime = processTime;
    }

    public void setRefundTime(LocalDateTime refundTime) {
        this.refundTime = refundTime;
    }

    public void setActualRefundAmount(BigDecimal actualRefundAmount) {
        this.actualRefundAmount = actualRefundAmount;
    }

    public void setRefundFee(BigDecimal refundFee) {
        this.refundFee = refundFee;
    }

    public void setThirdPartyRefundNo(String thirdPartyRefundNo) {
        this.thirdPartyRefundNo = thirdPartyRefundNo;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public void setExpectedArrivalTime(LocalDateTime expectedArrivalTime) {
        this.expectedArrivalTime = expectedArrivalTime;
    }

    public void setRefundVoucher(String refundVoucher) {
        this.refundVoucher = refundVoucher;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setRefundRecords(List<RefundRecordResponse> refundRecords) {
        this.refundRecords = refundRecords;
    }

    public void setCanCancel(Boolean canCancel) {
        this.canCancel = canCancel;
    }

    public void setNeedsManualProcess(Boolean needsManualProcess) {
        this.needsManualProcess = needsManualProcess;
    }

    public void setFinalStatus(Boolean finalStatus) {
        this.finalStatus = finalStatus;
    }

    public void setProcessDurationHours(Long processDurationHours) {
        this.processDurationHours = processDurationHours;
    }
}