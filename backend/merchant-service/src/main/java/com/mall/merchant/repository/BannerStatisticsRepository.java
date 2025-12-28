package com.mall.merchant.repository;

import com.mall.merchant.domain.entity.BannerStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 轮播图统计数据访问层
 * 提供轮播图统计相关的数据库操作方法
 * 
 * @author system
 * @version 1.0
 * @since 2025-12-28
 */
@Repository
public interface BannerStatisticsRepository extends JpaRepository<BannerStatistics, Long> {

    /**
     * 根据轮播图ID和日期查找统计数据
     * 
     * @param bannerId 轮播图ID
     * @param statDate 统计日期
     * @return 统计数据
     */
    Optional<BannerStatistics> findByBannerIdAndStatDate(Long bannerId, LocalDate statDate);

    /**
     * 根据轮播图ID查找所有统计数据
     * 
     * @param bannerId 轮播图ID
     * @return 统计数据列表
     */
    List<BannerStatistics> findByBannerIdOrderByStatDateDesc(Long bannerId);

    /**
     * 根据轮播图ID和日期范围查找统计数据
     * 
     * @param bannerId 轮播图ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据列表
     */
    List<BannerStatistics> findByBannerIdAndStatDateBetweenOrderByStatDateAsc(
            Long bannerId, LocalDate startDate, LocalDate endDate);

    /**
     * 汇总轮播图的总统计数据
     * 
     * @param bannerId 轮播图ID
     * @return [总曝光量, 总点击量, 独立曝光数, 独立点击数]
     */
    @Query("SELECT COALESCE(SUM(s.impressions), 0), COALESCE(SUM(s.clicks), 0), " +
           "COALESCE(SUM(s.uniqueImpressions), 0), COALESCE(SUM(s.uniqueClicks), 0) " +
           "FROM BannerStatistics s WHERE s.bannerId = :bannerId")
    List<Object[]> sumStatisticsByBannerId(@Param("bannerId") Long bannerId);

    /**
     * 汇总轮播图指定日期范围的统计数据
     * 
     * @param bannerId 轮播图ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return [总曝光量, 总点击量, 独立曝光数, 独立点击数]
     */
    @Query("SELECT COALESCE(SUM(s.impressions), 0), COALESCE(SUM(s.clicks), 0), " +
           "COALESCE(SUM(s.uniqueImpressions), 0), COALESCE(SUM(s.uniqueClicks), 0) " +
           "FROM BannerStatistics s WHERE s.bannerId = :bannerId " +
           "AND s.statDate BETWEEN :startDate AND :endDate")
    List<Object[]> sumStatisticsByBannerIdAndDateRange(
            @Param("bannerId") Long bannerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 增加曝光次数
     * 
     * @param bannerId 轮播图ID
     * @param statDate 统计日期
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE BannerStatistics s SET s.impressions = s.impressions + 1, " +
           "s.updateTime = CURRENT_TIMESTAMP WHERE s.bannerId = :bannerId AND s.statDate = :statDate")
    int incrementImpressions(@Param("bannerId") Long bannerId, @Param("statDate") LocalDate statDate);

    /**
     * 增加点击次数
     * 
     * @param bannerId 轮播图ID
     * @param statDate 统计日期
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE BannerStatistics s SET s.clicks = s.clicks + 1, " +
           "s.updateTime = CURRENT_TIMESTAMP WHERE s.bannerId = :bannerId AND s.statDate = :statDate")
    int incrementClicks(@Param("bannerId") Long bannerId, @Param("statDate") LocalDate statDate);

    /**
     * 增加独立曝光数
     * 
     * @param bannerId 轮播图ID
     * @param statDate 统计日期
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE BannerStatistics s SET s.uniqueImpressions = s.uniqueImpressions + 1, " +
           "s.updateTime = CURRENT_TIMESTAMP WHERE s.bannerId = :bannerId AND s.statDate = :statDate")
    int incrementUniqueImpressions(@Param("bannerId") Long bannerId, @Param("statDate") LocalDate statDate);

    /**
     * 增加独立点击数
     * 
     * @param bannerId 轮播图ID
     * @param statDate 统计日期
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE BannerStatistics s SET s.uniqueClicks = s.uniqueClicks + 1, " +
           "s.updateTime = CURRENT_TIMESTAMP WHERE s.bannerId = :bannerId AND s.statDate = :statDate")
    int incrementUniqueClicks(@Param("bannerId") Long bannerId, @Param("statDate") LocalDate statDate);

    /**
     * 删除指定日期之前的统计数据
     * 
     * @param beforeDate 指定日期
     * @return 删除的记录数
     */
    @Modifying
    @Query("DELETE FROM BannerStatistics s WHERE s.statDate < :beforeDate")
    int deleteByStatDateBefore(@Param("beforeDate") LocalDate beforeDate);

    /**
     * 检查统计数据是否存在
     * 
     * @param bannerId 轮播图ID
     * @param statDate 统计日期
     * @return 是否存在
     */
    boolean existsByBannerIdAndStatDate(Long bannerId, LocalDate statDate);
}
