package com.mall.payment.service.impl;

import com.mall.payment.dto.request.RefundCreateRequest;
import com.mall.payment.dto.response.RefundOrderResponse;
import com.mall.payment.dto.response.PageResponse;
import com.mall.payment.entity.PaymentOrder;
import com.mall.payment.entity.RefundOrder;
import com.mall.payment.entity.RefundRecord;
import com.mall.payment.enums.PaymentStatus;
import com.mall.payment.enums.RefundStatus;
import com.mall.payment.repository.PaymentOrderRepository;
import com.mall.payment.repository.RefundOrderRepository;
import com.mall.payment.repository.RefundRecordRepository;
import com.mall.payment.service.RefundService;
import com.mall.payment.service.RefundChannelService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 退款服务实现类
 * 实现退款相关的核心业务逻辑，包括创建退款订单、处理退款、查询退款状态等
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>退款订单管理：创建、查询、更新退款订单</li>
 *   <li>退款处理：调用第三方退款接口，处理退款流程</li>
 *   <li>状态同步：处理第三方退款回调，同步退款状态</li>
 *   <li>数据统计：提供退款相关的统计分析功能</li>
 * </ul>
 * 
 * <p>退款业务流程：</p>
 * <ol>
 *   <li>创建退款订单：验证原支付订单、计算退款金额、生成退款订单</li>
 *   <li>发起退款：调用对应支付渠道的退款接口</li>
 *   <li>处理回调：接收第三方退款结果通知，更新订单状态</li>
 *   <li>完成退款：更新相关业务数据，记录退款完成</li>
 * </ol>
 * 
 * <p>支持的退款类型：</p>
 * <ul>
 *   <li>全额退款：退还订单全部金额</li>
 *   <li>部分退款：退还订单部分金额</li>
 *   <li>多次退款：支持同一订单多次部分退款</li>
 * </ul>
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * <p>修改日志：</p>
 * <ul>
 *   <li>V1.0 2024-12-01：初始版本，实现基础退款功能</li>
 *   <li>V1.1 2025-01-10：增加部分退款和多次退款支持</li>
 *   <li>V1.2 2025-11-01：完善Javadoc注释，优化退款流程</li>
 * </ul>
 */
@Slf4j
@Service
@Transactional
public class RefundServiceImpl implements RefundService {

    private static final Logger log = LoggerFactory.getLogger(RefundServiceImpl.class);

    @Autowired
    private RefundOrderRepository refundOrderRepository;

    @Autowired
    private RefundRecordRepository refundRecordRepository;

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Autowired
    private RefundChannelService refundChannelService;

    /**
     * 创建退款订单
     */
    @Override
    public RefundOrderResponse createRefundOrder(RefundCreateRequest request) {
        log.info("开始创建退款订单，支付订单ID: {}, 退款金额: {}",
                request.getPaymentOrderId(), request.getRefundAmount());

        // 参数验证
        validateRefundRequest(request);

        // 查询支付订单
        PaymentOrder paymentOrder = paymentOrderRepository.findById(request.getPaymentOrderId())
                .orElseThrow(() -> new IllegalArgumentException("支付订单不存在"));

        // 验证支付订单状态
        validatePaymentOrderForRefund(paymentOrder, request.getRefundAmount());

        // 创建退款订单
        RefundOrder refundOrder = createRefundOrderEntity(request, paymentOrder);
        refundOrder = refundOrderRepository.save(refundOrder);

        String refundOrderId = refundOrder.getId();
        log.info("退款订单创建成功，退款订单ID: {}", refundOrderId);
        return convertToResponse(refundOrder);
    }

    /**
     * 根据退款订单ID查询退款详情
     */
    @Override
    @Transactional(readOnly = true)
    public RefundOrderResponse getRefundOrder(String refundOrderId) {
        log.debug("查询退款订单详情，退款订单ID: {}", refundOrderId);

        Optional<RefundOrder> refundOrder = refundOrderRepository.findById(refundOrderId);
        return refundOrder.map(this::convertToResponse).orElse(null);
    }

