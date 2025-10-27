package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 黑名单实体
 * 
 * 用户可以将其他用户加入黑名单，屏蔽其消息
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Entity
@Table(name = "t_blacklist", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "blocked_user_id"}),
       indexes = {
        @Index(name = "idx_blacklist_user", columnList = "user_id"),
        @Index(name = "idx_blacklist_blocked_user", columnList = "blocked_user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Blacklist implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID（拉黑的用户）
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 用户（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 被拉黑的用户ID
     */
    @Column(name = "blocked_user_id", nullable = false)
    private Long blockedUserId;

    /**
     * 被拉黑的用户（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_user_id", insertable = false, updatable = false)
    private User blockedUser;

    /**
     * 拉黑原因
     */
    @Column(name = "reason", length = 500)
    private String reason;

    /**
     * 创建时间（拉黑时间）
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
