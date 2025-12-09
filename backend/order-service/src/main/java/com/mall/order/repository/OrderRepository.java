package com.mall.order.repository;

import com.mall.order.entity.Order;
import com.mall.order.enums.OrderStatus;
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
 * 订单数据访问接口
 * 提供订单相关的数据库操作方法
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * 根据订单号查找订单
     * 
     * @param orderNo 订单号
     * @return 订单信息
     */
    Optional<Order> findByOrderNo(String orderNo);
    
    /**
     * 根据用户ID分页查询订单
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 订单分页数据
     */
    Page<Order> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);
    
    /**
     * 根据用户ID和订单状态分页查询订单
     * 
     * @param userId 用户ID
     * @param status 订单状态
     * @param pageable 分页参数
     * @return 订单分页数据
     */
    Page<Order> findByUserIdAndStatusOrderByCreateTimeDesc(Long userId, OrderStatus status, Pageable pageable);
    
    /**
     * 根据用户ID和订单状态列表分页查询订单
     * 
     * @param userId 用户ID
     * @param statuses 订单状态列表
     * @param pageable 分页参数
     * @return 订单分页数据
     */
    Page<Order> findByUserIdAndStatusInOrderByCreateTimeDesc(Long userId, List<OrderStatus> statuses, Pageable pageable);
    
    /**
     * 查询指定时间之前创建的待付款订单
     * 用于订单超时处理
     * 
     * @param createTime 创建时间
     * @return 超时订单列表
     */
    List<Order> findByStatusAndCreateTimeBefore(OrderStatus status, LocalDateTime createTime);
    
    /**
     * 查询指定时间之前发货的订单
     * 用于自动确认收货
     * 
     * @param shipTime 发货时间
     * @return 待自动确认的订单列表
     */
    List<Order> findByStatusAndShipTimeBefore(OrderStatus status, LocalDateTime shipTime);
    
    /**
     * 统计用户各状态订单数量
     * 
     * @param userId 用户ID
     * @return 订单统计信息
     */
    @Query("SELECT o.status, COUNT(o) FROM Order o WHERE o.userId = :userId GROUP BY o.status")
    List<Object[]> countOrdersByUserIdGroupByStatus(@Param("userId") Long userId);
    
    /**
     * 统计用户订单总数
     * 
     * @param userId 用户ID
     * @return 订单总数
     */
    long countByUserId(Long userId);
    
    /**
     * 查询用户最近的订单
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 最近订单列表
     */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.createTime DESC")
    List<Order> findRecentOrdersByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 检查订单号是否存在
     * 
     * @param orderNo 订单号
     * @return 是否存在
     */
    boolean existsByOrderNo(String orderNo);
    
    /**
     * 统计用户指定状态的订单数量
     * 
     * @param userId 用户ID
     * @param status 订单状态
     * @return 订单数量
     */
    long countByUserIdAndStatus(Long userId, OrderStatus status);
    
    /**
     * 计算用户指定状态订单的总金额
     * 
     * @param userId 用户ID
     * @param status 订单状态
     * @return 总金额
     */
    @Query("SELECT SUM(o.payAmount) FROM Order o WHERE o.userId = :userId AND o.status = :status")
    java.math.BigDecimal sumPayableAmountByUserIdAndStatus(@Param("userId") Long userId, @Param("status") OrderStatus status);
    
    // ==================== 商家订单查询 ====================
    
    /**
     * 根据商家ID分页查询订单
     * 
     * @param merchantId 商家ID
     * @param pageable 分页参数
     * @return 订单分页数据
     */
    Page<Order> findByMerchantIdOrderByCreateTimeDesc(Long merchantId, Pageable pageable);
    
    /**
     * 根据商家ID和订单状态分页查询订单
     * 
     * @param merchantId 商家ID
     * @param status 订单状态
     * @param pageable 分页参数
     * @return 订单分页数据
     */
    Page<Order> findByMerchantIdAndStatusOrderByCreateTimeDesc(Long merchantId, OrderStatus status, Pageable pageable);
    
    /**
     * 统计商家各状态订单数量
     * 
     * @param merchantId 商家ID
     * @return 订单统计信息
     */
    @Query("SELECT o.status, COUNT(o) FROM Order o WHERE o.merchantId = :merchantId GROUP BY o.status")
    List<Object[]> countOrdersByMerchantIdGroupByStatus(@Param("merchantId") Long merchantId);
    
    /**
     * 统计商家订单总数
     * 
     * @param merchantId 商家ID
     * @return 订单总数
     */
    long countByMerchantId(Long merchantId);
    
    // ==================== 管理员订单查询 ====================
    
    /**
     * 分页查询所有订单（管理员）
     * 
     * @param pageable 分页参数
     * @return 订单分页数据
     */
    Page<Order> findAllByOrderByCreateTimeDesc(Pageable pageable);
    
    /**
     * 根据订单状态分页查询所有订单（管理员）
     * 
     * @param status 订单状态
     * @param pageable 分页参数
     * @return 订单分页数据
     */
    Page<Order> findByStatusOrderByCreateTimeDesc(OrderStatus status, Pageable pageable);
    
    /**
     * 根据订单号模糊查询（管理员）
     * 
     * @param orderNo 订单号
     * @param pageable 分页参数
     * @return 订单分页数据
     */
    Page<Order> findByOrderNoContainingOrderByCreateTimeDesc(String orderNo, Pageable pageable);
    
    /**
     * 统计所有订单各状态数量（管理员）
     * 
     * @return 订单统计信息
     */
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countAllOrdersGroupByStatus();
}