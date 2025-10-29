package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.NotificationPreference;
import com.campus.marketplace.common.enums.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Notification Preference Repository
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    Optional<NotificationPreference> findByUserIdAndChannel(Long userId, NotificationChannel channel);
}
