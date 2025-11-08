package com.mall.product.service.impl;

import com.mall.product.service.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 审计日志服务实现类
 * 提供完整的审计日志记录和查询功能
 * 支持操作日志、业务日志、异常日志、安全日志的记录和管理
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Service
public class AuditLogServiceImpl implements AuditLogService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditLogServiceImpl.class);
    
    // 模拟数据存储
    private final Map<Long, OperationLog> operationLogCache = new ConcurrentHashMap<>();
    private final Map<Long, BusinessLog> businessLogCache = new ConcurrentHashMap<>();
    private final Map<Long, ExceptionLog> exceptionLogCache = new ConcurrentHashMap<>();
    private final Map<Long, SecurityLog> securityLogCache = new ConcurrentHashMap<>();
    
    // ID生成器
    private final AtomicLong operationLogIdGenerator = new AtomicLong(1);
    private final AtomicLong businessLogIdGenerator = new AtomicLong(1);
    private final AtomicLong exceptionLogIdGenerator = new AtomicLong(1);
    private final AtomicLong securityLogIdGenerator = new AtomicLong(1);
    
    // 日志保留天数配置
    private static final Map<String, Integer> LOG_RETENTION_DAYS = new HashMap<>();
    static {
        LOG_RETENTION_DAYS.put("operation", 90);  // 操作日志保留90天
        LOG_RETENTION_DAYS.put("business", 180);  // 业务日志保留180天
        LOG_RETENTION_DAYS.put("exception", 365); // 异常日志保留365天
        LOG_RETENTION_DAYS.put("security", 730);  // 安全日志保留730天
    }
    
    @Override
    public Long recordOperationLog(String action, String entityType, Long entityId, Long operatorId, 
                                 String operatorName, String description, String oldValue, String newValue,
                                 String ipAddress, String userAgent) {
        try {
            // 参数验证
            if (action == null || action.trim().isEmpty()) {
                logger.warn("记录操作日志失败：操作类型不能为空");
                return null;
            }
            
            if (entityType == null || entityType.trim().isEmpty()) {
                logger.warn("记录操作日志失败：实体类型不能为空");
                return null;
            }
            
            if (operatorId == null) {
                logger.warn("记录操作日志失败：操作人ID不能为空");
                return null;
            }
            
            Long logId = operationLogIdGenerator.getAndIncrement();
            OperationLog operationLog = new OperationLog();
            operationLog.setId(logId);
            operationLog.setAction(action.trim());
            operationLog.setEntityType(entityType.trim());
            operationLog.setEntityId(entityId);
            operationLog.setOperatorId(operatorId);
            operationLog.setOperatorName(operatorName != null ? operatorName.trim() : "");
            operationLog.setDescription(description != null ? description.trim() : "");
            operationLog.setOldValue(oldValue);
            operationLog.setNewValue(newValue);
            operationLog.setIpAddress(ipAddress != null ? ipAddress.trim() : "");
            operationLog.setUserAgent(userAgent != null ? userAgent.trim() : "");
            operationLog.setCreateTime(LocalDateTime.now());
            
            operationLogCache.put(logId, operationLog);
            
            logger.debug("记录操作日志成功 - ID: {}, 操作: {}, 实体: {}, 操作人: {}", 
                logId, action, entityType, operatorName);
            
            return logId;
            
        } catch (Exception e) {
            logger.error("记录操作日志异常 - 操作: {}, 实体: {}", action, entityType, e);
            return null;
        }
    }
    
    @Override
    public Long recordBusinessLog(String businessType, Long businessId, Long operatorId, String operatorName,
                                String operation, String result, Long duration, Map<String, Object> extendInfo) {
        try {
            // 参数验证
            if (businessType == null || businessType.trim().isEmpty()) {
                logger.warn("记录业务日志失败：业务类型不能为空");
                return null;
            }
            
            if (operatorId == null) {
                logger.warn("记录业务日志失败：操作人ID不能为空");
                return null;
            }
            
            Long logId = businessLogIdGenerator.getAndIncrement();
            BusinessLog businessLog = new BusinessLog();
            businessLog.setId(logId);
            businessLog.setBusinessType(businessType.trim());
            businessLog.setBusinessId(businessId);
            businessLog.setOperatorId(operatorId);
            businessLog.setOperatorName(operatorName != null ? operatorName.trim() : "");
            businessLog.setOperation(operation != null ? operation.trim() : "");
            businessLog.setResult(result != null ? result.trim() : "");
            businessLog.setDuration(duration != null ? duration : 0L);
            businessLog.setExtendInfo(extendInfo != null ? new HashMap<>(extendInfo) : new HashMap<>());
            businessLog.setCreateTime(LocalDateTime.now());
            
            businessLogCache.put(logId, businessLog);
            
            logger.debug("记录业务日志成功 - ID: {}, 业务类型: {}, 操作: {}, 结果: {}, 耗时: {}ms", 
                logId, businessType, operation, result, duration);
            
            return logId;
            
        } catch (Exception e) {
            logger.error("记录业务日志异常 - 业务类型: {}, 操作: {}", businessType, operation, e);
            return null;
        }
    }
    
    @Override
    public Long recordExceptionLog(String action, String entityType, Long entityId, Long operatorId,
                                 String errorMessage, String stackTrace, String requestParams) {
        try {
            // 参数验证
            if (action == null || action.trim().isEmpty()) {
                logger.warn("记录异常日志失败：操作类型不能为空");
                return null;
            }
            
            Long logId = exceptionLogIdGenerator.getAndIncrement();
            ExceptionLog exceptionLog = new ExceptionLog();
            exceptionLog.setId(logId);
            exceptionLog.setAction(action.trim());
            exceptionLog.setEntityType(entityType != null ? entityType.trim() : "");
            exceptionLog.setEntityId(entityId);
            exceptionLog.setOperatorId(operatorId);
            exceptionLog.setErrorMessage(errorMessage != null ? errorMessage.trim() : "");
            exceptionLog.setStackTrace(stackTrace != null ? stackTrace : "");
            exceptionLog.setRequestParams(requestParams != null ? requestParams : "");
            exceptionLog.setCreateTime(LocalDateTime.now());
            
            exceptionLogCache.put(logId, exceptionLog);
            
            logger.debug("记录异常日志成功 - ID: {}, 操作: {}, 错误: {}", logId, action, errorMessage);
            
            return logId;
            
        } catch (Exception e) {
            logger.error("记录异常日志异常 - 操作: {}", action, e);
            return null;
        }
    }
    
    @Override
    public Long recordSecurityLog(String securityEvent, Long userId, String userName, String description,
                                Integer riskLevel, String ipAddress, String location) {
        try {
            // 参数验证
            if (securityEvent == null || securityEvent.trim().isEmpty()) {
                logger.warn("记录安全日志失败：安全事件类型不能为空");
                return null;
            }
            
            if (riskLevel == null || riskLevel < 1 || riskLevel > 4) {
                logger.warn("记录安全日志失败：风险等级必须在1-4之间");
                return null;
            }
            
            Long logId = securityLogIdGenerator.getAndIncrement();
            SecurityLog securityLog = new SecurityLog();
            securityLog.setId(logId);
            securityLog.setSecurityEvent(securityEvent.trim());
            securityLog.setUserId(userId);
            securityLog.setUserName(userName != null ? userName.trim() : "");
            securityLog.setDescription(description != null ? description.trim() : "");
            securityLog.setRiskLevel(riskLevel);
            securityLog.setIpAddress(ipAddress != null ? ipAddress.trim() : "");
            securityLog.setLocation(location != null ? location.trim() : "");
            securityLog.setCreateTime(LocalDateTime.now());
            
            securityLogCache.put(logId, securityLog);
            
            logger.debug("记录安全日志成功 - ID: {}, 事件: {}, 风险等级: {}, 用户: {}", 
                logId, securityEvent, riskLevel, userName);
            
            // 高风险事件立即告警
            if (riskLevel >= 3) {
                logger.warn("检测到高风险安全事件 - 事件: {}, 用户: {}, IP: {}, 描述: {}", 
                    securityEvent, userName, ipAddress, description);
            }
            
            return logId;
            
        } catch (Exception e) {
            logger.error("记录安全日志异常 - 事件: {}", securityEvent, e);
            return null;
        }
    }
    
    @Override
    public Object getOperationLogs(String entityType, Long entityId, Long operatorId, String action,
                                 LocalDateTime startTime, LocalDateTime endTime, Long current, Long size) {
        try {
            logger.debug("查询操作日志 - 实体类型: {}, 实体ID: {}, 操作人: {}, 操作: {}", 
                entityType, entityId, operatorId, action);
            
            List<OperationLog> filteredLogs = operationLogCache.values().stream()
                .filter(log -> {
                    boolean match = true;
                    if (entityType != null && !entityType.trim().isEmpty()) {
                        match = entityType.equals(log.getEntityType());
                    }
                    if (entityId != null && match) {
                        match = entityId.equals(log.getEntityId());
                    }
                    if (operatorId != null && match) {
                        match = operatorId.equals(log.getOperatorId());
                    }
                    if (action != null && !action.trim().isEmpty() && match) {
                        match = action.equals(log.getAction());
                    }
                    if (startTime != null && match) {
                        match = log.getCreateTime().isAfter(startTime) || log.getCreateTime().isEqual(startTime);
                    }
                    if (endTime != null && match) {
                        match = log.getCreateTime().isBefore(endTime) || log.getCreateTime().isEqual(endTime);
                    }
                    return match;
                })
                .sorted((l1, l2) -> l2.getCreateTime().compareTo(l1.getCreateTime()))
                .collect(Collectors.toList());
            
            return buildPageResult(filteredLogs, current, size);
            
        } catch (Exception e) {
            logger.error("查询操作日志异常", e);
            return buildEmptyPageResult(current, size);
        }
    }
    
    @Override
    public Object getBusinessLogs(String businessType, Long businessId, Long operatorId, String result,
                                LocalDateTime startTime, LocalDateTime endTime, Long current, Long size) {
        try {
            logger.debug("查询业务日志 - 业务类型: {}, 业务ID: {}, 操作人: {}, 结果: {}", 
                businessType, businessId, operatorId, result);
            
            List<BusinessLog> filteredLogs = businessLogCache.values().stream()
                .filter(log -> {
                    boolean match = true;
                    if (businessType != null && !businessType.trim().isEmpty()) {
                        match = businessType.equals(log.getBusinessType());
                    }
                    if (businessId != null && match) {
                        match = businessId.equals(log.getBusinessId());
                    }
                    if (operatorId != null && match) {
                        match = operatorId.equals(log.getOperatorId());
                    }
                    if (result != null && !result.trim().isEmpty() && match) {
                        match = result.equals(log.getResult());
                    }
                    if (startTime != null && match) {
                        match = log.getCreateTime().isAfter(startTime) || log.getCreateTime().isEqual(startTime);
                    }
                    if (endTime != null && match) {
                        match = log.getCreateTime().isBefore(endTime) || log.getCreateTime().isEqual(endTime);
                    }
                    return match;
                })
                .sorted((l1, l2) -> l2.getCreateTime().compareTo(l1.getCreateTime()))
                .collect(Collectors.toList());
            
            return buildPageResult(filteredLogs, current, size);
            
        } catch (Exception e) {
            logger.error("查询业务日志异常", e);
            return buildEmptyPageResult(current, size);
        }
    }
    
    @Override
    public Object getExceptionLogs(String entityType, Long operatorId, LocalDateTime startTime, 
                                 LocalDateTime endTime, Long current, Long size) {
        try {
            logger.debug("查询异常日志 - 实体类型: {}, 操作人: {}", entityType, operatorId);
            
            List<ExceptionLog> filteredLogs = exceptionLogCache.values().stream()
                .filter(log -> {
                    boolean match = true;
                    if (entityType != null && !entityType.trim().isEmpty()) {
                        match = entityType.equals(log.getEntityType());
                    }
                    if (operatorId != null && match) {
                        match = operatorId.equals(log.getOperatorId());
                    }
                    if (startTime != null && match) {
                        match = log.getCreateTime().isAfter(startTime) || log.getCreateTime().isEqual(startTime);
                    }
                    if (endTime != null && match) {
                        match = log.getCreateTime().isBefore(endTime) || log.getCreateTime().isEqual(endTime);
                    }
                    return match;
                })
                .sorted((l1, l2) -> l2.getCreateTime().compareTo(l1.getCreateTime()))
                .collect(Collectors.toList());
            
            return buildPageResult(filteredLogs, current, size);
            
        } catch (Exception e) {
            logger.error("查询异常日志异常", e);
            return buildEmptyPageResult(current, size);
        }
    }
    
    @Override
    public Object getSecurityLogs(String securityEvent, Long userId, Integer riskLevel,
                                LocalDateTime startTime, LocalDateTime endTime, Long current, Long size) {
        try {
            logger.debug("查询安全日志 - 事件: {}, 用户: {}, 风险等级: {}", securityEvent, userId, riskLevel);
            
            List<SecurityLog> filteredLogs = securityLogCache.values().stream()
                .filter(log -> {
                    boolean match = true;
                    if (securityEvent != null && !securityEvent.trim().isEmpty()) {
                        match = securityEvent.equals(log.getSecurityEvent());
                    }
                    if (userId != null && match) {
                        match = userId.equals(log.getUserId());
                    }
                    if (riskLevel != null && match) {
                        match = riskLevel.equals(log.getRiskLevel());
                    }
                    if (startTime != null && match) {
                        match = log.getCreateTime().isAfter(startTime) || log.getCreateTime().isEqual(startTime);
                    }
                    if (endTime != null && match) {
                        match = log.getCreateTime().isBefore(endTime) || log.getCreateTime().isEqual(endTime);
                    }
                    return match;
                })
                .sorted((l1, l2) -> l2.getCreateTime().compareTo(l1.getCreateTime()))
                .collect(Collectors.toList());
            
            return buildPageResult(filteredLogs, current, size);
            
        } catch (Exception e) {
            logger.error("查询安全日志异常", e);
            return buildEmptyPageResult(current, size);
        }
    }
    
    @Override
    public Object getLogStatistics(String logType, LocalDateTime startTime, LocalDateTime endTime, String groupBy) {
        try {
            logger.debug("获取日志统计 - 类型: {}, 分组: {}", logType, groupBy);
            
            Map<String, Object> statistics = new HashMap<>();
            
            switch (logType.toLowerCase()) {
                case "operation":
                    statistics = getOperationLogStatistics(startTime, endTime, groupBy);
                    break;
                case "business":
                    statistics = getBusinessLogStatistics(startTime, endTime, groupBy);
                    break;
                case "exception":
                    statistics = getExceptionLogStatistics(startTime, endTime, groupBy);
                    break;
                case "security":
                    statistics = getSecurityLogStatistics(startTime, endTime, groupBy);
                    break;
                default:
                    logger.warn("不支持的日志类型: {}", logType);
                    statistics.put("error", "不支持的日志类型");
            }
            
            return statistics;
            
        } catch (Exception e) {
            logger.error("获取日志统计异常 - 类型: {}", logType, e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "统计异常: " + e.getMessage());
            return errorResult;
        }
    }
    
    @Override
    public String exportLogs(String logType, Map<String, Object> filters, LocalDateTime startTime, 
                           LocalDateTime endTime, String format) {
        try {
            logger.info("导出日志 - 类型: {}, 格式: {}", logType, format);
            
            // 模拟导出功能
            String fileName = String.format("%s_logs_%s.%s", 
                logType, 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")),
                format.toLowerCase());
            
            String exportPath = "/tmp/exports/" + fileName;
            
            // 实际实现中应该根据logType和filters查询数据，然后导出到指定格式的文件
            logger.info("日志导出完成 - 文件: {}", exportPath);
            
            return exportPath;
            
        } catch (Exception e) {
            logger.error("导出日志异常 - 类型: {}", logType, e);
            return null;
        }
    }
    
    @Override
    public LogCleanupResult cleanupExpiredLogs(String logType, Integer retentionDays) {
        try {
            logger.info("清理过期日志 - 类型: {}, 保留天数: {}", logType, retentionDays);
            
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
            int cleanedCount = 0;
            
            switch (logType.toLowerCase()) {
                case "operation":
                    cleanedCount = cleanupOperationLogs(cutoffTime);
                    break;
                case "business":
                    cleanedCount = cleanupBusinessLogs(cutoffTime);
                    break;
                case "exception":
                    cleanedCount = cleanupExceptionLogs(cutoffTime);
                    break;
                case "security":
                    cleanedCount = cleanupSecurityLogs(cutoffTime);
                    break;
                default:
                    return new LogCleanupResult(false, "不支持的日志类型: " + logType, 0);
            }
            
            logger.info("清理过期日志完成 - 类型: {}, 清理数量: {}", logType, cleanedCount);
            return new LogCleanupResult(true, "清理完成", cleanedCount);
            
        } catch (Exception e) {
            logger.error("清理过期日志异常 - 类型: {}", logType, e);
            return new LogCleanupResult(false, "清理异常: " + e.getMessage(), 0);
        }
    }
    
    @Override
    @Async
    public BatchLogResult batchRecordLogs(List<LogEntry> logEntries) {
        try {
            logger.info("批量记录日志 - 数量: {}", logEntries.size());
            
            int totalCount = logEntries.size();
            int successCount = 0;
            int failCount = 0;
            List<String> errors = new ArrayList<>();
            
            for (LogEntry entry : logEntries) {
                try {
                    Long logId = null;
                    
                    switch (entry.getLogType().toLowerCase()) {
                        case "operation":
                            logId = recordOperationLog(entry.getAction(), entry.getEntityType(), 
                                entry.getEntityId(), entry.getOperatorId(), entry.getOperatorName(),
                                entry.getDescription(), entry.getOldValue(), entry.getNewValue(),
                                entry.getIpAddress(), entry.getUserAgent());
                            break;
                        case "business":
                            logId = recordBusinessLog(entry.getEntityType(), entry.getEntityId(),
                                entry.getOperatorId(), entry.getOperatorName(), entry.getAction(),
                                "SUCCESS", 0L, entry.getExtendInfo());
                            break;
                        case "exception":
                            logId = recordExceptionLog(entry.getAction(), entry.getEntityType(),
                                entry.getEntityId(), entry.getOperatorId(), entry.getDescription(),
                                "", "");
                            break;
                        default:
                            errors.add("不支持的日志类型: " + entry.getLogType());
                            failCount++;
                            continue;
                    }
                    
                    if (logId != null) {
                        successCount++;
                    } else {
                        failCount++;
                        errors.add("记录日志失败");
                    }
                    
                } catch (Exception e) {
                    failCount++;
                    errors.add("记录日志异常: " + e.getMessage());
                    logger.error("批量记录日志项异常", e);
                }
            }
            
            boolean success = failCount == 0;
            String message = String.format("批量记录完成 - 总数: %d, 成功: %d, 失败: %d", 
                totalCount, successCount, failCount);
            
            BatchLogResult result = new BatchLogResult(success, message, totalCount, successCount, failCount);
            result.setErrors(errors);
            
            logger.info("批量记录日志完成 - {}", message);
            return result;
            
        } catch (Exception e) {
            logger.error("批量记录日志异常", e);
            return new BatchLogResult(false, "批量记录异常: " + e.getMessage(), 
                logEntries.size(), 0, logEntries.size());
        }
    }
    
    /**
     * 定时清理过期日志
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void scheduledCleanupExpiredLogs() {
        logger.info("开始定时清理过期日志");
        
        for (Map.Entry<String, Integer> entry : LOG_RETENTION_DAYS.entrySet()) {
            String logType = entry.getKey();
            Integer retentionDays = entry.getValue();
            
            try {
                LogCleanupResult result = cleanupExpiredLogs(logType, retentionDays);
                logger.info("定时清理日志结果 - 类型: {}, 结果: {}, 清理数量: {}", 
                    logType, result.isSuccess(), result.getCleanedCount());
            } catch (Exception e) {
                logger.error("定时清理日志异常 - 类型: {}", logType, e);
            }
        }
        
        logger.info("定时清理过期日志完成");
    }
    
    // ==================== 私有方法 ====================
    
    private <T> Object buildPageResult(List<T> data, Long current, Long size) {
        int start = (int) ((current - 1) * size);
        int end = Math.min(start + size.intValue(), data.size());
        List<T> pageData = data.subList(start, end);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageData);
        result.put("total", data.size());
        result.put("current", current);
        result.put("size", size);
        result.put("pages", (data.size() + size - 1) / size);
        
        return result;
    }
    
    private Object buildEmptyPageResult(Long current, Long size) {
        Map<String, Object> result = new HashMap<>();
        result.put("records", new ArrayList<>());
        result.put("total", 0);
        result.put("current", current);
        result.put("size", size);
        result.put("pages", 0);
        
        return result;
    }
    
    private Map<String, Object> getOperationLogStatistics(LocalDateTime startTime, LocalDateTime endTime, String groupBy) {
        Map<String, Object> statistics = new HashMap<>();
        
        List<OperationLog> logs = operationLogCache.values().stream()
            .filter(log -> {
                boolean match = true;
                if (startTime != null) {
                    match = log.getCreateTime().isAfter(startTime) || log.getCreateTime().isEqual(startTime);
                }
                if (endTime != null && match) {
                    match = log.getCreateTime().isBefore(endTime) || log.getCreateTime().isEqual(endTime);
                }
                return match;
            })
            .collect(Collectors.toList());
        
        statistics.put("total", logs.size());
        
        if ("action".equals(groupBy)) {
            Map<String, Long> actionStats = logs.stream()
                .collect(Collectors.groupingBy(OperationLog::getAction, Collectors.counting()));
            statistics.put("actionStats", actionStats);
        } else if ("operator".equals(groupBy)) {
            Map<String, Long> operatorStats = logs.stream()
                .collect(Collectors.groupingBy(OperationLog::getOperatorName, Collectors.counting()));
            statistics.put("operatorStats", operatorStats);
        }
        
        return statistics;
    }
    
    private Map<String, Object> getBusinessLogStatistics(LocalDateTime startTime, LocalDateTime endTime, String groupBy) {
        Map<String, Object> statistics = new HashMap<>();
        
        List<BusinessLog> logs = businessLogCache.values().stream()
            .filter(log -> {
                boolean match = true;
                if (startTime != null) {
                    match = log.getCreateTime().isAfter(startTime) || log.getCreateTime().isEqual(startTime);
                }
                if (endTime != null && match) {
                    match = log.getCreateTime().isBefore(endTime) || log.getCreateTime().isEqual(endTime);
                }
                return match;
            })
            .collect(Collectors.toList());
        
        statistics.put("total", logs.size());
        
        if ("businessType".equals(groupBy)) {
            Map<String, Long> typeStats = logs.stream()
                .collect(Collectors.groupingBy(BusinessLog::getBusinessType, Collectors.counting()));
            statistics.put("businessTypeStats", typeStats);
        } else if ("result".equals(groupBy)) {
            Map<String, Long> resultStats = logs.stream()
                .collect(Collectors.groupingBy(BusinessLog::getResult, Collectors.counting()));
            statistics.put("resultStats", resultStats);
        }
        
        return statistics;
    }
    
    private Map<String, Object> getExceptionLogStatistics(LocalDateTime startTime, LocalDateTime endTime, String groupBy) {
        Map<String, Object> statistics = new HashMap<>();
        
        List<ExceptionLog> logs = exceptionLogCache.values().stream()
            .filter(log -> {
                boolean match = true;
                if (startTime != null) {
                    match = log.getCreateTime().isAfter(startTime) || log.getCreateTime().isEqual(startTime);
                }
                if (endTime != null && match) {
                    match = log.getCreateTime().isBefore(endTime) || log.getCreateTime().isEqual(endTime);
                }
                return match;
            })
            .collect(Collectors.toList());
        
        statistics.put("total", logs.size());
        
        if ("action".equals(groupBy)) {
            Map<String, Long> actionStats = logs.stream()
                .collect(Collectors.groupingBy(ExceptionLog::getAction, Collectors.counting()));
            statistics.put("actionStats", actionStats);
        }
        
        return statistics;
    }
    
    private Map<String, Object> getSecurityLogStatistics(LocalDateTime startTime, LocalDateTime endTime, String groupBy) {
        Map<String, Object> statistics = new HashMap<>();
        
        List<SecurityLog> logs = securityLogCache.values().stream()
            .filter(log -> {
                boolean match = true;
                if (startTime != null) {
                    match = log.getCreateTime().isAfter(startTime) || log.getCreateTime().isEqual(startTime);
                }
                if (endTime != null && match) {
                    match = log.getCreateTime().isBefore(endTime) || log.getCreateTime().isEqual(endTime);
                }
                return match;
            })
            .collect(Collectors.toList());
        
        statistics.put("total", logs.size());
        
        if ("securityEvent".equals(groupBy)) {
            Map<String, Long> eventStats = logs.stream()
                .collect(Collectors.groupingBy(SecurityLog::getSecurityEvent, Collectors.counting()));
            statistics.put("securityEventStats", eventStats);
        } else if ("riskLevel".equals(groupBy)) {
            Map<Integer, Long> riskStats = logs.stream()
                .collect(Collectors.groupingBy(SecurityLog::getRiskLevel, Collectors.counting()));
            statistics.put("riskLevelStats", riskStats);
        }
        
        return statistics;
    }
    
    private int cleanupOperationLogs(LocalDateTime cutoffTime) {
        List<Long> expiredIds = operationLogCache.entrySet().stream()
            .filter(entry -> entry.getValue().getCreateTime().isBefore(cutoffTime))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        expiredIds.forEach(operationLogCache::remove);
        return expiredIds.size();
    }
    
    private int cleanupBusinessLogs(LocalDateTime cutoffTime) {
        List<Long> expiredIds = businessLogCache.entrySet().stream()
            .filter(entry -> entry.getValue().getCreateTime().isBefore(cutoffTime))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        expiredIds.forEach(businessLogCache::remove);
        return expiredIds.size();
    }
    
    private int cleanupExceptionLogs(LocalDateTime cutoffTime) {
        List<Long> expiredIds = exceptionLogCache.entrySet().stream()
            .filter(entry -> entry.getValue().getCreateTime().isBefore(cutoffTime))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        expiredIds.forEach(exceptionLogCache::remove);
        return expiredIds.size();
    }
    
    private int cleanupSecurityLogs(LocalDateTime cutoffTime) {
        List<Long> expiredIds = securityLogCache.entrySet().stream()
            .filter(entry -> entry.getValue().getCreateTime().isBefore(cutoffTime))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        expiredIds.forEach(securityLogCache::remove);
        return expiredIds.size();
    }
    
    // ==================== 内部类定义 ====================
    
    /**
     * 操作日志实体
     */
    private static class OperationLog {
        private Long id;
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
        private LocalDateTime createTime;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
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
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    }
    
    /**
     * 业务日志实体
     */
    private static class BusinessLog {
        private Long id;
        private String businessType;
        private Long businessId;
        private Long operatorId;
        private String operatorName;
        private String operation;
        private String result;
        private Long duration;
        private Map<String, Object> extendInfo;
        private LocalDateTime createTime;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }
        public Long getBusinessId() { return businessId; }
        public void setBusinessId(Long businessId) { this.businessId = businessId; }
        public Long getOperatorId() { return operatorId; }
        public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
        public String getOperatorName() { return operatorName; }
        public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
        public Long getDuration() { return duration; }
        public void setDuration(Long duration) { this.duration = duration; }
        public Map<String, Object> getExtendInfo() { return extendInfo; }
        public void setExtendInfo(Map<String, Object> extendInfo) { this.extendInfo = extendInfo; }
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    }
    
    /**
     * 异常日志实体
     */
    private static class ExceptionLog {
        private Long id;
        private String action;
        private String entityType;
        private Long entityId;
        private Long operatorId;
        private String errorMessage;
        private String stackTrace;
        private String requestParams;
        private LocalDateTime createTime;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getEntityType() { return entityType; }
        public void setEntityType(String entityType) { this.entityType = entityType; }
        public Long getEntityId() { return entityId; }
        public void setEntityId(Long entityId) { this.entityId = entityId; }
        public Long getOperatorId() { return operatorId; }
        public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public String getStackTrace() { return stackTrace; }
        public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
        public String getRequestParams() { return requestParams; }
        public void setRequestParams(String requestParams) { this.requestParams = requestParams; }
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    }
    
    /**
     * 安全日志实体
     */
    private static class SecurityLog {
        private Long id;
        private String securityEvent;
        private Long userId;
        private String userName;
        private String description;
        private Integer riskLevel;
        private String ipAddress;
        private String location;
        private LocalDateTime createTime;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getSecurityEvent() { return securityEvent; }
        public void setSecurityEvent(String securityEvent) { this.securityEvent = securityEvent; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Integer getRiskLevel() { return riskLevel; }
        public void setRiskLevel(Integer riskLevel) { this.riskLevel = riskLevel; }
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    }
}