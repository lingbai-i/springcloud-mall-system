package com.mall.payment.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 支付统计响应DTO
 * 用于返回支付相关的统计数据
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@Data
public class PaymentStatisticsResponse {

    /**
     * 统计时间范围开始
     */
    private LocalDateTime startTime;

    /**
     * 统计时间范围结束
     */
    private LocalDateTime endTime;

    /**
     * 总订单数
     */
    private Long totalOrders;

    /**
     * 成功支付订单数
     */
    private Long successOrders;

    /**
     * 失败支付订单数
     */
    private Long failedOrders;

    /**
     * 待支付订单数
     */
    private Long pendingOrders;

    /**
     * 已取消订单数
     */
    private Long cancelledOrders;

    /**
     * 总支付金额
     */
    private BigDecimal totalAmount;

    /**
     * 成功支付金额
     */
    private BigDecimal successAmount;

    /**
     * 失败支付金额
     */
    private BigDecimal failedAmount;

    /**
     * 待支付金额
     */
    private BigDecimal pendingAmount;

    /**
     * 总手续费
     */
    private BigDecimal totalFee;

    /**
     * 实际收入（成功支付金额 - 手续费）
     */
    private BigDecimal actualIncome;

    /**
     * 支付成功率（百分比）
     */
    private BigDecimal successRate;

    /**
     * 平均订单金额
     */
    private BigDecimal averageOrderAmount;

    /**
     * 平均支付时长（秒）
     */
    private Long averagePaymentDuration;

    /**
     * 各支付方式统计
     */
    private Map<String, PaymentMethodStatistics> paymentMethodStats;

    /**
     * 每日统计数据
     */
    private List<DailyStatistics> dailyStats;

    /**
     * 每小时统计数据
     */
    private List<HourlyStatistics> hourlyStats;

    /**
     * 退款统计
     */
    private RefundStatistics refundStats;

    /**
     * 支付方式统计内部类
     */
    @Data
    public static class PaymentMethodStatistics {
        /**
         * 支付方式
         */
        private String paymentMethod;

        /**
         * 支付方式名称
         */
        private String paymentMethodName;

        /**
         * 订单数量
         */
        private Long orderCount;

        /**
         * 成功订单数量
         */
        private Long successCount;

        /**
         * 支付金额
         */
        private BigDecimal amount;

        /**
         * 成功支付金额
         */
        private BigDecimal successAmount;

        /**
         * 手续费
         */
        private BigDecimal fee;

        /**
         * 成功率
         */
        private BigDecimal successRate;

        /**
         * 占比（按金额）
         */
        private BigDecimal amountRatio;

        /**
         * 占比（按订单数）
         */
        private BigDecimal orderRatio;

        /**
         * 平均处理时长（毫秒）
         */
        private Long averageDuration;

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public String getPaymentMethodName() {
            return paymentMethodName;
        }

        public void setPaymentMethodName(String paymentMethodName) {
            this.paymentMethodName = paymentMethodName;
        }

        public Long getOrderCount() {
            return orderCount;
        }

        public void setOrderCount(Long orderCount) {
            this.orderCount = orderCount;
        }

        public Long getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(Long successCount) {
            this.successCount = successCount;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public BigDecimal getSuccessAmount() {
            return successAmount;
        }

        public void setSuccessAmount(BigDecimal successAmount) {
            this.successAmount = successAmount;
        }

        public BigDecimal getFee() {
            return fee;
        }

        public void setFee(BigDecimal fee) {
            this.fee = fee;
        }

        public BigDecimal getSuccessRate() {
            return successRate;
        }

        public void setSuccessRate(BigDecimal successRate) {
            this.successRate = successRate;
        }

        public BigDecimal getAmountRatio() {
            return amountRatio;
        }

        public void setAmountRatio(BigDecimal amountRatio) {
            this.amountRatio = amountRatio;
        }

        public BigDecimal getOrderRatio() {
            return orderRatio;
        }

        public void setOrderRatio(BigDecimal orderRatio) {
            this.orderRatio = orderRatio;
        }

        public Long getAverageDuration() {
            return averageDuration;
        }

        public void setAverageDuration(Long averageDuration) {
            this.averageDuration = averageDuration;
        }
    }

