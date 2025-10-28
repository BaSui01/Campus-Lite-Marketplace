package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.ComplianceAction;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 文本/图片合规审计日志。
 *
 * 记录命中词、截断片段、操作者与目标对象。
 *
 * @author BaSui
 * @date 2025-10-28
 */
@Entity
@Table(name = "t_compliance_audit", indexes = {
        @Index(name = "idx_comp_target", columnList = "target_type,target_id"),
        @Index(name = "idx_comp_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceAuditLog {

    /** 主键 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 场景标识（如 POST_CONTENT、MESSAGE_CONTENT） */
    @Column(name = "scene", length = 50)
    private String scene;

    /** 处置动作（BLOCK/REVIEW/PASS） */
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private ComplianceAction action;

    /** 目标对象类型 */
    @Column(name = "target_type", length = 50)
    private String targetType;

    /** 目标对象 ID */
    @Column(name = "target_id")
    private Long targetId;

    /** 操作人 ID */
    @Column(name = "operator_id")
    private Long operatorId;

    /** 操作人名称 */
    @Column(name = "operator_name", length = 50)
    private String operatorName;

    /** 命中敏感词 */
    @Column(name = "hit_words", length = 500)
    private String hitWords;

    /** 命中片段（截断） */
    @Column(name = "snippet", columnDefinition = "TEXT")
    private String snippet;

    /** 创建时间 */
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
