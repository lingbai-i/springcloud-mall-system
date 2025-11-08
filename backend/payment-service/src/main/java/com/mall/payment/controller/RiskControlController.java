package com.mall.payment.controller;

import com.mall.payment.annotation.RequirePermission;
import com.mall.payment.entity.RiskRecord;
import com.mall.payment.entity.RiskRule;
import com.mall.payment.service.RiskControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 风控管理控制器
 * 提供风控规则和风控记录的管理接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@RestController
@RequestMapping("/api/risk")
public class RiskControlController {

    private static final Logger logger = LoggerFactory.getLogger(RiskControlController.class);

    @Autowired
    private RiskControlService riskControlService;

    // ==================== 风控记录相关接口 ====================

    /**
     * 根据支付订单ID查询风控记录
     * 
     * @param paymentOrderId 支付订单ID
     * @return 风控记录
     */
    @GetMapping("/records/payment/{paymentOrderId}")
    @RequirePermission(value = {"ADMIN"})
    public ResponseEntity<Map<String, Object>> getRiskRecordByPaymentOrderId(
            @PathVariable @NotBlank String paymentOrderId) {
        
        logger.info("查询风控记录，支付订单ID: {}", paymentOrderId);
        
        try {
            RiskRecord record = riskControlService.getRiskRecordByPaymentOrderId(paymentOrderId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", record);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("查询风控记录异常，支付订单ID: {}", paymentOrderId, e);
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 根据业务订单ID查询风控记录
     * 
     * @param businessOrderId 业务订单ID
     * @return 风控记录
     */
    @GetMapping("/records/business/{businessOrderId}")
    public ResponseEntity<Map<String, Object>> getRiskRecordByBusinessOrderId(
            @PathVariable @NotBlank String businessOrderId) {
        
        logger.info("查询风控记录，业务订单ID: {}", businessOrderId);
        
        try {
            RiskRecord record = riskControlService.getRiskRecordByBusinessOrderId(businessOrderId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", record);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("查询风控记录异常，业务订单ID: {}", businessOrderId, e);
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 分页查询风控记录
     * 
     * @param page 页码（从0开始）
     * @param size 页大小
     * @param sort 排序字段
     * @param direction 排序方向
     * @return 风控记录分页结果
     */
    @GetMapping("/records")
    @RequirePermission(value = {"ADMIN"})
    public ResponseEntity<Map<String, Object>> getRiskRecords(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        logger.info("分页查询风控记录，页码: {}, 页大小: {}", page, size);
        
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            
            Page<RiskRecord> records = riskControlService.getRiskRecords(pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", records.getContent());
            response.put("totalElements", records.getTotalElements());
            response.put("totalPages", records.getTotalPages());
            response.put("currentPage", records.getNumber());
            response.put("pageSize", records.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("分页查询风控记录异常", e);
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 根据用户ID分页查询风控记录
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 页大小
     * @return 风控记录分页结果
     */
    @GetMapping("/records/user/{userId}")
    public ResponseEntity<Map<String, Object>> getRiskRecordsByUserId(
            @PathVariable @NotBlank String userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        
        logger.info("根据用户ID查询风控记录，用户ID: {}", userId);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<RiskRecord> records = riskControlService.getRiskRecordsByUserId(userId, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", records.getContent());
            response.put("totalElements", records.getTotalElements());
            response.put("totalPages", records.getTotalPages());
            response.put("currentPage", records.getNumber());
            response.put("pageSize", records.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("根据用户ID查询风控记录异常，用户ID: {}", userId, e);
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 查询待审核的风控记录
     * 
     * @param page 页码
     * @param size 页大小
     * @return 待审核的风控记录分页结果
     */
    @GetMapping("/records/pending-review")
    public ResponseEntity<Map<String, Object>> getPendingReviewRecords(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        
        logger.info("查询待审核的风控记录");
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<RiskRecord> records = riskControlService.getPendingReviewRecords(pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", records.getContent());
            response.put("totalElements", records.getTotalElements());
            response.put("totalPages", records.getTotalPages());
            response.put("currentPage", records.getNumber());
            response.put("pageSize", records.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("查询待审核风控记录异常", e);
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 审核风控记录
     * 
     * @param recordId 记录ID
     * @param request 审核请求
     * @return 审核结果
     */
    @PostMapping("/records/{recordId}/review")
    @RequirePermission(value = {"ADMIN"})
    public ResponseEntity<Map<String, Object>> reviewRiskRecord(
            @PathVariable @NotBlank String recordId,
            @RequestBody @Valid ReviewRequest request) {
        
        logger.info("审核风控记录，记录ID: {}, 审核状态: {}", recordId, request.getStatus());
        
        try {
            boolean success = riskControlService.reviewRiskRecord(
                    recordId, request.getStatus(), request.getReviewer(), request.getComment());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "审核成功" : "审核失败");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("审核风控记录异常，记录ID: {}", recordId, e);
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 标记风控记录为误报
     * 
     * @param recordId 记录ID
     * @param request 标记请求
     * @return 标记结果
     */
    @PostMapping("/records/{recordId}/false-positive")
    public ResponseEntity<Map<String, Object>> markAsFalsePositive(
            @PathVariable @NotBlank String recordId,
            @RequestBody @Valid FalsePositiveRequest request) {
        
        logger.info("标记风控记录为误报，记录ID: {}", recordId);
        
        try {
            boolean success = riskControlService.markAsFalsePositive(
                    recordId, request.getReviewer(), request.getComment());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "标记成功" : "标记失败");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("标记误报异常，记录ID: {}", recordId, e);
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 查询高风险交易记录
     * 
     * @param minRiskScore 最小风险评分
     * @param page 页码
     * @param size 页大小
     * @return 高风险交易记录分页结果
     */
    @GetMapping("/records/high-risk")
    public ResponseEntity<Map<String, Object>> getHighRiskRecords(
            @RequestParam @NotNull BigDecimal minRiskScore,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        
        logger.info("查询高风险交易记录，最小风险评分: {}", minRiskScore);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<RiskRecord> records = riskControlService.getHighRiskRecords(minRiskScore, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", records.getContent());
            response.put("totalElements", records.getTotalElements());
            response.put("totalPages", records.getTotalPages());
            response.put("currentPage", records.getNumber());
            response.put("pageSize", records.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("查询高风险交易记录异常", e);
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取风控统计数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 风控统计数据
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getRiskStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        logger.info("获取风控统计数据，时间范围: {} - {}", startTime, endTime);
        
        try {
            RiskControlService.RiskStatistics statistics = riskControlService.getRiskStatistics(startTime, endTime);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", statistics);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取风控统计数据异常", e);
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // ==================== 风控规则相关接口 ====================

    /**
     * 创建风控规则
     * 
     * @param rule 风控规则
     * @return 创建的风控规则
     */
    @PostMapping("/rules")
    public ResponseEntity<Map<String, Object>> createRiskRule(@RequestBody @Valid RiskRule rule) {
        logger.info("创建风控规则，规则名称: {}", rule.getRuleName());
        
        try {
            RiskRule createdRule = riskControlService.createRiskRule(rule);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "创建成功");
            response.put("data", createdRule);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("创建风控规则异常", e);
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 更新风控规则
     * 
     * @param ruleId 规则ID
     * @param rule 风控规则
     * @return 更新的风控规则
     */
    @PutMapping("/rules/{ruleId}")
    public ResponseEntity<Map<String, Object>> updateRiskRule(
            @PathVariable @NotBlank String ruleId,
            @RequestBody @Valid RiskRule rule) {
        
        logger.info("更新风控规则，规则ID: {}", ruleId);
        
        try {
            RiskRule updatedRule = riskControlService.updateRiskRule(ruleId, rule);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "更新成功");
            response.put("data", updatedRule);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("更新风控规则异常，规则ID: {}", ruleId, e);
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 删除风控规则
     * 
     * @param ruleId 规则ID
     * @return 删除结果
     */
    @DeleteMapping("/rules/{ruleId}")
    public ResponseEntity<Map<String, Object>> deleteRiskRule(@PathVariable @NotBlank String ruleId) {
        logger.info("删除风控规则，规则ID: {}", ruleId);
        
        try {
            boolean success = riskControlService.deleteRiskRule(ruleId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "删除成功" : "删除失败");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("删除风控规则异常，规则ID: {}", ruleId, e);
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 根据ID查询风控规则
     * 
     * @param ruleId 规则ID
     * @return 风控规则
     */
    @GetMapping("/rules/{ruleId}")
    public ResponseEntity<Map<String, Object>> getRiskRuleById(@PathVariable @NotBlank String ruleId) {
        logger.debug("查询风控规则，规则ID: {}", ruleId);
        
        try {
            RiskRule rule = riskControlService.getRiskRuleById(ruleId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", rule);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("查询风控规则异常，规则ID: {}", ruleId, e);
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 分页查询风控规则
     * 
     * @param page 页码
     * @param size 页大小
     * @param sort 排序字段
     * @param direction 排序方向
     * @return 风控规则分页结果
     */
    @GetMapping("/rules")
    public ResponseEntity<Map<String, Object>> getRiskRules(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "priority") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        logger.debug("分页查询风控规则，页码: {}, 页大小: {}", page, size);
        
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            
            Page<RiskRule> rules = riskControlService.getRiskRules(pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", rules.getContent());
            response.put("totalElements", rules.getTotalElements());
            response.put("totalPages", rules.getTotalPages());
            response.put("currentPage", rules.getNumber());
            response.put("pageSize", rules.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("分页查询风控规则异常", e);
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 查询启用的风控规则
     * 
     * @return 启用的风控规则列表
     */
    @GetMapping("/rules/enabled")
    public ResponseEntity<Map<String, Object>> getEnabledRiskRules() {
        logger.debug("查询启用的风控规则");
        
        try {
            List<RiskRule> rules = riskControlService.getEnabledRiskRules();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", rules);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("查询启用风控规则异常", e);
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 启用/禁用风控规则
     * 
     * @param ruleId 规则ID
     * @param request 切换请求
     * @return 操作结果
     */
    @PostMapping("/rules/{ruleId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleRiskRule(
            @PathVariable @NotBlank String ruleId,
            @RequestBody @Valid ToggleRequest request) {
        
        logger.info("{}风控规则，规则ID: {}", request.isEnabled() ? "启用" : "禁用", ruleId);
        
        try {
            boolean success = riskControlService.toggleRiskRule(ruleId, request.isEnabled());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "操作成功" : "操作失败");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("切换风控规则状态异常，规则ID: {}", ruleId, e);
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 批量处理待审核的风控记录
     * 
     * @param timeoutHours 超时小时数
     * @return 处理结果
     */
    @PostMapping("/records/batch-process")
    public ResponseEntity<Map<String, Object>> processPendingReviewRecords(
            @RequestParam @Min(1) int timeoutHours) {
        
        logger.info("批量处理待审核风控记录，超时时间: {} 小时", timeoutHours);
        
        try {
            int processedCount = riskControlService.processPendingReviewRecords(timeoutHours);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "处理完成");
            response.put("processedCount", processedCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("批量处理待审核记录异常", e);
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 创建错误响应
     * 
     * @param message 错误信息
     * @return 错误响应
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }

    // ==================== 内部类定义 ====================

    /**
     * 审核请求
     */
    public static class ReviewRequest {
        @NotNull
        private RiskRecord.ReviewStatus status;
        
        @NotBlank
        private String reviewer;
        
        private String comment;

        public RiskRecord.ReviewStatus getStatus() {
            return status;
        }

        public void setStatus(RiskRecord.ReviewStatus status) {
            this.status = status;
        }

        public String getReviewer() {
            return reviewer;
        }

        public void setReviewer(String reviewer) {
            this.reviewer = reviewer;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }

    /**
     * 误报标记请求
     */
    public static class FalsePositiveRequest {
        @NotBlank
        private String reviewer;
        
        private String comment;

        public String getReviewer() {
            return reviewer;
        }

        public void setReviewer(String reviewer) {
            this.reviewer = reviewer;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }

    /**
     * 切换请求
     */
    public static class ToggleRequest {
        @NotNull
        private Boolean enabled;

        public Boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }
    }
}