    /**
     * 每日统计内部类
     */
    @Data
    public static class DailyStatistics {
        /**
         * 日期
         */
        private LocalDate date;

        /**
         * 订单数量
         */
        private Long orderCount;

        /**
         * 成功订单数量
         */
        private Long successCount;

        /**
         * 支付金额
         */
        private BigDecimal amount;

        /**
         * 成功支付金额
         */
        private BigDecimal successAmount;

        /**
         * 手续费
         */
        private BigDecimal fee;

        /**
         * 成功率
         */
        private BigDecimal successRate;

        /**
         * 平均订单金额
         */
        private BigDecimal averageAmount;

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public Long getOrderCount() {
            return orderCount;
        }

        public void setOrderCount(Long orderCount) {
            this.orderCount = orderCount;
        }

        public Long getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(Long successCount) {
            this.successCount = successCount;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public BigDecimal getSuccessAmount() {
            return successAmount;
        }

        public void setSuccessAmount(BigDecimal successAmount) {
            this.successAmount = successAmount;
        }

        public BigDecimal getFee() {
            return fee;
        }

        public void setFee(BigDecimal fee) {
            this.fee = fee;
        }

        public BigDecimal getSuccessRate() {
            return successRate;
        }

        public void setSuccessRate(BigDecimal successRate) {
            this.successRate = successRate;
        }

        public BigDecimal getAverageAmount() {
            return averageAmount;
        }

        public void setAverageAmount(BigDecimal averageAmount) {
            this.averageAmount = averageAmount;
        }
    }

    /**
     * 每小时统计内部类
     */
    @Data
    public static class HourlyStatistics {
        /**
         * 小时（0-23）
         */
        private Integer hour;

        /**
         * 订单数量
         */
        private Long orderCount;

        /**
         * 成功订单数量
         */
        private Long successCount;

        /**
         * 支付金额
         */
        private BigDecimal amount;

        /**
         * 成功支付金额
         */
        private BigDecimal successAmount;

        /**
         * 成功率
         */
        private BigDecimal successRate;

        public Integer getHour() {
            return hour;
        }

        public void setHour(Integer hour) {
            this.hour = hour;
        }

        public Long getOrderCount() {
            return orderCount;
        }

        public void setOrderCount(Long orderCount) {
            this.orderCount = orderCount;
        }

        public Long getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(Long successCount) {
            this.successCount = successCount;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public BigDecimal getSuccessAmount() {
            return successAmount;
        }

        public void setSuccessAmount(BigDecimal successAmount) {
            this.successAmount = successAmount;
        }

        public BigDecimal getSuccessRate() {
            return successRate;
        }

        public void setSuccessRate(BigDecimal successRate) {
            this.successRate = successRate;
        }
    }

    /**
     * 退款统计内部类
     */
    @Data
    public static class RefundStatistics {
        /**
         * 退款订单数
         */
        private Long refundOrders;

        /**
         * 成功退款订单数
         */
        private Long successRefunds;

        /**
         * 失败退款订单数
         */
        private Long failedRefunds;

        /**
         * 处理中退款订单数
         */
        private Long processingRefunds;

        /**
         * 退款总金额
         */
        private BigDecimal totalRefundAmount;

        /**
         * 成功退款金额
         */
        private BigDecimal successRefundAmount;

        /**
         * 退款手续费
         */
        private BigDecimal refundFee;

        /**
         * 退款成功率
         */
        private BigDecimal refundSuccessRate;

        /**
         * 平均退款处理时长（小时）
         */
        private Long averageProcessingHours;

        /**
         * 退款率（退款金额/支付金额）
         */
        private BigDecimal refundRate;
    }

    /**
     * 默认构造函数
     */
    public PaymentStatisticsResponse() {
    }

