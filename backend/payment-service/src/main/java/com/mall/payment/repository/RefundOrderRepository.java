package com.mall.payment.repository;

import com.mall.payment.entity.RefundOrder;
import com.mall.payment.enums.RefundStatus;
import com.mall.payment.enums.RefundType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 退款订单数据访问层接口
 * 提供退款订单相关的数据库操作方法
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@Repository
public interface RefundOrderRepository extends JpaRepository<RefundOrder, String>, JpaSpecificationExecutor<RefundOrder> {

    /**
     * 根据退款单号查找退款订单
     * 
     * @param refundNo 退款单号
     * @return 退款订单
     */
    Optional<RefundOrder> findByRefundNo(String refundNo);

    /**
     * 根据支付订单ID查找退款订单列表
     * 
     * @param paymentOrderId 支付订单ID
     * @return 退款订单列表
     */
    List<RefundOrder> findByPaymentOrderId(String paymentOrderId);

    /**
     * 根据支付订单ID和状态查找退款订单列表
     * 
     * @param paymentOrderId 支付订单ID
     * @param status 退款状态
     * @return 退款订单列表
     */
    List<RefundOrder> findByPaymentOrderIdAndStatus(String paymentOrderId, RefundStatus status);

    /**
     * 根据用户ID查找退款订单列表
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 退款订单分页列表
     */
    Page<RefundOrder> findByUserId(String userId, Pageable pageable);

    /**
     * 根据用户ID和状态查找退款订单列表
     * 
     * @param userId 用户ID
     * @param status 退款状态
     * @param pageable 分页参数
     * @return 退款订单分页列表
     */
    Page<RefundOrder> findByUserIdAndStatus(String userId, RefundStatus status, Pageable pageable);

    /**
     * 根据退款状态查找退款订单列表
     * 
     * @param status 退款状态
     * @param pageable 分页参数
     * @return 退款订单分页列表
     */
    Page<RefundOrder> findByStatus(RefundStatus status, Pageable pageable);

    /**
     * 根据退款类型查找退款订单列表
     * 
     * @param refundType 退款类型
     * @param pageable 分页参数
     * @return 退款订单分页列表
     */
    Page<RefundOrder> findByRefundType(RefundType refundType, Pageable pageable);

    /**
     * 根据第三方退款单号查找退款订单
     * 
     * @param thirdPartyRefundNo 第三方退款单号
     * @return 退款订单
     */
    Optional<RefundOrder> findByThirdPartyRefundNo(String thirdPartyRefundNo);

