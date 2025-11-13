package com.mall.product.feign;

import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 商家商品服务Feign客户端
 * 用于从merchant-service获取商品数据
 * 
 * @author system
 * @since 2025-11-12
 */
@FeignClient(name = "merchant-service", contextId = "merchantProduct")
public interface MerchantProductClient {

  /**
   * 获取商品列表
   */
  @GetMapping("/merchant/products/list")
  R<PageResult<Map<String, Object>>> getProductList(
      @RequestParam(name = "merchantId") Long merchantId,
      @RequestParam(name = "page", defaultValue = "1") Integer page,
      @RequestParam(name = "size", defaultValue = "10") Integer size,
      @RequestParam(name = "status", required = false) Integer status);

  /**
   * 获取热销商品
   */
  @GetMapping("/merchant/products/hot-selling")
  R<java.util.List<Map<String, Object>>> getHotSellingProducts(
      @RequestParam(name = "merchantId") Long merchantId,
      @RequestParam(name = "limit", defaultValue = "10") Integer limit);

  /**
   * 获取推荐商品
   */
  @GetMapping("/merchant/products/recommended")
  R<PageResult<Map<String, Object>>> getRecommendedProducts(
      @RequestParam(name = "merchantId") Long merchantId,
      @RequestParam(name = "page", defaultValue = "1") Integer page,
      @RequestParam(name = "size", defaultValue = "10") Integer size);
}
