package com.mall.payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 概览统计数据DTO
 * 用于封装支付系统的概览统计信息
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverviewStatistics {

    /**
     * 今日统计
     */
    private DailyStats todayStats;

    /**
     * 本月统计
     */
    private MonthlyStats monthStats;

    /**
     * 本年统计
     */
    private YearlyStats yearStats;
}