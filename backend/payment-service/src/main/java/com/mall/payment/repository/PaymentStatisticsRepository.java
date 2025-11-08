package com.mall.payment.repository;

import com.mall.payment.entity.PaymentStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 支付统计数据访问接口
 * 提供支付统计相关的数据库操作方法，支持多维度数据分析
 * 
 * <p>统计维度：</p>
 * <ul>
 *   <li>时间维度：按日、周、月、年统计支付数据</li>
 *   <li>支付方式：按支付宝、微信、银行卡等方式统计</li>
 *   <li>状态维度：按成功、失败、待支付等状态统计</li>
 *   <li>金额维度：统计交易金额、手续费、退款金额等</li>
 *   <li>用户维度：按用户群体、地区等维度分析</li>
 * </ul>
 * 
 * <p>统计类型：</p>
 * <ul>
 *   <li>DAILY：日统计数据</li>
 *   <li>WEEKLY：周统计数据</li>
 *   <li>MONTHLY：月统计数据</li>
 *   <li>YEARLY：年统计数据</li>
 *   <li>REAL_TIME：实时统计数据</li>
 * </ul>
 * 
 * <p>应用场景：</p>
 * <ul>
 *   <li>运营报表：生成日常运营数据报表</li>
 *   <li>趋势分析：分析支付数据变化趋势</li>
 *   <li>异常监控：监控支付成功率和异常情况</li>
 *   <li>决策支持：为业务决策提供数据支撑</li>
 *   <li>对账核查：与财务系统进行数据核对</li>
 * </ul>
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * 修改日志：
 * V1.2 2025-11-01：完善Javadoc注释，增加统计维度和应用场景说明
 * V1.1 2024-12-25：增加实时统计和趋势分析功能
 * V1.0 2024-12-01：初始版本，定义基础统计查询方法
 */
@Repository
public interface PaymentStatisticsRepository extends JpaRepository<PaymentStatistics, String> {

    // ==================== 基础查询方法 ====================

    /**
     * 根据统计日期、类型和支付方式查询统计记录
     * 
     * @param statDate 统计日期
     * @param statType 统计类型
     * @param paymentMethod 支付方式（可为null）
     * @return 统计记录
     */
    Optional<PaymentStatistics> findByStatDateAndStatTypeAndPaymentMethod(
            LocalDate statDate, PaymentStatistics.StatType statType, String paymentMethod);

    /**
     * 根据统计日期、统计类型查找全局统计数据（不区分支付方式）
     * 
     * @param statDate 统计日期
     * @param statType 统计类型
     * @return 统计数据，如果不存在则返回空
     */
    Optional<PaymentStatistics> findByStatDateAndStatTypeAndPaymentMethodIsNull(
            LocalDate statDate, PaymentStatistics.StatType statType);

    /**
     * 根据统计类型查询统计记录列表
     * 
     * @param statType 统计类型
     * @param pageable 分页参数
     * @return 统计记录分页结果
     */
    Page<PaymentStatistics> findByStatType(PaymentStatistics.StatType statType, Pageable pageable);

    /**
     * 根据统计类型和支付方式查询统计记录列表
     * 
     * @param statType 统计类型
     * @param paymentMethod 支付方式
     * @param pageable 分页参数
     * @return 统计记录分页结果
     */
    Page<PaymentStatistics> findByStatTypeAndPaymentMethod(
            PaymentStatistics.StatType statType, String paymentMethod, Pageable pageable);

    /**
     * 根据日期范围和统计类型查询统计记录列表
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statType 统计类型
     * @return 统计记录列表
     */
    List<PaymentStatistics> findByStatDateBetweenAndStatTypeOrderByStatDateAsc(
            LocalDate startDate, LocalDate endDate, PaymentStatistics.StatType statType);

    /**
     * 根据日期范围、统计类型和支付方式查询统计记录列表
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statType 统计类型
     * @param paymentMethod 支付方式
     * @return 统计记录列表
     */
    List<PaymentStatistics> findByStatDateBetweenAndStatTypeAndPaymentMethodOrderByStatDateAsc(
            LocalDate startDate, LocalDate endDate, PaymentStatistics.StatType statType, String paymentMethod);

