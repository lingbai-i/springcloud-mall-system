package com.mall.merchant.repository;

import com.mall.merchant.domain.entity.MerchantProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 商家商品数据访问层
 * 提供商家商品相关的数据库操作方法。
 *
 * 设计说明：
 * - 所有派生查询方法与实体字段严格一致，避免 JPQL 解析错误。
 * - 统一使用 `brand`（String）而非不存在的 `brandId` 字段。
 * - 分页方法统一返回 `Page<MerchantProduct>`，参数命名遵循小驼峰规范。
 *
 * 日志与注释规范：
 * - 使用 Javadoc3 对公共方法进行注释，说明参数与返回值。
 * - 记录必要的设计取舍（如可空参数在 JPQL 中的处理方式）。
 *
 * @author lingbai
 * @version 1.1
 * @since 2025-01-27
 *
 * 修改日志：
 * V1.1 2025-11-05：
 * 1) 删除重复的派生查询与注释（isHot/isRecommended/isNew 等方法的重复定义）。
 * 2) 统一多条件查询方法的参数签名与 JPQL 条件，修复命名不一致问题。
 * 3) 补充完整 Javadoc，明确分页与可空条件的行为约束。
 */
@Repository
public interface MerchantProductRepository extends JpaRepository<MerchantProduct, Long> {
    
    /**
     * 根据商家ID查找商品列表
     * 
     * @param merchantId 商家ID
     * @param pageable 分页参数
     * @return 商品分页列表
     */
    Page<MerchantProduct> findByMerchantId(Long merchantId, Pageable pageable);
    
    /**
     * 根据商家ID和商品状态查找商品列表
     * 
     * @param merchantId 商家ID
     * @param status 商品状态
     * @param pageable 分页参数
     * @return 商品分页列表
     */
    Page<MerchantProduct> findByMerchantIdAndStatus(Long merchantId, Integer status, Pageable pageable);
    
    /**
     * 根据商家ID和分类查找商品列表
     * 
     * @param merchantId 商家ID
     * @param categoryId 分类ID
     * @param pageable 分页参数
     * @return 商品分页列表
     */
    Page<MerchantProduct> findByMerchantIdAndCategoryId(Long merchantId, Long categoryId, Pageable pageable);
    
    /**
     * 根据商家ID和品牌查找商品列表。
     * 说明：实体字段为 {@code brand}（String），不存在 {@code brandId} 字段。
     *
     * @param merchantId 商家ID
     * @param brand 品牌名称
     * @param pageable 分页参数
     * @return 商品分页列表
     */
    Page<MerchantProduct> findByMerchantIdAndBrand(Long merchantId, String brand, Pageable pageable);
    
    /**
     * 根据商家ID和商品名称模糊查询
     * 
     * @param merchantId 商家ID
     * @param productName 商品名称
     * @param pageable 分页参数
     * @return 商品分页列表
     */
    Page<MerchantProduct> findByMerchantIdAndProductNameContaining(Long merchantId, String productName, Pageable pageable);
    
    /**
     * 多条件组合查询商品列表。
     * 说明：实体无 {@code brandId} 字段，使用 {@code brand}（String）进行过滤；
     * 当查询条件为 {@code null} 时，使用可选条件绕过该过滤。
     *
     * @param merchantId 商家ID（必填）
     * @param productName 商品名称（模糊查询，可为空）
     * @param categoryId 分类ID（可为空）
     * @param brand 品牌（可为空）
     * @param status 商品状态（可为空）
     * @param pageable 分页参数（必填）
     * @return 商品分页列表
     */
    @Query("SELECT p FROM MerchantProduct p WHERE p.merchantId = :merchantId AND " +
           "(:productName IS NULL OR p.productName LIKE %:productName%) AND " +
           "(:categoryId IS NULL OR p.categoryId = :categoryId) AND " +
           "(:brand IS NULL OR p.brand = :brand) AND " +
           "(:status IS NULL OR p.status = :status)")
    Page<MerchantProduct> findByConditions(@Param("merchantId") Long merchantId,
                                          @Param("productName") String productName,
                                          @Param("categoryId") Long categoryId,
                                          @Param("brand") String brand,
                                          @Param("status") Integer status,
                                          Pageable pageable);
    
    /**
     * 根据商家ID统计商品总数
     * 
     * @param merchantId 商家ID
     * @return 商品总数
     */
    Long countByMerchantId(Long merchantId);
    
