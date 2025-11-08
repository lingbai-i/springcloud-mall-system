package com.mall.payment.service;

import com.mall.payment.entity.PaymentOrder;
import com.mall.payment.entity.PaymentStatistics;
import com.mall.payment.entity.RefundOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 支付统计服务接口
 * 提供支付相关的统计分析功能
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
public interface PaymentStatisticsService {

    // ==================== 统计数据更新方法 ====================

    /**
     * 更新支付订单统计
     * 当支付订单状态发生变化时调用此方法更新统计数据
     * 
     * @param order 支付订单
     */
    void updatePaymentStatistics(PaymentOrder order);

    /**
     * 更新退款订单统计
     * 当退款订单状态发生变化时调用此方法更新统计数据
     * 
     * @param refund 退款订单
     */
    void updateRefundStatistics(RefundOrder refund);

    /**
     * 批量生成统计数据
     * 用于定时任务或数据修复
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statType 统计类型
     */
    void generateStatistics(LocalDate startDate, LocalDate endDate, PaymentStatistics.StatType statType);

    // ==================== 统计数据查询方法 ====================

    /**
     * 获取指定日期的统计数据
     * 
     * @param date 统计日期
     * @param statType 统计类型
     * @param paymentMethod 支付方式（可选）
     * @return 统计数据
     */
    PaymentStatistics getStatistics(LocalDate date, PaymentStatistics.StatType statType, String paymentMethod);

    /**
     * 分页查询统计数据
     * 
     * @param statType 统计类型
     * @param paymentMethod 支付方式（可选）
     * @param pageable 分页参数
     * @return 统计数据分页结果
     */
    Page<PaymentStatistics> getStatistics(PaymentStatistics.StatType statType, String paymentMethod, Pageable pageable);

    /**
     * 查询指定日期范围的统计数据
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statType 统计类型
     * @param paymentMethod 支付方式（可选）
     * @return 统计数据列表
     */
    List<PaymentStatistics> getStatistics(LocalDate startDate, LocalDate endDate, 
                                         PaymentStatistics.StatType statType, String paymentMethod);

    // ==================== 报表数据方法 ====================

    /**
     * 获取概览统计数据
     * 包含今日、本月、本年的关键指标
     * 
     * @return 概览统计数据
     */
    OverviewStatistics getOverviewStatistics();

    /**
     * 获取交易趋势数据
     * 
     * @param days 最近天数
     * @return 交易趋势数据
     */
    List<TrendData> getPaymentTrend(int days);

    /**
     * 获取月度交易趋势数据
     * 
     * @param months 最近月数
     * @return 月度交易趋势数据
     */
    List<TrendData> getMonthlyPaymentTrend(int months);

    /**
     * 获取支付方式统计数据
     * 
     * @param date 统计日期
     * @param statType 统计类型
     * @return 支付方式统计数据
     */
    List<PaymentMethodStats> getPaymentMethodStatistics(LocalDate date, PaymentStatistics.StatType statType);

    /**
     * 获取支付方式排名数据
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statType 统计类型
     * @param rankBy 排名依据（amount/orders）
     * @return 支付方式排名数据
     */
    List<PaymentMethodRanking> getPaymentMethodRanking(LocalDate startDate, LocalDate endDate, 
                                                      PaymentStatistics.StatType statType, String rankBy);

    /**
     * 获取用户统计数据
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 用户统计数据
     */
    UserStatistics getUserStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 获取风控统计数据
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 风控统计数据
     */
    RiskStatistics getRiskStatistics(LocalDate startDate, LocalDate endDate);

    // ==================== 数据导出方法 ====================

    /**
     * 导出统计报表
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statType 统计类型
     * @param format 导出格式（excel/csv）
     * @return 导出文件路径
     */
    String exportStatisticsReport(LocalDate startDate, LocalDate endDate, 
                                 PaymentStatistics.StatType statType, String format);

    // ==================== 数据清理方法 ====================

    /**
     * 清理过期统计数据
     * 
     * @param retentionDays 保留天数
     * @param statType 统计类型
     * @return 清理的记录数
     */
    int cleanExpiredStatistics(int retentionDays, PaymentStatistics.StatType statType);

