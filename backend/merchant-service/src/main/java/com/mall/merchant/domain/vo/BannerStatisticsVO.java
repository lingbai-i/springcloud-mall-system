package com.mall.merchant.domain.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 轮播图统计VO
 * 用于返回轮播图统计数据
 * 
 * @author system
 * @version 1.0
 * @since 2025-12-28
 */
@Data
public class BannerStatisticsVO {

    /**
     * 轮播图申请ID
     */
    private Long bannerId;

    /**
     * 轮播图标题
     */
    private String bannerTitle;

    /**
     * 总曝光量
     */
    private Long totalImpressions;

    /**
     * 总点击量
     */
    private Long totalClicks;

    /**
     * 独立曝光数（UV）
     */
    private Long uniqueImpressions;

    /**
     * 独立点击数（UV）
     */
    private Long uniqueClicks;

    /**
     * 点击率（CTR）百分比
     */
    private Double ctr;

    /**
     * 独立点击率百分比
     */
    private Double uniqueCtr;

    /**
     * 统计开始日期
     */
    private LocalDate startDate;

    /**
     * 统计结束日期
     */
    private LocalDate endDate;

    /**
     * 每日统计数据
     */
    private List<DailyStatistics> dailyStats;

    /**
     * 每日统计数据内部类
     */
    @Data
    public static class DailyStatistics {
        /**
         * 统计日期
         */
        private LocalDate date;

        /**
         * 曝光次数
         */
        private Integer impressions;

        /**
         * 点击次数
         */
        private Integer clicks;

        /**
         * 点击率
         */
        private Double ctr;
    }

    /**
     * 计算点击率
     */
    public void calculateCtr() {
        if (totalImpressions != null && totalImpressions > 0) {
            this.ctr = Math.round((totalClicks * 100.0 / totalImpressions) * 100.0) / 100.0;
        } else {
            this.ctr = 0.0;
        }

        if (uniqueImpressions != null && uniqueImpressions > 0) {
            this.uniqueCtr = Math.round((uniqueClicks * 100.0 / uniqueImpressions) * 100.0) / 100.0;
        } else {
            this.uniqueCtr = 0.0;
        }
    }
}
