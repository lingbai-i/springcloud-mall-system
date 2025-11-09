package com.mall.admin.domain.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 用户状态更新请求DTO
 */
@Data
public class UpdateUserStatusRequest {
    
    @NotNull(message = "状态不能为空")
    private Integer status;
    
    /**
     * 操作原因
     */
    private String reason;
}
