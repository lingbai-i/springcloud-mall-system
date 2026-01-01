package com.mall.admin.domain.vo;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 轮播图审核列表VO
 * 用于管理员审核列表展示
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-12-28
 */
@Data
public class BannerReviewVO {

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
     * 状态: PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝, EXPIRED-已过期, CANCELLED-已取消
     */
    private String status;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 提交时间（兼容 createTime 字段）
     */
    @JsonAlias("createTime")
    private LocalDateTime submitTime;

    /**
     * 创建时间（与 submitTime 相同，用于前端兼容）
     */
    private LocalDateTime createTime;

    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;
    
    /**
     * 设置提交时间，同时设置createTime
     */
    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
        this.createTime = submitTime;
    }
    
    /**
     * 设置创建时间，同时设置submitTime
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        if (this.submitTime == null) {
            this.submitTime = createTime;
        }
    }
}
