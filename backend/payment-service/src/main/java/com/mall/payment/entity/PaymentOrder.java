package com.mall.payment.entity;

import com.mall.payment.enums.PaymentMethod;
import com.mall.payment.enums.PaymentStatus;
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
 * 支付订单实体类
 * 存储支付订单的基本信息，包括订单金额、支付方式、支付状态等
 * 
 * <p>数据表结构：</p>
 * <ul>
 *   <li>表名：payment_orders</li>
 *   <li>主键：id（UUID）</li>
 *   <li>索引：order_id、user_id、status、created_at</li>
 * </ul>
 * 
 * <p>业务关系：</p>
 * <ul>
 *   <li>一个支付订单对应一个业务订单（order_id）</li>
 *   <li>一个支付订单可以有多条支付记录（重试场景）</li>
 *   <li>一个支付订单可以有多个退款订单（部分退款场景）</li>
 * </ul>
 * 
 * <p>状态流转：</p>
 * <ul>
 *   <li>PENDING（待支付）→ PROCESSING（支付中）→ SUCCESS（支付成功）</li>
 *   <li>支付成功后可能发生退款：SUCCESS → REFUNDED/PARTIAL_REFUNDED</li>
 *   <li>支付失败或超时：PROCESSING → FAILED/EXPIRED</li>
 * </ul>
 * 
 * <p>审计功能：</p>
 * <ul>
 *   <li>自动记录创建时间（created_at）</li>
 *   <li>自动记录更新时间（updated_at）</li>
 *   <li>支持软删除（deleted字段）</li>
 * </ul>
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * 修改日志：
 * V1.2 2025-11-01：完善Javadoc注释，增加数据表结构和业务关系说明
 * V1.1 2024-12-10：增加软删除功能和审计字段
 * V1.0 2024-12-01：初始版本，定义基本支付订单结构
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "payment_orders", indexes = {
    @Index(name = "idx_order_id", columnList = "orderId"),
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@EntityListeners(AuditingEntityListener.class)
public class PaymentOrder {

    /**
     * 支付订单ID - 主键，使用UUID生成
     */
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", length = 36)
    private String id;

    /**
     * 业务订单ID - 关联的业务订单编号
     */
    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    /**
     * 用户ID - 发起支付的用户
     */
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    /**
     * 支付金额 - 以分为单位存储，避免精度问题
     */
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * 支付方式 - 使用枚举类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    /**
     * 支付状态 - 使用枚举类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    /**
     * 支付描述 - 支付订单的描述信息
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * 支付成功后的返回URL
     */
    @Column(name = "return_url", length = 500)
    private String returnUrl;

    /**
     * 异步通知URL
     */
    @Column(name = "notify_url", length = 500)
    private String notifyUrl;

    /**
     * 支付过期时间 - 超过此时间支付订单自动失效
     */
    @Column(name = "expire_time")
    private LocalDateTime expireTime;

    /**
     * 第三方支付订单号 - 来自支付宝、微信等第三方平台的订单号
     */
    @Column(name = "third_party_order_no", length = 64)
    private String thirdPartyOrderNo;

    /**
     * 支付完成时间
     */
    @Column(name = "pay_time")
    private LocalDateTime payTime;

    /**
     * 实际支付金额 - 可能与订单金额不同（优惠券、折扣等）
     */
    @Column(name = "actual_amount", precision = 15, scale = 2)
    private BigDecimal actualAmount;

    /**
     * 手续费 - 支付渠道收取的手续费
     */
    @Column(name = "fee_amount", precision = 15, scale = 2)
    private BigDecimal feeAmount;

    /**
     * 支付渠道响应数据 - JSON格式存储第三方返回的原始数据
     */
    @Column(name = "channel_response", columnDefinition = "TEXT")
    private String channelResponse;

    /**
     * 失败原因 - 支付失败时的错误信息
     */
    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    /**
     * 重试次数 - 支付失败后的重试次数
     */
    @Column(name = "retry_count")
    private Integer retryCount = 0;

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
     * 删除标记 - 软删除标记，false表示未删除，true表示已删除
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    /**
     * 支付记录列表 - 一对多关系
     */
    @OneToMany(mappedBy = "paymentOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PaymentRecord> paymentRecords = new ArrayList<>();

    /**
     * 退款订单列表 - 一对多关系
     */
    @OneToMany(mappedBy = "paymentOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RefundOrder> refundOrders = new ArrayList<>();

    /**
     * 判断支付订单是否已过期
     * 
     * @return 如果已过期返回true，否则返回false
     */
    public boolean isExpired() {
        return expireTime != null && LocalDateTime.now().isAfter(expireTime);
    }

    /**
     * 判断是否可以支付
     * 只有待支付状态且未过期的订单才可以支付
     * 
     * @return 如果可以支付返回true，否则返回false
     */
    public boolean canPay() {
        return status == PaymentStatus.PENDING && !isExpired();
    }

    /**
     * 判断是否可以取消
     * 只有待支付和支付中状态的订单才可以取消
     * 
     * @return 如果可以取消返回true，否则返回false
     */
    public boolean canCancel() {
        return status == PaymentStatus.PENDING || status == PaymentStatus.PROCESSING;
    }

    /**
     * 判断是否可以退款
     * 只有支付成功或部分退款状态的订单才可以退款
     * 
     * @return 如果可以退款返回true，否则返回false
     */
    public boolean canRefund() {
        return status.canRefund();
    }

    /**
     * 计算可退款金额
     * 实际支付金额减去已退款金额
     * 
     * @return 可退款金额
     */
    public BigDecimal getRefundableAmount() {
        if (actualAmount == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal refundedAmount = refundOrders.stream()
                .filter(refund -> refund.getStatus().isSuccess())
                .map(RefundOrder::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        return actualAmount.subtract(refundedAmount);
    }

    /**
     * 更新支付状态
     * 同时更新相关的时间字段
     * 
     * @param newStatus 新的支付状态
     */
    public void updateStatus(PaymentStatus newStatus) {
        this.status = newStatus;
        if (newStatus == PaymentStatus.SUCCESS && this.payTime == null) {
            this.payTime = LocalDateTime.now();
        }
    }

    /**
     * 增加重试次数
     */
    public void incrementRetryCount() {
        this.retryCount = (this.retryCount == null ? 0 : this.retryCount) + 1;
    }
    
    // Getter methods for fields that Lombok is not generating
    public String getId() {
        return id;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public BigDecimal getActualAmount() {
        return actualAmount;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public BigDecimal getFeeAmount() {
        return feeAmount;
    }
    
    // Setter methods for fields that Lombok is not generating
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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

    public String getThirdPartyOrderNo() {
        return thirdPartyOrderNo;
    }

    public void setThirdPartyOrderNo(String thirdPartyOrderNo) {
        this.thirdPartyOrderNo = thirdPartyOrderNo;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public LocalDateTime getPayTime() {
        return payTime;
    }

    public void setPayTime(LocalDateTime payTime) {
        this.payTime = payTime;
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

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
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

    public List<PaymentRecord> getPaymentRecords() {
        return paymentRecords;
    }

    public void setPaymentRecords(List<PaymentRecord> paymentRecords) {
        this.paymentRecords = paymentRecords;
    }

    public List<RefundOrder> getRefundOrders() {
        return refundOrders;
    }

    public void setRefundOrders(List<RefundOrder> refundOrders) {
        this.refundOrders = refundOrders;
    }

    public LocalDateTime getPaidAt() {
        return payTime;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.payTime = paidAt;
    }

    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

    // 手动添加缺失的setter方法
    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}