package com.mall.payment.repository;

import com.mall.payment.entity.RefundRecord;
import com.mall.payment.enums.RefundStatus;
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
 * 退款记录数据访问层接口
 * 提供退款记录相关的数据库操作方法
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@Repository
public interface RefundRecordRepository extends JpaRepository<RefundRecord, String>, JpaSpecificationExecutor<RefundRecord> {

    /**
     * 根据退款订单ID查找退款记录列表
     * 
     * @param refundOrderId 退款订单ID
     * @return 退款记录列表
     */
    List<RefundRecord> findByRefundOrderId(String refundOrderId);

    /**
     * 根据退款订单ID和状态查找退款记录列表
     * 
     * @param refundOrderId 退款订单ID
     * @param status 退款状态
     * @return 退款记录列表
     */
    List<RefundRecord> findByRefundOrderIdAndStatus(String refundOrderId, RefundStatus status);

    /**
     * 根据第三方退款单号查找退款记录
     * 
     * @param thirdPartyRefundNo 第三方退款单号
     * @return 退款记录
     */
    Optional<RefundRecord> findByThirdPartyRefundNo(String thirdPartyRefundNo);

    /**
     * 根据退款订单ID查找最新的退款记录
     * 
     * @param refundOrderId 退款订单ID
     * @return 最新的退款记录
     */
    @Query("SELECT r FROM RefundRecord r WHERE r.refundOrderId = :refundOrderId ORDER BY r.createdAt DESC")
    Optional<RefundRecord> findLatestByRefundOrderId(@Param("refundOrderId") String refundOrderId);

    /**
     * 根据退款订单ID查找成功的退款记录
     * 
     * @param refundOrderId 退款订单ID
     * @param status 成功状态
     * @return 成功的退款记录
     */
    @Query("SELECT r FROM RefundRecord r WHERE r.refundOrderId = :refundOrderId AND r.status = :status")
    Optional<RefundRecord> findSuccessRecordByRefundOrderId(@Param("refundOrderId") String refundOrderId, 
                                                           @Param("status") RefundStatus status);

    /**
     * 根据退款状态查找退款记录列表
     * 
     * @param status 退款状态
     * @param pageable 分页参数
     * @return 退款记录分页列表
     */
    Page<RefundRecord> findByStatus(RefundStatus status, Pageable pageable);

    /**
     * 根据退款渠道查找退款记录列表
     * 
     * @param refundChannel 退款渠道
     * @param pageable 分页参数
     * @return 退款记录分页列表
     */
    Page<RefundRecord> findByRefundChannel(String refundChannel, Pageable pageable);

    /**
     * 根据操作人ID查找退款记录列表
     * 
     * @param operatorId 操作人ID
     * @param pageable 分页参数
     * @return 退款记录分页列表
     */
    Page<RefundRecord> findByOperatorId(String operatorId, Pageable pageable);

    /**
     * 根据操作人类型查找退款记录列表
     * 
     * @param operatorType 操作人类型
     * @param pageable 分页参数
     * @return 退款记录分页列表
     */
    Page<RefundRecord> findByOperatorType(String operatorType, Pageable pageable);

    /**
     * 查找指定时间范围内的退款记录
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 退款记录分页列表
     */
    Page<RefundRecord> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 查找指定时间范围内退款完成的记录
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 退款记录分页列表
     */
    Page<RefundRecord> findByRefundTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 查找需要重试的失败记录
     * 
     * @param status 失败状态
     * @param maxRetryCount 最大重试次数
     * @param retryInterval 重试间隔（分钟）
     * @return 需要重试的退款记录列表
     */
    @Query("SELECT r FROM RefundRecord r WHERE r.status = :status AND r.retryCount < :maxRetryCount " +
           "AND r.updatedAt < :retryTime")
    List<RefundRecord> findRecordsNeedRetry(@Param("status") RefundStatus status, 
                                           @Param("maxRetryCount") Integer maxRetryCount,
                                           @Param("retryTime") LocalDateTime retryTime);

