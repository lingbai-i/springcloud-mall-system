package com.mall.payment.service;

import com.mall.payment.dto.request.PaymentCreateRequest;
import com.mall.payment.entity.RiskRecord;
import com.mall.payment.entity.RiskRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 风控服务接口
 * 提供支付风控相关的业务功能
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
public interface RiskControlService {

    /**
     * 执行风控检查
     * 对支付请求进行风控检查，返回风控结果
     * 
     * @param request 支付创建请求
     * @param paymentOrderId 支付订单ID
     * @return 风控检查结果
     */
    RiskCheckResult performRiskCheck(PaymentCreateRequest request, String paymentOrderId);

    /**
     * 根据支付订单ID查询风控记录
     * 
     * @param paymentOrderId 支付订单ID
     * @return 风控记录
     */
    RiskRecord getRiskRecordByPaymentOrderId(String paymentOrderId);

    /**
     * 根据业务订单ID查询风控记录
     * 
     * @param businessOrderId 业务订单ID
     * @return 风控记录
     */
    RiskRecord getRiskRecordByBusinessOrderId(String businessOrderId);

    /**
     * 分页查询风控记录
     * 
     * @param pageable 分页参数
     * @return 风控记录分页结果
     */
    Page<RiskRecord> getRiskRecords(Pageable pageable);

    /**
     * 根据用户ID分页查询风控记录
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 风控记录分页结果
     */
    Page<RiskRecord> getRiskRecordsByUserId(String userId, Pageable pageable);

    /**
     * 查询待审核的风控记录
     * 
     * @param pageable 分页参数
     * @return 待审核的风控记录分页结果
     */
    Page<RiskRecord> getPendingReviewRecords(Pageable pageable);

    /**
     * 审核风控记录
     * 
     * @param recordId 记录ID
     * @param status 审核状态
     * @param reviewer 审核人
     * @param comment 审核备注
     * @return 是否审核成功
     */
    boolean reviewRiskRecord(String recordId, RiskRecord.ReviewStatus status, String reviewer, String comment);

    /**
     * 标记风控记录为误报
     * 
     * @param recordId 记录ID
     * @param reviewer 标记人
     * @param comment 备注
     * @return 是否标记成功
     */
    boolean markAsFalsePositive(String recordId, String reviewer, String comment);

    /**
     * 查询高风险交易记录
     * 
     * @param minRiskScore 最小风险评分
     * @param pageable 分页参数
     * @return 高风险交易记录分页结果
     */
    Page<RiskRecord> getHighRiskRecords(BigDecimal minRiskScore, Pageable pageable);

    /**
     * 获取风控统计数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 风控统计数据
     */
    RiskStatistics getRiskStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 创建风控规则
     * 
     * @param rule 风控规则
     * @return 创建的风控规则
     */
    RiskRule createRiskRule(RiskRule rule);

    /**
     * 更新风控规则
     * 
     * @param ruleId 规则ID
     * @param rule 风控规则
     * @return 更新的风控规则
     */
    RiskRule updateRiskRule(String ruleId, RiskRule rule);

    /**
     * 删除风控规则
     * 
     * @param ruleId 规则ID
     * @return 是否删除成功
     */
    boolean deleteRiskRule(String ruleId);

    /**
     * 根据ID查询风控规则
     * 
     * @param ruleId 规则ID
     * @return 风控规则
     */
    RiskRule getRiskRuleById(String ruleId);

    /**
     * 分页查询风控规则
     * 
     * @param pageable 分页参数
     * @return 风控规则分页结果
     */
    Page<RiskRule> getRiskRules(Pageable pageable);

    /**
     * 查询启用的风控规则
     * 
     * @return 启用的风控规则列表
     */
    List<RiskRule> getEnabledRiskRules();

    /**
     * 启用/禁用风控规则
     * 
     * @param ruleId 规则ID
     * @param enabled 是否启用
     * @return 是否操作成功
     */
    boolean toggleRiskRule(String ruleId, boolean enabled);

    /**
     * 批量处理待审核的风控记录
     * 处理超时的待审核记录，自动通过或拒绝
     * 
     * @param timeoutHours 超时小时数
     * @return 处理的记录数量
     */
    int processPendingReviewRecords(int timeoutHours);

    // ==================== 内部类定义 ====================

    /**
     * 风控检查结果
     */
    class RiskCheckResult {
        /**
         * 风控记录ID
         */
        private String recordId;

        /**
         * 是否通过风控检查
         */
        private boolean passed;

        /**
         * 风险评分
         */
        private BigDecimal riskScore;

        /**
         * 风险等级
         */
        private RiskRule.RiskLevel riskLevel;

        /**
         * 处理结果
         */
        private RiskRecord.RiskResult result;

        /**
         * 处理动作
         */
        private RiskRule.RiskAction action;

        /**
         * 风控原因
         */
        private String reason;

        /**
         * 触发的规则ID列表
         */
        private List<String> triggeredRules;

        /**
         * 处理时间（毫秒）
         */
        private Long processingTimeMs;

        // ==================== 构造函数 ====================

        public RiskCheckResult() {}

        public RiskCheckResult(boolean passed, BigDecimal riskScore, RiskRule.RiskLevel riskLevel,
                              RiskRecord.RiskResult result, RiskRule.RiskAction action, String reason) {
            this.passed = passed;
            this.riskScore = riskScore;
            this.riskLevel = riskLevel;
            this.result = result;
            this.action = action;
            this.reason = reason;
        }

