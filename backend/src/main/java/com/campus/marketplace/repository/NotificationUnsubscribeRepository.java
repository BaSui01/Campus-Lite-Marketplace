package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.NotificationUnsubscribe;
import com.campus.marketplace.common.enums.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationUnsubscribeRepository extends JpaRepository<NotificationUnsubscribe, Long> {
    boolean existsByUserIdAndTemplateCodeAndChannel(Long userId, String templateCode, NotificationChannel channel);
    void deleteByUserIdAndTemplateCodeAndChannel(Long userId, String templateCode, NotificationChannel channel);
}