    // ==================== 统计查询方法 ====================

    /**
     * 查询指定日期范围内的总交易金额
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statType 统计类型
     * @return 总交易金额
     */
    @Query("SELECT COALESCE(SUM(ps.totalAmount), 0) FROM PaymentStatistics ps " +
           "WHERE ps.statDate BETWEEN :startDate AND :endDate " +
           "AND ps.statType = :statType " +
           "AND ps.paymentMethod IS NULL")
    BigDecimal sumTotalAmountByDateRangeAndStatType(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statType") PaymentStatistics.StatType statType);

    /**
     * 查询指定日期范围内的成功交易金额
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statType 统计类型
     * @return 成功交易金额
     */
    @Query("SELECT COALESCE(SUM(ps.successAmount), 0) FROM PaymentStatistics ps " +
           "WHERE ps.statDate BETWEEN :startDate AND :endDate " +
           "AND ps.statType = :statType " +
           "AND ps.paymentMethod IS NULL")
    BigDecimal sumSuccessAmountByDateRangeAndStatType(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statType") PaymentStatistics.StatType statType);

    /**
     * 查询指定日期范围内的总订单数
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statType 统计类型
     * @return 总订单数
     */
    @Query("SELECT COALESCE(SUM(ps.totalOrders), 0) FROM PaymentStatistics ps " +
           "WHERE ps.statDate BETWEEN :startDate AND :endDate " +
           "AND ps.statType = :statType " +
           "AND ps.paymentMethod IS NULL")
    Long sumTotalOrdersByDateRangeAndStatType(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statType") PaymentStatistics.StatType statType);

    /**
     * 查询指定日期范围内的成功订单数
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statType 统计类型
     * @return 成功订单数
     */
    @Query("SELECT COALESCE(SUM(ps.successOrders), 0) FROM PaymentStatistics ps " +
           "WHERE ps.statDate BETWEEN :startDate AND :endDate " +
           "AND ps.statType = :statType " +
           "AND ps.paymentMethod IS NULL")
    Long sumSuccessOrdersByDateRangeAndStatType(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statType") PaymentStatistics.StatType statType);

    /**
     * 查询指定日期范围内的退款金额
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statType 统计类型
     * @return 退款金额
     */
    @Query("SELECT COALESCE(SUM(ps.refundAmount), 0) FROM PaymentStatistics ps " +
           "WHERE ps.statDate BETWEEN :startDate AND :endDate " +
           "AND ps.statType = :statType " +
           "AND ps.paymentMethod IS NULL")
    BigDecimal sumRefundAmountByDateRangeAndStatType(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statType") PaymentStatistics.StatType statType);

    /**
     * 查询指定日期范围内的手续费金额
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statType 统计类型
     * @return 手续费金额
     */
    @Query("SELECT COALESCE(SUM(ps.feeAmount), 0) FROM PaymentStatistics ps " +
           "WHERE ps.statDate BETWEEN :startDate AND :endDate " +
           "AND ps.statType = :statType " +
           "AND ps.paymentMethod IS NULL")
    BigDecimal sumFeeAmountByDateRangeAndStatType(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statType") PaymentStatistics.StatType statType);

    // ==================== 支付方式统计查询 ====================

    /**
     * 查询指定日期的各支付方式统计数据
     * 
     * @param statDate 统计日期
     * @param statType 统计类型
     * @return 各支付方式统计数据列表
     */
    @Query("SELECT ps FROM PaymentStatistics ps " +
           "WHERE ps.statDate = :statDate " +
           "AND ps.statType = :statType " +
           "AND ps.paymentMethod IS NOT NULL " +
           "ORDER BY ps.successAmount DESC")
    List<PaymentStatistics> findPaymentMethodStatsByDate(
            @Param("statDate") LocalDate statDate,
            @Param("statType") PaymentStatistics.StatType statType);

    /**
     * 查询指定日期范围内各支付方式的交易金额排名
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statType 统计类型
     * @return 支付方式交易金额排名
     */
    @Query("SELECT ps.paymentMethod, SUM(ps.successAmount) as totalAmount " +
           "FROM PaymentStatistics ps " +
           "WHERE ps.statDate BETWEEN :startDate AND :endDate " +
           "AND ps.statType = :statType " +
           "AND ps.paymentMethod IS NOT NULL " +
           "GROUP BY ps.paymentMethod " +
           "ORDER BY totalAmount DESC")
    List<Object[]> findPaymentMethodRankingByAmount(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statType") PaymentStatistics.StatType statType);

    /**
     * 查询指定日期范围内各支付方式的订单数排名
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statType 统计类型
     * @return 支付方式订单数排名
     */
    @Query("SELECT ps.paymentMethod, SUM(ps.successOrders) as totalOrders " +
           "FROM PaymentStatistics ps " +
           "WHERE ps.statDate BETWEEN :startDate AND :endDate " +
           "AND ps.statType = :statType " +
           "AND ps.paymentMethod IS NOT NULL " +
           "GROUP BY ps.paymentMethod " +
           "ORDER BY totalOrders DESC")
    List<Object[]> findPaymentMethodRankingByOrders(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statType") PaymentStatistics.StatType statType);

    // ==================== 趋势分析查询 ====================

    /**
     * 查询最近N天的交易趋势
     * 
     * @param days 天数
     * @return 交易趋势数据
     */
    @Query("SELECT ps.statDate, ps.totalOrders, ps.successOrders, ps.totalAmount, ps.successAmount " +
           "FROM PaymentStatistics ps " +
           "WHERE ps.statDate >= :startDate " +
           "AND ps.statType = 'DAILY' " +
           "AND ps.paymentMethod IS NULL " +
           "ORDER BY ps.statDate ASC")
    List<Object[]> findRecentTrend(@Param("startDate") LocalDate startDate);

    /**
     * 查询最近N个月的交易趋势
     * 
     * @param months 月数
     * @return 交易趋势数据
     */
    @Query("SELECT ps.statDate, ps.totalOrders, ps.successOrders, ps.totalAmount, ps.successAmount " +
           "FROM PaymentStatistics ps " +
           "WHERE ps.statDate >= :startDate " +
           "AND ps.statType = 'MONTHLY' " +
           "AND ps.paymentMethod IS NULL " +
           "ORDER BY ps.statDate ASC")
    List<Object[]> findMonthlyTrend(@Param("startDate") LocalDate startDate);

    // ==================== 数据清理方法 ====================

    /**
     * 删除指定日期之前的统计数据
     * 
     * @param beforeDate 指定日期
     * @param statType 统计类型
     * @return 删除的记录数
     */
    @Query("DELETE FROM PaymentStatistics ps " +
           "WHERE ps.statDate < :beforeDate " +
           "AND ps.statType = :statType")
    int deleteByStatDateBeforeAndStatType(
            @Param("beforeDate") LocalDate beforeDate,
            @Param("statType") PaymentStatistics.StatType statType);

    /**
     * 统计指定统计类型的记录数
     * 
     * @param statType 统计类型
     * @return 记录数
     */
    long countByStatType(PaymentStatistics.StatType statType);

    /**
     * 查询最新的统计日期
     * 
     * @param statType 统计类型
     * @return 最新统计日期
     */
    @Query("SELECT MAX(ps.statDate) FROM PaymentStatistics ps WHERE ps.statType = :statType")
    Optional<LocalDate> findLatestStatDate(@Param("statType") PaymentStatistics.StatType statType);

    /**
     * 查询最早的统计日期
     * 
     * @param statType 统计类型
     * @return 最早统计日期
     */
    @Query("SELECT MIN(ps.statDate) FROM PaymentStatistics ps WHERE ps.statType = :statType")
    Optional<LocalDate> findEarliestStatDate(@Param("statType") PaymentStatistics.StatType statType);
}