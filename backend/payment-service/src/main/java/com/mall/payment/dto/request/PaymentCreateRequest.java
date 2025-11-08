package com.mall.payment.dto.request;

import com.mall.payment.enums.PaymentMethod;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 创建支付订单请求DTO
 * 用于接收前端或其他服务创建支付订单的请求参数
 * 
 * <p>请求参数说明：</p>
 * <ul>
 *   <li>businessOrderId：关联的业务订单ID，用于关联具体的业务订单</li>
 *   <li>userId：发起支付的用户ID，用于用户身份验证和权限控制</li>
 *   <li>amount：支付金额，必须大于0.01且不超过999999.99</li>
 *   <li>paymentMethod：支付方式，使用枚举类型确保数据一致性</li>
 *   <li>description：支付描述，用于显示给用户的支付说明</li>
 *   <li>returnUrl：支付成功后的跳转地址</li>
 *   <li>notifyUrl：异步通知地址，用于接收支付结果通知</li>
 * </ul>
 * 
 * <p>验证规则：</p>
 * <ul>
 *   <li>所有必填字段都有@NotNull或@NotBlank验证</li>
 *   <li>字符串字段有长度限制，防止数据库字段溢出</li>
 *   <li>金额字段有精度和范围验证</li>
 *   <li>URL字段有格式验证</li>
 * </ul>
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * 修改日志：
 * V1.2 2025-11-01：完善Javadoc注释，增加参数说明和验证规则
 * V1.1 2024-12-10：增加URL格式验证
 * V1.0 2024-12-01：初始版本，定义基本请求参数
 */
@Data
public class PaymentCreateRequest {

    /**
     * 业务订单ID - 关联的业务订单编号
     */
    @NotBlank(message = "业务订单ID不能为空")
    @Size(max = 64, message = "业务订单ID长度不能超过64个字符")
    private String businessOrderId;

    /**
     * 用户ID - 发起支付的用户
     */
    @NotBlank(message = "用户ID不能为空")
    @Size(max = 36, message = "用户ID长度不能超过36个字符")
    private String userId;

    /**
     * 支付金额 - 必须大于0
     */
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0.01")
    @DecimalMax(value = "999999.99", message = "支付金额不能超过999999.99")
    @Digits(integer = 6, fraction = 2, message = "支付金额格式不正确")
    private BigDecimal amount;

    /**
     * 支付方式 - 使用枚举类型
     */
    @NotNull(message = "支付方式不能为空")
    private PaymentMethod paymentMethod;

    /**
     * 支付描述 - 支付订单的描述信息
     */
    @Size(max = 255, message = "支付描述长度不能超过255个字符")
    private String description;

    /**
     * 支付成功后的返回URL
     */
    @Size(max = 500, message = "返回URL长度不能超过500个字符")
    @Pattern(regexp = "^https?://.*", message = "返回URL格式不正确")
    private String returnUrl;

    /**
     * 异步通知URL
     */
    @Size(max = 500, message = "通知URL长度不能超过500个字符")
    @Pattern(regexp = "^https?://.*", message = "通知URL格式不正确")
    private String notifyUrl;

    /**
     * 支付过期时间（分钟） - 默认30分钟
     */
    @Min(value = 1, message = "过期时间至少为1分钟")
    @Max(value = 1440, message = "过期时间不能超过1440分钟（24小时）")
    private Integer expireMinutes = 30;

    /**
     * 客户端IP地址
     */
    @Size(max = 45, message = "客户端IP长度不能超过45个字符")
    @Pattern(regexp = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$", 
             message = "客户端IP格式不正确")
    private String clientIp;

    /**
     * 用户代理信息
     */
    @Size(max = 500, message = "用户代理信息长度不能超过500个字符")
    private String userAgent;

    /**
     * 设备信息
     */
    @Size(max = 200, message = "设备信息长度不能超过200个字符")
    private String deviceInfo;

    /**
     * 扩展参数 - JSON格式的额外参数
     */
    @Size(max = 1000, message = "扩展参数长度不能超过1000个字符")
    private String extParams;

    /**
     * 验证支付金额是否有效
     * 
     * @return 如果金额有效返回true，否则返回false
     */
    public boolean isValidAmount() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 验证支付方式是否启用
     * 
     * @return 如果支付方式启用返回true，否则返回false
     */
    public boolean isPaymentMethodEnabled() {
        return paymentMethod != null && paymentMethod.isEnabled();
    }

    /**
     * 获取格式化的支付金额（保留2位小数）
     * 
     * @return 格式化后的支付金额
     */
    public String getFormattedAmount() {
        return amount != null ? amount.setScale(2, BigDecimal.ROUND_HALF_UP).toString() : "0.00";
    }
    
    // Getter and Setter methods for fields that Lombok is not generating
    public String getBusinessOrderId() {
        return businessOrderId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public void setBusinessOrderId(String businessOrderId) {
        this.businessOrderId = businessOrderId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public Integer getExpireMinutes() {
        return expireMinutes;
    }

    public void setExpireMinutes(Integer expireMinutes) {
        this.expireMinutes = expireMinutes;
    }

    public String getExtParams() {
        return extParams;
    }

    public void setExtParams(String extParams) {
        this.extParams = extParams;
    }

    /**
     * 获取订单ID（别名方法，返回businessOrderId）
     * @return 业务订单ID
     */
    public String getOrderId() {
        return businessOrderId;
    }
}