package com.mall.product.service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 价格审批服务接口
 * 提供价格审批流程管理功能
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
public interface PriceApprovalService {
    
    /**
     * 提交价格审批申请
     * 
     * @param priceHistoryId 价格历史记录ID
     * @param applicantId 申请人ID
     * @param urgencyLevel 紧急程度：1-普通，2-紧急，3-特急
     * @param businessReason 业务原因
     * @return 审批申请结果
     */
    ApprovalApplicationResult submitApprovalApplication(Long priceHistoryId, Long applicantId, 
                                                      Integer urgencyLevel, String businessReason);
    
    /**
     * 审批价格变更
     * 
     * @param approvalId 审批ID
     * @param approverId 审批人ID
     * @param approved 是否通过
     * @param approvalComment 审批意见
     * @return 审批结果
     */
    ApprovalResult approvePrice(Long approvalId, Long approverId, Boolean approved, String approvalComment);
    
    /**
     * 批量审批价格变更
     * 
     * @param approvalIds 审批ID列表
     * @param approverId 审批人ID
     * @param approved 是否通过
     * @param approvalComment 审批意见
     * @return 批量审批结果
     */
    BatchApprovalResult batchApprovePrice(List<Long> approvalIds, Long approverId, 
                                        Boolean approved, String approvalComment);
    
    /**
     * 获取待审批列表
     * 
     * @param approverId 审批人ID（可选，为空则获取所有）
     * @param urgencyLevel 紧急程度过滤（可选）
     * @param current 当前页码
     * @param size 页面大小
     * @return 待审批列表
     */
    Object getPendingApprovals(Long approverId, Integer urgencyLevel, Long current, Long size);
    
    /**
     * 获取审批历史
     * 
     * @param priceHistoryId 价格历史记录ID（可选）
     * @param approverId 审批人ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param current 当前页码
     * @param size 页面大小
     * @return 审批历史列表
     */
    Object getApprovalHistory(Long priceHistoryId, Long approverId, LocalDateTime startTime, 
                            LocalDateTime endTime, Long current, Long size);
    
    /**
     * 撤回审批申请
     * 
     * @param approvalId 审批ID
     * @param applicantId 申请人ID
     * @param reason 撤回原因
     * @return 撤回结果
     */
    ApprovalResult withdrawApproval(Long approvalId, Long applicantId, String reason);
    
    /**
     * 转交审批
     * 
     * @param approvalId 审批ID
     * @param fromApproverId 原审批人ID
     * @param toApproverId 新审批人ID
     * @param reason 转交原因
     * @return 转交结果
     */
    ApprovalResult transferApproval(Long approvalId, Long fromApproverId, Long toApproverId, String reason);
    
    /**
     * 获取审批统计信息
     * 
     * @param approverId 审批人ID（可选）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 审批统计信息
     */
    ApprovalStatistics getApprovalStatistics(Long approverId, LocalDateTime startTime, LocalDateTime endTime);
    
    // ==================== 内部类定义 ====================
    
    /**
     * 审批申请结果
     */
    class ApprovalApplicationResult {
        private boolean success;
        private String message;
        private Long approvalId;
        private String status;
        
        public ApprovalApplicationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public ApprovalApplicationResult(boolean success, String message, Long approvalId, String status) {
            this.success = success;
            this.message = message;
            this.approvalId = approvalId;
            this.status = status;
        }
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Long getApprovalId() { return approvalId; }
        public void setApprovalId(Long approvalId) { this.approvalId = approvalId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    /**
     * 审批结果
     */
    class ApprovalResult {
        private boolean success;
        private String message;
        private Long approvalId;
        private String status;
        private LocalDateTime approvalTime;
        
        public ApprovalResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public ApprovalResult(boolean success, String message, Long approvalId, String status) {
            this.success = success;
            this.message = message;
            this.approvalId = approvalId;
            this.status = status;
            this.approvalTime = LocalDateTime.now();
        }
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Long getApprovalId() { return approvalId; }
        public void setApprovalId(Long approvalId) { this.approvalId = approvalId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getApprovalTime() { return approvalTime; }
        public void setApprovalTime(LocalDateTime approvalTime) { this.approvalTime = approvalTime; }
    }
    
    /**
     * 批量审批结果
     */
    class BatchApprovalResult {
        private boolean success;
        private String message;
        private int totalCount;
        private int successCount;
        private int failCount;
        private List<ApprovalResult> results;
        
        public BatchApprovalResult(boolean success, String message, int totalCount, int successCount, int failCount) {
            this.success = success;
            this.message = message;
            this.totalCount = totalCount;
            this.successCount = successCount;
            this.failCount = failCount;
        }
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        public int getFailCount() { return failCount; }
        public void setFailCount(int failCount) { this.failCount = failCount; }
        public List<ApprovalResult> getResults() { return results; }
        public void setResults(List<ApprovalResult> results) { this.results = results; }
    }
    
    /**
     * 审批统计信息
     */
    class ApprovalStatistics {
        private int totalApprovals;
        private int approvedCount;
        private int rejectedCount;
        private int pendingCount;
        private int withdrawnCount;
        private double approvalRate;
        private double avgProcessingTime; // 平均处理时间（小时）
        
        // Getters and Setters
        public int getTotalApprovals() { return totalApprovals; }
        public void setTotalApprovals(int totalApprovals) { this.totalApprovals = totalApprovals; }
        public int getApprovedCount() { return approvedCount; }
        public void setApprovedCount(int approvedCount) { this.approvedCount = approvedCount; }
        public int getRejectedCount() { return rejectedCount; }
        public void setRejectedCount(int rejectedCount) { this.rejectedCount = rejectedCount; }
        public int getPendingCount() { return pendingCount; }
        public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }
        public int getWithdrawnCount() { return withdrawnCount; }
        public void setWithdrawnCount(int withdrawnCount) { this.withdrawnCount = withdrawnCount; }
        public double getApprovalRate() { return approvalRate; }
        public void setApprovalRate(double approvalRate) { this.approvalRate = approvalRate; }
        public double getAvgProcessingTime() { return avgProcessingTime; }
        public void setAvgProcessingTime(double avgProcessingTime) { this.avgProcessingTime = avgProcessingTime; }
    }
}