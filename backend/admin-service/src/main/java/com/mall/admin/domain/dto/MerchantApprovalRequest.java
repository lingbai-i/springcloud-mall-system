package com.mall.admin.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 商家审核请求DTO
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Data
public class MerchantApprovalRequest {
    
    /** 商家ID */
    @NotNull(message = "商家ID不能为空")
    private Long merchantId;
    
    /** 审核结果：approved-通过，rejected-拒绝 */
    @NotBlank(message = "审核结果不能为空")
    private String result;
    
    /** 审核原因 */
    private String reason;
    
    /** 权限列表 */
    private List<String> permissions;
}