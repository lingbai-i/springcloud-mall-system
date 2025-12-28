package com.mall.merchant.domain.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 轮播图申请VO
 * 用于返回轮播图申请详情
 * 
 * @author system
 * @version 1.0
 * @since 2025-12-28
 */
@Data
public class BannerApplicationVO {

    /**
     * 申请ID
     */
    private Long id;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 商家名称（用于管理员查看）
     */
    private String merchantName;

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
     * 审核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 审核人ID
     */
    private Long reviewerId;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    /**
     * 创建时间（提交时间）
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    // ==================== 统计数据（可选填充） ====================

    /**
     * 总曝光量
     */
    private Long totalImpressions;

    /**
     * 总点击量
     */
    private Long totalClicks;

    /**
     * 点击率（CTR）
     */
    private Double ctr;

    /**
     * 是否可以取消
     */
    private Boolean canCancel;

    /**
     * 是否可以编辑
     */
    private Boolean canEdit;

    /**
     * 是否处于活跃展示期
     */
    private Boolean isActive;
}
