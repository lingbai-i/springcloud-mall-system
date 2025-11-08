package com.mall.order.service;

import com.mall.order.entity.Order;
import com.mall.order.enums.OrderStatus;
import com.mall.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 缓存预热服务
 * 负责在系统启动时预热常用的缓存数据
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 * 
 * 修改日志：
 * V1.0 2025-01-21：初始版本，实现订单缓存预热功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheWarmupService {
    
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final CacheManager cacheManager;
    
    /**
     * 异步预热用户订单缓存
     * 预热活跃用户的订单数据到缓存中
     * 
     * @param userIds 需要预热的用户ID列表
     * @return 预热任务的CompletableFuture
     */
    @Async
    public CompletableFuture<Void> warmupUserOrdersCache(List<Long> userIds) {
        log.info("开始预热用户订单缓存，用户数量: {}", userIds.size());
        
        try {
            for (Long userId : userIds) {
                // 预热用户订单列表（第一页）
                Pageable pageable = PageRequest.of(0, 10);
                Page<Order> orders = orderService.getUserOrders(userId, pageable);
                
                // 预热各状态的订单列表
                for (OrderStatus status : OrderStatus.values()) {
                    orderService.getUserOrdersByStatus(userId, status, pageable);
                }
                
                // 预热最近的订单详情
                if (!orders.isEmpty()) {
                    Order latestOrder = orders.getContent().get(0);
                    orderService.getOrderById(latestOrder.getId(), userId);
                    orderService.getOrderByOrderNo(latestOrder.getOrderNo());
                }
                
                log.debug("用户 {} 的订单缓存预热完成", userId);
            }
            
            log.info("用户订单缓存预热完成，共预热 {} 个用户", userIds.size());
            
        } catch (Exception e) {
            log.error("用户订单缓存预热失败", e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * 异步预热热门订单缓存
     * 预热最近创建的订单数据
     * 
     * @param limit 预热的订单数量限制
     * @return 预热任务的CompletableFuture
     */
    @Async
    public CompletableFuture<Void> warmupRecentOrdersCache(int limit) {
        log.info("开始预热最近订单缓存，限制数量: {}", limit);
        
        try {
            Pageable pageable = PageRequest.of(0, limit);
            
            // 获取最近的订单
            List<Order> recentOrders = orderRepository.findAll(pageable).getContent();
            
            for (Order order : recentOrders) {
                // 预热订单详情缓存
                orderService.getOrderById(order.getId(), order.getUserId());
                orderService.getOrderByOrderNo(order.getOrderNo());
            }
            
            log.info("最近订单缓存预热完成，共预热 {} 个订单", recentOrders.size());
            
        } catch (Exception e) {
            log.error("最近订单缓存预热失败", e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * 清空所有订单相关缓存
     * 用于缓存重置或系统维护
     */
    public void clearAllOrderCaches() {
        log.info("开始清空所有订单相关缓存");
        
        try {
            // 清空订单缓存
            if (cacheManager.getCache("order") != null) {
                cacheManager.getCache("order").clear();
                log.info("订单详情缓存已清空");
            }
            
            // 清空用户订单列表缓存
            if (cacheManager.getCache("userOrders") != null) {
                cacheManager.getCache("userOrders").clear();
                log.info("用户订单列表缓存已清空");
            }
            
            log.info("所有订单相关缓存清空完成");
            
        } catch (Exception e) {
            log.error("清空订单缓存失败", e);
        }
    }
    
    /**
     * 获取缓存统计信息
     * 返回各个缓存的统计数据
     * 
     * @return 缓存统计信息
     */
    public String getCacheStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("订单缓存统计信息:\n");
        
        try {
            // 订单详情缓存统计
            if (cacheManager.getCache("order") != null) {
                stats.append("- 订单详情缓存: 已启用\n");
            }
            
            // 用户订单列表缓存统计
            if (cacheManager.getCache("userOrders") != null) {
                stats.append("- 用户订单列表缓存: 已启用\n");
            }
            
        } catch (Exception e) {
            log.error("获取缓存统计信息失败", e);
            stats.append("- 获取缓存统计信息失败: ").append(e.getMessage());
        }
        
        return stats.toString();
    }
}