    /**
     * 根据商家ID和状态统计商品数量
     * 
     * @param merchantId 商家ID
     * @param status 商品状态
     * @return 商品数量
     */
    Long countByMerchantIdAndStatus(Long merchantId, Integer status);
    
    /**
     * 根据商家ID统计上架商品数量
     * 
     * @param merchantId 商家ID
     * @return 上架商品数量
     */
    @Query("SELECT COUNT(p) FROM MerchantProduct p WHERE p.merchantId = :merchantId AND p.status = 1")
    Long countOnlineProducts(@Param("merchantId") Long merchantId);
    
    /**
     * 根据商家ID统计下架商品数量
     * 
     * @param merchantId 商家ID
     * @return 下架商品数量
     */
    @Query("SELECT COUNT(p) FROM MerchantProduct p WHERE p.merchantId = :merchantId AND p.status = 0")
    Long countOfflineProducts(@Param("merchantId") Long merchantId);
    
    /**
     * 根据商家ID统计库存不足的商品数量
     * 
     * @param merchantId 商家ID
     * @param lowStockThreshold 库存不足阈值
     * @return 库存不足商品数量
     */
    @Query("SELECT COUNT(p) FROM MerchantProduct p WHERE p.merchantId = :merchantId AND p.stockQuantity <= :lowStockThreshold")
    Long countLowStockProducts(@Param("merchantId") Long merchantId, @Param("lowStockThreshold") Integer lowStockThreshold);
    
    /**
     * 根据商家ID查找库存不足的商品列表
     * 
     * @param merchantId 商家ID
     * @param lowStockThreshold 库存不足阈值
     * @param pageable 分页参数
     * @return 库存不足商品分页列表
     */
    @Query("SELECT p FROM MerchantProduct p WHERE p.merchantId = :merchantId AND p.stockQuantity <= :lowStockThreshold ORDER BY p.stockQuantity ASC")
    Page<MerchantProduct> findLowStockProducts(@Param("merchantId") Long merchantId, 
                                              @Param("lowStockThreshold") Integer lowStockThreshold, 
                                              Pageable pageable);

    /**
     * 根据商家ID查找库存小于阈值的商品（派生查询）
     */
    Page<MerchantProduct> findByMerchantIdAndStockQuantityLessThan(Long merchantId, Integer lowStockThreshold, Pageable pageable);
    
    /**
     * 根据商家ID查找热销商品列表
     * 
     * @param merchantId 商家ID
     * @param pageable 分页参数
     * @return 热销商品分页列表
     */
    @Query("SELECT p FROM MerchantProduct p WHERE p.merchantId = :merchantId AND p.status = 1 ORDER BY p.salesCount DESC")
    Page<MerchantProduct> findHotProducts(@Param("merchantId") Long merchantId, Pageable pageable);
    
    /**
     * 根据商家ID查找推荐商品列表（按标记值）。
     *
     * @param merchantId 商家ID
     * @param isRecommended 是否推荐（0/1）
     * @param pageable 分页参数
     * @return 推荐商品分页列表
     */
    Page<MerchantProduct> findByMerchantIdAndIsRecommended(Long merchantId, Integer isRecommended, Pageable pageable);
    
    /**
     * 根据商家ID查找新品列表（按标记值）。
     *
     * @param merchantId 商家ID
     * @param isNew 是否新品（0/1）
     * @param pageable 分页参数
     * @return 新品分页列表
     */
    Page<MerchantProduct> findByMerchantIdAndIsNew(Long merchantId, Integer isNew, Pageable pageable);
    
    /**
     * 根据商家ID查找热销标记商品列表（按标记值）。
     *
     * @param merchantId 商家ID
     * @param isHot 是否热销（0/1）
     * @param pageable 分页参数
     * @return 热销商品分页列表
     */
    Page<MerchantProduct> findByMerchantIdAndIsHot(Long merchantId, Integer isHot, Pageable pageable);
    
    /**
     * 根据价格范围查找商品。
     * 说明：价格区间为闭区间，包含最小值与最大值。
     *
     * @param merchantId 商家ID
     * @param minPrice 最低价格（必填）
     * @param maxPrice 最高价格（必填）
     * @param pageable 分页参数
     * @return 商品分页列表
     */
    @Query("SELECT p FROM MerchantProduct p WHERE p.merchantId = :merchantId AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<MerchantProduct> findByPriceRange(@Param("merchantId") Long merchantId,
                                          @Param("minPrice") BigDecimal minPrice,
                                          @Param("maxPrice") BigDecimal maxPrice,
                                          Pageable pageable);
    
