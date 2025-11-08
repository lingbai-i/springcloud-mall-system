package com.mall.product.domain.entity;

import com.mall.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * 价格审批实体类
 * 用于记录价格变更的审批流程
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceApproval extends BaseEntity {
    
    /**
     * 价格历史记录ID
     */
    private Long priceHistoryId;
    
    /**
     * 申请人ID
     */
    private Long applicantId;
    
    /**
     * 申请人姓名
     */
    private String applicantName;
    
    /**
     * 审批人ID
     */
    private Long approverId;
    
    /**
     * 审批人姓名
     */
    private String approverName;
    
    /**
     * 审批状态：PENDING-待审批，APPROVED-已通过，REJECTED-已拒绝，WITHDRAWN-已撤回，TRANSFERRED-已转交
     */
    private String status;
    
    /**
     * 紧急程度：1-普通，2-紧急，3-特急
     */
    private Integer urgencyLevel;
    
    /**
     * 业务原因
     */
    private String businessReason;
    
    /**
     * 审批意见
     */
    private String approvalComment;
    
    /**
     * 申请时间
     */
    private LocalDateTime applicationTime;
    
    /**
     * 审批时间
     */
    private LocalDateTime approvalTime;
    
    /**
     * 预期完成时间
     */
    private LocalDateTime expectedTime;
    
    /**
     * 商品ID（冗余字段，便于查询）
     */
    private Long productId;
    
    /**
     * SKU ID（冗余字段，便于查询）
     */
    private Long skuId;
    
    /**
     * 商品名称（冗余字段）
     */
    private String productName;
    
    /**
     * SKU名称（冗余字段）
     */
    private String skuName;
    
    /**
     * 变更前价格（冗余字段）
     */
    private Double oldPrice;
    
    /**
     * 变更后价格（冗余字段）
     */
    private Double newPrice;
    
    /**
     * 价格变更幅度（百分比）
     */
    private Double changeRate;
    
    /**
     * 审批流程节点
     */
    private String approvalNode;
    
    /**
     * 上一个审批人ID（转交时使用）
     */
    private Long previousApproverId;
    
    /**
     * 转交原因
     */
    private String transferReason;
    
    /**
     * 撤回原因
     */
    private String withdrawReason;
    
    /**
     * 处理时长（分钟）
     */
    private Long processingDuration;
    
    /**
     * 是否超时
     */
    private Boolean isOverdue;
    
    /**
     * 附件信息（JSON格式）
     */
    private String attachments;
    
    /**
     * 扩展信息（JSON格式）
     */
    private String extendInfo;
}