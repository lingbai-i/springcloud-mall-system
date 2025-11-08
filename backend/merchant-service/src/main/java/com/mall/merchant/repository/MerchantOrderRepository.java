package com.mall.merchant.repository;

import com.mall.merchant.domain.entity.MerchantOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 商家订单数据访问层
 * 提供商家订单相关的数据库操作方法
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Repository
public interface MerchantOrderRepository extends JpaRepository<MerchantOrder, Long> {
    
    /**
     * 根据订单号查找订单
     * 
     * @param orderNo 订单号
     * @return 订单信息
     */
    Optional<MerchantOrder> findByOrderNo(String orderNo);
    
    /**
     * 根据商家ID查找订单列表
     * 
     * @param merchantId 商家ID
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    Page<MerchantOrder> findByMerchantId(Long merchantId, Pageable pageable);
    
    /**
     * 根据商家ID和订单状态查找订单列表
     * 
     * @param merchantId 商家ID
     * @param status 订单状态
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    Page<MerchantOrder> findByMerchantIdAndStatus(Long merchantId, Integer status, Pageable pageable);
    
    /**
     * 根据商家ID和用户ID查找订单列表
     * 
     * @param merchantId 商家ID
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    Page<MerchantOrder> findByMerchantIdAndUserId(Long merchantId, Long userId, Pageable pageable);
    
    /**
     * 根据商家ID和商品ID查找订单列表
     * 
     * @param merchantId 商家ID
     * @param productId 商品ID
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    Page<MerchantOrder> findByMerchantIdAndProductId(Long merchantId, Long productId, Pageable pageable);
    
    /**
     * 根据多个条件查询订单列表
     * 
     * @param merchantId 商家ID
     * @param orderNo 订单号（模糊查询）
     * @param status 订单状态
     * @param paymentMethod 支付方式
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    @Query("SELECT o FROM MerchantOrder o WHERE o.merchantId = :merchantId AND " +
           "(:orderNo IS NULL OR o.orderNo LIKE %:orderNo%) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:paymentMethod IS NULL OR o.paymentMethod = :paymentMethod) AND " +
           "(:startTime IS NULL OR o.createTime >= :startTime) AND " +
           "(:endTime IS NULL OR o.createTime <= :endTime)")
    Page<MerchantOrder> findByConditions(@Param("merchantId") Long merchantId,
                                        @Param("orderNo") String orderNo,
                                        @Param("status") Integer status,
                                        @Param("paymentMethod") Integer paymentMethod,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime,
                                        Pageable pageable);
    
    /**
     * 根据商家ID统计订单总数
     * 
     * @param merchantId 商家ID
     * @return 订单总数
     */
    Long countByMerchantId(Long merchantId);
    
    /**
     * 根据商家ID和状态统计订单数量
     * 
     * @param merchantId 商家ID
     * @param status 订单状态
     * @return 订单数量
     */
    Long countByMerchantIdAndStatus(Long merchantId, Integer status);
    
