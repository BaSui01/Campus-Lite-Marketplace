package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.PrivacyRequestStatus;
import com.campus.marketplace.common.enums.PrivacyRequestType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 隐私请求实体
 *
 * 记录数据导出与删除申请，支持审核与延迟处理
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "t_privacy_request", indexes = {
        @Index(name = "idx_privacy_user", columnList = "user_id"),
        @Index(name = "idx_privacy_status", columnList = "status")
})
public class PrivacyRequest extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 30)
    private PrivacyRequestType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PrivacyRequestStatus status = PrivacyRequestStatus.PENDING;

    @Column(name = "reason", length = 200)
    private String reason;

    @Column(name = "result_path", length = 255)
    private String resultPath;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
