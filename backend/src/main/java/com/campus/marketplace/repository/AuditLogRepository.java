package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.AuditLog;
import com.campus.marketplace.common.enums.AuditActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 审计日志 Repository
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * 查询指定操作人的审计日志
     */
    Page<AuditLog> findByOperatorId(Long operatorId, Pageable pageable);

    /**
     * 查询指定操作类型的审计日志
     */
    Page<AuditLog> findByActionType(AuditActionType actionType, Pageable pageable);

    /**
     * 查询指定对象的审计日志
     */
    Page<AuditLog> findByTargetTypeAndTargetId(String targetType, Long targetId, Pageable pageable);

    /**
     * 查询指定时间范围的审计日志
     */
    Page<AuditLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
}