    /**
     * 根据支付订单ID查询退款订单列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<RefundOrderResponse> getRefundOrdersByPaymentOrderId(String paymentOrderId) {
        log.debug("查询支付订单的退款列表，支付订单ID: {}", paymentOrderId);

        List<RefundOrder> refundOrders = refundOrderRepository.findByPaymentOrderId(paymentOrderId);
        return refundOrders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 分页查询退款订单
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<RefundOrderResponse> queryRefundOrders(String userId, RefundStatus status, 
                                                              LocalDateTime startTime, LocalDateTime endTime, 
                                                              int page, int size) {
        log.debug("分页查询退款订单，用户ID: {}, 状态: {}, 页码: {}, 大小: {}", userId, status, page, size);

        // 创建分页参数
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 构建查询条件并执行查询
        Page<RefundOrder> refundOrderPage = buildQueryAndExecute(userId, status, startTime, endTime, pageable);

        // 转换响应
        List<RefundOrderResponse> responses = refundOrderPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(responses, refundOrderPage.getTotalElements(), page, size);
    }

    /**
     * 查询用户的退款订单列表
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<RefundOrderResponse> getUserRefundOrders(String userId, int page, int size) {
        log.debug("查询用户退款订单列表，用户ID: {}, 页码: {}, 大小: {}", userId, page, size);

        return queryRefundOrders(userId, null, null, null, page, size);
    }

    /**
     * 处理退款申请
     */
    @Override
    public boolean processRefund(String refundOrderId) {
        log.info("开始处理退款申请，退款订单ID: {}", refundOrderId);

        try {
            RefundOrder refundOrder = refundOrderRepository.findById(refundOrderId)
                    .orElseThrow(() -> new IllegalArgumentException("退款订单不存在"));

            // 检查退款订单状态
            if (refundOrder.getStatus() != RefundStatus.PENDING) {
                throw new IllegalStateException("退款订单状态不允许处理");
            }

            // 更新状态为处理中
            refundOrder.setStatus(RefundStatus.PROCESSING);
            refundOrder.setProcessTime(LocalDateTime.now());
            refundOrderRepository.save(refundOrder);

            // 调用退款渠道服务处理退款
            RefundChannelService.RefundResult result = refundChannelService.processRefund(
                    refundOrder.getPaymentOrder().getPaymentMethod(),
                    refundOrder.getPaymentOrder().getThirdPartyOrderNo(),
                    refundOrderId,
                    refundOrder.getRefundAmount(),
                    refundOrder.getRefundReason()
            );

            // 根据结果更新状态
            if (result.isSuccess()) {
                refundOrder.setStatus(RefundStatus.SUCCESS);
                refundOrder.setThirdPartyRefundNo(result.getThirdPartyRefundNo());
                refundOrder.setActualRefundAmount(result.getActualRefundAmount());
                refundOrder.setRefundTime(LocalDateTime.now());
                refundOrder.setChannelResponse(result.getChannelResponse());
                
                log.info("退款处理成功，退款订单ID: {}, 第三方退款单号: {}", 
                           refundOrderId, result.getThirdPartyRefundNo());
            } else {
                refundOrder.setStatus(RefundStatus.FAILED);
                refundOrder.setFailureReason(result.getFailureReason());
                refundOrder.setChannelResponse(result.getChannelResponse());
                
                log.warn("退款处理失败，退款订单ID: {}, 失败原因: {}", 
                           refundOrderId, result.getFailureReason());
            }

            refundOrderRepository.save(refundOrder);

            // 创建退款记录
            createRefundProcessRecord(refundOrder, result);

            return result.isSuccess();

        } catch (Exception e) {
            log.error("处理退款申请异常，退款订单ID: {}", refundOrderId, e);
            
            // 更新退款订单状态为失败
            RefundOrder refundOrder = refundOrderRepository.findById(refundOrderId).orElse(null);
            if (refundOrder != null) {
                refundOrder.setStatus(RefundStatus.FAILED);
                refundOrder.setFailureReason("系统异常: " + e.getMessage());
                refundOrderRepository.save(refundOrder);
            }
            
            throw new RuntimeException("处理退款申请失败", e);
        }
    }

