package com.mall.admin.domain.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 轮播图审核详情VO
 * 用于管理员查看申请详情
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-12-28
 */
@Data
public class BannerReviewDetailVO {

    /**
     * 申请ID
     */
    private Long id;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 商家名称
     */
    private String merchantName;

    /**
     * 商家编码
     */
    private String merchantCode;

    /**
     * 轮播图图片URL
     */
    private String imageUrl;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

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
     * 状态: PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝, EXPIRED-已过期, CANCELLED-已取消
     */
    private String status;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 审核人ID
     */
    private Long reviewerId;

    /**
     * 审核人名称
     */
    private String reviewerName;

    /**
     * 排序权重
     */
    private Integer sortOrder;
}
