package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.ViewLog;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 * View Log Repository
 *
 * @author BaSui
 * @date 2025-10-29
 */


public interface ViewLogRepository extends JpaRepository<ViewLog, Long> {
}
