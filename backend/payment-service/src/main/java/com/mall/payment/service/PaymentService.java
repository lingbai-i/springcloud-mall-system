package com.mall.payment.service;

import com.mall.payment.dto.request.PaymentCreateRequest;
import com.mall.payment.dto.request.PaymentQueryRequest;
import com.mall.payment.dto.response.PaymentOrderResponse;
import com.mall.payment.dto.response.PageResponse;
import com.mall.payment.entity.PaymentOrder;
import com.mall.payment.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 支付服务接口
 * 
 * 定义支付相关的核心业务方法，包括创建支付订单、查询订单、处理支付回调等功能。
 * 该接口是支付系统的核心服务层，提供完整的支付生命周期管理，包括：
 * - 支付订单的创建、查询和状态管理
 * - 与第三方支付平台的交互处理
 * - 支付回调的处理和状态同步
 * - 支付统计和监控功能
 * - 异常处理和重试机制
 * 
 * 支持的支付方式：
 * - 支付宝支付
 * - 微信支付
 * - 银行卡支付
 * - 余额支付
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * 修改日志：
 * V1.2 2025-11-01：完善Javadoc注释，添加详细的方法说明和异常处理
 * V1.1 2025-01-01：添加支付统计和监控功能
 * V1.0 2024-12-01：初始版本，基础支付功能
 */
public interface PaymentService {

    /**
     * 创建支付订单
     * 
     * 根据请求参数创建新的支付订单，并进行必要的业务验证。
     * 该方法会执行以下操作：
     * 1. 验证请求参数的有效性（订单ID、用户ID、金额等）
     * 2. 检查订单是否已存在，防止重复创建
     * 3. 验证支付金额是否在允许范围内
     * 4. 创建支付订单记录并设置初始状态
     * 5. 生成唯一的支付订单ID
     * 
     * @param request 创建支付订单的请求参数，包含订单ID、用户ID、支付金额、支付方式等信息
     * @return 创建成功的支付订单响应对象，包含支付订单ID、状态、过期时间等信息
     * @throws IllegalArgumentException 当请求参数无效时抛出（如金额为负数、订单ID为空等）
     * @throws RuntimeException 当创建订单失败时抛出（如数据库操作失败、系统异常等）
     * @since 2025-11-01
     */
    PaymentOrderResponse createPaymentOrder(PaymentCreateRequest request);

    /**
     * 根据支付订单ID查询订单详情
     * 
     * 通过支付订单的唯一标识符查询完整的订单信息，包括订单状态、支付金额、
     * 支付方式、创建时间等详细信息。该方法支持缓存机制以提高查询性能。
     * 
     * @param paymentOrderId 支付订单ID，不能为空或空字符串
     * @return 支付订单详情响应对象，包含完整的订单信息；如果订单不存在则返回null
     * @throws IllegalArgumentException 当支付订单ID为空或格式无效时抛出
     * @since 2025-11-01
     */
    PaymentOrderResponse getPaymentOrder(String paymentOrderId);

    /**
     * 根据业务订单ID查询支付订单
     * 
     * 通过业务系统的订单ID查询对应的支付订单信息。一个业务订单可能对应
     * 多个支付订单（如部分支付、重新支付等情况），该方法返回最新的支付订单。
     * 
     * @param orderId 业务订单ID，来自业务系统的订单标识符，不能为空
     * @return 支付订单详情响应对象，如果不存在对应的支付订单则返回null
     * @throws IllegalArgumentException 当业务订单ID为空或格式无效时抛出
     * @since 2025-11-01
     */
    PaymentOrderResponse getPaymentOrderByOrderId(String orderId);

    /**
     * 根据第三方订单号查询支付订单
     * 
     * @param thirdPartyOrderNo 第三方订单号
     * @return 支付订单详情，如果不存在则返回null
     */
    PaymentOrderResponse getPaymentOrderByThirdPartyOrderNo(String thirdPartyOrderNo);

    /**
     * 分页查询支付订单
     * 支持多种查询条件的组合查询
     * 
     * @param request 查询请求参数
     * @return 分页查询结果
     */
    PageResponse<PaymentOrderResponse> queryPaymentOrders(PaymentQueryRequest request);

    /**
     * 查询用户的支付订单列表
     * 
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页查询结果
     */
    PageResponse<PaymentOrderResponse> getUserPaymentOrders(String userId, int page, int size);

    /**
     * 发起支付
     * 
     * 调用第三方支付平台接口，发起实际的支付流程。该方法会执行以下操作：
     * 1. 验证支付订单状态，确保订单可以进行支付
     * 2. 进行风控检查，包括IP风险、设备风险等
     * 3. 获取分布式锁，防止重复支付
     * 4. 调用对应的支付渠道服务发起支付
     * 5. 更新订单状态为支付中
     * 6. 记录支付操作日志和监控指标
     * 
     * @param paymentOrderId 支付订单ID，必须是有效的支付订单标识符
     * @param clientIp 客户端IP地址，用于风控检查和日志记录
     * @param userAgent 用户代理信息，包含浏览器和设备信息
     * @return 支付结果Map，包含支付URL、二维码内容、订单信息等，具体内容根据支付方式而定
     * @throws IllegalArgumentException 当参数无效时抛出
     * @throws IllegalStateException 当订单状态不允许支付时抛出（如已支付、已取消等）
     * @throws RuntimeException 当调用第三方支付接口失败或系统异常时抛出
     * @since 2025-11-01
     */
    Map<String, Object> initiatePayment(String paymentOrderId, String clientIp, String userAgent);

