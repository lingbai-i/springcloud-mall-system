package com.mall.admin.domain.entity;

import com.mall.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商家实体类
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Merchant extends BaseEntity {
    
    private static final long serialVersionUID = 1L;
    
    /** 商家用户名 */
    private String username;
    
    /** 密码 */
    private String password;
    
    /** 店铺名称 */
    private String shopName;
    
    /** 商家类型：individual-个人，enterprise-企业 */
    private String merchantType;
    
    /** 联系人姓名 */
    private String contactName;
    
    /** 联系电话 */
    private String contactPhone;
    
    /** 邮箱 */
    private String email;
    
    /** 营业执照号 */
    private String businessLicense;
    
    /** 营业执照图片 */
    private String businessLicenseImage;
    
    /** 身份证号 */
    private String idCard;
    
    /** 身份证正面图片 */
    private String idCardFrontImage;
    
    /** 身份证反面图片 */
    private String idCardBackImage;
    
    /** 银行卡号 */
    private String bankCard;
    
    /** 开户行 */
    private String bankName;
    
    /** 开户名 */
    private String accountName;
    
    /** 店铺logo */
    private String shopLogo;
    
    /** 店铺描述 */
    private String shopDescription;
    
    /** 经营类目 */
    private String category;
    
    /** 详细地址 */
    private String address;
    
    /** 省份 */
    private String province;
    
    /** 城市 */
    private String city;
    
    /** 区县 */
    private String district;
    
    /** 审核状态：pending-待审核，approved-已通过，rejected-已拒绝 */
    private String auditStatus;
    
    /** 审核意见 */
    private String auditRemark;
    
    /** 审核时间 */
    private LocalDateTime auditTime;
    
    /** 审核人ID */
    private Long auditBy;
    
    /** 状态：0-禁用，1-正常 */
    private Integer status;
    
    /** 保证金 */
    private BigDecimal deposit;
    
    /** 是否已缴纳保证金 */
    private Boolean depositPaid;
    
    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;
    
    /** 最后登录IP */
    private String lastLoginIp;
    
    /** 登录次数 */
    private Integer loginCount;
    
    /**
     * 获取商家类型文本
     */
    public String getMerchantTypeText() {
        if (merchantType == null) {
            return "未知";
        }
        switch (merchantType) {
            case "individual":
                return "个人";
            case "enterprise":
                return "企业";
            default:
                return "未知";
        }
    }
    
    /**
     * 获取审核状态文本
     */
    public String getAuditStatusText() {
        if (auditStatus == null) {
            return "未知";
        }
        switch (auditStatus) {
            case "pending":
                return "待审核";
            case "approved":
                return "已通过";
            case "rejected":
                return "已拒绝";
            default:
                return "未知";
        }
    }
    
    /**
     * 获取状态文本
     */
    public String getStatusText() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "禁用";
            case 1:
                return "正常";
            default:
                return "未知";
        }
    }
    
    /**
     * 是否审核通过
     */
    public boolean isApproved() {
        return "approved".equals(auditStatus);
    }
    
    /**
     * 是否正常状态
     */
    public boolean isActive() {
        return Integer.valueOf(1).equals(status);
    }
}