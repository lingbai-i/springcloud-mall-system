package com.mall.payment.repository;

import com.mall.payment.entity.PaymentOrder;
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
 * 支付订单数据访问层接口
 * 提供支付订单相关的数据库操作方法，基于Spring Data JPA实现
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>基础CRUD操作：继承JpaRepository提供标准增删改查</li>
 *   <li>条件查询：继承JpaSpecificationExecutor支持动态条件查询</li>
 *   <li>业务查询：根据业务需求定义的特定查询方法</li>
 *   <li>分页查询：支持分页和排序功能</li>
 *   <li>统计查询：提供数据统计和聚合查询</li>
 * </ul>
 * 
 * <p>查询方法分类：</p>
 * <ul>
 *   <li>单条查询：根据订单ID、第三方订单号等唯一标识查询</li>
 *   <li>列表查询：根据用户ID、状态等条件查询订单列表</li>
 *   <li>分页查询：支持用户订单、状态筛选的分页查询</li>
 *   <li>统计查询：按时间、状态、支付方式等维度统计</li>
 *   <li>自定义查询：使用@Query注解定义复杂查询</li>
 * </ul>
 * 
 * <p>性能优化：</p>
 * <ul>
 *   <li>索引优化：在常用查询字段上建立索引</li>
 *   <li>懒加载：关联对象使用懒加载策略</li>
 *   <li>批量操作：支持批量插入和更新</li>
 *   <li>缓存策略：热点数据使用Redis缓存</li>
 * </ul>
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * 修改日志：
 * V1.2 2025-11-01：完善Javadoc注释，增加功能分类和性能优化说明
 * V1.1 2024-12-20：增加统计查询和自定义查询方法
 * V1.0 2024-12-01：初始版本，定义基础查询方法
 */
