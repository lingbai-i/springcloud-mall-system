package com.mall.merchant.controller;

import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import com.mall.merchant.domain.entity.MerchantOrder;
import com.mall.merchant.service.MerchantOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 商家订单管理控制器
 * 提供订单查询、处理、统计等功能的REST API接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@RestController
@RequestMapping("/api/merchant/order")
@RequiredArgsConstructor
@Validated
@Tag(name = "商家订单管理", description = "订单查询、处理、统计等功能")
public class MerchantOrderController {
    
    private static final Logger log = LoggerFactory.getLogger(MerchantOrderController.class);
    
    private final MerchantOrderService orderService;
    
    /**
     * 获取订单详情
     * 根据订单ID获取详细信息
     * 
     * @param orderId 订单ID
     * @param merchantId 商家ID
     * @return 订单详情
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "获取订单详情", description = "根据订单ID获取详细信息")
    public R<MerchantOrder> getOrderById(
            @Parameter(description = "订单ID") @PathVariable @NotNull Long orderId,
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId) {
        log.debug("获取订单详情请求，订单ID：{}，商家ID：{}", orderId, merchantId);
        return orderService.getOrderById(orderId, merchantId);
    }
    
    /**
     * 根据订单号获取订单
     * 根据订单号获取订单信息
     * 
     * @param orderNumber 订单号
     * @param merchantId 商家ID
     * @return 订单信息
     */
    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "根据订单号获取订单", description = "根据订单号获取订单信息")
    public R<MerchantOrder> getOrderByNumber(
            @Parameter(description = "订单号") @PathVariable @NotBlank String orderNumber,
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId) {
        log.debug("根据订单号获取订单请求，订单号：{}，商家ID：{}", orderNumber, merchantId);
        return orderService.getOrderByOrderNo(orderNumber, merchantId);
    }
    
    /**
     * 分页查询订单列表
     * 根据条件分页查询商家的订单列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @param orderNumber 订单号（可选）
     * @param status 订单状态（可选）
     * @param userId 用户ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 订单分页列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询订单列表", description = "根据条件分页查询商家的订单列表")
    public R<PageResult<MerchantOrder>> getOrderList(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "订单号") @RequestParam(required = false) String orderNumber,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        log.debug("分页查询订单列表请求，商家ID：{}，页码：{}，大小：{}", merchantId, page, size);
        return orderService.getOrderList(merchantId, page, size, orderNumber, status, userId, startTime, endTime);
    }
    
    /**
     * 获取待发货订单
     * 获取需要发货的订单列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @return 待发货订单列表
     */
    @GetMapping("/pending-shipment")
    @Operation(summary = "获取待发货订单", description = "获取需要发货的订单列表")
    public R<PageResult<MerchantOrder>> getPendingShipmentOrders(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
        log.debug("获取待发货订单请求，商家ID：{}", merchantId);
        return orderService.getPendingShipmentOrders(merchantId, page, size);
    }
    
    /**
     * 获取待收货订单
     * 获取已发货但未收货的订单列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @return 待收货订单列表
     */
    @GetMapping("/pending-receipt")
    @Operation(summary = "获取待收货订单", description = "获取已发货但未收货的订单列表")
    public R<PageResult<MerchantOrder>> getPendingReceiptOrders(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
        log.debug("获取待收货订单请求，商家ID：{}", merchantId);
        return orderService.getPendingReceiptOrders(merchantId, page, size);
    }
    
    /**
     * 获取退款订单
     * 获取申请退款的订单列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @return 退款订单列表
     */
    @GetMapping("/refund")
    @Operation(summary = "获取退款订单", description = "获取申请退款的订单列表")
    public R<PageResult<MerchantOrder>> getRefundOrders(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
        log.debug("获取退款订单请求，商家ID：{}", merchantId);
        return orderService.getRefundOrders(merchantId, page, size);
    }
    
    /**
     * 获取最近订单
     * 获取最近的订单列表
     * 
     * @param merchantId 商家ID
     * @param limit 数量限制
     * @return 最近订单列表
     */
    @GetMapping("/recent")
    @Operation(summary = "获取最近订单", description = "获取最近的订单列表")
    public R<List<MerchantOrder>> getRecentOrders(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") Integer limit) {
        log.debug("获取最近订单请求，商家ID：{}，限制：{}", merchantId, limit);
        return orderService.getRecentOrders(merchantId, limit);
    }
    
    /**
     * 发货
     * 处理订单发货，更新物流信息
     * 
     * @param orderId 订单ID
     * @param merchantId 商家ID
     * @param logisticsCompany 物流公司
     * @param trackingNumber 物流单号
     * @param merchantRemark 商家备注
     * @return 发货结果
     */
    @PostMapping("/{orderId}/ship")
    @Operation(summary = "订单发货", description = "处理订单发货，更新物流信息")
    public R<Void> shipOrder(
            @Parameter(description = "订单ID") @PathVariable @NotNull Long orderId,
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "物流公司") @RequestParam @NotBlank String logisticsCompany,
            @Parameter(description = "物流单号") @RequestParam @NotBlank String trackingNumber,
            @Parameter(description = "商家备注") @RequestParam(required = false) String merchantRemark) {
        log.info("订单发货请求，订单ID：{}，商家ID：{}，物流公司：{}，单号：{}", orderId, merchantId, logisticsCompany, trackingNumber);
        return orderService.shipOrder(merchantId, orderId, logisticsCompany, trackingNumber, merchantRemark);
    }
    
    /**
     * 批量发货
     * 批量处理多个订单的发货
     * 
     * @param merchantId 商家ID
     * @param shipmentInfo 发货信息列表
     * @return 批量发货结果
     */
    @PostMapping("/batch-ship")
    @Operation(summary = "批量发货", description = "批量处理多个订单的发货")
    public R<Void> batchShipOrders(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @RequestBody List<Map<String, Object>> shipmentInfo) {
        log.info("批量发货请求，商家ID：{}，订单数量：{}", merchantId, shipmentInfo.size());
        return orderService.batchShipOrders(merchantId, shipmentInfo);
    }
    
    /**
     * 确认收货
     * 商家代替用户确认收货（特殊情况）
     * 
     * @param orderId 订单ID
     * @param merchantId 商家ID
     * @return 确认收货结果
     */
    @PostMapping("/{orderId}/confirm-receipt")
    @Operation(summary = "确认收货", description = "商家代替用户确认收货")
    public R<Void> confirmOrderReceipt(
            @Parameter(description = "订单ID") @PathVariable @NotNull Long orderId,
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId) {
        log.info("确认收货请求，订单ID：{}，商家ID：{}", orderId, merchantId);
        return orderService.confirmReceipt(merchantId, orderId);
    }
    
    /**
     * 取消订单
     * 取消未发货的订单
     * 
     * @param orderId 订单ID
     * @param merchantId 商家ID
     * @param reason 取消原因
     * @return 取消订单结果
     */
    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "取消订单", description = "取消未发货的订单")
    public R<Void> cancelOrder(
            @Parameter(description = "订单ID") @PathVariable @NotNull Long orderId,
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "取消原因") @RequestParam(required = false) String reason) {
        log.info("取消订单请求，订单ID：{}，商家ID：{}，原因：{}", orderId, merchantId, reason);
        return orderService.cancelOrder(merchantId, orderId, reason);
    }
    
    /**
     * 处理退款
     * 处理用户的退款申请
     * 
     * @param orderId 订单ID
     * @param merchantId 商家ID
     * @param approved 是否同意退款
     * @param reason 处理原因
     * @return 处理退款结果
     */
    @PostMapping("/{orderId}/refund")
    @Operation(summary = "处理退款", description = "处理用户的退款申请")
    public R<Void> handleRefund(
            @Parameter(description = "订单ID") @PathVariable @NotNull Long orderId,
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "是否同意退款") @RequestParam @NotNull Boolean approved,
            @Parameter(description = "处理原因") @RequestParam(required = false) String reason) {
        log.info("处理退款请求，订单ID：{}，商家ID：{}，是否同意：{}，原因：{}", orderId, merchantId, approved, reason);
        return orderService.handleRefundRequest(merchantId, orderId, approved, reason);
    }
    
    /**
     * 更新订单备注
     * 商家更新订单的备注信息
     * 
     * @param orderId 订单ID
     * @param merchantId 商家ID
     * @param remark 备注内容
     * @return 更新订单备注结果
     */
    @PutMapping("/{orderId}/remark")
    @Operation(summary = "更新订单备注", description = "商家更新订单的备注信息")
    public R<Void> updateOrderRemark(
            @Parameter(description = "订单ID") @PathVariable @NotNull Long orderId,
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "备注内容") @RequestParam @NotNull String remark) {
        log.info("更新订单备注请求，订单ID：{}，商家ID：{}，备注：{}", orderId, merchantId, remark);
        return orderService.updateOrderRemark(merchantId, orderId, remark);
    }
    
    /**
     * 更新物流信息
     * 更新订单的物流跟踪信息
     * 
     * @param orderId 订单ID
     * @param merchantId 商家ID
     * @param logisticsInfo 物流信息
     * @return 更新结果
     */
    @PutMapping("/{orderId}/logistics")
    @Operation(summary = "更新物流信息", description = "更新订单的物流跟踪信息")
    public R<Void> updateLogisticsInfo(
            @Parameter(description = "订单ID") @PathVariable @NotNull Long orderId,
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @RequestBody Map<String, Object> logisticsInfo) {
        log.info("更新物流信息请求，订单ID：{}，商家ID：{}", orderId, merchantId);
        return orderService.updateLogisticsInfo(merchantId, orderId, logisticsInfo);
    }
    
    /**
     * 获取订单统计数据
     * 获取商家订单的统计信息
     * 
     * @param merchantId 商家ID
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取订单统计数据", description = "获取商家订单的统计信息")
    public R<Map<String, Object>> getOrderStatistics(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        log.debug("获取订单统计数据请求，商家ID：{}", merchantId);
        return orderService.getOrderStatistics(merchantId, startTime, endTime);
    }
    
    /**
     * 获取订单状态统计
     * 获取各个状态的订单数量统计
     * 
     * @param merchantId 商家ID
     * @return 状态统计数据
     */
    @GetMapping("/status-statistics")
    @Operation(summary = "获取订单状态统计", description = "获取各个状态的订单数量统计")
    public R<Map<String, Object>> getOrderStatusStatistics(@Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId) {
        log.debug("获取订单状态统计请求，商家ID：{}", merchantId);
        return orderService.getOrderStatusStatistics(merchantId);
    }
    
    /**
     * 获取热销商品统计
     * 获取销量排名靠前的商品统计
     * 
     * @param merchantId 商家ID
     * @param limit 数量限制
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 热销商品统计
     */
    @GetMapping("/hot-products")
    @Operation(summary = "获取热销商品统计", description = "获取销量排名靠前的商品统计")
    public R<List<Map<String, Object>>> getHotProductsStatistics(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") Integer limit,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        log.debug("获取热销商品统计请求，商家ID：{}，限制：{}", merchantId, limit);
        return orderService.getHotProductsStatistics(merchantId, limit, startTime, endTime);
    }
    
    /**
     * 获取客户购买统计
     * 获取客户购买行为统计
     * 
     * @param merchantId 商家ID
     * @param limit 数量限制
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 客户购买统计
     */
    @GetMapping("/customer-statistics")
    @Operation(summary = "获取客户购买统计", description = "获取客户购买行为统计")
    public R<List<Map<String, Object>>> getCustomerPurchaseStatistics(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") Integer limit,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        log.debug("获取客户购买统计请求，商家ID：{}，限制：{}", merchantId, limit);
        return orderService.getCustomerPurchaseStatistics(merchantId, limit, startTime, endTime);
    }
    
    /**
     * 获取每日销售统计
     * 获取指定时间范围内的每日销售数据
     * 
     * @param merchantId 商家ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 每日销售统计
     */
    @GetMapping("/daily-sales")
    @Operation(summary = "获取每日销售统计", description = "获取指定时间范围内的每日销售数据")
    public R<List<Map<String, Object>>> getDailySalesStatistics(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") java.time.LocalDate startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") java.time.LocalDate endTime) {
        log.debug("获取每日销售统计请求，商家ID：{}，开始：{}，结束：{}", merchantId, startTime, endTime);
        return orderService.getDailySalesStatistics(merchantId, startTime, endTime);
    }
    
    /**
     * 获取每月销售统计
     * 获取指定时间范围内的每月销售数据
     * 
     * @param merchantId 商家ID
     * @param startMonth 开始月份
     * @param endMonth 结束月份
     * @return 每月销售统计
     */
    @GetMapping("/monthly-sales")
    @Operation(summary = "获取每月销售统计", description = "获取指定时间范围内的每月销售数据")
    public R<List<Map<String, Object>>> getMonthlySalesStatistics(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "开始月份") @RequestParam @DateTimeFormat(pattern = "yyyy-MM") String startMonth,
            @Parameter(description = "结束月份") @RequestParam @DateTimeFormat(pattern = "yyyy-MM") String endMonth) {
        log.debug("获取每月销售统计请求，商家ID：{}，开始：{}，结束：{}", merchantId, startMonth, endMonth);
        return orderService.getMonthlySalesStatistics(merchantId, startMonth, endMonth);
    }
    
    /**
     * 导出订单数据
     * 导出商家的订单数据到Excel
     * 
     * @param merchantId 商家ID
     * @param orderNumber 订单号（可选）
     * @param status 订单状态（可选）
     * @param userId 用户ID（可选）
     * @param productId 商品ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return Excel文件数据
     */
    @GetMapping("/export")
    @Operation(summary = "导出订单数据", description = "导出商家的订单数据到Excel")
    public R<byte[]> exportOrderData(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "订单号") @RequestParam(required = false) String orderNumber,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "支付方式") @RequestParam(required = false) Integer paymentMethod,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        log.debug("导出订单数据请求，商家ID：{}，订单号：{}，状态：{}，支付方式：{}，开始时间：{}，结束时间：{}", 
                merchantId, orderNumber, status, paymentMethod, startTime, endTime);
        return orderService.exportOrderData(merchantId, orderNumber, status, paymentMethod, startTime, endTime);
    }
    
    /**
     * 检查订单归属
     * 验证订单是否属于指定商家
     * 
     * @param orderId 订单ID
     * @param merchantId 商家ID
     * @return 检查结果
     */
    @GetMapping("/{orderId}/check-ownership")
    @Operation(summary = "检查订单归属", description = "验证订单是否属于指定商家")
    public R<Boolean> checkOrderOwnership(
            @Parameter(description = "订单ID") @PathVariable @NotNull Long orderId,
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId) {
        log.debug("检查订单归属请求，订单ID：{}，商家ID：{}", orderId, merchantId);
        return orderService.checkOrderOwnership(orderId, merchantId);
    }
}