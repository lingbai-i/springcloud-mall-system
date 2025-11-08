package com.mall.merchant.repository;

import com.mall.merchant.domain.entity.Merchant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 商家数据访问层
 * 提供商家相关的数据库操作方法
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    
    /**
     * 根据用户名查找商家
     * 
     * @param username 用户名
     * @return 商家信息
     */
    Optional<Merchant> findByUsername(String username);
    
    /**
     * 根据店铺名称查找商家
     * 
     * @param shopName 店铺名称
     * @return 商家信息
     */
    Optional<Merchant> findByShopName(String shopName);
    
    /**
     * 根据手机号查找商家
     * 
     * @param contactPhone 手机号
     * @return 商家信息
     */
    Optional<Merchant> findByContactPhone(String contactPhone);
    
    /**
     * 根据邮箱查找商家
     * 修复说明：实体持久化字段为 {@code contactEmail}，因此派生查询需使用 {@code contactEmail}。
     *
     * @param contactEmail 邮箱
     * @return 商家信息
     */
    Optional<Merchant> findByContactEmail(String contactEmail);
    
    /**
     * 根据身份证号查找商家
     * 修复说明：实体持久化字段为 {@code idNumber}。
     *
     * @param idNumber 身份证号
     * @return 商家信息
     */
    Optional<Merchant> findByIdNumber(String idNumber);
    
    /**
     * 根据营业执照图片/编号查找商家
     * 修复说明：实体无 {@code businessLicense} 持久化字段，实际使用 {@code idFrontImage} 存储；
     * 若后续引入独立字段可再调整。
     *
     * @param idFrontImage 证件/执照图片或编号存储字段
     * @return 商家信息
     */
    Optional<Merchant> findByIdFrontImage(String idFrontImage);
    
    /**
     * 根据审核状态查找商家列表
     * 
     * @param auditStatus 审核状态
     * @param pageable 分页参数
     * @return 商家分页列表
     */
    /**
     * 根据审核状态查找商家列表
     * 修复说明：实体字段为 {@code approvalStatus}。
     */
    Page<Merchant> findByApprovalStatus(Integer auditStatus, Pageable pageable);
    
    /**
     * 根据商家状态查找商家列表
     * 
     * @param status 商家状态
     * @param pageable 分页参数
     * @return 商家分页列表
     */
    Page<Merchant> findByStatus(Integer status, Pageable pageable);
    
    /**
     * 根据商家类型查找商家列表
     * 
     * @param merchantType 商家类型
     * @param pageable 分页参数
     * @return 商家分页列表
     */
    Page<Merchant> findByMerchantType(Integer merchantType, Pageable pageable);
    
    /**
     * 根据多个条件查询商家列表
     * 
     * @param shopName 店铺名称（模糊查询）
     * @param merchantType 商家类型
     * @param auditStatus 审核状态
     * @param status 商家状态
     * @param pageable 分页参数
     * @return 商家分页列表
     */
    /**
     * 组合条件查询商家列表
     * 修复说明：实体字段为 {@code approvalStatus}，而非 {@code auditStatus}；
     * JPQL 必须与实体字段严格一致，否则在应用上下文加载阶段会抛出解析异常导致测试启动失败。
     */
    @Query("SELECT m FROM Merchant m WHERE " +
           "(:shopName IS NULL OR m.shopName LIKE %:shopName%) AND " +
           "(:merchantType IS NULL OR m.merchantType = :merchantType) AND " +
           "(:auditStatus IS NULL OR m.approvalStatus = :auditStatus) AND " +
           "(:status IS NULL OR m.status = :status)")
    Page<Merchant> findByConditions(@Param("shopName") String shopName,
                                   @Param("merchantType") Integer merchantType,
                                   @Param("auditStatus") Integer auditStatus,
                                    @Param("status") Integer status,
                                    Pageable pageable);
    
    /**
     * 统计商家总数
     * 
     * @return 商家总数
     */
    @Query("SELECT COUNT(m) FROM Merchant m")
    Long countTotalMerchants();
    
    /**
     * 根据审核状态统计商家数量
     * 修复说明：实体字段为 {@code approvalStatus}，派生统计方法需与之对齐。
     *
     * @param approvalStatus 审核状态
     * @return 商家数量
     */
    Long countByApprovalStatus(Integer approvalStatus);
    
    /**
     * 根据商家状态统计商家数量
     * 
     * @param status 商家状态
     * @return 商家数量
     */
    Long countByStatus(Integer status);
    
    /**
     * 根据商家类型统计商家数量
     * 
     * @param merchantType 商家类型
     * @return 商家数量
     */
    Long countByMerchantType(Integer merchantType);
    
    /**
     * 统计指定时间范围内新注册的商家数量
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 新注册商家数量
     */
    @Query("SELECT COUNT(m) FROM Merchant m WHERE m.createTime BETWEEN :startTime AND :endTime")
    Long countNewMerchants(@Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计指定时间范围内通过审核的商家数量
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 通过审核的商家数量
     */
    /**
     * 统计审核通过的商家数量（时间范围）
     * 修复说明：实体字段为 {@code approvalStatus} 与 {@code approvalTime}。
     */
    @Query("SELECT COUNT(m) FROM Merchant m WHERE m.approvalStatus = 2 AND m.approvalTime BETWEEN :startTime AND :endTime")
    Long countApprovedMerchants(@Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找最近登录的商家列表
     * 
     * @param limit 限制数量
     * @return 商家列表
     */
    @Query("SELECT m FROM Merchant m WHERE m.lastLoginTime IS NOT NULL ORDER BY m.lastLoginTime DESC")
    List<Merchant> findRecentlyLoggedInMerchants(Pageable pageable);
    
    /**
     * 查找活跃商家列表（最近30天有登录）
     * 
     * @param thirtyDaysAgo 30天前的时间
     * @param pageable 分页参数
     * @return 活跃商家分页列表
     */
    @Query("SELECT m FROM Merchant m WHERE m.lastLoginTime >= :thirtyDaysAgo ORDER BY m.lastLoginTime DESC")
    Page<Merchant> findActiveMerchants(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo, Pageable pageable);
    
    /**
     * 查找待审核的商家列表
     * 
     * @param pageable 分页参数
     * @return 待审核商家分页列表
     */
    /**
     * 查找待审核的商家列表
     * 修复说明：实体字段为 {@code approvalStatus}，将原来的 {@code auditStatus} 更正为 {@code approvalStatus}。
     */
    @Query("SELECT m FROM Merchant m WHERE m.approvalStatus = 1 ORDER BY m.createTime ASC")
    Page<Merchant> findPendingAuditMerchants(Pageable pageable);
    
    /**
     * 根据保证金范围查找商家
     * 
     * @param minDeposit 最小保证金
     * @param maxDeposit 最大保证金
     * @param pageable 分页参数
     * @return 商家分页列表
     */
    /**
     * 根据保证金范围查找商家
     * 修复说明：实体字段为 {@code depositAmount}，不存在 {@code deposit} 字段。
     */
    @Query("SELECT m FROM Merchant m WHERE m.depositAmount BETWEEN :minDeposit AND :maxDeposit")
    Page<Merchant> findByDepositRange(@Param("minDeposit") java.math.BigDecimal minDeposit,
                                     @Param("maxDeposit") java.math.BigDecimal maxDeposit,
                                     Pageable pageable);
    
    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查店铺名称是否存在
     * 
     * @param shopName 店铺名称
     * @return 是否存在
     */
    boolean existsByShopName(String shopName);
    
    /**
     * 检查手机号是否已存在
     * 
     * @param contactPhone 手机号
     * @return 是否存在
     */
    boolean existsByContactPhone(String contactPhone);
    
    /**
     * 检查邮箱是否存在
     * 修复说明：实体持久化字段为 {@code contactEmail}。
     *
     * @param contactEmail 邮箱
     * @return 是否存在
     */
    boolean existsByContactEmail(String contactEmail);
    
    /**
     * 检查身份证号是否存在
     * 修复说明：实体持久化字段为 {@code idNumber}。
     *
     * @param idNumber 身份证号
     * @return 是否存在
     */
    boolean existsByIdNumber(String idNumber);
    
    /**
     * 检查执照图片/编号是否存在
     * 修复说明：实体无 {@code businessLicense} 持久化字段，使用 {@code idFrontImage}。
     *
     * @param idFrontImage 证件/执照图片或编号存储字段
     * @return 是否存在
     */
    boolean existsByIdFrontImage(String idFrontImage);
}