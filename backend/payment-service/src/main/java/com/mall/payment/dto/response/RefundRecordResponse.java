package com.mall.payment.dto.response;

import com.mall.payment.enums.RefundStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录响应DTO
 * 用于返回退款记录的详细信息给前端或其他服务
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@Data
public class RefundRecordResponse {

    /**
     * 退款记录ID
     */
    private String id;

    /**
     * 退款订单ID
     */
    private String refundOrderId;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款状态
     */
    private RefundStatus status;

    /**
     * 退款状态描述
     */
    private String statusDesc;

    /**
     * 第三方退款单号
     */
    private String thirdPartyRefundNo;

    /**
     * 退款渠道
     */
    private String refundChannel;

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
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 操作人ID
     */
    private String operatorId;

    /**
     * 操作人类型
     */
    private Integer operatorType;

    /**
     * 操作人类型描述
     */
    private String operatorTypeDesc;

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
     * 退款耗时（毫秒）
     */
    private Long refundDuration;

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
     * 获取格式化的退款耗时
     * 
     * @return 格式化后的退款耗时字符串
     */
    public String getFormattedRefundDuration() {
        if (refundDuration == null) {
            return "未完成";
        }
        
        if (refundDuration < 1000) {
            return refundDuration + "毫秒";
        } else if (refundDuration < 60000) {
            return (refundDuration / 1000) + "秒";
        } else if (refundDuration < 3600000) {
            return (refundDuration / 60000) + "分钟";
        } else {
            return (refundDuration / 3600000) + "小时";
        }
    }

    /**
     * 获取操作人类型描述
     * 
     * @return 操作人类型的中文描述
     */
    public String getOperatorTypeDescription() {
        if (operatorType == null) {
            return "未知";
        }
        switch (operatorType) {
            case 1:
                return "系统自动";
            case 2:
                return "管理员手动";
            case 3:
                return "用户申请";
            default:
                return "未知";
        }
    }

    /**
     * 判断是否为成功状态
     * 
     * @return 如果退款成功返回true，否则返回false
     */
    public boolean isSuccess() {
        return status == RefundStatus.SUCCESS;
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
     * 判断是否有错误信息
     * 
     * @return 如果有错误信息返回true，否则返回false
     */
    public boolean hasError() {
        return errorCode != null && !errorCode.trim().isEmpty();
    }

    /**
     * 获取完整的错误信息
     * 
     * @return 包含错误代码和错误信息的完整描述
     */
    public String getFullErrorMessage() {
        if (!hasError()) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        if (errorCode != null) {
            sb.append("[").append(errorCode).append("]");
        }
        if (errorMessage != null) {
            sb.append(" ").append(errorMessage);
        }
        
        return sb.toString().trim();
    }

    /**
     * 判断是否可以重试
     * 
     * @param maxRetryCount 最大重试次数
     * @return 如果可以重试返回true，否则返回false
     */
    public boolean canRetry(int maxRetryCount) {
        return isFailed() && (retryCount == null || retryCount < maxRetryCount);
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

    /**
     * 获取退款渠道显示名称
     * 
     * @return 退款渠道的显示名称
     */
    public String getRefundChannelDisplayName() {
        if (refundChannel == null) {
            return "未知";
        }
        
        // 根据退款渠道返回友好的显示名称
        switch (refundChannel.toLowerCase()) {
            case "alipay":
                return "支付宝";
            case "wechat":
                return "微信支付";
            case "unionpay":
                return "银联支付";
            case "balance":
                return "余额退款";
            case "bank":
                return "银行卡";
            default:
                return refundChannel;
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
        if (hours < 1) {
            return "1小时内";
        } else if (hours < 24) {
            return hours + "小时内";
        } else {
            long days = hours / 24;
            return days + "天内";
        }
    }

    /**
     * 获取退款状态颜色
     * 用于前端显示不同状态的颜色
     * 
     * @return 状态对应的颜色代码
     */
    public String getStatusColor() {
        if (status == null) {
            return "#999999"; // 灰色
        }
        
        switch (status) {
            case SUCCESS:
                return "#52c41a"; // 绿色
            case FAILED:
                return "#ff4d4f"; // 红色
            case PROCESSING:
                return "#1890ff"; // 蓝色
            default:
                return "#faad14"; // 橙色
        }
    }
}