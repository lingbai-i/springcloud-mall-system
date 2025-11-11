package com.mall.order.utils;

/**
 * 缓存键生成工具类
 * 统一管理订单服务的缓存键格式
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
public class CacheKeyUtils {
    
    private static final String ORDER_DETAIL_KEY = "order";
    private static final String USER_ORDERS_KEY = "userOrders";
    private static final String ORDER_NO_PREFIX = "orderNo_";
    
    /**
     * 生成订单详情缓存键
     * 格式：order:orderId_userId
     * 
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 缓存键
     */
    public static String getOrderDetailKey(Long orderId, Long userId) {
        return ORDER_DETAIL_KEY + ":" + orderId + "_" + userId;
    }
    
    /**
     * 生成订单号查询缓存键
     * 格式：order:orderNo_订单号
     * 
     * @param orderNo 订单号
     * @return 缓存键
     */
    public static String getOrderByNoKey(String orderNo) {
        return ORDER_DETAIL_KEY + ":" + ORDER_NO_PREFIX + orderNo;
    }
    
    /**
     * 生成用户订单列表缓存键
     * 格式：userOrders:userId_page_size
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 缓存键
     */
    public static String getUserOrdersKey(Long userId, int page, int size) {
        return USER_ORDERS_KEY + ":" + userId + "_" + page + "_" + size;
    }
    
    /**
     * 生成用户订单状态列表缓存键
     * 格式：userOrders:userId_status_page_size
     * 
     * @param userId 用户ID
     * @param status 订单状态
     * @param page 页码
     * @param size 每页大小
     * @return 缓存键
     */
    public static String getUserOrdersByStatusKey(Long userId, String status, int page, int size) {
        return USER_ORDERS_KEY + ":" + userId + "_" + status + "_" + page + "_" + size;
    }
    
    /**
     * 获取订单缓存值名称（用于@CacheEvict等注解）
     * 
     * @return 缓存值名称
     */
    public static String getOrderCacheName() {
        return ORDER_DETAIL_KEY;
    }
    
    /**
     * 获取用户订单列表缓存值名称（用于@CacheEvict等注解）
     * 
     * @return 缓存值名称
     */
    public static String getUserOrdersCacheName() {
        return USER_ORDERS_KEY;
    }
}
