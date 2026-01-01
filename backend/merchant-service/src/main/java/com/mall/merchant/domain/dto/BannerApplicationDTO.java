package com.mall.merchant.domain.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

/**
 * 轮播图申请DTO
 * 用于商家提交轮播图投流申请
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-12-28
 */
@Data
public class BannerApplicationDTO {

    /**
     * 轮播图图片URL
     */
    @NotBlank(message = "图片URL不能为空")
    @Size(max = 500, message = "图片URL最多500字符")
    private String imageUrl;

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最多100字")
    private String title;

    /**
     * 描述
     */
    @Size(max = 500, message = "描述最多500字")
    private String description;

    /**
     * 跳转链接
     */
    @NotBlank(message = "跳转链接不能为空")
    @Size(max = 500, message = "跳转链接最多500字符")
    private String targetUrl;

    /**
     * 展示开始日期
     */
    @NotNull(message = "开始日期不能为空")
    @FutureOrPresent(message = "开始日期不能是过去的日期")
    private LocalDate startDate;

    /**
     * 展示结束日期
     */
    @NotNull(message = "结束日期不能为空")
    @Future(message = "结束日期必须是未来日期")
    private LocalDate endDate;

    /**
     * 验证日期范围
     * 确保结束日期在开始日期之后
     * 
     * @return 是否有效
     */
    @AssertTrue(message = "结束日期必须在开始日期之后")
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) {
            return true; // 让@NotNull注解处理空值
        }
        return endDate.isAfter(startDate);
    }
}
