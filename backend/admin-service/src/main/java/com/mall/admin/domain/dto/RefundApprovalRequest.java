package com.mall.admin.domain.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 退款审批请求DTO
 */
@Data
public class RefundApprovalRequest {
    
    @NotNull(message = "审批结果不能为空")
    private Boolean approved;
    
    @NotNull(message = "退款金额不能为空")
    private BigDecimal refundAmount;
    
    /**
     * 审批意见
     */
    private String reason;
}
