package com.mall.merchant.controller;

import com.mall.merchant.service.MerchantApplicationService;
import com.mall.merchant.service.MerchantService;
import com.mall.merchant.domain.vo.MerchantApplicationVO;
import com.mall.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * 商家申请管理接口（供管理员使用）
 * 简化版：直接在merchant-service中实现，避免跨服务调用
 * 
 * @author system
 * @since 2025-11-11
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/merchants")
@RequiredArgsConstructor
public class MerchantAdminController {

  private final MerchantApplicationService applicationService;
  private final MerchantService merchantService;

  /**
   * 获取商家统计数据（管理员）
   * 供 admin-service Feign 调用
   */
  @GetMapping("/statistics")
  public R<Map<String, Object>> getMerchantStatistics() {
    log.info("管理员获取商家统计数据");
    
    try {
      R<Map<String, Object>> result = merchantService.getMerchantStatistics();
      if (result.isSuccess()) {
        return R.ok(result.getData());
      }
      return R.fail("获取商家统计失败");
    } catch (Exception e) {
      log.error("获取商家统计失败", e);
      return R.fail("获取商家统计失败: " + e.getMessage());
    }
  }

  /**
   * 获取申请列表（管理员）
   */
  @GetMapping("/applications")
  public R<Map<String, Object>> getApplications(
      @RequestParam(value = "page", defaultValue = "1") Integer page,
      @RequestParam(value = "size", defaultValue = "20") Integer size,
      @RequestParam(value = "status", required = false) Integer status,
      @RequestParam(value = "keyword", required = false) String keyword) {

    log.info("管理员查询申请列表");

    try {
      Page<MerchantApplicationVO> applications = applicationService.getApplicationList(page, size, status, keyword);

      Map<String, Object> data = new HashMap<>();
      data.put("total", applications.getTotalElements());
      data.put("page", page);
      data.put("size", size);
      data.put("records", applications.getContent());

      return R.ok(data);

    } catch (Exception e) {
      log.error("查询失败", e);
      return R.fail("查询失败: " + e.getMessage());
    }
  }

  /**
   * 审批申请（管理员）
   */
  @PutMapping("/applications/{id}/approve")
  public R<String> approveApplication(
      @PathVariable Long id,
      @RequestBody Map<String, String> request,
      HttpServletRequest httpRequest) {

    Boolean approved = Boolean.valueOf(request.get("approved"));
    String reason = request.get("reason");

    log.info("审批申请 - ID: {}, 结果: {}, 原因: {}", id, approved ? "通过" : "拒绝", reason);

    try {
      // TODO: 实现审批逻辑
      return R.ok(approved ? "审批通过" : "已拒绝");
    } catch (Exception e) {
      log.error("审批失败", e);
      return R.fail("审批失败: " + e.getMessage());
    }
  }

  /**
   * 获取商家列表（管理员）
   */
  @GetMapping("")
  public R<Map<String, Object>> getMerchantList(
      @RequestParam(value = "page", defaultValue = "1") Integer page,
      @RequestParam(value = "size", defaultValue = "10") Integer size,
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "status", required = false) Integer status) {

    log.info("管理员查询商家列表 - page: {}, size: {}, keyword: {}, status: {}", page, size, keyword, status);

    try {
      // 调用 merchantService 查询商家列表
      R<com.mall.common.core.domain.PageResult<com.mall.merchant.domain.entity.Merchant>> listResult = merchantService
          .getMerchantList(page, size, keyword, null, null, status);

      // 获取统计数据
      R<Map<String, Object>> statsResult = merchantService.getMerchantStatistics();

      Map<String, Object> data = new HashMap<>();

      if (listResult.isSuccess() && listResult.getData() != null) {
        data.put("total", listResult.getData().getTotal());
        data.put("list", listResult.getData().getRecords());
      } else {
        data.put("total", 0);
        data.put("list", java.util.Collections.emptyList());
      }

      if (statsResult.isSuccess() && statsResult.getData() != null) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", statsResult.getData().getOrDefault("totalMerchants", 0));
        stats.put("pending", statsResult.getData().getOrDefault("pendingMerchants", 0));
        stats.put("approved", statsResult.getData().getOrDefault("approvedMerchants", 0));
        stats.put("active", statsResult.getData().getOrDefault("activeMerchants", 0));
        data.put("stats", stats);
      }

      return R.ok(data);

    } catch (Exception e) {
      log.error("查询商家列表失败", e);
      return R.fail("查询失败: " + e.getMessage());
    }
  }
}