        // ==================== 业务方法 ====================

        /**
         * 检查是否需要人工审核
         * 
         * @return 是否需要人工审核
         */
        public boolean requiresManualReview() {
            return result == RiskRecord.RiskResult.MANUAL_REVIEW;
        }

        /**
         * 检查是否为高风险交易
         * 
         * @return 是否为高风险交易
         */
        public boolean isHighRisk() {
            return riskLevel == RiskRule.RiskLevel.HIGH || riskLevel == RiskRule.RiskLevel.CRITICAL;
        }

        // ==================== Getter和Setter方法 ====================

        public String getRecordId() {
            return recordId;
        }

        public void setRecordId(String recordId) {
            this.recordId = recordId;
        }

        public boolean isPassed() {
            return passed;
        }

        public void setPassed(boolean passed) {
            this.passed = passed;
        }

        public BigDecimal getRiskScore() {
            return riskScore;
        }

        public void setRiskScore(BigDecimal riskScore) {
            this.riskScore = riskScore;
        }

        public RiskRule.RiskLevel getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(RiskRule.RiskLevel riskLevel) {
            this.riskLevel = riskLevel;
        }

        public RiskRecord.RiskResult getResult() {
            return result;
        }

        public void setResult(RiskRecord.RiskResult result) {
            this.result = result;
        }

        public RiskRule.RiskAction getAction() {
            return action;
        }

        public void setAction(RiskRule.RiskAction action) {
            this.action = action;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public List<String> getTriggeredRules() {
            return triggeredRules;
        }

        public void setTriggeredRules(List<String> triggeredRules) {
            this.triggeredRules = triggeredRules;
        }

        public Long getProcessingTimeMs() {
            return processingTimeMs;
        }

        public void setProcessingTimeMs(Long processingTimeMs) {
            this.processingTimeMs = processingTimeMs;
        }
    }

    /**
     * 风控统计数据
     */
    class RiskStatistics {
        /**
         * 总交易数量
         */
        private long totalTransactions;

        /**
         * 风控拦截数量
         */
        private long blockedTransactions;

        /**
         * 人工审核数量
         */
        private long manualReviewTransactions;

        /**
         * 警告通过数量
         */
        private long warningTransactions;

        /**
         * 正常通过数量
         */
        private long passedTransactions;

        /**
         * 拦截率
         */
        private BigDecimal blockRate;

        /**
         * 人工审核率
         */
        private BigDecimal manualReviewRate;

        /**
         * 平均风险评分
         */
        private BigDecimal avgRiskScore;

        /**
         * 最高风险评分
         */
        private BigDecimal maxRiskScore;

        /**
         * 平均处理时间（毫秒）
         */
        private Double avgProcessingTime;

        /**
         * 误报数量
         */
        private long falsePositives;

        /**
         * 误报率
         */
        private BigDecimal falsePositiveRate;

        // ==================== 构造函数 ====================

        public RiskStatistics() {}

        // ==================== Getter和Setter方法 ====================

        public long getTotalTransactions() {
            return totalTransactions;
        }

        public void setTotalTransactions(long totalTransactions) {
            this.totalTransactions = totalTransactions;
        }

        public long getBlockedTransactions() {
            return blockedTransactions;
        }

        public void setBlockedTransactions(long blockedTransactions) {
            this.blockedTransactions = blockedTransactions;
        }

        public long getManualReviewTransactions() {
            return manualReviewTransactions;
        }

        public void setManualReviewTransactions(long manualReviewTransactions) {
            this.manualReviewTransactions = manualReviewTransactions;
        }

        public long getWarningTransactions() {
            return warningTransactions;
        }

        public void setWarningTransactions(long warningTransactions) {
            this.warningTransactions = warningTransactions;
        }

        public long getPassedTransactions() {
            return passedTransactions;
        }

        public void setPassedTransactions(long passedTransactions) {
            this.passedTransactions = passedTransactions;
        }

        public BigDecimal getBlockRate() {
            return blockRate;
        }

        public void setBlockRate(BigDecimal blockRate) {
            this.blockRate = blockRate;
        }

        public BigDecimal getManualReviewRate() {
            return manualReviewRate;
        }

        public void setManualReviewRate(BigDecimal manualReviewRate) {
            this.manualReviewRate = manualReviewRate;
        }

        public BigDecimal getAvgRiskScore() {
            return avgRiskScore;
        }

        public void setAvgRiskScore(BigDecimal avgRiskScore) {
            this.avgRiskScore = avgRiskScore;
        }

        public BigDecimal getMaxRiskScore() {
            return maxRiskScore;
        }

        public void setMaxRiskScore(BigDecimal maxRiskScore) {
            this.maxRiskScore = maxRiskScore;
        }

        public Double getAvgProcessingTime() {
            return avgProcessingTime;
        }

        public void setAvgProcessingTime(Double avgProcessingTime) {
            this.avgProcessingTime = avgProcessingTime;
        }

        public long getFalsePositives() {
            return falsePositives;
        }

        public void setFalsePositives(long falsePositives) {
            this.falsePositives = falsePositives;
        }

        public BigDecimal getFalsePositiveRate() {
            return falsePositiveRate;
        }

        public void setFalsePositiveRate(BigDecimal falsePositiveRate) {
            this.falsePositiveRate = falsePositiveRate;
        }
    }
}