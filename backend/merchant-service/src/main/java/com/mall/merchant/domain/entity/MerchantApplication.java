package com.mall.merchant.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 商家入驻申请实体类
 * 
 * @author lingbai
 * @since 2025-11-12
 */
@Data
@Entity
@Table(name = "merchant_applications")
public class MerchantApplication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 主体类型：enterprise-企业, individual-个体, personal-个人
     */
    @Column(name = "entity_type", nullable = false, length = 20)
    private String entityType;
    
    /**
     * 店铺类型
     */
    @Column(name = "shop_type", length = 20)
    private String shopType;
    
    /**
     * 店铺名称
     */
    @Column(name = "shop_name", nullable = false, length = 100)
    private String shopName;
    
    /**
     * 联系人姓名
     */
    @Column(name = "contact_name", nullable = false, length = 50)
    private String contactName;
    
    /**
     * 联系电话
     */
    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone;
    
    /**
     * 邮箱地址
     */
    @Column(name = "email", length = 100)
    private String email;
    
    /**
     * 公司名称
     */
    @Column(name = "company_name", length = 100)
    private String companyName;
    
    /**
     * 统一社会信用代码
     */
    @Column(name = "credit_code", length = 18)
    private String creditCode;
    
    /**
     * 法人代表
     */
    @Column(name = "legal_person", length = 50)
    private String legalPerson;
    
    /**
     * 营业执照图片URL
     */
    @Column(name = "business_license", length = 500)
    private String businessLicense;
    
    /**
     * 身份证号
     */
    @Column(name = "id_card", length = 18)
    private String idCard;
    
    /**
     * 身份证正面照片URL
     */
    @Column(name = "id_card_front", length = 500)
    private String idCardFront;
    
    /**
     * 身份证反面照片URL
     */
    @Column(name = "id_card_back", length = 500)
    private String idCardBack;
    
    /**
     * 登录账号
     * 注意：不使用数据库唯一约束，由业务逻辑控制（允许被拒绝的申请重新提交）
     */
    @Column(name = "username", nullable = false, length = 50)
    private String username;
    
    /**
     * 登录密码（加密）
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    
    /**
     * 审批状态：0-待审批，1-已通过，2-已拒绝
     */
    @Column(name = "approval_status")
    private Integer approvalStatus = 0;
    
    /**
     * 审批备注
     */
    @Column(name = "approval_reason", columnDefinition = "TEXT")
    private String approvalReason;
    
    /**
     * 审批时间
     */
    @Column(name = "approval_time")
    private LocalDateTime approvalTime;
    
    /**
     * 审批人ID
     */
    @Column(name = "approval_by")
    private Long approvalBy;
    
    /**
     * 审批人用户名
     */
    @Column(name = "approval_by_name", length = 50)
    private String approvalByName;
    
    /**
     * 关联的商家ID（审批通过后创建）
     */
    @Column(name = "merchant_id")
    private Long merchantId;
    
    /**
     * 短信是否已发送
     */
    @Column(name = "sms_sent")
    private Boolean smsSent = false;
    
    /**
     * 短信发送时间
     */
    @Column(name = "sms_sent_time")
    private LocalDateTime smsSentTime;
    
    /**
     * 短信重试次数
     */
    @Column(name = "sms_retry_count")
    private Integer smsRetryCount = 0;
    
    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}

