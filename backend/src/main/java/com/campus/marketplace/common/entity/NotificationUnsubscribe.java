package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.NotificationChannel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_notification_unsubscribe", indexes = {
        @Index(name = "idx_unsub_user", columnList = "user_id"),
        @Index(name = "idx_unsub_channel", columnList = "channel")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_tpl_channel", columnNames = {"user_id", "template_code", "channel"})
})
/**
 * Notification Unsubscribe
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationUnsubscribe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "template_code", nullable = false, length = 100)
    private String templateCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;
}