    /**
     * 统计指定时间范围内的退款记录数量
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 退款记录数量
     */
    @Query("SELECT COUNT(r) FROM RefundRecord r WHERE r.createdAt BETWEEN :startTime AND :endTime")
    Long countByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内指定状态的退款记录数量
     * 
     * @param status 退款状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 退款记录数量
     */
    @Query("SELECT COUNT(r) FROM RefundRecord r WHERE r.status = :status AND r.createdAt BETWEEN :startTime AND :endTime")
    Long countByStatusAndCreatedAtBetween(@Param("status") RefundStatus status, 
                                         @Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内的退款金额
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 退款金额总和
     */
    @Query("SELECT COALESCE(SUM(r.refundAmount), 0) FROM RefundRecord r WHERE r.createdAt BETWEEN :startTime AND :endTime")
    BigDecimal sumRefundAmountByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内指定状态的退款金额
     * 
     * @param status 退款状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 退款金额总和
     */
    @Query("SELECT COALESCE(SUM(r.actualRefundAmount), 0) FROM RefundRecord r WHERE r.status = :status " +
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
    @Query("SELECT COALESCE(SUM(r.refundFee), 0) FROM RefundRecord r WHERE r.status = :status " +
           "AND r.refundTime BETWEEN :startTime AND :endTime")
    BigDecimal sumRefundFeeByStatusAndRefundTimeBetween(@Param("status") RefundStatus status,
                                                      @Param("startTime") LocalDateTime startTime, 
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各退款渠道的记录数量和金额
     * 
     * @param status 退款状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 退款渠道统计结果
     */
    @Query("SELECT r.refundChannel, COUNT(r), COALESCE(SUM(r.actualRefundAmount), 0), COALESCE(SUM(r.refundFee), 0) FROM RefundRecord r " +
           "WHERE r.status = :status AND r.refundTime BETWEEN :startTime AND :endTime " +
           "GROUP BY r.refundChannel")
    List<Object[]> countByRefundChannelAndStatusAndRefundTimeBetween(@Param("status") RefundStatus status,
                                                                   @Param("startTime") LocalDateTime startTime, 
                                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各操作人类型的记录数量和金额
     * 
     * @param status 退款状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 操作人类型统计结果
     */
    @Query("SELECT r.operatorType, COUNT(r), COALESCE(SUM(r.actualRefundAmount), 0) FROM RefundRecord r " +
           "WHERE r.status = :status AND r.refundTime BETWEEN :startTime AND :endTime " +
           "GROUP BY r.operatorType")
    List<Object[]> countByOperatorTypeAndStatusAndRefundTimeBetween(@Param("status") RefundStatus status,
                                                                  @Param("startTime") LocalDateTime startTime, 
                                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 计算平均退款时长
     * 
     * @param status 退款状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 平均退款时长（秒）
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(SECOND, r.createdAt, r.refundTime)) FROM RefundRecord r " +
           "WHERE r.status = :status AND r.refundTime BETWEEN :startTime AND :endTime " +
           "AND r.refundTime IS NOT NULL")
    Double getAverageRefundDuration(@Param("status") RefundStatus status,
                                   @Param("startTime") LocalDateTime startTime, 
                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 更新退款记录状态和相关信息
     * 
     * @param recordId 记录ID
     * @param status 新状态
     * @param thirdPartyRefundId 第三方退款单号
     * @param refundTime 退款完成时间
     * @param actualRefundAmount 实际退款金额
     * @param refundFee 退款手续费
     * @param expectedArrivalTime 预计到账时间
     * @param updatedTime 更新时间
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE RefundRecord r SET r.status = :status, r.thirdPartyRefundNo = :thirdPartyRefundNo, " +
           "r.refundTime = :refundTime, r.actualRefundAmount = :actualRefundAmount, " +
           "r.refundFee = :refundFee, r.expectedArrivalTime = :expectedArrivalTime, r.updatedAt = :updatedTime " +
           "WHERE r.id = :recordId")
    int updateRefundSuccess(@Param("recordId") String recordId,
                           @Param("status") RefundStatus status,
                           @Param("thirdPartyRefundNo") String thirdPartyRefundNo,
                           @Param("refundTime") LocalDateTime refundTime,
                           @Param("actualRefundAmount") BigDecimal actualRefundAmount,
                           @Param("refundFee") BigDecimal refundFee,
                           @Param("expectedArrivalTime") LocalDateTime expectedArrivalTime,
                           @Param("updatedTime") LocalDateTime updatedTime);

    /**
     * 更新退款记录失败信息
     * 
     * @param recordId 记录ID
     * @param status 新状态
     * @param errorCode 错误代码
     * @param errorMessage 错误信息
     * @param updatedTime 更新时间
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE RefundRecord r SET r.status = :status, r.errorCode = :errorCode, " +
           "r.errorMessage = :errorMessage, r.updatedAt = :updatedTime WHERE r.id = :recordId")
    int updateRefundFailure(@Param("recordId") String recordId,
                           @Param("status") RefundStatus status,
                           @Param("errorCode") String errorCode,
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
    @Query("UPDATE RefundRecord r SET r.retryCount = r.retryCount + 1, r.updatedAt = :updatedTime " +
           "WHERE r.id = :recordId")
    int incrementRetryCount(@Param("recordId") String recordId, @Param("updatedTime") LocalDateTime updatedTime);

    /**
     * 根据退款订单ID删除退款记录
     * 
     * @param refundOrderId 退款订单ID
     * @return 删除的记录数
     */
    @Modifying
    @Query("DELETE FROM RefundRecord r WHERE r.refundOrderId = :refundOrderId")
    int deleteByRefundOrderId(@Param("refundOrderId") String refundOrderId);

    /**
     * 检查第三方退款单号是否存在
     * 
     * @param thirdPartyRefundNo 第三方退款单号
     * @return 是否存在
     */
    boolean existsByThirdPartyRefundNo(String thirdPartyRefundNo);

    /**
     * 查找指定退款订单的失败记录数量
     * 
     * @param refundOrderId 退款订单ID
     * @param status 失败状态
     * @return 失败记录数量
     */
    @Query("SELECT COUNT(r) FROM RefundRecord r WHERE r.refundOrderId = :refundOrderId AND r.status = :status")
    Long countFailedRecordsByRefundOrderId(@Param("refundOrderId") String refundOrderId, 
                                          @Param("status") RefundStatus status);

    /**
     * 查找指定时间范围内的重试记录统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 重试统计结果 [重试次数, 记录数量]
     */
    @Query("SELECT r.retryCount, COUNT(r) FROM RefundRecord r " +
           "WHERE r.createdAt BETWEEN :startTime AND :endTime AND r.retryCount > 0 " +
           "GROUP BY r.retryCount ORDER BY r.retryCount")
    List<Object[]> getRetryStatistics(@Param("startTime") LocalDateTime startTime, 
                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 查找操作人的退款记录统计
     * 
     * @param operatorId 操作人ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 操作人统计结果 [总数量, 成功数量, 失败数量, 总金额]
     */
    @Query("SELECT COUNT(r), " +
           "SUM(CASE WHEN r.status = :successStatus THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.status = :failureStatus THEN 1 ELSE 0 END), " +
           "COALESCE(SUM(CASE WHEN r.status = :successStatus THEN r.actualRefundAmount ELSE 0 END), 0) " +
           "FROM RefundRecord r WHERE r.operatorId = :operatorId " +
           "AND r.createdAt BETWEEN :startTime AND :endTime")
    Object[] getOperatorStatistics(@Param("operatorId") String operatorId,
                                  @Param("successStatus") RefundStatus successStatus,
                                  @Param("failureStatus") RefundStatus failureStatus,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 查找异常退款记录（金额异常、时间异常等）
     * 
     * @param minAmount 最小异常金额
     * @param maxDurationHours 最大处理时长（小时）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 异常退款记录列表
     */
    @Query("SELECT r FROM RefundRecord r WHERE " +
           "(r.actualRefundAmount > :minAmount OR " +
           "TIMESTAMPDIFF(HOUR, r.createdAt, r.refundTime) > :maxDurationHours) " +
           "AND r.createdAt BETWEEN :startTime AND :endTime")
    List<RefundRecord> findAbnormalRecords(@Param("minAmount") BigDecimal minAmount,
                                          @Param("maxDurationHours") Long maxDurationHours,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 查找指定退款渠道的成功率
     * 
     * @param refundChannel 退款渠道
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 成功率统计 [总数量, 成功数量]
     */
    @Query("SELECT COUNT(r), SUM(CASE WHEN r.status = :successStatus THEN 1 ELSE 0 END) " +
           "FROM RefundRecord r WHERE r.refundChannel = :refundChannel " +
           "AND r.createdAt BETWEEN :startTime AND :endTime")
    Object[] getChannelSuccessRate(@Param("refundChannel") String refundChannel,
                                  @Param("successStatus") RefundStatus successStatus,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 查找退款凭证不为空的记录
     * 
     * @param pageable 分页参数
     * @return 有退款凭证的记录列表
     */
    @Query("SELECT r FROM RefundRecord r WHERE r.refundVoucher IS NOT NULL AND r.refundVoucher != ''")
    Page<RefundRecord> findRecordsWithVoucher(Pageable pageable);

    /**
     * 查找指定金额范围的退款记录
     * 
     * @param minAmount 最小金额
     * @param maxAmount 最大金额
     * @param pageable 分页参数
     * @return 退款记录分页列表
     */
    Page<RefundRecord> findByRefundAmountBetween(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    /**
     * 查找预计到账时间在指定范围内的记录
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 退款记录分页列表
     */
    Page<RefundRecord> findByExpectedArrivalTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
}