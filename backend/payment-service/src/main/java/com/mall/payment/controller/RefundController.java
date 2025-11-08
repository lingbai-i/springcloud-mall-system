package com.mall.payment.controller;

import com.mall.payment.annotation.RequirePermission;
import com.mall.payment.dto.request.RefundCreateRequest;
import com.mall.payment.dto.response.RefundOrderResponse;
import com.mall.payment.dto.response.PageResponse;
import com.mall.payment.enums.RefundStatus;
import com.mall.payment.service.RefundService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 退款控制器
 * 提供退款相关的RESTful API接口，包括创建退款订单、处理退款、查询退款状态等功能
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@RestController
@RequestMapping("/api/refund")
@Validated
public class RefundController {

    private static final Logger logger = LoggerFactory.getLogger(RefundController.class);

    @Autowired
    private RefundService refundService;

    /**
     * 创建退款订单
     * 根据支付订单创建退款订单，并进行必要的业务验证
     * 
     * @param request 创建退款订单的请求参数
     * @return 创建成功的退款订单响应
     */
    @PostMapping("/orders")
    @RequirePermission(value = {"USER", "ADMIN"}, allowOwner = true, ownerField = "userId")
    public ResponseEntity<Map<String, Object>> createRefundOrder(
            @Valid @RequestBody RefundCreateRequest request) {
        
        logger.info("创建退款订单请求，支付订单ID: {}, 用户ID: {}, 退款金额: {}", 
                   request.getPaymentOrderId(), request.getUserId(), request.getRefundAmount());

        try {
            RefundOrderResponse response = refundService.createRefundOrder(request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "退款订单创建成功");
            result.put("data", response);

            logger.info("退款订单创建成功，退款订单ID: {}", response.getId());
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.warn("创建退款订单参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("参数错误", e.getMessage()));
        } catch (IllegalStateException e) {
            logger.warn("创建退款订单状态错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("状态错误", e.getMessage()));
        } catch (Exception e) {
            logger.error("创建退款订单异常", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("系统异常", "创建退款订单失败"));
        }
    }

    /**
     * 根据退款订单ID查询退款详情
     * 
     * @param refundOrderId 退款订单ID
     * @return 退款订单详情
     */
    @GetMapping("/orders/{refundOrderId}")
    public ResponseEntity<Map<String, Object>> getRefundOrder(
            @PathVariable @NotBlank(message = "退款订单ID不能为空") String refundOrderId) {
        
        logger.debug("查询退款订单详情，退款订单ID: {}", refundOrderId);

        try {
            RefundOrderResponse response = refundService.getRefundOrder(refundOrderId);
            
            if (response == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("查询退款订单异常，退款订单ID: {}", refundOrderId, e);
            return ResponseEntity.internalServerError().body(createErrorResponse("系统异常", "查询退款订单失败"));
        }
    }

    /**
     * 根据支付订单ID查询退款订单列表
     * 
     * @param paymentOrderId 支付订单ID
     * @return 退款订单列表
     */
    @GetMapping("/orders/payment/{paymentOrderId}")
    public ResponseEntity<Map<String, Object>> getRefundOrdersByPaymentOrderId(
            @PathVariable @NotBlank(message = "支付订单ID不能为空") String paymentOrderId) {
        
        logger.debug("根据支付订单ID查询退款订单列表，支付订单ID: {}", paymentOrderId);

        try {
            List<RefundOrderResponse> responses = refundService.getRefundOrdersByPaymentOrderId(paymentOrderId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", responses);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("根据支付订单ID查询退款订单列表异常，支付订单ID: {}", paymentOrderId, e);
            return ResponseEntity.internalServerError().body(createErrorResponse("系统异常", "查询退款订单列表失败"));
        }
    }

    /**
     * 分页查询退款订单
     * 支持多种查询条件的组合查询
     * 
     * @param userId 用户ID（可选）
     * @param status 退款状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页查询结果
     */
    @GetMapping("/orders")
    @RequirePermission(value = {"ADMIN"})
    public ResponseEntity<Map<String, Object>> queryRefundOrders(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) RefundStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") int size) {
        
        logger.debug("分页查询退款订单，用户ID: {}, 状态: {}, 页码: {}, 大小: {}", userId, status, page, size);

        try {
            PageResponse<RefundOrderResponse> response = refundService.queryRefundOrders(
                    userId, status, startTime, endTime, page, size);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("分页查询退款订单异常", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("系统异常", "查询退款订单失败"));
        }
    }

    /**
     * 查询用户的退款订单列表
     * 
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页查询结果
     */
    @GetMapping("/orders/user/{userId}")
    @RequirePermission(value = {"USER", "ADMIN"}, allowOwner = true, ownerField = "userId")
    public ResponseEntity<Map<String, Object>> getUserRefundOrders(
            @PathVariable @NotBlank(message = "用户ID不能为空") String userId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") int size) {
        
        logger.debug("查询用户退款订单列表，用户ID: {}, 页码: {}, 大小: {}", userId, page, size);

        try {
            PageResponse<RefundOrderResponse> response = refundService.getUserRefundOrders(userId, page, size);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("查询用户退款订单列表异常，用户ID: {}", userId, e);
            return ResponseEntity.internalServerError().body(createErrorResponse("系统异常", "查询用户退款订单失败"));
        }
    }

    /**
     * 处理退款申请
     * 发起实际的退款流程，调用第三方支付平台退款接口
     * 
     * @param refundOrderId 退款订单ID
     * @return 处理结果
     */
    @PostMapping("/orders/{refundOrderId}/process")
    public ResponseEntity<Map<String, Object>> processRefund(
            @PathVariable @NotBlank(message = "退款订单ID不能为空") String refundOrderId) {
        
        logger.info("处理退款申请请求，退款订单ID: {}", refundOrderId);

        try {
            boolean success = refundService.processRefund(refundOrderId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("message", success ? "退款处理成功" : "退款处理失败");

            if (success) {
                logger.info("退款处理成功，退款订单ID: {}", refundOrderId);
                return ResponseEntity.ok(result);
            } else {
                logger.warn("退款处理失败，退款订单ID: {}", refundOrderId);
                return ResponseEntity.badRequest().body(result);
            }

        } catch (IllegalStateException e) {
            logger.warn("处理退款申请状态错误，退款订单ID: {}, 错误: {}", refundOrderId, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("状态错误", e.getMessage()));
        } catch (Exception e) {
            logger.error("处理退款申请异常，退款订单ID: {}", refundOrderId, e);
            return ResponseEntity.internalServerError().body(createErrorResponse("系统异常", "处理退款申请失败"));
        }
    }

    /**
     * 审核退款申请
     * 对退款申请进行人工审核，决定是否同意退款
     * 
     * @param refundOrderId 退款订单ID
     * @param approved 是否同意退款
     * @param auditReason 审核意见
     * @param auditorId 审核人ID
     * @return 审核结果
     */
    @PostMapping("/orders/{refundOrderId}/audit")
    public ResponseEntity<Map<String, Object>> auditRefund(
            @PathVariable @NotBlank(message = "退款订单ID不能为空") String refundOrderId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String auditReason,
            @RequestParam @NotBlank(message = "审核人ID不能为空") String auditorId) {
        
        logger.info("审核退款申请请求，退款订单ID: {}, 审核结果: {}, 审核人: {}", 
                   refundOrderId, approved ? "通过" : "拒绝", auditorId);

        try {
            boolean success = refundService.auditRefund(refundOrderId, approved, auditReason, auditorId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("message", success ? (approved ? "审核通过" : "审核拒绝") : "审核失败");

            logger.info("退款申请审核完成，退款订单ID: {}, 结果: {}", refundOrderId, success ? "成功" : "失败");
            return ResponseEntity.ok(result);

        } catch (IllegalStateException e) {
            logger.warn("审核退款申请状态错误，退款订单ID: {}, 错误: {}", refundOrderId, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("状态错误", e.getMessage()));
        } catch (Exception e) {
            logger.error("审核退款申请异常，退款订单ID: {}", refundOrderId, e);
            return ResponseEntity.internalServerError().body(createErrorResponse("系统异常", "审核退款申请失败"));
        }
    }

    /**
     * 取消退款申请
     * 用户主动取消退款申请或系统自动取消
     * 
     * @param refundOrderId 退款订单ID
     * @param reason 取消原因
     * @return 取消结果
     */
    @PostMapping("/orders/{refundOrderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelRefund(
            @PathVariable @NotBlank(message = "退款订单ID不能为空") String refundOrderId,
            @RequestParam(required = false) String reason) {
        
        logger.info("取消退款申请请求，退款订单ID: {}, 取消原因: {}", refundOrderId, reason);

        try {
            boolean success = refundService.cancelRefund(refundOrderId, reason);

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("message", success ? "退款申请取消成功" : "退款申请取消失败");

            if (success) {
                logger.info("退款申请取消成功，退款订单ID: {}", refundOrderId);
                return ResponseEntity.ok(result);
            } else {
                logger.warn("退款申请取消失败，退款订单ID: {}", refundOrderId);
                return ResponseEntity.badRequest().body(result);
            }

        } catch (IllegalStateException e) {
            logger.warn("取消退款申请状态错误，退款订单ID: {}, 错误: {}", refundOrderId, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("状态错误", e.getMessage()));
        } catch (Exception e) {
            logger.error("取消退款申请异常，退款订单ID: {}", refundOrderId, e);
            return ResponseEntity.internalServerError().body(createErrorResponse("系统异常", "取消退款申请失败"));
        }
    }

    /**
     * 查询退款状态
     * 主动查询第三方支付平台的退款状态，用于状态同步
     * 
     * @param refundOrderId 退款订单ID
     * @return 最新的退款状态
     */
    @GetMapping("/orders/{refundOrderId}/status")
    public ResponseEntity<Map<String, Object>> queryRefundStatus(
            @PathVariable @NotBlank(message = "退款订单ID不能为空") String refundOrderId) {
        
        logger.debug("查询退款状态，退款订单ID: {}", refundOrderId);

        try {
            RefundStatus status = refundService.queryRefundStatus(refundOrderId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", Map.of("refundOrderId", refundOrderId, "status", status));

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.warn("查询退款状态参数错误，退款订单ID: {}, 错误: {}", refundOrderId, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("参数错误", e.getMessage()));
        } catch (Exception e) {
            logger.error("查询退款状态异常，退款订单ID: {}", refundOrderId, e);
            return ResponseEntity.internalServerError().body(createErrorResponse("系统异常", "查询退款状态失败"));
        }
    }

    /**
     * 重试失败的退款订单
     * 对于退款失败但可以重试的订单，重新发起退款
     * 
     * @param refundOrderId 退款订单ID
     * @return 重试结果
     */
    @PostMapping("/orders/{refundOrderId}/retry")
    public ResponseEntity<Map<String, Object>> retryRefund(
            @PathVariable @NotBlank(message = "退款订单ID不能为空") String refundOrderId) {
        
        logger.info("重试退款订单请求，退款订单ID: {}", refundOrderId);

        try {
            boolean success = refundService.retryRefund(refundOrderId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("message", success ? "退款重试成功" : "退款重试失败");

            if (success) {
                logger.info("退款重试成功，退款订单ID: {}", refundOrderId);
                return ResponseEntity.ok(result);
            } else {
                logger.warn("退款重试失败，退款订单ID: {}", refundOrderId);
                return ResponseEntity.badRequest().body(result);
            }

        } catch (IllegalStateException e) {
            logger.warn("重试退款订单状态错误，退款订单ID: {}, 错误: {}", refundOrderId, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("状态错误", e.getMessage()));
        } catch (Exception e) {
            logger.error("重试退款订单异常，退款订单ID: {}", refundOrderId, e);
            return ResponseEntity.internalServerError().body(createErrorResponse("系统异常", "重试退款订单失败"));
        }
    }

    /**
     * 获取退款统计数据
     * 统计指定时间范围内的退款数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param userId 用户ID（可选）
     * @return 统计结果
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getRefundStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String userId) {
        
        logger.debug("获取退款统计数据，开始时间: {}, 结束时间: {}, 用户ID: {}", startTime, endTime, userId);

        try {
            RefundService.RefundStatistics statistics = refundService.getRefundStatistics(startTime, endTime, userId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", statistics);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.warn("获取退款统计数据参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("参数错误", e.getMessage()));
        } catch (Exception e) {
            logger.error("获取退款统计数据异常", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("系统异常", "获取退款统计数据失败"));
        }
    }

    /**
     * 批量处理待审核的退款申请
     * 定时任务调用，自动处理符合条件的退款申请
     * 
     * @return 处理结果
     */
    @PostMapping("/batch-process")
    public ResponseEntity<Map<String, Object>> batchProcessPendingRefunds() {
        
        logger.info("批量处理待审核退款申请请求");

        try {
            int processedCount = refundService.batchProcessPendingRefunds();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "批量处理完成");
            result.put("data", Map.of("processedCount", processedCount));

            logger.info("批量处理待审核退款申请完成，处理数量: {}", processedCount);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("批量处理待审核退款申请异常", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("系统异常", "批量处理失败"));
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String error, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", error);
        result.put("message", message);
        return result;
    }
}