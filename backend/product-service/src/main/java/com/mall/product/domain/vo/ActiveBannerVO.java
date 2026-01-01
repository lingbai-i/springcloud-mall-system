package com.mall.product.domain.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 活跃轮播图VO
 * 用于首页轮播图展示
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-12-28
 */
@Data
public class ActiveBannerVO {

    /**
     * 轮播图ID（申请ID）
     */
    private Long id;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 轮播图图片URL
     */
    private String imageUrl;

    /**
     * 标题
     */
    private String title;

    /**
     * 跳转链接
     */
    private String targetUrl;

    /**
     * 展示开始日期
     */
    private LocalDate startDate;

    /**
     * 展示结束日期
     */
    private LocalDate endDate;

    /**
     * 排序权重（越大越靠前）
     */
    private Integer sortOrder;
}
