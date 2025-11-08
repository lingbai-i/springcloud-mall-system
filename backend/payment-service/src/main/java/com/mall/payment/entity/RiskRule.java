package com.mall.payment.entity;

import com.mall.payment.enums.PaymentMethod;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 风控规则实体类
 * 定义支付风控的各种规则配置
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@Entity
@Table(name = "risk_rules")
public class RiskRule {

    /**
     * 规则ID
     */
    @Id
    @Column(name = "rule_id", length = 64)
    private String ruleId;

    /**
     * 规则名称
     */
    @Column(name = "rule_name", length = 100, nullable = false)
    private String ruleName;

    /**
     * 规则类型
     * AMOUNT_LIMIT: 金额限制
     * FREQUENCY_LIMIT: 频率限制
     * IP_BLACKLIST: IP黑名单
     * DEVICE_LIMIT: 设备限制
     * TIME_LIMIT: 时间限制
     * VELOCITY_CHECK: 速度检查
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", length = 20, nullable = false)
    private RuleType ruleType;

    /**
     * 支付方式（可选，为空表示适用于所有支付方式）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    /**
     * 规则配置（JSON格式）
     * 存储规则的具体配置参数
     */
    @Column(name = "rule_config", columnDefinition = "TEXT")
    private String ruleConfig;

    /**
     * 阈值
     */
    @Column(name = "threshold_value", precision = 15, scale = 2)
    private BigDecimal thresholdValue;

    /**
     * 时间窗口（秒）
     */
    @Column(name = "time_window_seconds")
    private Integer timeWindowSeconds;

    /**
     * 风险等级
     * LOW: 低风险
     * MEDIUM: 中风险
     * HIGH: 高风险
     * CRITICAL: 严重风险
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 10, nullable = false)
    private RiskLevel riskLevel;

    /**
     * 处理动作
     * ALLOW: 允许
     * WARN: 警告
     * BLOCK: 阻止
     * MANUAL_REVIEW: 人工审核
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 20, nullable = false)
    private RiskAction action;

    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * 优先级（数值越小优先级越高）
     */
    @Column(name = "priority", nullable = false)
    private Integer priority = 100;

    /**
     * 规则描述
     */
    @Column(name = "description", length = 500)
    private String description;

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

    /**
     * 创建人
     */
    @Column(name = "created_by", length = 64)
    private String createdBy;

    /**
     * 更新人
     */
    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    // ==================== 构造函数 ====================

    public RiskRule() {
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
     * 检查规则是否适用于指定的支付方式
     * 
     * @param paymentMethod 支付方式
     * @return 是否适用
     */
    public boolean isApplicableToPaymentMethod(PaymentMethod paymentMethod) {
        return this.paymentMethod == null || this.paymentMethod == paymentMethod;
    }

    /**
     * 检查规则是否启用且有效
     * 
     * @return 是否启用且有效
     */
    public boolean isActiveRule() {
        return enabled != null && enabled;
    }

    /**
     * 获取规则的风险权重
     * 根据风险等级返回相应的权重值
     * 
     * @return 风险权重
     */
    public int getRiskWeight() {
        if (riskLevel == null) {
            return 0;
        }
        
        switch (riskLevel) {
            case LOW:
                return 1;
            case MEDIUM:
                return 3;
            case HIGH:
                return 5;
            case CRITICAL:
                return 10;
            default:
                return 0;
        }
    }

    // ==================== Getter和Setter方法 ====================

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getRuleConfig() {
        return ruleConfig;
    }

    public void setRuleConfig(String ruleConfig) {
        this.ruleConfig = ruleConfig;
    }

    public BigDecimal getThresholdValue() {
        return thresholdValue;
    }

    public void setThresholdValue(BigDecimal thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    public Integer getTimeWindowSeconds() {
        return timeWindowSeconds;
    }

    public void setTimeWindowSeconds(Integer timeWindowSeconds) {
        this.timeWindowSeconds = timeWindowSeconds;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public RiskAction getAction() {
        return action;
    }

    public void setAction(RiskAction action) {
        this.action = action;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    // ==================== 枚举定义 ====================

    /**
     * 规则类型枚举
     */
    public enum RuleType {
        /**
         * 金额限制
         */
        AMOUNT_LIMIT,
        
        /**
         * 频率限制
         */
        FREQUENCY_LIMIT,
        
        /**
         * IP黑名单
         */
        IP_BLACKLIST,
        
        /**
         * 设备限制
         */
        DEVICE_LIMIT,
        
        /**
         * 时间限制
         */
        TIME_LIMIT,
        
        /**
         * 速度检查
         */
        VELOCITY_CHECK
    }

    /**
     * 风险等级枚举
     */
    public enum RiskLevel {
        /**
         * 低风险
         */
        LOW,
        
        /**
         * 中风险
         */
        MEDIUM,
        
        /**
         * 高风险
         */
        HIGH,
        
        /**
         * 严重风险
         */
        CRITICAL
    }

    /**
     * 风险处理动作枚举
     */
    public enum RiskAction {
        /**
         * 允许
         */
        ALLOW,
        
        /**
         * 警告
         */
        WARN,
        
        /**
         * 阻止
         */
        BLOCK,
        
        /**
         * 人工审核
         */
        MANUAL_REVIEW
    }
}