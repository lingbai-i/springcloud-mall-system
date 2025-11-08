package com.mall.product.service.impl;

import com.mall.product.service.PriceApprovalService;
import com.mall.product.domain.entity.PriceApproval;
import com.mall.product.domain.entity.PriceHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 价格审批服务实现类
 * 提供完整的价格审批流程管理
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Service
public class PriceApprovalServiceImpl implements PriceApprovalService {
    
    private static final Logger logger = LoggerFactory.getLogger(PriceApprovalServiceImpl.class);
    
    // 模拟数据存储
    private static final Map<Long, PriceApproval> APPROVAL_CACHE = new ConcurrentHashMap<>();
    private static final Map<Long, PriceHistory> PRICE_HISTORY_CACHE = new ConcurrentHashMap<>();
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);
    
    // 审批超时时间配置（小时）
    private static final Map<Integer, Integer> URGENCY_TIMEOUT_MAP = Map.of(
        1, 72,  // 普通：72小时
        2, 24,  // 紧急：24小时
        3, 4    // 特急：4小时
    );
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApprovalApplicationResult submitApprovalApplication(Long priceHistoryId, Long applicantId, 
                                                             Integer urgencyLevel, String businessReason) {
        logger.info("提交价格审批申请 - 价格历史ID: {}, 申请人: {}, 紧急程度: {}", 
            priceHistoryId, applicantId, urgencyLevel);
        
        try {
            // 参数验证
            if (priceHistoryId == null) {
                return new ApprovalApplicationResult(false, "价格历史记录ID不能为空");
            }
            if (applicantId == null) {
                return new ApprovalApplicationResult(false, "申请人ID不能为空");
            }
            if (urgencyLevel == null || urgencyLevel < 1 || urgencyLevel > 3) {
                return new ApprovalApplicationResult(false, "紧急程度必须在1-3之间");
            }
            
            // 检查价格历史记录是否存在
            PriceHistory priceHistory = PRICE_HISTORY_CACHE.get(priceHistoryId);
            if (priceHistory == null) {
                return new ApprovalApplicationResult(false, "价格历史记录不存在");
            }
            
            // 检查是否已经有审批申请
            boolean hasExistingApproval = APPROVAL_CACHE.values().stream()
                .anyMatch(approval -> approval.getPriceHistoryId().equals(priceHistoryId) 
                    && "PENDING".equals(approval.getStatus()));
            
            if (hasExistingApproval) {
                return new ApprovalApplicationResult(false, "该价格变更已有待审批申请");
            }
            
            // 创建审批记录
            Long approvalId = ID_GENERATOR.getAndIncrement();
            PriceApproval approval = new PriceApproval();
            approval.setId(approvalId);
            approval.setPriceHistoryId(priceHistoryId);
            approval.setApplicantId(applicantId);
            approval.setApplicantName("申请人" + applicantId); // 实际应从用户服务获取
            approval.setStatus("PENDING");
            approval.setUrgencyLevel(urgencyLevel);
            approval.setBusinessReason(businessReason);
            approval.setApplicationTime(LocalDateTime.now());
            
            // 设置预期完成时间
            int timeoutHours = URGENCY_TIMEOUT_MAP.get(urgencyLevel);
            approval.setExpectedTime(LocalDateTime.now().plusHours(timeoutHours));
            
            // 冗余字段设置
            approval.setProductId(priceHistory.getProductId());
            approval.setSkuId(priceHistory.getSkuId());
            approval.setProductName(priceHistory.getProductName());
            approval.setSkuName(priceHistory.getSkuName());
            approval.setOldPrice(priceHistory.getOldPrice());
            approval.setNewPrice(priceHistory.getNewPrice());
            
            // 计算价格变更幅度
            if (priceHistory.getOldPrice() != null && priceHistory.getOldPrice() > 0) {
                double changeRate = Math.abs(priceHistory.getNewPrice() - priceHistory.getOldPrice()) 
                    / priceHistory.getOldPrice() * 100;
                approval.setChangeRate(changeRate);
            }
            
            approval.setApprovalNode("FIRST_LEVEL"); // 一级审批
            approval.setCreateTime(LocalDateTime.now());
            approval.setUpdateTime(LocalDateTime.now());
            
            APPROVAL_CACHE.put(approvalId, approval);
            
            logger.info("价格审批申请提交成功 - 审批ID: {}, 预期完成时间: {}", 
                approvalId, approval.getExpectedTime());
            
            return new ApprovalApplicationResult(true, "审批申请提交成功", approvalId, "PENDING");
            
        } catch (Exception e) {
            logger.error("提交价格审批申请异常", e);
            return new ApprovalApplicationResult(false, "提交审批申请异常: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApprovalResult approvePrice(Long approvalId, Long approverId, Boolean approved, String approvalComment) {
        logger.info("审批价格变更 - 审批ID: {}, 审批人: {}, 审批结果: {}", approvalId, approverId, approved);
        
        try {
            // 参数验证
            if (approvalId == null) {
                return new ApprovalResult(false, "审批ID不能为空");
            }
            if (approverId == null) {
                return new ApprovalResult(false, "审批人ID不能为空");
            }
            if (approved == null) {
                return new ApprovalResult(false, "审批结果不能为空");
            }
            
            PriceApproval approval = APPROVAL_CACHE.get(approvalId);
            if (approval == null) {
                return new ApprovalResult(false, "审批记录不存在");
            }
            
            if (!"PENDING".equals(approval.getStatus())) {
                return new ApprovalResult(false, "该审批已处理，当前状态: " + approval.getStatus());
            }
            
            // 更新审批记录
            approval.setApproverId(approverId);
            approval.setApproverName("审批人" + approverId); // 实际应从用户服务获取
            approval.setStatus(approved ? "APPROVED" : "REJECTED");
            approval.setApprovalComment(approvalComment);
            approval.setApprovalTime(LocalDateTime.now());
            approval.setUpdateTime(LocalDateTime.now());
            
            // 计算处理时长
            if (approval.getApplicationTime() != null) {
                long duration = ChronoUnit.MINUTES.between(approval.getApplicationTime(), LocalDateTime.now());
                approval.setProcessingDuration(duration);
                
                // 检查是否超时
                if (approval.getExpectedTime() != null && LocalDateTime.now().isAfter(approval.getExpectedTime())) {
                    approval.setIsOverdue(true);
                }
            }
            
            // 如果审批通过，更新价格历史记录状态
            if (approved) {
                PriceHistory priceHistory = PRICE_HISTORY_CACHE.get(approval.getPriceHistoryId());
                if (priceHistory != null) {
                    priceHistory.setAuditStatus("APPROVED");
                    priceHistory.setAuditorId(approverId);
                    priceHistory.setAuditTime(LocalDateTime.now());
                    priceHistory.setAuditReason(approvalComment);
                }
                
                logger.info("价格审批通过 - 商品ID: {}, SKU ID: {}, 新价格: {}", 
                    approval.getProductId(), approval.getSkuId(), approval.getNewPrice());
            } else {
                PriceHistory priceHistory = PRICE_HISTORY_CACHE.get(approval.getPriceHistoryId());
                if (priceHistory != null) {
                    priceHistory.setAuditStatus("REJECTED");
                    priceHistory.setAuditorId(approverId);
                    priceHistory.setAuditTime(LocalDateTime.now());
                    priceHistory.setAuditReason(approvalComment);
                }
                
                logger.info("价格审批拒绝 - 商品ID: {}, SKU ID: {}, 拒绝原因: {}", 
                    approval.getProductId(), approval.getSkuId(), approvalComment);
            }
            
            return new ApprovalResult(true, "审批完成", approvalId, approval.getStatus());
            
        } catch (Exception e) {
            logger.error("审批价格变更异常 - 审批ID: {}", approvalId, e);
            return new ApprovalResult(false, "审批异常: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchApprovalResult batchApprovePrice(List<Long> approvalIds, Long approverId, 
                                               Boolean approved, String approvalComment) {
        logger.info("批量审批价格变更 - 审批数量: {}, 审批人: {}, 审批结果: {}", 
            approvalIds != null ? approvalIds.size() : 0, approverId, approved);
        
        try {
            if (approvalIds == null || approvalIds.isEmpty()) {
                return new BatchApprovalResult(false, "审批ID列表不能为空", 0, 0, 0);
            }
            
            List<ApprovalResult> results = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;
            
            for (Long approvalId : approvalIds) {
                try {
                    ApprovalResult result = approvePrice(approvalId, approverId, approved, approvalComment);
                    results.add(result);
                    
                    if (result.isSuccess()) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    logger.error("批量审批单个记录失败 - 审批ID: {}", approvalId, e);
                    results.add(new ApprovalResult(false, "审批异常: " + e.getMessage()));
                    failCount++;
                }
            }
            
            BatchApprovalResult batchResult = new BatchApprovalResult(
                failCount == 0,
                String.format("批量审批完成，成功: %d, 失败: %d", successCount, failCount),
                approvalIds.size(),
                successCount,
                failCount
            );
            batchResult.setResults(results);
            
            logger.info("批量价格审批完成 - 总数: {}, 成功: {}, 失败: {}", 
                approvalIds.size(), successCount, failCount);
            
            return batchResult;
            
        } catch (Exception e) {
            logger.error("批量审批价格变更异常", e);
            return new BatchApprovalResult(false, "批量审批异常: " + e.getMessage(), 0, 0, 0);
        }
    }
    
    @Override
    public Object getPendingApprovals(Long approverId, Integer urgencyLevel, Long current, Long size) {
        logger.info("获取待审批列表 - 审批人: {}, 紧急程度: {}, 页码: {}, 大小: {}", 
            approverId, urgencyLevel, current, size);
        
        try {
            List<PriceApproval> allApprovals = APPROVAL_CACHE.values().stream()
                .filter(approval -> {
                    if (!"PENDING".equals(approval.getStatus())) {
                        return false;
                    }
                    
                    // 审批人过滤
                    if (approverId != null && approval.getApproverId() != null && 
                        !approval.getApproverId().equals(approverId)) {
                        return false;
                    }
                    
                    // 紧急程度过滤
                    if (urgencyLevel != null && !urgencyLevel.equals(approval.getUrgencyLevel())) {
                        return false;
                    }
                    
                    return true;
                })
                .sorted((a1, a2) -> {
                    // 按紧急程度和申请时间排序
                    int urgencyCompare = Integer.compare(a2.getUrgencyLevel(), a1.getUrgencyLevel());
                    if (urgencyCompare != 0) {
                        return urgencyCompare;
                    }
                    return a1.getApplicationTime().compareTo(a2.getApplicationTime());
                })
                .collect(Collectors.toList());
            
            // 分页处理
            int start = (int) ((current - 1) * size);
            int end = Math.min(start + size.intValue(), allApprovals.size());
            List<PriceApproval> pageData = allApprovals.subList(start, end);
            
            // 检查超时状态
            pageData.forEach(approval -> {
                if (approval.getExpectedTime() != null && LocalDateTime.now().isAfter(approval.getExpectedTime())) {
                    approval.setIsOverdue(true);
                }
            });
            
            Map<String, Object> result = new HashMap<>();
            result.put("records", pageData);
            result.put("total", allApprovals.size());
            result.put("current", current);
            result.put("size", size);
            result.put("pages", (allApprovals.size() + size - 1) / size);
            
            return result;
            
        } catch (Exception e) {
            logger.error("获取待审批列表异常", e);
            return new HashMap<>();
        }
    }
    
    @Override
    public Object getApprovalHistory(Long priceHistoryId, Long approverId, LocalDateTime startTime, 
                                   LocalDateTime endTime, Long current, Long size) {
        logger.info("获取审批历史 - 价格历史ID: {}, 审批人: {}, 开始时间: {}, 结束时间: {}", 
            priceHistoryId, approverId, startTime, endTime);
        
        try {
            List<PriceApproval> allApprovals = APPROVAL_CACHE.values().stream()
                .filter(approval -> {
                    // 价格历史ID过滤
                    if (priceHistoryId != null && !priceHistoryId.equals(approval.getPriceHistoryId())) {
                        return false;
                    }
                    
                    // 审批人过滤
                    if (approverId != null && !approverId.equals(approval.getApproverId())) {
                        return false;
                    }
                    
                    // 时间范围过滤
                    if (startTime != null && approval.getApplicationTime() != null && 
                        approval.getApplicationTime().isBefore(startTime)) {
                        return false;
                    }
                    
                    if (endTime != null && approval.getApplicationTime() != null && 
                        approval.getApplicationTime().isAfter(endTime)) {
                        return false;
                    }
                    
                    return true;
                })
                .sorted(Comparator.comparing(PriceApproval::getApplicationTime).reversed())
                .collect(Collectors.toList());
            
            // 分页处理
            int start = (int) ((current - 1) * size);
            int end = Math.min(start + size.intValue(), allApprovals.size());
            List<PriceApproval> pageData = allApprovals.subList(start, end);
            
            Map<String, Object> result = new HashMap<>();
            result.put("records", pageData);
            result.put("total", allApprovals.size());
            result.put("current", current);
            result.put("size", size);
            result.put("pages", (allApprovals.size() + size - 1) / size);
            
            return result;
            
        } catch (Exception e) {
            logger.error("获取审批历史异常", e);
            return new HashMap<>();
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApprovalResult withdrawApproval(Long approvalId, Long applicantId, String reason) {
        logger.info("撤回审批申请 - 审批ID: {}, 申请人: {}, 原因: {}", approvalId, applicantId, reason);
        
        try {
            if (approvalId == null) {
                return new ApprovalResult(false, "审批ID不能为空");
            }
            if (applicantId == null) {
                return new ApprovalResult(false, "申请人ID不能为空");
            }
            
            PriceApproval approval = APPROVAL_CACHE.get(approvalId);
            if (approval == null) {
                return new ApprovalResult(false, "审批记录不存在");
            }
            
            if (!approval.getApplicantId().equals(applicantId)) {
                return new ApprovalResult(false, "只能撤回自己提交的审批申请");
            }
            
            if (!"PENDING".equals(approval.getStatus())) {
                return new ApprovalResult(false, "只能撤回待审批状态的申请，当前状态: " + approval.getStatus());
            }
            
            // 更新审批状态
            approval.setStatus("WITHDRAWN");
            approval.setWithdrawReason(reason);
            approval.setUpdateTime(LocalDateTime.now());
            
            // 更新价格历史记录状态
            PriceHistory priceHistory = PRICE_HISTORY_CACHE.get(approval.getPriceHistoryId());
            if (priceHistory != null) {
                priceHistory.setAuditStatus("WITHDRAWN");
                priceHistory.setAuditReason("申请人撤回: " + reason);
                priceHistory.setAuditTime(LocalDateTime.now());
            }
            
            logger.info("审批申请撤回成功 - 审批ID: {}", approvalId);
            return new ApprovalResult(true, "撤回成功", approvalId, "WITHDRAWN");
            
        } catch (Exception e) {
            logger.error("撤回审批申请异常 - 审批ID: {}", approvalId, e);
            return new ApprovalResult(false, "撤回异常: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApprovalResult transferApproval(Long approvalId, Long fromApproverId, Long toApproverId, String reason) {
        logger.info("转交审批 - 审批ID: {}, 原审批人: {}, 新审批人: {}", approvalId, fromApproverId, toApproverId);
        
        try {
            if (approvalId == null) {
                return new ApprovalResult(false, "审批ID不能为空");
            }
            if (fromApproverId == null) {
                return new ApprovalResult(false, "原审批人ID不能为空");
            }
            if (toApproverId == null) {
                return new ApprovalResult(false, "新审批人ID不能为空");
            }
            if (fromApproverId.equals(toApproverId)) {
                return new ApprovalResult(false, "不能转交给自己");
            }
            
            PriceApproval approval = APPROVAL_CACHE.get(approvalId);
            if (approval == null) {
                return new ApprovalResult(false, "审批记录不存在");
            }
            
            if (!"PENDING".equals(approval.getStatus())) {
                return new ApprovalResult(false, "只能转交待审批状态的申请，当前状态: " + approval.getStatus());
            }
            
            // 更新审批人信息
            approval.setPreviousApproverId(fromApproverId);
            approval.setApproverId(toApproverId);
            approval.setApproverName("审批人" + toApproverId); // 实际应从用户服务获取
            approval.setTransferReason(reason);
            approval.setUpdateTime(LocalDateTime.now());
            
            logger.info("审批转交成功 - 审批ID: {}, 新审批人: {}", approvalId, toApproverId);
            return new ApprovalResult(true, "转交成功", approvalId, "PENDING");
            
        } catch (Exception e) {
            logger.error("转交审批异常 - 审批ID: {}", approvalId, e);
            return new ApprovalResult(false, "转交异常: " + e.getMessage());
        }
    }
    
    @Override
    public ApprovalStatistics getApprovalStatistics(Long approverId, LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("获取审批统计 - 审批人: {}, 开始时间: {}, 结束时间: {}", approverId, startTime, endTime);
        
        try {
            List<PriceApproval> filteredApprovals = APPROVAL_CACHE.values().stream()
                .filter(approval -> {
                    // 审批人过滤
                    if (approverId != null && !approverId.equals(approval.getApproverId())) {
                        return false;
                    }
                    
                    // 时间范围过滤
                    if (startTime != null && approval.getApplicationTime() != null && 
                        approval.getApplicationTime().isBefore(startTime)) {
                        return false;
                    }
                    
                    if (endTime != null && approval.getApplicationTime() != null && 
                        approval.getApplicationTime().isAfter(endTime)) {
                        return false;
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
            
            ApprovalStatistics statistics = new ApprovalStatistics();
            statistics.setTotalApprovals(filteredApprovals.size());
            
            // 统计各状态数量
            Map<String, Long> statusCount = filteredApprovals.stream()
                .collect(Collectors.groupingBy(PriceApproval::getStatus, Collectors.counting()));
            
            statistics.setApprovedCount(statusCount.getOrDefault("APPROVED", 0L).intValue());
            statistics.setRejectedCount(statusCount.getOrDefault("REJECTED", 0L).intValue());
            statistics.setPendingCount(statusCount.getOrDefault("PENDING", 0L).intValue());
            statistics.setWithdrawnCount(statusCount.getOrDefault("WITHDRAWN", 0L).intValue());
            
            // 计算审批通过率
            int processedCount = statistics.getApprovedCount() + statistics.getRejectedCount();
            if (processedCount > 0) {
                statistics.setApprovalRate((double) statistics.getApprovedCount() / processedCount * 100);
            }
            
            // 计算平均处理时间
            List<Long> processingTimes = filteredApprovals.stream()
                .filter(approval -> approval.getProcessingDuration() != null)
                .map(PriceApproval::getProcessingDuration)
                .collect(Collectors.toList());
            
            if (!processingTimes.isEmpty()) {
                double avgMinutes = processingTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
                statistics.setAvgProcessingTime(avgMinutes / 60.0); // 转换为小时
            }
            
            return statistics;
            
        } catch (Exception e) {
            logger.error("获取审批统计异常", e);
            return new ApprovalStatistics();
        }
    }
    
    /**
     * 初始化模拟数据
     */
    public void initMockData() {
        // 初始化价格历史数据
        for (long i = 1; i <= 10; i++) {
            PriceHistory priceHistory = new PriceHistory();
            priceHistory.setId(i);
            priceHistory.setProductId(i);
            priceHistory.setSkuId(i);
            priceHistory.setOldPrice(100.0 + i * 10);
            priceHistory.setNewPrice(120.0 + i * 10);
            priceHistory.setPriceType("PRODUCT");
            priceHistory.setReason("市场调价");
            priceHistory.setAuditStatus("PENDING");
            priceHistory.setOperatorId(1L);
            priceHistory.setOperatorName("操作员1");
            priceHistory.setProductName("商品" + i);
            priceHistory.setSkuName("SKU" + i);
            priceHistory.setPriceVersion(1L);
            priceHistory.setCreateTime(LocalDateTime.now().minusDays(i));
            
            PRICE_HISTORY_CACHE.put(i, priceHistory);
        }
        
        logger.info("价格审批服务模拟数据初始化完成");
    }
}