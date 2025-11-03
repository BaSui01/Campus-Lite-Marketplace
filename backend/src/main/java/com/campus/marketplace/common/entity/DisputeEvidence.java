package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.*;
import jakarta.persistence.*;
import lombok.*;

/**
 * 纠纷证据实体
 *
 * 存储纠纷相关的证据材料（图片、视频、聊天记录等）
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Entity
@Table(name = "t_dispute_evidence", indexes = {
    @Index(name = "idx_evidence_dispute", columnList = "dispute_id"),
    @Index(name = "idx_evidence_uploader", columnList = "uploader_id"),
    @Index(name = "idx_evidence_type", columnList = "evidence_type"),
    @Index(name = "idx_evidence_validity", columnList = "validity"),
    @Index(name = "idx_evidence_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputeEvidence extends BaseEntity {

    /**
     * 关联纠纷ID
     */
    @Column(name = "dispute_id", nullable = false)
    private Long disputeId;

    /**
     * 纠纷（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispute_id", insertable = false, updatable = false)
    private Dispute dispute;

    /**
     * 上传者ID（买家或卖家）
     */
    @Column(name = "uploader_id", nullable = false)
    private Long uploaderId;

    /**
     * 上传者（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", insertable = false, updatable = false)
    private User uploader;

    /**
     * 上传者角色
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "uploader_role", nullable = false, length = 20)
    private DisputeRole uploaderRole;

    /**
     * 证据类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_type", nullable = false, length = 30)
    private EvidenceType evidenceType;

    /**
     * 文件URL
     */
    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    /**
     * 文件名
     */
    @Column(name = "file_name", nullable = false, length = 200)
    private String fileName;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * 证据描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 证据有效性（仲裁员评估）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "validity", length = 20)
    private EvidenceValidity validity;

    /**
     * 有效性评估说明
     */
    @Column(name = "validity_reason", columnDefinition = "TEXT")
    private String validityReason;

    /**
     * 评估人ID（仲裁员）
     */
    @Column(name = "evaluated_by")
    private Long evaluatedBy;

    /**
     * 检查证据是否有效
     */
    public boolean isValid() {
        return this.validity == EvidenceValidity.VALID;
    }

    /**
     * 检查证据是否无效
     */
    public boolean isInvalid() {
        return this.validity == EvidenceValidity.INVALID;
    }

    /**
     * 检查证据是否存疑
     */
    public boolean isDoubtful() {
        return this.validity == EvidenceValidity.DOUBTFUL;
    }

    /**
     * 检查是否图片证据
     */
    public boolean isImage() {
        return this.evidenceType == EvidenceType.IMAGE;
    }

    /**
     * 检查是否视频证据
     */
    public boolean isVideo() {
        return this.evidenceType == EvidenceType.VIDEO;
    }

    /**
     * 检查是否聊天记录
     */
    public boolean isChatRecord() {
        return this.evidenceType == EvidenceType.CHAT_RECORD;
    }
}
