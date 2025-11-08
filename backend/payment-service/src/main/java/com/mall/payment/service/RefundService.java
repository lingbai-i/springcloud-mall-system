package com.mall.payment.service;

import com.mall.payment.dto.request.RefundCreateRequest;
import com.mall.payment.dto.response.RefundOrderResponse;
import com.mall.payment.dto.response.PageResponse;
import com.mall.payment.enums.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 退款服务接口
 * 
 * 定义退款相关的核心业务方法，提供完整的退款生命周期管理功能。
 * 该接口是退款系统的核心服务层，涵盖退款业务的各个环节：
 * - 退款订单的创建、查询和状态管理
 * - 退款申请的审核和处理流程
 * - 与第三方支付平台的退款交互
 * - 退款回调的处理和状态同步
 * - 退款统计和监控功能
 * - 异常处理和重试机制
 * 
 * 退款业务流程：
 * 1. 用户发起退款申请 -> 创建退款订单
 * 2. 系统或人工审核退款申请
 * 3. 审核通过后调用第三方退款接口
 * 4. 处理第三方退款回调结果
 * 5. 更新退款状态并通知用户
 * 
 * 支持的退款类型：
 * - 全额退款：退还订单全部金额
 * - 部分退款：退还订单部分金额
 * - 多次退款：同一订单可多次申请退款
 * 
 * 退款状态管理：
 * - PENDING：待审核
 * - APPROVED：审核通过
 * - PROCESSING：退款处理中
 * - SUCCESS：退款成功
 * - FAILED：退款失败
 * - CANCELLED：已取消
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * 修改日志：
 * V1.2 2025-11-01：完善Javadoc注释，添加详细的业务流程说明和状态管理
 * V1.1 2025-01-01：添加退款统计和监控功能，完善异常处理
 * V1.0 2024-12-01：初始版本，基础退款功能
 */
public interface RefundService {

    /**
     * 创建退款订单
     * 
     * 根据支付订单创建退款订单，并进行必要的业务验证。
     * 该方法会执行以下操作：
     * 1. 验证请求参数的有效性（支付订单ID、退款金额、退款原因等）
     * 2. 检查支付订单状态是否允许退款（必须是已支付状态）
     * 3. 验证退款金额是否超过可退款金额
     * 4. 检查是否存在重复的退款申请
     * 5. 创建退款订单记录并设置初始状态为待审核
     * 6. 生成唯一的退款订单号
     * 
     * 业务规则：
     * - 只有支付成功的订单才能申请退款
     * - 退款金额不能超过原支付金额减去已退款金额
     * - 同一支付订单可以多次申请部分退款
     * - 退款申请创建后需要经过审核流程
     * 
     * @param request 创建退款订单的请求参数，包含支付订单ID、退款金额、退款原因等信息
     * @return 创建成功的退款订单响应对象，包含退款订单ID、状态、创建时间等信息
     * @throws IllegalArgumentException 当请求参数无效时抛出（如退款金额为负数、原因为空等）
     * @throws IllegalStateException 当支付订单状态不允许退款时抛出（如未支付、已全额退款等）
     * @throws RuntimeException 当创建退款订单失败时抛出（如数据库操作失败、系统异常等）
     * @since 2025-11-01
     */
    RefundOrderResponse createRefundOrder(RefundCreateRequest request);

    /**
     * 根据退款订单ID查询退款详情
     * 
     * @param refundOrderId 退款订单ID
     * @return 退款订单详情，如果不存在则返回null
     */
    RefundOrderResponse getRefundOrder(String refundOrderId);

    /**
     * 根据支付订单ID查询退款订单列表
     * 
     * @param paymentOrderId 支付订单ID
     * @return 退款订单列表
     */
    List<RefundOrderResponse> getRefundOrdersByPaymentOrderId(String paymentOrderId);

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
    PageResponse<RefundOrderResponse> queryRefundOrders(String userId, RefundStatus status, 
                                                       LocalDateTime startTime, LocalDateTime endTime, 
                                                       int page, int size);

    /**
     * 查询用户的退款订单列表
     * 
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页查询结果
     */
    PageResponse<RefundOrderResponse> getUserRefundOrders(String userId, int page, int size);

    /**
     * 处理退款申请
     * 发起实际的退款流程，调用第三方支付平台退款接口
     * 
     * @param refundOrderId 退款订单ID
     * @return 处理结果
     * @throws IllegalStateException 当退款订单状态不允许处理时抛出
     * @throws RuntimeException 当调用第三方退款接口失败时抛出
     */
    boolean processRefund(String refundOrderId);

