package com.mall.merchant.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 商品库存更新DTO
 * 用于更新单个商品的库存信息
 * 
 * @author system
 * @since 2025-11-12
 */
@Data
@Schema(description = "商品库存更新DTO")
public class ProductInventoryUpdateDTO {

  @Schema(description = "商品ID", required = true)
  @NotNull(message = "商品ID不能为空")
  private Long productId;

  @Schema(description = "库存数量", required = true, example = "100")
  @NotNull(message = "库存数量不能为空")
  @Min(value = 0, message = "库存数量不能为负数")
  private Integer stock;

  @Schema(description = "库存预警阈值", example = "10")
  @Min(value = 0, message = "预警阈值不能为负数")
  private Integer lowStockThreshold;

  @Schema(description = "更新原因", example = "采购入库")
  private String reason;
}
