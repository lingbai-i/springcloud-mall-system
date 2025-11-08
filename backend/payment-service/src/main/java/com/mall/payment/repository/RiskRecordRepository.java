package com.mall.payment.repository;

import com.mall.payment.entity.RiskRecord;
import com.mall.payment.entity.RiskRule;
import com.mall.payment.enums.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 风控记录数据访问接口
 * 提供风控记录的数据库操作方法
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@Repository
public interface RiskRecordRepository extends JpaRepository<RiskRecord, String> {

    /**
     * 根据支付订单ID查询风控记录
     * 
     * @param paymentOrderId 支付订单ID
     * @return 风控记录
     */
    Optional<RiskRecord> findByPaymentOrderId(String paymentOrderId);

    /**
     * 根据业务订单ID查询风控记录
     * 
     * @param businessOrderId 业务订单ID
     * @return 风控记录
     */
    Optional<RiskRecord> findByBusinessOrderId(String businessOrderId);

    /**
     * 根据用户ID分页查询风控记录
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 风控记录分页结果
     */
    Page<RiskRecord> findByUserId(String userId, Pageable pageable);

    /**
     * 根据风控结果分页查询风控记录
     * 
     * @param result 风控结果
     * @param pageable 分页参数
     * @return 风控记录分页结果
     */
    Page<RiskRecord> findByResult(RiskRecord.RiskResult result, Pageable pageable);

    /**
     * 根据风险等级分页查询风控记录
     * 
     * @param riskLevel 风险等级
     * @param pageable 分页参数
     * @return 风控记录分页结果
     */
    Page<RiskRecord> findByRiskLevel(RiskRule.RiskLevel riskLevel, Pageable pageable);

    /**
     * 根据审核状态分页查询风控记录
     * 
     * @param reviewStatus 审核状态
     * @param pageable 分页参数
     * @return 风控记录分页结果
     */
    Page<RiskRecord> findByReviewStatus(RiskRecord.ReviewStatus reviewStatus, Pageable pageable);

    /**
     * 查询待审核的风控记录
     * 
     * @param pageable 分页参数
     * @return 待审核的风控记录分页结果
     */
    @Query("SELECT r FROM RiskRecord r WHERE r.result = 'MANUAL_REVIEW' AND r.reviewStatus = 'PENDING' ORDER BY r.createdAt ASC")
    Page<RiskRecord> findPendingReviewRecords(Pageable pageable);

