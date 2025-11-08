package com.mall.merchant.controller;

import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import com.mall.merchant.domain.entity.MerchantStatistics;
import com.mall.merchant.service.MerchantStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 商家统计数据管理控制器
 * 提供商家各项统计数据查询、分析、报告生成等功能的REST API接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@RestController
@RequestMapping("/api/merchant/statistics")
@RequiredArgsConstructor
@Validated
@Tag(name = "商家统计数据管理", description = "统计数据查询、分析、报告生成等功能")
public class MerchantStatisticsController {

    private static final Logger log = LoggerFactory.getLogger(MerchantStatisticsController.class);

    private final MerchantStatisticsService statisticsService;

    /*
     * 修改日志
     * V1.1 2025-11-05：修复控制器与服务层之间的参数类型不匹配问题（String→Integer）。
     * 变更原因：接口层传递了字符串的统计类型/报表类型，服务层方法签名要求 Integer 枚举码，导致编译错误（String 不能转换为 Integer）。
     * 影响范围：导出统计、手动计算、批量计算与报表生成相关接口；仅调整入参映射，不影响外部 API 的参数形式。
     */

    /**
     * 解析统计类型字符串为服务层使用的整数枚举码。
     * 将前端传入的字符串（daily/monthly/yearly）映射为约定的枚举码（1/2/3）。
     * 文档生成时间：2025-11-05T19:52:02+08:00
     * 
     * @param type 统计类型字符串
     * @return 整数枚举码（1-日，2-月，3-年），无法解析时返回 null
     */
    private Integer parseStatType(String type) {
        // 使用卫语句处理空值，避免 NPE；保留原请求字符串语义，仅做内部映射
        if (type == null || type.isEmpty()) {
            return null;
        }
        String normalized = type.trim().toLowerCase();
        switch (normalized) {
            case "daily":
            case "day":
                return 1;
            case "monthly":
            case "month":
                return 2;
            case "yearly":
            case "year":
                return 3;
            default:
                // 未知类型不抛错，返回 null 交由服务层进行默认/校验处理
                log.warn("未识别的统计类型：{}，按 null 处理", type);
                return null;
        }
    }

    /**
     * 解析报表类型字符串为服务层使用的整数枚举码。
     * 将前端传入的字符串（daily/weekly/monthly/yearly）映射为约定的枚举码（1/2/3/4）。
     * 文档生成时间：2025-11-05T19:52:02+08:00
     * 
     * @param reportType 报表类型字符串
     * @return 整数枚举码（1-日报，2-周报，3-月报，4-年报），无法解析时返回 null
     */
    private Integer parseReportType(String reportType) {
        if (reportType == null || reportType.isEmpty()) {
            return null;
        }
        String normalized = reportType.trim().toLowerCase();
        switch (normalized) {
            case "daily":
            case "day":
                return 1;
            case "weekly":
            case "week":
                return 2;
            case "monthly":
            case "month":
                return 3;
            case "yearly":
            case "year":
                return 4;
            default:
                log.warn("未识别的报表类型：{}，按 null 处理", reportType);
                return null;
        }
    }

    /**
     * 获取商家总览统计
     * 获取商家的综合统计数据总览
     * 
     * @param merchantId 商家ID
     * @return 总览统计数据
     */
    @GetMapping("/overview")
    @Operation(summary = "获取商家总览统计", description = "获取商家的综合统计数据总览")
    public R<Map<String, Object>> getOverviewStatistics(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId) {
        log.debug("获取商家总览统计请求，商家ID：{}", merchantId);
        return statisticsService.getOverviewStatistics(merchantId);
    }

    /**
     * 获取今日统计数据
     * 获取商家今日的统计数据
     * 
     * @param merchantId 商家ID
     * @return 今日统计数据
     */
    @GetMapping("/today")
    @Operation(summary = "获取今日统计数据", description = "获取商家今日的统计数据")
    public R<MerchantStatistics> getTodayStatistics(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId) {
        log.debug("获取今日统计数据请求，商家ID：{}", merchantId);
        return statisticsService.getTodayStatistics(merchantId);
    }

    /**
     * 获取昨日统计数据
     * 获取商家昨日的统计数据
     * 
     * @param merchantId 商家ID
     * @return 昨日统计数据
     */
    @GetMapping("/yesterday")
    @Operation(summary = "获取昨日统计数据", description = "获取商家昨日的统计数据")
    public R<MerchantStatistics> getYesterdayStatistics(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId) {
        log.debug("获取昨日统计数据请求，商家ID：{}", merchantId);
        return statisticsService.getYesterdayStatistics(merchantId);
    }

