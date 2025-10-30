package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.AuditActionType;
import jakarta.persistence.*;
import lombok.*;

/**
 * 审计日志实体
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Entity
@Table(name = "t_audit_log", indexes = {
        @Index(name = "idx_audit_operator", columnList = "operator_id"),
        @Index(name = "idx_audit_action", columnList = "action_type"),
        @Index(name = "idx_audit_target", columnList = "target_type, target_id"),
        @Index(name = "idx_audit_created_at", columnList = "created_at")
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
}
