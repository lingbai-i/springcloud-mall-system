package com.mall.payment.repository;

import com.mall.payment.entity.PaymentRecord;
import com.mall.payment.enums.PaymentMethod;
import com.mall.payment.enums.PaymentStatus;
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
 * 支付记录数据访问层接口
 * 提供支付记录相关的数据库操作方法，基于Spring Data JPA实现
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>支付记录管理：创建、查询、更新支付记录</li>
 *   <li>关联查询：根据支付订单ID查询相关记录</li>
 *   <li>状态筛选：按支付状态筛选记录</li>
 *   <li>第三方关联：通过第三方交易号查询记录</li>
 *   <li>时间排序：按创建时间排序获取最新记录</li>
 * </ul>
 * 
 * <p>业务场景：</p>
 * <ul>
 *   <li>支付流水：记录每次支付操作的详细信息</li>
 *   <li>重试记录：支付失败后的重试操作记录</li>
 *   <li>状态追踪：跟踪支付状态的变更历史</li>
 *   <li>对账核查：与第三方平台进行对账验证</li>
 *   <li>异常排查：支付异常时的问题定位</li>
 * </ul>
 * 
 * <p>数据关系：</p>
 * <ul>
 *   <li>一对多：一个支付订单可能有多条支付记录</li>
 *   <li>状态关联：记录与支付订单状态保持同步</li>
 *   <li>第三方映射：通过第三方交易号关联外部系统</li>
 * </ul>
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * 修改日志：
 * V1.2 2025-11-01：完善Javadoc注释，增加业务场景和数据关系说明
 * V1.1 2024-12-20：增加最新记录查询和状态筛选方法
 * V1.0 2024-12-01：初始版本，定义基础查询方法
 */
