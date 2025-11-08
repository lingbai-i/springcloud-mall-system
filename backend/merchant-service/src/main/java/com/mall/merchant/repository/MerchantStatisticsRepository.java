package com.mall.merchant.repository;

import com.mall.merchant.domain.entity.MerchantStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 商家统计数据访问层
 * 提供商家统计相关的数据库操作方法
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Repository
public interface MerchantStatisticsRepository extends JpaRepository<MerchantStatistics, Long> {
    
    /**
     * 根据商家ID、统计日期和统计类型查找统计数据
     * 
     * @param merchantId 商家ID
     * @param statDate 统计日期
     * @param statType 统计类型
     * @return 统计数据
     */
    Optional<MerchantStatistics> findByMerchantIdAndStatDateAndStatType(Long merchantId, LocalDate statDate, Integer statType);
    
    /**
     * 根据商家ID和统计类型查找统计数据列表
     * 
     * @param merchantId 商家ID
     * @param statType 统计类型
     * @param pageable 分页参数
     * @return 统计数据分页列表
     */
    Page<MerchantStatistics> findByMerchantIdAndStatType(Long merchantId, Integer statType, Pageable pageable);
    
    /**
     * 根据商家ID、统计类型和日期范围查找统计数据列表
     * 
     * @param merchantId 商家ID
     * @param statType 统计类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param pageable 分页参数
     * @return 统计数据分页列表
     */
    Page<MerchantStatistics> findByMerchantIdAndStatTypeAndStatDateBetween(Long merchantId, Integer statType, 
                                                                          LocalDate startDate, LocalDate endDate, 
                                                                          Pageable pageable);
    
    /**
     * 根据商家ID、统计类型和日期范围查找统计数据列表（不分页）
     * 
     * @param merchantId 商家ID
     * @param statType 统计类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据列表
     */
    List<MerchantStatistics> findByMerchantIdAndStatTypeAndStatDateBetweenOrderByStatDateAsc(Long merchantId, Integer statType, 
                                                                                            LocalDate startDate, LocalDate endDate);
    
    /**
     * 根据商家ID查找最新的日统计数据
     * 
     * @param merchantId 商家ID
     * @return 最新日统计数据
     */
    @Query("SELECT s FROM MerchantStatistics s WHERE s.merchantId = :merchantId AND s.statType = 1 ORDER BY s.statDate DESC")
    Optional<MerchantStatistics> findLatestDailyStatistics(@Param("merchantId") Long merchantId);
    
    /**
     * 根据商家ID查找最新的月统计数据
     * 
     * @param merchantId 商家ID
     * @return 最新月统计数据
     */
    @Query("SELECT s FROM MerchantStatistics s WHERE s.merchantId = :merchantId AND s.statType = 2 ORDER BY s.statDate DESC")
    Optional<MerchantStatistics> findLatestMonthlyStatistics(@Param("merchantId") Long merchantId);
    
    /**
     * 根据商家ID查找最新的年统计数据
     * 
     * @param merchantId 商家ID
     * @return 最新年统计数据
     */
    @Query("SELECT s FROM MerchantStatistics s WHERE s.merchantId = :merchantId AND s.statType = 3 ORDER BY s.statDate DESC")
    Optional<MerchantStatistics> findLatestYearlyStatistics(@Param("merchantId") Long merchantId);
    
    /**
     * 根据商家ID查找最近N天的日统计数据
     * 
     * @param merchantId 商家ID
     * @param days 天数
     * @return 日统计数据列表
     */
    @Query("SELECT s FROM MerchantStatistics s WHERE s.merchantId = :merchantId AND s.statType = 1 " +
           "AND s.statDate >= :startDate ORDER BY s.statDate DESC")
    List<MerchantStatistics> findRecentDailyStatistics(@Param("merchantId") Long merchantId, 
                                                       @Param("startDate") LocalDate startDate);
    
    /**
     * 根据商家ID查找最近N个月的月统计数据
     * 
     * @param merchantId 商家ID
     * @param startDate 开始日期
     * @return 月统计数据列表
     */
    @Query("SELECT s FROM MerchantStatistics s WHERE s.merchantId = :merchantId AND s.statType = 2 " +
           "AND s.statDate >= :startDate ORDER BY s.statDate DESC")
    List<MerchantStatistics> findRecentMonthlyStatistics(@Param("merchantId") Long merchantId, 
                                                         @Param("startDate") LocalDate startDate);
    
