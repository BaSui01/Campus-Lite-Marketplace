package com.campus.marketplace.common.dto;

import com.campus.marketplace.common.entity.DisputeEvidence;
import com.campus.marketplace.common.enums.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 证据信息DTO
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 证据ID
     */
    private Long id;

    /**
     * 纠纷ID
     */
    private Long disputeId;

    /**
     * 上传者ID
     */
    private Long uploaderId;

    /**
     * 上传者昵称
     */
    private String uploaderNickname;

    /**
     * 上传者角色
     */
    private DisputeRole uploaderRole;

    /**
     * 证据类型
     */
    private EvidenceType evidenceType;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 证据描述
     */
    private String description;

    /**
     * 证据有效性
     */
    private EvidenceValidity validity;

    /**
     * 有效性评估说明
     */
    private String validityReason;

    /**
     * 评估人ID
     */
    private Long evaluatedBy;

    /**
     * 评估人昵称
     */
    private String evaluatorNickname;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 从实体转换为DTO
     */
    public static EvidenceDTO from(DisputeEvidence evidence) {
        if (evidence == null) {
            return null;
        }

        return EvidenceDTO.builder()
                .id(evidence.getId())
                .disputeId(evidence.getDisputeId())
                .uploaderId(evidence.getUploaderId())
                .uploaderNickname(evidence.getUploader() != null ? evidence.getUploader().getNickname() : null)
                .uploaderRole(evidence.getUploaderRole())
                .evidenceType(evidence.getEvidenceType())
                .fileUrl(evidence.getFileUrl())
                .fileName(evidence.getFileName())
                .fileSize(evidence.getFileSize())
                .description(evidence.getDescription())
                .validity(evidence.getValidity())
                .validityReason(evidence.getValidityReason())
                .evaluatedBy(evidence.getEvaluatedBy())
                .createdAt(evidence.getCreatedAt())
                .build();
    }

    /**
     * 检查是否已评估
     */
    public boolean isEvaluated() {
        return this.validity != null;
    }

    /**
     * 检查是否有效
     */
    public boolean isValid() {
        return this.validity == EvidenceValidity.VALID;
    }
}
