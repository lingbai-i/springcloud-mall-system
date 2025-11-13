package com.mall.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 令牌验证请求DTO
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-11-11
 */
@Data
@Schema(description = "令牌验证请求")
public class TokenValidationRequest {

    /**
     * 访问令牌
     */
    @NotBlank(message = "令牌不能为空")
    @Schema(description = "访问令牌", required = true)
    private String token;
}
