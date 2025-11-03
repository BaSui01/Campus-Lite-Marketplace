package com.campus.marketplace.common.dto.response;

import com.campus.marketplace.common.entity.Appeal;
import com.campus.marketplace.common.entity.AppealMaterial;
import com.campus.marketplace.common.enums.AppealStatus;
import com.campus.marketplace.common.enums.MaterialStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 申诉详情响应
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppealDetailResponse {

    /**
     * 申诉ID
     */
    private Long id;

    /**
     * 申诉用户ID
     */
    private Long userId;

    /**
     * 目标对象类型
     */
    private String targetType;

    /**
     * 目标对象ID
     */
    private Long targetId;

    /**
     * 申诉类型
     */
    private String appealType;

    /**
     * 申诉原因
     */
    private String reason;

    /**
     * 申诉状态
     */
    private AppealStatus status;

    /**
     * 截止时间
     */
    private LocalDateTime deadline;

    /**
     * 审核人ID
     */
    private Long reviewerId;

    /**
     * 审核人用户名
     */
    private String reviewerName;

    /**
     * 审核时间
     */
    private LocalDateTime reviewedAt;

    /**
     * 审核意见
     */
    private String reviewComment;

    /**
     * 处理结果详情
     */
    private String resultDetails;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

     /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 附件列表
     */
    private List<String> attachments;

    /**
     * 材料文件列表
     */
    private List<AppealMaterialResponse> materials;

    /**
     * 是否可以取消
     */
    private Boolean canCancel;

    /**
     * 是否可以审核
     */
    private Boolean canReview;

    /**
     * 是否已过期
     */
    private Boolean isExpired;

    /**
     * 材料数量
     */
    private Integer materialCount;

    /**
     * 材料总大小（字节）
     */
    private Long totalMaterialSize;

    /**
     * 从 Appeal 实体创建详情响应
     * 
     * @param appeal 申诉实体
     * @param materials 材料列表
     * @return 详情响应
     */
    public static AppealDetailResponse fromAppeal(Appeal appeal, List<AppealMaterial> materials) {
        return builder()
            .id(appeal.getId())
            .userId(appeal.getUserId())
            .targetType(appeal.getTargetType().name())
            .targetId(appeal.getTargetId())
            .appealType(appeal.getAppealType().name())
            .reason(appeal.getReason())
            .status(appeal.getStatus())
            .deadline(appeal.getDeadline())
            .reviewerId(appeal.getReviewerId())
            .reviewerName(appeal.getReviewerName())
            .reviewedAt(appeal.getReviewedAt())
            .reviewComment(appeal.getReviewComment())
            .resultDetails(appeal.getResultDetails())
            .createdAt(appeal.getCreatedAt())
            .updatedAt(appeal.getUpdatedAt())
            .attachments(parseAttachments(appeal.getAttachments()))
            .materials(convertMaterials(materials))
            .canCancel(appeal.canCancel())
            .canReview(appeal.canReview())
            .isExpired(appeal.isExpired())
            .materialCount(materials != null ? materials.size() : 0)
            .totalMaterialSize(materials != null ? materials.stream().mapToLong(m -> m.getFileSize() != null ? m.getFileSize() : 0L).sum() : 0L)
            .build();
    }

    /**
     * 获取Appeal对象（向后兼容）
     */
    public Appeal getAppeal() {
        Appeal appeal = Appeal.builder()
            .userId(this.userId)
            .targetType(com.campus.marketplace.common.enums.AppealTargetType.valueOf(this.targetType))
            .targetId(this.targetId)
            .appealType(com.campus.marketplace.common.enums.AppealType.valueOf(this.appealType))
            .reason(this.reason)
            .status(this.status)
            .deadline(this.deadline)
            .reviewerId(this.reviewerId)
            .reviewerName(this.reviewerName)
            .reviewedAt(this.reviewedAt)
            .reviewComment(this.reviewComment)
            .resultDetails(this.resultDetails)
            .build();
        appeal.setId(this.id);
        return appeal;
    }

    /**
     * 转换材料列表
     */
    private static List<AppealMaterialResponse> convertMaterials(List<AppealMaterial> materials) {
        if (materials == null || materials.isEmpty()) {
            return List.of();
        }
        
        return materials.stream()
                .map(material -> new AppealMaterialResponse(
                    material.getId(),
                    material.getFileName(),
                    material.getFileSize(),
                    material.getFileType(),
                    material.getMimeType(),
                    material.getThumbnailPath(),
                    material.getStatus(),
                    material.getUploadedBy(),
                    material.getUploadedByName(),
                    material.getUploadedAt(),
                    material.getFilePath(),
                    material.getDescription(),
                    material.getIsPrimary(),
                    material.getFormattedFileSize()
                ))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 附件字符串解析
     */
    private static List<String> parseAttachments(String attachmentsString) {
        if (attachmentsString == null || attachmentsString.trim().isEmpty()) {
            return List.of();
        }
        
        return Arrays.stream(attachmentsString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 材料响应DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AppealMaterialResponse {
        private Long id;
        private String fileName;
        private Long fileSize;
        private String fileType;
        private String mimeType;
        private String thumbnailPath;
        private MaterialStatus status;
        private Long uploadedBy;
        private String uploadedByName;
        private LocalDateTime uploadedAt;
        private String filePath;
        private String description;
        private Boolean isPrimary;
        private String formattedFileSize;
    }
}
