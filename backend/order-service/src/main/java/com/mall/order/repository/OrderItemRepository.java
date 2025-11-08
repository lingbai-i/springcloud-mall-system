package com.mall.order.repository;

import com.mall.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 订单项数据访问接口
 * 提供订单项相关的数据库操作方法
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * 根据订单ID查询订单项列表
     * 
     * @param orderId 订单ID
     * @return 订单项列表
     */
    List<OrderItem> findByOrderId(Long orderId);
    
    /**
     * 根据订单ID列表查询订单项列表
     * 
     * @param orderIds 订单ID列表
     * @return 订单项列表
     */
    List<OrderItem> findByOrderIdIn(List<Long> orderIds);
    
    /**
     * 根据商品ID查询订单项列表
     * 用于统计商品销量
     * 
     * @param productId 商品ID
     * @return 订单项列表
     */
    List<OrderItem> findByProductId(Long productId);
    
    /**
     * 统计商品销量
     * 
     * @param productId 商品ID
     * @return 销量总数
     */
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.productId = :productId")
    Long sumQuantityByProductId(@Param("productId") Long productId);
    
    /**
     * 统计订单项总数
     * 
     * @param orderId 订单ID
     * @return 订单项总数
     */
    long countByOrderId(Long orderId);
    
    /**
     * 删除指定订单的所有订单项
     * 
     * @param orderId 订单ID
     */
    void deleteByOrderId(Long orderId);
}