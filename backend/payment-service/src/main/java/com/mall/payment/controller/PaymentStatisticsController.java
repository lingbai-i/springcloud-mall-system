package com.mall.payment.controller;

import com.mall.payment.annotation.RequirePermission;
import com.mall.payment.entity.PaymentStatistics;
import com.mall.payment.service.PaymentStatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付统计控制器
 * 提供支付统计和报表相关的RESTful API接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@RestController
@RequestMapping("/api/payment/statistics")
public class PaymentStatisticsController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentStatisticsController.class);

    @Autowired
    private PaymentStatisticsService statisticsService;

    // ==================== 统计数据查询接口 ====================

    /**
     * 获取指定日期的统计数据
     * 
     * @param date 统计日期
     * @param statType 统计类型（DAILY/MONTHLY/YEARLY）
     * @param paymentMethod 支付方式（可选）
     * @return 统计数据
     */
    @GetMapping("/date/{date}")
    @RequirePermission(value = {"ADMIN"})
    public ResponseEntity<Map<String, Object>> getStatisticsByDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam PaymentStatistics.StatType statType,
            @RequestParam(required = false) String paymentMethod) {
        
        logger.info("查询统计数据，日期: {}, 类型: {}, 支付方式: {}", date, statType, paymentMethod);
        
        try {
            PaymentStatistics statistics = statisticsService.getStatistics(date, statType, paymentMethod);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", statistics);
            response.put("message", "查询成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("查询统计数据异常", e);
            return createErrorResponse("查询统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询统计数据
     * 
     * @param statType 统计类型
     * @param paymentMethod 支付方式（可选）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分页统计数据
     */
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getStatisticsPage(
            @RequestParam PaymentStatistics.StatType statType,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.info("分页查询统计数据，类型: {}, 支付方式: {}, 页码: {}, 大小: {}", 
                   statType, paymentMethod, page, size);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<PaymentStatistics> statisticsPage = statisticsService.getStatistics(statType, paymentMethod, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", statisticsPage.getContent());
            response.put("totalElements", statisticsPage.getTotalElements());
            response.put("totalPages", statisticsPage.getTotalPages());
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("message", "查询成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("分页查询统计数据异常", e);
            return createErrorResponse("分页查询统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 查询日期范围内的统计数据
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statType 统计类型
     * @param paymentMethod 支付方式（可选）
     * @return 统计数据列表
     */
    @GetMapping("/range")
    public ResponseEntity<Map<String, Object>> getStatisticsByRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam PaymentStatistics.StatType statType,
            @RequestParam(required = false) String paymentMethod) {
        
        logger.info("查询日期范围统计数据，开始日期: {}, 结束日期: {}, 类型: {}, 支付方式: {}", 
                   startDate, endDate, statType, paymentMethod);
        
        try {
            List<PaymentStatistics> statistics = statisticsService.getStatistics(
                    startDate, endDate, statType, paymentMethod);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", statistics);
            response.put("count", statistics.size());
            response.put("message", "查询成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("查询日期范围统计数据异常", e);
            return createErrorResponse("查询日期范围统计数据失败: " + e.getMessage());
        }
    }

    // ==================== 报表数据接口 ====================

    /**
     * 获取概览统计数据
     * 包含今日、本月、本年的统计数据
     * 
     * @return 概览统计数据
     */
    @GetMapping("/overview")
    @RequirePermission(value = {"ADMIN"})
    public ResponseEntity<Map<String, Object>> getOverviewStatistics() {
        logger.info("获取概览统计数据");
        
        try {
            PaymentStatisticsService.OverviewStatistics overview = statisticsService.getOverviewStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", overview);
            response.put("message", "查询成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取概览统计数据异常", e);
            return createErrorResponse("获取概览统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取支付趋势数据
     * 
     * @param days 天数（默认30天）
     * @return 支付趋势数据
     */
    @GetMapping("/trend/daily")
    public ResponseEntity<Map<String, Object>> getPaymentTrend(
            @RequestParam(defaultValue = "30") int days) {
        
        logger.info("获取支付趋势数据，天数: {}", days);
        
        try {
            List<PaymentStatisticsService.TrendData> trendData = statisticsService.getPaymentTrend(days);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", trendData);
            response.put("message", "查询成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取支付趋势数据异常", e);
            return createErrorResponse("获取支付趋势数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取月度支付趋势数据
     * 
     * @param months 月数（默认12个月）
     * @return 月度支付趋势数据
     */
    @GetMapping("/trend/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyPaymentTrend(
            @RequestParam(defaultValue = "12") int months) {
        
        logger.info("获取月度支付趋势数据，月数: {}", months);
        
        try {
            List<PaymentStatisticsService.TrendData> trendData = statisticsService.getMonthlyPaymentTrend(months);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", trendData);
            response.put("message", "查询成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取月度支付趋势数据异常", e);
            return createErrorResponse("获取月度支付趋势数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取支付方式统计数据
     * 
     * @param date 统计日期
     * @param statType 统计类型
     * @return 支付方式统计数据
     */
    @GetMapping("/payment-method")
    public ResponseEntity<Map<String, Object>> getPaymentMethodStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam PaymentStatistics.StatType statType) {
        
        logger.info("获取支付方式统计数据，日期: {}, 类型: {}", date, statType);
        
        try {
            List<PaymentStatisticsService.PaymentMethodStats> stats = 
                    statisticsService.getPaymentMethodStatistics(date, statType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("message", "查询成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取支付方式统计数据异常", e);
            return createErrorResponse("获取支付方式统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取支付方式排名数据
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statType 统计类型
     * @param rankBy 排名依据（amount/orders）
     * @return 支付方式排名数据
     */
    @GetMapping("/payment-method/ranking")
    public ResponseEntity<Map<String, Object>> getPaymentMethodRanking(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam PaymentStatistics.StatType statType,
            @RequestParam(defaultValue = "amount") String rankBy) {
        
        logger.info("获取支付方式排名数据，开始日期: {}, 结束日期: {}, 类型: {}, 排名依据: {}", 
                   startDate, endDate, statType, rankBy);
        
        try {
            List<PaymentStatisticsService.PaymentMethodRanking> rankings = 
                    statisticsService.getPaymentMethodRanking(startDate, endDate, statType, rankBy);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", rankings);
            response.put("message", "查询成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取支付方式排名数据异常", e);
            return createErrorResponse("获取支付方式排名数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户统计数据
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 用户统计数据
     */
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUserStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        logger.info("获取用户统计数据，开始日期: {}, 结束日期: {}", startDate, endDate);
        
        try {
            PaymentStatisticsService.UserStatistics userStats = 
                    statisticsService.getUserStatistics(startDate, endDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", userStats);
            response.put("message", "查询成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取用户统计数据异常", e);
            return createErrorResponse("获取用户统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取风控统计数据
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 风控统计数据
     */
    @GetMapping("/risk")
    public ResponseEntity<Map<String, Object>> getRiskStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        logger.info("获取风控统计数据，开始日期: {}, 结束日期: {}", startDate, endDate);
        
        try {
            PaymentStatisticsService.RiskStatistics riskStats = 
                    statisticsService.getRiskStatistics(startDate, endDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", riskStats);
            response.put("message", "查询成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取风控统计数据异常", e);
            return createErrorResponse("获取风控统计数据失败: " + e.getMessage());
        }
    }

    // ==================== 数据管理接口 ====================

    /**
     * 批量生成统计数据
     * 
     * @param request 生成请求
     * @return 生成结果
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateStatistics(@RequestBody GenerateStatisticsRequest request) {
        logger.info("批量生成统计数据，开始日期: {}, 结束日期: {}, 类型: {}", 
                   request.getStartDate(), request.getEndDate(), request.getStatType());
        
        try {
            statisticsService.generateStatistics(request.getStartDate(), request.getEndDate(), request.getStatType());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "统计数据生成成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("批量生成统计数据异常", e);
            return createErrorResponse("批量生成统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 导出统计报表
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statType 统计类型
     * @param format 导出格式（excel/csv）
     * @return 导出文件路径
     */
    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportStatisticsReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam PaymentStatistics.StatType statType,
            @RequestParam(defaultValue = "excel") String format) {
        
        logger.info("导出统计报表，开始日期: {}, 结束日期: {}, 类型: {}, 格式: {}", 
                   startDate, endDate, statType, format);
        
        try {
            String filePath = statisticsService.exportStatisticsReport(startDate, endDate, statType, format);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("filePath", filePath);
            response.put("message", "报表导出成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("导出统计报表异常", e);
            return createErrorResponse("导出统计报表失败: " + e.getMessage());
        }
    }

    /**
     * 清理过期统计数据
     * 
     * @param retentionDays 保留天数
     * @param statType 统计类型
     * @return 清理结果
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanExpiredStatistics(
            @RequestParam int retentionDays,
            @RequestParam PaymentStatistics.StatType statType) {
        
        logger.info("清理过期统计数据，保留天数: {}, 统计类型: {}", retentionDays, statType);
        
        try {
            int deletedCount = statisticsService.cleanExpiredStatistics(retentionDays, statType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("deletedCount", deletedCount);
            response.put("message", "过期数据清理成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("清理过期统计数据异常", e);
            return createErrorResponse("清理过期统计数据失败: " + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 创建错误响应
     * 
     * @param message 错误消息
     * @return 错误响应
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return ResponseEntity.badRequest().body(response);
    }

    // ==================== 内部类 ====================

    /**
     * 生成统计数据请求类
     */
    public static class GenerateStatisticsRequest {
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;
        
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;
        
        private PaymentStatistics.StatType statType;

        // Getters and Setters
        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public PaymentStatistics.StatType getStatType() {
            return statType;
        }

        public void setStatType(PaymentStatistics.StatType statType) {
            this.statType = statType;
        }
    }
}