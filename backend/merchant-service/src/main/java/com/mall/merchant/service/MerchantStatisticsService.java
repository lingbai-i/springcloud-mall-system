package com.mall.merchant.service;

import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import com.mall.merchant.domain.entity.MerchantStatistics;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 商家统计服务接口
 * 提供商家统计相关的业务逻辑处理
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
public interface MerchantStatisticsService {
    
    /**
     * 获取商家总览统计数据
     * 
     * @param merchantId 商家ID
     * @return 总览统计数据
     */
    R<Map<String, Object>> getOverviewStatistics(Long merchantId);
    
    /**
     * 获取商家今日统计数据
     * 
     * @param merchantId 商家ID
     * @return 今日统计数据
     */
    R<MerchantStatistics> getTodayStatistics(Long merchantId);
    
    /**
     * 获取商家昨日统计数据
     * 
     * @param merchantId 商家ID
     * @return 昨日统计数据
     */
    R<MerchantStatistics> getYesterdayStatistics(Long merchantId);
    
    /**
     * 获取商家本月统计数据
     * 
     * @param merchantId 商家ID
     * @return 本月统计数据
     */
    R<MerchantStatistics> getCurrentMonthStatistics(Long merchantId);
    
    /**
     * 获取商家上月统计数据
     * 
     * @param merchantId 商家ID
     * @return 上月统计数据
     */
    R<MerchantStatistics> getLastMonthStatistics(Long merchantId);
    
    /**
     * 获取商家本年统计数据
     * 
     * @param merchantId 商家ID
     * @return 本年统计数据
     */
    R<MerchantStatistics> getCurrentYearStatistics(Long merchantId);
    
    /**
     * 获取商家去年统计数据
     * 
     * @param merchantId 商家ID
     * @return 去年统计数据
     */
    R<MerchantStatistics> getLastYearStatistics(Long merchantId);
    
    /**
     * 获取指定日期的统计数据
     * 
     * @param merchantId 商家ID
     * @param statDate 统计日期
     * @param statType 统计类型：1-日统计，2-月统计，3-年统计
     * @return 统计数据
     */
    R<MerchantStatistics> getStatisticsByDate(Long merchantId, LocalDate statDate, Integer statType);
    
    /**
     * 分页查询统计数据列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @param statType 统计类型
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 统计数据分页列表
     */
    R<PageResult<MerchantStatistics>> getStatisticsList(Long merchantId, Integer page, Integer size, 
                                                        Integer statType, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取最近N天的日统计数据
     * 
     * @param merchantId 商家ID
     * @param days 天数
     * @return 日统计数据列表
     */
    R<List<MerchantStatistics>> getRecentDailyStatistics(Long merchantId, Integer days);
    
    /**
     * 获取最近N个月的月统计数据
     * 
     * @param merchantId 商家ID
     * @param months 月数
     * @return 月统计数据列表
     */
    R<List<MerchantStatistics>> getRecentMonthlyStatistics(Long merchantId, Integer months);
    
    /**
     * 获取最近N年的年统计数据
     * 
     * @param merchantId 商家ID
     * @param years 年数
     * @return 年统计数据列表
     */
    R<List<MerchantStatistics>> getRecentYearlyStatistics(Long merchantId, Integer years);
    
    /**
     * 获取销售趋势数据
     * 
     * @param merchantId 商家ID
     * @param days 天数（最近N天）
     * @return 销售趋势数据
     */
    R<List<Map<String, Object>>> getSalesTrend(Long merchantId, Integer days);
    
    /**
     * 获取订单趋势数据
     * 
     * @param merchantId 商家ID
     * @param days 天数（最近N天）
     * @return 订单趋势数据
     */
    R<List<Map<String, Object>>> getOrderTrend(Long merchantId, Integer days);
    
    /**
     * 获取访问量趋势数据
     * 
     * @param merchantId 商家ID
     * @param days 天数（最近N天）
     * @return 访问量趋势数据
     */
    R<List<Map<String, Object>>> getTrafficTrend(Long merchantId, Integer days);
    
    /**
     * 获取转化率趋势数据
     * 
     * @param merchantId 商家ID
     * @param days 天数（最近N天）
     * @return 转化率趋势数据
     */
    R<List<Map<String, Object>>> getConversionTrend(Long merchantId, Integer days);
    
    /**
     * 获取客单价趋势数据
     * 
     * @param merchantId 商家ID
     * @param days 天数（最近N天）
     * @return 客单价趋势数据
     */
    R<List<Map<String, Object>>> getAvgOrderValueTrend(Long merchantId, Integer days);
    
    /**
     * 获取商家排名数据
     * 
     * @param merchantId 商家ID
     * @return 排名数据
     */
    R<Map<String, Object>> getMerchantRanking(Long merchantId);
    
    /**
     * 获取商家对比数据
     * 
     * @param merchantId 商家ID
     * @param compareType 对比类型：1-同行业平均，2-平台平均
     * @return 对比数据
     */
    R<Map<String, Object>> getComparisonData(Long merchantId, Integer compareType);
    
    /**
     * 生成统计报告
     * 
     * @param merchantId 商家ID
     * @param reportType 报告类型：1-日报，2-周报，3-月报，4-年报
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计报告
     */
    R<Map<String, Object>> generateStatisticsReport(Long merchantId, Integer reportType, 
                                                    LocalDate startDate, LocalDate endDate);
    
    /**
     * 导出统计数据
     * 
     * @param merchantId 商家ID
     * @param statType 统计类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 导出数据
     */
    R<byte[]> exportStatisticsData(Long merchantId, Integer statType, LocalDate startDate, LocalDate endDate);
    
    /**
     * 手动触发统计数据计算
     * 
     * @param merchantId 商家ID
     * @param statDate 统计日期
     * @param statType 统计类型
     * @return 计算结果
     */
    R<Void> calculateStatistics(Long merchantId, LocalDate statDate, Integer statType);
    
    /**
     * 批量计算统计数据
     * 
     * @param merchantId 商家ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statType 统计类型
     * @return 计算结果
     */
    R<Void> batchCalculateStatistics(Long merchantId, LocalDate startDate, LocalDate endDate, Integer statType);
    
    /**
     * 获取统计数据汇总
     * 
     * @param merchantId 商家ID
     * @param statType 统计类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 汇总数据
     */
    R<Map<String, Object>> getSummaryStatistics(Long merchantId, Integer statType, 
                                                LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取关键指标数据
     * 
     * @param merchantId 商家ID
     * @return 关键指标数据
     */
    R<Map<String, Object>> getKeyMetrics(Long merchantId);
    
    /**
     * 获取实时统计数据
     * 
     * @param merchantId 商家ID
     * @return 实时统计数据
     */
    R<Map<String, Object>> getRealTimeStatistics(Long merchantId);
    
    /**
     * 清理过期统计数据
     * 
     * @param merchantId 商家ID
     * @param statType 统计类型
     * @param beforeDate 指定日期之前的数据将被清理
     * @return 清理结果
     */
    R<Void> cleanExpiredStatistics(Long merchantId, Integer statType, LocalDate beforeDate);
    
    /**
     * 清理过期统计数据（全局清理）
     * 
     * @param beforeDate 指定日期之前的数据将被清理
     * @return 清理结果
     */
    R<Map<String, Object>> cleanupExpiredStatistics(LocalDate beforeDate);
    
    /**
     * 获取统计数据存储状态
     * 
     * @param merchantId 商家ID
     * @return 存储状态信息
     */
    R<Map<String, Object>> getStatisticsStorageStatus(Long merchantId);
}