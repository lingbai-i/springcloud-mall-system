package com.mall.merchant.domain.entity;

import lombok.Data;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 轮播图统计实体类
 * 存储轮播图的曝光和点击统计数据（按日汇总）
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-12-28
 */
@Data
@Entity
@Table(name = "banner_statistics", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"banner_id", "stat_date"}))
public class BannerStatistics {

    /**
     * 统计ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 轮播图申请ID
     */
    @Column(name = "banner_id", nullable = false)
    private Long bannerId;

    /**
     * 统计日期
     */
    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    /**
     * 曝光次数
     */
    @Column(name = "impressions")
    private Integer impressions = 0;

    /**
     * 点击次数
     */
    @Column(name = "clicks")
    private Integer clicks = 0;

    /**
     * 独立曝光数（去重UV）
     */
    @Column(name = "unique_impressions")
    private Integer uniqueImpressions = 0;

    /**
     * 独立点击数（去重UV）
     */
    @Column(name = "unique_clicks")
    private Integer uniqueClicks = 0;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    // ==================== 业务方法 ====================

    /**
     * 计算点击率（CTR）
     * 
     * @return 点击率百分比，保留2位小数
     */
    public Double getCtr() {
        if (impressions == null || impressions == 0) {
            return 0.0;
        }
        return Math.round((clicks * 100.0 / impressions) * 100.0) / 100.0;
    }

    /**
     * 计算独立点击率
     * 
     * @return 独立点击率百分比，保留2位小数
     */
    public Double getUniqueCtr() {
        if (uniqueImpressions == null || uniqueImpressions == 0) {
            return 0.0;
        }
        return Math.round((uniqueClicks * 100.0 / uniqueImpressions) * 100.0) / 100.0;
    }

    /**
     * 增加曝光次数
     */
    public void incrementImpressions() {
        if (this.impressions == null) {
            this.impressions = 0;
        }
        this.impressions++;
    }

    /**
     * 增加点击次数
     */
    public void incrementClicks() {
        if (this.clicks == null) {
            this.clicks = 0;
        }
        this.clicks++;
    }

    /**
     * 持久化前设置时间
     */
    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        if (this.impressions == null) this.impressions = 0;
        if (this.clicks == null) this.clicks = 0;
        if (this.uniqueImpressions == null) this.uniqueImpressions = 0;
        if (this.uniqueClicks == null) this.uniqueClicks = 0;
    }

    /**
     * 更新前设置时间
     */
    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
