package com.mall.order.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.mall.common.core.domain.R;
import com.mall.order.client.CartClient;
import com.mall.order.client.PaymentClient;
import com.mall.order.client.ProductClient;
import com.mall.order.dto.CreateOrderRequest;
import com.mall.order.entity.Order;
import com.mall.order.entity.OrderItem;
import com.mall.order.enums.OrderStatus;
import com.mall.order.event.OrderEvent;
import com.mall.order.event.OrderEventPublisher;
import com.mall.order.event.OrderEventType;
import com.mall.order.exception.InsufficientStockException;
import com.mall.order.exception.OrderException;
import com.mall.order.exception.OrderNotFoundException;
import com.mall.order.exception.OrderPermissionException;
import com.mall.order.exception.OrderStatusException;
import com.mall.order.metrics.OrderMetricsService;
import com.mall.order.repository.OrderItemRepository;
import com.mall.order.repository.OrderRepository;
import com.mall.order.service.DistributedLockService;
import com.mall.order.service.OrderService;
import com.mall.order.service.OrderValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 * 实现订单相关的业务逻辑
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductClient productClient;
    private final CartClient cartClient;
    private final PaymentClient paymentClient;
    private final OrderEventPublisher orderEventPublisher;
    private final DistributedLockService distributedLockService;
    private final OrderMetricsService orderMetricsService;
    private final OrderValidator orderValidator;

    @Value("${order.timeout-minutes:30}")
    private Integer orderTimeoutMinutes;

    @Value("${order.auto-confirm-days:7}")
    private Integer autoConfirmDays;

    @Value("${order.number-prefix:ORD}")
    private String orderNumberPrefix;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(CreateOrderRequest request) {
        log.info("开始创建订单，用户ID: {}", request.getUserId());

        // 获取分布式锁，防止用户重复创建订单
        String[] lockInfo = distributedLockService.getOrderCreateLock(request.getUserId());
        String lockKey = lockInfo[0];
        String lockValue = lockInfo[1];

        return distributedLockService.executeWithLock(lockKey, lockValue, 10L, () -> {
            return doCreateOrder(request);
        });
    }

    /**
     * 执行订单创建的具体逻辑
     * 
     * @param request 创建订单请求
     * @return 创建的订单
     */
    private Order doCreateOrder(CreateOrderRequest request) {

        // 记录订单创建开始时间
        long startTime = System.currentTimeMillis();

        try {
            // 1. 验证订单项不为空
            if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
                throw new IllegalArgumentException("订单项不能为空");
            }

            // 2. 获取商品信息并验证库存
            List<Long> productIds = request.getOrderItems().stream()
                    .map(CreateOrderRequest.OrderItemRequest::getProductId)
                    .toList();

            R<List<Map<String, Object>>> productsResponse = productClient.getProductsBatch(productIds);
            if (!productsResponse.isSuccess() || productsResponse.getData() == null) {
                throw new IllegalArgumentException("获取商品信息失败: " + productsResponse.getMessage());
            }
            List<Map<String, Object>> products = productsResponse.getData();
            if (products.size() != productIds.size()) {
                throw new IllegalArgumentException("部分商品不存在");
            }

            // 3. 验证库存充足
            for (CreateOrderRequest.OrderItemRequest item : request.getOrderItems()) {
                Boolean stockSufficient = productClient.checkStock(item.getProductId(), item.getQuantity());
                if (!stockSufficient) {
                    throw new InsufficientStockException(item.getProductId());
                }
            }

            // 4. 创建订单
            Order order = new Order();
            order.setOrderNo(generateOrderNo());
            order.setUserId(request.getUserId());
            order.setStatus(OrderStatus.PENDING);
            order.setReceiverName(request.getReceiverName());
            order.setReceiverPhone(request.getReceiverPhone());
            order.setReceiverAddress(request.getReceiverAddress());
            order.setShippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO);
            order.setDiscountAmount(
                    request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
            order.setRemark(request.getRemark());

            // 5. 计算订单金额
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();

            for (CreateOrderRequest.OrderItemRequest itemRequest : request.getOrderItems()) {
                // 获取商品详情
                Map<String, Object> product = products.stream()
                        .filter(p -> Objects.equals(p.get("id"), itemRequest.getProductId()))
                        .findFirst()
                        .orElseThrow(() -> new OrderNotFoundException("商品不存在: " + itemRequest.getProductId()));

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProductId(itemRequest.getProductId());
                orderItem.setProductName((String) product.get("name"));
                orderItem.setProductImage((String) product.get("image"));
                orderItem.setProductSpec(itemRequest.getProductSpec());
                orderItem.setProductPrice(new BigDecimal(product.get("price").toString()));
                orderItem.setQuantity(itemRequest.getQuantity());

                // 计算小计
                BigDecimal subtotal = orderItem.getProductPrice().multiply(new BigDecimal(itemRequest.getQuantity()));
                orderItem.setSubtotal(subtotal);
                totalAmount = totalAmount.add(subtotal);

                orderItems.add(orderItem);
            }

            // 6. 设置订单金额
            order.setProductAmount(totalAmount);
            order.setTotalAmount(totalAmount.add(order.getShippingFee()).subtract(order.getDiscountAmount()));
            order.setPayableAmount(order.getTotalAmount());
            order.setOrderItems(orderItems);

            // 7. 保存订单
            Order savedOrder = orderRepository.save(order);
            log.info("订单创建成功: orderId={}, orderNo={}, userId={}, totalAmount={}",
                    savedOrder.getId(), savedOrder.getOrderNo(), request.getUserId(), totalAmount);

            // 发布订单创建事件
            try {
                OrderEvent orderCreatedEvent = OrderEvent.createOrderCreatedEvent(
                        savedOrder.getId(), savedOrder.getOrderNo(), request.getUserId(), totalAmount);
                orderEventPublisher.publishOrderCreatedEvent(orderCreatedEvent);
                log.debug("订单创建事件发布成功: orderId={}", savedOrder.getId());
            } catch (Exception e) {
                log.error("发布订单创建事件失败: orderId={}, error={}", savedOrder.getId(), e.getMessage(), e);
                // 不影响主流程，继续执行
            }

            // 8. 扣减库存
            for (CreateOrderRequest.OrderItemRequest item : request.getOrderItems()) {
                Map<String, Object> stockRequest = new HashMap<>();
                stockRequest.put("productId", item.getProductId());
                stockRequest.put("quantity", item.getQuantity());
                stockRequest.put("orderNo", savedOrder.getOrderNo());

                Boolean deductResult = productClient.deductStock(stockRequest);
                if (!deductResult) {
                    throw new OrderException("库存扣减失败，商品ID: " + item.getProductId());
                }
            }

            // 9. 清空购物车中的选中商品
            try {
                cartClient.clearSelectedItems(request.getUserId());
            } catch (Exception e) {
                log.warn("清空购物车失败，用户ID: {}", request.getUserId(), e);
            }

            // 记录订单创建成功指标
            orderMetricsService.recordOrderCreated(totalAmount);
            orderMetricsService.recordOrderCreateTime(System.currentTimeMillis() - startTime);

            return savedOrder;

        } catch (OrderException e) {
            // 业务异常直接抛出
            log.error("创建订单失败，用户ID: {}, 错误: {}", request.getUserId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("创建订单失败，用户ID: {}", request.getUserId(), e);
            throw new OrderException("创建订单失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(value = "order", key = "#orderId + '_' + #userId", unless = "#result == null")
    public Order getOrderById(Long orderId, Long userId) {
        log.info("获取订单详情，订单ID: {}, 用户ID: {}", orderId, userId);

        try {
            // 1. 根据订单ID查询订单
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException(orderId));

            // 2. 验证订单所有者
            orderValidator.validateOrderOwner(order, userId);

            return order;

        } catch (OrderException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取订单详情失败，订单ID: {}", orderId, e);
            throw new OrderException("获取订单详情失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(value = "order", key = "'orderNo_' + #orderNo", unless = "#result == null")
    public Order getOrderByOrderNo(String orderNo) {
        log.info("根据订单号获取订单详情，订单号: {}", orderNo);

        return orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new OrderNotFoundException(orderNo));
    }

    @Override
    // 移除 @Cacheable 注解，避免 Page 对象的 Redis 序列化问题
    public Page<Order> getUserOrders(Long userId, Pageable pageable) {
        log.info("分页查询用户订单，用户ID: {}, 页码: {}, 页大小: {}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        return orderRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable);
    }

    @Override
    public Page<Order> getUserOrdersByStatus(Long userId, OrderStatus status, Pageable pageable) {
        log.info("根据状态分页查询用户订单，用户ID: {}, 状态: {}", userId, status);
        return orderRepository.findByUserIdAndStatusOrderByCreateTimeDesc(userId, status, pageable);
    }

    /**
     * 生成订单号
     * 格式：前缀 + 时间戳 + 随机数
     * 
     * @return 订单号
     */
    private String generateOrderNo() {
        return orderNumberPrefix + System.currentTimeMillis() + IdUtil.randomUUID().substring(0, 6).toUpperCase();
    }

    /**
     * 根据订单状态获取对应的事件类型
     * 
     * @param status 订单状态
     * @return 事件类型
     */
    private OrderEventType getEventTypeByStatus(OrderStatus status) {
        switch (status) {
            case PENDING:
                return OrderEventType.ORDER_CREATED;
            case PAID:
                return OrderEventType.ORDER_PAID;
            case SHIPPED:
                return OrderEventType.ORDER_SHIPPED;
            case COMPLETED:
                return OrderEventType.ORDER_COMPLETED;
            case CANCELLED:
                return OrderEventType.ORDER_CANCELLED;
            default:
                return OrderEventType.ORDER_CREATED;
        }
    }

    /**
     * 根据订单状态发布相应的事件
     * 
     * @param event  订单事件
     * @param status 订单状态
     */
    private void publishEventByStatus(OrderEvent event, OrderStatus status) {
        switch (status) {
            case PENDING:
                orderEventPublisher.publishOrderCreatedEvent(event);
                break;
            case PAID:
                orderEventPublisher.publishOrderPaidEvent(event);
                break;
            case SHIPPED:
                orderEventPublisher.publishOrderShippedEvent(event);
                break;
            case COMPLETED:
                orderEventPublisher.publishOrderCompletedEvent(event);
                break;
            case CANCELLED:
                orderEventPublisher.publishOrderCancelledEvent(event);
                break;
            default:
                log.warn("未知的订单状态，无法发布事件: status={}", status);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = { "order", "userOrders" }, allEntries = true)
    public Boolean cancelOrder(Long orderId, Long userId, String reason) {
        log.info("取消订单，订单ID: {}, 用户ID: {}, 原因: {}", orderId, userId, reason);

        // 获取分布式锁，防止订单重复取消
        String[] lockInfo = distributedLockService.getOrderCancelLock(orderId);
        String lockKey = lockInfo[0];
        String lockValue = lockInfo[1];

        return distributedLockService.executeWithLock(lockKey, lockValue, 5L, () -> {
            return doCancelOrder(orderId, userId, reason);
        });
    }

    /**
     * 执行订单取消的具体逻辑
     * 
     * @param orderId 订单ID
     * @param userId  用户ID
     * @param reason  取消原因
     * @return 取消结果
     */
    private Boolean doCancelOrder(Long orderId, Long userId, String reason) {
        log.info("执行订单取消，订单ID: {}, 用户ID: {}, 原因: {}", orderId, userId, reason);

        try {
            // 1. 获取订单信息
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

            // 2. 验证订单所有者和状态
            orderValidator.validateOrderOwner(order, userId);
            orderValidator.validateCancellable(order);

            // 4. 更新订单状态
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelReason(reason);
            order.setCancelTime(LocalDateTime.now());
            orderRepository.save(order);

            // 5. 恢复库存
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
            for (OrderItem item : orderItems) {
                Map<String, Object> restoreRequest = new HashMap<>();
                restoreRequest.put("productId", item.getProductId());
                restoreRequest.put("quantity", item.getQuantity());
                restoreRequest.put("orderNo", order.getOrderNo());

                try {
                    productClient.restoreStock(restoreRequest);
                } catch (Exception e) {
                    log.warn("恢复库存失败，商品ID: {}", item.getProductId(), e);
                }
            }

            // 6. 如果已付款，需要申请退款
            if (order.getStatus() == OrderStatus.PAID) {
                try {
                    Map<String, Object> refundRequest = new HashMap<>();
                    refundRequest.put("orderNo", order.getOrderNo());
                    refundRequest.put("amount", order.getPayableAmount());
                    refundRequest.put("reason", "订单取消");

                    paymentClient.refund(refundRequest);
                } catch (Exception e) {
                    log.error("申请退款失败，订单号: {}", order.getOrderNo(), e);
                }
            }

            // 7. 发布订单取消事件
            try {
                OrderEvent orderCancelledEvent = OrderEvent.createOrderCancelledEvent(
                        orderId, order.getOrderNo(), userId, reason);
                orderEventPublisher.publishOrderCancelledEvent(orderCancelledEvent);
                log.debug("订单取消事件发布成功: orderId={}", orderId);
            } catch (Exception e) {
                log.error("发布订单取消事件失败: orderId={}, error={}", orderId, e.getMessage(), e);
                // 不影响主流程，继续执行
            }

            log.info("订单取消成功，订单号: {}", order.getOrderNo());
            return true;

        } catch (OrderException e) {
            throw e;
        } catch (Exception e) {
            log.error("取消订单失败，订单ID: {}", orderId, e);
            throw new OrderException("取消订单失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = { "order", "userOrders" }, allEntries = true)
    public Boolean confirmOrder(Long orderId, Long userId) {
        log.info("确认收货，订单ID: {}, 用户ID: {}", orderId, userId);

        try {
            // 1. 获取订单信息
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

            // 2. 验证订单所有者和状态
            orderValidator.validateOrderOwner(order, userId);
            orderValidator.validateConfirmable(order);

            // 4. 更新订单状态
            order.setStatus(OrderStatus.COMPLETED);
            order.setConfirmTime(LocalDateTime.now());
            orderRepository.save(order);

            // 5. 发布订单完成事件
            try {
                OrderEvent orderCompletedEvent = OrderEvent.createOrderCompletedEvent(
                        orderId, order.getOrderNo(), userId);
                orderEventPublisher.publishOrderCompletedEvent(orderCompletedEvent);
                log.debug("订单完成事件发布成功: orderId={}", orderId);
            } catch (Exception e) {
                log.error("发布订单完成事件失败: orderId={}, error={}", orderId, e.getMessage(), e);
                // 不影响主流程，继续执行
            }

            log.info("确认收货成功，订单号: {}", order.getOrderNo());
            return true;

        } catch (OrderException e) {
            throw e;
        } catch (Exception e) {
            log.error("确认收货失败，订单ID: {}", orderId, e);
            throw new OrderException("确认收货失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean applyRefund(Long orderId, Long userId, String reason) {
        log.info("申请退款，订单ID: {}, 用户ID: {}, 原因: {}", orderId, userId, reason);

        try {
            // 1. 获取订单信息
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

            // 2. 验证订单所有者和状态
            orderValidator.validateOrderOwner(order, userId);
            orderValidator.validateRefundable(order);

            // 4. 更新订单状态
            order.setStatus(OrderStatus.REFUND_PENDING);
            order.setRefundReason(reason);
            order.setRefundApplyTime(LocalDateTime.now());
            orderRepository.save(order);

            // 5. 调用支付服务申请退款
            try {
                Map<String, Object> refundRequest = new HashMap<>();
                refundRequest.put("orderNo", order.getOrderNo());
                refundRequest.put("amount", order.getPayableAmount());
                refundRequest.put("reason", reason);

                paymentClient.refund(refundRequest);
            } catch (Exception e) {
                log.error("调用支付服务申请退款失败，订单号: {}", order.getOrderNo(), e);
                // 回滚订单状态
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);
                throw new OrderException("申请退款失败，请稍后重试");
            }

            log.info("申请退款成功，订单号: {}", order.getOrderNo());
            return true;

        } catch (OrderException e) {
            throw e;
        } catch (Exception e) {
            log.error("申请退款失败，订单ID: {}", orderId, e);
            throw new OrderException("申请退款失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> payOrder(Long orderId, Long userId, String paymentMethod) {
        log.info("订单支付，订单ID: {}, 用户ID: {}, 支付方式: {}", orderId, userId, paymentMethod);

        try {
            // 1. 获取订单信息
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

            // 2. 验证订单所有者和状态
            orderValidator.validateOrderOwner(order, userId);
            orderValidator.validatePayable(order);

            // 4. 调用支付服务创建支付订单
            Map<String, Object> paymentRequest = new HashMap<>();
            paymentRequest.put("orderNo", order.getOrderNo());
            paymentRequest.put("amount", order.getPayableAmount());
            paymentRequest.put("paymentMethod", paymentMethod);
            paymentRequest.put("userId", userId);
            paymentRequest.put("description", "商城订单支付");

            Map<String, Object> paymentResult = paymentClient.createPayment(paymentRequest);

            log.info("创建支付订单成功，订单号: {}", order.getOrderNo());
            return paymentResult;

        } catch (OrderException e) {
            throw e;
        } catch (Exception e) {
            log.error("订单支付失败，订单ID: {}", orderId, e);
            throw new OrderException("订单支付失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean handlePaymentSuccess(String orderNo, String paymentId) {
        log.info("处理支付成功回调，订单号: {}, 支付ID: {}", orderNo, paymentId);

        // 获取分布式锁，防止重复支付处理
        String[] lockInfo = distributedLockService.getOrderPaymentLock(orderNo);
        String lockKey = lockInfo[0];
        String lockValue = lockInfo[1];

        return distributedLockService.executeWithLock(lockKey, lockValue, 5L, () -> {
            return doHandlePaymentSuccess(orderNo, paymentId);
        });
    }

    /**
     * 执行支付成功处理的具体逻辑
     * 
     * @param orderNo   订单号
     * @param paymentId 支付ID
     * @return 处理结果
     */
    private Boolean doHandlePaymentSuccess(String orderNo, String paymentId) {
        log.info("执行支付成功处理，订单号: {}, 支付ID: {}", orderNo, paymentId);

        try {
            // 1. 获取订单信息
            Order order = orderRepository.findByOrderNo(orderNo)
                    .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

            // 2. 验证订单状态
            if (order.getStatus() != OrderStatus.PENDING) {
                log.warn("订单状态异常，当前状态: {}, 订单号: {}", order.getStatus(), orderNo);
                return false;
            }

            // 3. 更新订单状态
            order.setStatus(OrderStatus.PAID);
            order.setPaymentId(paymentId);
            order.setPayTime(LocalDateTime.now());
            orderRepository.save(order);

            // 4. 发布订单支付成功事件
            try {
                OrderEvent orderPaidEvent = OrderEvent.createOrderPaidEvent(
                        order.getId(), orderNo, order.getUserId(), order.getPayableAmount());
                orderEventPublisher.publishOrderPaidEvent(orderPaidEvent);
                log.debug("订单支付成功事件发布成功: orderNo={}", orderNo);
            } catch (Exception e) {
                log.error("发布订单支付成功事件失败: orderNo={}, error={}", orderNo, e.getMessage(), e);
                // 不影响主流程，继续执行
            }

            log.info("订单支付成功，订单号: {}", orderNo);
            return true;

        } catch (Exception e) {
            log.error("处理支付成功回调失败，订单号: {}", orderNo, e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getOrderLogistics(Long orderId, Long userId) {
        log.info("获取订单物流信息，订单ID: {}, 用户ID: {}", orderId, userId);

        try {
            // 1. 获取订单信息
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

            // 2. 验证订单所有者和状态
            orderValidator.validateOrderOwner(order, userId);
            orderValidator.validateShipped(order);

            // 4. 构造物流信息（这里是模拟数据，实际应该调用物流服务）
            Map<String, Object> logistics = new HashMap<>();
            logistics.put("orderNo", order.getOrderNo());
            logistics.put("trackingNo", order.getTrackingNo());
            logistics.put("logisticsCompany", order.getLogisticsCompany());
            logistics.put("shipTime", order.getShipTime());

            // 模拟物流轨迹
            List<Map<String, Object>> tracks = new ArrayList<>();
            Map<String, Object> track1 = new HashMap<>();
            track1.put("time", order.getShipTime());
            track1.put("status", "已发货");
            track1.put("description", "商品已从仓库发出");
            tracks.add(track1);

            if (order.getStatus() == OrderStatus.COMPLETED) {
                Map<String, Object> track2 = new HashMap<>();
                track2.put("time", order.getConfirmTime());
                track2.put("status", "已签收");
                track2.put("description", "商品已签收");
                tracks.add(track2);
            }

            logistics.put("tracks", tracks);

            return logistics;

        } catch (Exception e) {
            log.error("获取订单物流信息失败，订单ID: {}", orderId, e);
            throw new RuntimeException("获取物流信息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order reorder(Long orderId, Long userId) {
        log.info("重新购买，订单ID: {}, 用户ID: {}", orderId, userId);

        try {
            // 1. 获取原订单信息
            Order originalOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("原订单不存在"));

            // 2. 验证订单所有者
            if (!originalOrder.getUserId().equals(userId)) {
                throw new IllegalArgumentException("无权限操作此订单");
            }

            // 3. 获取原订单的商品项
            List<OrderItem> originalItems = orderItemRepository.findByOrderId(orderId);
            if (originalItems.isEmpty()) {
                throw new IllegalArgumentException("原订单无商品信息");
            }

            // 4. 构造新订单请求
            CreateOrderRequest request = new CreateOrderRequest();
            request.setUserId(userId);
            request.setReceiverName(originalOrder.getReceiverName());
            request.setReceiverPhone(originalOrder.getReceiverPhone());
            request.setReceiverAddress(originalOrder.getReceiverAddress());
            request.setShippingFee(originalOrder.getShippingFee());

            // 5. 构造订单项
            List<CreateOrderRequest.OrderItemRequest> itemRequests = new ArrayList<>();
            for (OrderItem item : originalItems) {
                CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest();
                itemRequest.setProductId(item.getProductId());
                itemRequest.setProductSpec(item.getProductSpec());
                itemRequest.setQuantity(item.getQuantity());
                itemRequests.add(itemRequest);
            }
            request.setOrderItems(itemRequests);

            // 6. 创建新订单
            Order newOrder = createOrder(request);

            log.info("重新购买成功，新订单号: {}", newOrder.getOrderNo());
            return newOrder;

        } catch (OrderException e) {
            throw e;
        } catch (Exception e) {
            log.error("重新购买失败，订单ID: {}", orderId, e);
            throw new OrderException("重新购买失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getOrderStats(Long userId) {
        log.info("获取用户订单统计，用户ID: {}", userId);

        try {
            Map<String, Object> stats = new HashMap<>();

            // 1. 统计各状态订单数量
            stats.put("pendingPayment", orderRepository.countByUserIdAndStatus(userId, OrderStatus.PENDING));
            stats.put("paid", orderRepository.countByUserIdAndStatus(userId, OrderStatus.PAID));
            stats.put("shipped", orderRepository.countByUserIdAndStatus(userId, OrderStatus.SHIPPED));
            stats.put("completed", orderRepository.countByUserIdAndStatus(userId, OrderStatus.COMPLETED));
            stats.put("cancelled", orderRepository.countByUserIdAndStatus(userId, OrderStatus.CANCELLED));
            stats.put("refunded", orderRepository.countByUserIdAndStatus(userId, OrderStatus.REFUNDED));

            // 2. 统计总订单数和总消费金额
            stats.put("totalOrders", orderRepository.countByUserId(userId));
            BigDecimal totalAmount = orderRepository.sumPayableAmountByUserIdAndStatus(userId, OrderStatus.COMPLETED);
            stats.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);

            return stats;

        } catch (Exception e) {
            log.error("获取用户订单统计失败，用户ID: {}", userId, e);
            throw new RuntimeException("获取订单统计失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer handleTimeoutOrders() {
        log.info("开始处理超时订单");

        try {
            // 计算超时时间点（当前时间减去超时分钟数）
            LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(orderTimeoutMinutes);

            // 查询超时的待付款订单
            List<Order> timeoutOrders = orderRepository.findByStatusAndCreateTimeBefore(OrderStatus.PENDING,
                    timeoutTime);

            int processedCount = 0;
            for (Order order : timeoutOrders) {
                try {
                    // 取消订单
                    order.setStatus(OrderStatus.CANCELLED);
                    order.setCancelTime(LocalDateTime.now());
                    order.setCancelReason("订单超时自动取消");
                    orderRepository.save(order);

                    // 恢复库存
                    List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
                    for (OrderItem item : orderItems) {
                        try {
                            Map<String, Object> restoreRequest = new HashMap<>();
                            restoreRequest.put("productId", item.getProductId());
                            restoreRequest.put("quantity", item.getQuantity());
                            restoreRequest.put("orderNo", order.getOrderNo());
                            productClient.restoreStock(restoreRequest);
                        } catch (Exception e) {
                            log.error("恢复库存失败，商品ID: {}, 数量: {}",
                                    item.getProductId(), item.getQuantity(), e);
                        }
                    }

                    processedCount++;
                    log.info("订单超时自动取消成功，订单号: {}", order.getOrderNo());

                } catch (Exception e) {
                    log.error("处理超时订单失败，订单号: {}", order.getOrderNo(), e);
                }
            }

            log.info("处理超时订单完成，共处理 {} 个订单", processedCount);
            return processedCount;

        } catch (Exception e) {
            log.error("处理超时订单异常", e);
            return 0;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer autoConfirmOrders() {
        log.info("开始自动确认收货");

        try {
            // 计算自动确认时间点（当前时间减去自动确认天数）
            LocalDateTime autoConfirmTime = LocalDateTime.now().minusDays(autoConfirmDays);

            // 查询需要自动确认的已发货订单
            List<Order> ordersToConfirm = orderRepository.findByStatusAndShipTimeBefore(OrderStatus.SHIPPED,
                    autoConfirmTime);

            int processedCount = 0;
            for (Order order : ordersToConfirm) {
                try {
                    // 自动确认收货
                    order.setStatus(OrderStatus.COMPLETED);
                    order.setConfirmTime(LocalDateTime.now());
                    orderRepository.save(order);

                    processedCount++;
                    log.info("订单自动确认收货成功，订单号: {}", order.getOrderNo());

                } catch (Exception e) {
                    log.error("自动确认收货失败，订单号: {}", order.getOrderNo(), e);
                }
            }

            log.info("自动确认收货完成，共处理 {} 个订单", processedCount);
            return processedCount;

        } catch (Exception e) {
            log.error("自动确认收货异常", e);
            return 0;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = { "order", "userOrders" }, allEntries = true)
    public Boolean updateOrderStatus(Long orderId, OrderStatus status) {
        log.info("更新订单状态，订单ID: {}, 新状态: {}", orderId, status);

        // 获取分布式锁，防止订单状态并发更新
        String[] lockInfo = distributedLockService.getOrderStatusLock(orderId);
        String lockKey = lockInfo[0];
        String lockValue = lockInfo[1];

        return distributedLockService.executeWithLock(lockKey, lockValue, 5L, () -> {
            return doUpdateOrderStatus(orderId, status);
        });
    }

    /**
     * 执行订单状态更新的具体逻辑
     * 
     * @param orderId 订单ID
     * @param status  新状态
     * @return 更新结果
     */
    private Boolean doUpdateOrderStatus(Long orderId, OrderStatus status) {
        log.info("执行订单状态更新，订单ID: {}, 新状态: {}", orderId, status);

        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

            OrderStatus oldStatus = order.getStatus();
            order.setStatus(status);

            // 根据状态更新相应的时间字段
            LocalDateTime now = LocalDateTime.now();
            switch (status) {
                case PAID:
                    order.setPayTime(now);
                    // 记录支付成功指标
                    orderMetricsService.recordOrderPaid(order.getTotalAmount());
                    break;
                case SHIPPED:
                    order.setShipTime(now);
                    break;
                case COMPLETED:
                    order.setConfirmTime(now);
                    // 记录订单完成指标
                    orderMetricsService.recordOrderCompleted(order.getTotalAmount());
                    break;
                case CANCELLED:
                    order.setCancelTime(now);
                    // 记录订单取消指标
                    orderMetricsService.recordOrderCancelled();
                    break;
                case REFUNDED:
                    order.setRefundTime(now);
                    break;
            }

            orderRepository.save(order);

            // 发布订单状态变更事件
            try {
                // 根据新状态发布相应事件
                OrderEvent event = OrderEvent.builder()
                        .eventType(getEventTypeByStatus(status))
                        .orderId(orderId)
                        .orderNo(order.getOrderNo())
                        .userId(order.getUserId())
                        .eventTime(LocalDateTime.now())
                        .message("订单状态更新")
                        .build();
                publishEventByStatus(event, status);
                log.debug("订单状态变更事件发布成功: orderId={}, status={}", orderId, status);
            } catch (Exception e) {
                log.error("发布订单状态变更事件失败: orderId={}, status={}, error={}",
                        orderId, status, e.getMessage(), e);
                // 不影响主流程，继续执行
            }

            log.info("订单状态更新成功，订单ID: {}, 从 {} 更新为 {}", orderId, oldStatus, status);
            return true;

        } catch (Exception e) {
            log.error("更新订单状态失败，订单ID: {}", orderId, e);
            return false;
        }
    }
}