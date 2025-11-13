package com.mall.merchant.controller;

import com.mall.merchant.service.MerchantApplicationService;
import com.mall.merchant.service.MerchantService;
import com.mall.merchant.domain.vo.MerchantApplicationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
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
@RequestMapping({ "/api/admin/merchants", "/admin/merchants" })
@RequiredArgsConstructor
public class MerchantAdminController {

  private final MerchantApplicationService applicationService;
  private final MerchantService merchantService;

  /**
   * 获取申请列表（管理员）
   */
  @GetMapping("/applications")
  public ResponseEntity<Map<String, Object>> getApplications(
      @RequestParam(value = "page", defaultValue = "1") Integer page,
      @RequestParam(value = "size", defaultValue = "20") Integer size,
      @RequestParam(value = "status", required = false) Integer status,
      @RequestParam(value = "keyword", required = false) String keyword) {

    log.info("管理员查询申请列表");

    Map<String, Object> response = new HashMap<>();

    try {
      Page<MerchantApplicationVO> applications = applicationService.getApplicationList(page, size, status, keyword);

      Map<String, Object> data = new HashMap<>();
      data.put("total", applications.getTotalElements());
      data.put("page", page);
      data.put("size", size);
      data.put("records", applications.getContent());

      response.put("code", 200);
      response.put("success", true);
      response.put("data", data);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("查询失败", e);
      response.put("code", 500);
      response.put("success", false);
      response.put("message", e.getMessage());
      return ResponseEntity.status(500).body(response);
    }
  }

  /**
   * 审批申请（管理员）
   */
  @PutMapping("/applications/{id}/approve")
  public ResponseEntity<Map<String, Object>> approveApplication(
      @PathVariable Long id,
      @RequestBody Map<String, String> request,
      HttpServletRequest httpRequest) {

    Boolean approved = Boolean.valueOf(request.get("approved"));
    String reason = request.get("reason");

    log.info("审批申请 - ID: {}, 结果: {}, 原因: {}", id, approved ? "通过" : "拒绝", reason);

    Map<String, Object> response = new HashMap<>();

    try {
      // TODO: 实现审批逻辑
      response.put("code", 200);
      response.put("success", true);
      response.put("message", approved ? "审批通过" : "已拒绝");
      if (reason != null && !reason.isEmpty()) {
        response.put("reason", reason);
      }

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("审批失败", e);
      response.put("code", 500);
      response.put("success", false);
      response.put("message", e.getMessage());
      return ResponseEntity.status(500).body(response);
    }
  }

  /**
   * 获取商家列表（管理员）
   */
  @GetMapping("")
  public ResponseEntity<Map<String, Object>> getMerchantList(
      @RequestParam(value = "page", defaultValue = "1") Integer page,
      @RequestParam(value = "size", defaultValue = "10") Integer size,
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "status", required = false) Integer status) {

    log.info("管理员查询商家列表 - page: {}, size: {}, keyword: {}, status: {}", page, size, keyword, status);

    Map<String, Object> response = new HashMap<>();

    try {
      // 调用 merchantService 查询商家列表
      com.mall.common.core.domain.R<com.mall.common.core.domain.PageResult<com.mall.merchant.domain.entity.Merchant>> listResult = merchantService
          .getMerchantList(page, size, keyword, null, null, status);

      // 获取统计数据
      com.mall.common.core.domain.R<Map<String, Object>> statsResult = merchantService.getMerchantStatistics();

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

      response.put("code", 200);
      response.put("success", true);
      response.put("data", data);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("查询商家列表失败", e);
      response.put("code", 500);
      response.put("success", false);
      response.put("message", e.getMessage());
      return ResponseEntity.status(500).body(response);
    }
  }
}