@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, String>, JpaSpecificationExecutor<PaymentOrder> {

    /**
     * 根据业务订单ID查找支付订单
     * 
     * @param orderId 业务订单ID
     * @return 支付订单
     */
    Optional<PaymentOrder> findByOrderId(String orderId);

    /**
     * 根据第三方支付订单号查找支付订单
     * 
     * @param thirdPartyOrderNo 第三方支付订单号
     * @return 支付订单
     */
    Optional<PaymentOrder> findByThirdPartyOrderNo(String thirdPartyOrderNo);

    /**
     * 根据用户ID查找支付订单列表
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 支付订单分页列表
     */
    Page<PaymentOrder> findByUserId(String userId, Pageable pageable);

    /**
     * 根据用户ID和支付状态查找支付订单列表
     * 
     * @param userId 用户ID
     * @param status 支付状态
     * @param pageable 分页参数
     * @return 支付订单分页列表
     */
    Page<PaymentOrder> findByUserIdAndStatus(String userId, PaymentStatus status, Pageable pageable);

    /**
     * 根据支付状态查找支付订单列表
     * 
     * @param status 支付状态
     * @param pageable 分页参数
     * @return 支付订单分页列表
     */
    Page<PaymentOrder> findByStatus(PaymentStatus status, Pageable pageable);

    /**
     * 根据支付方式查找支付订单列表
     * 
     * @param paymentMethod 支付方式
     * @param pageable 分页参数
     * @return 支付订单分页列表
     */
    Page<PaymentOrder> findByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);

    /**
     * 根据创建时间范围分页查询支付订单
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 支付订单分页结果
     */
    Page<PaymentOrder> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 根据支付完成时间范围分页查询支付订单
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 支付订单分页结果
     */
    Page<PaymentOrder> findByPayTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 查找已过期的待支付订单
     * 
     * @param currentTime 当前时间
     * @return 已过期的支付订单列表
     */
    @Query("SELECT p FROM PaymentOrder p WHERE p.status = :status AND p.expireTime < :currentTime")
    List<PaymentOrder> findExpiredOrders(@Param("status") PaymentStatus status, @Param("currentTime") LocalDateTime currentTime);

    /**
     * 查找需要重试的失败订单
     * 
     * @param maxRetryCount 最大重试次数
     * @param retryInterval 重试间隔（分钟）
     * @return 需要重试的支付订单列表
     */
    @Query("SELECT p FROM PaymentOrder p WHERE p.status = :status AND p.retryCount < :maxRetryCount " +
           "AND p.updatedAt < :retryTime")
    List<PaymentOrder> findOrdersNeedRetry(@Param("status") PaymentStatus status, 
                                          @Param("maxRetryCount") Integer maxRetryCount,
                                          @Param("retryTime") LocalDateTime retryTime);

    /**
     * 统计指定时间范围内的订单数量
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 订单数量
     */
    @Query("SELECT COUNT(p) FROM PaymentOrder p WHERE p.createdAt BETWEEN :startTime AND :endTime")
    Long countByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内指定状态的订单数量
     * 
     * @param status 支付状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 订单数量
     */
    @Query("SELECT COUNT(p) FROM PaymentOrder p WHERE p.status = :status AND p.createdAt BETWEEN :startTime AND :endTime")
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
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentOrder p WHERE p.createdAt BETWEEN :startTime AND :endTime")
    BigDecimal sumAmountByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内指定状态的支付金额
     * 
     * @param status 支付状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 支付金额总和
     */
    @Query("SELECT COALESCE(SUM(p.actualAmount), 0) FROM PaymentOrder p WHERE p.status = :status " +
           "AND p.payTime BETWEEN :startTime AND :endTime")
    BigDecimal sumActualAmountByStatusAndPayTimeBetween(@Param("status") PaymentStatus status,
                                                       @Param("startTime") LocalDateTime startTime, 
                                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各支付方式的订单数量
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 支付方式统计结果
     */
    @Query("SELECT p.paymentMethod, COUNT(p), COALESCE(SUM(p.actualAmount), 0) FROM PaymentOrder p " +
           "WHERE p.status = :status AND p.payTime BETWEEN :startTime AND :endTime " +
           "GROUP BY p.paymentMethod")
    List<Object[]> countByPaymentMethodAndStatusAndPayTimeBetween(@Param("status") PaymentStatus status,
                                                                 @Param("startTime") LocalDateTime startTime, 
                                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 统计每日支付数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 每日统计结果
     */
    @Query("SELECT DATE(p.payTime), COUNT(p), COALESCE(SUM(p.actualAmount), 0), COALESCE(SUM(p.feeAmount), 0) " +
           "FROM PaymentOrder p WHERE p.status = :status AND p.payTime BETWEEN :startTime AND :endTime " +
           "GROUP BY DATE(p.payTime) ORDER BY DATE(p.payTime)")
    List<Object[]> getDailyStatistics(@Param("status") PaymentStatus status,
                                     @Param("startTime") LocalDateTime startTime, 
                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 统计每小时支付数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 每小时统计结果
     */
    @Query("SELECT HOUR(p.payTime), COUNT(p), COALESCE(SUM(p.actualAmount), 0) " +
           "FROM PaymentOrder p WHERE p.status = :status AND p.payTime BETWEEN :startTime AND :endTime " +
           "GROUP BY HOUR(p.payTime) ORDER BY HOUR(p.payTime)")
    List<Object[]> getHourlyStatistics(@Param("status") PaymentStatus status,
                                      @Param("startTime") LocalDateTime startTime, 
                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 批量更新过期订单状态
     * 
     * @param oldStatus 原状态
     * @param newStatus 新状态
     * @param currentTime 当前时间
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE PaymentOrder p SET p.status = :newStatus, p.updatedAt = :currentTime " +
           "WHERE p.status = :oldStatus AND p.expireTime < :currentTime")
    int updateExpiredOrdersStatus(@Param("oldStatus") PaymentStatus oldStatus,
                                 @Param("newStatus") PaymentStatus newStatus,
                                 @Param("currentTime") LocalDateTime currentTime);

    /**
     * 更新支付订单状态和相关信息
     * 
     * @param orderId 订单ID
     * @param status 新状态
     * @param thirdPartyOrderNo 第三方订单号
     * @param actualAmount 实际支付金额
     * @param fee 手续费
     * @param paymentCompletedTime 支付完成时间
     * @param updatedTime 更新时间
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE PaymentOrder p SET p.status = :status, p.thirdPartyOrderNo = :thirdPartyOrderNo, " +
           "p.actualAmount = :actualAmount, p.feeAmount = :fee, p.payTime = :paymentCompletedTime, " +
           "p.updatedAt = :updatedTime WHERE p.id = :orderId")
    int updatePaymentResult(@Param("orderId") String orderId,
                           @Param("status") PaymentStatus status,
                           @Param("thirdPartyOrderNo") String thirdPartyOrderNo,
                           @Param("actualAmount") BigDecimal actualAmount,
                           @Param("fee") BigDecimal fee,
                           @Param("paymentCompletedTime") LocalDateTime paymentCompletedTime,
                           @Param("updatedTime") LocalDateTime updatedTime);

    /**
     * 增加重试次数
     * 
     * @param orderId 订单ID
     * @param updatedTime 更新时间
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE PaymentOrder p SET p.retryCount = p.retryCount + 1, p.updatedAt = :updatedTime " +
           "WHERE p.id = :orderId")
    int incrementRetryCount(@Param("orderId") String orderId, @Param("updatedTime") LocalDateTime updatedTime);

    /**
     * 根据用户ID和时间范围查找支付订单
     * 
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 支付订单分页列表
     */
    Page<PaymentOrder> findByUserIdAndCreatedAtBetween(String userId, LocalDateTime startTime, 
                                                      LocalDateTime endTime, Pageable pageable);

    /**
     * 检查业务订单是否已存在支付订单
     * 
     * @param orderId 业务订单ID
     * @return 是否存在
     */
    boolean existsByOrderId(String orderId);

    /**
     * 检查第三方订单号是否已存在
     * 
     * @param thirdPartyOrderNo 第三方订单号
     * @return 是否存在
     */
    boolean existsByThirdPartyOrderNo(String thirdPartyOrderNo);

    /**
     * 查找用户最近的支付订单
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 最近的支付订单列表
     */
    @Query("SELECT p FROM PaymentOrder p WHERE p.userId = :userId ORDER BY p.createdAt DESC")
    List<PaymentOrder> findRecentOrdersByUserId(@Param("userId") String userId, Pageable pageable);

    /**
     * 查找指定金额范围的订单
     * 
     * @param minAmount 最小金额
     * @param maxAmount 最大金额
     * @param pageable 分页参数
     * @return 支付订单分页列表
     */
    Page<PaymentOrder> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    /**
     * 查找失败状态且可重试的支付订单
     * 用于定时任务重试失败的支付订单
     * 
     * @param cutoffTime 截止时间（创建时间在此时间之后的订单）
     * @return 失败且可重试的支付订单列表
     */
    @Query("SELECT p FROM PaymentOrder p WHERE p.status = 'FAILED' AND p.createdAt >= :cutoffTime " +
           "AND p.retryCount < 3 ORDER BY p.createdAt ASC")
    List<PaymentOrder> findFailedOrdersForRetry(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 查找处理中状态的支付订单用于状态同步
     * 用于定时任务同步第三方支付平台的订单状态
     * 
     * @param cutoffTime 截止时间（创建时间在此时间之后的订单）
     * @return 处理中的支付订单列表
     */
    @Query("SELECT p FROM PaymentOrder p WHERE p.status = 'PROCESSING' AND p.createdAt >= :cutoffTime " +
           "AND p.thirdPartyOrderNo IS NOT NULL ORDER BY p.createdAt ASC")
    List<PaymentOrder> findProcessingOrdersForSync(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 统计指定时间范围内不同用户数量
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户数量
     */
    @Query("SELECT COUNT(DISTINCT p.userId) FROM PaymentOrder p WHERE p.createdAt BETWEEN :startTime AND :endTime")
    Long countDistinctByUserIdAndCreatedAtBetween(@Param("startTime") LocalDateTime startTime, 
                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围和状态下的不同用户数量
     * 
     * @param status 支付状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户数量
     */
    @Query("SELECT COUNT(DISTINCT p.userId) FROM PaymentOrder p WHERE p.status = :status " +
           "AND p.createdAt BETWEEN :startTime AND :endTime")
    Long countDistinctByUserIdAndStatusAndCreatedAtBetween(@Param("status") PaymentStatus status,
                                                          @Param("startTime") LocalDateTime startTime, 
                                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 计算指定时间范围和状态下的平均支付金额
     * 
     * @param status 支付状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 平均支付金额
     */
    @Query("SELECT AVG(p.actualAmount) FROM PaymentOrder p WHERE p.status = :status " +
           "AND p.createdAt BETWEEN :startTime AND :endTime")
    BigDecimal findAvgPaymentAmountByStatusAndCreatedAtBetween(@Param("status") PaymentStatus status,
                                                              @Param("startTime") LocalDateTime startTime, 
                                                              @Param("endTime") LocalDateTime endTime);



    /**
     * 查找指定时间范围内的支付订单（按创建时间）
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 支付订单列表
     */
    List<PaymentOrder> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查找指定时间范围内的不重复用户ID列表
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户ID列表
     */
    @Query("SELECT DISTINCT p.userId FROM PaymentOrder p WHERE p.createdAt BETWEEN :startTime AND :endTime")
    List<String> findDistinctUserIdByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, 
                                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定用户在指定时间之前的订单数量
     * 
     * @param userId 用户ID
     * @param beforeTime 指定时间
     * @return 订单数量
     */
    @Query("SELECT COUNT(p) FROM PaymentOrder p WHERE p.userId = :userId AND p.createdAt < :beforeTime")
    Long countByUserIdAndCreatedAtBefore(@Param("userId") String userId, @Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 根据ID和删除状态查找支付订单
     * 
     * @param id 订单ID
     * @param deleted 删除状态
     * @return 支付订单
     */
    Optional<PaymentOrder> findByIdAndDeleted(String id, Boolean deleted);

    /**
     * 根据ID和删除状态查找支付订单（未删除）
     * 
     * @param id 订单ID
     * @return 支付订单
     */
    default Optional<PaymentOrder> findByIdAndDeletedFalse(String id) {
        return findByIdAndDeleted(id, false);
    }

    /**
     * 根据状态和创建时间查找支付订单（早于指定时间）
     * 
     * @param status 支付状态
     * @param cutoffTime 截止时间
     * @return 支付订单列表
     */
    List<PaymentOrder> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime cutoffTime);

    /**
     * 根据订单号和删除状态查找支付订单
     * 
     * @param orderId 订单号
     * @param deleted 删除状态
     * @return 支付订单
     */
    Optional<PaymentOrder> findByOrderIdAndDeleted(String orderId, Boolean deleted);

    /**
     * 根据订单号和删除状态查找支付订单（未删除）
     * 
     * @param orderId 订单号
     * @return 支付订单
     */
    default Optional<PaymentOrder> findByOrderIdAndDeletedFalse(String orderId) {
        return findByOrderIdAndDeleted(orderId, false);
    }

    /**
     * 根据用户ID和删除状态查找支付订单列表
     * 
     * @param userId 用户ID
     * @param deleted 删除状态
     * @param pageable 分页参数
     * @return 支付订单分页列表
     */
    Page<PaymentOrder> findByUserIdAndDeleted(String userId, Boolean deleted, Pageable pageable);

    /**
     * 根据用户ID和删除状态查找支付订单列表（未删除）
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 支付订单分页列表
     */
    default Page<PaymentOrder> findByUserIdAndDeletedFalse(String userId, Pageable pageable) {
        return findByUserIdAndDeleted(userId, false, pageable);
    }
}