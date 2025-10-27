package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 封禁日志实体
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Entity
@Table(name = "t_ban_log", indexes = {
        @Index(name = "idx_ban_user", columnList = "user_id"),
        @Index(name = "idx_ban_admin", columnList = "admin_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BanLog extends BaseEntity {

    /**
     * 被封禁用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 管理员ID
     */
    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    /**
     * 封禁原因
     */
    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    /**
     * 封禁天数（0表示永久）
     */
    @Column(name = "days", nullable = false)
    private Integer days;

    /**
     * 解封时间（null表示永久封禁）
     */
    @Column(name = "unban_time")
    private LocalDateTime unbanTime;

    /**
     * 是否已解封
     */
    @Column(name = "is_unbanned", nullable = false)
    @Builder.Default
    private Boolean isUnbanned = false;
}
