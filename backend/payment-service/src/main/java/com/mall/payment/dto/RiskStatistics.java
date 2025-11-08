package com.mall.payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 风险统计数据DTO
 * 用于封装支付风险相关的统计信息
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskStatistics {

    /**
     * 总风险事件数
     */
    private Long totalRiskEvents;

    /**
     * 高风险事件数
     */
    private Long highRiskEvents;

    /**
     * 中风险事件数
     */
    private Long mediumRiskEvents;

    /**
     * 低风险事件数
     */
    private Long lowRiskEvents;

    /**
     * 风险拦截数
     */
    private Long riskBlockedCount;

    /**
     * 风险拦截金额
     */
    private BigDecimal riskBlockedAmount;

    /**
     * 风险拦截率
     */
    private BigDecimal riskBlockedRate;

    /**
     * 误拦截数
     */
    private Long falsePositiveCount;

    /**
     * 误拦截率
     */
    private BigDecimal falsePositiveRate;

    /**
     * 风险类型分布
     */
    private List<RiskTypeStats> riskTypeDistribution;

    /**
     * 风险等级分布
     */
    private List<RiskLevelStats> riskLevelDistribution;

    /**
     * 获取风险拦截率
     * 
     * @return 风险拦截率（百分比）
     */
    public BigDecimal getRiskBlockedRate() {
        if (totalRiskEvents == null || totalRiskEvents == 0 || riskBlockedCount == null) {
            return BigDecimal.ZERO;
        }
        
        return BigDecimal.valueOf(riskBlockedCount)
                .divide(BigDecimal.valueOf(totalRiskEvents), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 获取误拦截率
     * 
     * @return 误拦截率（百分比）
     */
    public BigDecimal getFalsePositiveRate() {
        if (riskBlockedCount == null || riskBlockedCount == 0 || falsePositiveCount == null) {
            return BigDecimal.ZERO;
        }
        
        return BigDecimal.valueOf(falsePositiveCount)
                .divide(BigDecimal.valueOf(riskBlockedCount), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 获取格式化的风险拦截金额
     * 
     * @return 格式化的风险拦截金额字符串
     */
    public String getFormattedRiskBlockedAmount() {
        if (riskBlockedAmount == null) {
            return "0.00元";
        }
        return String.format("%.2f元", riskBlockedAmount);
    }

    /**
     * 获取格式化的风险拦截率
     * 
     * @return 格式化的风险拦截率字符串
     */
    public String getFormattedRiskBlockedRate() {
        BigDecimal rate = getRiskBlockedRate();
        return String.format("%.2f%%", rate);
    }

    /**
     * 获取格式化的误拦截率
     * 
     * @return 格式化的误拦截率字符串
     */
    public String getFormattedFalsePositiveRate() {
        BigDecimal rate = getFalsePositiveRate();
        return String.format("%.2f%%", rate);
    }

    /**
     * 风险类型统计内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskTypeStats {
        /**
         * 风险类型
         */
        private String riskType;

        /**
         * 风险类型名称
         */
        private String riskTypeName;

        /**
         * 事件数量
         */
        private Long eventCount;

        /**
         * 占比
         */
        private BigDecimal ratio;
    }

    /**
     * 风险等级统计内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskLevelStats {
        /**
         * 风险等级
         */
        private String riskLevel;

        /**
         * 风险等级名称
         */
        private String riskLevelName;

        /**
         * 事件数量
         */
        private Long eventCount;

        /**
         * 占比
         */
        private BigDecimal ratio;
    }
}