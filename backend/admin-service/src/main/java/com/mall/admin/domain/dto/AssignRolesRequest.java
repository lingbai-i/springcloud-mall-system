package com.mall.admin.domain.dto;

import lombok.Data;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 分配角色请求DTO
 */
@Data
public class AssignRolesRequest {
    
    @NotEmpty(message = "角色ID列表不能为空")
    private List<Long> roleIds;
}
