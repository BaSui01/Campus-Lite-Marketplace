package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.response.AuditLogResponse;
import com.campus.marketplace.common.entity.AuditLog;
import com.campus.marketplace.common.enums.AuditActionType;
import com.campus.marketplace.common.enums.AuditEntityType;
import com.campus.marketplace.repository.AuditLogRepository;
import com.campus.marketplace.service.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Audit Log Service Impl - 增强版支持数据追踪和撤销
 *
 * @author BaSui
 * @date 2025-10-29
 * @updated 2025-11-02 - 增加数据追踪功能
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logAction(Long operatorId, String operatorName, AuditActionType actionType,
                          String targetType, Long targetId, String details, String result,
                          String ipAddress, String userAgent) {
        try {
            AuditLog log = AuditLog.builder()
                    .operatorId(operatorId)
                    .operatorName(operatorName)
                    .actionType(actionType)
                    .targetType(targetType)
                    .targetId(targetId)
                    .details(details)
                    .result(result)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            auditLogRepository.save(log);
            
            AuditLogServiceImpl.log.info("审计日志记录成功: operator={}, action={}, target={}:{}", 
                    operatorName, actionType, targetType, targetId);
        } catch (Exception e) {
            log.error("审计日志记录失败: operator={}, action={}, error={}", 
                    operatorName, actionType, e.getMessage());
        }
    }

    @Override
    @Async
    public void logActionAsync(Long operatorId, String operatorName, AuditActionType actionType,
                                String targetType, Long targetId, String details, String result,
                                String ipAddress, String userAgent) {
        logAction(operatorId, operatorName, actionType, targetType, targetId, 
                 details, result, ipAddress, userAgent);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> listAuditLogs(Long operatorId, AuditActionType actionType,
                                                LocalDateTime startTime, LocalDateTime endTime,
                                                int page, int size) {
        log.info("查询审计日志: operatorId={}, actionType={}, page={}, size={}", 
                operatorId, actionType, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> logPage;

        if (operatorId != null) {
            logPage = auditLogRepository.findByOperatorId(operatorId, pageable);
        } else if (actionType != null) {
            logPage = auditLogRepository.findByActionType(actionType, pageable);
        } else if (startTime != null && endTime != null) {
            logPage = auditLogRepository.findByCreatedAtBetween(startTime, endTime, pageable);
        } else {
            logPage = auditLogRepository.findAll(pageable);
        }

        return logPage.map(AuditLogResponse::from);
    }

    // ===== 增强方法实现 =====

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logEntityChange(Long operatorId, String operatorName, AuditActionType actionType, 
                               String entityName, Long entityId, Object oldValue, Object newValue) {
        try {
            String oldJson = objectMapper.writeValueAsString(oldValue);
            String newJson = objectMapper.writeValueAsString(newValue);
            
            AuditLog auditLog = AuditLog.builder()
                    .operatorId(operatorId)
                    .operatorName(operatorName)
                    .actionType(actionType)
                    .entityName(entityName)
                    .entityId(entityId)
                    .entityType(AuditEntityType.valueOf(entityName.toUpperCase()))
                    .oldValue(oldJson)
                    .newValue(newJson)
                    .isReversible(true)
                    .details(String.format("实体变更: %s[%d]", entityName, entityId))
                    .result("SUCCESS")
                    .build();

            auditLogRepository.save(auditLog);
            
            log.info("实体变更审计记录成功: operator={}, entity={}, id={}", 
                    operatorName, entityName, String.valueOf(entityId));
        } catch (Exception e) {
            log.error("实体变更审计记录失败: operator={}, entity={}, error={}", 
                    operatorName, entityName, e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logBatchOperation(Long operatorId, String operatorName, AuditActionType actionType,
                                  String targetType, String targetIds, String details, 
                                  boolean isReversible) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .operatorId(operatorId)
                    .operatorName(operatorName)
                    .actionType(actionType)
                    .targetType(targetType)
                    .targetIds(targetIds)
                    .details(details)
                    .isReversible(isReversible)
                    .result("SUCCESS")
                    .build();

            auditLogRepository.save(auditLog);
            
            log.info("批量操作审计记录成功: operator={}, target={}, count={}", 
                    operatorName, targetType, String.valueOf(targetIds.split(",").length));
        } catch (Exception e) {
            log.error("批量操作审计记录失败: operator={}, target={}, error={}", 
                    operatorName, targetType, e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logReversibleAction(Long operatorId, String operatorName, AuditActionType actionType, 
                                   String entityName, Long entityId, Object oldValue, Object newValue) {
        try {
            String oldJson = objectMapper.writeValueAsString(oldValue);
            String newJson = objectMapper.writeValueAsString(newValue);
            
            AuditLog auditLog = AuditLog.builder()
                    .operatorId(operatorId)
                    .operatorName(operatorName)
                    .actionType(actionType)
                    .entityName(entityName)
                    .entityId(entityId)
                    .entityType(AuditEntityType.valueOf(entityName.toUpperCase()))
                    .oldValue(oldJson)
                    .newValue(newJson)
                    .isReversible(true)
                    .details(String.format("可撤销操作: %s[%d]", entityName, entityId))
                    .result("SUCCESS")
                    .build();

            auditLogRepository.save(auditLog);
            
            log.info("可撤销操作审计记录成功: operator={}, entity={}, id={}", 
                    operatorName, entityName, String.valueOf(entityId));
        } catch (Exception e) {
            log.error("可撤销操作审计记录失败: operator={}, entity={}, error={}",
                    operatorName, entityName, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getStatistics(Long operatorId, AuditActionType actionType,
                                                       java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        log.info("获取操作日志统计: operatorId={}, actionType={}, startTime={}, endTime={}",
                operatorId, actionType, startTime, endTime);

        java.util.Map<String, Object> statistics = new java.util.HashMap<>();

        try {
            // 查询所有符合条件的日志
            java.util.List<AuditLog> logs = new java.util.ArrayList<>();

            if (operatorId != null && actionType != null && startTime != null && endTime != null) {
                // 所有条件都有
                logs = auditLogRepository.findAll().stream()
                        .filter(log -> log.getOperatorId().equals(operatorId))
                        .filter(log -> log.getActionType().equals(actionType))
                        .filter(log -> log.getCreatedAt().isAfter(startTime) && log.getCreatedAt().isBefore(endTime))
                        .toList();
            } else if (operatorId != null) {
                logs = auditLogRepository.findAll().stream()
                        .filter(log -> log.getOperatorId().equals(operatorId))
                        .toList();
            } else if (actionType != null) {
                logs = auditLogRepository.findAll().stream()
                        .filter(log -> log.getActionType().equals(actionType))
                        .toList();
            } else if (startTime != null && endTime != null) {
                logs = auditLogRepository.findAll().stream()
                        .filter(log -> log.getCreatedAt().isAfter(startTime) && log.getCreatedAt().isBefore(endTime))
                        .toList();
            } else {
                logs = auditLogRepository.findAll();
            }

            // 统计总操作数
            long totalOperations = logs.size();

            // 统计成功数
            long successCount = logs.stream()
                    .filter(log -> "SUCCESS".equals(log.getResult()))
                    .count();

            // 统计失败数
            long failureCount = totalOperations - successCount;

            // 统计今日操作数
            java.time.LocalDateTime todayStart = java.time.LocalDate.now().atStartOfDay();
            long todayCount = logs.stream()
                    .filter(log -> log.getCreatedAt().isAfter(todayStart))
                    .count();

            statistics.put("totalOperations", totalOperations);
            statistics.put("successCount", successCount);
            statistics.put("failureCount", failureCount);
            statistics.put("todayCount", todayCount);

            log.info("操作日志统计成功: total={}, success={}, failure={}, today={}",
                    totalOperations, successCount, failureCount, todayCount);
        } catch (Exception e) {
            log.error("操作日志统计失败: {}", e.getMessage());
            statistics.put("totalOperations", 0);
            statistics.put("successCount", 0);
            statistics.put("failureCount", 0);
            statistics.put("todayCount", 0);
        }

        return statistics;
    }
}
