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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商家审批服务实现
 * 
 * @author system
 * @since 2025-11-11
 */
@Slf4j
@Service
public class MerchantApprovalServiceImpl implements MerchantApprovalService {

  private final WebClient webClient;
  private static final String MERCHANT_SERVICE_URL = "http://localhost:8087"; // Merchant Service地址
  private static final String SMS_SERVICE_URL = "http://localhost:8089"; // SMS Service地址

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
        List<Map<String, Object>> records = (List<Map<String, Object>>) data.get("content");
        int total = (Integer) data.get("totalElements");

        return new PageImpl<>(records, PageRequest.of(page - 1, size), total);
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

      // 4. 调用Merchant Service更新审批状态
      Map<String, Object> updateData = new HashMap<>();
      updateData.put("approvalStatus", request.getApproved() ? 1 : 2);
      updateData.put("approvalReason", request.getReason());
      updateData.put("approvalBy", adminId);
      updateData.put("approvalByName", adminUsername);
      updateData.put("approvalTime", LocalDateTime.now().toString());

      String updateUrl = String.format("%s/api/merchants/applications/%d/status",
          MERCHANT_SERVICE_URL, applicationId);

      @SuppressWarnings("unchecked")
      Mono<Map<String, Object>> updateResponse = (Mono<Map<String, Object>>) (Mono<?>) webClient.put()
          .uri(updateUrl)
          .bodyValue(updateData)
          .retrieve()
          .bodyToMono(Map.class);

      updateResponse.block(); // 执行更新请求

      // 5. 如果通过，创建商家账号
      Long merchantId = null;
      if (request.getApproved()) {
        merchantId = createMerchantAccount(application);
        result.put("merchantId", merchantId);
        log.info("商家账号创建成功 - ID: {}", merchantId);
      }

      // 6. 记录审批日志
      recordApprovalLog(applicationId, adminId, adminUsername,
          request.getApproved() ? "approve" : "reject",
          request.getApproved() ? "approved" : "rejected",
          request.getReason(), ipAddress);

      // 7. 发送短信通知
      sendApprovalSms(application, request.getApproved(), request.getReason());

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
   * 创建商家账号（审批通过时）
   */
  private Long createMerchantAccount(Map<String, Object> application) {
    try {
      log.info("创建商家账号 - 店铺: {}", application.get("shopName"));

      // 构建商家账号数据
      Map<String, Object> merchantData = new HashMap<>();
      merchantData.put("username", application.get("username"));
      merchantData.put("password", application.get("password")); // 已加密
      merchantData.put("shopName", application.get("shopName"));
      merchantData.put("contactName", application.get("contactName"));
      merchantData.put("contactPhone", application.get("contactPhone"));
      merchantData.put("contactEmail", application.get("email"));
      merchantData.put("merchantType", "enterprise".equals(application.get("entityType")) ? 2 : 1);
      merchantData.put("approvalStatus", 1);
      merchantData.put("status", 1);

      // 企业信息
      if (application.get("companyName") != null) {
        merchantData.put("companyName", application.get("companyName"));
        merchantData.put("creditCode", application.get("creditCode"));
        merchantData.put("legalPerson", application.get("legalPerson"));
        merchantData.put("businessLicense", application.get("businessLicense"));
      }

      // 个人信息
      if (application.get("idCard") != null) {
        merchantData.put("idNumber", application.get("idCard"));
        merchantData.put("idFrontImage", application.get("idCardFront"));
        merchantData.put("idBackImage", application.get("idCardBack"));
      }

      // 调用Merchant Service创建账号
      String url = MERCHANT_SERVICE_URL + "/api/merchants/create";

      @SuppressWarnings("unchecked")
      Mono<Map<String, Object>> response = (Mono<Map<String, Object>>) (Mono<?>) webClient.post()
          .uri(url)
          .bodyValue(merchantData)
          .retrieve()
          .bodyToMono(Map.class);

      Map<String, Object> result = response.block();

      if (result != null && result.get("data") != null) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        return Long.valueOf(data.get("merchantId").toString());
      }

      throw new RuntimeException("商家账号创建失败");

    } catch (Exception e) {
      log.error("创建商家账号失败", e);
      throw new RuntimeException("创建商家账号失败: " + e.getMessage());
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
              error -> log.error("审批日志记录失败", error));

    } catch (Exception e) {
      log.error("记录审批日志异常", e);
      // 不抛出异常，避免影响主流程
    }
  }

  /**
   * 发送审批短信通知
   */
  private void sendApprovalSms(Map<String, Object> application, boolean approved, String reason) {
    try {
      String phone = (String) application.get("contactPhone");
      String shopName = (String) application.get("shopName");
      String username = (String) application.get("username");

      Map<String, Object> smsData = new HashMap<>();
      smsData.put("phoneNumber", phone);

      if (approved) {
        // 通过通知
        String message = String.format(
            "【在线商城】恭喜！您的商家入驻申请已审核通过！店铺名称：%s，登录账号：%s，请访问商家后台开启电商之旅！",
            shopName, username);
        smsData.put("purpose", "MERCHANT_APPROVAL_PASS");
        smsData.put("message", message);
      } else {
        // 拒绝通知
        String message = String.format(
            "【在线商城】很遗憾，您的商家入驻申请未通过审核。店铺名称：%s，拒绝原因：%s",
            shopName, reason != null ? reason : "未提供");
        smsData.put("purpose", "MERCHANT_APPROVAL_REJECT");
        smsData.put("message", message);
      }

      // 异步发送短信
      String url = SMS_SERVICE_URL + "/send";

      webClient.post()
          .uri(url)
          .bodyValue(smsData)
          .retrieve()
          .bodyToMono(Map.class)
          .subscribe(
              result -> log.info("审批短信发送成功 - 手机: {}", phone),
              error -> log.error("审批短信发送失败 - 手机: {}", phone, error));

    } catch (Exception e) {
      log.error("发送审批短信异常", e);
      // 不抛出异常，避免影响审批流程
    }
  }
}
