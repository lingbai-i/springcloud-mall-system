package com.mall.payment.service.impl;

import com.mall.payment.dto.*;
import com.mall.payment.entity.PaymentOrder;
import com.mall.payment.entity.PaymentStatistics;
import com.mall.payment.entity.RefundOrder;
import com.mall.payment.entity.RiskRecord;
import com.mall.payment.entity.RiskRule;
import com.mall.payment.enums.PaymentStatus;
import com.mall.payment.repository.PaymentOrderRepository;
import com.mall.payment.repository.PaymentStatisticsRepository;
import com.mall.payment.repository.RefundOrderRepository;
import com.mall.payment.repository.RiskRecordRepository;
import com.mall.payment.service.PaymentStatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 支付统计服务实现类
 * 提供支付相关的统计分析功能实现
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@Service
@Transactional
public class PaymentStatisticsServiceImpl implements PaymentStatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentStatisticsServiceImpl.class);

    @Autowired
    private PaymentStatisticsRepository statisticsRepository;

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Autowired
    private RefundOrderRepository refundOrderRepository;

    @Autowired
    private RiskRecordRepository riskRecordRepository;

    // ==================== 统计数据更新方法 ====================

    @Override
    public void updatePaymentStatistics(PaymentOrder order) {
        logger.debug("更新支付订单统计，订单ID: {}", order.getId());
        
        try {
            LocalDate orderDate = order.getCreatedAt().toLocalDate();
            
            // 更新日统计
            updateDailyStatistics(order, orderDate);
            
            // 更新月统计
            updateMonthlyStatistics(order, orderDate);
            
            // 更新年统计
            updateYearlyStatistics(order, orderDate);
            
            logger.debug("支付订单统计更新完成，订单ID: {}", order.getId());
            
        } catch (Exception e) {
            logger.error("更新支付订单统计异常，订单ID: {}", order.getId(), e);
            throw e;
        }
    }

    @Override
    public void updateRefundStatistics(RefundOrder refund) {
        logger.debug("更新退款订单统计，退款ID: {}", refund.getId());
        
        try {
            LocalDate refundDate = refund.getCreatedAt().toLocalDate();
            
            // 更新日统计
            updateDailyRefundStatistics(refund, refundDate);
            
            // 更新月统计
            updateMonthlyRefundStatistics(refund, refundDate);
            
            // 更新年统计
            updateYearlyRefundStatistics(refund, refundDate);
            
            logger.debug("退款订单统计更新完成，退款ID: {}", refund.getId());
            
        } catch (Exception e) {
            logger.error("更新退款订单统计异常，退款ID: {}", refund.getId(), e);
            throw e;
        }
    }

    @Override
    public void generateStatistics(LocalDate startDate, LocalDate endDate, PaymentStatistics.StatType statType) {
        logger.info("批量生成统计数据，日期范围: {} - {}, 统计类型: {}", startDate, endDate, statType);
        
        try {
            LocalDate currentDate = startDate;
            int generatedCount = 0;
            
            while (!currentDate.isAfter(endDate)) {
                generateStatisticsForDate(currentDate, statType);
                currentDate = getNextDate(currentDate, statType);
                generatedCount++;
            }
            
            logger.info("批量生成统计数据完成，生成记录数: {}", generatedCount);
            
        } catch (Exception e) {
            logger.error("批量生成统计数据异常", e);
            throw e;
        }
    }

    // ==================== 统计数据查询方法 ====================

    @Override
    @Transactional(readOnly = true)
    public PaymentStatistics getStatistics(LocalDate date, PaymentStatistics.StatType statType, String paymentMethod) {
        logger.debug("查询统计数据，日期: {}, 类型: {}, 支付方式: {}", date, statType, paymentMethod);
        
        Optional<PaymentStatistics> optional;
        if (paymentMethod != null) {
            optional = statisticsRepository.findByStatDateAndStatTypeAndPaymentMethod(date, statType, paymentMethod);
        } else {
            optional = statisticsRepository.findByStatDateAndStatTypeAndPaymentMethodIsNull(date, statType);
        }
        
        return optional.orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentStatistics> getStatistics(PaymentStatistics.StatType statType, String paymentMethod, Pageable pageable) {
        logger.debug("分页查询统计数据，类型: {}, 支付方式: {}", statType, paymentMethod);
        
        if (paymentMethod != null) {
            return statisticsRepository.findByStatTypeAndPaymentMethod(statType, paymentMethod, pageable);
        } else {
            return statisticsRepository.findByStatType(statType, pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentStatistics> getStatistics(LocalDate startDate, LocalDate endDate, 
                                                PaymentStatistics.StatType statType, String paymentMethod) {
        logger.debug("查询日期范围统计数据，日期范围: {} - {}, 类型: {}, 支付方式: {}", 
                    startDate, endDate, statType, paymentMethod);
        
        if (paymentMethod != null) {
            return statisticsRepository.findByStatDateBetweenAndStatTypeAndPaymentMethodOrderByStatDateAsc(
                    startDate, endDate, statType, paymentMethod);
        } else {
            return statisticsRepository.findByStatDateBetweenAndStatTypeOrderByStatDateAsc(
                    startDate, endDate, statType);
        }
    }

    // ==================== 报表数据方法 ====================

    @Override
    @Transactional(readOnly = true)
    public OverviewStatistics getOverviewStatistics() {
        logger.debug("获取概览统计数据");
        
        try {
            LocalDate today = LocalDate.now();
            LocalDate thisMonth = today.withDayOfMonth(1);
            LocalDate thisYear = today.withDayOfYear(1);
            
            // 今日统计
            DailyStats todayStats = getDailyStats(today);
            
            // 本月统计
            MonthlyStats monthStats = getMonthlyStats(thisMonth, today);
            
            // 本年统计
            YearlyStats yearStats = getYearlyStats(thisYear, today);
            
            return new OverviewStatistics(todayStats, monthStats, yearStats);
            
        } catch (Exception e) {
            logger.error("获取概览统计数据异常", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrendData> getPaymentTrend(int days) {
        logger.debug("获取支付趋势数据，天数: {}", days);
        
        try {
            LocalDate startDate = LocalDate.now().minusDays(days - 1);
            List<Object[]> results = statisticsRepository.findRecentTrend(startDate);
            
            return results.stream()
                    .map(row -> new TrendData(
                            (LocalDate) row[0],
                            (Long) row[1],
                            (Long) row[2],
                            (BigDecimal) row[3],
                            (BigDecimal) row[4]
                    ))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("获取支付趋势数据异常", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrendData> getMonthlyPaymentTrend(int months) {
        logger.debug("获取月度支付趋势数据，月数: {}", months);
        
        try {
            LocalDate startDate = LocalDate.now().minusMonths(months - 1).withDayOfMonth(1);
            List<Object[]> results = statisticsRepository.findMonthlyTrend(startDate);
            
            return results.stream()
                    .map(row -> new TrendData(
                            (LocalDate) row[0],
                            (Long) row[1],
                            (Long) row[2],
                            (BigDecimal) row[3],
                            (BigDecimal) row[4]
                    ))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("获取月度支付趋势数据异常", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodStats> getPaymentMethodStatistics(LocalDate date, PaymentStatistics.StatType statType) {
        logger.debug("获取支付方式统计数据，日期: {}, 类型: {}", date, statType);
        
        try {
            List<PaymentStatistics> statistics = statisticsRepository.findPaymentMethodStatsByDate(date, statType);
            
            return statistics.stream()
                    .map(this::convertToPaymentMethodStats)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("获取支付方式统计数据异常", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodRanking> getPaymentMethodRanking(LocalDate startDate, LocalDate endDate, 
                                                            PaymentStatistics.StatType statType, String rankBy) {
        logger.debug("获取支付方式排名数据，日期范围: {} - {}, 类型: {}, 排名依据: {}", 
                    startDate, endDate, statType, rankBy);
        
        try {
            List<Object[]> results;
            String unit;
            
            if ("amount".equals(rankBy)) {
                results = statisticsRepository.findPaymentMethodRankingByAmount(startDate, endDate, statType);
                unit = "元";
            } else {
                results = statisticsRepository.findPaymentMethodRankingByOrders(startDate, endDate, statType);
                unit = "笔";
            }
            
            List<PaymentMethodRanking> rankings = new ArrayList<>();
            for (int i = 0; i < results.size(); i++) {
                Object[] row = results.get(i);
                rankings.add(new PaymentMethodRanking(
                        (String) row[0],
                        (BigDecimal) row[1],
                        unit,
                        i + 1
                ));
            }
            
            return rankings;
            
        } catch (Exception e) {
            logger.error("获取支付方式排名数据异常", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatistics getUserStatistics(LocalDate startDate, LocalDate endDate) {
        logger.debug("获取用户统计数据，日期范围: {} - {}", startDate, endDate);
        
        try {
            UserStatistics stats = new UserStatistics();
            
            // 查询用户相关统计数据
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
            
            // 总用户数（有支付记录的用户）
            Long totalUsers = paymentOrderRepository.countDistinctByUserIdAndCreatedAtBetween(startDateTime, endDateTime);
            stats.setTotalUsers(totalUsers);
            
            // 活跃用户数（成功支付的用户）
            Long activeUsers = paymentOrderRepository.countDistinctByUserIdAndStatusAndCreatedAtBetween(
                    PaymentStatus.SUCCESS, startDateTime, endDateTime);
            stats.setActiveUsers(activeUsers);
            
            // 新用户数（首次支付的用户）
            Long newUsers = calculateNewUsers(startDateTime, endDateTime);
            stats.setNewUsers(newUsers);
            
            // 平均订单金额
            BigDecimal avgOrderAmount = paymentOrderRepository.findAvgPaymentAmountByStatusAndCreatedAtBetween(
                    PaymentStatus.SUCCESS, startDateTime, endDateTime);
            stats.setAvgOrderAmount(avgOrderAmount != null ? avgOrderAmount : BigDecimal.ZERO);
            
            // 平均每用户订单数
            if (activeUsers > 0) {
                Long totalOrders = paymentOrderRepository.countByStatusAndCreatedAtBetween(
                        PaymentStatus.SUCCESS, startDateTime, endDateTime);
                stats.setAvgOrdersPerUser(totalOrders / activeUsers);
            } else {
                stats.setAvgOrdersPerUser(0L);
            }
            
            return stats;
            
        } catch (Exception e) {
            logger.error("获取用户统计数据异常", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RiskStatistics getRiskStatistics(LocalDate startDate, LocalDate endDate) {
        logger.debug("获取风控统计数据，日期范围: {} - {}", startDate, endDate);
        
        try {
            RiskStatistics stats = new RiskStatistics();
            
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
            
            // 总风控检查数
            Long totalRiskChecks = riskRecordRepository.countByCreatedAtBetween(startDateTime, endDateTime);
            stats.setTotalRiskChecks(totalRiskChecks);
            
            // 拦截订单数
            Long blockedOrders = riskRecordRepository.countByResultAndCreatedAtBetween(
                    RiskRecord.RiskResult.BLOCKED, startDateTime, endDateTime);
            stats.setBlockedOrders(blockedOrders);
            
            // 高风险订单数
            Long highRiskOrders = riskRecordRepository.countByRiskLevelAndCreatedAtBetween(
                    RiskRule.RiskLevel.HIGH, startDateTime, endDateTime);
            stats.setHighRiskOrders(highRiskOrders);
            
            // 拦截率
            if (totalRiskChecks > 0) {
                BigDecimal blockRate = new BigDecimal(blockedOrders)
                        .divide(new BigDecimal(totalRiskChecks), 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(new BigDecimal(100));
                stats.setBlockRate(blockRate);
            } else {
                stats.setBlockRate(BigDecimal.ZERO);
            }
            
            // 误报率
            Long falsePositives = riskRecordRepository.countByIsFalsePositiveAndCreatedAtBetween(
                    true, startDateTime, endDateTime);
            if (blockedOrders > 0) {
                BigDecimal falsePositiveRate = new BigDecimal(falsePositives)
                        .divide(new BigDecimal(blockedOrders), 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(new BigDecimal(100));
                stats.setFalsePositiveRate(falsePositiveRate);
            } else {
                stats.setFalsePositiveRate(BigDecimal.ZERO);
            }
            
            return stats;
            
        } catch (Exception e) {
            logger.error("获取风控统计数据异常", e);
            throw e;
        }
    }

    // ==================== 数据导出方法 ====================

    @Override
    @Transactional(readOnly = true)
    public String exportStatisticsReport(LocalDate startDate, LocalDate endDate, 
                                        PaymentStatistics.StatType statType, String format) {
        logger.info("导出统计报表，日期范围: {} - {}, 类型: {}, 格式: {}", startDate, endDate, statType, format);
        
        try {
            // 查询统计数据
            List<PaymentStatistics> statistics = getStatistics(startDate, endDate, statType, null);
            
            // 生成文件名
            String fileName = generateReportFileName(startDate, endDate, statType, format);
            
            // 根据格式导出文件
            if ("excel".equalsIgnoreCase(format)) {
                return exportToExcel(statistics, fileName);
            } else if ("csv".equalsIgnoreCase(format)) {
                return exportToCsv(statistics, fileName);
            } else {
                throw new IllegalArgumentException("不支持的导出格式: " + format);
            }
            
        } catch (Exception e) {
            logger.error("导出统计报表异常", e);
            throw e;
        }
    }

    // ==================== 数据清理方法 ====================

    @Override
    public int cleanExpiredStatistics(int retentionDays, PaymentStatistics.StatType statType) {
        logger.info("清理过期统计数据，保留天数: {}, 统计类型: {}", retentionDays, statType);
        
        try {
            LocalDate cutoffDate = LocalDate.now().minusDays(retentionDays);
            int deletedCount = statisticsRepository.deleteByStatDateBeforeAndStatType(cutoffDate, statType);
            
            logger.info("清理过期统计数据完成，删除记录数: {}", deletedCount);
            return deletedCount;
            
        } catch (Exception e) {
            logger.error("清理过期统计数据异常", e);
            throw e;
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 更新日统计数据
     */
    private void updateDailyStatistics(PaymentOrder order, LocalDate orderDate) {
        // 更新全部支付方式统计
        updateStatistics(orderDate, PaymentStatistics.StatType.DAILY, null, order, null);
        
        // 更新具体支付方式统计
        updateStatistics(orderDate, PaymentStatistics.StatType.DAILY, order.getPaymentMethod().name(), order, null);
    }

    /**
     * 更新月统计数据
     */
    private void updateMonthlyStatistics(PaymentOrder order, LocalDate orderDate) {
        LocalDate monthDate = orderDate.withDayOfMonth(1);
        
        // 更新全部支付方式统计
        updateStatistics(monthDate, PaymentStatistics.StatType.MONTHLY, null, order, null);
        
        // 更新具体支付方式统计
        updateStatistics(monthDate, PaymentStatistics.StatType.MONTHLY, order.getPaymentMethod().name(), order, null);
    }

    /**
     * 更新年统计数据
     */
    private void updateYearlyStatistics(PaymentOrder order, LocalDate orderDate) {
        LocalDate yearDate = orderDate.withDayOfYear(1);
        
        // 更新全部支付方式统计
        updateStatistics(yearDate, PaymentStatistics.StatType.YEARLY, null, order, null);
        
        // 更新具体支付方式统计
        updateStatistics(yearDate, PaymentStatistics.StatType.YEARLY, order.getPaymentMethod().name(), order, null);
    }

    /**
     * 更新日退款统计数据
     */
    private void updateDailyRefundStatistics(RefundOrder refund, LocalDate refundDate) {
        // 更新全部支付方式统计
        updateStatistics(refundDate, PaymentStatistics.StatType.DAILY, null, null, refund);
        
        // 更新具体支付方式统计
        if (refund.getPaymentOrder() != null) {
            updateStatistics(refundDate, PaymentStatistics.StatType.DAILY, 
                           refund.getPaymentOrder().getPaymentMethod().name(), null, refund);
        }
    }

    /**
     * 更新月退款统计数据
     */
    private void updateMonthlyRefundStatistics(RefundOrder refund, LocalDate refundDate) {
        LocalDate monthDate = refundDate.withDayOfMonth(1);
        
        // 更新全部支付方式统计
        updateStatistics(monthDate, PaymentStatistics.StatType.MONTHLY, null, null, refund);
        
        // 更新具体支付方式统计
        if (refund.getPaymentOrder() != null) {
            updateStatistics(monthDate, PaymentStatistics.StatType.MONTHLY, 
                           refund.getPaymentOrder().getPaymentMethod().name(), null, refund);
        }
    }

    /**
     * 更新年退款统计数据
     */
    private void updateYearlyRefundStatistics(RefundOrder refund, LocalDate refundDate) {
        LocalDate yearDate = refundDate.withDayOfYear(1);
        
        // 更新全部支付方式统计
        updateStatistics(yearDate, PaymentStatistics.StatType.YEARLY, null, null, refund);
        
        // 更新具体支付方式统计
        if (refund.getPaymentOrder() != null) {
            updateStatistics(yearDate, PaymentStatistics.StatType.YEARLY, 
                           refund.getPaymentOrder().getPaymentMethod().name(), null, refund);
        }
    }

    /**
     * 更新统计数据
     */
    private void updateStatistics(LocalDate statDate, PaymentStatistics.StatType statType, 
                                 String paymentMethod, PaymentOrder order, RefundOrder refund) {
        Optional<PaymentStatistics> optional;
        if (paymentMethod != null) {
            optional = statisticsRepository.findByStatDateAndStatTypeAndPaymentMethod(statDate, statType, paymentMethod);
        } else {
            optional = statisticsRepository.findByStatDateAndStatTypeAndPaymentMethodIsNull(statDate, statType);
        }
        
        PaymentStatistics statistics;
        if (optional.isPresent()) {
            statistics = optional.get();
        } else {
            // 创建新的统计记录
            String statId = generateStatId(statDate, statType, paymentMethod);
            statistics = new PaymentStatistics(statId, statDate, statType, paymentMethod);
        }
        
        // 更新统计数据
        if (order != null) {
            statistics.updateWithOrder(order);
        }
        if (refund != null) {
            statistics.updateWithRefund(refund);
        }
        
        statisticsRepository.save(statistics);
    }

    /**
     * 生成统计记录ID
     */
    private String generateStatId(LocalDate statDate, PaymentStatistics.StatType statType, String paymentMethod) {
        StringBuilder sb = new StringBuilder();
        sb.append(statDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        sb.append("_").append(statType.name());
        if (paymentMethod != null) {
            sb.append("_").append(paymentMethod);
        }
        return sb.toString();
    }

    /**
     * 为指定日期生成统计数据
     */
    private void generateStatisticsForDate(LocalDate date, PaymentStatistics.StatType statType) {
        LocalDateTime startDateTime = date.atStartOfDay();
        LocalDateTime endDateTime = date.plusDays(1).atStartOfDay();
        
        // 查询该日期的所有支付订单
        List<PaymentOrder> orders = paymentOrderRepository.findByCreatedAtBetween(startDateTime, endDateTime);
        
        // 查询该日期的所有退款订单
        List<RefundOrder> refunds = refundOrderRepository.findByCreatedAtBetween(startDateTime, endDateTime);
        
        // 按支付方式分组统计
        Map<String, List<PaymentOrder>> ordersByMethod = orders.stream()
                .collect(Collectors.groupingBy(order -> order.getPaymentMethod().name()));
        
        Map<String, List<RefundOrder>> refundsByMethod = refunds.stream()
                .filter(refund -> refund.getPaymentOrder() != null)
                .collect(Collectors.groupingBy(refund -> refund.getPaymentOrder().getPaymentMethod().name()));
        
        // 生成全部支付方式统计
        generateStatisticsRecord(date, statType, null, orders, refunds);
        
        // 生成各支付方式统计
        Set<String> allMethods = new HashSet<>();
        allMethods.addAll(ordersByMethod.keySet());
        allMethods.addAll(refundsByMethod.keySet());
        
        for (String method : allMethods) {
            List<PaymentOrder> methodOrders = ordersByMethod.getOrDefault(method, Collections.emptyList());
            List<RefundOrder> methodRefunds = refundsByMethod.getOrDefault(method, Collections.emptyList());
            generateStatisticsRecord(date, statType, method, methodOrders, methodRefunds);
        }
    }

    /**
     * 生成统计记录
     */
    private void generateStatisticsRecord(LocalDate date, PaymentStatistics.StatType statType, 
                                        String paymentMethod, List<PaymentOrder> orders, List<RefundOrder> refunds) {
        String statId = generateStatId(date, statType, paymentMethod);
        PaymentStatistics statistics = new PaymentStatistics(statId, date, statType, paymentMethod);
        
        // 统计支付订单数据
        for (PaymentOrder order : orders) {
            statistics.updateWithOrder(order);
        }
        
        // 统计退款订单数据
        for (RefundOrder refund : refunds) {
            statistics.updateWithRefund(refund);
        }
        
        statisticsRepository.save(statistics);
    }

    /**
     * 获取下一个统计日期
     */
    private LocalDate getNextDate(LocalDate currentDate, PaymentStatistics.StatType statType) {
        switch (statType) {
            case DAILY:
                return currentDate.plusDays(1);
            case MONTHLY:
                return currentDate.plusMonths(1);
            case YEARLY:
                return currentDate.plusYears(1);
            default:
                return currentDate.plusDays(1);
        }
    }

    /**
     * 获取日统计数据
     */
    private DailyStats getDailyStats(LocalDate date) {
        PaymentStatistics stats = getStatistics(date, PaymentStatistics.StatType.DAILY, null);
        if (stats == null) {
            return new DailyStats();
        }
        
        DailyStats dailyStats = new DailyStats();
        dailyStats.setTotalOrders(stats.getTotalOrders());
        dailyStats.setSuccessOrders(stats.getSuccessOrders());
        dailyStats.setTotalAmount(stats.getTotalAmount());
        dailyStats.setSuccessAmount(stats.getSuccessAmount());
        dailyStats.setSuccessRate(stats.getSuccessRate());
        dailyStats.setRefundOrders(stats.getRefundOrders());
        dailyStats.setRefundAmount(stats.getRefundAmount());
        
        return dailyStats;
    }

    /**
     * 获取月统计数据
     */
    private MonthlyStats getMonthlyStats(LocalDate startDate, LocalDate endDate) {
        BigDecimal totalAmount = statisticsRepository.sumTotalAmountByDateRangeAndStatType(
                startDate, endDate, PaymentStatistics.StatType.DAILY);
        BigDecimal successAmount = statisticsRepository.sumSuccessAmountByDateRangeAndStatType(
                startDate, endDate, PaymentStatistics.StatType.DAILY);
        Long totalOrders = statisticsRepository.sumTotalOrdersByDateRangeAndStatType(
                startDate, endDate, PaymentStatistics.StatType.DAILY);
        Long successOrders = statisticsRepository.sumSuccessOrdersByDateRangeAndStatType(
                startDate, endDate, PaymentStatistics.StatType.DAILY);
        BigDecimal refundAmount = statisticsRepository.sumRefundAmountByDateRangeAndStatType(
                startDate, endDate, PaymentStatistics.StatType.DAILY);
        
        MonthlyStats monthlyStats = new MonthlyStats();
        monthlyStats.setTotalOrders(totalOrders);
        monthlyStats.setSuccessOrders(successOrders);
        monthlyStats.setTotalAmount(totalAmount);
        monthlyStats.setSuccessAmount(successAmount);
        monthlyStats.setRefundAmount(refundAmount);
        
        // 计算成功率
        if (totalOrders > 0) {
            BigDecimal successRate = new BigDecimal(successOrders)
                    .divide(new BigDecimal(totalOrders), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal(100));
            monthlyStats.setSuccessRate(successRate);
        } else {
            monthlyStats.setSuccessRate(BigDecimal.ZERO);
        }
        
        return monthlyStats;
    }

    /**
     * 获取年统计数据
     */
    private YearlyStats getYearlyStats(LocalDate startDate, LocalDate endDate) {
        MonthlyStats monthlyStats = getMonthlyStats(startDate, endDate);
        
        YearlyStats yearlyStats = new YearlyStats();
        yearlyStats.setTotalOrders(monthlyStats.getTotalOrders());
        yearlyStats.setSuccessOrders(monthlyStats.getSuccessOrders());
        yearlyStats.setTotalAmount(monthlyStats.getTotalAmount());
        yearlyStats.setSuccessAmount(monthlyStats.getSuccessAmount());
        yearlyStats.setSuccessRate(monthlyStats.getSuccessRate());
        yearlyStats.setRefundAmount(monthlyStats.getRefundAmount());
        
        // 获取手续费金额
        BigDecimal feeAmount = statisticsRepository.sumFeeAmountByDateRangeAndStatType(
                startDate, endDate, PaymentStatistics.StatType.DAILY);
        yearlyStats.setFeeAmount(feeAmount);
        
        return yearlyStats;
    }

    /**
     * 转换为支付方式统计数据
     */
    private PaymentMethodStats convertToPaymentMethodStats(PaymentStatistics statistics) {
        PaymentMethodStats stats = new PaymentMethodStats();
        stats.setPaymentMethod(statistics.getPaymentMethod());
        stats.setTotalOrders(statistics.getTotalOrders());
        stats.setSuccessOrders(statistics.getSuccessOrders());
        stats.setTotalAmount(statistics.getTotalAmount());
        stats.setSuccessAmount(statistics.getSuccessAmount());
        stats.setSuccessRate(statistics.getSuccessRate());
        stats.setAvgAmount(statistics.getAvgAmount());
        
        return stats;
    }

    /**
     * 计算新用户数
     */
    private Long calculateNewUsers(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // 查询在指定时间范围内首次支付的用户
        List<String> userIds = paymentOrderRepository.findDistinctUserIdByCreatedAtBetween(startDateTime, endDateTime);
        
        long newUserCount = 0;
        for (String userId : userIds) {
            // 检查用户是否在此时间范围之前有支付记录
            Long previousOrderCount = paymentOrderRepository.countByUserIdAndCreatedAtBefore(userId, startDateTime);
            if (previousOrderCount == 0) {
                newUserCount++;
            }
        }
        
        return newUserCount;
    }

    /**
     * 生成报表文件名
     */
    private String generateReportFileName(LocalDate startDate, LocalDate endDate, 
                                        PaymentStatistics.StatType statType, String format) {
        return String.format("payment_statistics_%s_%s_%s.%s",
                statType.name().toLowerCase(),
                startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                format.toLowerCase());
    }

    /**
     * 导出到Excel文件
     */
    private String exportToExcel(List<PaymentStatistics> statistics, String fileName) {
        // TODO: 实现Excel导出逻辑
        logger.info("导出Excel文件: {}", fileName);
        return "/tmp/" + fileName;
    }

    /**
     * 导出到CSV文件
     */
    private String exportToCsv(List<PaymentStatistics> statistics, String fileName) {
        // TODO: 实现CSV导出逻辑
        logger.info("导出CSV文件: {}", fileName);
        return "/tmp/" + fileName;
    }
}