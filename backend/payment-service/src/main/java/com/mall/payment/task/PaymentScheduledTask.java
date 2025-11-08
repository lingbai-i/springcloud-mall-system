package com.mall.payment.task;

import com.mall.payment.entity.PaymentStatistics;
import com.mall.payment.service.PaymentService;
import com.mall.payment.service.PaymentStatisticsService;
import com.mall.payment.service.RefundService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import java.time.LocalDateTime;

/**
 * 支付定时任务处理类
 * 处理支付相关的定时任务，包括订单过期检查、失败订单重试、统计数据更新等
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@Component
public class PaymentScheduledTask {

    private static final Logger logger = LoggerFactory.getLogger(PaymentScheduledTask.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RefundService refundService;

    @Autowired
    private PaymentStatisticsService statisticsService;

    /**
     * 处理过期的支付订单
     * 每5分钟执行一次，检查并处理过期的支付订单
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5分钟
    public void handleExpiredPaymentOrders() {
        logger.info("开始处理过期的支付订单任务");
        
        try {
            int processedCount = paymentService.handleExpiredOrders();
            logger.info("过期支付订单处理完成，处理数量: {}", processedCount);
            
        } catch (Exception e) {
            logger.error("处理过期支付订单任务异常", e);
        }
    }

    /**
     * 重试失败的支付订单
     * 每10分钟执行一次，重试符合条件的失败支付订单
     */
    @Scheduled(fixedRate = 10 * 60 * 1000) // 10分钟
    public void retryFailedPaymentOrders() {
        logger.info("开始重试失败的支付订单任务");
        
        try {
            int retryCount = paymentService.retryFailedOrders();
            logger.info("失败支付订单重试完成，重试数量: {}", retryCount);
            
        } catch (Exception e) {
            logger.error("重试失败支付订单任务异常", e);
        }
    }

    /**
     * 批量处理待审核的退款申请
     * 每30分钟执行一次，自动处理符合条件的退款申请
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // 30分钟
    public void batchProcessPendingRefunds() {
        logger.info("开始批量处理待审核退款申请任务");
        
        try {
            int processedCount = refundService.batchProcessPendingRefunds();
            logger.info("批量处理待审核退款申请完成，处理数量: {}", processedCount);
            
        } catch (Exception e) {
            logger.error("批量处理待审核退款申请任务异常", e);
        }
    }

    /**
     * 重试失败的退款订单
     * 每15分钟执行一次，重试符合条件的失败退款订单
     */
    @Scheduled(fixedRate = 15 * 60 * 1000) // 15分钟
    public void retryFailedRefundOrders() {
        logger.info("开始重试失败的退款订单任务");
        
        try {
            int retryCount = refundService.retryFailedRefunds();
            logger.info("失败退款订单重试完成，重试数量: {}", retryCount);
            
        } catch (Exception e) {
            logger.error("重试失败退款订单任务异常", e);
        }
    }

    /**
     * 同步支付状态
     * 每小时执行一次，主动查询第三方支付平台的订单状态进行同步
     */
    @Scheduled(fixedRate = 60 * 60 * 1000) // 1小时
    public void syncPaymentStatus() {
        logger.info("开始同步支付状态任务");
        
        try {
            int syncCount = paymentService.syncPaymentStatus();
            logger.info("支付状态同步完成，同步数量: {}", syncCount);
            
        } catch (Exception e) {
            logger.error("同步支付状态任务异常", e);
        }
    }

    /**
     * 同步退款状态
     * 每小时执行一次，主动查询第三方支付平台的退款状态进行同步
     */
    @Scheduled(fixedRate = 60 * 60 * 1000) // 1小时
    public void syncRefundStatus() {
        logger.info("开始同步退款状态任务");
        
        try {
            int syncCount = refundService.syncRefundStatus();
            logger.info("退款状态同步完成，同步数量: {}", syncCount);
            
        } catch (Exception e) {
            logger.error("同步退款状态任务异常", e);
        }
    }

    /**
     * 清理过期的支付记录
     * 每天凌晨2点执行，清理超过保留期限的支付记录
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点
    public void cleanupExpiredPaymentRecords() {
        logger.info("开始清理过期支付记录任务");
        
        try {
            // 清理90天前的已完成支付记录
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(90);
            int cleanedCount = paymentService.cleanupExpiredRecords(cutoffTime);
            logger.info("过期支付记录清理完成，清理数量: {}", cleanedCount);
            
        } catch (Exception e) {
            logger.error("清理过期支付记录任务异常", e);
        }
    }

    /**
     * 清理过期的退款记录
     * 每天凌晨2点30分执行，清理超过保留期限的退款记录
     */
    @Scheduled(cron = "0 30 2 * * ?") // 每天凌晨2点30分
    public void cleanupExpiredRefundRecords() {
        logger.info("开始清理过期退款记录任务");
        
        try {
            // 清理90天前的已完成退款记录
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(90);
            int cleanedCount = refundService.cleanupExpiredRecords(cutoffTime);
            logger.info("过期退款记录清理完成，清理数量: {}", cleanedCount);
            
        } catch (Exception e) {
            logger.error("清理过期退款记录任务异常", e);
        }
    }

    /**
     * 生成日统计报表
     * 每天凌晨1点执行，生成前一天的统计数据
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void generateDailyReport() {
        logger.info("开始生成日统计报表");
        
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            statisticsService.generateStatistics(yesterday, yesterday, PaymentStatistics.StatType.DAILY);
            logger.info("日统计报表生成完成，日期: {}", yesterday);
            
        } catch (Exception e) {
            logger.error("生成日统计报表异常", e);
        }
    }

    /**
     * 生成月统计报表
     * 每月1号凌晨2点执行，生成上个月的统计数据
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    public void generateMonthlyReport() {
        logger.info("开始生成月统计报表");
        
        try {
            LocalDate lastMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
            LocalDate lastMonthEnd = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());
            statisticsService.generateStatistics(lastMonth, lastMonthEnd, PaymentStatistics.StatType.MONTHLY);
            logger.info("月统计报表生成完成，月份: {}", lastMonth.getYear() + "-" + lastMonth.getMonthValue());
            
        } catch (Exception e) {
            logger.error("生成月统计报表异常", e);
        }
    }

    /**
     * 生成年统计报表
     * 每年1月1号凌晨3点执行，生成上一年的统计数据
     */
    @Scheduled(cron = "0 0 3 1 1 ?")
    public void generateYearlyReport() {
        logger.info("开始生成年统计报表");
        
        try {
            LocalDate lastYear = LocalDate.now().minusYears(1).withDayOfYear(1);
            LocalDate lastYearEnd = lastYear.withDayOfYear(lastYear.lengthOfYear());
            statisticsService.generateStatistics(lastYear, lastYearEnd, PaymentStatistics.StatType.YEARLY);
            logger.info("年统计报表生成完成，年份: {}", lastYear.getYear());
            
        } catch (Exception e) {
            logger.error("生成年统计报表异常", e);
        }
    }

    /**
     * 清理过期统计数据
     * 每周日凌晨4点执行，清理超过保留期的统计数据
     */
    @Scheduled(cron = "0 0 4 * * SUN")
    public void cleanExpiredStatistics() {
        logger.info("开始清理过期统计数据");
        
        try {
            // 清理超过2年的日统计数据
            int dailyDeleted = statisticsService.cleanExpiredStatistics(730, PaymentStatistics.StatType.DAILY);
            logger.info("清理过期日统计数据完成，删除记录数: {}", dailyDeleted);
            
            // 清理超过5年的月统计数据
            int monthlyDeleted = statisticsService.cleanExpiredStatistics(1825, PaymentStatistics.StatType.MONTHLY);
            logger.info("清理过期月统计数据完成，删除记录数: {}", monthlyDeleted);
            
            // 年统计数据永久保留，不清理
            
        } catch (Exception e) {
            logger.error("清理过期统计数据异常", e);
        }
    }

    /**
     * 健康检查任务
     * 每5分钟执行一次，检查支付服务的健康状态
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5分钟
    public void healthCheck() {
        logger.debug("开始支付服务健康检查");
        
        try {
            // 检查数据库连接
            boolean dbHealthy = checkDatabaseHealth();
            
            // 检查第三方支付平台连接
            boolean paymentChannelHealthy = checkPaymentChannelHealth();
            
            if (dbHealthy && paymentChannelHealthy) {
                logger.debug("支付服务健康检查通过");
            } else {
                logger.warn("支付服务健康检查失败 - 数据库: {}, 支付渠道: {}", dbHealthy, paymentChannelHealthy);
            }
            
        } catch (Exception e) {
            logger.error("支付服务健康检查异常", e);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 检查数据库健康状态
     * 
     * @return 数据库是否健康
     */
    private boolean checkDatabaseHealth() {
        try {
            // 执行简单的数据库查询来检查连接
            paymentService.getPaymentStatistics(
                LocalDateTime.now().minusMinutes(1), 
                LocalDateTime.now(), 
                null
            );
            return true;
        } catch (Exception e) {
            logger.warn("数据库健康检查失败", e);
            return false;
        }
    }

    /**
     * 检查支付渠道健康状态
     * 
     * @return 支付渠道是否健康
     */
    private boolean checkPaymentChannelHealth() {
        try {
            // 这里可以添加对第三方支付平台的健康检查
            // 例如调用支付平台的心跳接口或查询接口
            return true;
        } catch (Exception e) {
            logger.warn("支付渠道健康检查失败", e);
            return false;
        }
    }
}