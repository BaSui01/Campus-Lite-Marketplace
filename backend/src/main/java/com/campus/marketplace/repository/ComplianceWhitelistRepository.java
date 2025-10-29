package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.ComplianceWhitelist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Compliance Whitelist Repository
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Repository
public interface ComplianceWhitelistRepository extends JpaRepository<ComplianceWhitelist, Long> {
    boolean existsByTypeAndTargetId(String type, Long targetId);
}
