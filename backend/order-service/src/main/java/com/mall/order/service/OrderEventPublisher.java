package com.mall.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.order.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 订单事件发布服务
 * 负责将订单相关事件发布到消息队列
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 * 
 * 修改日志：
 * V1.0 2025-01-21：初始版本，实现订单事件发布功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${mall.rabbitmq.exchange.order:mall.order.exchange}")
    private String orderExchange;

    @Value("${mall.rabbitmq.routing-key.order-created:order.created}")
    private String orderCreatedRoutingKey;

    @Value("${mall.rabbitmq.routing-key.order-paid:order.paid}")
    private String orderPaidRoutingKey;

    @Value("${mall.rabbitmq.routing-key.order-shipped:order.shipped}")
    private String orderShippedRoutingKey;

    @Value("${mall.rabbitmq.routing-key.order-completed:order.completed}")
    private String orderCompletedRoutingKey;

    @Value("${mall.rabbitmq.routing-key.order-cancelled:order.cancelled}")
    private String orderCancelledRoutingKey;

    @Value("${mall.rabbitmq.routing-key.stock-deduction-failed:stock.deduction.failed}")
    private String stockDeductionFailedRoutingKey;

    @Value("${mall.rabbitmq.routing-key.order-timeout:order.timeout}")
    private String orderTimeoutRoutingKey;

    /**
     * 发布订单事件
     * 
     * @param event 订单事件
     */
    public void publishOrderEvent(OrderEvent event) {
        try {
            String routingKey = getRoutingKeyByEventType(event.getEventType());
            String message = objectMapper.writeValueAsString(event);
            
            log.info("发布订单事件: eventType={}, orderId={}, orderNo={}, routingKey={}", 
                    event.getEventType(), event.getOrderId(), event.getOrderNo(), routingKey);
            
            // 发送消息到RabbitMQ
            rabbitTemplate.convertAndSend(orderExchange, routingKey, message);
            
            log.info("订单事件发布成功: eventId={}, eventType={}, orderId={}", 
                    event.getEventId(), event.getEventType(), event.getOrderId());
                    
        } catch (JsonProcessingException e) {
            log.error("订单事件序列化失败: eventId={}, eventType={}, orderId={}", 
                    event.getEventId(), event.getEventType(), event.getOrderId(), e);
            throw new RuntimeException("订单事件发布失败", e);
        } catch (Exception e) {
            log.error("订单事件发布失败: eventId={}, eventType={}, orderId={}", 
                    event.getEventId(), event.getEventType(), event.getOrderId(), e);
            throw new RuntimeException("订单事件发布失败", e);
        }
    }

    /**
     * 发布订单创建事件
     * 
     * @param event 订单创建事件
     */
    public void publishOrderCreatedEvent(OrderEvent event) {
        log.info("发布订单创建事件: orderId={}, orderNo={}, userId={}", 
                event.getOrderId(), event.getOrderNo(), event.getUserId());
        publishOrderEvent(event);
    }

    /**
     * 发布订单支付事件
     * 
     * @param event 订单支付事件
     */
    public void publishOrderPaidEvent(OrderEvent event) {
        log.info("发布订单支付事件: orderId={}, orderNo={}, userId={}", 
                event.getOrderId(), event.getOrderNo(), event.getUserId());
        publishOrderEvent(event);
    }

    /**
     * 发布订单发货事件
     * 
     * @param event 订单发货事件
     */
    public void publishOrderShippedEvent(OrderEvent event) {
        log.info("发布订单发货事件: orderId={}, orderNo={}, userId={}", 
                event.getOrderId(), event.getOrderNo(), event.getUserId());
        publishOrderEvent(event);
    }

    /**
     * 发布订单完成事件
     * 
     * @param event 订单完成事件
     */
    public void publishOrderCompletedEvent(OrderEvent event) {
        log.info("发布订单完成事件: orderId={}, orderNo={}, userId={}", 
                event.getOrderId(), event.getOrderNo(), event.getUserId());
        publishOrderEvent(event);
    }

    /**
     * 发布订单取消事件
     * 
     * @param event 订单取消事件
     */
    public void publishOrderCancelledEvent(OrderEvent event) {
        log.info("发布订单取消事件: orderId={}, orderNo={}, userId={}", 
                event.getOrderId(), event.getOrderNo(), event.getUserId());
        publishOrderEvent(event);
    }

    /**
     * 发布库存扣减失败事件
     * 
     * @param event 库存扣减失败事件
     */
    public void publishStockDeductionFailedEvent(OrderEvent event) {
        log.warn("发布库存扣减失败事件: orderId={}, orderNo={}, userId={}, description={}", 
                event.getOrderId(), event.getOrderNo(), event.getUserId(), event.getDescription());
        publishOrderEvent(event);
    }

    /**
     * 发布订单超时事件
     * 
     * @param event 订单超时事件
     */
    public void publishOrderTimeoutEvent(OrderEvent event) {
        log.warn("发布订单超时事件: orderId={}, orderNo={}, userId={}", 
                event.getOrderId(), event.getOrderNo(), event.getUserId());
        publishOrderEvent(event);
    }

    /**
     * 根据事件类型获取路由键
     * 
     * @param eventType 事件类型
     * @return 路由键
     */
    private String getRoutingKeyByEventType(OrderEvent.OrderEventType eventType) {
        switch (eventType) {
            case ORDER_CREATED:
                return orderCreatedRoutingKey;
            case ORDER_PAID:
                return orderPaidRoutingKey;
            case ORDER_SHIPPED:
                return orderShippedRoutingKey;
            case ORDER_COMPLETED:
                return orderCompletedRoutingKey;
            case ORDER_CANCELLED:
                return orderCancelledRoutingKey;
            case STOCK_DEDUCTION_FAILED:
                return stockDeductionFailedRoutingKey;
            case ORDER_TIMEOUT:
                return orderTimeoutRoutingKey;
            default:
                log.warn("未知的订单事件类型: {}", eventType);
                return "order.unknown";
        }
    }
}