    // ==================== 内部类定义 ====================

    /**
     * 概览统计数据
     */
    class OverviewStatistics {
        private DailyStats today;
        private MonthlyStats thisMonth;
        private YearlyStats thisYear;

        public OverviewStatistics() {}

        public OverviewStatistics(DailyStats today, MonthlyStats thisMonth, YearlyStats thisYear) {
            this.today = today;
            this.thisMonth = thisMonth;
            this.thisYear = thisYear;
        }

        // Getters and Setters
        public DailyStats getToday() { return today; }
        public void setToday(DailyStats today) { this.today = today; }
        public MonthlyStats getThisMonth() { return thisMonth; }
        public void setThisMonth(MonthlyStats thisMonth) { this.thisMonth = thisMonth; }
        public YearlyStats getThisYear() { return thisYear; }
        public void setThisYear(YearlyStats thisYear) { this.thisYear = thisYear; }
    }

    /**
     * 日统计数据
     */
    class DailyStats {
        private Long totalOrders;
        private Long successOrders;
        private BigDecimal totalAmount;
        private BigDecimal successAmount;
        private BigDecimal successRate;
        private Long refundOrders;
        private BigDecimal refundAmount;

        public DailyStats() {}

        // Getters and Setters
        public Long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }
        public Long getSuccessOrders() { return successOrders; }
        public void setSuccessOrders(Long successOrders) { this.successOrders = successOrders; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public BigDecimal getSuccessAmount() { return successAmount; }
        public void setSuccessAmount(BigDecimal successAmount) { this.successAmount = successAmount; }
        public BigDecimal getSuccessRate() { return successRate; }
        public void setSuccessRate(BigDecimal successRate) { this.successRate = successRate; }
        public Long getRefundOrders() { return refundOrders; }
        public void setRefundOrders(Long refundOrders) { this.refundOrders = refundOrders; }
        public BigDecimal getRefundAmount() { return refundAmount; }
        public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    }

    /**
     * 月统计数据
     */
    class MonthlyStats extends DailyStats {
        private Long uniqueUsers;
        private Long newUsers;

        public MonthlyStats() {}

        // Getters and Setters
        public Long getUniqueUsers() { return uniqueUsers; }
        public void setUniqueUsers(Long uniqueUsers) { this.uniqueUsers = uniqueUsers; }
        public Long getNewUsers() { return newUsers; }
        public void setNewUsers(Long newUsers) { this.newUsers = newUsers; }
    }

    /**
     * 年统计数据
     */
    class YearlyStats extends MonthlyStats {
        private BigDecimal feeAmount;
        private Long riskBlockedOrders;

        public YearlyStats() {}

