package com.mall.admin.domain.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * 商家审批请求DTO
 */
@Data
public class MerchantApprovalRequest {
    
    @NotNull(message = "审批结果不能为空")
    private Boolean approved;
    
    /**
     * 审批意见
     */
    private String reason;
}
