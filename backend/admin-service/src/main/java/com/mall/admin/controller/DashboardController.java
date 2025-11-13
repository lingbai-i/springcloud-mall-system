package com.mall.admin.controller;

import com.mall.admin.client.MerchantServiceClient;
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

    // 如果没有数据，返回默认值
    stats.putIfAbsent("totalUsers", 0);
    stats.putIfAbsent("activeUsers", 0);
    stats.putIfAbsent("totalOrders", 0);
    stats.putIfAbsent("totalSales", 0);
    stats.putIfAbsent("todayOrders", 0);
    stats.putIfAbsent("todaySales", 0);
    stats.putIfAbsent("totalMerchants", 0);
    stats.putIfAbsent("activeMerchants", 0);

    return R.ok(stats);
  }

  /**
   * 获取销售趋势数据
   */
  @GetMapping("/sales-trend")
  public R<Map<String, Object>> getSalesTrend(@RequestParam(value = "period", defaultValue = "30d") String period) {
    log.info("获取销售趋势数据 - period: {}", period);

    Map<String, Object> result = new HashMap<>();

    // 返回空数据结构，前端会显示"暂无数据"
    result.put("dates", new ArrayList<>());
    result.put("sales", new ArrayList<>());
    result.put("orders", new ArrayList<>());

    return R.ok(result);
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
}
