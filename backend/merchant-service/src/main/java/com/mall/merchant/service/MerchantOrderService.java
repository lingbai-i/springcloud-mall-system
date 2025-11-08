package com.mall.merchant.service;

import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import com.mall.merchant.domain.entity.MerchantOrder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 商家订单服务接口
 * 提供商家订单相关的业务逻辑处理
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
public interface MerchantOrderService {
    
    /**
     * 根据ID获取订单详情
     * 
     * @param orderId 订单ID
     * @param merchantId 商家ID
     * @return 订单详情
     */
    R<MerchantOrder> getOrderById(Long orderId, Long merchantId);
    
    /**
     * 根据订单号获取订单详情
     * 
     * @param orderNo 订单号
     * @param merchantId 商家ID
     * @return 订单详情
     */
    R<MerchantOrder> getOrderByOrderNo(String orderNo, Long merchantId);
    
    /**
     * 分页查询订单列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @param orderNo 订单号（可选）
     * @param status 订单状态（可选）
     * @param userId 用户ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 订单分页列表
     */
    R<PageResult<MerchantOrder>> getOrderList(Long merchantId, Integer page, Integer size, 
                                             String orderNo, Integer status, Long userId,
                                             LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取待发货订单列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @return 待发货订单分页列表
     */
    R<PageResult<MerchantOrder>> getPendingShipmentOrders(Long merchantId, Integer page, Integer size);
    
    /**
     * 获取待收货订单列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @return 待收货订单分页列表
     */
    R<PageResult<MerchantOrder>> getPendingReceiptOrders(Long merchantId, Integer page, Integer size);
    
    /**
     * 获取退款订单列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @return 退款订单分页列表
     */
    R<PageResult<MerchantOrder>> getRefundOrders(Long merchantId, Integer page, Integer size);
    
    /**
     * 获取最近订单列表
     * 
     * @param merchantId 商家ID
     * @param limit 数量限制
     * @return 最近订单列表
     */
    R<List<MerchantOrder>> getRecentOrders(Long merchantId, Integer limit);
    
    /**
     * 发货
     * 
     * @param merchantId 商家ID
     * @param orderId 订单ID
     * @param logisticsCompany 物流公司
     * @param logisticsNo 物流单号
     * @param merchantRemark 商家备注
     * @return 发货结果
     */
    R<Void> shipOrder(Long merchantId, Long orderId, String logisticsCompany, String logisticsNo, String merchantRemark);
    
    /**
     * 批量发货
     * 
     * @param merchantId 商家ID
     * @param shipmentInfos 发货信息列表，包含订单ID、物流公司、物流单号等
     * @return 发货结果
     */
    R<Void> batchShipOrders(Long merchantId, List<Map<String, Object>> shipmentInfos);
    
    /**
     * 确认收货（商家代确认）
     * 
     * @param merchantId 商家ID
     * @param orderId 订单ID
     * @return 确认结果
     */
    R<Void> confirmReceipt(Long merchantId, Long orderId);
    
    /**
     * 取消订单
     * 
     * @param merchantId 商家ID
     * @param orderId 订单ID
     * @param cancelReason 取消原因
     * @return 取消结果
     */
    R<Void> cancelOrder(Long merchantId, Long orderId, String cancelReason);
    
    /**
     * 处理退款申请
     * 
     * @param merchantId 商家ID
     * @param orderId 订单ID
     * @param agree 是否同意退款
     * @param refundReason 退款原因
     * @return 处理结果
     */
    R<Void> handleRefundRequest(Long merchantId, Long orderId, Boolean agree, String refundReason);
    
    /**
     * 更新订单备注
     * 
     * @param merchantId 商家ID
     * @param orderId 订单ID
     * @param merchantRemark 商家备注
     * @return 更新结果
     */
    R<Void> updateOrderRemark(Long merchantId, Long orderId, String merchantRemark);
    
    /**
     * 获取订单统计数据
     * 
     * @param merchantId 商家ID
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 统计数据
     */
    R<Map<String, Object>> getOrderStatistics(Long merchantId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取订单状态统计
     * 
     * @param merchantId 商家ID
     * @return 订单状态统计
     */
    R<Map<String, Object>> getOrderStatusStatistics(Long merchantId);
    
    /**
     * 获取热销商品统计
     * 
     * @param merchantId 商家ID
     * @param limit 限制数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 热销商品统计
     */
    R<List<Map<String, Object>>> getHotProductsStatistics(Long merchantId, Integer limit,
                                                          LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取客户购买统计
     * 
     * @param merchantId 商家ID
     * @param limit 限制数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 客户购买统计
     */
    R<List<Map<String, Object>>> getCustomerPurchaseStatistics(Long merchantId, Integer limit,
                                                               LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取每日销售统计
     * 
     * @param merchantId 商家ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 每日销售统计
     */
    R<List<Map<String, Object>>> getDailySalesStatistics(Long merchantId, java.time.LocalDate startDate, java.time.LocalDate endDate);
    
    /**
     * 获取每月销售统计
     * 
     * @param merchantId 商家ID
     * @param startMonth 开始月份
     * @param endMonth 结束月份
     * @return 每月销售统计
     */
    R<List<Map<String, Object>>> getMonthlySalesStatistics(Long merchantId, String startMonth, String endMonth);
    
    /**
     * 导出订单数据
     * 
     * @param merchantId 商家ID
     * @param orderNo 订单号（可选）
     * @param status 订单状态（可选）
     * @param paymentMethod 支付方式（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 导出数据
     */
    R<byte[]> exportOrderData(Long merchantId, String orderNo, Integer status, Integer paymentMethod,
                             LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取物流信息
     * 
     * @param merchantId 商家ID
     * @param orderId 订单ID
     * @return 物流信息
     */
    R<Map<String, Object>> getLogisticsInfo(Long merchantId, Long orderId);
    
    /**
     * 更新物流信息
     * 
     * @param merchantId 商家ID
     * @param orderId 订单ID
     * @param logisticsInfo 物流信息
     * @return 更新结果
     */
    R<Void> updateLogisticsInfo(Long merchantId, Long orderId, Map<String, Object> logisticsInfo);
    
    /**
     * 检查订单是否属于指定商家
     * 
     * @param orderId 订单ID
     * @param merchantId 商家ID
     * @return 是否属于该商家
     */
    R<Boolean> checkOrderOwnership(Long orderId, Long merchantId);
    
    /**
     * 获取订单数量统计（按状态分组）
     * 
     * @param merchantId 商家ID
     * @return 订单数量统计
     */
    R<Map<Integer, Long>> getOrderCountByStatus(Long merchantId);
    
    /**
     * 获取指定时间范围内的订单趋势
     * 
     * @param merchantId 商家ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 订单趋势数据
     */
    R<List<Map<String, Object>>> getOrderTrend(Long merchantId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取销售趋势
     * 
     * @param merchantId 商家ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 销售趋势数据
     */
    R<List<Map<String, Object>>> getSalesTrend(Long merchantId, LocalDateTime startTime, LocalDateTime endTime);
}