    /**
     * 审核退款申请
     */
    @Override
    public boolean auditRefund(String refundOrderId, boolean approved, String auditReason, String auditorId) {
        log.info("审核退款申请，退款订单ID: {}, 审核结果: {}, 审核人: {}",
                refundOrderId, approved ? "通过" : "拒绝", auditorId);

        RefundOrder refundOrder = refundOrderRepository.findById(refundOrderId)
                .orElseThrow(() -> new IllegalArgumentException("退款订单不存在"));

        // 检查退款订单状态
        if (refundOrder.getStatus() != RefundStatus.PENDING) {
            throw new IllegalStateException("退款订单状态不允许审核");
        }

        // 更新审核信息
        refundOrder.setReviewerId(auditorId);
        refundOrder.setReviewTime(LocalDateTime.now());
        refundOrder.setReviewRemark(auditReason);

        if (approved) {
            refundOrder.setStatus(RefundStatus.APPROVED);
            log.info("退款申请审核通过，退款订单ID: {}", refundOrderId);
        } else {
            refundOrder.setStatus(RefundStatus.REJECTED);
            log.info("退款申请审核拒绝，退款订单ID: {}, 拒绝原因: {}", refundOrderId, auditReason);
        }

        refundOrderRepository.save(refundOrder);

        // 创建审核记录
        createAuditRecord(refundOrder, approved);

        return approved;
    }

    /**
     * 处理退款成功回调
     */
    @Override
    public boolean handleRefundSuccess(String refundOrderId, String thirdPartyRefundNo, 
                                      BigDecimal actualRefundAmount, String channelResponse) {
        log.info("处理退款成功回调，退款订单ID: {}, 第三方退款单号: {}", refundOrderId, thirdPartyRefundNo);

        try {
            RefundOrder refundOrder = refundOrderRepository.findById(refundOrderId)
                    .orElseThrow(() -> new IllegalArgumentException("退款订单不存在"));

            // 更新退款订单状态
            refundOrder.setStatus(RefundStatus.SUCCESS);
            refundOrder.setThirdPartyRefundNo(thirdPartyRefundNo);
            refundOrder.setActualRefundAmount(actualRefundAmount);
            refundOrder.setRefundTime(LocalDateTime.now());
            refundOrder.setChannelResponse(channelResponse);
            refundOrderRepository.save(refundOrder);

            // 创建成功记录
            createCallbackRecord(refundOrder, true, null);

            log.info("退款成功回调处理完成，退款订单ID: {}", refundOrderId);
            return true;

        } catch (Exception e) {
            log.error("处理退款成功回调异常，退款订单ID: {}", refundOrderId, e);
            return false;
        }
    }

    /**
     * 处理退款失败回调
     */
    @Override
    public boolean handleRefundFailure(String refundOrderId, String failureReason, String channelResponse) {
        log.info("处理退款失败回调，退款订单ID: {}, 失败原因: {}", refundOrderId, failureReason);

        try {
            RefundOrder refundOrder = refundOrderRepository.findById(refundOrderId)
                    .orElseThrow(() -> new IllegalArgumentException("退款订单不存在"));

            // 更新退款订单状态
            refundOrder.setStatus(RefundStatus.FAILED);
            refundOrder.setFailureReason(failureReason);
            refundOrder.setChannelResponse(channelResponse);
            refundOrderRepository.save(refundOrder);

            // 创建失败记录
            createCallbackRecord(refundOrder, false, failureReason);

            log.info("退款失败回调处理完成，退款订单ID: {}", refundOrderId);
            return true;

        } catch (Exception e) {
            log.error("处理退款失败回调异常，退款订单ID: {}", refundOrderId, e);
            return false;
        }
    }

