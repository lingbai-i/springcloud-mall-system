package com.mall.merchant.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家申请信息VO
 * 
 * @author system
 * @since 2025-11-11
 */
@Data
public class MerchantApplicationVO {

    /**
     * 申请ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系人电话
     */
    private String contactPhone;

    /**
     * 联系人邮箱
     */
    private String email;

    /**
     * 主体类型（individual-个人，enterprise-企业）
     */
    private String entityType;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 统一社会信用代码
     */
    private String creditCode;

    /**
     * 法人代表
     */
    private String legalPerson;

    /**
     * 营业执照图片
     */
    private String businessLicense;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 身份证正面照
     */
    private String idCardFront;

    /**
     * 身份证反面照
     */
    private String idCardBack;

    /**
     * 审批状态（0-待审批，1-已通过，2-已拒绝）
     */
    private Integer approvalStatus;

    /**
     * 审批备注
     */
    private String approvalReason;

    /**
     * 审批人ID
     */
    private Long approvalBy;

    /**
     * 审批人用户名
     */
    private String approvalByName;

    /**
     * 审批时间
     */
    private LocalDateTime approvalTime;

    /**
     * 申请时间
     */
    private LocalDateTime createdTime;

    /**
     * 创建时间（兼容字段）
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 主体类型文本
     */
    private String entityTypeText;

    /**
     * 店铺类型文本
     */
    private String shopTypeText;

    /**
     * 脱敏后的手机号
     */
    private String contactPhoneMasked;
}
