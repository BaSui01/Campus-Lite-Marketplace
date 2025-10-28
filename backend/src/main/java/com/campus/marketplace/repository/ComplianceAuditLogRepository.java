package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.ComplianceAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplianceAuditLogRepository extends JpaRepository<ComplianceAuditLog, Long> {
    Page<ComplianceAuditLog> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(String targetType, Long targetId, Pageable pageable);
}