    /**
     * 根据商家ID统计指定时间范围内新增的商品数量。
     *
     * @param merchantId 商家ID
     * @param startTime 开始时间（包含）
     * @param endTime 结束时间（包含）
     * @return 新增商品数量
     */
    @Query("SELECT COUNT(p) FROM MerchantProduct p WHERE p.merchantId = :merchantId AND p.createTime BETWEEN :startTime AND :endTime")
    Long countNewProducts(@Param("merchantId") Long merchantId,
                         @Param("startTime") LocalDateTime startTime,
                         @Param("endTime") LocalDateTime endTime);
    
    /**
     * 批量更新商品状态。
     *
     * @param productIds 商品ID列表
     * @param status 新状态
     * @param merchantId 商家ID（用于权限验证）
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE MerchantProduct p SET p.status = :status WHERE p.id IN :productIds AND p.merchantId = :merchantId")
    int batchUpdateStatus(@Param("productIds") List<Long> productIds, 
                         @Param("status") Integer status, 
                         @Param("merchantId") Long merchantId);
    
    /**
     * 批量更新商品库存。
     *
     * @param productId 商品ID
     * @param quantity 库存变化量（正数增加，负数减少）
     * @param merchantId 商家ID（用于权限验证）
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE MerchantProduct p SET p.stockQuantity = p.stockQuantity + :quantity WHERE p.id = :productId AND p.merchantId = :merchantId")
    int updateStock(@Param("productId") Long productId, 
                   @Param("quantity") Integer quantity, 
                   @Param("merchantId") Long merchantId);
    
    /**
     * 增加商品销售数量。
     *
     * @param productId 商品ID
     * @param quantity 销售数量
     * @param merchantId 商家ID（用于权限验证）
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE MerchantProduct p SET p.salesCount = p.salesCount + :quantity WHERE p.id = :productId AND p.merchantId = :merchantId")
    int increaseSalesCount(@Param("productId") Long productId, 
                          @Param("quantity") Integer quantity, 
                          @Param("merchantId") Long merchantId);
    
    /**
     * 增加商品浏览量。
     *
     * @param productId 商品ID
     * @param count 增加数量
     * @return 影响行数
     */
    @Modifying
    @Query("UPDATE MerchantProduct p SET p.viewCount = p.viewCount + :count WHERE p.id = :productId")
    int increaseViewCount(@Param("productId") Long productId, @Param("count") Integer count);
    
    /**
     * 增加商品收藏数。
     *
     * @param productId 商品ID
     * @return 影响行数
     */
    @Modifying
    @Query("UPDATE MerchantProduct p SET p.favoriteCount = p.favoriteCount + 1 WHERE p.id = :productId")
    int increaseFavoriteCount(@Param("productId") Long productId);
    
    /**
     * 减少商品收藏数。
     *
     * @param productId 商品ID
     * @return 影响行数
     */
    @Modifying
    @Query("UPDATE MerchantProduct p SET p.favoriteCount = CASE WHEN p.favoriteCount > 0 THEN p.favoriteCount - 1 ELSE 0 END WHERE p.id = :productId")
    int decreaseFavoriteCount(@Param("productId") Long productId);
    
    /**
     * 检查商品是否属于指定商家。
     *
     * @param productId 商品ID
     * @param merchantId 商家ID
     * @return 是否属于该商家
     */
    boolean existsByIdAndMerchantId(Long productId, Long merchantId);
    
    /**
     * 统计推荐商品数量。
     *
     * @param merchantId 商家ID
     * @param isRecommended 是否推荐
     * @param status 商品状态
     * @return 推荐商品数量
     */
    Long countByMerchantIdAndIsRecommendedAndStatus(Long merchantId, Integer isRecommended, Integer status);
    
    /**
     * 统计新品数量。
     *
     * @param merchantId 商家ID
     * @param isNew 是否新品
     * @param status 商品状态
     * @return 新品数量
     */
    Long countByMerchantIdAndIsNewAndStatus(Long merchantId, Integer isNew, Integer status);
    
    /**
     * 根据商家ID和热销状态统计商品数量。
     *
     * @param merchantId 商家ID
     * @param isHot 是否热销
     * @param status 商品状态
     * @return 商品数量
     */
    Long countByMerchantIdAndIsHotAndStatus(Long merchantId, Integer isHot, Integer status);
    
    // 重复的派生查询定义已清理（isHot / isRecommended / isNew / stockQuantityLessThan）。
}