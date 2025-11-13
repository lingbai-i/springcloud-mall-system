package com.mall.merchant.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量更新库存DTO
 * 用于批量更新多个商品的库存信息
 * 
 * @author system
 * @since 2025-11-12
 */
@Data
@Schema(description = "批量更新库存DTO")
public class BatchInventoryUpdateDTO {

  @Schema(description = "商家ID", required = true)
  @NotNull(message = "商家ID不能为空")
  private Long merchantId;

  @Schema(description = "商品库存列表", required = true)
  @NotEmpty(message = "商品库存列表不能为空")
  @Valid
  private List<ProductInventoryUpdateDTO> items;
}
