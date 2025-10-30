package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.AuditLogResponse;
import com.campus.marketplace.common.enums.AuditActionType;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

/**
 * 审计日志服务接口
 * 
 * @author BaSui
 * @date 2025-10-27
 */
public interface AuditLogService {

    /**
     * 记录审计日志（同步）
     * 
     * @param operatorId 操作人ID
     * @param operatorName 操作人用户名
     * @param actionType 操作类型
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param details 操作详情
     * @param result 操作结果
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     */
    void logAction(Long operatorId, String operatorName, AuditActionType actionType, 
                   String targetType, Long targetId, String details, String result,
                   String ipAddress, String userAgent);

    /**
     * 记录审计日志（异步）
     */
    void logActionAsync(Long operatorId, String operatorName, AuditActionType actionType,
                        String targetType, Long targetId, String details, String result,
                        String ipAddress, String userAgent);

    /**
     * 查询审计日志列表
     * 
     * @param operatorId 操作人ID（可选）
     * @param actionType 操作类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 审计日志分页结果
     */
    Page<AuditLogResponse> listAuditLogs(Long operatorId, AuditActionType actionType, 
                                         LocalDateTime startTime, LocalDateTime endTime,
                                         int page, int size);
}