@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, String>, JpaSpecificationExecutor<PaymentRecord> {

    /**
     * 根据支付订单ID查找支付记录列表
     * 
     * @param paymentOrderId 支付订单ID
     * @return 支付记录列表
     */
    List<PaymentRecord> findByPaymentOrderId(String paymentOrderId);

    /**
     * 根据支付订单ID和状态查找支付记录列表
     * 
     * @param paymentOrderId 支付订单ID
     * @param status 支付状态
     * @return 支付记录列表
     */
    List<PaymentRecord> findByPaymentOrderIdAndStatus(String paymentOrderId, PaymentStatus status);

    /**
     * 根据第三方交易号查找支付记录
     * 
     * @param thirdPartyTransactionId 第三方交易号
     * @return 支付记录
     */
    Optional<PaymentRecord> findByThirdPartyTradeNo(String thirdPartyTradeNo);

    /**
     * 根据支付订单ID查找最新的支付记录
     * 
     * @param paymentOrderId 支付订单ID
     * @return 最新的支付记录
     */
    @Query("SELECT p FROM PaymentRecord p WHERE p.paymentOrderId = :paymentOrderId ORDER BY p.createdAt DESC")
    Optional<PaymentRecord> findLatestByPaymentOrderId(@Param("paymentOrderId") String paymentOrderId);

    /**
     * 根据支付订单ID查找成功的支付记录
     * 
     * @param paymentOrderId 支付订单ID
     * @return 成功的支付记录
     */
    @Query("SELECT p FROM PaymentRecord p WHERE p.paymentOrderId = :paymentOrderId AND p.status = :status")
    Optional<PaymentRecord> findSuccessRecordByPaymentOrderId(@Param("paymentOrderId") String paymentOrderId, 
                                                             @Param("status") PaymentStatus status);

    /**
     * 根据支付状态查找支付记录列表
     * 
     * @param status 支付状态
     * @param pageable 分页参数
     * @return 支付记录分页列表
     */
    Page<PaymentRecord> findByStatus(PaymentStatus status, Pageable pageable);

    /**
     * 根据支付方式查找支付记录列表
     * 
     * @param paymentMethod 支付方式
     * @param pageable 分页参数
     * @return 支付记录分页列表
     */
    Page<PaymentRecord> findByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);

    /**
     * 根据支付渠道查找支付记录列表
     * 
     * @param paymentChannel 支付渠道
     * @param pageable 分页参数
     * @return 支付记录分页列表
     */
    Page<PaymentRecord> findByPaymentChannel(String paymentChannel, Pageable pageable);

    /**
     * 查找指定时间范围内的支付记录
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 支付记录分页列表
     */
    Page<PaymentRecord> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 查找指定时间范围内支付完成的记录
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 支付记录分页列表
     */
    Page<PaymentRecord> findByPayTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 查找需要重试的失败记录
     * 
     * @param status 支付状态
     * @param maxRetryCount 最大重试次数
     * @param retryInterval 重试间隔（分钟）
     * @return 需要重试的支付记录列表
     */
    @Query("SELECT p FROM PaymentRecord p WHERE p.status = :status AND p.retryCount < :maxRetryCount " +
           "AND p.updatedAt < :retryTime")
    List<PaymentRecord> findRecordsNeedRetry(@Param("status") PaymentStatus status, 
                                           @Param("maxRetryCount") Integer maxRetryCount,
                                           @Param("retryTime") LocalDateTime retryTime);

    /**
     * 根据客户端IP查找支付记录
     * 
     * @param clientIp 客户端IP
     * @param pageable 分页参数
     * @return 支付记录分页列表
     */
    Page<PaymentRecord> findByClientIp(String clientIp, Pageable pageable);

    /**
     * 根据设备信息查找支付记录
     * 
     * @param deviceInfo 设备信息
     * @param pageable 分页参数
     * @return 支付记录分页列表
     */
    Page<PaymentRecord> findByDeviceInfoContaining(String deviceInfo, Pageable pageable);

    /**
     * 统计指定时间范围内的支付记录数量
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 支付记录数量
     */
    @Query("SELECT COUNT(p) FROM PaymentRecord p WHERE p.createdAt BETWEEN :startTime AND :endTime")
    Long countByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内指定状态的支付记录数量
     * 
     * @param status 支付状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 支付记录数量
     */
    @Query("SELECT COUNT(p) FROM PaymentRecord p WHERE p.status = :status AND p.createdAt BETWEEN :startTime AND :endTime")
    Long countByStatusAndCreatedAtBetween(@Param("status") PaymentStatus status, 
                                         @Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内的支付金额
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 支付金额总和
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentRecord p WHERE p.createdAt BETWEEN :startTime AND :endTime")
    BigDecimal sumAmountByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内指定状态的支付金额
     * 
     * @param status 支付状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 支付金额总和
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentRecord p WHERE p.status = :status " +
           "AND p.payTime BETWEEN :startTime AND :endTime")
    BigDecimal sumAmountByStatusAndPayTimeBetween(@Param("status") PaymentStatus status,
                                                 @Param("startTime") LocalDateTime startTime, 
                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内的手续费总和
     * 
     * @param status 支付状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 手续费总和
     */
    @Query("SELECT COALESCE(SUM(p.feeAmount), 0) FROM PaymentRecord p WHERE p.status = :status " +
           "AND p.payTime BETWEEN :startTime AND :endTime")
    BigDecimal sumFeeByStatusAndPayTimeBetween(@Param("status") PaymentStatus status,
                                              @Param("startTime") LocalDateTime startTime, 
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各支付方式的记录数量和金额
     * 
     * @param status 支付状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 支付方式统计结果
     */
    @Query("SELECT p.paymentMethod, COUNT(p), COALESCE(SUM(p.amount), 0), COALESCE(SUM(p.feeAmount), 0) FROM PaymentRecord p " +
           "WHERE p.status = :status AND p.payTime BETWEEN :startTime AND :endTime " +
           "GROUP BY p.paymentMethod")
    List<Object[]> countByPaymentMethodAndStatusAndPayTimeBetween(@Param("status") PaymentStatus status,
                                                                 @Param("startTime") LocalDateTime startTime, 
                                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各支付渠道的记录数量和金额
     * 
     * @param status 支付状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 支付渠道统计结果
     */
    @Query("SELECT p.paymentChannel, COUNT(p), COALESCE(SUM(p.amount), 0), COALESCE(SUM(p.feeAmount), 0) FROM PaymentRecord p " +
           "WHERE p.status = :status AND p.payTime BETWEEN :startTime AND :endTime " +
           "GROUP BY p.paymentChannel")
    List<Object[]> countByPaymentChannelAndStatusAndPayTimeBetween(@Param("status") PaymentStatus status,
                                                                  @Param("startTime") LocalDateTime startTime, 
                                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 计算平均支付时长
     * 
     * @param status 支付状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 平均支付时长（秒）
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(SECOND, p.createdAt, p.payTime)) FROM PaymentRecord p " +
           "WHERE p.status = :status AND p.payTime BETWEEN :startTime AND :endTime " +
           "AND p.payTime IS NOT NULL")
    Double getAveragePaymentDuration(@Param("status") PaymentStatus status,
                                    @Param("startTime") LocalDateTime startTime, 
                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 更新支付记录状态和相关信息
     * 
     * @param recordId 记录ID
     * @param status 新状态
     * @param thirdPartyTransactionId 第三方交易号
     * @param paymentCompletedTime 支付完成时间
     * @param fee 手续费
     * @param updatedTime 更新时间
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE PaymentRecord p SET p.status = :status, p.thirdPartyTradeNo = :thirdPartyTransactionId, " +
           "p.payTime = :paymentCompletedTime, p.feeAmount = :fee, p.updatedAt = :updatedTime " +
           "WHERE p.id = :recordId")
    int updatePaymentResult(@Param("recordId") String recordId,
                           @Param("status") PaymentStatus status,
                           @Param("thirdPartyTransactionId") String thirdPartyTransactionId,
                           @Param("paymentCompletedTime") LocalDateTime paymentCompletedTime,
                           @Param("fee") BigDecimal fee,
                           @Param("updatedTime") LocalDateTime updatedTime);

    /**
     * 更新支付记录失败信息
     * 
     * @param recordId 记录ID
     * @param status 新状态
     * @param errorMessage 错误信息
     * @param updatedTime 更新时间
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE PaymentRecord p SET p.status = :status, p.errorMessage = :errorMessage, " +
           "p.updatedAt = :updatedTime WHERE p.id = :recordId")
    int updatePaymentFailure(@Param("recordId") String recordId,
                            @Param("status") PaymentStatus status,
                            @Param("errorMessage") String errorMessage,
                            @Param("updatedTime") LocalDateTime updatedTime);

    /**
     * 增加重试次数
     * 
     * @param recordId 记录ID
     * @param updatedTime 更新时间
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE PaymentRecord p SET p.retryCount = p.retryCount + 1, p.updatedAt = :updatedTime " +
           "WHERE p.id = :recordId")
    int incrementRetryCount(@Param("recordId") String recordId, @Param("updatedTime") LocalDateTime updatedTime);

    /**
     * 根据支付订单ID删除支付记录
     * 
     * @param paymentOrderId 支付订单ID
     * @return 删除的记录数
     */
    @Modifying
    @Query("DELETE FROM PaymentRecord p WHERE p.paymentOrderId = :paymentOrderId")
    int deleteByPaymentOrderId(@Param("paymentOrderId") String paymentOrderId);

    /**
     * 检查第三方交易号是否已存在
     * 
     * @param thirdPartyTransactionId 第三方交易号
     * @return 是否存在
     */
    boolean existsByThirdPartyTradeNo(String thirdPartyTradeNo);

    /**
     * 查找指定支付订单的失败记录数量
     * 
     * @param paymentOrderId 支付订单ID
     * @param status 失败状态
     * @return 失败记录数量
     */
    @Query("SELECT COUNT(p) FROM PaymentRecord p WHERE p.paymentOrderId = :paymentOrderId AND p.status = :status")
    Long countFailedRecordsByPaymentOrderId(@Param("paymentOrderId") String paymentOrderId, 
                                           @Param("status") PaymentStatus status);

    /**
     * 查找指定时间范围内的重试记录统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 重试统计结果 [重试次数, 记录数量]
     */
    @Query("SELECT p.retryCount, COUNT(p) FROM PaymentRecord p " +
           "WHERE p.createdAt BETWEEN :startTime AND :endTime AND p.retryCount > 0 " +
           "GROUP BY p.retryCount ORDER BY p.retryCount")
    List<Object[]> getRetryStatistics(@Param("startTime") LocalDateTime startTime, 
                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 查找高风险IP的支付记录
     * 
     * @param minFailureCount 最小失败次数
     * @param timeWindow 时间窗口（小时）
     * @return 高风险IP列表
     */
    @Query("SELECT p.clientIp, COUNT(p) FROM PaymentRecord p " +
           "WHERE p.status = :failureStatus AND p.createdAt >= :timeThreshold " +
           "GROUP BY p.clientIp HAVING COUNT(p) >= :minFailureCount " +
           "ORDER BY COUNT(p) DESC")
    List<Object[]> findHighRiskIps(@Param("failureStatus") PaymentStatus failureStatus,
                                  @Param("minFailureCount") Long minFailureCount,
                                  @Param("timeThreshold") LocalDateTime timeThreshold);

    /**
     * 查找异常设备的支付记录
     * 
     * @param deviceInfo 设备信息关键词
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 异常设备支付记录列表
     */
    @Query("SELECT p FROM PaymentRecord p WHERE p.deviceInfo LIKE %:deviceInfo% " +
           "AND p.createdAt BETWEEN :startTime AND :endTime")
    List<PaymentRecord> findAbnormalDeviceRecords(@Param("deviceInfo") String deviceInfo,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);
}