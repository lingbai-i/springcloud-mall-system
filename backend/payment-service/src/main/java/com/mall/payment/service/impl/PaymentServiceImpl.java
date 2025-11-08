package com.mall.payment.service.impl;

import com.mall.payment.dto.request.PaymentCreateRequest;
import com.mall.payment.dto.request.PaymentQueryRequest;
import com.mall.payment.dto.response.PaymentOrderResponse;
import com.mall.payment.dto.PaymentResult;
import com.mall.payment.dto.response.PageResponse;
import com.mall.payment.entity.PaymentOrder;
import com.mall.payment.entity.PaymentRecord;
import com.mall.payment.enums.PaymentMethod;
import com.mall.payment.enums.PaymentStatus;
import com.mall.payment.exception.PaymentException;
import com.mall.payment.repository.PaymentOrderRepository;
import com.mall.payment.repository.PaymentRecordRepository;
import com.mall.payment.service.PaymentService;
import com.mall.payment.service.PaymentChannelService;
import com.mall.payment.service.RiskControlService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import java.util.Map;
import java.util.HashMap;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 支付服务实现类
 * 提供支付订单的创建、查询、状态更新等核心业务功能
 * 
 * <p>功能特性：</p>
 * <ul>
 *   <li>支持多种支付方式（支付宝、微信、银行卡、余额）</li>
 *   <li>集成风控检查和缓存机制</li>
 *   <li>提供完整的监控指标和日志记录</li>
 *   <li>支持分布式锁防止重复支付</li>
 *   <li>异步处理支付回调和状态更新</li>
 * </ul>
 * 
 * <p>核心业务流程：</p>
 * <ol>
 *   <li>创建支付订单：验证参数、风控检查、生成订单</li>
 *   <li>发起支付：状态检查、分布式锁、调用第三方接口</li>
 *   <li>处理回调：验证签名、更新状态、清除缓存</li>
 *   <li>查询订单：缓存优先、数据库兜底</li>
 * </ol>
 * 
 * <p>技术特性：</p>
 * <ul>
 *   <li>缓存策略：Redis缓存热点数据，提升查询性能</li>
 *   <li>分布式锁：防止并发支付导致的数据不一致</li>
 *   <li>监控指标：Micrometer集成，提供完整的业务监控</li>
 *   <li>风控集成：支付前风控检查，保障资金安全</li>
 *   <li>异常处理：完善的异常处理和状态回滚机制</li>
 * </ul>
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * <p>修改日志：</p>
 * <ul>
 *   <li>V1.0 2024-12-01：初始版本，实现基础支付功能</li>
 *   <li>V1.1 2025-01-01：添加缓存支持、监控指标、风控集成和分布式锁</li>
 *   <li>V1.2 2025-11-01：完善Javadoc注释，优化异常处理和日志记录</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final PaymentChannelService paymentChannelService;
    private final RiskControlService riskControlService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MeterRegistry meterRegistry;
    
    // 监控指标
    private Counter paymentCreateCounter;
    private Counter paymentSuccessCounter;
    private Counter paymentFailureCounter;
    private Timer paymentProcessTimer;
    
    // 缓存键前缀
    private static final String PAYMENT_ORDER_CACHE_PREFIX = "payment:order:";
    private static final String PAYMENT_LOCK_PREFIX = "payment:lock:";
    
    // 缓存过期时间（秒）
    private static final long CACHE_EXPIRE_TIME = 300;
    private static final long LOCK_EXPIRE_TIME = 30;
    
    /**
     * 初始化监控指标
     * 在Bean创建完成后自动调用
     */
    @PostConstruct
    public void initMetrics() {
        // 初始化监控指标
        this.paymentCreateCounter = Counter.builder("payment.create.total")
                .description("支付订单创建总数")
                .register(meterRegistry);
        this.paymentSuccessCounter = Counter.builder("payment.success.total")
                .description("支付成功总数")
                .register(meterRegistry);
        this.paymentFailureCounter = Counter.builder("payment.failure.total")
                .description("支付失败总数")
                .register(meterRegistry);
        this.paymentProcessTimer = Timer.builder("payment.process.duration")
                .description("支付处理耗时")
                .register(meterRegistry);
    }

    /**
     * 创建支付订单
     * 根据请求参数创建新的支付订单，包括参数验证、风控检查、订单创建和支付记录生成
     * 
     * @param request 支付订单创建请求
     * @return 支付订单响应
     * @throws PaymentException 当支付业务异常时
     * @throws IllegalArgumentException 当请求参数无效时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentOrderResponse createPaymentOrder(PaymentCreateRequest request) {
        Timer.Sample sample = Timer.start(meterRegistry);
        log.info("开始创建支付订单，订单ID: {}, 用户ID: {}, 金额: {}, 支付方式: {}", 
                request.getBusinessOrderId(), request.getUserId(), request.getAmount(), request.getPaymentMethod());

        try {
            // 1. 参数验证
            validateCreateRequest(request);

            // 2. 检查是否存在重复订单（分布式锁）
            String lockKey = PAYMENT_LOCK_PREFIX + request.getBusinessOrderId();
            if (!acquireDistributedLock(lockKey, LOCK_EXPIRE_TIME)) {
                throw PaymentException.orderProcessing("订单正在处理中，请勿重复提交");
            }

            try {
                // 3. 检查业务订单是否已存在支付订单
                Optional<PaymentOrder> existingOrder = paymentOrderRepository
                        .findByOrderIdAndDeletedFalse(request.getBusinessOrderId());
                if (existingOrder.isPresent()) {
                    throw PaymentException.duplicateOrder("业务订单已存在支付订单");
                }

                // 4. 创建支付订单
                PaymentOrder paymentOrder = createPaymentOrderEntity(request);
                
                // 5. 保存订单（先保存以获得ID）
                paymentOrder = paymentOrderRepository.save(paymentOrder);

                // 6. 风控检查
                performRiskControl(request, paymentOrder.getId());
                
                // 7. 创建支付记录
                createPaymentRecord(paymentOrder, "订单创建", "支付订单创建成功");

                // 8. 缓存支付订单
                cachePaymentOrder(paymentOrder);

                // 9. 更新监控指标
                paymentCreateCounter.increment();

                log.info("支付订单创建成功，支付订单ID: {}, 业务订单ID: {}", paymentOrder.getId(), request.getOrderId());
                
                return convertToResponse(paymentOrder);
                
            } finally {
                // 释放分布式锁
                releaseDistributedLock(lockKey);
            }
            
        } catch (PaymentException e) {
            log.error("支付业务异常，订单ID: {}, 错误信息: {}", request.getOrderId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("创建支付订单失败，订单ID: {}, 错误信息: {}", request.getOrderId(), e.getMessage(), e);
            throw PaymentException.systemError("创建支付订单失败: " + e.getMessage());
        } finally {
            sample.stop(paymentProcessTimer);
        }
    }

    /**
     * 根据支付订单ID查询订单详情
     * 优先从缓存中获取，缓存未命中时从数据库查询并更新缓存
     * 
     * @param paymentOrderId 支付订单ID
     * @return 支付订单详情
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "paymentOrder", key = "#paymentOrderId", unless = "#result == null")
    public PaymentOrderResponse getPaymentOrder(String paymentOrderId) {
        log.debug("查询支付订单详情，支付订单ID: {}", paymentOrderId);
        
        // 先从缓存获取
        PaymentOrderResponse cachedResponse = getCachedPaymentOrder(paymentOrderId);
        if (cachedResponse != null) {
            log.debug("从缓存获取支付订单，订单ID: {}", paymentOrderId);
            return cachedResponse;
        }
        
        // 缓存未命中，从数据库查询
        Optional<PaymentOrder> orderOpt = paymentOrderRepository.findByIdAndDeletedFalse(paymentOrderId);
        if (orderOpt.isPresent()) {
            PaymentOrderResponse response = convertToResponse(orderOpt.get());
            // 更新缓存
            cachePaymentOrderResponse(paymentOrderId, response);
            return response;
        }
        
        return null;
    }

    /**
     * 根据业务订单ID查询支付订单
     * 
     * @param orderId 业务订单ID
     * @return 支付订单详情
     */
    @Override
    @Transactional(readOnly = true)
    public PaymentOrderResponse getPaymentOrderByOrderId(String orderId) {
        log.debug("根据业务订单ID查询支付订单，订单ID: {}", orderId);
        
        Optional<PaymentOrder> orderOpt = paymentOrderRepository.findByOrderIdAndDeletedFalse(orderId);
        return orderOpt.map(this::convertToResponse).orElse(null);
    }

    /**
     * 根据第三方订单号查询支付订单
     * 
     * @param thirdPartyOrderNo 第三方订单号
     * @return 支付订单详情
     */
    @Override
    @Transactional(readOnly = true)
    public PaymentOrderResponse getPaymentOrderByThirdPartyOrderNo(String thirdPartyOrderNo) {
        log.debug("根据第三方订单号查询支付订单，第三方订单号: {}", thirdPartyOrderNo);
        
        Optional<PaymentOrder> orderOpt = paymentOrderRepository.findByThirdPartyOrderNo(thirdPartyOrderNo);
        return orderOpt.map(this::convertToResponse).orElse(null);
    }

    /**
     * 分页查询支付订单
     * 
     * @param request 查询请求参数
     * @return 分页查询结果
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentOrderResponse> queryPaymentOrders(PaymentQueryRequest request) {
        log.debug("分页查询支付订单，查询条件: {}", request);

        // 构建查询条件
        Specification<PaymentOrder> spec = buildQuerySpecification(request);
        
        // 构建分页参数
        Pageable pageable = PageRequest.of(
                request.getPage() - 1, 
                request.getSize(), 
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // 执行查询
        Page<PaymentOrder> page = paymentOrderRepository.findAll(spec, pageable);
        
        // 转换结果
        List<PaymentOrderResponse> content = page.getContent().stream()
                .map(this::convertToResponse)
                .collect(java.util.stream.Collectors.toList());

        return new PageResponse<>(content, page.getTotalElements(), 
                                request.getPage(), request.getSize());
    }

    /**
     * 查询用户的支付订单列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页查询结果
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentOrderResponse> getUserPaymentOrders(String userId, int page, int size) {
        log.debug("查询用户支付订单列表，用户ID: {}, 页码: {}, 每页大小: {}", userId, page, size);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PaymentOrder> orderPage = paymentOrderRepository.findByUserIdAndDeletedFalse(userId, pageable);
        
        List<PaymentOrderResponse> content = orderPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(java.util.stream.Collectors.toList());

        return new PageResponse<>(content, orderPage.getTotalElements(), page, size);
    }

    /**
     * 发起支付
     * 使用分布式锁防止重复支付，包含风控检查和监控指标更新
     * 
     * @param paymentOrderId 支付订单ID
     * @param clientIp 客户端IP地址
     * @param userAgent 用户代理信息
     * @return 支付结果
     * @throws PaymentException 当支付业务异常时
     */
    @Override
    @CacheEvict(value = "paymentOrder", key = "#paymentOrderId")
    public Map<String, Object> initiatePayment(String paymentOrderId, String clientIp, String userAgent) {
        Timer.Sample sample = Timer.start(meterRegistry);
        log.info("发起支付，支付订单ID: {}", paymentOrderId);

        // 分布式锁防止重复支付
         String lockKey = PAYMENT_LOCK_PREFIX + "initiate:" + paymentOrderId;
         if (!acquireDistributedLock(lockKey, LOCK_EXPIRE_TIME)) {
             throw PaymentException.orderProcessing("支付正在处理中，请勿重复操作");
         }

        try {
            // 1. 查询支付订单
            PaymentOrder paymentOrder = getPaymentOrderEntity(paymentOrderId);
            
            // 2. 验证订单状态
            if (!paymentOrder.canPay()) {
                throw new IllegalStateException("订单状态不允许支付，当前状态: " + paymentOrder.getStatus());
            }

            // 3. 风控检查
            performPaymentRiskControl(paymentOrder);

            try {
                // 4. 更新订单状态为支付中
                paymentOrder.updateStatus(PaymentStatus.PROCESSING);
                paymentOrderRepository.save(paymentOrder);
                
                // 5. 清除缓存
                evictPaymentOrderCache(paymentOrderId);
                
                // 6. 调用支付渠道服务发起支付
                String paymentResult = paymentChannelService.initiatePayment(paymentOrder);
                
                // 7. 记录支付操作
                createPaymentRecord(paymentOrder, "发起支付", "调用第三方支付接口成功");
                
                // 8. 构建返回结果
                Map<String, Object> result = new HashMap<>();
                result.put("paymentUrl", paymentResult);
                result.put("paymentOrderId", paymentOrderId);
                result.put("orderId", paymentOrder.getOrderId());
                result.put("amount", paymentOrder.getAmount());
                result.put("paymentMethod", paymentOrder.getPaymentMethod());
                result.put("clientIp", clientIp);
                result.put("userAgent", userAgent);
                
                log.info("支付发起成功，支付订单ID: {}, 支付结果: {}", paymentOrderId, paymentResult);
                return result;
                
            } catch (Exception e) {
                log.error("支付发起失败，支付订单ID: {}", paymentOrderId, e);
                
                // 恢复订单状态
                paymentOrder.updateStatus(PaymentStatus.PENDING);
                paymentOrder.setFailureReason("支付发起失败: " + e.getMessage());
                paymentOrderRepository.save(paymentOrder);
                
                // 记录失败信息
                createPaymentRecord(paymentOrder, "发起支付失败", e.getMessage());
                
                // 更新失败指标
                paymentFailureCounter.increment();
                
                throw PaymentException.systemError("支付发起失败: " + e.getMessage());
            }
            
        } finally {
            releaseDistributedLock(lockKey);
            sample.stop(paymentProcessTimer);
        }
    }

    /**
     * 处理支付成功回调
     * 使用分布式锁防止重复处理，包含缓存更新和监控指标
     * 
     * @param paymentOrderId 支付订单ID
     * @param thirdPartyOrderNo 第三方支付订单号
     * @param actualAmount 实际支付金额
     * @param channelResponse 第三方平台响应数据
     * @return 处理结果
     */
    @Override
    @CacheEvict(value = "paymentOrder", key = "#paymentOrderId")
    public boolean handlePaymentSuccess(String paymentOrderId, String thirdPartyOrderNo, 
                                      BigDecimal actualAmount, String channelResponse) {
        log.info("处理支付成功回调，支付订单ID: {}, 第三方订单号: {}, 实际金额: {}", 
                paymentOrderId, thirdPartyOrderNo, actualAmount);

        // 分布式锁防止重复处理
        String lockKey = PAYMENT_LOCK_PREFIX + "success:" + paymentOrderId;
        if (!acquireDistributedLock(lockKey, LOCK_EXPIRE_TIME)) {
            log.warn("支付成功回调正在处理中，支付订单ID: {}", paymentOrderId);
            return false;
        }

        try {
            // 1. 查询支付订单
            PaymentOrder paymentOrder = getPaymentOrderEntity(paymentOrderId);
            
            // 2. 验证订单状态
            if (paymentOrder.getStatus() == PaymentStatus.SUCCESS) {
                log.warn("订单已经是成功状态，忽略重复回调，支付订单ID: {}", paymentOrderId);
                return true;
            }
            
            if (!paymentOrder.canPay() && paymentOrder.getStatus() != PaymentStatus.PROCESSING) {
                log.warn("订单状态不允许更新为成功，当前状态: {}, 支付订单ID: {}", 
                        paymentOrder.getStatus(), paymentOrderId);
                return false;
            }

            // 3. 更新订单信息
            paymentOrder.updateStatus(PaymentStatus.SUCCESS);
            paymentOrder.setThirdPartyOrderNo(thirdPartyOrderNo);
            paymentOrder.setActualAmount(actualAmount);
            paymentOrder.setChannelResponse(channelResponse);
            paymentOrder.setPayTime(LocalDateTime.now());
            
            // 4. 计算手续费
            BigDecimal feeAmount = calculateFeeAmount(actualAmount, paymentOrder.getPaymentMethod());
            paymentOrder.setFeeAmount(feeAmount);
            
            // 5. 保存订单
            paymentOrderRepository.save(paymentOrder);
            
            // 6. 清除缓存
            evictPaymentOrderCache(paymentOrderId);
            
            // 7. 记录支付成功
            createPaymentRecord(paymentOrder, "支付成功", "第三方支付回调成功");
            
            // 8. 更新成功指标
            paymentSuccessCounter.increment();
            
            log.info("支付成功处理完成，支付订单ID: {}", paymentOrderId);
            return true;
            
        } catch (Exception e) {
            log.error("处理支付成功回调失败，支付订单ID: {}", paymentOrderId, e);
            return false;
        } finally {
            releaseDistributedLock(lockKey);
        }
    }

    /**
     * 处理支付失败回调
     * 
     * @param paymentOrderId 支付订单ID
     * @param failureReason 失败原因
     * @param channelResponse 第三方平台响应数据
     * @return 处理结果
     */
    @Override
    public boolean handlePaymentFailure(String paymentOrderId, String failureReason, String channelResponse) {
        log.info("处理支付失败回调，支付订单ID: {}, 失败原因: {}", paymentOrderId, failureReason);

        try {
            // 1. 查询支付订单
            PaymentOrder paymentOrder = getPaymentOrderEntity(paymentOrderId);
            
            // 2. 更新订单状态
            paymentOrder.updateStatus(PaymentStatus.FAILED);
            paymentOrder.setFailureReason(failureReason);
            paymentOrder.setChannelResponse(channelResponse);
            paymentOrder.incrementRetryCount();
            
            // 3. 保存订单
            paymentOrderRepository.save(paymentOrder);
            
            // 4. 记录支付失败
            createPaymentRecord(paymentOrder, "支付失败", failureReason);
            
            log.info("支付失败处理完成，支付订单ID: {}", paymentOrderId);
            return true;
            
        } catch (Exception e) {
            log.error("处理支付失败回调失败，支付订单ID: {}", paymentOrderId, e);
            return false;
        }
    }

    /**
     * 取消支付订单
     * 
     * @param paymentOrderId 支付订单ID
     * @param reason 取消原因
     * @return 取消结果
     */
    @Override
    public boolean cancelPaymentOrder(String paymentOrderId, String reason) {
        log.info("取消支付订单，支付订单ID: {}, 取消原因: {}", paymentOrderId, reason);

        try {
            // 1. 查询支付订单
            PaymentOrder paymentOrder = getPaymentOrderEntity(paymentOrderId);
            
            // 2. 验证订单状态
            if (!paymentOrder.canCancel()) {
                throw new IllegalStateException("订单状态不允许取消，当前状态: " + paymentOrder.getStatus());
            }

            // 3. 如果订单正在支付中，需要调用第三方平台取消接口
            if (paymentOrder.getStatus() == PaymentStatus.PROCESSING) {
                try {
                    paymentChannelService.cancelPayment(paymentOrder);
                } catch (Exception e) {
                    log.warn("调用第三方平台取消支付失败，继续执行本地取消，支付订单ID: {}", paymentOrderId, e);
                }
            }

            // 4. 更新订单状态
            paymentOrder.updateStatus(PaymentStatus.CANCELLED);
            paymentOrder.setFailureReason(reason);
            
            // 5. 保存订单
            paymentOrderRepository.save(paymentOrder);
            
            // 6. 记录取消操作
            createPaymentRecord(paymentOrder, "订单取消", reason);
            
            log.info("支付订单取消成功，支付订单ID: {}", paymentOrderId);
            return true;
            
        } catch (Exception e) {
            log.error("取消支付订单失败，支付订单ID: {}", paymentOrderId, e);
            return false;
        }
    }

    /**
     * 查询支付状态
     * 
     * @param paymentOrderId 支付订单ID
     * @return 最新的支付状态
     */
    @Override
    public PaymentStatus queryPaymentStatus(String paymentOrderId) {
        log.debug("查询支付状态，支付订单ID: {}", paymentOrderId);

        try {
            // 1. 查询本地订单状态
            PaymentOrder paymentOrder = getPaymentOrderEntity(paymentOrderId);
            
            // 2. 如果订单是处理中状态，查询第三方平台状态
            if (paymentOrder.getStatus() == PaymentStatus.PROCESSING) {
                PaymentStatus remoteStatus = paymentChannelService.queryPaymentStatus(paymentOrder);
                
                // 3. 如果远程状态与本地状态不一致，更新本地状态
                if (remoteStatus != null && remoteStatus != paymentOrder.getStatus()) {
                    log.info("同步远程支付状态，支付订单ID: {}, 本地状态: {}, 远程状态: {}", 
                            paymentOrderId, paymentOrder.getStatus(), remoteStatus);
                    
                    paymentOrder.updateStatus(remoteStatus);
                    paymentOrderRepository.save(paymentOrder);
                    
                    // 记录状态同步
                    createPaymentRecord(paymentOrder, "状态同步", "从第三方平台同步状态: " + remoteStatus);
                }
            }
            
            return paymentOrder.getStatus();
            
        } catch (Exception e) {
            log.error("查询支付状态失败，支付订单ID: {}", paymentOrderId, e);
            // 返回本地状态
            PaymentOrder paymentOrder = getPaymentOrderEntity(paymentOrderId);
            return paymentOrder.getStatus();
        }
    }

    /**
     * 处理过期订单
     * 
     * @return 处理的过期订单数量
     */
    @Override
    public int handleExpiredOrders() {
        log.info("开始处理过期订单");

        try {
            // 查询所有过期的待支付订单
            LocalDateTime now = LocalDateTime.now();
            List<PaymentOrder> expiredOrders = paymentOrderRepository
                    .findExpiredOrders(PaymentStatus.PENDING, now);

            int count = 0;
            for (PaymentOrder order : expiredOrders) {
                try {
                    // 更新订单状态为已过期
                    order.updateStatus(PaymentStatus.EXPIRED);
                    order.setFailureReason("订单已过期");
                    paymentOrderRepository.save(order);
                    
                    // 记录过期操作
                    createPaymentRecord(order, "订单过期", "订单超过有效期自动过期");
                    
                    count++;
                    log.debug("订单过期处理完成，支付订单ID: {}", order.getId());
                    
                } catch (Exception e) {
                    log.error("处理过期订单失败，支付订单ID: {}", order.getId(), e);
                }
            }

            log.info("过期订单处理完成，共处理 {} 个订单", count);
            return count;
            
        } catch (Exception e) {
            log.error("处理过期订单失败", e);
            return 0;
        }
    }

    /**
     * 重试失败的支付订单
     * 
     * @param paymentOrderId 支付订单ID
     * @param clientIp 客户端IP地址
     * @param userAgent 用户代理信息
     * @return 重试结果
     */
    @Override
    public Map<String, Object> retryPayment(String paymentOrderId, String clientIp, String userAgent) {
        log.info("重试支付订单，支付订单ID: {}", paymentOrderId);

        try {
            // 1. 查询支付订单
            PaymentOrder paymentOrder = getPaymentOrderEntity(paymentOrderId);
            
            // 2. 验证订单状态
            if (paymentOrder.getStatus() != PaymentStatus.FAILED) {
                throw new IllegalStateException("只有失败状态的订单才能重试，当前状态: " + paymentOrder.getStatus());
            }
            
            // 3. 检查重试次数
            if (paymentOrder.getRetryCount() >= 3) {
                throw new IllegalStateException("订单重试次数已达上限");
            }

            // 4. 重置订单状态
            paymentOrder.updateStatus(PaymentStatus.PENDING);
            paymentOrder.setFailureReason(null);
            paymentOrder.setRetryCount(paymentOrder.getRetryCount() + 1);
            
            // 5. 保存订单
            paymentOrderRepository.save(paymentOrder);
            
            // 6. 记录重试操作
            createPaymentRecord(paymentOrder, "支付重试", "重置订单状态，准备重新支付");
            
            // 7. 直接发起支付
            Map<String, Object> paymentResult = initiatePayment(paymentOrderId, clientIp, userAgent);
            
            log.info("支付订单重试成功，支付订单ID: {}", paymentOrderId);
            return paymentResult;
            
        } catch (Exception e) {
            log.error("重试支付订单失败，支付订单ID: {}", paymentOrderId, e);
            throw new RuntimeException("重试支付失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取支付统计数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param userId 用户ID
     * @return 统计结果
     */
    @Override
    @Transactional(readOnly = true)
    public PaymentStatistics getPaymentStatistics(LocalDateTime startTime, LocalDateTime endTime, String userId) {
        log.debug("获取支付统计数据，开始时间: {}, 结束时间: {}, 用户ID: {}", startTime, endTime, userId);

        try {
            // 构建查询条件
            Specification<PaymentOrder> spec = (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                
                // 时间范围
                if (startTime != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startTime));
                }
                if (endTime != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endTime));
                }
                
                // 用户ID
                if (StringUtils.hasText(userId)) {
                    predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
                }
                
                // 未删除
                predicates.add(criteriaBuilder.equal(root.get("deleted"), false));
                
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };

            // 查询所有订单
            List<PaymentOrder> orders = paymentOrderRepository.findAll(spec);
            
            // 统计数据
            long totalOrders = orders.size();
            long successOrders = orders.stream().mapToLong(o -> o.getStatus() == PaymentStatus.SUCCESS ? 1 : 0).sum();
            long failedOrders = orders.stream().mapToLong(o -> o.getStatus() == PaymentStatus.FAILED ? 1 : 0).sum();
            long cancelledOrders = orders.stream().mapToLong(o -> o.getStatus() == PaymentStatus.CANCELLED ? 1 : 0).sum();
            
            BigDecimal totalAmount = orders.stream()
                    .map(PaymentOrder::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
            BigDecimal successAmount = orders.stream()
                    .filter(o -> o.getStatus() == PaymentStatus.SUCCESS)
                    .map(o -> {
                        BigDecimal actualAmount = o.getActualAmount();
                        return actualAmount != null ? actualAmount : o.getAmount();
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return new PaymentStatistics(totalOrders, successOrders, failedOrders, 
                                       cancelledOrders, totalAmount, successAmount);
                                       
        } catch (Exception e) {
            log.error("获取支付统计数据失败", e);
            return new PaymentStatistics(0, 0, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO);
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 验证创建支付订单请求参数
     */
    private void validateCreateRequest(PaymentCreateRequest request) {
        if (!request.isValidAmount()) {
            throw new IllegalArgumentException("支付金额无效");
        }
        
        if (!request.isPaymentMethodEnabled()) {
            throw new IllegalArgumentException("支付方式未启用");
        }
    }

    /**
     * 创建支付订单实体
     */
    private PaymentOrder createPaymentOrderEntity(PaymentCreateRequest request) {
        PaymentOrder paymentOrder = new PaymentOrder();
        // ID由@GeneratedValue自动生成，不需要手动设置
        paymentOrder.setOrderId(request.getBusinessOrderId());
        paymentOrder.setUserId(request.getUserId());
        paymentOrder.setAmount(request.getAmount());
        paymentOrder.setPaymentMethod(request.getPaymentMethod());
        paymentOrder.setDescription(request.getDescription());
        paymentOrder.setReturnUrl(request.getReturnUrl());
        paymentOrder.setNotifyUrl(request.getNotifyUrl());
        
        // 设置过期时间
        if (request.getExpireMinutes() != null && request.getExpireMinutes() > 0) {
            paymentOrder.setExpireTime(LocalDateTime.now().plusMinutes(request.getExpireMinutes()));
        } else {
            paymentOrder.setExpireTime(LocalDateTime.now().plusMinutes(30)); // 默认30分钟
        }
        
        return paymentOrder;
    }

    /**
     * 创建支付记录
     */
    private void createPaymentRecord(PaymentOrder paymentOrder, String action, String description) {
        try {
            PaymentRecord record = new PaymentRecord();
            record.setId(UUID.randomUUID().toString());
            record.setPaymentOrder(paymentOrder);
            record.setAction(action);
            record.setDescription(description);
            record.setStatus(paymentOrder.getStatus());
            record.setCreatedAt(LocalDateTime.now());
            
            paymentRecordRepository.save(record);
        } catch (Exception e) {
            log.error("创建支付记录失败，支付订单ID: {}, 操作: {}", paymentOrder.getId(), action, e);
        }
    }

    /**
     * 获取支付订单实体
     */
    private PaymentOrder getPaymentOrderEntity(String paymentOrderId) {
        return paymentOrderRepository.findByIdAndDeletedFalse(paymentOrderId)
                .orElseThrow(() -> new IllegalArgumentException("支付订单不存在: " + paymentOrderId));
    }

    /**
     * 计算手续费
     */
    private BigDecimal calculateFeeAmount(BigDecimal amount, PaymentMethod paymentMethod) {
        if (amount == null || paymentMethod == null) {
            return BigDecimal.ZERO;
        }
        
        double feeRate = paymentMethod.getFeeRate() / 100.0; // 转换为小数
        return amount.multiply(BigDecimal.valueOf(feeRate)).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 构建查询条件
     */
    private Specification<PaymentOrder> buildQuerySpecification(PaymentQueryRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 基础条件：未删除
            predicates.add(criteriaBuilder.equal(root.get("deleted"), false));
            
            // 用户ID
            if (StringUtils.hasText(request.getUserId())) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), request.getUserId()));
            }
            
            // 订单ID
            if (StringUtils.hasText(request.getOrderId())) {
                predicates.add(criteriaBuilder.equal(root.get("orderId"), request.getOrderId()));
            }
            
            // 支付状态
            if (request.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }
            
            // 支付方式
            if (request.getPaymentMethod() != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentMethod"), request.getPaymentMethod()));
            }
            
            // 时间范围
            if (request.getCreatedAtStart() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), request.getCreatedAtStart()));
            }
            if (request.getCreatedAtEnd() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), request.getCreatedAtEnd()));
            }
            
            // 金额范围
            if (request.getMinAmount() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), request.getMinAmount()));
            }
            if (request.getMaxAmount() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("amount"), request.getMaxAmount()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 转换为响应对象
     */
    private PaymentOrderResponse convertToResponse(PaymentOrder paymentOrder) {
        PaymentOrderResponse response = new PaymentOrderResponse();
        response.setId(paymentOrder.getId());
        response.setOrderId(paymentOrder.getOrderId());
        response.setUserId(paymentOrder.getUserId());
        response.setAmount(paymentOrder.getAmount());
        response.setActualAmount(paymentOrder.getActualAmount());
        response.setPaymentMethod(paymentOrder.getPaymentMethod());
        response.setStatus(paymentOrder.getStatus());
        response.setDescription(paymentOrder.getDescription());
        response.setThirdPartyOrderNo(paymentOrder.getThirdPartyOrderNo());
        response.setFailureReason(paymentOrder.getFailureReason());
        response.setRetryCount(paymentOrder.getRetryCount());
        response.setExpireTime(paymentOrder.getExpireTime());
        response.setPayTime(paymentOrder.getPayTime());
        response.setCreatedAt(paymentOrder.getCreatedAt());
        response.setUpdatedAt(paymentOrder.getUpdatedAt());
        
        return response;
    }
    
    // ==================== 缓存相关方法 ====================
    
    /**
     * 缓存支付订单
     * 
     * @param paymentOrder 支付订单
     */
    private void cachePaymentOrder(PaymentOrder paymentOrder) {
        try {
            String cacheKey = PAYMENT_ORDER_CACHE_PREFIX + paymentOrder.getId();
            PaymentOrderResponse response = convertToResponse(paymentOrder);
            redisTemplate.opsForValue().set(cacheKey, response, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            log.debug("缓存支付订单成功，订单ID: {}", paymentOrder.getId());
        } catch (Exception e) {
            log.warn("缓存支付订单失败，订单ID: {}, 错误: {}", paymentOrder.getId(), e.getMessage());
        }
    }
    
    /**
     * 从缓存获取支付订单
     * 
     * @param paymentOrderId 支付订单ID
     * @return 支付订单响应
     */
    private PaymentOrderResponse getCachedPaymentOrder(String paymentOrderId) {
        try {
            String cacheKey = PAYMENT_ORDER_CACHE_PREFIX + paymentOrderId;
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof PaymentOrderResponse) {
                return (PaymentOrderResponse) cached;
            }
        } catch (Exception e) {
            log.warn("从缓存获取支付订单失败，订单ID: {}, 错误: {}", paymentOrderId, e.getMessage());
        }
        return null;
    }
    
    /**
     * 缓存支付订单响应
     * 
     * @param paymentOrderId 支付订单ID
     * @param response 响应对象
     */
    private void cachePaymentOrderResponse(String paymentOrderId, PaymentOrderResponse response) {
        try {
            String cacheKey = PAYMENT_ORDER_CACHE_PREFIX + paymentOrderId;
            redisTemplate.opsForValue().set(cacheKey, response, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            log.debug("缓存支付订单响应成功，订单ID: {}", paymentOrderId);
        } catch (Exception e) {
            log.warn("缓存支付订单响应失败，订单ID: {}, 错误: {}", paymentOrderId, e.getMessage());
        }
    }
    
    /**
     * 清除支付订单缓存
     * 
     * @param paymentOrderId 支付订单ID
     */
    private void evictPaymentOrderCache(String paymentOrderId) {
        try {
            String cacheKey = PAYMENT_ORDER_CACHE_PREFIX + paymentOrderId;
            redisTemplate.delete(cacheKey);
            log.debug("清除支付订单缓存成功，订单ID: {}", paymentOrderId);
        } catch (Exception e) {
            log.warn("清除支付订单缓存失败，订单ID: {}, 错误: {}", paymentOrderId, e.getMessage());
        }
    }
    
    // ==================== 分布式锁相关方法 ====================
    
    /**
     * 获取分布式锁
     * 
     * @param lockKey 锁键
     * @param expireTime 过期时间（秒）
     * @return 是否获取成功
     */
    private boolean acquireDistributedLock(String lockKey, long expireTime) {
        try {
            String lockValue = UUID.randomUUID().toString();
            Boolean result = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, expireTime, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(result)) {
                log.debug("获取分布式锁成功，锁键: {}", lockKey);
                return true;
            }
            log.debug("获取分布式锁失败，锁键: {}", lockKey);
            return false;
        } catch (Exception e) {
            log.warn("获取分布式锁异常，锁键: {}, 错误: {}", lockKey, e.getMessage());
            return false;
        }
    }
    
    /**
     * 释放分布式锁
     * 
     * @param lockKey 锁键
     */
    private void releaseDistributedLock(String lockKey) {
        try {
            redisTemplate.delete(lockKey);
            log.debug("释放分布式锁成功，锁键: {}", lockKey);
        } catch (Exception e) {
            log.warn("释放分布式锁失败，锁键: {}, 错误: {}", lockKey, e.getMessage());
        }
    }
    
    // ==================== 风控相关方法 ====================
    
    /**
     * 执行风控检查（创建订单时）
     * 
     * @param request 支付创建请求
     * @param paymentOrderId 支付订单ID
     * @throws PaymentException 当风控检查失败时
     */
    private void performRiskControl(PaymentCreateRequest request, String paymentOrderId) {
        try {
            log.debug("执行风控检查，用户ID: {}, 金额: {}, 支付订单ID: {}", 
                     request.getUserId(), request.getAmount(), paymentOrderId);
            
            // 调用风控服务进行检查
            RiskControlService.RiskCheckResult riskCheckResult = riskControlService.performRiskCheck(
                request, paymentOrderId
            );
            
            if (!riskCheckResult.isPassed()) {
                throw PaymentException.riskControlReject("风控检查未通过，订单被拒绝: " + riskCheckResult.getReason());
            }
            
            log.debug("风控检查通过，用户ID: {}, 风险评分: {}", 
                     request.getUserId(), riskCheckResult.getRiskScore());
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("风控检查异常，用户ID: {}, 错误: {}", request.getUserId(), e.getMessage(), e);
            // 风控检查异常时，为了安全起见，拒绝支付
            throw PaymentException.riskControlReject("风控检查异常，订单被拒绝");
        }
    }
    
    /**
     * 执行支付风控检查（发起支付时）
     * 
     * @param paymentOrder 支付订单
     * @throws PaymentException 当风控检查失败时
     */
    private void performPaymentRiskControl(PaymentOrder paymentOrder) {
        try {
            log.debug("执行支付风控检查，订单ID: {}, 用户ID: {}", paymentOrder.getId(), paymentOrder.getUserId());
            
            // 构建支付创建请求用于风控检查
            PaymentCreateRequest request = new PaymentCreateRequest();
            request.setUserId(paymentOrder.getUserId());
            request.setBusinessOrderId(paymentOrder.getOrderId());
            request.setAmount(paymentOrder.getAmount());
            request.setPaymentMethod(paymentOrder.getPaymentMethod());
            
            // 调用风控服务进行支付前检查
            RiskControlService.RiskCheckResult riskCheckResult = riskControlService.performRiskCheck(
                request, paymentOrder.getId()
            );
            
            if (!riskCheckResult.isPassed()) {
                throw PaymentException.riskControlReject("支付风控检查未通过，支付被拒绝: " + riskCheckResult.getReason());
            }
            
            log.debug("支付风控检查通过，订单ID: {}, 风险评分: {}", 
                     paymentOrder.getId(), riskCheckResult.getRiskScore());
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("支付风控检查异常，订单ID: {}, 错误: {}", paymentOrder.getId(), e.getMessage(), e);
            // 风控检查异常时，为了安全起见，拒绝支付
            throw PaymentException.riskControlReject("支付风控检查异常，支付被拒绝");
        }
    }

    /**
     * 重试失败的支付订单
     * 定时任务调用，重新处理失败状态的支付订单
     * 
     * @return 重试成功的订单数量
     * @author lingbai
     * @since V1.0 2025-01-27: 新增定时任务重试失败订单功能
     */
    @Override
    public int retryFailedOrders() {
        log.info("开始重试失败的支付订单");
        
        try {
            // 查询失败状态且可重试的支付订单（创建时间在24小时内）
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
            List<PaymentOrder> failedOrders = paymentOrderRepository.findFailedOrdersForRetry(cutoffTime);
            
            if (failedOrders.isEmpty()) {
                log.info("没有需要重试的失败支付订单");
                return 0;
            }
            
            int successCount = 0;
            for (PaymentOrder order : failedOrders) {
                try {
                    log.debug("重试支付订单，订单ID: {}, 用户ID: {}", order.getId(), order.getUserId());
                    
                    // 重新发起支付
                    PaymentResult result = paymentChannelService.processPayment(
                        order.getPaymentMethod(),
                        order.getAmount(),
                        order.getId(),
                        order.getOrderId(),
                        order.getUserId()
                    );
                    
                    if (result.isSuccess()) {
                        // 更新订单状态为处理中
                        order.setStatus(PaymentStatus.PROCESSING);
                        order.setThirdPartyOrderNo(result.getThirdPartyOrderNo());
                        order.setUpdatedAt(LocalDateTime.now());
                        paymentOrderRepository.save(order);
                        
                        successCount++;
                        log.info("支付订单重试成功，订单ID: {}, 第三方订单号: {}", 
                                order.getId(), result.getThirdPartyOrderNo());
                    } else {
                        log.warn("支付订单重试失败，订单ID: {}, 失败原因: {}", 
                                order.getId(), result.getFailureReason());
                    }
                    
                } catch (Exception e) {
                    log.error("重试支付订单异常，订单ID: {}, 错误: {}", order.getId(), e.getMessage(), e);
                }
            }
            
            log.info("支付订单重试完成，总数: {}, 成功: {}", failedOrders.size(), successCount);
            return successCount;
            
        } catch (Exception e) {
            log.error("重试失败支付订单异常: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 同步支付状态
     * 定时任务调用，从第三方支付平台同步订单状态
     * 
     * @return 同步成功的订单数量
     * @author lingbai
     * @since V1.0 2025-01-27: 新增定时任务同步支付状态功能
     */
    @Override
    public int syncPaymentStatus() {
        log.info("开始同步支付状态");
        
        try {
            // 查询处理中状态的支付订单（创建时间在48小时内）
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(48);
            List<PaymentOrder> processingOrders = paymentOrderRepository.findProcessingOrdersForSync(cutoffTime);
            
            if (processingOrders.isEmpty()) {
                log.info("没有需要同步状态的支付订单");
                return 0;
            }
            
            int syncCount = 0;
            for (PaymentOrder order : processingOrders) {
                try {
                    log.debug("同步支付状态，订单ID: {}, 第三方订单号: {}", 
                             order.getId(), order.getThirdPartyOrderNo());
                    
                    // 查询第三方支付状态
                    PaymentChannelService.PaymentQueryResult queryResult = 
                        paymentChannelService.queryPaymentStatus(
                            order.getPaymentMethod(),
                            order.getThirdPartyOrderNo()
                        );
                    
                    if (queryResult != null && queryResult.getStatus() != null) {
                        PaymentStatus newStatus = queryResult.getStatus();
                        
                        // 如果状态有变化，更新订单状态
                        if (!order.getStatus().equals(newStatus)) {
                            PaymentStatus oldStatus = order.getStatus();
                            order.setStatus(newStatus);
                            order.setUpdatedAt(LocalDateTime.now());
                            
                            // 如果支付成功，记录支付成功信息
                            if (newStatus == PaymentStatus.SUCCESS) {
                                order.setPaidAt(LocalDateTime.now());
                                if (queryResult.getActualAmount() != null) {
                                    order.setActualAmount(queryResult.getActualAmount());
                                }
                            }
                            
                            paymentOrderRepository.save(order);
                            syncCount++;
                            
                            log.info("支付状态同步成功，订单ID: {}, 状态变更: {} -> {}", 
                                    order.getId(), oldStatus, newStatus);
                        }
                    }
                    
                } catch (Exception e) {
                    log.error("同步支付状态异常，订单ID: {}, 错误: {}", order.getId(), e.getMessage(), e);
                }
            }
            
            log.info("支付状态同步完成，总数: {}, 同步: {}", processingOrders.size(), syncCount);
            return syncCount;
            
        } catch (Exception e) {
            log.error("同步支付状态异常: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 清理过期的支付记录
     * 清理指定时间之前的已完成支付记录
     * 
     * @param cutoffTime 截止时间，早于此时间的记录将被清理
     * @return 清理的记录数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanupExpiredRecords(LocalDateTime cutoffTime) {
        log.info("开始清理过期支付记录，截止时间: {}", cutoffTime);
        
        try {
            // 查找需要清理的支付订单（已完成状态且创建时间早于截止时间）
            List<PaymentOrder> expiredOrders = paymentOrderRepository.findByStatusAndCreatedAtBefore(
                    PaymentStatus.SUCCESS, cutoffTime);
            
            if (expiredOrders.isEmpty()) {
                log.info("没有找到需要清理的过期支付记录");
                return 0;
            }
            
            int cleanedCount = 0;
            for (PaymentOrder order : expiredOrders) {
                try {
                    // 删除相关的支付记录
                    paymentRecordRepository.deleteByPaymentOrderId(order.getId());
                    
                    // 删除支付订单
                    paymentOrderRepository.delete(order);
                    
                    // 清理缓存
                    evictPaymentOrderCache(order.getId());
                    
                    cleanedCount++;
                    log.debug("清理过期支付记录成功，支付订单ID: {}", order.getId());
                    
                } catch (Exception e) {
                    log.error("清理过期支付记录失败，支付订单ID: {}", order.getId(), e);
                }
            }
            
            log.info("过期支付记录清理完成，清理数量: {}", cleanedCount);
            return cleanedCount;
            
        } catch (Exception e) {
            log.error("清理过期支付记录异常", e);
            return 0;
        }
    }
}