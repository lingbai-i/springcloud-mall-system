package com.mall.merchant.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存预警VO
 * 返回库存不足的商品信息
 * 
 * @author lingbai
 * @since 2025-11-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "库存预警VO")
public class InventoryAlertVO {

  @Schema(description = "商品ID")
  private Long productId;

  @Schema(description = "商品名称")
  private String productName;

  @Schema(description = "商品SKU")
  private String sku;

  @Schema(description = "当前库存")
  private Integer currentStock;

  @Schema(description = "预警阈值")
  private Integer threshold;

  @Schema(description = "缺货数量", example = "5")
  private Integer shortageQty;

  @Schema(description = "商品价格")
  private BigDecimal price;

  @Schema(description = "商品状态", example = "1:在售, 2:下架, 3:售罄")
  private Integer status;

  @Schema(description = "最后更新时间")
  private LocalDateTime updateTime;
}
