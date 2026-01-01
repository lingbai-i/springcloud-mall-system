package com.mall.merchant.repository;

import com.mall.merchant.domain.entity.BannerApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 轮播图申请数据访问层
 * 提供轮播图申请相关的数据库操作方法
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-12-28
 */
@Repository
public interface BannerApplicationRepository extends JpaRepository<BannerApplication, Long> {

    // ==================== 商家端查询 ====================

    /**
     * 根据商家ID查找申请列表（分页）
     * 
     * @param merchantId 商家ID
     * @param pageable 分页参数
     * @return 申请分页列表
     */
    Page<BannerApplication> findByMerchantIdAndDeletedOrderByCreateTimeDesc(
            Long merchantId, Integer deleted, Pageable pageable);

    /**
     * 根据商家ID和状态查找申请列表（分页）
     * 
     * @param merchantId 商家ID
     * @param status 状态
     * @param pageable 分页参数
     * @return 申请分页列表
     */
    Page<BannerApplication> findByMerchantIdAndStatusAndDeletedOrderByCreateTimeDesc(
            Long merchantId, String status, Integer deleted, Pageable pageable);

    /**
     * 根据商家ID和申请ID查找申请
     * 
     * @param id 申请ID
     * @param merchantId 商家ID
     * @return 申请详情
     */
    Optional<BannerApplication> findByIdAndMerchantIdAndDeleted(Long id, Long merchantId, Integer deleted);

    /**
     * 统计商家的申请数量
     * 
     * @param merchantId 商家ID
     * @param status 状态
     * @return 申请数量
     */
    Long countByMerchantIdAndStatusAndDeleted(Long merchantId, String status, Integer deleted);

    // ==================== 管理员端查询 ====================

    /**
     * 查找待审核的申请列表（分页）
     * 
     * @param status 状态
     * @param pageable 分页参数
     * @return 申请分页列表
     */
    Page<BannerApplication> findByStatusAndDeletedOrderByCreateTimeAsc(
            String status, Integer deleted, Pageable pageable);

    /**
     * 根据状态查找申请列表（分页，按创建时间倒序）
     * 
     * @param status 状态
     * @param deleted 删除标志
     * @param pageable 分页参数
     * @return 申请分页列表
     */
    Page<BannerApplication> findByStatusAndDeletedOrderByCreateTimeDesc(
            String status, Integer deleted, Pageable pageable);

    /**
     * 查找所有申请列表（分页，支持状态筛选）
     * 
     * @param pageable 分页参数
     * @return 申请分页列表
     */
    Page<BannerApplication> findByDeletedOrderByCreateTimeDesc(Integer deleted, Pageable pageable);

    /**
     * 根据ID和删除标志查找申请（管理员用，不校验商家ID）
     * 
     * @param id 申请ID
     * @param deleted 删除标志
     * @return 申请详情
     */
    Optional<BannerApplication> findByIdAndDeleted(Long id, Integer deleted);

    /**
     * 统计待审核申请数量
     * 
     * @param status 状态
     * @return 待审核数量
     */
    Long countByStatusAndDeleted(String status, Integer deleted);

    /**
     * 根据状态和日期范围查找申请列表
     * 
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param pageable 分页参数
     * @return 申请分页列表
     */
    @Query("SELECT b FROM BannerApplication b WHERE b.status = :status AND b.deleted = 0 " +
           "AND b.createTime BETWEEN :startDate AND :endDate ORDER BY b.createTime DESC")
    Page<BannerApplication> findByStatusAndDateRange(
            @Param("status") String status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // ==================== 活跃轮播图查询 ====================

    /**
     * 查找当前活跃的轮播图列表
     * 条件：状态为已通过，当前日期在展示周期内
     * 
     * @param status 状态（APPROVED）
     * @param currentDate 当前日期
     * @param deleted 删除标志
     * @param pageable 分页参数（限制数量）
     * @return 活跃轮播图列表
     */
    @Query("SELECT b FROM BannerApplication b WHERE b.status = :status AND b.deleted = :deleted " +
           "AND b.startDate <= :currentDate AND b.endDate >= :currentDate " +
           "ORDER BY b.sortOrder DESC, b.reviewTime ASC")
    List<BannerApplication> findActiveBanners(
            @Param("status") String status,
            @Param("currentDate") LocalDate currentDate,
            @Param("deleted") Integer deleted,
            Pageable pageable);

    /**
     * 统计当前活跃的轮播图数量
     * 
     * @param status 状态（APPROVED）
     * @param currentDate 当前日期
     * @return 活跃轮播图数量
     */
    @Query("SELECT COUNT(b) FROM BannerApplication b WHERE b.status = :status AND b.deleted = 0 " +
           "AND b.startDate <= :currentDate AND b.endDate >= :currentDate")
    Long countActiveBanners(@Param("status") String status, @Param("currentDate") LocalDate currentDate);

    // ==================== 状态更新 ====================

    /**
     * 更新申请状态为已过期
     * 将所有已通过但展示结束日期已过的申请标记为已过期
     * 
     * @param newStatus 新状态（EXPIRED）
     * @param oldStatus 旧状态（APPROVED）
     * @param currentDate 当前日期
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE BannerApplication b SET b.status = :newStatus, b.updateTime = CURRENT_TIMESTAMP " +
           "WHERE b.status = :oldStatus AND b.endDate < :currentDate AND b.deleted = 0")
    int updateExpiredBanners(
            @Param("newStatus") String newStatus,
            @Param("oldStatus") String oldStatus,
            @Param("currentDate") LocalDate currentDate);

    /**
     * 审核通过申请
     * 
     * @param id 申请ID
     * @param status 新状态
     * @param reviewerId 审核人ID
     * @param reviewTime 审核时间
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE BannerApplication b SET b.status = :status, b.reviewerId = :reviewerId, " +
           "b.reviewTime = :reviewTime, b.updateTime = CURRENT_TIMESTAMP WHERE b.id = :id")
    int approveApplication(
            @Param("id") Long id,
            @Param("status") String status,
            @Param("reviewerId") Long reviewerId,
            @Param("reviewTime") LocalDateTime reviewTime);

    /**
     * 审核拒绝申请
     * 
     * @param id 申请ID
     * @param status 新状态
     * @param rejectReason 拒绝原因
     * @param reviewerId 审核人ID
     * @param reviewTime 审核时间
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE BannerApplication b SET b.status = :status, b.rejectReason = :rejectReason, " +
           "b.reviewerId = :reviewerId, b.reviewTime = :reviewTime, b.updateTime = CURRENT_TIMESTAMP " +
           "WHERE b.id = :id")
    int rejectApplication(
            @Param("id") Long id,
            @Param("status") String status,
            @Param("rejectReason") String rejectReason,
            @Param("reviewerId") Long reviewerId,
            @Param("reviewTime") LocalDateTime reviewTime);

    /**
     * 取消申请
     * 
     * @param id 申请ID
     * @param merchantId 商家ID
     * @param status 新状态
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE BannerApplication b SET b.status = :status, b.updateTime = CURRENT_TIMESTAMP " +
           "WHERE b.id = :id AND b.merchantId = :merchantId AND b.status = 'PENDING'")
    int cancelApplication(
            @Param("id") Long id,
            @Param("merchantId") Long merchantId,
            @Param("status") String status);
}