    /**
     * 根据商家ID统计指定时间范围内的订单数量
     * 
     * @param merchantId 商家ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 订单数量
     */
    @Query("SELECT COUNT(o) FROM MerchantOrder o WHERE o.merchantId = :merchantId AND o.createTime BETWEEN :startTime AND :endTime")
    Long countOrdersByTimeRange(@Param("merchantId") Long merchantId,
                               @Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据商家ID和状态统计指定时间范围内的订单数量
     * 
     * @param merchantId 商家ID
     * @param status 订单状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 订单数量
     */
    @Query("SELECT COUNT(o) FROM MerchantOrder o WHERE o.merchantId = :merchantId AND o.status = :status AND o.createTime BETWEEN :startTime AND :endTime")
    Long countOrdersByStatusAndTimeRange(@Param("merchantId") Long merchantId,
                                        @Param("status") Integer status,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据商家ID统计指定时间范围内的销售总额
     * 
     * @param merchantId 商家ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 销售总额
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM MerchantOrder o WHERE o.merchantId = :merchantId AND o.status >= 2 AND o.createTime BETWEEN :startTime AND :endTime")
    BigDecimal sumSalesByTimeRange(@Param("merchantId") Long merchantId,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据商家ID统计指定时间范围内的实际收入
     * 
     * @param merchantId 商家ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 实际收入
     */
    @Query("SELECT COALESCE(SUM(o.paidAmount), 0) FROM MerchantOrder o WHERE o.merchantId = :merchantId AND o.status = 5 AND o.finishTime BETWEEN :startTime AND :endTime")
    BigDecimal sumIncomeByTimeRange(@Param("merchantId") Long merchantId,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据商家ID统计指定时间范围内的退款金额
     * 
     * @param merchantId 商家ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 退款金额
     */
    @Query("SELECT COALESCE(SUM(o.refundAmount), 0) FROM MerchantOrder o WHERE o.merchantId = :merchantId AND o.refundStatus = 2 AND o.refundTime BETWEEN :startTime AND :endTime")
    BigDecimal sumRefundByTimeRange(@Param("merchantId") Long merchantId,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据商家ID查找待发货订单列表
     * 
     * @param merchantId 商家ID
     * @param pageable 分页参数
     * @return 待发货订单分页列表
     */
    @Query("SELECT o FROM MerchantOrder o WHERE o.merchantId = :merchantId AND o.status = 2 ORDER BY o.paymentTime ASC")
    Page<MerchantOrder> findPendingShipmentOrders(@Param("merchantId") Long merchantId, Pageable pageable);
    
    /**
     * 根据商家ID查找待收货订单列表
     * 
     * @param merchantId 商家ID
     * @param pageable 分页参数
     * @return 待收货订单分页列表
     */
    @Query("SELECT o FROM MerchantOrder o WHERE o.merchantId = :merchantId AND o.status = 3 ORDER BY o.shipTime ASC")
    Page<MerchantOrder> findPendingReceiptOrders(@Param("merchantId") Long merchantId, Pageable pageable);
    
    /**
     * 根据商家ID查找退款订单列表
     * 
     * @param merchantId 商家ID
     * @param pageable 分页参数
     * @return 退款订单分页列表
     */
    @Query("SELECT o FROM MerchantOrder o WHERE o.merchantId = :merchantId AND o.refundStatus = 1 ORDER BY o.createTime DESC")
    Page<MerchantOrder> findRefundOrders(@Param("merchantId") Long merchantId, Pageable pageable);
    
    /**
     * 根据商家ID查找最近的订单列表
     * 
     * @param merchantId 商家ID
     * @param pageable 分页参数
     * @return 最近订单分页列表
     */
    @Query("SELECT o FROM MerchantOrder o WHERE o.merchantId = :merchantId ORDER BY o.createTime DESC")
    Page<MerchantOrder> findRecentOrders(@Param("merchantId") Long merchantId, Pageable pageable);
    
    /**
     * 根据商家ID查找热销商品统计
     * 
     * @param merchantId 商家ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 热销商品统计列表
     */
    @Query("SELECT o.productId, o.productName, SUM(o.quantity) as totalSales, COUNT(o) as orderCount " +
           "FROM MerchantOrder o WHERE o.merchantId = :merchantId AND o.status >= 2 " +
           "AND o.createTime BETWEEN :startTime AND :endTime " +
           "GROUP BY o.productId, o.productName ORDER BY totalSales DESC")
    List<Object[]> findHotProductsStatistics(@Param("merchantId") Long merchantId,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime,
                                            Pageable pageable);
    
    /**
     * 根据商家ID查找客户购买统计
     * 
     * @param merchantId 商家ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 客户购买统计列表
     */
    @Query("SELECT o.userId, COUNT(o) as orderCount, SUM(o.paidAmount) as totalAmount " +
           "FROM MerchantOrder o WHERE o.merchantId = :merchantId AND o.status = 5 " +
           "AND o.finishTime BETWEEN :startTime AND :endTime " +
           "GROUP BY o.userId ORDER BY totalAmount DESC")
    List<Object[]> findCustomerStatistics(@Param("merchantId") Long merchantId,
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime,
                                         Pageable pageable);
    
    /**
     * 根据商家ID查找每日销售统计
     * 
     * @param merchantId 商家ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 每日销售统计列表
     */
    @Query("SELECT DATE(o.createTime) as saleDate, COUNT(o) as orderCount, SUM(o.totalAmount) as totalAmount " +
           "FROM MerchantOrder o WHERE o.merchantId = :merchantId AND o.status >= 2 " +
           "AND o.createTime BETWEEN :startTime AND :endTime " +
           "GROUP BY DATE(o.createTime) ORDER BY saleDate")
    List<Object[]> findDailySalesStatistics(@Param("merchantId") Long merchantId,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据商家ID查找每月销售统计
     * 
     * @param merchantId 商家ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 每月销售统计列表
     */
    @Query("SELECT YEAR(o.createTime) as saleYear, MONTH(o.createTime) as saleMonth, " +
           "COUNT(o) as orderCount, SUM(o.totalAmount) as totalAmount " +
           "FROM MerchantOrder o WHERE o.merchantId = :merchantId AND o.status >= 2 " +
           "AND o.createTime BETWEEN :startTime AND :endTime " +
           "GROUP BY YEAR(o.createTime), MONTH(o.createTime) ORDER BY saleYear, saleMonth")
    List<Object[]> findMonthlySalesStatistics(@Param("merchantId") Long merchantId,
                                             @Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);
    

    


    /**
     * 检查订单是否存在且属于指定商家
     * 
     * @param orderId 订单ID
     * @param merchantId 商家ID
     * @return 是否存在
     */
    boolean existsByIdAndMerchantId(Long orderId, Long merchantId);
    
    /**
     * 检查订单号是否存在
     * 
     * @param orderNo 订单号
     * @return 是否存在
     */
    boolean existsByOrderNo(String orderNo);
    
    /**
     * 统计指定商家在时间范围内的销售总额
     * 设计说明：
     * - 使用实体字段 {@code status}，避免错误引用不存在的 {@code orderStatus} 导致上下文加载失败；
     * - 仅统计已完成订单（按业务约定，已完成状态为 5），与其他统计查询保持一致口径；
     * - 保留原有时间范围过滤，确保统计窗口准确。
     *
     * V1.1：修复JPQL字段名错误，统一使用 status 字段并限定已完成状态
     *
     * @author lingbai
     * @param merchantId 商家ID（不能为空）
     * @param startDate  开始时间（不能为空）
     * @param endDate    结束时间（不能为空）
     * @return 销售总额（可能为空，建议调用方做空值处理）
     */
    @Query("SELECT SUM(o.totalAmount) FROM MerchantOrder o WHERE o.merchantId = :merchantId AND o.status = 5 AND o.createTime BETWEEN :startDate AND :endDate")
    BigDecimal sumSalesByDateRange(@Param("merchantId") Long merchantId,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * 根据商家ID统计各状态订单数量
     * 
     * @param merchantId 商家ID
     * @return 状态-数量映射
     */
    @Query("SELECT o.status, COUNT(o) FROM MerchantOrder o WHERE o.merchantId = :merchantId GROUP BY o.status")
    List<Object[]> findOrderCountByStatusRaw(@Param("merchantId") Long merchantId);

    /**
     * 根据商家ID统计各状态订单数量（返回Map格式）
     * 
     * @param merchantId 商家ID
     * @return 状态-数量映射
     */
    default Map<Integer, Long> findOrderCountByStatus(Long merchantId) {
        List<Object[]> results = findOrderCountByStatusRaw(merchantId);
        return results.stream()
                .collect(java.util.stream.Collectors.toMap(
                    row -> (Integer) row[0],
                    row -> (Long) row[1]
                ));
    }
}