    /**
     * 取消退款申请
     */
    @Override
    public boolean cancelRefund(String refundOrderId, String reason) {
        log.info("取消退款申请，退款订单ID: {}, 取消原因: {}", refundOrderId, reason);

        RefundOrder refundOrder = refundOrderRepository.findById(refundOrderId)
                .orElseThrow(() -> new IllegalArgumentException("退款订单不存在"));

        // 检查退款订单状态
        if (!canCancelRefund(refundOrder.getStatus())) {
            throw new IllegalStateException("当前状态不允许取消退款");
        }

        // 更新状态为已取消
        refundOrder.setStatus(RefundStatus.CANCELLED);
        refundOrder.setFailureReason("用户取消: " + reason);
        refundOrderRepository.save(refundOrder);

        // 创建取消记录
        createCancelRecord(refundOrder, reason);

        log.info("退款申请取消成功，退款订单ID: {}", refundOrderId);
        return true;
    }

    /**
     * 查询退款状态
     */
    @Override
    @Transactional(readOnly = true)
    public RefundStatus queryRefundStatus(String refundOrderId) {
        log.debug("查询退款状态，退款订单ID: {}", refundOrderId);

        RefundOrder refundOrder = refundOrderRepository.findById(refundOrderId)
                .orElseThrow(() -> new IllegalArgumentException("退款订单不存在"));

        // 如果是处理中状态，尝试同步第三方状态
        if (refundOrder.getStatus() == RefundStatus.PROCESSING && 
            StringUtils.hasText(refundOrder.getThirdPartyRefundNo())) {
            
            try {
                RefundChannelService.RefundQueryResult queryResult = 
                    refundChannelService.queryRefundStatus(
                        refundOrder.getPaymentOrder().getPaymentMethod(),
                        refundOrder.getThirdPartyRefundNo()
                    );
                
                if (queryResult != null && queryResult.getStatus() != null) {
                    RefundStatus latestStatus = queryResult.getStatus();
                    if (latestStatus != refundOrder.getStatus()) {
                        // 更新本地状态
                        refundOrder.setStatus(latestStatus);
                        if (latestStatus == RefundStatus.SUCCESS) {
                            refundOrder.setActualRefundAmount(queryResult.getActualRefundAmount());
                            refundOrder.setRefundTime(LocalDateTime.now());
                        } else if (latestStatus == RefundStatus.FAILED) {
                            refundOrder.setFailureReason(queryResult.getFailureReason());
                        }
                        refundOrderRepository.save(refundOrder);
                        
                        log.info("同步退款状态，退款订单ID: {}, 最新状态: {}", refundOrderId, latestStatus);
                    }

                    return latestStatus;
                }
            } catch (Exception e) {
                log.warn("查询第三方退款状态失败，退款订单ID: {}", refundOrderId, e);
            }
        }

        return refundOrder.getStatus();
    }

    /**
     * 重试退款订单
     */
    @Override
    public boolean retryRefund(String refundOrderId) {
        log.info("重试退款订单，退款订单ID: {}", refundOrderId);

        RefundOrder refundOrder = refundOrderRepository.findById(refundOrderId)
                .orElseThrow(() -> new IllegalArgumentException("退款订单不存在"));

        // 检查是否可以重试
        if (refundOrder.getStatus() != RefundStatus.FAILED) {
            throw new IllegalStateException("只有失败状态的退款订单才能重试");
        }

        if (refundOrder.getRetryCount() >= 3) {
            throw new IllegalStateException("重试次数已达上限");
        }

        // 重新处理退款
        return processRefund(refundOrderId);
    }

    /**
     * 获取退款统计数据
     */
    @Override
    @Transactional(readOnly = true)
    public RefundService.RefundStatistics getRefundStatistics(LocalDateTime startTime, LocalDateTime endTime, String userId) {
        log.debug("获取退款统计数据，开始时间: {}, 结束时间: {}, 用户ID: {}", startTime, endTime, userId);

        // 统计总退款订单数
        long totalRefunds = refundOrderRepository.countByCreatedAtBetween(startTime, endTime);

        // 统计成功退款订单数
        long successRefunds = refundOrderRepository.countByStatusAndCreatedAtBetween(
                RefundStatus.SUCCESS, startTime, endTime);

        // 统计失败退款订单数
        long failedRefunds = refundOrderRepository.countByStatusAndCreatedAtBetween(
                RefundStatus.FAILED, startTime, endTime);

        // 统计待处理退款订单数
        long pendingRefunds = refundOrderRepository.countByStatusAndCreatedAtBetween(
                RefundStatus.PENDING, startTime, endTime);

        // 统计总退款金额
        BigDecimal totalAmount = refundOrderRepository.sumRefundAmountByCreatedAtBetween(
                startTime, endTime);

        // 统计成功退款金额
        BigDecimal successAmount = refundOrderRepository.sumActualRefundAmountByStatusAndRefundTimeBetween(
                RefundStatus.SUCCESS, startTime, endTime);

        return new RefundService.RefundStatistics(totalRefunds, successRefunds, failedRefunds, 
                                                 pendingRefunds, totalAmount, successAmount);
    }