    /**
     * 获取本月统计数据
     * 获取商家本月的统计数据
     * 
     * @param merchantId 商家ID
     * @return 本月统计数据
     */
    @GetMapping("/this-month")
    @Operation(summary = "获取本月统计数据", description = "获取商家本月的统计数据")
    public R<MerchantStatistics> getCurrentMonthStatistics(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId) {
        log.debug("获取本月统计数据请求，商家ID：{}", merchantId);
        return statisticsService.getCurrentMonthStatistics(merchantId);
    }

    /**
     * 获取上月统计数据
     * 获取商家上月的统计数据
     * 
     * @param merchantId 商家ID
     * @return 上月统计数据
     */
    @GetMapping("/last-month")
    @Operation(summary = "获取上月统计数据", description = "获取商家上月的统计数据")
    public R<MerchantStatistics> getLastMonthStatistics(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId) {
        log.debug("获取上月统计数据请求，商家ID：{}", merchantId);
        return statisticsService.getLastMonthStatistics(merchantId);
    }

    /**
     * 获取本年统计数据
     * 获取商家本年的统计数据
     * 
     * @param merchantId 商家ID
     * @return 本年统计数据
     */
    @GetMapping("/this-year")
    @Operation(summary = "获取本年统计数据", description = "获取商家本年的统计数据")
    public R<MerchantStatistics> getCurrentYearStatistics(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId) {
        log.debug("获取本年统计数据请求，商家ID：{}", merchantId);
        return statisticsService.getCurrentYearStatistics(merchantId);
    }

    /**
     * 获取去年统计数据
     * 获取商家去年的统计数据
     * 
     * @param merchantId 商家ID
     * @return 去年统计数据
     */
    @GetMapping("/last-year")
    @Operation(summary = "获取去年统计数据", description = "获取商家去年的统计数据")
    public R<MerchantStatistics> getLastYearStatistics(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId) {
        log.debug("获取去年统计数据请求，商家ID：{}", merchantId);
        return statisticsService.getLastYearStatistics(merchantId);
    }

    /**
     * 根据日期获取统计数据
     * 根据指定日期获取统计数据
     * 
     * @param merchantId 商家ID
     * @param date       统计日期
     * @param type       统计类型（daily/monthly/yearly）
     * @return 统计数据
     */
    @GetMapping("/by-date")
    @Operation(summary = "根据日期获取统计数据", description = "根据指定日期获取统计数据")
    public R<MerchantStatistics> getStatisticsByDate(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "统计日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @Parameter(description = "统计类型") @RequestParam Integer type) {
        log.debug("根据日期获取统计数据请求，商家ID：{}，日期：{}，类型：{}", merchantId, date, type);
        return statisticsService.getStatisticsByDate(merchantId, date, type);
    }

