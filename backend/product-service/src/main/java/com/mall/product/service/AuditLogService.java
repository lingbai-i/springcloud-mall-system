package com.mall.product.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审计日志服务接口
 * 提供完整的审计日志记录和查询功能
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
public interface AuditLogService {
    
    /**
     * 记录操作日志
     * 
     * @param action 操作类型
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @param description 操作描述
     * @param oldValue 变更前值
     * @param newValue 变更后值
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 日志记录ID
     */
    Long recordOperationLog(String action, String entityType, Long entityId, Long operatorId, 
                          String operatorName, String description, String oldValue, String newValue,
                          String ipAddress, String userAgent);
    
    /**
     * 记录业务日志
     * 
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @param operation 操作内容
     * @param result 操作结果
     * @param duration 执行时长（毫秒）
     * @param extendInfo 扩展信息
     * @return 日志记录ID
     */
    Long recordBusinessLog(String businessType, Long businessId, Long operatorId, String operatorName,
                         String operation, String result, Long duration, Map<String, Object> extendInfo);
    
    /**
     * 记录异常日志
     * 
     * @param action 操作类型
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @param operatorId 操作人ID
     * @param errorMessage 错误信息
     * @param stackTrace 堆栈信息
     * @param requestParams 请求参数
     * @return 日志记录ID
     */
    Long recordExceptionLog(String action, String entityType, Long entityId, Long operatorId,
                          String errorMessage, String stackTrace, String requestParams);
    
    /**
     * 记录安全日志
     * 
     * @param securityEvent 安全事件类型
     * @param userId 用户ID
     * @param userName 用户名
     * @param description 事件描述
     * @param riskLevel 风险等级：1-低，2-中，3-高，4-严重
     * @param ipAddress IP地址
     * @param location 地理位置
     * @return 日志记录ID
     */
    Long recordSecurityLog(String securityEvent, Long userId, String userName, String description,
                         Integer riskLevel, String ipAddress, String location);
    
    /**
     * 查询操作日志
     * 
     * @param entityType 实体类型（可选）
     * @param entityId 实体ID（可选）
     * @param operatorId 操作人ID（可选）
     * @param action 操作类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param current 当前页码
     * @param size 页面大小
     * @return 操作日志列表
     */
    Object getOperationLogs(String entityType, Long entityId, Long operatorId, String action,
                          LocalDateTime startTime, LocalDateTime endTime, Long current, Long size);
    
    /**
     * 查询业务日志
     * 
     * @param businessType 业务类型（可选）
     * @param businessId 业务ID（可选）
     * @param operatorId 操作人ID（可选）
     * @param result 操作结果（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param current 当前页码
     * @param size 页面大小
     * @return 业务日志列表
     */
    Object getBusinessLogs(String businessType, Long businessId, Long operatorId, String result,
                         LocalDateTime startTime, LocalDateTime endTime, Long current, Long size);
    
    /**
     * 查询异常日志
     * 
     * @param entityType 实体类型（可选）
     * @param operatorId 操作人ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param current 当前页码
     * @param size 页面大小
     * @return 异常日志列表
     */
    Object getExceptionLogs(String entityType, Long operatorId, LocalDateTime startTime, 
                          LocalDateTime endTime, Long current, Long size);
    
    /**
     * 查询安全日志
     * 
     * @param securityEvent 安全事件类型（可选）
     * @param userId 用户ID（可选）
     * @param riskLevel 风险等级（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param current 当前页码
     * @param size 页面大小
     * @return 安全日志列表
     */
    Object getSecurityLogs(String securityEvent, Long userId, Integer riskLevel,
                         LocalDateTime startTime, LocalDateTime endTime, Long current, Long size);
    
    /**
     * 获取日志统计信息
     * 
     * @param logType 日志类型：operation, business, exception, security
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param groupBy 分组字段：action, operator, date等
     * @return 统计信息
     */
    Object getLogStatistics(String logType, LocalDateTime startTime, LocalDateTime endTime, String groupBy);
    
    /**
     * 导出日志
     * 
     * @param logType 日志类型
     * @param filters 过滤条件
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param format 导出格式：excel, csv, json
     * @return 导出文件路径或内容
     */
    String exportLogs(String logType, Map<String, Object> filters, LocalDateTime startTime, 
                    LocalDateTime endTime, String format);
    
    /**
     * 清理过期日志
     * 
     * @param logType 日志类型
     * @param retentionDays 保留天数
     * @return 清理结果
     */
    LogCleanupResult cleanupExpiredLogs(String logType, Integer retentionDays);
    
    /**
     * 批量记录操作日志
     * 
     * @param logEntries 日志条目列表
     * @return 批量记录结果
     */
    BatchLogResult batchRecordLogs(List<LogEntry> logEntries);
    
    // ==================== 内部类定义 ====================
    
    /**
     * 日志条目
     */
    class LogEntry {
        private String logType; // operation, business, exception, security
        private String action;
        private String entityType;
        private Long entityId;
        private Long operatorId;
        private String operatorName;
        private String description;
        private String oldValue;
        private String newValue;
        private String ipAddress;
        private String userAgent;
        private Map<String, Object> extendInfo;
        
        // Getters and Setters
        public String getLogType() { return logType; }
        public void setLogType(String logType) { this.logType = logType; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getEntityType() { return entityType; }
        public void setEntityType(String entityType) { this.entityType = entityType; }
        public Long getEntityId() { return entityId; }
        public void setEntityId(Long entityId) { this.entityId = entityId; }
        public Long getOperatorId() { return operatorId; }
        public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
        public String getOperatorName() { return operatorName; }
        public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getOldValue() { return oldValue; }
        public void setOldValue(String oldValue) { this.oldValue = oldValue; }
        public String getNewValue() { return newValue; }
        public void setNewValue(String newValue) { this.newValue = newValue; }
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
        public Map<String, Object> getExtendInfo() { return extendInfo; }
        public void setExtendInfo(Map<String, Object> extendInfo) { this.extendInfo = extendInfo; }
    }
    
    /**
     * 日志清理结果
     */
    class LogCleanupResult {
        private boolean success;
        private String message;
        private int cleanedCount;
        private LocalDateTime cleanupTime;
        
        public LogCleanupResult(boolean success, String message, int cleanedCount) {
            this.success = success;
            this.message = message;
            this.cleanedCount = cleanedCount;
            this.cleanupTime = LocalDateTime.now();
        }
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public int getCleanedCount() { return cleanedCount; }
        public void setCleanedCount(int cleanedCount) { this.cleanedCount = cleanedCount; }
        public LocalDateTime getCleanupTime() { return cleanupTime; }
        public void setCleanupTime(LocalDateTime cleanupTime) { this.cleanupTime = cleanupTime; }
    }
    
    /**
     * 批量日志记录结果
     */
    class BatchLogResult {
        private boolean success;
        private String message;
        private int totalCount;
        private int successCount;
        private int failCount;
        private List<String> errors;
        
        public BatchLogResult(boolean success, String message, int totalCount, int successCount, int failCount) {
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
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
    }
}