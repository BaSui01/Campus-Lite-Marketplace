package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_notification_template", indexes = {
        @Index(name = "uk_tpl_code", columnList = "code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "title_key", nullable = false, length = 200)
    private String titleKey;

    @Column(name = "content_key", nullable = false, length = 200)
    private String contentKey;

    /**
     * 可用渠道：逗号分隔，例如 IN_APP,EMAIL,WEB_PUSH
     */
    @Column(name = "channels", nullable = false, length = 100)
    private String channels;
}
