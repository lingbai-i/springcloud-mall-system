package com.mall.payment.controller;

import com.mall.payment.annotation.RequirePermission;
import com.mall.payment.dto.request.PaymentCreateRequest;
import com.mall.payment.dto.request.PaymentQueryRequest;
import com.mall.payment.dto.response.PaymentOrderResponse;
import com.mall.payment.dto.response.PageResponse;
import com.mall.payment.enums.PaymentStatus;
import com.mall.payment.service.PaymentService;
import com.mall.payment.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付控制器
 * 提供支付相关的RESTful API接口，包括创建支付订单、查询订单、发起支付等功能
 * 
 * <p>主要功能包括：</p>
 * <ul>
 *   <li>支付订单管理：创建、查询、取消支付订单</li>
 *   <li>支付流程控制：发起支付、重试支付、查询支付状态</li>
 *   <li>数据查询：分页查询、用户订单查询、统计数据查询</li>
 *   <li>权限控制：基于角色和资源所有者的访问控制</li>
 * </ul>
 * 
 * <p>支持的支付方式：</p>
 * <ul>
 *   <li>支付宝支付</li>
 *   <li>微信支付</li>
 *   <li>银行卡支付</li>
 * </ul>
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * <p>修改日志：</p>
 * <ul>
 *   <li>V1.0 2024-12-01：初始版本，实现基础支付功能</li>
 *   <li>V1.1 2025-01-15：增加支付重试和状态查询功能</li>
 *   <li>V1.2 2025-11-01：完善Javadoc注释，增强权限控制</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/payment")