    /**
     * 计算支付成功率
     */
    public void calculateSuccessRate() {
        if (totalOrders != null && totalOrders > 0) {
            this.successRate = BigDecimal.valueOf(successOrders != null ? successOrders : 0)
                    .divide(BigDecimal.valueOf(totalOrders), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            this.successRate = BigDecimal.ZERO;
        }
    }

    /**
     * 计算平均订单金额
     */
    public void calculateAverageOrderAmount() {
        if (totalOrders != null && totalOrders > 0 && totalAmount != null) {
            this.averageOrderAmount = totalAmount
                    .divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            this.averageOrderAmount = BigDecimal.ZERO;
        }
    }

    /**
     * 计算实际收入
     */
    public void calculateActualIncome() {
        if (successAmount != null && totalFee != null) {
            this.actualIncome = successAmount.subtract(totalFee);
        } else if (successAmount != null) {
            this.actualIncome = successAmount;
        } else {
            this.actualIncome = BigDecimal.ZERO;
        }
    }

    /**
     * 获取统计摘要
     * 
     * @return 统计摘要字符串
     */
    public String getStatisticsSummary() {
        return String.format(
                "统计期间：%s 至 %s，总订单：%d笔，成功：%d笔，成功率：%.2f%%，总金额：%.2f元，实际收入：%.2f元",
                startTime != null ? startTime.toLocalDate() : "未知",
                endTime != null ? endTime.toLocalDate() : "未知",
                totalOrders != null ? totalOrders : 0,
                successOrders != null ? successOrders : 0,
                successRate != null ? successRate : BigDecimal.ZERO,
                totalAmount != null ? totalAmount : BigDecimal.ZERO,
                actualIncome != null ? actualIncome : BigDecimal.ZERO
        );
    }

    /**
     * 获取支付方式排行（按金额）
     * 
     * @return 支付方式排行列表
     */
    public List<PaymentMethodStatistics> getPaymentMethodRankingByAmount() {
        if (paymentMethodStats == null) {
            return List.of();
        }
        
        return paymentMethodStats.values().stream()
                .sorted((a, b) -> {
                    BigDecimal amountA = a.getSuccessAmount() != null ? a.getSuccessAmount() : BigDecimal.ZERO;
                    BigDecimal amountB = b.getSuccessAmount() != null ? b.getSuccessAmount() : BigDecimal.ZERO;
                    return amountB.compareTo(amountA);
                })
                .toList();
    }

    /**
     * 获取支付方式排行（按订单数）
     * 
     * @return 支付方式排行列表
     */
    public List<PaymentMethodStatistics> getPaymentMethodRankingByCount() {
        if (paymentMethodStats == null) {
            return List.of();
        }
        
        return paymentMethodStats.values().stream()
                .sorted((a, b) -> {
                    Long countA = a.getSuccessCount() != null ? a.getSuccessCount() : 0L;
                    Long countB = b.getSuccessCount() != null ? b.getSuccessCount() : 0L;
                    return countB.compareTo(countA);
                })
                .toList();
    }

    /**
     * 获取最佳支付时段
     * 
     * @return 最佳支付时段（小时）
     */
    public Integer getBestPaymentHour() {
        if (hourlyStats == null || hourlyStats.isEmpty()) {
            return null;
        }
        
        return hourlyStats.stream()
                .max((a, b) -> {
                    BigDecimal amountA = a.getSuccessAmount() != null ? a.getSuccessAmount() : BigDecimal.ZERO;
                    BigDecimal amountB = b.getSuccessAmount() != null ? b.getSuccessAmount() : BigDecimal.ZERO;
                    return amountA.compareTo(amountB);
                })
                .map(HourlyStatistics::getHour)
                .orElse(null);
    }

    /**
     * 判断是否有统计数据
     * 
     * @return 如果有统计数据返回true，否则返回false
     */
    public boolean hasData() {
        return totalOrders != null && totalOrders > 0;
    }

    /**
     * 获取增长趋势描述
     * 
     * @return 增长趋势描述
     */
    public String getGrowthTrend() {
        if (dailyStats == null || dailyStats.size() < 2) {
            return "数据不足，无法分析趋势";
        }
        
        // 简单的趋势分析：比较最近几天的数据
        int size = dailyStats.size();
        BigDecimal recentAmount = dailyStats.get(size - 1).getSuccessAmount();
        BigDecimal previousAmount = dailyStats.get(size - 2).getSuccessAmount();
        
        if (recentAmount == null || previousAmount == null) {
            return "数据不完整，无法分析趋势";
        }
        
        if (recentAmount.compareTo(previousAmount) > 0) {
            return "上升趋势";
        } else if (recentAmount.compareTo(previousAmount) < 0) {
            return "下降趋势";
        } else {
            return "平稳趋势";
        }
    }
}