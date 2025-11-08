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

/**
 * 支付记录实体类
 * 记录每次支付操作的详细信息，包括支付请求、响应、状态变更等
 * 一个支付订单可能有多条支付记录（重试、部分支付等场景）
 * 
 * <p>数据表结构：</p>
 * <ul>
 *   <li>表名：payment_records</li>
 *   <li>主键：id（UUID）</li>
 *   <li>索引：payment_order_id, third_party_trade_no, status, created_at</li>
 *   <li>审计：自动记录创建时间和更新时间</li>
 * </ul>
 * 
 * <p>业务关系：</p>
 * <ul>
 *   <li>多对一：多条支付记录对应一个支付订单</li>
 *   <li>状态同步：记录状态与订单状态保持一致</li>
 *   <li>第三方关联：通过第三方交易号关联外部支付平台</li>
 * </ul>
 * 
 * <p>记录场景：</p>
 * <ul>
 *   <li>首次支付：用户发起支付时创建首条记录</li>
 *   <li>支付重试：支付失败后重新发起支付</li>
 *   <li>状态更新：第三方回调更新支付状态</li>
 *   <li>异常处理：记录支付过程中的异常信息</li>
 *   <li>对账核查：与第三方平台进行数据核对</li>
 * </ul>
 * 
 * <p>审计功能：</p>
 * <ul>
 *   <li>创建时间：记录支付记录的创建时间</li>
 *   <li>更新时间：记录最后一次状态更新时间</li>
 *   <li>操作追踪：跟踪支付操作的完整流程</li>
 * </ul>
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * 修改日志：
 * V1.2 2025-11-01：完善Javadoc注释，增加数据表结构和业务关系说明
 * V1.1 2024-12-20：增加第三方交易号字段和索引优化
 * V1.0 2024-12-01：初始版本，定义基础支付记录结构
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "payment_records", indexes = {
    @Index(name = "idx_payment_order_id", columnList = "paymentOrderId"),
    @Index(name = "idx_third_party_trade_no", columnList = "thirdPartyTradeNo"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@EntityListeners(AuditingEntityListener.class)
public class PaymentRecord {

    /**
     * 支付记录ID - 主键，使用UUID生成
     */
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", length = 36)
    private String id;

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
     * 支付方式 - 本次支付使用的方式
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    /**
     * 支付金额 - 本次支付的金额
     */
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * 支付状态 - 本次支付的状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    /**
     * 第三方交易号 - 支付渠道返回的交易流水号
     */
    @Column(name = "third_party_trade_no", length = 64)
    private String thirdPartyTradeNo;

    /**
     * 支付渠道 - 具体的支付渠道标识
     */
    @Column(name = "payment_channel", length = 50)
    private String paymentChannel;

    /**
     * 支付请求参数 - JSON格式存储发送给第三方的请求参数
     */
    @Column(name = "request_params", columnDefinition = "TEXT")
    private String requestParams;

    /**
     * 支付响应数据 - JSON格式存储第三方返回的响应数据
     */
    @Column(name = "response_data", columnDefinition = "TEXT")
    private String responseData;

    /**
     * 支付完成时间 - 第三方确认支付成功的时间
     */
    @Column(name = "pay_time")
    private LocalDateTime payTime;

    /**
     * 手续费 - 本次支付产生的手续费
     */
    @Column(name = "fee_amount", precision = 15, scale = 2)
    private BigDecimal feeAmount;

    /**
     * 错误代码 - 支付失败时的错误代码
     */
    @Column(name = "error_code", length = 50)
    private String errorCode;

    /**
     * 错误信息 - 支付失败时的详细错误信息
     */
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    /**
     * 重试次数 - 当前记录的重试次数
     */
    @Column(name = "retry_count")
    private Integer retryCount = 0;

    /**
     * 客户端IP - 发起支付的客户端IP地址
     */
    @Column(name = "client_ip", length = 45)
    private String clientIp;

    /**
     * 用户代理 - 客户端浏览器信息
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 设备信息 - 支付设备的相关信息
     */
    @Column(name = "device_info", length = 200)
    private String deviceInfo;

    /**
     * 操作类型 - 记录本次支付记录的操作类型
     */
    @Column(name = "action", length = 100)
    private String action;

    /**
     * 操作描述 - 记录本次支付记录的详细描述
     */
    @Column(name = "description", length = 500)
    private String description;

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
     * 判断支付记录是否成功
     * 
     * @return 如果支付成功返回true，否则返回false
     */
    public boolean isSuccess() {
        return status == PaymentStatus.SUCCESS;
    }

    /**
     * 判断支付记录是否失败
     * 
     * @return 如果支付失败返回true，否则返回false
     */
    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }

    /**
     * 判断支付记录是否处于处理中状态
     * 
     * @return 如果正在处理返回true，否则返回false
     */
    public boolean isProcessing() {
        return status == PaymentStatus.PROCESSING;
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
     * 更新支付状态
     * 同时更新相关的时间和错误信息字段
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
     * 设置支付失败信息
     * 
     * @param errorCode 错误代码
     * @param errorMessage 错误信息
     */
    public void setFailureInfo(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.status = PaymentStatus.FAILED;
    }

    /**
     * 增加重试次数
     */
    public void incrementRetryCount() {
        this.retryCount = (this.retryCount == null ? 0 : this.retryCount) + 1;
    }

    /**
     * 计算支付耗时（毫秒）
     * 从创建到支付完成的时间差
     * 
     * @return 支付耗时，如果未完成支付返回null
     */
    public Long getPaymentDuration() {
        if (payTime == null || createdAt == null) {
            return null;
        }
        return java.time.Duration.between(createdAt, payTime).toMillis();
    }

    // 手动添加缺失的setter方法
    public void setId(String id) {
        this.id = id;
    }

    public void setPaymentOrder(PaymentOrder paymentOrder) {
        this.paymentOrder = paymentOrder;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}