    /**
     * 批量处理待审核的退款申请
     */
    @Override
    public int batchProcessPendingRefunds() {
        log.info("开始批量处理待审核的退款申请");

        List<RefundOrder> pendingRefunds = refundOrderRepository.findByStatus(RefundStatus.PENDING, 
                PageRequest.of(0, 100)).getContent();
        int processedCount = 0;

        for (RefundOrder refundOrder : pendingRefunds) {
            try {
                // 自动审核逻辑（这里可以根据业务规则实现）
                boolean autoApprove = shouldAutoApprove(refundOrder);
                
                if (autoApprove) {
                    auditRefund(refundOrder.getId(), true, "系统自动审核通过", "SYSTEM");
                    processedCount++;
                }
            } catch (Exception e) {
                log.error("自动审核退款申请失败，退款订单ID: {}", refundOrder.getId(), e);
            }
        }

        log.info("批量处理待审核退款申请完成，处理数量: {}", processedCount);
        return processedCount;
    }

    /**
     * 重试失败的退款订单
     * 定时任务调用，重新处理失败状态的退款订单
     * 
     * @return 重试成功的订单数量
     * @author lingbai
     * @since V1.0 2025-01-27: 新增定时任务重试失败退款订单功能
     */
    @Override
    public int retryFailedRefunds() {
        log.info("开始重试失败的退款订单");
        
        try {
            // 查询失败状态且可重试的退款订单（创建时间24小时内，重试次数小于3次）
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
            List<RefundOrder> failedRefunds = refundOrderRepository.findFailedRefundsForRetry(cutoffTime, 3);
            
            if (failedRefunds.isEmpty()) {
                log.info("没有需要重试的失败退款订单");
                return 0;
            }
            
            int successCount = 0;
            for (RefundOrder refundOrder : failedRefunds) {
                try {
                    log.debug("重试退款订单，退款订单ID: {}, 用户ID: {}", 
                             refundOrder.getRefundNo(), refundOrder.getUserId());
                    
                    // 重新发起退款
                    RefundChannelService.RefundResult result = refundChannelService.processRefund(
                        refundOrder.getPaymentOrder().getPaymentMethod(),
                        refundOrder.getPaymentOrder().getThirdPartyOrderNo(),
                        refundOrder.getRefundNo(),
                        refundOrder.getRefundAmount(),
                        refundOrder.getRefundReason()
                    );
                    
                    if (result.isSuccess()) {
                        // 更新退款订单状态为处理中
                        refundOrder.setStatus(RefundStatus.PROCESSING);
                        refundOrder.setThirdPartyRefundNo(result.getThirdPartyRefundNo());
                        refundOrder.setRetryCount(refundOrder.getRetryCount() + 1);
                        refundOrder.setProcessTime(LocalDateTime.now());
                        refundOrderRepository.save(refundOrder);
                        
                        successCount++;
                        log.info("退款订单重试成功，退款订单ID: {}, 第三方退款单号: {}", 
                                refundOrder.getRefundNo(), result.getThirdPartyRefundNo());
                    } else {
                        // 更新重试次数
                        refundOrder.setRetryCount(refundOrder.getRetryCount() + 1);
                        refundOrder.setFailureReason(result.getFailureReason());
                        refundOrderRepository.save(refundOrder);
                        
                        log.warn("退款订单重试失败，退款订单ID: {}, 失败原因: {}", 
                                refundOrder.getRefundNo(), result.getFailureReason());
                    }
                    
                } catch (Exception e) {
                    log.error("重试退款订单异常，退款订单ID: {}, 错误: {}", 
                             refundOrder.getRefundNo(), e.getMessage(), e);
                }
            }
            
            log.info("退款订单重试完成，总数: {}, 成功: {}", failedRefunds.size(), successCount);
            return successCount;
            
        } catch (Exception e) {
            log.error("重试失败退款订单异常: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 同步退款状态
     * 定时任务调用，从第三方支付平台同步退款状态
     * 
     * @return 同步成功的订单数量
     * @author lingbai
     * @since V1.0 2025-01-27: 新增定时任务同步退款状态功能
     */
    @Override
    public int syncRefundStatus() {
        log.info("开始同步退款状态");
        
        try {
            // 查询处理中状态的退款订单（创建时间48小时内）
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(48);
            List<RefundOrder> processingRefunds = refundOrderRepository.findProcessingRefundsForSync(cutoffTime);
            
            if (processingRefunds.isEmpty()) {
                log.info("没有需要同步状态的退款订单");
                return 0;
            }
            
            int syncCount = 0;
            for (RefundOrder refundOrder : processingRefunds) {
                try {
                    log.debug("同步退款状态，退款订单ID: {}, 第三方退款单号: {}", 
                             refundOrder.getId(), refundOrder.getThirdPartyRefundNo());
                    
                    // 查询第三方退款状态
                    RefundChannelService.RefundQueryResult queryResult = 
                        refundChannelService.queryRefundStatus(
                            refundOrder.getPaymentOrder().getPaymentMethod(),
                            refundOrder.getThirdPartyRefundNo()
                        );
                    
                    if (queryResult != null && queryResult.getStatus() != null) {
                        RefundStatus oldStatus = refundOrder.getStatus();
                        RefundStatus newStatus = queryResult.getStatus();
                        
                        if (newStatus != oldStatus) {
                            // 更新本地状态
                            refundOrder.setStatus(newStatus);
                            
                            if (newStatus == RefundStatus.SUCCESS) {
                                refundOrder.setActualRefundAmount(queryResult.getActualRefundAmount());
                                refundOrder.setRefundTime(LocalDateTime.now());
                            } else if (newStatus == RefundStatus.FAILED) {
                                refundOrder.setFailureReason(queryResult.getFailureReason());
                            }
                            
                            refundOrderRepository.save(refundOrder);
                            syncCount++;
                            
                            log.info("退款状态同步成功，退款订单ID: {}, 状态变更: {} -> {}", 
                                    refundOrder.getId(), oldStatus, newStatus);
                        }
                    }
                    
                } catch (Exception e) {
                    log.error("同步退款状态异常，退款订单ID: {}, 错误: {}", 
                             refundOrder.getId(), e.getMessage(), e);
                }
            }
            
            log.info("退款状态同步完成，总数: {}, 同步: {}", processingRefunds.size(), syncCount);
            return syncCount;
            
        } catch (Exception e) {
            log.error("同步退款状态异常: {}", e.getMessage(), e);
            return 0;
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 验证退款请求参数
     */
    private void validateRefundRequest(RefundCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("退款请求不能为空");
        }
        if (!StringUtils.hasText(request.getPaymentOrderId())) {
            throw new IllegalArgumentException("支付订单ID不能为空");
        }
        if (request.getRefundAmount() == null || request.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("退款金额必须大于0");
        }
        if (!StringUtils.hasText(request.getRefundReason())) {
            throw new IllegalArgumentException("退款原因不能为空");
        }
    }

    /**
     * 验证支付订单是否可以退款
     */
    private void validatePaymentOrderForRefund(PaymentOrder paymentOrder, BigDecimal refundAmount) {
        // 检查支付订单状态
        if (!paymentOrder.canRefund()) {
            throw new IllegalStateException("支付订单状态不允许退款");
        }

        // 检查退款金额
        BigDecimal availableRefundAmount = paymentOrder.getRefundableAmount();
        if (refundAmount.compareTo(availableRefundAmount) > 0) {
            throw new IllegalArgumentException("退款金额超过可退款金额，可退款金额: " + availableRefundAmount);
        }
    }

    /**
     * 生成退款订单ID
     */
    private String generateRefundOrderId() {
        return "RF" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 创建退款订单实体
     */
    private RefundOrder createRefundOrderEntity(RefundCreateRequest request, PaymentOrder paymentOrder) {
        RefundOrder refundOrder = new RefundOrder();
        refundOrder.setRefundNo(generateRefundOrderId());
        refundOrder.setPaymentOrderId(paymentOrder.getId());
        refundOrder.setUserId(paymentOrder.getUserId());
        refundOrder.setRefundAmount(request.getRefundAmount());
        refundOrder.setRefundReason(request.getRefundReason());
        refundOrder.setRefundType(request.getRefundType());
        refundOrder.setStatus(RefundStatus.PENDING);
        refundOrder.setRetryCount(0);
        refundOrder.setCreatedAt(LocalDateTime.now());
        refundOrder.setUpdatedAt(LocalDateTime.now());
        return refundOrder;
    }

    /**
     * 构建查询条件并执行查询
     */
    private Page<RefundOrder> buildQueryAndExecute(String userId, RefundStatus status, 
                                                  LocalDateTime startTime, LocalDateTime endTime, 
                                                  Pageable pageable) {
        if (StringUtils.hasText(userId) && status != null && startTime != null && endTime != null) {
            return refundOrderRepository.findByUserIdAndStatusAndCreatedAtBetween(
                    userId, status, startTime, endTime, pageable);
        } else if (StringUtils.hasText(userId) && status != null) {
            return refundOrderRepository.findByUserIdAndStatus(userId, status, pageable);
        } else if (StringUtils.hasText(userId) && startTime != null && endTime != null) {
            return refundOrderRepository.findByUserIdAndCreatedAtBetween(userId, startTime, endTime, pageable);
        } else if (StringUtils.hasText(userId)) {
            return refundOrderRepository.findByUserId(userId, pageable);
        } else if (status != null && startTime != null && endTime != null) {
            return refundOrderRepository.findByStatusAndCreatedAtBetween(status, startTime, endTime, pageable);
        } else if (status != null) {
            return refundOrderRepository.findByStatus(status, pageable);
        } else if (startTime != null && endTime != null) {
            return refundOrderRepository.findByCreatedAtBetween(startTime, endTime, pageable);
        } else {
            return refundOrderRepository.findAll(pageable);
        }
    }

    /**
     * 创建退款处理记录
     */
    private void createRefundProcessRecord(RefundOrder refundOrder, RefundChannelService.RefundResult result) {
        RefundRecord record = new RefundRecord();
        record.setRefundOrderId(refundOrder.getId());
        record.setRefundAmount(refundOrder.getRefundAmount());
        record.setStatus(result.isSuccess() ? RefundStatus.SUCCESS : RefundStatus.FAILED);
        record.setRemark(result.isSuccess() ? "退款处理成功" : "退款处理失败: " + result.getFailureReason());
        if (!result.isSuccess()) {
            record.setErrorMessage(result.getFailureReason());
        }
        refundRecordRepository.save(record);
    }

    /**
     * 创建审核记录
     */
    private void createAuditRecord(RefundOrder refundOrder, boolean approved) {
        RefundRecord record = new RefundRecord();
        record.setRefundOrderId(refundOrder.getId());
        record.setRefundAmount(refundOrder.getRefundAmount());
        record.setStatus(approved ? RefundStatus.APPROVED : RefundStatus.REJECTED);
        record.setRemark(approved ? "审核通过" : "审核拒绝: " + refundOrder.getReviewRemark());
        refundRecordRepository.save(record);
    }

    /**
     * 创建回调记录
     */
    private void createCallbackRecord(RefundOrder refundOrder, boolean success, String reason) {
        RefundRecord record = new RefundRecord();
        record.setRefundOrderId(refundOrder.getId());
        record.setRefundAmount(refundOrder.getRefundAmount());
        record.setStatus(success ? RefundStatus.SUCCESS : RefundStatus.FAILED);
        record.setRemark(success ? "退款成功回调" : "退款失败回调: " + reason);
        if (!success) {
            record.setErrorMessage(reason);
        }
        refundRecordRepository.save(record);
    }

    /**
     * 创建取消记录
     */
    private void createCancelRecord(RefundOrder refundOrder, String reason) {
        RefundRecord record = new RefundRecord();
        record.setRefundOrderId(refundOrder.getId());
        record.setRefundAmount(refundOrder.getRefundAmount());
        record.setStatus(RefundStatus.CANCELLED);
        record.setRemark("退款取消: " + reason);
        refundRecordRepository.save(record);
    }

    /**
     * 判断是否可以取消退款
     */
    private boolean canCancelRefund(RefundStatus status) {
        return status == RefundStatus.PENDING || status == RefundStatus.APPROVED;
    }

    /**
     * 判断是否应该自动审核通过
     */
    private boolean shouldAutoApprove(RefundOrder refundOrder) {
        // 这里可以根据业务规则实现自动审核逻辑
        // 例如：小额退款自动通过，大额退款需要人工审核
        return refundOrder.getRefundAmount().compareTo(new BigDecimal("100")) <= 0;
    }

    /**
     * 转换为响应对象
     */
    private RefundOrderResponse convertToResponse(RefundOrder refundOrder) {
        RefundOrderResponse response = new RefundOrderResponse();
        response.setId(refundOrder.getId());
        response.setRefundNo(refundOrder.getRefundNo());
        response.setPaymentOrderId(refundOrder.getPaymentOrderId());
        response.setUserId(refundOrder.getUserId());
        response.setRefundAmount(refundOrder.getRefundAmount());
        response.setActualRefundAmount(refundOrder.getActualRefundAmount());
        response.setRefundReason(refundOrder.getRefundReason());
        response.setRefundType(refundOrder.getRefundType());
        response.setStatus(refundOrder.getStatus());
        response.setThirdPartyRefundNo(refundOrder.getThirdPartyRefundNo());
        response.setFailureReason(refundOrder.getFailureReason());
        response.setRetryCount(refundOrder.getRetryCount());
        response.setReviewTime(refundOrder.getReviewTime());
        response.setReviewRemark(refundOrder.getReviewRemark());
        response.setReviewerId(refundOrder.getReviewerId());
        response.setProcessTime(refundOrder.getProcessTime());
        response.setRefundTime(refundOrder.getRefundTime());
        response.setCreatedAt(refundOrder.getCreatedAt());
        response.setUpdatedAt(refundOrder.getUpdatedAt());
        return response;
    }
    
    /**
     * 清理过期的退款记录
     * 清理指定时间之前的已完成退款记录
     * 
     * @param cutoffTime 截止时间，早于此时间的记录将被清理
     * @return 清理的记录数量
     */
    @Override
    public int cleanupExpiredRecords(LocalDateTime cutoffTime) {
        log.info("开始清理过期退款记录，截止时间: {}", cutoffTime);
        
        try {
            // 查找需要清理的退款订单（已完成状态且创建时间早于截止时间）
            List<RefundOrder> expiredOrders = refundOrderRepository.findByStatusAndCreatedAtBefore(
                    RefundStatus.SUCCESS, cutoffTime);
            
            if (expiredOrders.isEmpty()) {
                log.info("没有找到需要清理的过期退款记录");
                return 0;
            }
            
            int cleanedCount = 0;
            for (RefundOrder order : expiredOrders) {
                try {
                    // 删除相关的退款记录
                    refundRecordRepository.deleteByRefundOrderId(order.getId());
                    
                    // 删除退款订单
                    refundOrderRepository.delete(order);
                    
                    cleanedCount++;
                    log.debug("清理过期退款记录成功，退款订单ID: {}", order.getId());
                    
                } catch (Exception e) {
                    log.error("清理过期退款记录失败，退款订单ID: {}, 错误: {}", 
                             order.getId(), e.getMessage(), e);
                }
            }
            
            log.info("过期退款记录清理完成，总数: {}, 清理: {}", expiredOrders.size(), cleanedCount);
            return cleanedCount;
            
        } catch (Exception e) {
            log.error("清理过期退款记录异常: {}", e.getMessage(), e);
            return 0;
        }
    }
}