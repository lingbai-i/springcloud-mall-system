package com.mall.admin.controller;

import com.mall.admin.client.MerchantServiceClient;
import com.mall.admin.client.OrderServiceClient;
import com.mall.admin.client.UserServiceClient;
import com.mall.admin.mapper.AdminMapper;
import com.mall.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 仪表盘控制器
 * 
 * @author system
 * @since 2025-11-13
 */
@Slf4j
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

  private final UserServiceClient userServiceClient;
  private final MerchantServiceClient merchantServiceClient;
  private final OrderServiceClient orderServiceClient;
  private final AdminMapper adminMapper;

  /**
   * 获取仪表盘统计数据
   */
  @GetMapping("/stats")
  public R<Map<String, Object>> getDashboardStats() {
    log.info("获取仪表盘统计数据");

    Map<String, Object> stats = new HashMap<>();

    try {
      // 获取用户统计
      R<Map<String, Object>> userStats = userServiceClient.getUserStatistics();
      if (userStats != null && userStats.isSuccess() && userStats.getData() != null) {
        stats.putAll(userStats.getData());
      }
    } catch (Exception e) {
      log.error("获取用户统计失败", e);
      stats.put("totalUsers", 0);
      stats.put("activeUsers", 0);
    }

    try {
      // 获取商家统计
      R<Map<String, Object>> merchantStats = merchantServiceClient.getMerchantStatistics();
      if (merchantStats != null && merchantStats.isSuccess() && merchantStats.getData() != null) {
        stats.putAll(merchantStats.getData());
      }
    } catch (Exception e) {
      log.error("获取商家统计失败", e);
      stats.put("totalMerchants", 0);
      stats.put("activeMerchants", 0);
    }

    try {
      // 获取订单统计（包含交易额）
      R<Map<String, Object>> orderStats = orderServiceClient.getAdminOrderStats();
      if (orderStats != null && orderStats.isSuccess() && orderStats.getData() != null) {
        stats.putAll(orderStats.getData());
      }
    } catch (Exception e) {
      log.error("获取订单统计失败", e);
      stats.put("totalOrders", 0);
      stats.put("totalTransactionAmount", 0);
      stats.put("ordersTrend", 0);
      stats.put("transactionTrend", 0);
    }

    // 如果没有数据，返回默认值
    stats.putIfAbsent("totalUsers", 0);
    stats.putIfAbsent("usersTrend", 0);
    stats.putIfAbsent("activeUsers", 0);
    stats.putIfAbsent("totalOrders", 0);
    stats.putIfAbsent("ordersTrend", 0);
    stats.putIfAbsent("totalTransactionAmount", 0);
    stats.putIfAbsent("transactionTrend", 0);
    stats.putIfAbsent("todayOrders", 0);
    stats.putIfAbsent("todayTransactionAmount", 0);
    stats.putIfAbsent("totalMerchants", 0);
    stats.putIfAbsent("merchantsTrend", 0);
    stats.putIfAbsent("activeMerchants", 0);

    return R.ok(stats);
  }

  /**
   * 获取销售趋势数据
   */
  @GetMapping("/sales-trend")
  public R<Map<String, Object>> getSalesTrend(@RequestParam(value = "period", defaultValue = "30d") String period) {
    log.info("获取销售趋势数据 - period: {}", period);

    // 解析周期参数
    int days = 30;
    if ("7d".equals(period)) {
      days = 7;
    } else if ("90d".equals(period)) {
      days = 90;
    }

    try {
      R<Map<String, Object>> result = orderServiceClient.getSalesTrend(days);
      if (result != null && result.isSuccess() && result.getData() != null) {
        return R.ok(result.getData());
      }
    } catch (Exception e) {
      log.error("获取销售趋势数据失败", e);
    }

    // 返回空数据结构
    Map<String, Object> emptyResult = new HashMap<>();
    emptyResult.put("dates", new ArrayList<>());
    emptyResult.put("sales", new ArrayList<>());
    emptyResult.put("orders", new ArrayList<>());

    return R.ok(emptyResult);
  }

  /**
   * 获取用户分布数据
   */
  @GetMapping("/user-distribution")
  public R<Map<String, Object>> getUserDistribution() {
    log.info("获取用户分布数据");

    Map<String, Object> result = new HashMap<>();

    // 返回空数据结构
    result.put("regionDistribution", new ArrayList<>());
    result.put("ageDistribution", new ArrayList<>());

    return R.ok(result);
  }

  /**
   * 获取热门商品排行
   */
  @GetMapping("/top-products")
  public R<List<Map<String, Object>>> getTopProducts(
      @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
    log.info("获取热门商品排行 - limit: {}", limit);

    // 返回空列表
    return R.ok(new ArrayList<>());
  }

  /**
   * 获取最近订单
   */
  @GetMapping("/recent-orders")
  public R<List<Map<String, Object>>> getRecentOrders(
      @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
    log.info("获取最近订单 - limit: {}", limit);

    try {
      R<List<Map<String, Object>>> result = orderServiceClient.getRecentOrders(limit);
      if (result != null && result.isSuccess() && result.getData() != null) {
        return R.ok(result.getData());
      }
    } catch (Exception e) {
      log.error("获取最近订单失败", e);
    }

    // 返回空列表
    return R.ok(new ArrayList<>());
  }

  /**
   * 获取系统通知
   */
  @GetMapping("/notifications")
  public R<List<Map<String, Object>>> getNotifications(
      @RequestParam(value = "limit", defaultValue = "5") Integer limit) {
    log.info("获取系统通知 - limit: {}", limit);

    // 返回空列表
    return R.ok(new ArrayList<>());
  }

  /**
   * 获取待处理事项统计
   */
  @GetMapping("/pending-items")
  public R<Map<String, Object>> getPendingItems() {
    log.info("获取待处理事项统计");

    Map<String, Object> pendingItems = new HashMap<>();

    try {
      // 获取待审核商家数量
      R<Map<String, Object>> merchantStats = merchantServiceClient.getMerchantStatistics();
      if (merchantStats != null && merchantStats.isSuccess() && merchantStats.getData() != null) {
        pendingItems.put("pendingMerchants", merchantStats.getData().getOrDefault("pendingMerchants", 0));
      } else {
        pendingItems.put("pendingMerchants", 0);
      }
    } catch (Exception e) {
      log.error("获取待审核商家数量失败", e);
      pendingItems.put("pendingMerchants", 0);
    }

    try {
      // 获取待处理退款数量（需要管理员/客服介入的退款纠纷）
      R<Map<String, Object>> orderStats = orderServiceClient.getAdminOrderStats();
      if (orderStats != null && orderStats.isSuccess() && orderStats.getData() != null) {
        // 退款中的订单数量
        Object refundPending = orderStats.getData().get("refund_pending");
        pendingItems.put("pendingRefunds", refundPending != null ? refundPending : 0);
      } else {
        pendingItems.put("pendingRefunds", 0);
      }
    } catch (Exception e) {
      log.error("获取待处理退款数量失败", e);
      pendingItems.put("pendingRefunds", 0);
    }

    // 待审核商品（暂时设为0，后续可接入商品审核功能）
    pendingItems.put("pendingProducts", 0);
    // 用户举报/投诉（暂时设为0，后续可接入投诉系统）
    pendingItems.put("userReports", 0);

    return R.ok(pendingItems);
  }
}
