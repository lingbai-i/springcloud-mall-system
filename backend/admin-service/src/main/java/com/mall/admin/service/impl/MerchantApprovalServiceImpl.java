package com.mall.admin.service.impl;

import com.mall.admin.domain.dto.ApprovalRequest;
import com.mall.admin.service.MerchantApprovalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商家审批服务实现
 * 
 * @author lingbai
 * @since 2025-11-11
 */
@Slf4j
@Service
public class MerchantApprovalServiceImpl implements MerchantApprovalService {

  private final WebClient webClient;
  // Docker环境使用服务名，本地开发使用localhost
  private static final String MERCHANT_SERVICE_URL = "http://merchant-service:8087"; // Merchant Service地址

  public MerchantApprovalServiceImpl() {
    this.webClient = WebClient.builder()
        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
        .build();
  }

  @Override
  @SuppressWarnings("null")
  public Page<Map<String, Object>> getPendingApplications(Integer page, Integer size, Integer status, String keyword) {
    log.info("查询申请列表 - page: {}, size: {}, status: {}, keyword: {}", page, size, status, keyword);

    try {
      // 调用Merchant Service获取申请列表
      String url = String.format("%s/api/merchants/applications?page=%d&size=%d",
          MERCHANT_SERVICE_URL, page, size);

      if (status != null) {
        url += "&status=" + status;
      }
      if (keyword != null && !keyword.trim().isEmpty()) {
        url += "&keyword=" + keyword;
      }

      @SuppressWarnings("unchecked")
      Mono<Map<String, Object>> response = (Mono<Map<String, Object>>) (Mono<?>) webClient.get()
          .uri(url)
          .retrieve()
          .bodyToMono(Map.class);

      Map<String, Object> result = response.block();

      if (result != null && result.get("data") != null) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> records = (List<Map<String, Object>>) data.get("records");
        // 兼容不同的字段名
        Object totalObj = data.get("total");
        if (totalObj == null) {
          totalObj = data.get("totalElements");
        }
        long total = totalObj != null ? ((Number) totalObj).longValue() : 0L;

        return new PageImpl<>(records != null ? records : List.of(), PageRequest.of(page - 1, size), total);
      }

      return Page.empty();

    } catch (Exception e) {
      log.error("查询申请列表失败", e);
      throw new RuntimeException("查询申请列表失败: " + e.getMessage());
    }
  }

  @Override
  @SuppressWarnings("null")
  public Map<String, Object> approveApplication(Long applicationId, ApprovalRequest request,
      Long adminId, String adminUsername, String ipAddress) {
    log.info("审批商家申请 - ID: {}, 审批结果: {}, 管理员: {}",
        applicationId, request.getApproved() ? "通过" : "拒绝", adminUsername);

    Map<String, Object> result = new HashMap<>();

    try {
      // 1. 获取申请详情
      Map<String, Object> application = getApplicationDetail(applicationId);
      Integer currentStatus = (Integer) application.get("approvalStatus");

      // 2. 检查是否已审批
      if (currentStatus != null && currentStatus != 0) {
        throw new RuntimeException("该申请已被审批，无法重复操作");
      }

      // 3. 验证拒绝原因
      if (!request.getApproved() && (request.getReason() == null || request.getReason().trim().isEmpty())) {
        throw new RuntimeException("拒绝申请时必须填写原因");
      }

      // 4. 调用Merchant Service审核接口（使用查询参数）
      // merchant-service 的 auditApplication 方法会自动创建商家账号（如果通过）
      String reason = request.getReason() != null ? request.getReason() : "";
      String auditUrl = String.format("%s/api/merchants/applications/%d/audit?approved=%s&reason=%s&adminId=%d&adminName=%s",
          MERCHANT_SERVICE_URL, applicationId, request.getApproved(), 
          java.net.URLEncoder.encode(reason, java.nio.charset.StandardCharsets.UTF_8),
          adminId, java.net.URLEncoder.encode(adminUsername, java.nio.charset.StandardCharsets.UTF_8));

      log.info("调用审核接口: {}", auditUrl);

      @SuppressWarnings("unchecked")
      Mono<Map<String, Object>> auditResponse = (Mono<Map<String, Object>>) (Mono<?>) webClient.put()
          .uri(auditUrl)
          .retrieve()
          .bodyToMono(Map.class);

      Map<String, Object> auditResult = auditResponse.block();
      log.info("审核接口返回: {}", auditResult);

      // 5. 记录审批日志
      recordApprovalLog(applicationId, adminId, adminUsername,
          request.getApproved() ? "approve" : "reject",
          request.getApproved() ? "approved" : "rejected",
          request.getReason(), ipAddress);

      result.put("success", true);
      result.put("applicationId", applicationId);
      result.put("approvalStatus", request.getApproved() ? 1 : 2);
      result.put("smsSent", true);

      log.info("商家申请审批成功 - ID: {}, 结果: {}", applicationId, request.getApproved() ? "通过" : "拒绝");

      return result;

    } catch (Exception e) {
      log.error("审批操作失败", e);
      throw new RuntimeException("审批操作失败: " + e.getMessage());
    }
  }

  @Override
  @SuppressWarnings("null")
  public Map<String, Object> getApplicationDetail(Long applicationId) {
    log.info("获取申请详情 - ID: {}", applicationId);

    try {
      String url = String.format("%s/api/merchants/applications/%d", MERCHANT_SERVICE_URL, applicationId);

      @SuppressWarnings("unchecked")
      Mono<Map<String, Object>> response = (Mono<Map<String, Object>>) (Mono<?>) webClient.get()
          .uri(url)
          .retrieve()
          .bodyToMono(Map.class);

      Map<String, Object> result = response.block();

      if (result != null && result.get("data") != null) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        return data;
      }

      throw new RuntimeException("申请不存在");

    } catch (Exception e) {
      log.error("获取申请详情失败", e);
      throw new RuntimeException("获取申请详情失败: " + e.getMessage());
    }
  }

  /**
   * 记录审批日志
   */
  private void recordApprovalLog(Long applicationId, Long adminId, String adminUsername,
      String action, String approvalResult, String reason, String ipAddress) {
    try {
      Map<String, Object> logData = new HashMap<>();
      logData.put("applicationId", applicationId);
      logData.put("adminId", adminId);
      logData.put("adminUsername", adminUsername);
      logData.put("action", action);
      logData.put("approvalResult", approvalResult);
      logData.put("reason", reason);
      logData.put("ipAddress", ipAddress);

      String url = MERCHANT_SERVICE_URL + "/api/merchants/approval-logs";

      webClient.post()
          .uri(url)
          .bodyValue(logData)
          .retrieve()
          .bodyToMono(Void.class)
          .subscribe(
              result -> log.info("审批日志记录成功"),
              error -> log.warn("审批日志记录失败（非关键错误）: {}", error.getMessage()));

    } catch (Exception e) {
      log.warn("记录审批日志异常（非关键错误）: {}", e.getMessage());
      // 不抛出异常，避免影响主流程
    }
  }
}
