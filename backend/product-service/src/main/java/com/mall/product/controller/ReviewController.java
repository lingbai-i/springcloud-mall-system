package com.mall.product.controller;

import com.mall.common.core.domain.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品评论控制器
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-11-13
 */
@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "*")
public class ReviewController {

  private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

  /**
   * 获取商品评论列表
   * 目前返回空列表，后续可以接入评论服务
   * 
   * @param id 商品ID
   * @return 统一响应结果，包含评论列表
   */
  @GetMapping("/{id}/reviews")
  public R<List<Object>> getProductReviews(@PathVariable Long id) {
    logger.info("获取商品评论 - 商品ID: {}", id);
    // TODO: 后续接入评论服务
    return R.ok(List.of());
  }
}
