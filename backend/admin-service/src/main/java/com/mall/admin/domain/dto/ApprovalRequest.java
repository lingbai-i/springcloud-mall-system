package com.mall.admin.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商家审批请求DTO
 * 
 * @author lingbai
 * @since 2025-11-11
 */
@Data
public class ApprovalRequest {

  /**
   * 是否通过审批
   */
  @NotNull(message = "审批结果不能为空")
  private Boolean approved;

  /**
   * 审批备注/拒绝原因
   */
  private String reason;
}
