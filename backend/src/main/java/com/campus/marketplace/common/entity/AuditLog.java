package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.AuditActionType;
import com.campus.marketplace.common.enums.AuditEntityType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 审计日志实体增强版 - 支持数据追踪和撤销功能
 * 
 * @author BaSui
 * @date 2025-10-27
 * @updated 2025-11-02 - 增强数据追踪功能
 */
@Entity
@Table(name = "t_audit_log", indexes = {
        @Index(name = "idx_audit_operator", columnList = "operator_id"),
        @Index(name = "idx_audit_action", columnList = "action_type"),
        @Index(name = "idx_audit_target", columnList = "target_type, target_id"),
        @Index(name = "idx_audit_created_at", columnList = "created_at"),
        @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_audit_reversible", columnList = "is_reversible")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseEntity {

    /**
     * 操作人 ID
     */
    @Column(name = "operator_id")
    private Long operatorId;

    /**
     * 操作人用户名
     */
    @Column(name = "operator_name", length = 50)
    private String operatorName;

    /**
     * 操作类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private AuditActionType actionType;

    /**
     * 目标对象类型（如 User、Goods、Post）
     */
    @Column(name = "target_type", length = 50)
    private String targetType;

    /**
     * 目标对象 ID
     */
    @Column(name = "target_id")
    private Long targetId;

    /**
     * 批量操作的ID列表（逗号分隔）
     */
    @Column(name = "target_ids", columnDefinition = "TEXT")
    private String targetIds;

    /**
     * 操作详情
     */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    /**
     * 操作结果（SUCCESS/FAILED）
     */
    @Column(name = "result", length = 20)
    private String result;

    /**
     * 客户端IP
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /**
     * 用户代理
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    // ===== 增强字段 - 支持数据追踪和撤销 =====

    /**
     * 修改前的数据（JSON格式）
     */
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    /**
     * 修改后的数据（JSON格式）
     */
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    /**
     * 实体名称
     */
    @Column(name = "entity_name", nullable = false, length = 50)
    private String entityName;

    /**
     * 实体类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 20)
    private AuditEntityType entityType;

    /**
     * 被操作实体的ID
     */
    @Column(name = "entity_id")
    private Long entityId;

    /**
     * 是否可撤销
     */
    @Column(name = "is_reversible")
    @Builder.Default
    private Boolean isReversible = false;

    /**
     * 撤销截止时间（超过此时间不可撤销）
     */
    @Column(name = "revert_deadline")
    private LocalDateTime revertDeadline;

    /**
     * 撤销操作的审计日志ID
     */
    @Column(name = "reverted_by_log_id")
    private Long revertedByLogId;

    /**
     * 撤销时间
     */
    @Column(name = "reverted_at")
    private LocalDateTime revertedAt;

    /**
     * 撤销次数（支持多次撤销和重做）
     */
    @Column(name = "revert_count")
    @Builder.Default
    private Integer revertCount = 0;

    /**
     * 检查是否在撤销期限内
     */
    public boolean isWithinRevertDeadline() {
        if (revertDeadline == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(revertDeadline);
    }

    /**
     * 检查是否已被撤销
     */
    public boolean isReverted() {
        return revertedByLogId != null && revertedAt != null;
    }
}