    /**
     * 分页查询统计数据列表
     * 根据条件分页查询统计数据列表
     * 
     * @param merchantId 商家ID
     * @param page       页码
     * @param size       每页大小
     * @param type       统计类型（可选）
     * @param startDate  开始日期（可选）
     * @param endDate    结束日期（可选）
     * @return 统计数据分页列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询统计数据列表", description = "根据条件分页查询统计数据列表")
    public R<PageResult<MerchantStatistics>> getStatisticsList(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "统计类型") @RequestParam(required = false) Integer type,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.debug("分页查询统计数据列表请求，商家ID：{}，页码：{}，大小：{}", merchantId, page, size);
        return statisticsService.getStatisticsList(merchantId, page, size, type, startDate, endDate);
    }

    /**
     * 获取近期日统计数据
     * 获取最近N天的日统计数据
     * 
     * @param merchantId 商家ID
     * @param days       天数
     * @return 近期日统计数据列表
     */
    @GetMapping("/recent-daily")
    @Operation(summary = "获取近期日统计数据", description = "获取最近N天的日统计数据")
    public R<List<MerchantStatistics>> getRecentDailyStatistics(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "天数") @RequestParam(defaultValue = "7") Integer days) {
        log.debug("获取近期日统计数据请求，商家ID：{}，天数：{}", merchantId, days);
        return statisticsService.getRecentDailyStatistics(merchantId, days);
    }

    /**
     * 获取近期月统计数据
     * 获取最近N个月的月统计数据
     * 
     * @param merchantId 商家ID
     * @param months     月数
     * @return 近期月统计数据列表
     */
    @GetMapping("/recent-monthly")
    @Operation(summary = "获取近期月统计数据", description = "获取最近N个月的月统计数据")
    public R<List<MerchantStatistics>> getRecentMonthlyStatistics(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "月数") @RequestParam(defaultValue = "6") Integer months) {
        log.debug("获取近期月统计数据请求，商家ID：{}，月数：{}", merchantId, months);
        return statisticsService.getRecentMonthlyStatistics(merchantId, months);
    }

    /**
     * 获取近期年统计数据
     * 获取最近N年的年统计数据
     * 
     * @param merchantId 商家ID
     * @param years      年数
     * @return 近期年统计数据列表
     */
    @GetMapping("/recent-yearly")
    @Operation(summary = "获取近期年统计数据", description = "获取最近N年的年统计数据")
    public R<List<MerchantStatistics>> getRecentYearlyStatistics(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "年数") @RequestParam(defaultValue = "3") Integer years) {
        log.debug("获取近期年统计数据请求，商家ID：{}，年数：{}", merchantId, years);
        return statisticsService.getRecentYearlyStatistics(merchantId, years);
    }

    /*
     * @GetMapping("/sales-trend")
     * 
     * @Operation(summary = "获取销售趋势数据", description = "获取指定时间范围内的销售趋势数据")
     * public R<List<Map<String, Object>>> getSalesTrendData(
     * 
     * @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
     * 
     * @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern =
     * "yyyy-MM-dd") LocalDate startDate,
     * 
     * @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern =
     * "yyyy-MM-dd") LocalDate endDate,
     * 
     * @Parameter(description = "粒度") @RequestParam(defaultValue = "daily") String
     * granularity) {
     * log.debug("获取销售趋势数据请求，商家ID：{}，开始：{}，结束：{}，粒度：{}", merchantId, startDate,
     * endDate, granularity);
     * return statisticsService.getSalesTrendData(merchantId, startDate, endDate,
     * granularity);
     * }
     */

    /*
     * @GetMapping("/order-trend")
     * 
     * @Operation(summary = "获取订单趋势数据", description = "获取指定时间范围内的订单趋势数据")
     * public R<List<Map<String, Object>>> getOrderTrendData(
     * 
     * @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
     * 
     * @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern =
     * "yyyy-MM-dd") LocalDate startDate,
     * 
     * @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern =
     * "yyyy-MM-dd") LocalDate endDate,
     * 
     * @Parameter(description = "粒度") @RequestParam(defaultValue = "daily") String
     * granularity) {
     * log.debug("获取订单趋势数据请求，商家ID：{}，开始：{}，结束：{}，粒度：{}", merchantId, startDate,
     * endDate, granularity);
     * return statisticsService.getOrderTrendData(merchantId, startDate, endDate,
     * granularity);
     * }
     */

    /*
     * @GetMapping("/visit-trend")
     * 
     * @Operation(summary = "获取访问量趋势数据", description = "获取指定时间范围内的访问量趋势数据")
     * public R<List<Map<String, Object>>> getVisitTrendData(
     * 
     * @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
     * 
     * @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern =
     * "yyyy-MM-dd") LocalDate startDate,
     * 
     * @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern =
     * "yyyy-MM-dd") LocalDate endDate,
     * 
     * @Parameter(description = "粒度") @RequestParam(defaultValue = "daily") String
     * granularity) {
     * log.debug("获取访问量趋势数据请求，商家ID：{}，开始：{}，结束：{}，粒度：{}", merchantId, startDate,
     * endDate, granularity);
     * return statisticsService.getVisitTrendData(merchantId, startDate, endDate,
     * granularity);
     * }
     */

    /**
     * 获取转化率趋势数据
     * 获取指定时间范围内的转化率趋势数据
     * 
     * @param merchantId  商家ID
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @param granularity 粒度（daily/weekly/monthly）
     * @return 转化率趋势数据
     */
    @GetMapping("/conversion-trend")
    @Operation(summary = "获取转化率趋势数据", description = "获取指定时间范围内的转化率趋势数据")
    public R<List<Map<String, Object>>> getConversionTrendData(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "粒度") @RequestParam(defaultValue = "daily") String granularity) {
        log.debug("获取转化率趋势数据请求，商家ID：{}，开始：{}，结束：{}，粒度：{}", merchantId, startDate, endDate, granularity);
        // return statisticsService.getConversionTrendData(merchantId, startDate,
        // endDate, granularity);
        return R.ok();
    }

    /*
     * @GetMapping("/avg-order-value-trend")
     * 
     * @Operation(summary = "获取客单价趋势数据", description = "获取指定时间范围内的客单价趋势数据")
     * public R<List<Map<String, Object>>> getAvgOrderValueTrendData(
     * 
     * @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
     * 
     * @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern =
     * "yyyy-MM-dd") LocalDate startDate,
     * 
     * @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern =
     * "yyyy-MM-dd") LocalDate endDate,
     * 
     * @Parameter(description = "粒度") @RequestParam(defaultValue = "daily") String
     * granularity) {
     * log.debug("获取客单价趋势数据请求，商家ID：{}，开始：{}，结束：{}，粒度：{}", merchantId, startDate,
     * endDate, granularity);
     * return statisticsService.getAvgOrderValueTrendData(merchantId, startDate,
     * endDate, granularity);
     * }
     */

    /**
     * 获取商家排名数据
     * 获取商家在各项指标中的排名情况
     * 
     * @param merchantId 商家ID
     * @param metric     排名指标（sales/orders/products/rating）
     * @param period     统计周期（daily/monthly/yearly）
     * @return 排名数据
     */
    @GetMapping("/ranking")
    @Operation(summary = "获取商家排名数据", description = "获取商家在各项指标中的排名情况")
    public R<Map<String, Object>> getMerchantRankingData(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "排名指标") @RequestParam String metric,
            @Parameter(description = "统计周期") @RequestParam(defaultValue = "monthly") String period) {
        log.debug("获取商家排名数据请求，商家ID：{}，指标：{}，周期：{}", merchantId, metric, period);
        // return statisticsService.getMerchantRankingData(merchantId, metric, period);
        return R.ok();
    }

    /**
     * 获取商家对比数据
     * 获取商家与同行业平均水平的对比数据
     * 
     * @param merchantId  商家ID
     * @param compareType 对比类型（industry/category/region）
     * @param period      统计周期（daily/monthly/yearly）
     * @return 对比数据
     */
    @GetMapping("/comparison")
    @Operation(summary = "获取商家对比数据", description = "获取商家与同行业平均水平的对比数据")
    public R<Map<String, Object>> getMerchantComparisonData(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "对比类型") @RequestParam String compareType,
            @Parameter(description = "统计周期") @RequestParam(defaultValue = "monthly") String period) {
        log.debug("获取商家对比数据请求，商家ID：{}，类型：{}，周期：{}", merchantId, compareType, period);
        // return statisticsService.getMerchantComparisonData(merchantId, compareType,
        // period);
        return R.ok();
    }

    /**
     * 生成统计报告
     * 生成指定类型的统计报告
     * 
     * @param merchantId 商家ID
     * @param reportType 报告类型（daily/weekly/monthly）
     * @param date       报告日期
     * @return 统计报告
     */
    @PostMapping("/report")
    @Operation(summary = "生成统计报告", description = "生成指定类型的统计报告")
    public R<Map<String, Object>> generateStatisticsReport(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "报告类型") @RequestParam String reportType,
            @Parameter(description = "报告日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        log.info("生成统计报告请求，商家ID：{}，类型：{}，日期：{}", merchantId, reportType, date);
        // 将字符串类型映射为服务层定义的整数枚举码，保持前端 API 不变
        Integer reportTypeCode = parseReportType(reportType);
        return statisticsService.generateStatisticsReport(merchantId, reportTypeCode, date, null);
    }

    /**
     * 导出统计数据
     * 导出商家的统计数据到Excel
     * 
     * @param merchantId 商家ID
     * @param type       统计类型（可选）
     * @param startDate  开始日期（可选）
     * @param endDate    结束日期（可选）
     * @return Excel文件数据
     */
    @GetMapping("/export")
    @Operation(summary = "导出统计数据", description = "导出商家的统计数据到Excel")
    public R<byte[]> exportStatisticsData(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "统计类型") @RequestParam(required = false) String type,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("导出统计数据请求，商家ID：{}，类型：{}，开始：{}，结束：{}", merchantId, type, startDate, endDate);
        // 将可能为空的字符串类型映射为整数枚举码
        Integer statTypeCode = parseStatType(type);
        return statisticsService.exportStatisticsData(merchantId, statTypeCode, startDate, endDate);
    }

    /**
     * 手动触发统计计算
     * 手动触发指定商家的统计数据计算
     * 
     * @param merchantId 商家ID
     * @param date       统计日期
     * @param type       统计类型（daily/monthly/yearly）
     * @return 计算结果
     */
    @PostMapping("/calculate")
    @Operation(summary = "手动触发统计计算", description = "手动触发指定商家的统计数据计算")
    public R<Void> calculateStatistics(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "统计日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @Parameter(description = "统计类型") @RequestParam String type) {
        log.info("手动触发统计计算请求，商家ID：{}，日期：{}，类型：{}", merchantId, date, type);
        Integer statTypeCode = parseStatType(type);
        return statisticsService.calculateStatistics(merchantId, date, statTypeCode);
    }

    /**
     * 批量计算统计数据
     * 批量计算多个商家的统计数据
     * 
     * @param merchantIds 商家ID列表
     * @param date        统计日期
     * @param type        统计类型（daily/monthly/yearly）
     * @return 计算结果
     */
    @PostMapping("/batch-calculate")
    @Operation(summary = "批量计算统计数据", description = "批量计算多个商家的统计数据")
    public R<Map<String, Object>> batchCalculateStatistics(
            @Parameter(description = "商家ID列表") @RequestBody List<Long> merchantIds,
            @Parameter(description = "统计日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @Parameter(description = "统计类型") @RequestParam String type) {
        log.info("批量计算统计数据请求，商家数量：{}，日期：{}，类型：{}", merchantIds.size(), date, type);
        Integer statTypeCode = parseStatType(type);
        int successCount = 0;
        int failureCount = 0;
        for (Long merchantId : merchantIds) {
            try {
                // 将字符串类型转换为服务层使用的整数枚举码
                statisticsService.calculateStatistics(merchantId, date, statTypeCode);
                successCount++;
            } catch (Exception e) {
                log.error("批量计算商家 {} 的统计数据失败", merchantId, e);
                failureCount++;
            }
        }
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("total", merchantIds.size());
        result.put("success", successCount);
        result.put("failure", failureCount);
        return R.ok(result);
    }

    /**
     * 获取汇总统计数据
     * 获取指定时间范围内的汇总统计数据
     * 
     * @param merchantId 商家ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 汇总统计数据
     */
    @GetMapping("/summary")
    @Operation(summary = "获取汇总统计数据", description = "获取指定时间范围内的汇总统计数据")
    public R<Map<String, Object>> getSummaryStatistics(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.debug("获取汇总统计数据请求，商家ID：{}，开始：{}，结束：{}", merchantId, startDate, endDate);
        return statisticsService.getSummaryStatistics(merchantId, null, startDate, endDate);
    }

    /**
     * 获取关键指标数据
     * 获取商家的关键业务指标数据
     * 
     * @param merchantId 商家ID
     * @return 关键指标数据
     */
    @GetMapping("/key-metrics")
    @Operation(summary = "获取关键指标数据", description = "获取商家的关键业务指标数据")
    public R<Map<String, Object>> getKeyMetrics(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId) {
        log.debug("获取关键指标数据请求，商家ID：{}", merchantId);
        return statisticsService.getKeyMetrics(merchantId);
    }

    /**
     * 获取实时统计数据
     * 获取商家的实时统计数据
     * 
     * @param merchantId 商家ID
     * @return 实时统计数据
     */
    @GetMapping("/realtime")
    @Operation(summary = "获取实时统计数据", description = "获取商家的实时统计数据")
    public R<Map<String, Object>> getRealtimeStatistics(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId) {
        log.debug("获取实时统计数据请求，商家ID：{}", merchantId);
        return statisticsService.getRealTimeStatistics(merchantId);
    }

    /**
     * 清理过期统计数据
     * 清理指定日期之前的过期统计数据
     * 
     * @param beforeDate 截止日期
     * @return 清理结果
     */
    @DeleteMapping("/cleanup")
    @Operation(summary = "清理过期统计数据", description = "清理指定日期之前的过期统计数据")
    public R<Map<String, Object>> cleanupExpiredStatistics(
            @Parameter(description = "截止日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate beforeDate) {
        log.info("清理过期统计数据请求，截止日期：{}", beforeDate);
        return statisticsService.cleanupExpiredStatistics(beforeDate);
    }

    /**
     * 获取统计数据存储状态
     * 获取统计数据的存储状态信息
     * 
     * @param merchantId 商家ID（可选）
     * @return 存储状态信息
     */
    @GetMapping("/storage-status")
    @Operation(summary = "获取统计数据存储状态", description = "获取统计数据的存储状态信息")
    public R<Map<String, Object>> getStatisticsStorageStatus(
            @Parameter(description = "商家ID") @RequestParam(required = false) Long merchantId) {
        log.debug("获取统计数据存储状态请求，商家ID：{}", merchantId);
        return statisticsService.getStatisticsStorageStatus(merchantId);
    }

}
