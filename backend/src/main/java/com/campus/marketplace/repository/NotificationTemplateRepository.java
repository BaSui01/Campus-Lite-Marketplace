package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Notification Template Repository
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    Optional<NotificationTemplate> findByCode(String code);
}
