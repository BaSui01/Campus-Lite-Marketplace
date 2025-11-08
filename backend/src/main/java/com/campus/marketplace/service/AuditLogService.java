package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.AuditLogResponse;
import com.campus.marketplace.common.enums.AuditActionType;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

/**
 * 审计日志服务接口 - 增强版支持数据追踪和撤销
 * 
 * @author BaSui
 * @date 2025-10-27
 * @updated 2025-11-02 - 增加数据追踪功能
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

    // ===== 增强方法 - 支持数据追踪和撤销 =====

    /**
     * 记录实体变更前后数据
     * 
     * @param operatorId 操作人ID
     * @param operatorName 操作人用户名
     * @param actionType 操作类型
     * @param entityName 实体名称
     * @param entityId 实体ID
     * @param oldValue 变更前数据
     * @param newValue 变更后数据
     */
    void logEntityChange(Long operatorId, String operatorName, AuditActionType actionType, 
                        String entityName, Long entityId, Object oldValue, Object newValue);

    /**
     * 记录批量操作审计
     * 
     * @param operatorId 操作人ID
     * @param operatorName 操作人用户名
     * @param actionType 操作类型
     * @param targetType 目标类型
     * @param targetIds 批量目标ID（逗号分隔）
     * @param details 操作详情
     * @param isReversible 是否可撤销
     */
    void logBatchOperation(Long operatorId, String operatorName, AuditActionType actionType,
                          String targetType, String targetIds, String details, 
                          boolean isReversible);

    /**
     * 记录可撤销操作
     *
     * @param operatorId 操作人ID
     * @param operatorName 操作人用户名
     * @param actionType 操作类型
     * @param entityName 实体名称
     * @param entityId 实体ID
     * @param oldValue 变更前数据
     * @param newValue 变更后数据
     */
    void logReversibleAction(Long operatorId, String operatorName, AuditActionType actionType,
                            String entityName, Long entityId, Object oldValue, Object newValue);

    /**
     * 获取操作日志统计数据
     *
     * @param operatorId 操作人ID（可选）
     * @param actionType 操作类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 统计数据（总操作数、成功数、失败数、今日操作数）
     */
    java.util.Map<String, Object> getStatistics(Long operatorId, AuditActionType actionType,
                                                LocalDateTime startTime, LocalDateTime endTime);
}