    /**
     * 审核退款申请
     * 对退款申请进行人工审核，决定是否同意退款
     * 
     * @param refundOrderId 退款订单ID
     * @param approved 是否同意退款
     * @param auditReason 审核意见
     * @param auditorId 审核人ID
     * @return 审核结果
     * @throws IllegalStateException 当退款订单状态不允许审核时抛出
     */
    boolean auditRefund(String refundOrderId, boolean approved, String auditReason, String auditorId);

    /**
     * 处理退款成功回调
     * 接收第三方支付平台的退款成功回调，更新退款订单状态
     * 
     * @param refundOrderId 退款订单ID
     * @param thirdPartyRefundNo 第三方退款单号
     * @param actualRefundAmount 实际退款金额
     * @param channelResponse 第三方平台响应数据
     * @return 处理结果
     */
    boolean handleRefundSuccess(String refundOrderId, String thirdPartyRefundNo, 
                               BigDecimal actualRefundAmount, String channelResponse);

    /**
     * 处理退款失败回调
     * 接收第三方支付平台的退款失败回调，更新退款订单状态
     * 
     * @param refundOrderId 退款订单ID
     * @param failureReason 失败原因
     * @param channelResponse 第三方平台响应数据
     * @return 处理结果
     */
    boolean handleRefundFailure(String refundOrderId, String failureReason, String channelResponse);

    /**
     * 取消退款申请
     * 用户主动取消退款申请或系统自动取消
     * 
     * @param refundOrderId 退款订单ID
     * @param reason 取消原因
     * @return 取消结果
     * @throws IllegalStateException 当退款订单状态不允许取消时抛出
     */
    boolean cancelRefund(String refundOrderId, String reason);

    /**
     * 查询退款状态
     * 主动查询第三方支付平台的退款状态，用于状态同步
     * 
     * @param refundOrderId 退款订单ID
     * @return 最新的退款状态
     */
    RefundStatus queryRefundStatus(String refundOrderId);

    /**
     * 重试失败的退款订单
     * 对于退款失败但可以重试的订单，重新发起退款
     * 
     * @param refundOrderId 退款订单ID
     * @return 重试结果
     * @throws IllegalStateException 当退款订单不允许重试时抛出
     */
    boolean retryRefund(String refundOrderId);

    /**
     * 获取退款统计数据
     * 统计指定时间范围内的退款数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param userId 用户ID（可选，为null时统计所有用户）
     * @return 统计结果
     */
    RefundStatistics getRefundStatistics(LocalDateTime startTime, LocalDateTime endTime, String userId);

    /**
     * 批量处理待审核的退款申请
     * 定时任务调用，自动处理符合条件的退款申请
     * 
     * @return 处理的退款申请数量
     */
    int batchProcessPendingRefunds();

    /**
     * 重试失败的退款订单
     * 定时任务调用，重新处理失败状态的退款订单
     * @return 重试成功的订单数量
     */
    int retryFailedRefunds();

    /**
     * 同步退款状态
     * 定时任务调用，从第三方支付平台同步退款状态
     * @return 同步成功的订单数量
     */
    int syncRefundStatus();
    
    /**
     * 清理过期的退款记录
     * 定时任务调用，清理指定时间之前的已完成退款记录
     * 
     * @param cutoffTime 截止时间，早于此时间的记录将被清理
     * @return 清理的记录数量
     */
    int cleanupExpiredRecords(LocalDateTime cutoffTime);

    /**
     * 退款统计数据内部类
     */
    class RefundStatistics {
        private long totalRefunds;          // 总退款数
        private long successRefunds;        // 成功退款数
        private long failedRefunds;         // 失败退款数
        private long pendingRefunds;        // 待处理退款数
        private BigDecimal totalAmount;     // 总退款金额
        private BigDecimal successAmount;   // 成功退款金额
        private double successRate;         // 退款成功率

        // 构造函数
        public RefundStatistics(long totalRefunds, long successRefunds, long failedRefunds, 
                              long pendingRefunds, BigDecimal totalAmount, BigDecimal successAmount) {
            this.totalRefunds = totalRefunds;
            this.successRefunds = successRefunds;
            this.failedRefunds = failedRefunds;
            this.pendingRefunds = pendingRefunds;
            this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
            this.successAmount = successAmount != null ? successAmount : BigDecimal.ZERO;
            this.successRate = totalRefunds > 0 ? (double) successRefunds / totalRefunds * 100 : 0.0;
        }

        // Getter方法
        public long getTotalRefunds() { return totalRefunds; }
        public long getSuccessRefunds() { return successRefunds; }
        public long getFailedRefunds() { return failedRefunds; }
        public long getPendingRefunds() { return pendingRefunds; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public BigDecimal getSuccessAmount() { return successAmount; }
        public double getSuccessRate() { return successRate; }
    }
}