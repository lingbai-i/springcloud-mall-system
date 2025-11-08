package com.mall.payment.dto.request;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 创建退款订单请求DTO
 * 用于接收前端或其他服务创建退款订单的请求参数
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@Data
public class RefundCreateRequest {

    /**
     * 支付订单ID - 要退款的支付订单
     */
    @NotBlank(message = "支付订单ID不能为空")
    @Size(max = 36, message = "支付订单ID长度不能超过36个字符")
    private String paymentOrderId;

    /**
     * 用户ID - 申请退款的用户
     */
    @NotBlank(message = "用户ID不能为空")
    @Size(max = 36, message = "用户ID长度不能超过36个字符")
    private String userId;

    /**
     * 退款金额 - 必须大于0且不能超过可退款金额
     */
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0.01")
    @DecimalMax(value = "999999.99", message = "退款金额不能超过999999.99")
    @Digits(integer = 6, fraction = 2, message = "退款金额格式不正确")
    private BigDecimal refundAmount;

    /**
     * 退款原因 - 用户填写的退款原因
     */
    @NotBlank(message = "退款原因不能为空")
    @Size(min = 5, max = 500, message = "退款原因长度必须在5-500个字符之间")
    private String refundReason;

    /**
     * 退款类型 - 1:用户申请 2:系统自动 3:客服处理
     */
    @NotNull(message = "退款类型不能为空")
    @Min(value = 1, message = "退款类型必须为1、2或3")
    @Max(value = 3, message = "退款类型必须为1、2或3")
    private Integer refundType = 1;

    /**
     * 联系电话 - 用于退款处理时联系用户
     */
    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    private String contactPhone;

    /**
     * 联系邮箱 - 用于退款处理时联系用户
     */
    @Size(max = 100, message = "联系邮箱长度不能超过100个字符")
    @Email(message = "联系邮箱格式不正确")
    private String contactEmail;

    /**
     * 退款凭证 - 退款相关的凭证文件URL
     */
    @Size(max = 500, message = "退款凭证URL长度不能超过500个字符")
    private String refundVoucher;

    /**
     * 备注信息 - 额外的说明信息
     */
    @Size(max = 500, message = "备注信息长度不能超过500个字符")
    private String remark;

    /**
     * 是否加急处理 - 是否需要加急处理退款
     */
    private Boolean urgent = false;

    /**
     * 客户端IP地址
     */
    @Size(max = 45, message = "客户端IP长度不能超过45个字符")
    @Pattern(regexp = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$", 
             message = "客户端IP格式不正确")
    private String clientIp;

    /**
     * 验证退款金额是否有效
     * 
     * @return 如果金额有效返回true，否则返回false
     */
    public boolean isValidRefundAmount() {
        return refundAmount != null && refundAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 验证退款类型是否有效
     * 
     * @return 如果退款类型有效返回true，否则返回false
     */
    public boolean isValidRefundType() {
        return refundType != null && refundType >= 1 && refundType <= 3;
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
     * 获取格式化的退款金额（保留2位小数）
     * 
     * @return 格式化后的退款金额
     */
    public String getFormattedRefundAmount() {
        return refundAmount != null ? refundAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toString() : "0.00";
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
     * 验证联系方式是否完整
     * 至少需要提供电话或邮箱中的一种
     * 
     * @return 如果联系方式完整返回true，否则返回false
     */
    public boolean hasValidContact() {
        return (contactPhone != null && !contactPhone.trim().isEmpty()) ||
               (contactEmail != null && !contactEmail.trim().isEmpty());
    }

    // Getter methods for fields that Lombok is not generating
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

    public String getContactPhone() {
        return contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getRefundVoucher() {
        return refundVoucher;
    }

    public String getRemark() {
        return remark;
    }

    public Boolean getUrgent() {
        return urgent;
    }

    public String getClientIp() {
        return clientIp;
    }
}