package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.SearchLog;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 * Search Log Repository
 *
 * @author BaSui
 * @date 2025-10-29
 */


public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {
}