    /**
     * 根据商家ID查找最近N年的年统计数据
     * 
     * @param merchantId 商家ID
     * @param startDate 开始日期
     * @return 年统计数据列表
     */
    @Query("SELECT s FROM MerchantStatistics s WHERE s.merchantId = :merchantId AND s.statType = 3 " +
           "AND s.statDate >= :startDate ORDER BY s.statDate DESC")
    List<MerchantStatistics> findRecentYearlyStatistics(@Param("merchantId") Long merchantId, 
                                                        @Param("startDate") LocalDate startDate);
    
    /**
     * 根据商家ID和统计类型统计记录数量
     * 
     * @param merchantId 商家ID
     * @param statType 统计类型
     * @return 记录数量
     */
    Long countByMerchantIdAndStatType(Long merchantId, Integer statType);
    
    /**
     * 删除指定商家、统计类型和日期之前的统计数据
     * 
     * @param merchantId 商家ID
     * @param statType 统计类型
     * @param beforeDate 指定日期之前的数据将被删除
     * @return 删除的记录数
     */
    @Query("DELETE FROM MerchantStatistics s WHERE s.merchantId = :merchantId AND s.statType = :statType AND s.statDate < :beforeDate")
    int deleteByMerchantIdAndStatTypeAndStatDateBefore(@Param("merchantId") Long merchantId, 
                                                      @Param("statType") Integer statType, 
                                                      @Param("beforeDate") LocalDate beforeDate);
    
    /**
     * 删除指定商家和日期之前的统计数据
     * 
     * @param merchantId 商家ID
     * @param beforeDate 指定日期之前的数据将被删除
     * @return 删除的记录数
     */
    @Query("DELETE FROM MerchantStatistics s WHERE s.merchantId = :merchantId AND s.statDate < :beforeDate")
    int deleteByMerchantIdAndStatDateBefore(@Param("merchantId") Long merchantId, 
                                           @Param("beforeDate") LocalDate beforeDate);
    
    /**
     * 删除指定日期之前的所有统计数据
     * 
     * @param beforeDate 指定日期之前的数据将被删除
     * @return 删除的记录数
     */
    @Query("DELETE FROM MerchantStatistics s WHERE s.statDate < :beforeDate")
    long deleteByStatDateBefore(@Param("beforeDate") LocalDate beforeDate);
    
    /**
     * 根据商家ID查找销售趋势数据（最近30天）
     * 
     * @param merchantId 商家ID
     * @param startDate 开始日期
     * @return 销售趋势数据
     */
    @Query("SELECT s.statDate, s.totalSales, s.totalOrders FROM MerchantStatistics s " +
           "WHERE s.merchantId = :merchantId AND s.statType = 1 AND s.statDate >= :startDate " +
           "ORDER BY s.statDate ASC")
    List<Object[]> findSalesTrend(@Param("merchantId") Long merchantId, @Param("startDate") LocalDate startDate);
    
    /**
     * 根据商家ID查找订单趋势数据（最近30天）
     * 
     * @param merchantId 商家ID
     * @param startDate 开始日期
     * @return 订单趋势数据
     */
    @Query("SELECT s.statDate, s.totalOrders, s.completedOrders, s.cancelledOrders FROM MerchantStatistics s " +
           "WHERE s.merchantId = :merchantId AND s.statType = 1 AND s.statDate >= :startDate " +
           "ORDER BY s.statDate ASC")
    List<Object[]> findOrderTrend(@Param("merchantId") Long merchantId, @Param("startDate") LocalDate startDate);
    
    /**
     * 根据商家ID查找访问量趋势数据（最近30天）
     * 
     * @param merchantId 商家ID
     * @param startDate 开始日期
     * @return 访问量趋势数据
     */
    @Query("SELECT s.statDate, s.pageViews, s.uniqueVisitors FROM MerchantStatistics s " +
           "WHERE s.merchantId = :merchantId AND s.statType = 1 AND s.statDate >= :startDate " +
           "ORDER BY s.statDate ASC")
    List<Object[]> findTrafficTrend(@Param("merchantId") Long merchantId, @Param("startDate") LocalDate startDate);
    
