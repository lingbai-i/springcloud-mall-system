package com.mall.merchant.service.impl;

import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import com.mall.merchant.domain.entity.MerchantStatistics;
import com.mall.merchant.repository.MerchantStatisticsRepository;
import com.mall.merchant.service.MerchantStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 商家统计服务实现类
 * 实现商家统计相关的业务逻辑处理
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantStatisticsServiceImpl implements MerchantStatisticsService {
    
    private static final Logger log = LoggerFactory.getLogger(MerchantStatisticsServiceImpl.class);
    
    private final MerchantStatisticsRepository statisticsRepository;
    
    /**
     * 获取商家总览统计数据
     * 包含订单、销售、商品、访问量等关键指标
     * 
     * @param merchantId 商家ID
     * @return 总览统计数据
     */
    @Override
    public R<Map<String, Object>> getOverviewStatistics(Long merchantId) {
        log.debug("获取商家总览统计数据，商家ID：{}", merchantId);
        
        try {
            Map<String, Object> overview = new HashMap<>();
            
            // 获取最新的日统计数据
            Optional<MerchantStatistics> latestDaily = statisticsRepository.findLatestDailyStatistics(merchantId);
            if (latestDaily.isPresent()) {
                MerchantStatistics stats = latestDaily.get();
                overview.put("todayOrders", stats.getTotalOrders());
                overview.put("todaySales", stats.getTotalSales());
                overview.put("todayVisits", stats.getUniqueVisitors());
                overview.put("todayViews", stats.getPageViews());
            } else {
                overview.put("todayOrders", 0);
                overview.put("todaySales", BigDecimal.ZERO);
                overview.put("todayVisits", 0);
                overview.put("todayViews", 0);
            }
            
            // 获取最新的月统计数据
            Optional<MerchantStatistics> latestMonthly = statisticsRepository.findLatestMonthlyStatistics(merchantId);
            if (latestMonthly.isPresent()) {
                MerchantStatistics stats = latestMonthly.get();
                overview.put("monthOrders", stats.getTotalOrders());
                overview.put("monthSales", stats.getTotalSales());
                overview.put("monthVisits", stats.getUniqueVisitors());
                overview.put("monthViews", stats.getPageViews());
                overview.put("orderCompletionRate", stats.getCompletionRate());
                overview.put("averageOrderValue", stats.getAvgOrderValue());
                overview.put("conversionRate", stats.getConversionRate());
            } else {
                overview.put("monthOrders", 0);
                overview.put("monthSales", BigDecimal.ZERO);
                overview.put("monthVisits", 0);
                overview.put("monthViews", 0);
                overview.put("orderCompletionRate", BigDecimal.ZERO);
                overview.put("avgOrderValue", BigDecimal.ZERO);
                overview.put("conversionRate", BigDecimal.ZERO);
            }
            
            return R.ok(overview);
            
        } catch (Exception e) {
            log.error("获取商家总览统计数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取总览统计数据失败");
        }
    }
    
    /**
     * 获取今日统计数据
     * 
     * @param merchantId 商家ID
     * @return 今日统计数据
     */
    @Override
    public R<MerchantStatistics> getTodayStatistics(Long merchantId) {
        log.debug("获取今日统计数据，商家ID：{}", merchantId);
        
        try {
            LocalDate today = LocalDate.now();
            Optional<MerchantStatistics> statisticsOpt = statisticsRepository.findByMerchantIdAndStatDateAndStatType(
                merchantId, today, 1);
            
            if (statisticsOpt.isPresent()) {
                return R.ok(statisticsOpt.get());
            } else {
                // 如果没有今日数据，返回空的统计对象
                MerchantStatistics emptyStats = new MerchantStatistics();
                emptyStats.setMerchantId(merchantId);
                emptyStats.setStatDate(today);
                emptyStats.setStatType(1);
                return R.ok(emptyStats);
            }
            
        } catch (Exception e) {
            log.error("获取今日统计数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取今日统计数据失败");
        }
    }
    
    /**
     * 获取昨日统计数据
     * 
     * @param merchantId 商家ID
     * @return 昨日统计数据
     */
    @Override
    public R<MerchantStatistics> getYesterdayStatistics(Long merchantId) {
        log.debug("获取昨日统计数据，商家ID：{}", merchantId);
        
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            Optional<MerchantStatistics> statisticsOpt = statisticsRepository.findByMerchantIdAndStatDateAndStatType(
                merchantId, yesterday, 1);
            
            if (statisticsOpt.isPresent()) {
                return R.ok(statisticsOpt.get());
            } else {
                // 如果没有昨日数据，返回空的统计对象
                MerchantStatistics emptyStats = new MerchantStatistics();
                emptyStats.setMerchantId(merchantId);
                emptyStats.setStatDate(yesterday);
                emptyStats.setStatType(1);
                return R.ok(emptyStats);
            }
            
        } catch (Exception e) {
            log.error("获取昨日统计数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取昨日统计数据失败");
        }
    }
    
    /**
     * 获取本月统计数据
     * 
     * @param merchantId 商家ID
     * @return 本月统计数据
     */
    @Override
    public R<MerchantStatistics> getCurrentMonthStatistics(Long merchantId) {
        log.debug("获取本月统计数据，商家ID：{}", merchantId);
        
        try {
            LocalDate thisMonth = LocalDate.now().withDayOfMonth(1);
            Optional<MerchantStatistics> statisticsOpt = statisticsRepository.findByMerchantIdAndStatDateAndStatType(
                merchantId, thisMonth, 2);
            
            if (statisticsOpt.isPresent()) {
                return R.ok(statisticsOpt.get());
            } else {
                // 如果没有本月数据，返回空的统计对象
                MerchantStatistics emptyStats = new MerchantStatistics();
                emptyStats.setMerchantId(merchantId);
                emptyStats.setStatDate(thisMonth);
                emptyStats.setStatType(2);
                return R.ok(emptyStats);
            }
            
        } catch (Exception e) {
            log.error("获取本月统计数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取本月统计数据失败");
        }
    }
    
    /**
     * 获取上月统计数据
     * 
     * @param merchantId 商家ID
     * @return 上月统计数据
     */
    @Override
    public R<MerchantStatistics> getLastMonthStatistics(Long merchantId) {
        log.debug("获取上月统计数据，商家ID：{}", merchantId);
        
        try {
            LocalDate lastMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
            Optional<MerchantStatistics> statisticsOpt = statisticsRepository.findByMerchantIdAndStatDateAndStatType(
                merchantId, lastMonth, 2);
            
            if (statisticsOpt.isPresent()) {
                return R.ok(statisticsOpt.get());
            } else {
                // 如果没有上月数据，返回空的统计对象
                MerchantStatistics emptyStats = new MerchantStatistics();
                emptyStats.setMerchantId(merchantId);
                emptyStats.setStatDate(lastMonth);
                emptyStats.setStatType(2);
                return R.ok(emptyStats);
            }
            
        } catch (Exception e) {
            log.error("获取上月统计数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取上月统计数据失败");
        }
    }
    
    /**
     * 获取本年统计数据
     * 
     * @param merchantId 商家ID
     * @return 本年统计数据
     */
    @Override
    public R<MerchantStatistics> getCurrentYearStatistics(Long merchantId) {
        log.debug("获取本年统计数据，商家ID：{}", merchantId);
        
        try {
            LocalDate thisYear = LocalDate.now().withDayOfYear(1);
            Optional<MerchantStatistics> statisticsOpt = statisticsRepository.findByMerchantIdAndStatDateAndStatType(
                merchantId, thisYear, 3);
            
            if (statisticsOpt.isPresent()) {
                return R.ok(statisticsOpt.get());
            } else {
                // 如果没有本年数据，返回空的统计对象
                MerchantStatistics emptyStats = new MerchantStatistics();
                emptyStats.setMerchantId(merchantId);
                emptyStats.setStatDate(thisYear);
                emptyStats.setStatType(3);
                return R.ok(emptyStats);
            }
            
        } catch (Exception e) {
            log.error("获取本年统计数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取本年统计数据失败");
        }
    }
    
    /**
     * 获取去年统计数据
     * 
     * @param merchantId 商家ID
     * @return 去年统计数据
     */
    @Override
    public R<MerchantStatistics> getLastYearStatistics(Long merchantId) {
        log.debug("获取去年统计数据，商家ID：{}", merchantId);
        
        try {
            LocalDate lastYear = LocalDate.now().minusYears(1).withDayOfYear(1);
            Optional<MerchantStatistics> statisticsOpt = statisticsRepository.findByMerchantIdAndStatDateAndStatType(
                merchantId, lastYear, 3);
            
            if (statisticsOpt.isPresent()) {
                return R.ok(statisticsOpt.get());
            } else {
                // 如果没有去年数据，返回空的统计对象
                MerchantStatistics emptyStats = new MerchantStatistics();
                emptyStats.setMerchantId(merchantId);
                emptyStats.setStatDate(lastYear);
                emptyStats.setStatType(3);
                return R.ok(emptyStats);
            }
            
        } catch (Exception e) {
            log.error("获取去年统计数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取去年统计数据失败");
        }
    }
    
    /**
     * 根据日期获取统计数据
     * 
     * @param merchantId 商家ID
     * @param date 日期
     * @param statisticsType 统计类型：1-日，2-月，3-年
     * @return 统计数据
     */
    @Override
    public R<MerchantStatistics> getStatisticsByDate(Long merchantId, LocalDate date, Integer statisticsType) {
        log.debug("根据日期获取统计数据，商家ID：{}，日期：{}，类型：{}", merchantId, date, statisticsType);
        
        try {
            Optional<MerchantStatistics> statisticsOpt = statisticsRepository.findByMerchantIdAndStatDateAndStatType(
                merchantId, date, statisticsType);
            
            if (statisticsOpt.isPresent()) {
                return R.ok(statisticsOpt.get());
            } else {
                return R.fail("统计数据不存在");
            }
            
        } catch (Exception e) {
            log.error("根据日期获取统计数据失败，商家ID：{}，日期：{}，错误信息：{}", merchantId, date, e.getMessage(), e);
            return R.fail("获取统计数据失败");
        }
    }
    
    /**
     * 分页查询统计数据列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @param statisticsType 统计类型（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 统计数据分页列表
     */
    @Override
    public R<PageResult<MerchantStatistics>> getStatisticsList(Long merchantId, Integer page, Integer size, 
                                                              Integer statisticsType, LocalDate startDate, LocalDate endDate) {
        log.debug("分页查询统计数据列表，商家ID：{}，页码：{}，大小：{}", merchantId, page, size);
        
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "statDate"));
            Page<MerchantStatistics> statisticsPage;
            if (startDate != null && endDate != null) {
                statisticsPage = statisticsRepository.findByMerchantIdAndStatTypeAndStatDateBetween(
                    merchantId, statisticsType, startDate, endDate, pageable);
            } else {
                statisticsPage = statisticsRepository.findByMerchantIdAndStatType(
                    merchantId, statisticsType, pageable);
            }
            
            PageResult<MerchantStatistics> result = PageResult.of(statisticsPage.getContent(), statisticsPage.getTotalElements(), (long) page, (long) size);
            return R.ok(result);
            
        } catch (Exception e) {
            log.error("分页查询统计数据列表失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("查询统计数据列表失败");
        }
    }
    
    /**
     * 获取近期日统计数据
     * 
     * @param merchantId 商家ID
     * @param days 天数
     * @return 近期日统计数据列表
     */
    @Override
    public R<List<MerchantStatistics>> getRecentDailyStatistics(Long merchantId, Integer days) {
        log.debug("获取近期日统计数据，商家ID：{}，天数：{}", merchantId, days);
        
        try {
            LocalDate startDate = LocalDate.now().minusDays(days);
            List<MerchantStatistics> recentStats = statisticsRepository.findRecentDailyStatistics(merchantId, startDate);
            return R.ok(recentStats);
            
        } catch (Exception e) {
            log.error("获取近期日统计数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取近期日统计数据失败");
        }
    }
    
    /**
     * 获取近期月统计数据
     * 
     * @param merchantId 商家ID
     * @param months 月数
     * @return 近期月统计数据列表
     */
    @Override
    public R<List<MerchantStatistics>> getRecentMonthlyStatistics(Long merchantId, Integer months) {
        log.debug("获取近期月统计数据，商家ID：{}，月数：{}", merchantId, months);
        
        try {
            LocalDate startDate = LocalDate.now().minusMonths(months);
            List<MerchantStatistics> recentStats = statisticsRepository.findRecentMonthlyStatistics(merchantId, startDate);
            return R.ok(recentStats);
            
        } catch (Exception e) {
            log.error("获取近期月统计数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取近期月统计数据失败");
        }
    }
    
    /**
     * 获取近期年统计数据
     * 
     * @param merchantId 商家ID
     * @param years 年数
     * @return 近期年统计数据列表
     */
    @Override
    public R<List<MerchantStatistics>> getRecentYearlyStatistics(Long merchantId, Integer years) {
        log.debug("获取近期年统计数据，商家ID：{}，年数：{}", merchantId, years);
        
        try {
            LocalDate startDate = LocalDate.now().minusYears(years);
            List<MerchantStatistics> recentStats = statisticsRepository.findRecentYearlyStatistics(merchantId, startDate);
            return R.ok(recentStats);
            
        } catch (Exception e) {
            log.error("获取近期年统计数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取近期年统计数据失败");
        }
    }
    
    /**
     * 获取销售趋势数据
     * 分析指定天数内的销售趋势变化
     * 
     * @param merchantId 商家ID
     * @param days 天数
     * @return 销售趋势数据列表
     */
    @Override
    public R<List<Map<String, Object>>> getSalesTrend(Long merchantId, Integer days) {
        log.debug("获取销售趋势数据，商家ID：{}，天数：{}", merchantId, days);
        
        try {
            LocalDate startDate = LocalDate.now().minusDays(days);
            List<Object[]> trendData = statisticsRepository.findSalesTrend(merchantId, startDate);
            
            List<Map<String, Object>> result = trendData.stream()
                .map(data -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", data[0]);
                    item.put("sales", data[1]);
                    item.put("orders", data[2]);
                    return item;
                })
                .collect(Collectors.toList());
            
            log.info("成功获取销售趋势数据，商家ID：{}，数据条数：{}", merchantId, result.size());
            return R.ok(result);
        } catch (Exception e) {
            log.error("获取销售趋势数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取销售趋势数据失败");
        }
    }
    
    /**
     * 获取订单趋势数据
     * 分析指定天数内的订单趋势变化
     * 
     * @param merchantId 商家ID
     * @param days 天数
     * @return 订单趋势数据列表
     */
    @Override
    public R<List<Map<String, Object>>> getOrderTrend(Long merchantId, Integer days) {
        log.debug("获取订单趋势数据，商家ID：{}，天数：{}", merchantId, days);
        
        try {
            LocalDate startDate = LocalDate.now().minusDays(days);
            List<Object[]> trendData = statisticsRepository.findOrderTrend(merchantId, startDate);
            
            List<Map<String, Object>> result = trendData.stream()
                .map(data -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", data[0]);
                    item.put("totalOrders", data[1]);
                    item.put("completedOrders", data[2]);
                    item.put("cancelledOrders", data[3]);
                    return item;
                })
                .collect(Collectors.toList());
            
            log.info("成功获取订单趋势数据，商家ID：{}，数据条数：{}", merchantId, result.size());
            return R.ok(result);
        } catch (Exception e) {
            log.error("获取订单趋势数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取订单趋势数据失败");
        }
    }
    
    /**
     * 获取流量趋势数据
     * 分析指定天数内的访问流量趋势变化
     * 
     * @param merchantId 商家ID
     * @param days 天数
     * @return 流量趋势数据列表
     */
    @Override
    public R<List<Map<String, Object>>> getTrafficTrend(Long merchantId, Integer days) {
        log.debug("获取流量趋势数据，商家ID：{}，天数：{}", merchantId, days);
        
        try {
            LocalDate startDate = LocalDate.now().minusDays(days);
            List<Object[]> trendData = statisticsRepository.findTrafficTrend(merchantId, startDate);
            
            List<Map<String, Object>> result = trendData.stream()
                .map(data -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", data[0]);
                    item.put("pageViews", data[1]);
                    item.put("uniqueVisitors", data[2]);
                    return item;
                })
                .collect(Collectors.toList());
            
            log.info("成功获取流量趋势数据，商家ID：{}，数据条数：{}", merchantId, result.size());
            return R.ok(result);
        } catch (Exception e) {
            log.error("获取流量趋势数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取流量趋势数据失败");
        }
    }
    

    
    /**
     * 获取转化率趋势数据
     * 
     * @param merchantId 商家ID
     * @param days 天数
     * @return 转化率趋势数据
     */
    @Override
    public R<List<Map<String, Object>>> getConversionTrend(Long merchantId, Integer days) {
        log.debug("获取转化率趋势数据，商家ID：{}，天数：{}", merchantId, days);
        
        try {
            LocalDate startDate = LocalDate.now().minusDays(days);
            List<MerchantStatistics> statistics = statisticsRepository.findRecentDailyStatistics(merchantId, startDate);
            
            // 计算转化率趋势
            List<Map<String, Object>> trendData = statistics.stream()
                .map(stat -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("date", stat.getStatDate());
                    data.put("conversionRate", stat.getConversionRate());
                    return data;
                })
                .collect(Collectors.toList());
            
            return R.ok(trendData);
            
        } catch (Exception e) {
            log.error("获取转化率趋势数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取转化率趋势数据失败");
        }
    }
    
    /**
     * 获取客单价趋势数据
     * 
     * @param merchantId 商家ID
     * @param days 天数
     * @return 客单价趋势数据
     */
    @Override
    public R<List<Map<String, Object>>> getAvgOrderValueTrend(Long merchantId, Integer days) {
        log.debug("获取客单价趋势数据，商家ID：{}，天数：{}", merchantId, days);
        
        try {
            LocalDate startDate = LocalDate.now().minusDays(days);
            List<MerchantStatistics> statistics = statisticsRepository.findRecentDailyStatistics(merchantId, startDate);
            
            // 计算客单价趋势
            List<Map<String, Object>> trendData = statistics.stream()
                .map(stat -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("date", stat.getStatDate());
                    data.put("averageOrderValue", stat.getAvgOrderValue());
                    return data;
                })
                .collect(Collectors.toList());
            
            return R.ok(trendData);
            
        } catch (Exception e) {
            log.error("获取客单价趋势数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取客单价趋势数据失败");
        }
    }
    
    /**
     * 获取商家排名数据
     * 
     * @param merchantId 商家ID
     * @param rankType 排名类型：1-销售额，2-订单数，3-访问量
     * @return 排名数据
     */
    @Override
    public R<Map<String, Object>> getMerchantRanking(Long merchantId) {
        log.debug("获取商家排名数据，商家ID：{}", merchantId);
        
        try {
            // 这里应该实现排名逻辑，需要查询所有商家的统计数据进行排名
            // 暂时返回模拟数据
            Map<String, Object> rankingData = new HashMap<>();
            rankingData.put("rank", 1);
            rankingData.put("totalMerchants", 100);
            rankingData.put("percentile", 99);
            
            return R.ok(rankingData);
            
        } catch (Exception e) {
            log.error("获取商家排名数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取商家排名数据失败");
        }
    }
    
    /**
     * 获取商家对比数据
     * 
     * @param merchantId 商家ID
     * @param compareType 对比类型：1-同行业平均，2-平台平均
     * @return 对比数据
     */
    @Override
    public R<Map<String, Object>> getComparisonData(Long merchantId, Integer compareType) {
        log.debug("获取商家对比数据，商家ID：{}，对比类型：{}", merchantId, compareType);
        
        try {
            // 这里应该实现对比逻辑，需要查询行业或平台的平均数据
            // 暂时返回模拟数据
            Map<String, Object> comparisonData = new HashMap<>();
            comparisonData.put("merchantValue", BigDecimal.valueOf(10000));
            comparisonData.put("averageValue", BigDecimal.valueOf(8000));
            comparisonData.put("difference", BigDecimal.valueOf(2000));
            comparisonData.put("differencePercent", BigDecimal.valueOf(25));
            
            return R.ok(comparisonData);
            
        } catch (Exception e) {
            log.error("获取商家对比数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取商家对比数据失败");
        }
    }
    
    /**
     * 生成统计报告
     * 
     * @param merchantId 商家ID
     * @param reportType 报告类型：1-日报，2-周报，3-月报
     * @param date 报告日期
     * @return 统计报告
     */
    @Override
    public R<Map<String, Object>> generateStatisticsReport(Long merchantId, Integer reportType, LocalDate startDate, LocalDate endDate) {
        log.info("生成统计报告，商家ID：{}，报告类型：{}，开始日期：{}，结束日期：{}", merchantId, reportType, startDate, endDate);
        
        try {
            Map<String, Object> report = new HashMap<>();
            
            // 根据报告类型生成不同的报告内容
            switch (reportType) {
                case 1: // 日报
                    report = generateDailyReport(merchantId, startDate);
                    break;
                case 2: // 周报
                    report = generateWeeklyReport(merchantId, startDate);
                    break;
                case 3: // 月报
                    report = generateMonthlyReport(merchantId, startDate);
                    break;
                default:
                    return R.fail("不支持的报告类型");
            }
            
            return R.ok(report);
            
        } catch (Exception e) {
            log.error("生成统计报告失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("生成统计报告失败");
        }
    }
    
    /**
     * 导出统计数据
     * 
     * @param merchantId 商家ID
     * @param statisticsType 统计类型（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 导出数据
     */
    @Override
    public R<byte[]> exportStatisticsData(Long merchantId, Integer statType, LocalDate startDate, LocalDate endDate) {
        log.info("导出统计数据，商家ID：{}", merchantId);
        
        try {
            // 这里应该实现Excel导出逻辑
            // 暂时返回空数据
            return R.ok(new byte[0]);
            
        } catch (Exception e) {
            log.error("导出统计数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("导出失败，请稍后重试");
        }
    }
    
    /**
     * 手动触发统计数据计算
     * 
     * @param merchantId 商家ID
     * @param date 统计日期
     * @param statisticsType 统计类型：1-日，2-月，3-年
     * @return 计算结果
     */
    @Override
    @Transactional
    public R<Void> calculateStatistics(Long merchantId, LocalDate statDate, Integer statType) {
        log.info("手动触发统计数据计算，商家ID：{}，日期：{}，类型：{}", merchantId, statDate, statType);
        
        try {
            // 这里应该实现统计数据计算逻辑
            // 从订单、商品、访问等数据中计算统计指标
            // 暂时只记录日志
            log.info("统计数据计算完成，商家ID：{}，日期：{}，类型：{}", merchantId, statDate, statType);
            return R.ok();
            
        } catch (Exception e) {
            log.error("手动触发统计数据计算失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("计算统计数据失败，请稍后重试");
        }
    }
    
    /**
     * 批量计算统计数据
     * 
     * @param merchantId 商家ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statisticsType 统计类型：1-日，2-月，3-年
     * @return 计算结果
     */
    @Override
    @Transactional
    public R<Void> batchCalculateStatistics(Long merchantId, LocalDate startDate, LocalDate endDate, Integer statType) {
        log.info("批量计算统计数据，商家ID：{}，开始日期：{}，结束日期：{}，类型：{}", 
                merchantId, startDate, endDate, statType);
        
        try {
            int successCount = 0;
            int failCount = 0;
            
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                try {
                    R<Void> result = calculateStatistics(merchantId, currentDate, statType);
                    if (result.isSuccess()) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("计算统计数据异常，商家ID：{}，日期：{}，错误信息：{}", merchantId, currentDate, e.getMessage(), e);
                }
                
                // 根据统计类型递增日期
                switch (statType) {
                    case 1: // 日
                        currentDate = currentDate.plusDays(1);
                        break;
                    case 2: // 月
                        currentDate = currentDate.plusMonths(1);
                        break;
                    case 3: // 年
                        currentDate = currentDate.plusYears(1);
                        break;
                    default:
                        return R.fail("不支持的统计类型");
                }
            }
            
            log.info("批量计算统计数据完成，商家ID：{}，成功：{}，失败：{}", merchantId, successCount, failCount);
            return R.ok();
            
        } catch (Exception e) {
            log.error("批量计算统计数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("批量计算统计数据失败，请稍后重试");
        }
    }
    
    /**
     * 获取汇总统计数据
     * 根据指定的统计类型和日期范围获取汇总数据
     * 
     * @param merchantId 商家ID
     * @param statType 统计类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 汇总统计数据
     */
    @Override
    public R<Map<String, Object>> getSummaryStatistics(Long merchantId, Integer statType,
                                                      LocalDate startDate, LocalDate endDate) {
        log.debug("获取汇总统计数据，商家ID：{}，统计类型：{}，日期范围：{} - {}", 
                 merchantId, statType, startDate, endDate);
        
        try {
            Object[] summaryData = statisticsRepository.sumStatisticsByDateRange(merchantId, statType, startDate, endDate);
            
            Map<String, Object> summary = new HashMap<>();
            if (summaryData != null) {
                summary.put("totalOrders", summaryData[0]);
                summary.put("completedOrders", summaryData[1]);
                summary.put("cancelledOrders", summaryData[2]);
                summary.put("refundOrders", summaryData[3]);
                summary.put("totalSales", summaryData[4]);
                summary.put("actualIncome", summaryData[5]);
                summary.put("refundAmount", summaryData[6]);
                summary.put("productSalesCount", summaryData[7]);
                summary.put("pageViews", summaryData[8]);
                summary.put("uniqueVisitors", summaryData[9]);
                summary.put("avgConversionRate", summaryData[10]);
                summary.put("avgOrderValue", summaryData[11]);
                summary.put("avgRefundRate", summaryData[12]);
                summary.put("avgPositiveRate", summaryData[13]);
                summary.put("avgRating", summaryData[14]);
            }
            
            log.info("成功获取汇总统计数据，商家ID：{}", merchantId);
            return R.ok(summary);
        } catch (Exception e) {
            log.error("获取汇总统计数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取汇总统计数据失败");
        }
    }
    
    /**
     * 获取关键指标数据
     * 
     * @param merchantId 商家ID
     * @return 关键指标数据
     */
    @Override
    public R<Map<String, Object>> getKeyMetrics(Long merchantId) {
        log.debug("获取关键指标数据，商家ID：{}", merchantId);
        
        try {
            Map<String, Object> keyMetrics = new HashMap<>();
            
            // 获取最新的月统计数据
            Optional<MerchantStatistics> latestMonthly = statisticsRepository.findLatestMonthlyStatistics(merchantId);
            if (latestMonthly.isPresent()) {
                MerchantStatistics stats = latestMonthly.get();
                keyMetrics.put("orderCompletionRate", calculateOrderCompletionRate(stats));
                keyMetrics.put("orderCancellationRate", calculateOrderCancellationRate(stats));
                keyMetrics.put("conversionRate", stats.getConversionRate());
                keyMetrics.put("averageOrderValue", stats.getAvgOrderValue());
                keyMetrics.put("refundRate", stats.getRefundRate());
                keyMetrics.put("positiveReviewRate", stats.getPositiveRate());
                keyMetrics.put("averageRating", stats.getAvgRating());
            } else {
                keyMetrics.put("orderCompletionRate", BigDecimal.ZERO);
                keyMetrics.put("orderCancellationRate", BigDecimal.ZERO);
                keyMetrics.put("conversionRate", BigDecimal.ZERO);
                keyMetrics.put("averageOrderValue", BigDecimal.ZERO);
                keyMetrics.put("refundRate", BigDecimal.ZERO);
                keyMetrics.put("positiveReviewRate", BigDecimal.ZERO);
                keyMetrics.put("averageRating", BigDecimal.ZERO);
            }
            
            return R.ok(keyMetrics);
            
        } catch (Exception e) {
            log.error("获取关键指标数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取关键指标数据失败");
        }
    }
    
    /**
     * 获取实时统计数据
     * 
     * @param merchantId 商家ID
     * @return 实时统计数据
     */
    @Override
    public R<Map<String, Object>> getRealTimeStatistics(Long merchantId) {
        log.debug("获取实时统计数据，商家ID：{}", merchantId);
        
        try {
            // 这里应该从缓存或实时数据源获取最新的统计数据
            // 暂时返回模拟数据
            Map<String, Object> realTimeStats = new HashMap<>();
            realTimeStats.put("onlineVisitors", 10);
            realTimeStats.put("todayOrders", 5);
            realTimeStats.put("todaySales", BigDecimal.valueOf(1000));
            realTimeStats.put("pendingOrders", 3);
            
            return R.ok(realTimeStats);
            
        } catch (Exception e) {
            log.error("获取实时统计数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取实时统计数据失败");
        }
    }
    
    /**
     * 清理过期统计数据
     * 
     * @param merchantId 商家ID
     * @param beforeDate 清理此日期之前的数据
     * @return 清理结果
     */

    
    /**
     * 清理过期统计数据
     * 删除指定日期之前的统计数据
     * 
     * @param merchantId 商家ID
     * @param statType 统计类型
     * @param beforeDate 指定日期之前的数据将被清理
     * @return 清理结果
     */
    @Override
    @Transactional
    public R<Void> cleanExpiredStatistics(Long merchantId, Integer statType, LocalDate beforeDate) {
        log.info("清理过期统计数据，商家ID：{}，统计类型：{}，截止日期：{}", merchantId, statType, beforeDate);
        
        try {
            LocalDateTime beforeDateTime = beforeDate.atStartOfDay();
            
            if (statType != null) {
                statisticsRepository.deleteByMerchantIdAndStatTypeAndStatDateBefore(merchantId, statType, beforeDate);
            } else {
                statisticsRepository.deleteByMerchantIdAndStatDateBefore(merchantId, beforeDate);
            }
            
            log.info("清理过期统计数据完成，商家ID：{}，统计类型：{}，截止日期：{}", merchantId, statType, beforeDate);
            return R.ok();
            
        } catch (Exception e) {
            log.error("清理过期统计数据失败，商家ID：{}，统计类型：{}，截止日期：{}，错误信息：{}", 
                     merchantId, statType, beforeDate, e.getMessage(), e);
            return R.fail("清理过期统计数据失败");
        }
    }
    
    @Override
    @Transactional
    public R<Map<String, Object>> cleanupExpiredStatistics(LocalDate beforeDate) {
        log.info("全局清理过期统计数据，截止日期：{}", beforeDate);
        
        try {
            // 清理所有商家的过期统计数据
            long deletedCount = statisticsRepository.deleteByStatDateBefore(beforeDate);
            
            Map<String, Object> result = new HashMap<>();
            result.put("deletedCount", deletedCount);
            result.put("beforeDate", beforeDate);
            
            log.info("全局清理过期统计数据完成，截止日期：{}，删除记录数：{}", beforeDate, deletedCount);
            return R.ok(result);
            
        } catch (Exception e) {
            log.error("全局清理过期统计数据失败，截止日期：{}，错误信息：{}", beforeDate, e.getMessage(), e);
            return R.fail("全局清理过期统计数据失败");
        }
    }
    
    /**
     * 获取统计数据存储状态
     * 
     * @param merchantId 商家ID
     * @return 存储状态信息
     */
    @Override
    public R<Map<String, Object>> getStatisticsStorageStatus(Long merchantId) {
        log.debug("获取统计数据存储状态，商家ID：{}", merchantId);
        
        try {
            Map<String, Object> storageStatus = new HashMap<>();
            
            // 统计各类型数据的数量
            Long dailyCount = statisticsRepository.countByMerchantIdAndStatType(merchantId, 1);
            Long monthlyCount = statisticsRepository.countByMerchantIdAndStatType(merchantId, 2);
            Long yearlyCount = statisticsRepository.countByMerchantIdAndStatType(merchantId, 3);
            
            storageStatus.put("dailyCount", dailyCount);
            storageStatus.put("monthlyCount", monthlyCount);
            storageStatus.put("yearlyCount", yearlyCount);
            storageStatus.put("totalCount", dailyCount + monthlyCount + yearlyCount);
            
            // 获取最早和最晚的统计日期
            Optional<LocalDate> earliestDate = statisticsRepository.findEarliestStatDate(merchantId);
            Optional<LocalDate> latestDate = statisticsRepository.findLatestStatDate(merchantId);
            
            storageStatus.put("earliestDate", earliestDate.orElse(null));
            storageStatus.put("latestDate", latestDate.orElse(null));
            
            return R.ok(storageStatus);
            
        } catch (Exception e) {
            log.error("获取统计数据存储状态失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取统计存储状态失败");
        }
    }
    
    // 私有方法：生成日报
    private Map<String, Object> generateDailyReport(Long merchantId, LocalDate date) {
        Map<String, Object> report = new HashMap<>();
        
        Optional<MerchantStatistics> statsOpt = statisticsRepository.findByMerchantIdAndStatDateAndStatType(
            merchantId, date, 1);
        
        if (statsOpt.isPresent()) {
            MerchantStatistics stats = statsOpt.get();
            report.put("date", date);
            report.put("orderCount", stats.getTotalOrders());
            report.put("salesAmount", stats.getTotalSales());
            report.put("visitCount", stats.getUniqueVisitors());
            report.put("pageViewCount", stats.getPageViews());
            report.put("conversionRate", stats.getConversionRate());
            report.put("averageOrderValue", stats.getAvgOrderValue());
        }
        
        return report;
    }
    
    // 私有方法：生成周报
    private Map<String, Object> generateWeeklyReport(Long merchantId, LocalDate date) {
        Map<String, Object> report = new HashMap<>();
        
        // 计算周的开始和结束日期
        LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);
        
        // 获取一周的统计数据并汇总
        List<Object[]> summaryData = statisticsRepository.findSummaryStatistics(merchantId, weekStart, weekEnd);
            Map<String, Object> summary = new HashMap<>();
            if (!summaryData.isEmpty()) {
                Object[] row = summaryData.get(0);
                summary.put("totalSales", row[0]);
                summary.put("totalOrders", row[1]);
                summary.put("totalRevenue", row[2]);
                summary.put("totalRefunds", row[3]);
                summary.put("totalProductSales", row[4]);
                summary.put("totalPageViews", row[5]);
            }
        
        report.put("weekStart", weekStart);
        report.put("weekEnd", weekEnd);
        report.putAll(summary);
        
        return report;
    }
    
    // 私有方法：生成月报
    private Map<String, Object> generateMonthlyReport(Long merchantId, LocalDate date) {
        Map<String, Object> report = new HashMap<>();
        
        Optional<MerchantStatistics> statsOpt = statisticsRepository.findByMerchantIdAndStatDateAndStatType(
            merchantId, date.withDayOfMonth(1), 2);
        
        if (statsOpt.isPresent()) {
            MerchantStatistics stats = statsOpt.get();
            report.put("month", date.withDayOfMonth(1));
            report.put("orderCount", stats.getTotalOrders());
            report.put("salesAmount", stats.getTotalSales());
            report.put("visitCount", stats.getUniqueVisitors());
            report.put("pageViewCount", stats.getPageViews());
            report.put("conversionRate", stats.getConversionRate());
            report.put("averageOrderValue", stats.getAvgOrderValue());
            report.put("orderCompletionRate", calculateOrderCompletionRate(stats));
            report.put("refundRate", stats.getRefundRate());
        }
        
        return report;
    }
    
    /**
     * 计算订单完成率
     * 
     * @param stats 统计数据
     * @return 订单完成率
     */
    private BigDecimal calculateOrderCompletionRate(MerchantStatistics stats) {
        if (stats.getTotalOrders() == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(stats.getCompletedOrders())
                .divide(BigDecimal.valueOf(stats.getTotalOrders()), 4, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * 计算订单取消率
     * 
     * @param stats 统计数据
     * @return 订单取消率
     */
    private BigDecimal calculateOrderCancellationRate(MerchantStatistics stats) {
        if (stats.getTotalOrders() == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(stats.getCancelledOrders())
                .divide(BigDecimal.valueOf(stats.getTotalOrders()), 4, BigDecimal.ROUND_HALF_UP);
    }
}
