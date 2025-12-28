package com.mall.merchant.domain.entity;

import com.mall.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 轮播图申请实体类
 * 存储商家提交的轮播图投流申请信息
 * 
 * @author system
 * @version 1.0
 * @since 2025-12-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "banner_application")
public class BannerApplication extends BaseEntity {

    /**
     * 商家ID
     */
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    /**
     * 轮播图图片URL
     */
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    /**
     * 标题（最多100字）
     */
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    /**
     * 描述（最多500字）
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 跳转链接
     */
    @Column(name = "target_url", nullable = false, length = 500)
    private String targetUrl;

    /**
     * 展示开始日期
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * 展示结束日期
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * 状态: PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝, EXPIRED-已过期, CANCELLED-已取消
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    /**
     * 拒绝原因
     */
    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    /**
     * 审核时间
     */
    @Column(name = "review_time")
    private LocalDateTime reviewTime;

    /**
     * 审核人ID
     */
    @Column(name = "reviewer_id")
    private Long reviewerId;

    /**
     * 排序权重（数值越大越靠前）
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // ==================== 状态常量 ====================
    
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // ==================== 业务方法 ====================

    /**
     * 获取状态文本
     * 
     * @return 状态文本
     */
    public String getStatusText() {
        if (status == null) return "未知";
        switch (status) {
            case STATUS_PENDING:
                return "待审核";
            case STATUS_APPROVED:
                return "已通过";
            case STATUS_REJECTED:
                return "已拒绝";
            case STATUS_EXPIRED:
                return "已过期";
            case STATUS_CANCELLED:
                return "已取消";
            default:
                return "未知";
        }
    }

    /**
     * 判断是否可以取消
     * 只有待审核状态可以取消
     * 
     * @return 是否可以取消
     */
    public boolean canCancel() {
        return STATUS_PENDING.equals(status);
    }

    /**
     * 判断是否可以编辑
     * 待审核和已拒绝状态可以编辑
     * 
     * @return 是否可以编辑
     */
    public boolean canEdit() {
        return STATUS_PENDING.equals(status) || STATUS_REJECTED.equals(status);
    }

    /**
     * 判断是否处于活跃展示期
     * 
     * @return 是否活跃
     */
    public boolean isActive() {
        if (!STATUS_APPROVED.equals(status)) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    /**
     * 判断是否已过期
     * 
     * @return 是否已过期
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
    }

    /**
     * 持久化前的默认值填充
     */
    @PrePersist
    protected void onPrePersist() {
        if (this.status == null) {
            this.status = STATUS_PENDING;
        }
        if (this.sortOrder == null) {
            this.sortOrder = 0;
        }
    }
}