@Validated
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    /**
     * 创建支付订单
     * 根据业务订单信息创建支付订单，返回支付订单详情
     * 
     * <p>操作步骤：</p>
     * <ol>
     *   <li>验证请求参数的有效性</li>
     *   <li>获取客户端IP和User-Agent信息</li>
     *   <li>调用支付服务创建支付订单</li>
     *   <li>返回支付订单详情</li>
     * </ol>
     * 
     * <p>权限要求：</p>
     * <ul>
     *   <li>用户角色：USER</li>
     *   <li>资源所有者：允许用户访问自己的订单</li>
     * </ul>
     * 
     * @param request 创建支付订单的请求参数，包含业务订单ID、用户ID、支付金额等信息
     * @param httpRequest HTTP请求对象，用于获取客户端IP和User-Agent信息
     * @return ResponseEntity包装的创建结果，成功时返回支付订单详情
     * @throws IllegalArgumentException 当请求参数无效时抛出
     * @throws RuntimeException 当系统异常时抛出
     * @since 2025-11-01
     */
    @PostMapping("/orders")
    @RequirePermission(value = {"USER"}, allowOwner = true, ownerField = "userId")
    public ResponseEntity<Map<String, Object>> createPaymentOrder(
            @Valid @RequestBody PaymentCreateRequest request,
            HttpServletRequest httpRequest) {
        
        logger.info("创建支付订单请求，业务订单ID: {}, 用户ID: {}, 支付金额: {}", 
                   request.getBusinessOrderId(), request.getUserId(), request.getAmount());

        try {
            // 设置客户端信息
            request.setClientIp(getClientIp(httpRequest));
            request.setUserAgent(httpRequest.getHeader("User-Agent"));

            // 创建支付订单
            PaymentOrderResponse response = paymentService.createPaymentOrder(request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "支付订单创建成功");
            result.put("data", response);

            logger.info("支付订单创建成功，支付订单ID: {}", response.getPaymentOrderId());
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.warn("创建支付订单参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("参数错误", e.getMessage()));
        } catch (Exception e) {
            logger.error("创建支付订单异常", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("系统异常", "创建支付订单失败"));
        }
    }

    /**
     * 根据支付订单ID查询订单详情
     * 
     * @param paymentOrderId 支付订单ID
     * @return 支付订单详情
     */
    @GetMapping("/orders/{paymentOrderId}")
    @RequirePermission(value = {"USER", "ADMIN"})
    public ResponseEntity<Map<String, Object>> getPaymentOrder(
            @PathVariable @NotBlank(message = "支付订单ID不能为空") String paymentOrderId) {
        
        logger.debug("查询支付订单详情，支付订单ID: {}", paymentOrderId);

        try {
            PaymentOrderResponse response = paymentService.getPaymentOrder(paymentOrderId);
            
            if (response == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("查询支付订单异常，支付订单ID: {}", paymentOrderId, e);
            return ResponseEntity.internalServerError().body(createErrorResponse("系统异常", "查询支付订单失败"));
        }
    }

    /**
     * 根据业务订单ID查询支付订单
     * 
     * @param businessOrderId 业务订单ID
     * @return 支付订单详情
     */
    @GetMapping("/orders/business/{businessOrderId}")
    public ResponseEntity<Map<String, Object>> getPaymentOrderByBusinessId(
            @PathVariable @NotBlank(message = "业务订单ID不能为空") String businessOrderId) {
        
        logger.debug("根据业务订单ID查询支付订单，业务订单ID: {}", businessOrderId);

        try {
            PaymentOrderResponse response = paymentService.getPaymentOrderByOrderId(businessOrderId);
            
            if (response == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("根据业务订单ID查询支付订单异常，业务订单ID: {}", businessOrderId, e);
            return ResponseEntity.internalServerError().body(createErrorResponse("系统异常", "查询支付订单失败"));
        }
    }

    /**
     * 分页查询支付订单
     * 支持多种查询条件的组合查询
     * 
     * @param userId 用户ID（可选）
     * @param status 支付状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页查询结果
     */
    @GetMapping("/orders")
    @RequirePermission(value = {"ADMIN"})
    public ResponseEntity<Map<String, Object>> queryPaymentOrders(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") int size) {
        
        logger.debug("分页查询支付订单，用户ID: {}, 状态: {}, 页码: {}, 大小: {}", userId, status, page, size);

        try {
            // 构建查询请求对象
            PaymentQueryRequest queryRequest = new PaymentQueryRequest();
            queryRequest.setUserId(userId);
            queryRequest.setStatus(status);
            queryRequest.setCreatedAtStart(startTime);
            queryRequest.setCreatedAtEnd(endTime);
            queryRequest.setPage(page);
            queryRequest.setSize(size);
            
            PageResponse<PaymentOrderResponse> response = paymentService.queryPaymentOrders(queryRequest);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("分页查询支付订单异常", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("系统异常", "查询支付订单失败"));
        }
    }

    /**
     * 查询用户的支付订单列表
     * 
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页查询结果
     */
    @GetMapping("/orders/user/{userId}")
    @RequirePermission(value = {"USER", "ADMIN"}, allowOwner = true, ownerField = "userId")
    public ResponseEntity<Map<String, Object>> getUserPaymentOrders(
            @PathVariable @NotBlank(message = "用户ID不能为空") String userId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") int size) {
        
        logger.debug("查询用户支付订单列表，用户ID: {}, 页码: {}, 大小: {}", userId, page, size);

        try {
            PageResponse<PaymentOrderResponse> response = paymentService.getUserPaymentOrders(userId, page, size);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("查询用户支付订单列表异常，用户ID: {}", userId, e);
            return ResponseEntity.internalServerError().body(createErrorResponse("系统异常", "查询用户支付订单失败"));
        }
    }

    /**
     * 发起支付
     * 调用第三方支付平台接口，发起支付流程
     * 
     * <p>操作流程：</p>
     * <ol>
     *   <li>验证支付订单ID的有效性</li>
     *   <li>获取客户端IP和User-Agent信息</li>
     *   <li>调用支付服务发起支付</li>
     *   <li>返回支付信息（跳转URL、二维码等）</li>
     * </ol>
     * 
     * <p>支付信息包含：</p>
     * <ul>
     *   <li>支付宝：跳转URL或表单HTML</li>
     *   <li>微信：二维码URL或小程序支付参数</li>
     *   <li>银行卡：跳转URL或支付表单</li>
     * </ul>
     * 
     * @param paymentOrderId 支付订单ID，不能为空
     * @param httpRequest HTTP请求对象，用于获取客户端信息
     * @return ResponseEntity包装的支付发起结果，包含支付跳转URL或二维码等信息
     * @throws IllegalArgumentException 当支付订单ID无效时抛出
     * @throws IllegalStateException 当订单状态不允许支付时抛出
     * @throws RuntimeException 当系统异常时抛出
     * @since 2025-11-01
     */
    @PostMapping("/orders/{paymentOrderId}/pay")
    @RequirePermission(value = {"USER"})
    public ResponseEntity<Map<String, Object>> initiatePayment(
            @PathVariable @NotBlank(message = "支付订单ID不能为空") String paymentOrderId,
            HttpServletRequest httpRequest) {
        
        logger.info("发起支付请求，支付订单ID: {}", paymentOrderId);

        try {
            // 获取客户端信息
            String clientIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");

            // 发起支付
            Map<String, Object> paymentInfo = paymentService.initiatePayment(paymentOrderId, clientIp, userAgent);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "支付发起成功");
            result.put("data", paymentInfo);

            logger.info("支付发起成功，支付订单ID: {}", paymentOrderId);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.warn("发起支付参数错误，支付订单ID: {}, 错误: {}", paymentOrderId, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("参数错误", e.getMessage()));
        } catch (IllegalStateException e) {
            logger.warn("发起支付状态错误，支付订单ID: {}, 错误: {}", paymentOrderId, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("状态错误", e.getMessage()));
        } catch (Exception e) {
            logger.error("发起支付异常，支付订单ID: {}", paymentOrderId, e);
            return ResponseEntity.internalServerError().body(createErrorResponse("系统异常", "发起支付失败"));
        }
    }

    /**
     * 取消支付订单
     * 取消未支付的订单
     * 
     * @param paymentOrderId 支付订单ID
     * @param reason 取消原因
     * @return 取消结果
     */
    @PostMapping("/orders/{paymentOrderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelPaymentOrder(
            @PathVariable @NotBlank(message = "支付订单ID不能为空") String paymentOrderId,
            @RequestParam(required = false) String reason) {
        
        logger.info("取消支付订单请求，支付订单ID: {}, 取消原因: {}", paymentOrderId, reason);

        try {
            boolean success = paymentService.cancelPaymentOrder(paymentOrderId, reason);

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("message", success ? "订单取消成功" : "订单取消失败");

            if (success) {
                logger.info("支付订单取消成功，支付订单ID: {}", paymentOrderId);
                return ResponseEntity.ok(result);
            } else {
                logger.warn("支付订单取消失败，支付订单ID: {}", paymentOrderId);
                return ResponseEntity.badRequest().body(result);
            }

        } catch (IllegalArgumentException e) {
            logger.warn("取消支付订单参数错误，支付订单ID: {}, 错误: {}", paymentOrderId, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("参数错误", e.getMessage()));
        } catch (IllegalStateException e) {
            logger.warn("取消支付订单状态错误，支付订单ID: {}, 错误: {}", paymentOrderId, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("状态错误", e.getMessage()));
        } catch (Exception e) {
            logger.error("取消支付订单异常，支付订单ID: {}", paymentOrderId, e);
            return ResponseEntity.internalServerError().body(createErrorResponse("系统异常", "取消支付订单失败"));
        }
    }

    /**
     * 查询支付状态
     * 主动查询第三方支付平台的支付状态，用于状态同步
     * 
     * @param paymentOrderId 支付订单ID
     * @return 最新的支付状态
     */
    @GetMapping("/orders/{paymentOrderId}/status")
    public ResponseEntity<Map<String, Object>> queryPaymentStatus(
            @PathVariable @NotBlank(message = "支付订单ID不能为空") String paymentOrderId) {
        
        logger.debug("查询支付状态，支付订单ID: {}", paymentOrderId);

        try {
            PaymentStatus status = paymentService.queryPaymentStatus(paymentOrderId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", Map.of("paymentOrderId", paymentOrderId, "status", status));

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.warn("查询支付状态参数错误，支付订单ID: {}, 错误: {}", paymentOrderId, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("参数错误", e.getMessage()));
        } catch (Exception e) {
            logger.error("查询支付状态异常，支付订单ID: {}", paymentOrderId, e);
            return ResponseEntity.internalServerError().body(createErrorResponse("系统异常", "查询支付状态失败"));
        }
    }

    /**
     * 重试失败的支付订单
     * 对于支付失败但可以重试的订单，重新发起支付
     * 
     * @param paymentOrderId 支付订单ID
     * @param httpRequest HTTP请求对象
     * @return 重试结果
     */
    @PostMapping("/orders/{paymentOrderId}/retry")
    public ResponseEntity<Map<String, Object>> retryPayment(
            @PathVariable @NotBlank(message = "支付订单ID不能为空") String paymentOrderId,
            HttpServletRequest httpRequest) {
        
        logger.info("重试支付订单请求，支付订单ID: {}", paymentOrderId);

        try {
            // 获取客户端信息
            String clientIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");

            // 重试支付
            Map<String, Object> paymentInfo = paymentService.retryPayment(paymentOrderId, clientIp, userAgent);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "支付重试成功");
            result.put("data", paymentInfo);

            logger.info("支付重试成功，支付订单ID: {}", paymentOrderId);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.warn("重试支付参数错误，支付订单ID: {}, 错误: {}", paymentOrderId, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("参数错误", e.getMessage()));
        } catch (IllegalStateException e) {
            logger.warn("重试支付状态错误，支付订单ID: {}, 错误: {}", paymentOrderId, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("状态错误", e.getMessage()));
        } catch (Exception e) {
            logger.error("重试支付异常，支付订单ID: {}", paymentOrderId, e);
            return ResponseEntity.internalServerError().body(createErrorResponse("系统异常", "重试支付失败"));
        }
    }

    /**
     * 获取支付统计数据
     * 统计指定时间范围内的支付数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param userId 用户ID（可选）
     * @return 统计结果
     */
    @GetMapping("/statistics")
    @RequirePermission(value = {"ADMIN"})
    public ResponseEntity<Map<String, Object>> getPaymentStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String userId) {
        
        logger.debug("获取支付统计数据，开始时间: {}, 结束时间: {}, 用户ID: {}", startTime, endTime, userId);

        try {
            PaymentService.PaymentStatistics statistics = paymentService.getPaymentStatistics(startTime, endTime, userId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", statistics);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.warn("获取支付统计数据参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("参数错误", e.getMessage()));
        } catch (Exception e) {
            logger.error("获取支付统计数据异常", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("系统异常", "获取支付统计数据失败"));
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取客户端IP地址
     * 考虑代理服务器的情况，优先获取真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 如果是多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }

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