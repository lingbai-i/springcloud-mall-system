package com.mall.order.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 订单事件发布器
 * 负责发布订单相关事件到RabbitMQ
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    
    private static final String EXCHANGE_NAME = "order.exchange";
    
    /**
     * 发布订单创建事件
     */
    public void publishOrderCreatedEvent(OrderEvent event) {
        publishEvent(event, OrderEventType.ORDER_CREATED.getRoutingKey());
    }
    
    /**
     * 发布订单支付事件
     */
    public void publishOrderPaidEvent(OrderEvent event) {
        publishEvent(event, OrderEventType.ORDER_PAID.getRoutingKey());
    }
    
    /**
     * 发布订单发货事件
     */
    public void publishOrderShippedEvent(OrderEvent event) {
        publishEvent(event, OrderEventType.ORDER_SHIPPED.getRoutingKey());
    }
    
    /**
     * 发布订单完成事件
     */
    public void publishOrderCompletedEvent(OrderEvent event) {
        publishEvent(event, OrderEventType.ORDER_COMPLETED.getRoutingKey());
    }
    
    /**
     * 发布订单取消事件
     */
    public void publishOrderCancelledEvent(OrderEvent event) {
        publishEvent(event, OrderEventType.ORDER_CANCELLED.getRoutingKey());
    }
    
    /**
     * 发布订单超时事件
     */
    public void publishOrderTimeoutEvent(OrderEvent event) {
        publishEvent(event, OrderEventType.ORDER_TIMEOUT.getRoutingKey());
    }
    
    /**
     * 发布事件到RabbitMQ
     */
    private void publishEvent(OrderEvent event, String routingKey) {
        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, routingKey, message);
            log.info("订单事件发布成功: eventType={}, orderNo={}", 
                    event.getEventType(), event.getOrderNo());
        } catch (JsonProcessingException e) {
            log.error("订单事件序列化失败: eventType={}, orderNo={}", 
                    event.getEventType(), event.getOrderNo(), e);
        } catch (Exception e) {
            log.error("订单事件发布失败: eventType={}, orderNo={}", 
                    event.getEventType(), event.getOrderNo(), e);
        }
    }
}
