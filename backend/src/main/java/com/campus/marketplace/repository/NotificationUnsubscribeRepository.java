package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.NotificationUnsubscribe;
import com.campus.marketplace.common.enums.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Notification Unsubscribe Repository
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Repository
public interface NotificationUnsubscribeRepository extends JpaRepository<NotificationUnsubscribe, Long> {
    boolean existsByUserIdAndTemplateCodeAndChannel(Long userId, String templateCode, NotificationChannel channel);
    void deleteByUserIdAndTemplateCodeAndChannel(Long userId, String templateCode, NotificationChannel channel);
}
