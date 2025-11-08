package com.mall.payment.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.payment.dto.request.PaymentCreateRequest;
import com.mall.payment.entity.RiskRecord;
import com.mall.payment.entity.RiskRule;
import com.mall.payment.exception.PaymentException;
import com.mall.payment.repository.RiskRecordRepository;
import com.mall.payment.repository.RiskRuleRepository;
import com.mall.payment.service.RiskControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 风控服务实现类
 * 实现支付风控相关的业务逻辑
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@Service
@Transactional
public class RiskControlServiceImpl implements RiskControlService {

    private static final Logger logger = LoggerFactory.getLogger(RiskControlServiceImpl.class);

    @Autowired
    private RiskRuleRepository riskRuleRepository;

    @Autowired
    private RiskRecordRepository riskRecordRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 执行风控检查
     * 对支付请求进行全面的风控检查
     * 
     * @param request 支付创建请求
     * @param paymentOrderId 支付订单ID
     * @return 风控检查结果
     */
    @Override
    public RiskCheckResult performRiskCheck(PaymentCreateRequest request, String paymentOrderId) {
        logger.info("开始执行风控检查，支付订单ID: {}, 用户ID: {}, 支付金额: {}", 
                   paymentOrderId, request.getUserId(), request.getAmount());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 获取适用的风控规则
            List<RiskRule> applicableRules = getApplicableRules(request.getPaymentMethod());
            logger.debug("获取到 {} 条适用的风控规则", applicableRules.size());
            
            // 执行风控检查
            RiskCheckResult result = executeRiskCheck(request, paymentOrderId, applicableRules);
            
            // 记录处理时间
            long processingTime = System.currentTimeMillis() - startTime;
            result.setProcessingTimeMs(processingTime);
            
            // 保存风控记录
            saveRiskRecord(request, paymentOrderId, result);
            
            logger.info("风控检查完成，支付订单ID: {}, 结果: {}, 风险评分: {}, 处理时间: {}ms", 
                       paymentOrderId, result.getResult(), result.getRiskScore(), processingTime);
            
            return result;
            
        } catch (Exception e) {
            logger.error("风控检查异常，支付订单ID: {}", paymentOrderId, e);
            throw PaymentException.systemError("风控检查异常: " + e.getMessage());
        }
    }

    /**
     * 根据支付订单ID查询风控记录
     * 
     * @param paymentOrderId 支付订单ID
     * @return 风控记录
     */
    @Override
    @Transactional(readOnly = true)
    public RiskRecord getRiskRecordByPaymentOrderId(String paymentOrderId) {
        logger.debug("查询风控记录，支付订单ID: {}", paymentOrderId);
        
        if (!StringUtils.hasText(paymentOrderId)) {
            throw PaymentException.invalidParameter("支付订单ID不能为空");
        }
        
        return riskRecordRepository.findByPaymentOrderId(paymentOrderId)
                .orElseThrow(() -> PaymentException.recordNotFound("风控记录不存在"));
    }

    /**
     * 根据业务订单ID查询风控记录
     * 
     * @param businessOrderId 业务订单ID
     * @return 风控记录
     */
    @Override
    @Transactional(readOnly = true)
    public RiskRecord getRiskRecordByBusinessOrderId(String businessOrderId) {
        logger.debug("查询风控记录，业务订单ID: {}", businessOrderId);
        
        if (!StringUtils.hasText(businessOrderId)) {
            throw PaymentException.invalidParameter("业务订单ID不能为空");
        }
        
        return riskRecordRepository.findByBusinessOrderId(businessOrderId)
                .orElseThrow(() -> PaymentException.recordNotFound("风控记录不存在"));
    }

    /**
     * 分页查询风控记录
     * 
     * @param pageable 分页参数
     * @return 风控记录分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public Page<RiskRecord> getRiskRecords(Pageable pageable) {
        logger.debug("分页查询风控记录，页码: {}, 页大小: {}", pageable.getPageNumber(), pageable.getPageSize());
        return riskRecordRepository.findAll(pageable);
    }

    /**
     * 根据用户ID分页查询风控记录
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 风控记录分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public Page<RiskRecord> getRiskRecordsByUserId(String userId, Pageable pageable) {
        logger.debug("根据用户ID查询风控记录，用户ID: {}", userId);
        
        if (!StringUtils.hasText(userId)) {
            throw PaymentException.invalidParameter("用户ID不能为空");
        }
        
        return riskRecordRepository.findByUserId(userId, pageable);
    }

    /**
     * 查询待审核的风控记录
     * 
     * @param pageable 分页参数
     * @return 待审核的风控记录分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public Page<RiskRecord> getPendingReviewRecords(Pageable pageable) {
        logger.debug("查询待审核的风控记录");
        return riskRecordRepository.findPendingReviewRecords(pageable);
    }

    /**
     * 审核风控记录
     * 
     * @param recordId 记录ID
     * @param status 审核状态
     * @param reviewer 审核人
     * @param comment 审核备注
     * @return 是否审核成功
     */
    @Override
    public boolean reviewRiskRecord(String recordId, RiskRecord.ReviewStatus status, String reviewer, String comment) {
        logger.info("审核风控记录，记录ID: {}, 审核状态: {}, 审核人: {}", recordId, status, reviewer);
        
        if (!StringUtils.hasText(recordId)) {
            throw PaymentException.invalidParameter("记录ID不能为空");
        }
        
        if (status == null) {
            throw PaymentException.invalidParameter("审核状态不能为空");
        }
        
        if (!StringUtils.hasText(reviewer)) {
            throw PaymentException.invalidParameter("审核人不能为空");
        }
        
        try {
            RiskRecord record = riskRecordRepository.findById(recordId)
                    .orElseThrow(() -> PaymentException.recordNotFound("风控记录不存在"));
            
            // 检查记录是否需要审核
            if (!record.requiresManualReview()) {
                throw PaymentException.invalidState("该记录不需要人工审核");
            }
            
            // 检查是否已经审核过
            if (record.isReviewCompleted()) {
                throw PaymentException.invalidState("该记录已经审核过");
            }
            
            // 完成审核
            record.completeReview(status, reviewer, comment);
            riskRecordRepository.save(record);
            
            logger.info("风控记录审核完成，记录ID: {}, 审核结果: {}", recordId, status);
            return true;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("审核风控记录异常，记录ID: {}", recordId, e);
            throw PaymentException.systemError("审核风控记录异常: " + e.getMessage());
        }
    }

    /**
     * 标记风控记录为误报
     * 
     * @param recordId 记录ID
     * @param reviewer 标记人
     * @param comment 备注
     * @return 是否标记成功
     */
    @Override
    public boolean markAsFalsePositive(String recordId, String reviewer, String comment) {
        logger.info("标记风控记录为误报，记录ID: {}, 标记人: {}", recordId, reviewer);
        
        if (!StringUtils.hasText(recordId)) {
            throw PaymentException.invalidParameter("记录ID不能为空");
        }
        
        if (!StringUtils.hasText(reviewer)) {
            throw PaymentException.invalidParameter("标记人不能为空");
        }
        
        try {
            RiskRecord record = riskRecordRepository.findById(recordId)
                    .orElseThrow(() -> PaymentException.recordNotFound("风控记录不存在"));
            
            // 标记为误报
            record.markAsFalsePositive(reviewer, comment);
            riskRecordRepository.save(record);
            
            logger.info("风控记录已标记为误报，记录ID: {}", recordId);
            return true;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("标记误报异常，记录ID: {}", recordId, e);
            throw PaymentException.systemError("标记误报异常: " + e.getMessage());
        }
    }

    /**
     * 查询高风险交易记录
     * 
     * @param minRiskScore 最小风险评分
     * @param pageable 分页参数
     * @return 高风险交易记录分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public Page<RiskRecord> getHighRiskRecords(BigDecimal minRiskScore, Pageable pageable) {
        logger.debug("查询高风险交易记录，最小风险评分: {}", minRiskScore);
        
        if (minRiskScore == null || minRiskScore.compareTo(BigDecimal.ZERO) < 0) {
            throw PaymentException.invalidParameter("最小风险评分必须大于等于0");
        }
        
        return riskRecordRepository.findHighRiskRecords(minRiskScore, pageable);
    }

    /**
     * 获取风控统计数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 风控统计数据
     */
    @Override
    @Transactional(readOnly = true)
    public RiskStatistics getRiskStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        logger.debug("获取风控统计数据，时间范围: {} - {}", startTime, endTime);
        
        if (startTime == null || endTime == null) {
            throw PaymentException.invalidParameter("开始时间和结束时间不能为空");
        }
        
        if (startTime.isAfter(endTime)) {
            throw PaymentException.invalidParameter("开始时间不能晚于结束时间");
        }
        
        try {
            RiskStatistics statistics = new RiskStatistics();
            
            // 统计总交易数量
            long totalTransactions = riskRecordRepository.countByCreatedAtBetween(startTime, endTime);
            statistics.setTotalTransactions(totalTransactions);
            
            if (totalTransactions > 0) {
                // 统计各种结果的数量
                long blockedTransactions = riskRecordRepository.countByResultAndCreatedAtBetween(
                        RiskRecord.RiskResult.BLOCKED, startTime, endTime);
                long manualReviewTransactions = riskRecordRepository.countByResultAndCreatedAtBetween(
                        RiskRecord.RiskResult.MANUAL_REVIEW, startTime, endTime);
                long warningTransactions = riskRecordRepository.countByResultAndCreatedAtBetween(
                        RiskRecord.RiskResult.WARNING, startTime, endTime);
                long passedTransactions = riskRecordRepository.countByResultAndCreatedAtBetween(
                        RiskRecord.RiskResult.PASSED, startTime, endTime);
                
                statistics.setBlockedTransactions(blockedTransactions);
                statistics.setManualReviewTransactions(manualReviewTransactions);
                statistics.setWarningTransactions(warningTransactions);
                statistics.setPassedTransactions(passedTransactions);
                
                // 计算比率
                statistics.setBlockRate(BigDecimal.valueOf(blockedTransactions)
                        .divide(BigDecimal.valueOf(totalTransactions), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)));
                statistics.setManualReviewRate(BigDecimal.valueOf(manualReviewTransactions)
                        .divide(BigDecimal.valueOf(totalTransactions), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)));
                
                // 统计风险评分
                BigDecimal avgRiskScore = riskRecordRepository.avgRiskScoreByCreatedAtBetween(startTime, endTime);
                BigDecimal maxRiskScore = riskRecordRepository.maxRiskScoreByCreatedAtBetween(startTime, endTime);
                statistics.setAvgRiskScore(avgRiskScore);
                statistics.setMaxRiskScore(maxRiskScore);
                
                // 统计处理时间
                Double avgProcessingTime = riskRecordRepository.avgProcessingTimeByCreatedAtBetween(startTime, endTime);
                statistics.setAvgProcessingTime(avgProcessingTime);
                
                // 统计误报
                long falsePositives = riskRecordRepository.findByIsFalsePositive(true, Pageable.unpaged()).getTotalElements();
                statistics.setFalsePositives(falsePositives);
                if (blockedTransactions + manualReviewTransactions > 0) {
                    statistics.setFalsePositiveRate(BigDecimal.valueOf(falsePositives)
                            .divide(BigDecimal.valueOf(blockedTransactions + manualReviewTransactions), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)));
                } else {
                    statistics.setFalsePositiveRate(BigDecimal.ZERO);
                }
            }
            
            logger.debug("风控统计数据获取完成，总交易数: {}", totalTransactions);
            return statistics;
            
        } catch (Exception e) {
            logger.error("获取风控统计数据异常", e);
            throw PaymentException.systemError("获取风控统计数据异常: " + e.getMessage());
        }
    }

    /**
     * 创建风控规则
     * 
     * @param rule 风控规则
     * @return 创建的风控规则
     */
    @Override
    public RiskRule createRiskRule(RiskRule rule) {
        logger.info("创建风控规则，规则名称: {}", rule.getRuleName());
        
        validateRiskRule(rule);
        
        // 检查规则名称是否已存在
        if (riskRuleRepository.existsByRuleName(rule.getRuleName())) {
            throw PaymentException.invalidParameter("规则名称已存在");
        }
        
        // 生成规则ID
        rule.setRuleId(generateRuleId());
        
        // 设置默认优先级
        if (rule.getPriority() == null) {
            Integer maxPriority = riskRuleRepository.findMaxPriority();
            rule.setPriority(maxPriority + 10);
        }
        
        try {
            RiskRule savedRule = riskRuleRepository.save(rule);
            logger.info("风控规则创建成功，规则ID: {}", savedRule.getRuleId());
            return savedRule;
            
        } catch (Exception e) {
            logger.error("创建风控规则异常，规则名称: {}", rule.getRuleName(), e);
            throw PaymentException.systemError("创建风控规则异常: " + e.getMessage());
        }
    }

    /**
     * 更新风控规则
     * 
     * @param ruleId 规则ID
     * @param rule 风控规则
     * @return 更新的风控规则
     */
    @Override
    public RiskRule updateRiskRule(String ruleId, RiskRule rule) {
        logger.info("更新风控规则，规则ID: {}", ruleId);
        
        if (!StringUtils.hasText(ruleId)) {
            throw PaymentException.invalidParameter("规则ID不能为空");
        }
        
        validateRiskRule(rule);
        
        try {
            RiskRule existingRule = riskRuleRepository.findById(ruleId)
                    .orElseThrow(() -> PaymentException.recordNotFound("风控规则不存在"));
            
            // 检查规则名称是否已存在（排除当前规则）
            if (riskRuleRepository.existsByRuleNameAndRuleIdNot(rule.getRuleName(), ruleId)) {
                throw PaymentException.invalidParameter("规则名称已存在");
            }
            
            // 更新规则信息
            existingRule.setRuleName(rule.getRuleName());
            existingRule.setRuleType(rule.getRuleType());
            existingRule.setPaymentMethod(rule.getPaymentMethod());
            existingRule.setRuleConfig(rule.getRuleConfig());
            existingRule.setThresholdValue(rule.getThresholdValue());
            existingRule.setTimeWindowSeconds(rule.getTimeWindowSeconds());
            existingRule.setRiskLevel(rule.getRiskLevel());
            existingRule.setAction(rule.getAction());
            existingRule.setEnabled(rule.getEnabled());
            existingRule.setPriority(rule.getPriority());
            existingRule.setDescription(rule.getDescription());
            existingRule.setUpdatedBy(rule.getUpdatedBy());
            
            RiskRule savedRule = riskRuleRepository.save(existingRule);
            logger.info("风控规则更新成功，规则ID: {}", ruleId);
            return savedRule;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("更新风控规则异常，规则ID: {}", ruleId, e);
            throw PaymentException.systemError("更新风控规则异常: " + e.getMessage());
        }
    }

    /**
     * 删除风控规则
     * 
     * @param ruleId 规则ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteRiskRule(String ruleId) {
        logger.info("删除风控规则，规则ID: {}", ruleId);
        
        if (!StringUtils.hasText(ruleId)) {
            throw PaymentException.invalidParameter("规则ID不能为空");
        }
        
        try {
            if (!riskRuleRepository.existsById(ruleId)) {
                throw PaymentException.recordNotFound("风控规则不存在");
            }
            
            riskRuleRepository.deleteById(ruleId);
            logger.info("风控规则删除成功，规则ID: {}", ruleId);
            return true;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("删除风控规则异常，规则ID: {}", ruleId, e);
            throw PaymentException.systemError("删除风控规则异常: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询风控规则
     * 
     * @param ruleId 规则ID
     * @return 风控规则
     */
    @Override
    @Transactional(readOnly = true)
    public RiskRule getRiskRuleById(String ruleId) {
        logger.debug("查询风控规则，规则ID: {}", ruleId);
        
        if (!StringUtils.hasText(ruleId)) {
            throw PaymentException.invalidParameter("规则ID不能为空");
        }
        
        return riskRuleRepository.findById(ruleId)
                .orElseThrow(() -> PaymentException.recordNotFound("风控规则不存在"));
    }

    /**
     * 分页查询风控规则
     * 
     * @param pageable 分页参数
     * @return 风控规则分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public Page<RiskRule> getRiskRules(Pageable pageable) {
        logger.debug("分页查询风控规则，页码: {}, 页大小: {}", pageable.getPageNumber(), pageable.getPageSize());
        return riskRuleRepository.findAll(pageable);
    }

    /**
     * 查询启用的风控规则
     * 
     * @return 启用的风控规则列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<RiskRule> getEnabledRiskRules() {
        logger.debug("查询启用的风控规则");
        return riskRuleRepository.findEnabledRulesOrderByPriority();
    }

    /**
     * 启用/禁用风控规则
     * 
     * @param ruleId 规则ID
     * @param enabled 是否启用
     * @return 是否操作成功
     */
    @Override
    public boolean toggleRiskRule(String ruleId, boolean enabled) {
        logger.info("{}风控规则，规则ID: {}", enabled ? "启用" : "禁用", ruleId);
        
        if (!StringUtils.hasText(ruleId)) {
            throw PaymentException.invalidParameter("规则ID不能为空");
        }
        
        try {
            RiskRule rule = riskRuleRepository.findById(ruleId)
                    .orElseThrow(() -> PaymentException.recordNotFound("风控规则不存在"));
            
            rule.setEnabled(enabled);
            riskRuleRepository.save(rule);
            
            logger.info("风控规则状态更新成功，规则ID: {}, 状态: {}", ruleId, enabled ? "启用" : "禁用");
            return true;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("更新风控规则状态异常，规则ID: {}", ruleId, e);
            throw PaymentException.systemError("更新风控规则状态异常: " + e.getMessage());
        }
    }

    /**
     * 批量处理待审核的风控记录
     * 
     * @param timeoutHours 超时小时数
     * @return 处理的记录数量
     */
    @Override
    public int processPendingReviewRecords(int timeoutHours) {
        logger.info("开始批量处理待审核的风控记录，超时时间: {} 小时", timeoutHours);
        
        if (timeoutHours <= 0) {
            throw PaymentException.invalidParameter("超时时间必须大于0");
        }
        
        try {
            LocalDateTime timeoutTime = LocalDateTime.now().minusHours(timeoutHours);
            
            // 查询超时的待审核记录
            List<RiskRecord> timeoutRecords = riskRecordRepository.findPendingReviewRecords(Pageable.unpaged())
                    .getContent()
                    .stream()
                    .filter(record -> record.getCreatedAt().isBefore(timeoutTime))
                    .collect(Collectors.toList());
            
            int processedCount = 0;
            
            for (RiskRecord record : timeoutRecords) {
                try {
                    // 根据风险等级决定处理方式
                    RiskRecord.ReviewStatus status;
                    String comment;
                    
                    if (record.getRiskLevel() == RiskRule.RiskLevel.CRITICAL) {
                        // 严重风险自动拒绝
                        status = RiskRecord.ReviewStatus.REJECTED;
                        comment = "系统自动拒绝：严重风险交易超时未审核";
                    } else {
                        // 其他风险等级自动通过
                        status = RiskRecord.ReviewStatus.APPROVED;
                        comment = "系统自动通过：超时未审核";
                    }
                    
                    record.completeReview(status, "SYSTEM", comment);
                    riskRecordRepository.save(record);
                    processedCount++;
                    
                    logger.debug("处理超时审核记录，记录ID: {}, 处理结果: {}", record.getRecordId(), status);
                    
                } catch (Exception e) {
                    logger.error("处理超时审核记录异常，记录ID: {}", record.getRecordId(), e);
                }
            }
            
            logger.info("批量处理待审核记录完成，处理数量: {}", processedCount);
            return processedCount;
            
        } catch (Exception e) {
            logger.error("批量处理待审核记录异常", e);
            throw PaymentException.systemError("批量处理待审核记录异常: " + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 获取适用的风控规则
     * 
     * @param paymentMethod 支付方式
     * @return 适用的风控规则列表
     */
    private List<RiskRule> getApplicableRules(com.mall.payment.enums.PaymentMethod paymentMethod) {
        return riskRuleRepository.findEnabledRulesByPaymentMethod(paymentMethod);
    }

    /**
     * 执行风控检查
     * 
     * @param request 支付请求
     * @param paymentOrderId 支付订单ID
     * @param rules 风控规则列表
     * @return 风控检查结果
     */
    private RiskCheckResult executeRiskCheck(PaymentCreateRequest request, String paymentOrderId, List<RiskRule> rules) {
        List<String> triggeredRules = new ArrayList<>();
        BigDecimal totalRiskScore = BigDecimal.ZERO;
        RiskRule.RiskLevel maxRiskLevel = RiskRule.RiskLevel.LOW;
        RiskRule.RiskAction finalAction = RiskRule.RiskAction.ALLOW;
        List<String> reasons = new ArrayList<>();
        
        // 逐个检查规则
        for (RiskRule rule : rules) {
            try {
                boolean triggered = checkRule(rule, request);
                
                if (triggered) {
                    triggeredRules.add(rule.getRuleId());
                    totalRiskScore = totalRiskScore.add(BigDecimal.valueOf(rule.getRiskWeight()));
                    
                    // 更新最高风险等级
                    if (rule.getRiskLevel().ordinal() > maxRiskLevel.ordinal()) {
                        maxRiskLevel = rule.getRiskLevel();
                    }
                    
                    // 更新最终处理动作（优先级：BLOCK > MANUAL_REVIEW > WARN > ALLOW）
                    if (rule.getAction().ordinal() > finalAction.ordinal()) {
                        finalAction = rule.getAction();
                    }
                    
                    reasons.add(rule.getRuleName());
                    
                    logger.debug("触发风控规则，规则ID: {}, 规则名称: {}, 风险等级: {}, 处理动作: {}", 
                               rule.getRuleId(), rule.getRuleName(), rule.getRiskLevel(), rule.getAction());
                }
                
            } catch (Exception e) {
                logger.error("检查风控规则异常，规则ID: {}", rule.getRuleId(), e);
            }
        }
        
        // 构建检查结果
        RiskCheckResult result = new RiskCheckResult();
        result.setRiskScore(totalRiskScore);
        result.setRiskLevel(maxRiskLevel);
        result.setAction(finalAction);
        result.setTriggeredRules(triggeredRules);
        result.setReason(String.join(", ", reasons));
        
        // 确定最终结果
        switch (finalAction) {
            case BLOCK:
                result.setResult(RiskRecord.RiskResult.BLOCKED);
                result.setPassed(false);
                break;
            case MANUAL_REVIEW:
                result.setResult(RiskRecord.RiskResult.MANUAL_REVIEW);
                result.setPassed(false);
                break;
            case WARN:
                result.setResult(RiskRecord.RiskResult.WARNING);
                result.setPassed(true);
                break;
            default:
                result.setResult(RiskRecord.RiskResult.PASSED);
                result.setPassed(true);
                break;
        }
        
        return result;
    }

    /**
     * 检查单个风控规则
     * 
     * @param rule 风控规则
     * @param request 支付请求
     * @return 是否触发规则
     */
    private boolean checkRule(RiskRule rule, PaymentCreateRequest request) {
        switch (rule.getRuleType()) {
            case AMOUNT_LIMIT:
                return checkAmountLimit(rule, request);
            case FREQUENCY_LIMIT:
                return checkFrequencyLimit(rule, request);
            case IP_BLACKLIST:
                return checkIpBlacklist(rule, request);
            case DEVICE_LIMIT:
                return checkDeviceLimit(rule, request);
            case TIME_LIMIT:
                return checkTimeLimit(rule, request);
            case VELOCITY_CHECK:
                return checkVelocity(rule, request);
            default:
                logger.warn("未知的风控规则类型: {}", rule.getRuleType());
                return false;
        }
    }

    /**
     * 检查金额限制规则
     * 
     * @param rule 风控规则
     * @param request 支付请求
     * @return 是否触发规则
     */
    private boolean checkAmountLimit(RiskRule rule, PaymentCreateRequest request) {
        if (rule.getThresholdValue() == null) {
            return false;
        }
        
        return request.getAmount().compareTo(rule.getThresholdValue()) > 0;
    }

    /**
     * 检查频率限制规则
     * 
     * @param rule 风控规则
     * @param request 支付请求
     * @return 是否触发规则
     */
    private boolean checkFrequencyLimit(RiskRule rule, PaymentCreateRequest request) {
        if (rule.getThresholdValue() == null || rule.getTimeWindowSeconds() == null) {
            return false;
        }
        
        LocalDateTime startTime = LocalDateTime.now().minusSeconds(rule.getTimeWindowSeconds());
        LocalDateTime endTime = LocalDateTime.now();
        
        long count = riskRecordRepository.countByUserIdAndCreatedAtBetween(
                request.getUserId(), startTime, endTime);
        
        return count >= rule.getThresholdValue().longValue();
    }

    /**
     * 检查IP黑名单规则
     * 
     * @param rule 风控规则
     * @param request 支付请求
     * @return 是否触发规则
     */
    private boolean checkIpBlacklist(RiskRule rule, PaymentCreateRequest request) {
        if (!StringUtils.hasText(rule.getRuleConfig()) || !StringUtils.hasText(request.getClientIp())) {
            return false;
        }
        
        try {
            // 解析IP黑名单配置
            String[] blacklistIps = rule.getRuleConfig().split(",");
            for (String ip : blacklistIps) {
                if (request.getClientIp().trim().equals(ip.trim())) {
                    return true;
                }
            }
            return false;
            
        } catch (Exception e) {
            logger.error("检查IP黑名单规则异常", e);
            return false;
        }
    }

    /**
     * 检查设备限制规则
     * 
     * @param rule 风控规则
     * @param request 支付请求
     * @return 是否触发规则
     */
    private boolean checkDeviceLimit(RiskRule rule, PaymentCreateRequest request) {
        if (rule.getThresholdValue() == null || rule.getTimeWindowSeconds() == null) {
            return false;
        }
        
        String deviceFingerprint = extractDeviceFingerprint(request);
        if (!StringUtils.hasText(deviceFingerprint)) {
            return false;
        }
        
        LocalDateTime startTime = LocalDateTime.now().minusSeconds(rule.getTimeWindowSeconds());
        LocalDateTime endTime = LocalDateTime.now();
        
        long count = riskRecordRepository.countByDeviceFingerprintAndCreatedAtBetween(
                deviceFingerprint, startTime, endTime);
        
        return count >= rule.getThresholdValue().longValue();
    }

    /**
     * 检查时间限制规则
     * 
     * @param rule 风控规则
     * @param request 支付请求
     * @return 是否触发规则
     */
    private boolean checkTimeLimit(RiskRule rule, PaymentCreateRequest request) {
        if (!StringUtils.hasText(rule.getRuleConfig())) {
            return false;
        }
        
        try {
            // 解析时间限制配置（例如：禁止在22:00-06:00之间支付）
            String[] timeParts = rule.getRuleConfig().split("-");
            if (timeParts.length != 2) {
                return false;
            }
            
            int startHour = Integer.parseInt(timeParts[0]);
            int endHour = Integer.parseInt(timeParts[1]);
            int currentHour = LocalDateTime.now().getHour();
            
            if (startHour <= endHour) {
                return currentHour >= startHour && currentHour < endHour;
            } else {
                return currentHour >= startHour || currentHour < endHour;
            }
            
        } catch (Exception e) {
            logger.error("检查时间限制规则异常", e);
            return false;
        }
    }

    /**
     * 检查速度检查规则
     * 
     * @param rule 风控规则
     * @param request 支付请求
     * @return 是否触发规则
     */
    private boolean checkVelocity(RiskRule rule, PaymentCreateRequest request) {
        if (rule.getThresholdValue() == null || rule.getTimeWindowSeconds() == null) {
            return false;
        }
        
        LocalDateTime startTime = LocalDateTime.now().minusSeconds(rule.getTimeWindowSeconds());
        LocalDateTime endTime = LocalDateTime.now();
        
        BigDecimal totalAmount = riskRecordRepository.sumPaymentAmountByUserIdAndCreatedAtBetween(
                request.getUserId(), startTime, endTime);
        
        return totalAmount.compareTo(rule.getThresholdValue()) > 0;
    }

    /**
     * 保存风控记录
     * 
     * @param request 支付请求
     * @param paymentOrderId 支付订单ID
     * @param result 风控检查结果
     */
    private void saveRiskRecord(PaymentCreateRequest request, String paymentOrderId, RiskCheckResult result) {
        try {
            RiskRecord record = new RiskRecord();
            record.setRecordId(generateRecordId());
            record.setPaymentOrderId(paymentOrderId);
            record.setBusinessOrderId(request.getBusinessOrderId());
            record.setUserId(request.getUserId());
            record.setPaymentMethod(request.getPaymentMethod());
            record.setPaymentAmount(request.getAmount());
            record.setClientIp(request.getClientIp());
            record.setUserAgent(request.getUserAgent());
            record.setDeviceFingerprint(extractDeviceFingerprint(request));
            record.setTriggeredRules(String.join(",", result.getTriggeredRules()));
            record.setRiskScore(result.getRiskScore());
            record.setRiskLevel(result.getRiskLevel());
            record.setResult(result.getResult());
            record.setAction(result.getAction());
            record.setReason(result.getReason());
            record.setProcessingTimeMs(result.getProcessingTimeMs());
            
            // 如果需要人工审核，设置审核状态
            if (result.requiresManualReview()) {
                record.setReviewStatus(RiskRecord.ReviewStatus.PENDING);
            }
            
            // 保存详细信息
            Map<String, Object> details = new HashMap<>();
            details.put("triggeredRules", result.getTriggeredRules());
            details.put("riskScore", result.getRiskScore());
            details.put("processingTime", result.getProcessingTimeMs());
            record.setDetails(objectMapper.writeValueAsString(details));
            
            RiskRecord savedRecord = riskRecordRepository.save(record);
            result.setRecordId(savedRecord.getRecordId());
            
        } catch (JsonProcessingException e) {
            logger.error("序列化风控记录详情异常", e);
        } catch (Exception e) {
            logger.error("保存风控记录异常", e);
        }
    }

    /**
     * 验证风控规则
     * 
     * @param rule 风控规则
     */
    private void validateRiskRule(RiskRule rule) {
        if (rule == null) {
            throw PaymentException.invalidParameter("风控规则不能为空");
        }
        
        if (!StringUtils.hasText(rule.getRuleName())) {
            throw PaymentException.invalidParameter("规则名称不能为空");
        }
        
        if (rule.getRuleType() == null) {
            throw PaymentException.invalidParameter("规则类型不能为空");
        }
        
        if (rule.getRiskLevel() == null) {
            throw PaymentException.invalidParameter("风险等级不能为空");
        }
        
        if (rule.getAction() == null) {
            throw PaymentException.invalidParameter("处理动作不能为空");
        }
        
        if (rule.getEnabled() == null) {
            rule.setEnabled(true);
        }
        
        if (rule.getPriority() == null || rule.getPriority() < 0) {
            throw PaymentException.invalidParameter("优先级必须大于等于0");
        }
    }

    /**
     * 生成规则ID
     * 
     * @return 规则ID
     */
    private String generateRuleId() {
        return "RULE_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    /**
     * 生成记录ID
     * 
     * @return 记录ID
     */
    private String generateRecordId() {
        return "RISK_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    /**
     * 提取设备指纹
     * 
     * @param request 支付请求
     * @return 设备指纹
     */
    private String extractDeviceFingerprint(PaymentCreateRequest request) {
        // 简单的设备指纹生成逻辑，实际项目中可能需要更复杂的算法
        if (StringUtils.hasText(request.getDeviceInfo())) {
            return request.getDeviceInfo();
        }
        
        if (StringUtils.hasText(request.getUserAgent())) {
            return String.valueOf(request.getUserAgent().hashCode());
        }
        
        return null;
    }
}