        // Getters and Setters
        public BigDecimal getFeeAmount() { return feeAmount; }
        public void setFeeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; }
        public Long getRiskBlockedOrders() { return riskBlockedOrders; }
        public void setRiskBlockedOrders(Long riskBlockedOrders) { this.riskBlockedOrders = riskBlockedOrders; }
    }

    /**
     * 趋势数据
     */
    class TrendData {
        private LocalDate date;
        private Long totalOrders;
        private Long successOrders;
        private BigDecimal totalAmount;
        private BigDecimal successAmount;
        private BigDecimal successRate;

        public TrendData() {}

        public TrendData(LocalDate date, Long totalOrders, Long successOrders, 
                        BigDecimal totalAmount, BigDecimal successAmount) {
            this.date = date;
            this.totalOrders = totalOrders;
            this.successOrders = successOrders;
            this.totalAmount = totalAmount;
            this.successAmount = successAmount;
            // 计算成功率
            if (totalOrders > 0) {
                this.successRate = new BigDecimal(successOrders)
                        .divide(new BigDecimal(totalOrders), 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(new BigDecimal(100));
            } else {
                this.successRate = BigDecimal.ZERO;
            }
        }

        // Getters and Setters
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public Long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }
        public Long getSuccessOrders() { return successOrders; }
        public void setSuccessOrders(Long successOrders) { this.successOrders = successOrders; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public BigDecimal getSuccessAmount() { return successAmount; }
        public void setSuccessAmount(BigDecimal successAmount) { this.successAmount = successAmount; }
        public BigDecimal getSuccessRate() { return successRate; }
        public void setSuccessRate(BigDecimal successRate) { this.successRate = successRate; }
    }

    /**
     * 支付方式统计数据
     */
    class PaymentMethodStats {
        private String paymentMethod;
        private Long totalOrders;
        private Long successOrders;
        private BigDecimal totalAmount;
        private BigDecimal successAmount;
        private BigDecimal successRate;
        private BigDecimal avgAmount;

        public PaymentMethodStats() {}

        // Getters and Setters
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public Long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }
        public Long getSuccessOrders() { return successOrders; }
        public void setSuccessOrders(Long successOrders) { this.successOrders = successOrders; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public BigDecimal getSuccessAmount() { return successAmount; }
        public void setSuccessAmount(BigDecimal successAmount) { this.successAmount = successAmount; }
        public BigDecimal getSuccessRate() { return successRate; }
        public void setSuccessRate(BigDecimal successRate) { this.successRate = successRate; }
        public BigDecimal getAvgAmount() { return avgAmount; }
        public void setAvgAmount(BigDecimal avgAmount) { this.avgAmount = avgAmount; }
    }

    /**
     * 支付方式排名数据
     */
    class PaymentMethodRanking {
        private String paymentMethod;
        private BigDecimal value;
        private String unit;
        private int rank;

        public PaymentMethodRanking() {}

        public PaymentMethodRanking(String paymentMethod, BigDecimal value, String unit, int rank) {
            this.paymentMethod = paymentMethod;
            this.value = value;
            this.unit = unit;
            this.rank = rank;
        }

        // Getters and Setters
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public BigDecimal getValue() { return value; }
        public void setValue(BigDecimal value) { this.value = value; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public int getRank() { return rank; }
        public void setRank(int rank) { this.rank = rank; }
    }

    /**
     * 用户统计数据
     */
    class UserStatistics {
        private Long totalUsers;
        private Long activeUsers;
        private Long newUsers;
        private BigDecimal avgOrderAmount;
        private Long avgOrdersPerUser;

        public UserStatistics() {}

        // Getters and Setters
        public Long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }
        public Long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(Long activeUsers) { this.activeUsers = activeUsers; }
        public Long getNewUsers() { return newUsers; }
        public void setNewUsers(Long newUsers) { this.newUsers = newUsers; }
        public BigDecimal getAvgOrderAmount() { return avgOrderAmount; }
        public void setAvgOrderAmount(BigDecimal avgOrderAmount) { this.avgOrderAmount = avgOrderAmount; }
        public Long getAvgOrdersPerUser() { return avgOrdersPerUser; }
        public void setAvgOrdersPerUser(Long avgOrdersPerUser) { this.avgOrdersPerUser = avgOrdersPerUser; }
    }

    /**
     * 风控统计数据
     */
    class RiskStatistics {
        private Long totalRiskChecks;
        private Long blockedOrders;
        private Long highRiskOrders;
        private BigDecimal blockRate;
        private BigDecimal falsePositiveRate;

        public RiskStatistics() {}

        // Getters and Setters
        public Long getTotalRiskChecks() { return totalRiskChecks; }
        public void setTotalRiskChecks(Long totalRiskChecks) { this.totalRiskChecks = totalRiskChecks; }
        public Long getBlockedOrders() { return blockedOrders; }
        public void setBlockedOrders(Long blockedOrders) { this.blockedOrders = blockedOrders; }
        public Long getHighRiskOrders() { return highRiskOrders; }
        public void setHighRiskOrders(Long highRiskOrders) { this.highRiskOrders = highRiskOrders; }
        public BigDecimal getBlockRate() { return blockRate; }
        public void setBlockRate(BigDecimal blockRate) { this.blockRate = blockRate; }
        public BigDecimal getFalsePositiveRate() { return falsePositiveRate; }
        public void setFalsePositiveRate(BigDecimal falsePositiveRate) { this.falsePositiveRate = falsePositiveRate; }
    }
}