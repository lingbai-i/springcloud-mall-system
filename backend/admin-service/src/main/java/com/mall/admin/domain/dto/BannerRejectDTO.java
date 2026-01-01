package com.mall.admin.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 轮播图拒绝请求DTO
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-12-28
 */
@Data
public class BannerRejectDTO {

    /**
     * 拒绝原因（必填）
     */
    @NotBlank(message = "拒绝原因不能为空")
    @Size(max = 500, message = "拒绝原因最多500字")
    private String reason;
}
