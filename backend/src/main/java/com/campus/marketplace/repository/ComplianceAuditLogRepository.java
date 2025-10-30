package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.ComplianceAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Compliance Audit Log Repository
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Repository
public interface ComplianceAuditLogRepository extends JpaRepository<ComplianceAuditLog, Long> {
    Page<ComplianceAuditLog> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(String targetType, Long targetId, Pageable pageable);
}
