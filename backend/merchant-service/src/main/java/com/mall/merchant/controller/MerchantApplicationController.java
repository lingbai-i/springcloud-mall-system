package com.mall.merchant.controller;

import com.mall.merchant.domain.dto.MerchantApplicationDTO;
import com.mall.merchant.domain.entity.MerchantApplication;
import com.mall.merchant.domain.vo.MerchantApplicationVO;
import com.mall.merchant.service.MerchantApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 商家申请控制器
 * 处理商家入驻申请相关接口
 * 
 * @author system
 * @since 2025-11-12
 */
@Slf4j
@RestController
@RequestMapping("/merchants")
@RequiredArgsConstructor
public class MerchantApplicationController {

  private final MerchantApplicationService applicationService;

  /**
   * 提交商家入驻申请
   * POST /merchants/apply
   */
  @PostMapping("/apply")
  public ResponseEntity<Map<String, Object>> submitApplication(
      @Valid @RequestBody MerchantApplicationDTO applicationDTO) {

    log.info("收到商家入驻申请 - shopName: {}, entityType: {}, contactPhone: {}",
        applicationDTO.getShopName(),
        applicationDTO.getEntityType(),
        applicationDTO.getContactPhone());

    Map<String, Object> response = new HashMap<>();

    try {
      // 保存申请到数据库
      Long applicationId = applicationService.submitApplication(applicationDTO);

      Map<String, Object> data = new HashMap<>();
      data.put("applicationId", applicationId);
      data.put("status", "pending"); // 待审批
      data.put("submittedAt", LocalDateTime.now().toString());
      data.put("shopName", applicationDTO.getShopName());

      response.put("code", 200);
      response.put("success", true);
      response.put("message", "申请提交成功，请等待审核");
      response.put("data", data);

      log.info("商家申请提交成功 - applicationId: {}, shopName: {}", applicationId, applicationDTO.getShopName());

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("提交商家申请失败", e);

      response.put("code", 500);
      response.put("success", false);
      response.put("message", "提交失败: " + e.getMessage());

      return ResponseEntity.status(500).body(response);
    }
  }

  /**
   * 查询申请详情
   * GET /merchants/applications/{id}
   */
  @GetMapping("/applications/{id}")
  public ResponseEntity<Map<String, Object>> getApplicationDetail(@PathVariable Long id) {
    try {
      MerchantApplication application = applicationService.getApplicationDetail(id);
      
      Map<String, Object> response = new HashMap<>();
      response.put("code", 200);
      response.put("success", true);
      response.put("data", application);
      
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("查询申请详情失败", e);
      
      Map<String, Object> response = new HashMap<>();
      response.put("code", 500);
      response.put("success", false);
      response.put("message", "查询失败: " + e.getMessage());
      
      return ResponseEntity.status(500).body(response);
    }
  }

  /**
   * 查询申请列表（分页）
   * GET /merchants/applications
   */
  @GetMapping("/applications")
  public ResponseEntity<Map<String, Object>> getApplicationList(
      @RequestParam(value = "page", defaultValue = "1") Integer page,
      @RequestParam(value = "size", defaultValue = "10") Integer size,
      @RequestParam(value = "status", required = false) Integer status,
      @RequestParam(value = "keyword", required = false) String keyword) {
    
    try {
      Page<MerchantApplicationVO> applicationPage = applicationService.getApplicationList(page, size, status, keyword);
      
      // 转换为前端需要的格式
      Map<String, Object> pageData = new HashMap<>();
      pageData.put("records", applicationPage.getContent());
      pageData.put("total", applicationPage.getTotalElements());
      pageData.put("size", applicationPage.getSize());
      pageData.put("current", applicationPage.getNumber() + 1);
      pageData.put("pages", applicationPage.getTotalPages());
      
      Map<String, Object> response = new HashMap<>();
      response.put("code", 200);
      response.put("success", true);
      response.put("data", pageData);
      
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("查询申请列表失败", e);
      
      Map<String, Object> response = new HashMap<>();
      response.put("code", 500);
      response.put("success", false);
      response.put("message", "查询失败: " + e.getMessage());
      
      return ResponseEntity.status(500).body(response);
    }
  }
  
  /**
   * 获取申请统计
   * GET /merchants/applications/stats
   */
  @GetMapping("/applications/stats")
  public ResponseEntity<Map<String, Object>> getApplicationStats() {
    try {
      Map<String, Object> stats = applicationService.getApplicationStats();
      
      Map<String, Object> response = new HashMap<>();
      response.put("code", 200);
      response.put("success", true);
      response.put("data", stats);
      
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("查询统计失败", e);
      
      Map<String, Object> response = new HashMap<>();
      response.put("code", 500);
      response.put("success", false);
      response.put("message", "查询失败: " + e.getMessage());
      
      return ResponseEntity.status(500).body(response);
    }
  }

  /**
   * 审核申请
   * PUT /merchants/applications/{id}/audit
   */
  @PutMapping("/applications/{id}/audit")
  public ResponseEntity<Map<String, Object>> auditApplication(
      @PathVariable Long id,
      @RequestParam Boolean approved,
      @RequestParam(required = false) String reason,
      @RequestParam Long adminId,
      @RequestParam String adminName) {
    
    try {
      applicationService.auditApplication(id, approved, reason, adminId, adminName);
      
      Map<String, Object> response = new HashMap<>();
      response.put("code", 200);
      response.put("success", true);
      response.put("message", approved ? "审核通过" : "审核拒绝");
      
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("审核申请失败", e);
      
      Map<String, Object> response = new HashMap<>();
      response.put("code", 500);
      response.put("success", false);
      response.put("message", "审核失败: " + e.getMessage());
      
      return ResponseEntity.status(500).body(response);
    }
  }
}