    /**
     * 根据用户ID和时间范围查询风控记录数量
     * 
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 风控记录数量
     */
    @Query("SELECT COUNT(r) FROM RiskRecord r WHERE r.userId = :userId AND r.createdAt BETWEEN :startTime AND :endTime")
    long countByUserIdAndCreatedAtBetween(@Param("userId") String userId, 
                                         @Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 根据用户ID和时间范围查询支付金额总和
     * 
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 支付金额总和
     */
    @Query("SELECT COALESCE(SUM(r.paymentAmount), 0) FROM RiskRecord r WHERE r.userId = :userId AND r.createdAt BETWEEN :startTime AND :endTime")
    BigDecimal sumPaymentAmountByUserIdAndCreatedAtBetween(@Param("userId") String userId, 
                                                          @Param("startTime") LocalDateTime startTime, 
                                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 根据客户端IP和时间范围查询风控记录数量
     * 
     * @param clientIp 客户端IP
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 风控记录数量
     */
    @Query("SELECT COUNT(r) FROM RiskRecord r WHERE r.clientIp = :clientIp AND r.createdAt BETWEEN :startTime AND :endTime")
    long countByClientIpAndCreatedAtBetween(@Param("clientIp") String clientIp, 
                                           @Param("startTime") LocalDateTime startTime, 
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 根据设备指纹和时间范围查询风控记录数量
     * 
     * @param deviceFingerprint 设备指纹
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 风控记录数量
     */
    @Query("SELECT COUNT(r) FROM RiskRecord r WHERE r.deviceFingerprint = :deviceFingerprint AND r.createdAt BETWEEN :startTime AND :endTime")
    long countByDeviceFingerprintAndCreatedAtBetween(@Param("deviceFingerprint") String deviceFingerprint, 
                                                    @Param("startTime") LocalDateTime startTime, 
                                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 根据支付方式和时间范围统计风控记录
     * 
     * @param paymentMethod 支付方式
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 风控记录数量
     */
    @Query("SELECT COUNT(r) FROM RiskRecord r WHERE r.paymentMethod = :paymentMethod AND r.createdAt BETWEEN :startTime AND :endTime")
    long countByPaymentMethodAndCreatedAtBetween(@Param("paymentMethod") PaymentMethod paymentMethod, 
                                                @Param("startTime") LocalDateTime startTime, 
                                                @Param("endTime") LocalDateTime endTime);

    /**
     * 根据风控结果和时间范围统计风控记录
     * 
     * @param result 风控结果
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 风控记录数量
     */
    @Query("SELECT COUNT(r) FROM RiskRecord r WHERE r.result = :result AND r.createdAt BETWEEN :startTime AND :endTime")
    long countByResultAndCreatedAtBetween(@Param("result") RiskRecord.RiskResult result, 
                                         @Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 根据风险等级和时间范围统计风控记录
     * 
     * @param riskLevel 风险等级
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 风控记录数量
     */
    @Query("SELECT COUNT(r) FROM RiskRecord r WHERE r.riskLevel = :riskLevel AND r.createdAt BETWEEN :startTime AND :endTime")
    long countByRiskLevelAndCreatedAtBetween(@Param("riskLevel") RiskRule.RiskLevel riskLevel, 
                                            @Param("startTime") LocalDateTime startTime, 
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询高风险交易记录
     * 
     * @param minRiskScore 最小风险评分
     * @param pageable 分页参数
     * @return 高风险交易记录分页结果
     */
    @Query("SELECT r FROM RiskRecord r WHERE r.riskScore >= :minRiskScore ORDER BY r.riskScore DESC, r.createdAt DESC")
    Page<RiskRecord> findHighRiskRecords(@Param("minRiskScore") BigDecimal minRiskScore, Pageable pageable);

    /**
     * 查询误报记录
     * 
     * @param pageable 分页参数
     * @return 误报记录分页结果
     */
    Page<RiskRecord> findByIsFalsePositive(Boolean isFalsePositive, Pageable pageable);

    /**
     * 根据触发规则查询风控记录
     * 
     * @param ruleId 规则ID
     * @param pageable 分页参数
     * @return 风控记录分页结果
     */
    @Query("SELECT r FROM RiskRecord r WHERE r.triggeredRules LIKE %:ruleId% ORDER BY r.createdAt DESC")
    Page<RiskRecord> findByTriggeredRulesContaining(@Param("ruleId") String ruleId, Pageable pageable);

    /**
     * 统计时间范围内的风控记录总数
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 风控记录总数
     */
    @Query("SELECT COUNT(r) FROM RiskRecord r WHERE r.createdAt BETWEEN :startTime AND :endTime")
    long countByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计时间范围内的平均风险评分
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 平均风险评分
     */
    @Query("SELECT COALESCE(AVG(r.riskScore), 0) FROM RiskRecord r WHERE r.createdAt BETWEEN :startTime AND :endTime")
    BigDecimal avgRiskScoreByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计时间范围内的最高风险评分
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 最高风险评分
     */
    @Query("SELECT COALESCE(MAX(r.riskScore), 0) FROM RiskRecord r WHERE r.createdAt BETWEEN :startTime AND :endTime")
    BigDecimal maxRiskScoreByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询处理时间最长的风控记录
     * 
     * @param pageable 分页参数
     * @return 处理时间最长的风控记录
     */
    @Query("SELECT r FROM RiskRecord r WHERE r.processingTimeMs IS NOT NULL ORDER BY r.processingTimeMs DESC")
    Page<RiskRecord> findByProcessingTimeDesc(Pageable pageable);

    /**
     * 统计平均处理时间
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 平均处理时间（毫秒）
     */
    @Query("SELECT COALESCE(AVG(r.processingTimeMs), 0) FROM RiskRecord r WHERE r.createdAt BETWEEN :startTime AND :endTime AND r.processingTimeMs IS NOT NULL")
    Double avgProcessingTimeByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据多个条件查询风控记录
     * 
     * @param userId 用户ID（可选）
     * @param paymentMethod 支付方式（可选）
     * @param result 风控结果（可选）
     * @param riskLevel 风险等级（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param pageable 分页参数
     * @return 风控记录分页结果
     */
    @Query("SELECT r FROM RiskRecord r WHERE " +
           "(:userId IS NULL OR r.userId = :userId) AND " +
           "(:paymentMethod IS NULL OR r.paymentMethod = :paymentMethod) AND " +
           "(:result IS NULL OR r.result = :result) AND " +
           "(:riskLevel IS NULL OR r.riskLevel = :riskLevel) AND " +
           "(:startTime IS NULL OR r.createdAt >= :startTime) AND " +
           "(:endTime IS NULL OR r.createdAt <= :endTime) " +
           "ORDER BY r.createdAt DESC")
    Page<RiskRecord> findByMultipleConditions(@Param("userId") String userId,
                                             @Param("paymentMethod") PaymentMethod paymentMethod,
                                             @Param("result") RiskRecord.RiskResult result,
                                             @Param("riskLevel") RiskRule.RiskLevel riskLevel,
                                             @Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime,
                                             Pageable pageable);

    /**
     * 统计指定时间范围内误报记录数量
     * 
     * @param isFalsePositive 是否误报
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 误报记录数量
     */
    Long countByIsFalsePositiveAndCreatedAtBetween(Boolean isFalsePositive, 
                                                  LocalDateTime startTime, 
                                                  LocalDateTime endTime);
}