    /**
     * 根据创建时间范围查找退款订单（分页）
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 退款订单分页列表
     */
    Page<RefundOrder> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 根据创建时间范围查找退款订单（不分页）
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 退款订单列表
     */
    List<RefundOrder> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据用户ID和创建时间范围查找退款订单
     * 
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 退款订单分页列表
     */
    Page<RefundOrder> findByUserIdAndCreatedAtBetween(String userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 根据状态和创建时间范围查找退款订单
     * 
     * @param status 退款状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 退款订单分页列表
     */
    Page<RefundOrder> findByStatusAndCreatedAtBetween(RefundStatus status, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * 根据状态和创建时间查找退款订单（创建时间早于指定时间）
     * 
     * @param status 退款状态
     * @param cutoffTime 截止时间
     * @return 退款订单列表
     */
    List<RefundOrder> findByStatusAndCreatedAtBefore(RefundStatus status, LocalDateTime cutoffTime);

    /**
     * 根据用户ID、状态和创建时间范围查找退款订单
     * 
     * @param userId 用户ID
     * @param status 退款状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 退款订单分页列表
     */
    Page<RefundOrder> findByUserIdAndStatusAndCreatedAtBetween(String userId, RefundStatus status, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 查找指定时间范围内退款完成的订单
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 退款订单分页列表
     */
    Page<RefundOrder> findByRefundTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 查找待审核的退款订单
     * 
     * @param status 待审核状态
     * @param pageable 分页参数
     * @return 退款订单分页列表
     */
    Page<RefundOrder> findByStatusOrderByCreatedAtAsc(RefundStatus status, Pageable pageable);

    /**
     * 查找需要人工处理的退款订单
     * 
     * @param statuses 需要人工处理的状态列表
     * @param pageable 分页参数
     * @return 退款订单分页列表
     */
    @Query("SELECT r FROM RefundOrder r WHERE r.status IN :statuses ORDER BY r.createdAt ASC")
    Page<RefundOrder> findOrdersNeedManualProcess(@Param("statuses") List<RefundStatus> statuses, Pageable pageable);

    /**
     * 查找需要重试的失败退款订单
     * 
     * @param status 失败状态
     * @param maxRetryCount 最大重试次数
     * @param retryInterval 重试间隔（分钟）
     * @return 需要重试的退款订单列表
     */
    @Query("SELECT r FROM RefundOrder r WHERE r.status = :status AND r.retryCount < :maxRetryCount " +
           "AND r.updatedAt < :retryTime")
    List<RefundOrder> findOrdersNeedRetry(@Param("status") RefundStatus status, 
                                         @Param("maxRetryCount") Integer maxRetryCount,
                                         @Param("retryTime") LocalDateTime retryTime);

    /**
     * 查找超时未处理的退款订单
     * 
     * @param status 处理中状态
     * @param timeoutThreshold 超时阈值
     * @return 超时的退款订单列表
     */
    @Query("SELECT r FROM RefundOrder r WHERE r.status = :status AND r.processTime < :timeoutThreshold")
    List<RefundOrder> findTimeoutOrders(@Param("status") RefundStatus status, 
                                       @Param("timeoutThreshold") LocalDateTime timeoutThreshold);

    /**
     * 统计指定时间范围内的退款订单数量
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 退款订单数量
     */
    @Query("SELECT COUNT(r) FROM RefundOrder r WHERE r.createdAt BETWEEN :startTime AND :endTime")
    Long countByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内指定状态的退款订单数量
     * 
     * @param status 退款状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 退款订单数量
     */
    @Query("SELECT COUNT(r) FROM RefundOrder r WHERE r.status = :status AND r.createdAt BETWEEN :startTime AND :endTime")
    Long countByStatusAndCreatedAtBetween(@Param("status") RefundStatus status, 
                                        @Param("startTime") LocalDateTime startTime, 
                                        @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内的退款总金额
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 退款总金额
     */
    @Query("SELECT COALESCE(SUM(r.refundAmount), 0) FROM RefundOrder r WHERE r.createdAt BETWEEN :startTime AND :endTime")
    BigDecimal sumRefundAmountByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内指定状态的退款金额
     * 
     * @param status 退款状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 退款金额总和
     */
    @Query("SELECT COALESCE(SUM(r.actualRefundAmount), 0) FROM RefundOrder r WHERE r.status = :status " +
           "AND r.refundTime BETWEEN :startTime AND :endTime")
    BigDecimal sumActualRefundAmountByStatusAndRefundTimeBetween(@Param("status") RefundStatus status,
                                                               @Param("startTime") LocalDateTime startTime, 
                                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内的退款手续费总和
     * 
     * @param status 退款状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 退款手续费总和
     */
    @Query("SELECT COALESCE(SUM(r.refundFee), 0) FROM RefundOrder r WHERE r.status = :status " +
           "AND r.refundTime BETWEEN :startTime AND :endTime")
    BigDecimal sumRefundFeeByStatusAndRefundTimeBetween(@Param("status") RefundStatus status,
                                                      @Param("startTime") LocalDateTime startTime, 
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各退款类型的订单数量和金额
     * 
     * @param status 退款状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 退款类型统计结果
     */
    @Query("SELECT r.refundType, COUNT(r), COALESCE(SUM(r.actualRefundAmount), 0), COALESCE(SUM(r.refundFee), 0) FROM RefundOrder r " +
           "WHERE r.status = :status AND r.refundTime BETWEEN :startTime AND :endTime " +
           "GROUP BY r.refundType")
    List<Object[]> countByRefundTypeAndStatusAndRefundTimeBetween(@Param("status") RefundStatus status,
                                                                @Param("startTime") LocalDateTime startTime, 
                                                                @Param("endTime") LocalDateTime endTime);

    /**
     * 统计每日退款数据
     * 
     * @param status 退款状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 每日统计结果
     */
    @Query("SELECT DATE(r.refundTime), COUNT(r), COALESCE(SUM(r.actualRefundAmount), 0), COALESCE(SUM(r.refundFee), 0) " +
           "FROM RefundOrder r WHERE r.status = :status AND r.refundTime BETWEEN :startTime AND :endTime " +
           "GROUP BY DATE(r.refundTime) ORDER BY DATE(r.refundTime)")
    List<Object[]> getDailyRefundStatistics(@Param("status") RefundStatus status,
                                           @Param("startTime") LocalDateTime startTime, 
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 计算平均退款处理时长
     * 
     * @param status 退款状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 平均处理时长（小时）
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, r.createdAt, r.refundTime)) FROM RefundOrder r " +
           "WHERE r.status = :status AND r.refundTime BETWEEN :startTime AND :endTime " +
           "AND r.refundTime IS NOT NULL")
    Double getAverageProcessingHours(@Param("status") RefundStatus status,
                                    @Param("startTime") LocalDateTime startTime, 
                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 更新退款订单审核结果
     * 
     * @param orderId 订单ID
     * @param status 新状态
     * @param reviewerId 审核人ID
     * @param reviewTime 审核时间
     * @param reviewRemark 审核备注
     * @param updatedTime 更新时间
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE RefundOrder r SET r.status = :status, r.reviewerId = :reviewerId, " +
           "r.reviewTime = :reviewTime, r.reviewRemark = :reviewRemark, r.updatedAt = :updatedTime " +
           "WHERE r.id = :orderId")
    int updateReviewResult(@Param("orderId") String orderId,
                          @Param("status") RefundStatus status,
                          @Param("reviewerId") String reviewerId,
                          @Param("reviewTime") LocalDateTime reviewTime,
                          @Param("reviewRemark") String reviewRemark,
                          @Param("updatedTime") LocalDateTime updatedTime);

    /**
     * 更新退款订单处理信息
     * 
     * @param orderId 订单ID
     * @param status 新状态
     * @param processorId 处理人ID
     * @param processTime 处理开始时间
     * @param updatedTime 更新时间
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE RefundOrder r SET r.status = :status, r.processorId = :processorId, " +
           "r.processTime = :processTime, r.updatedAt = :updatedTime " +
           "WHERE r.id = :orderId")
    int updateProcessInfo(@Param("orderId") String orderId,
                         @Param("status") RefundStatus status,
                         @Param("processorId") String processorId,
                         @Param("processTime") LocalDateTime processTime,
                         @Param("updatedTime") LocalDateTime updatedTime);

    /**
     * 更新退款订单成功结果
     * 
     * @param orderId 订单ID
     * @param status 新状态
     * @param actualRefundAmount 实际退款金额
     * @param refundFee 退款手续费
     * @param thirdPartyRefundNo 第三方退款单号
     * @param refundTime 退款完成时间
     * @param updatedTime 更新时间
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE RefundOrder r SET r.status = :status, r.actualRefundAmount = :actualRefundAmount, " +
           "r.refundFee = :refundFee, r.thirdPartyRefundNo = :thirdPartyRefundNo, " +
           "r.refundTime = :refundTime, " +
           "r.updatedAt = :updatedTime WHERE r.id = :orderId")
    int updateRefundSuccess(@Param("orderId") String orderId,
                           @Param("status") RefundStatus status,
                           @Param("actualRefundAmount") BigDecimal actualRefundAmount,
                           @Param("refundFee") BigDecimal refundFee,
                           @Param("thirdPartyRefundNo") String thirdPartyRefundNo,
                           @Param("refundTime") LocalDateTime refundTime,
                           @Param("updatedTime") LocalDateTime updatedTime);

    /**
     * 更新退款订单失败结果
     * 
     * @param orderId 订单ID
     * @param status 新状态
     * @param failureReason 失败原因
     * @param updatedTime 更新时间
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE RefundOrder r SET r.status = :status, r.failureReason = :failureReason, " +
           "r.updatedAt = :updatedTime WHERE r.id = :orderId")
    int updateRefundFailure(@Param("orderId") String orderId,
                           @Param("status") RefundStatus status,
                           @Param("failureReason") String failureReason,
                           @Param("updatedTime") LocalDateTime updatedTime);

    /**
     * 增加退款订单重试次数
     * 
     * @param orderId 订单ID
     * @param updatedTime 更新时间
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE RefundOrder r SET r.retryCount = r.retryCount + 1, r.updatedAt = :updatedTime " +
           "WHERE r.id = :orderId")
    int incrementRetryCount(@Param("orderId") String orderId, @Param("updatedTime") LocalDateTime updatedTime);

    /**
     * 检查退款单号是否已存在
     * 
     * @param refundNo 退款单号
     * @return 是否存在
     */
    boolean existsByRefundNo(String refundNo);

    /**
     * 检查第三方退款单号是否存在
     * 
     * @param thirdPartyRefundNo 第三方退款单号
     * @return 是否存在
     */
    boolean existsByThirdPartyRefundNo(String thirdPartyRefundNo);

    /**
     * 查找指定支付订单的退款总金额
     * 
     * @param paymentOrderId 支付订单ID
     * @param successStatus 成功状态
     * @return 退款总金额
     */
    @Query("SELECT COALESCE(SUM(r.actualRefundAmount), 0) FROM RefundOrder r " +
           "WHERE r.paymentOrderId = :paymentOrderId AND r.status = :successStatus")
    BigDecimal sumRefundedAmountByPaymentOrderId(@Param("paymentOrderId") String paymentOrderId, 
                                                @Param("successStatus") RefundStatus successStatus);

    /**
     * 查找用户最近的退款订单
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 最近的退款订单列表
     */
    @Query("SELECT r FROM RefundOrder r WHERE r.userId = :userId ORDER BY r.createdAt DESC")
    List<RefundOrder> findRecentOrdersByUserId(@Param("userId") String userId, Pageable pageable);

    /**
     * 查找指定金额范围的退款订单
     * 
     * @param minAmount 最小金额
     * @param maxAmount 最大金额
     * @param pageable 分页参数
     * @return 退款订单分页列表
     */
    Page<RefundOrder> findByRefundAmountBetween(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    /**
     * 查找审核人的退款订单
     * 
     * @param reviewerId 审核人ID
     * @param pageable 分页参数
     * @return 退款订单分页列表
     */
    Page<RefundOrder> findByReviewerId(String reviewerId, Pageable pageable);

    /**
     * 查找处理人的退款订单
     * 
     * @param processorId 处理人ID
     * @param pageable 分页参数
     * @return 退款订单分页列表
     */
    Page<RefundOrder> findByProcessorId(String processorId, Pageable pageable);

    /**
     * 查找失败状态且可重试的退款订单
     * 用于定时任务重试失败的退款订单
     * 
     * @param cutoffTime 截止时间（创建时间在此时间之后的订单）
     * @param maxRetryCount 最大重试次数
     * @return 失败且可重试的退款订单列表
     */
    @Query("SELECT r FROM RefundOrder r WHERE r.status = 'FAILED' AND r.createdAt >= :cutoffTime " +
           "AND r.retryCount < :maxRetryCount ORDER BY r.createdAt ASC")
    List<RefundOrder> findFailedRefundsForRetry(@Param("cutoffTime") LocalDateTime cutoffTime, 
                                               @Param("maxRetryCount") Integer maxRetryCount);

    /**
     * 查找处理中状态的退款订单用于状态同步
     * 用于定时任务同步第三方支付平台的退款状态
     * 
     * @param cutoffTime 截止时间（创建时间在此时间之后的订单）
     * @return 处理中的退款订单列表
     */
    @Query("SELECT r FROM RefundOrder r WHERE r.status = 'PROCESSING' AND r.createdAt >= :cutoffTime " +
           "AND r.thirdPartyRefundNo IS NOT NULL ORDER BY r.createdAt ASC")
    List<RefundOrder> findProcessingRefundsForSync(@Param("cutoffTime") LocalDateTime cutoffTime);
}