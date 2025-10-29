package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.response.AuditLogResponse;
import com.campus.marketplace.common.entity.AuditLog;
import com.campus.marketplace.common.enums.AuditActionType;
import com.campus.marketplace.repository.AuditLogRepository;
import com.campus.marketplace.service.AuditLogService;
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
 * Audit Log Service Impl
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

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
}