    /**
     * 根据商家ID汇总指定时间范围内的统计数据
     * 
     * @param merchantId 商家ID
     * @param statType 统计类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 汇总统计数据
     */
    @Query("SELECT " +
           "SUM(s.totalOrders) as totalOrders, " +
           "SUM(s.completedOrders) as completedOrders, " +
           "SUM(s.cancelledOrders) as cancelledOrders, " +
           "SUM(s.refundOrders) as refundOrders, " +
           "SUM(s.totalSales) as totalSales, " +
           "SUM(s.actualIncome) as actualIncome, " +
           "SUM(s.refundAmount) as refundAmount, " +
           "SUM(s.productSalesCount) as productSalesCount, " +
           "SUM(s.pageViews) as pageViews, " +
           "SUM(s.uniqueVisitors) as uniqueVisitors, " +
           "AVG(s.conversionRate) as avgConversionRate, " +
           "AVG(s.avgOrderValue) as avgOrderValue, " +
           "AVG(s.refundRate) as avgRefundRate, " +
           "AVG(s.positiveRate) as avgPositiveRate, " +
           "AVG(s.avgRating) as avgRating " +
           "FROM MerchantStatistics s " +
           "WHERE s.merchantId = :merchantId AND s.statType = :statType " +
           "AND s.statDate BETWEEN :startDate AND :endDate")
    Object[] sumStatisticsByDateRange(@Param("merchantId") Long merchantId,
                                     @Param("statType") Integer statType,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);
    
    /**
     * 检查统计数据是否存在
     * 
     * @param merchantId 商家ID
     * @param statDate 统计日期
     * @param statType 统计类型
     * @return 是否存在
     */
    boolean existsByMerchantIdAndStatDateAndStatType(Long merchantId, LocalDate statDate, Integer statType);
    
    /**
     * 查找最早的统计日期
     * 
     * @param merchantId 商家ID
     * @return 最早的统计日期
     */
    @Query("SELECT MIN(s.statDate) FROM MerchantStatistics s WHERE s.merchantId = :merchantId")
    Optional<LocalDate> findEarliestStatDate(@Param("merchantId") Long merchantId);
    
    /**
     * 查找最新的统计日期
     * 
     * @param merchantId 商家ID
     * @return 最新统计日期
     */
    @Query("SELECT MAX(s.statDate) FROM MerchantStatistics s WHERE s.merchantId = :merchantId")
    Optional<LocalDate> findLatestStatDate(@Param("merchantId") Long merchantId);
    
    /**
     * 查找访问趋势数据
     * 
     * @param merchantId 商家ID
     * @param startDate 开始日期
     * @return 访问趋势数据
     */
    @Query("SELECT s.statDate, s.pageViews, s.uniqueVisitors FROM MerchantStatistics s " +
           "WHERE s.merchantId = :merchantId AND s.statType = 1 AND s.statDate >= :startDate " +
           "ORDER BY s.statDate ASC")
    List<Object[]> findVisitTrend(@Param("merchantId") Long merchantId, @Param("startDate") LocalDate startDate);
    
    /**
     * 查找转化率趋势数据
     * 
     * @param merchantId 商家ID
     * @param startDate 开始日期
     * @return 转化率趋势数据
     */
    @Query("SELECT s.statDate, s.conversionRate, s.totalOrders, s.pageViews FROM MerchantStatistics s " +
           "WHERE s.merchantId = :merchantId AND s.statType = 1 AND s.statDate >= :startDate " +
           "ORDER BY s.statDate ASC")
    List<Object[]> findConversionTrend(@Param("merchantId") Long merchantId, @Param("startDate") LocalDate startDate);
    
    /**
     * 查找平均订单价值趋势数据
     * 
     * @param merchantId 商家ID
     * @param startDate 开始日期
     * @return 平均订单价值趋势数据
     */
    @Query("SELECT s.statDate, s.avgOrderValue, s.totalSales, s.totalOrders FROM MerchantStatistics s " +
           "WHERE s.merchantId = :merchantId AND s.statType = 1 AND s.statDate >= :startDate " +
           "ORDER BY s.statDate ASC")
    List<Object[]> findAvgOrderValueTrend(@Param("merchantId") Long merchantId, @Param("startDate") LocalDate startDate);
    
    /**
     * 查找汇总统计数据
     * 
     * @param merchantId 商家ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 汇总统计数据：[总销售额, 总订单数, 实际收入, 退款金额, 商品销售数量, 页面浏览量]
     */
    @Query("SELECT SUM(s.totalSales), SUM(s.totalOrders), SUM(s.actualIncome), SUM(s.refundAmount), " +
           "SUM(s.productSalesCount), SUM(s.pageViews) FROM MerchantStatistics s " +
           "WHERE s.merchantId = :merchantId AND s.statType = 1 AND s.statDate BETWEEN :startDate AND :endDate")
    List<Object[]> findSummaryStatistics(@Param("merchantId") Long merchantId, 
                                        @Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);
}