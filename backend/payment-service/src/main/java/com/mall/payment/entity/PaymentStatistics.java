package com.mall.payment.entity;

import com.mall.payment.enums.RefundStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 支付统计数据实体类
 * 用于存储支付相关的统计数据，支持按日、月、年等维度统计
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@Entity
@Table(name = "payment_statistics", indexes = {
    @Index(name = "idx_stat_date_type", columnList = "statDate,statType"),
    @Index(name = "idx_stat_date", columnList = "statDate"),
    @Index(name = "idx_stat_type", columnList = "statType"),
    @Index(name = "idx_payment_method", columnList = "paymentMethod"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
public class PaymentStatistics {

    /**
     * 统计记录ID
     */
    @Id
    @Column(name = "stat_id", length = 32)
    private String statId;

    /**
     * 统计日期
     */
    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    /**
     * 统计类型（日统计、月统计、年统计）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "stat_type", nullable = false, length = 20)
    private StatType statType;

    /**
     * 支付方式（可选，为空表示全部支付方式）
     */
    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    /**
     * 支付订单总数
     */
    @Column(name = "total_orders", nullable = false)
    private Long totalOrders = 0L;

    /**
     * 成功支付订单数
     */
    @Column(name = "success_orders", nullable = false)
    private Long successOrders = 0L;

    /**
     * 失败支付订单数
     */
    @Column(name = "failed_orders", nullable = false)
    private Long failedOrders = 0L;

    /**
     * 取消支付订单数
     */
    @Column(name = "cancelled_orders", nullable = false)
    private Long cancelledOrders = 0L;

    /**
     * 支付总金额
     */
    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /**
     * 成功支付金额
     */
    @Column(name = "success_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal successAmount = BigDecimal.ZERO;

    /**
     * 退款订单总数
     */
    @Column(name = "refund_orders", nullable = false)
    private Long refundOrders = 0L;

    /**
     * 退款总金额
     */
    @Column(name = "refund_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal refundAmount = BigDecimal.ZERO;

    /**
     * 手续费总金额
     */
    @Column(name = "fee_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal feeAmount = BigDecimal.ZERO;

    /**
     * 平均支付金额
     */
    @Column(name = "avg_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal avgAmount = BigDecimal.ZERO;

    /**
     * 支付成功率（百分比）
     */
    @Column(name = "success_rate", precision = 5, scale = 2, nullable = false)
    private BigDecimal successRate = BigDecimal.ZERO;

    /**
     * 独立用户数
     */
    @Column(name = "unique_users", nullable = false)
    private Long uniqueUsers = 0L;

    /**
     * 新用户数
     */
    @Column(name = "new_users", nullable = false)
    private Long newUsers = 0L;

    /**
     * 风控拦截订单数
     */
    @Column(name = "risk_blocked_orders", nullable = false)
    private Long riskBlockedOrders = 0L;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 统计类型枚举
     */
    public enum StatType {
        DAILY("日统计"),
        MONTHLY("月统计"),
        YEARLY("年统计");

        private final String description;

        StatType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // ==================== 构造函数 ====================

    public PaymentStatistics() {
        this.createdAt = LocalDateTime.now();
    }

    public PaymentStatistics(String statId, LocalDate statDate, StatType statType) {
        this();
        this.statId = statId;
        this.statDate = statDate;
        this.statType = statType;
    }

    public PaymentStatistics(String statId, LocalDate statDate, StatType statType, String paymentMethod) {
        this(statId, statDate, statType);
        this.paymentMethod = paymentMethod;
    }

    // ==================== 业务方法 ====================

    /**
     * 计算支付成功率
     */
    public void calculateSuccessRate() {
        if (totalOrders > 0) {
            this.successRate = new BigDecimal(successOrders)
                    .divide(new BigDecimal(totalOrders), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal(100));
        } else {
            this.successRate = BigDecimal.ZERO;
        }
    }

    /**
     * 计算平均支付金额
     */
    public void calculateAvgAmount() {
        if (successOrders > 0) {
            this.avgAmount = successAmount.divide(new BigDecimal(successOrders), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            this.avgAmount = BigDecimal.ZERO;
        }
    }

    /**
     * 更新统计数据
     * 
     * @param order 支付订单
     */
    public void updateWithOrder(PaymentOrder order) {
        this.totalOrders++;
        this.totalAmount = this.totalAmount.add(order.getAmount());
        
        switch (order.getStatus()) {
            case SUCCESS:
                this.successOrders++;
                this.successAmount = this.successAmount.add(order.getAmount());
                break;
            case FAILED:
                this.failedOrders++;
                break;
            case CANCELLED:
                this.cancelledOrders++;
                break;
        }
        
        // 累加手续费
        if (order.getFeeAmount() != null) {
            this.feeAmount = this.feeAmount.add(order.getFeeAmount());
        }
        
        // 重新计算成功率和平均金额
        calculateSuccessRate();
        calculateAvgAmount();
        
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新退款统计
     * 
     * @param refund 退款订单
     */
    public void updateWithRefund(RefundOrder refund) {
        if (refund.getStatus() == RefundStatus.SUCCESS) {
            this.refundOrders++;
            this.refundAmount = this.refundAmount.add(refund.getRefundAmount());
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 检查是否为今日统计
     * 
     * @return 是否为今日统计
     */
    public boolean isToday() {
        return statType == StatType.DAILY && statDate.equals(LocalDate.now());
    }

    /**
     * 检查是否为本月统计
     * 
     * @return 是否为本月统计
     */
    public boolean isThisMonth() {
        LocalDate now = LocalDate.now();
        return statType == StatType.MONTHLY && 
               statDate.getYear() == now.getYear() && 
               statDate.getMonth() == now.getMonth();
    }

    /**
     * 检查是否为本年统计
     * 
     * @return 是否为本年统计
     */
    public boolean isThisYear() {
        return statType == StatType.YEARLY && statDate.getYear() == LocalDate.now().getYear();
    }

    // ==================== Getter和Setter ====================

    public String getStatId() {
        return statId;
    }

    public void setStatId(String statId) {
        this.statId = statId;
    }

    public LocalDate getStatDate() {
        return statDate;
    }

    public void setStatDate(LocalDate statDate) {
        this.statDate = statDate;
    }

    public StatType getStatType() {
        return statType;
    }

    public void setStatType(StatType statType) {
        this.statType = statType;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getSuccessOrders() {
        return successOrders;
    }

    public void setSuccessOrders(Long successOrders) {
        this.successOrders = successOrders;
    }

    public Long getFailedOrders() {
        return failedOrders;
    }

    public void setFailedOrders(Long failedOrders) {
        this.failedOrders = failedOrders;
    }

    public Long getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(Long cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getSuccessAmount() {
        return successAmount;
    }

    public void setSuccessAmount(BigDecimal successAmount) {
        this.successAmount = successAmount;
    }

    public Long getRefundOrders() {
        return refundOrders;
    }

    public void setRefundOrders(Long refundOrders) {
        this.refundOrders = refundOrders;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }

    public BigDecimal getAvgAmount() {
        return avgAmount;
    }

    public void setAvgAmount(BigDecimal avgAmount) {
        this.avgAmount = avgAmount;
    }

    public BigDecimal getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(BigDecimal successRate) {
        this.successRate = successRate;
    }

    public Long getUniqueUsers() {
        return uniqueUsers;
    }

    public void setUniqueUsers(Long uniqueUsers) {
        this.uniqueUsers = uniqueUsers;
    }

    public Long getNewUsers() {
        return newUsers;
    }

    public void setNewUsers(Long newUsers) {
        this.newUsers = newUsers;
    }

    public Long getRiskBlockedOrders() {
        return riskBlockedOrders;
    }

    public void setRiskBlockedOrders(Long riskBlockedOrders) {
        this.riskBlockedOrders = riskBlockedOrders;
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

    @Override
    public String toString() {
        return "PaymentStatistics{" +
                "statId='" + statId + '\'' +
                ", statDate=" + statDate +
                ", statType=" + statType +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", totalOrders=" + totalOrders +
                ", successOrders=" + successOrders +
                ", totalAmount=" + totalAmount +
                ", successAmount=" + successAmount +
                ", successRate=" + successRate +
                '}';
    }
}