    /**
     * 处理支付成功回调
     * 
     * 接收第三方支付平台的成功回调通知，更新订单状态为支付成功。
     * 该方法会执行以下操作：
     * 1. 验证回调数据的有效性和签名
     * 2. 获取分布式锁防止重复处理
     * 3. 检查订单当前状态是否允许更新
     * 4. 更新订单状态、实际支付金额等信息
     * 5. 计算并记录手续费
     * 6. 清除相关缓存
     * 7. 记录支付成功日志和监控指标
     * 
     * @param paymentOrderId 支付订单ID，必须是有效的支付订单标识符
     * @param thirdPartyOrderNo 第三方支付平台的订单号，用于对账和查询
     * @param actualAmount 实际支付金额，可能与订单金额存在微小差异
     * @param channelResponse 第三方平台的完整响应数据，JSON格式
     * @return 处理结果，true表示处理成功，false表示处理失败或重复处理
     * @throws IllegalArgumentException 当参数无效时抛出
     * @since 2025-11-01
     */
    boolean handlePaymentSuccess(String paymentOrderId, String thirdPartyOrderNo, 
                                BigDecimal actualAmount, String channelResponse);

    /**
     * 处理支付失败回调
     * 接收第三方支付平台的失败回调，更新订单状态
     * 
     * @param paymentOrderId 支付订单ID
     * @param failureReason 失败原因
     * @param channelResponse 第三方平台响应数据
     * @return 处理结果
     */
    boolean handlePaymentFailure(String paymentOrderId, String failureReason, String channelResponse);

    /**
     * 取消支付订单
     * 将订单状态更新为已取消，如果已发起支付则调用第三方平台取消接口
     * 
     * @param paymentOrderId 支付订单ID
     * @param reason 取消原因
     * @return 取消结果
     * @throws IllegalStateException 当订单状态不允许取消时抛出
     */
    boolean cancelPaymentOrder(String paymentOrderId, String reason);

    /**
     * 查询支付状态
     * 主动查询第三方支付平台的订单状态，用于状态同步
     * 
     * @param paymentOrderId 支付订单ID
     * @return 最新的支付状态
     */
    PaymentStatus queryPaymentStatus(String paymentOrderId);

    /**
     * 处理过期订单
     * 定时任务调用，将过期的待支付订单状态更新为已过期
     * 
     * @return 处理的过期订单数量
     */
    int handleExpiredOrders();

    /**
     * 重试失败的支付订单
     * 对于支付失败但可以重试的订单，重新发起支付
     * 
     * @param paymentOrderId 支付订单ID
     * @param clientIp 客户端IP地址
     * @param userAgent 用户代理信息
     * @return 重试结果
     * @throws IllegalStateException 当订单不允许重试时抛出
     */
    Map<String, Object> retryPayment(String paymentOrderId, String clientIp, String userAgent);

    /**
     * 获取支付统计数据
     * 统计指定时间范围内的支付数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param userId 用户ID（可选，为null时统计所有用户）
     * @return 统计结果
     */
    PaymentStatistics getPaymentStatistics(LocalDateTime startTime, LocalDateTime endTime, String userId);

    /**
     * 重试失败的支付订单
     * 定时任务调用，重新处理失败状态的支付订单
     * @return 重试成功的订单数量
     */
    int retryFailedOrders();

    /**
     * 同步支付状态
     * 定时任务调用，从第三方支付平台同步订单状态
     * @return 同步成功的订单数量
     */
    int syncPaymentStatus();

    /**
     * 清理过期的支付记录
     * 清理指定时间之前的已完成支付记录
     * 
     * @param cutoffTime 截止时间，早于此时间的记录将被清理
     * @return 清理的记录数量
     */
    int cleanupExpiredRecords(LocalDateTime cutoffTime);

    /**
     * 支付统计数据内部类
     */
    class PaymentStatistics {
        private long totalOrders;           // 总订单数
        private long successOrders;         // 成功订单数
        private long failedOrders;          // 失败订单数
        private long cancelledOrders;       // 取消订单数
        private BigDecimal totalAmount;     // 总金额
        private BigDecimal successAmount;   // 成功金额
        private double successRate;         // 成功率

        // 构造函数、getter和setter方法
        public PaymentStatistics(long totalOrders, long successOrders, long failedOrders, 
                               long cancelledOrders, BigDecimal totalAmount, BigDecimal successAmount) {
            this.totalOrders = totalOrders;
            this.successOrders = successOrders;
            this.failedOrders = failedOrders;
            this.cancelledOrders = cancelledOrders;
            this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
            this.successAmount = successAmount != null ? successAmount : BigDecimal.ZERO;
            this.successRate = totalOrders > 0 ? (double) successOrders / totalOrders * 100 : 0.0;
        }

        // Getter方法
        public long getTotalOrders() { return totalOrders; }
        public long getSuccessOrders() { return successOrders; }
        public long getFailedOrders() { return failedOrders; }
        public long getCancelledOrders() { return cancelledOrders; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public BigDecimal getSuccessAmount() { return successAmount; }
        public double getSuccessRate() { return successRate; }
    }
}