package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
/**
 * Feature Flag Repository
 *
 * @author BaSui
 * @date 2025-10-29
 */


public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, Long> {
    Optional<FeatureFlag> findByKey(String key);
}
