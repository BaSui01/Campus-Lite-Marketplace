package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.MaterialStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 申诉材料实体 - 支持文件附件的存储和管理
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Entity
@Table(name = "t_appeal_material", indexes = {
        @Index(name = "idx_appeal_material_appeal_id", columnList = "appeal_id"),
        @Index(name = "idx_appeal_material_file_type", columnList = "file_type"),
        @Index(name = "idx_appeal_material_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppealMaterial extends BaseEntity {

    /**
     * 申诉ID
     */
    @Column(name = "appeal_id", nullable = false)
    private String appealId;

    /**
     * 原始文件名
     */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /**
     * 文件存储路径
     */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * 文件类型 (image/pdf/document等)
     */
    @Column(name = "file_type", nullable = false, length = 50)
    private String fileType;

    /**
     * MIME类型
     */
    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    /**
     * 缩略图路径
     */
    @Column(name = "thumbnail_path", length = 500)
    private String thumbnailPath;

    /**
     * 文件状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private MaterialStatus status = MaterialStatus.UPLOADED;

    /**
     * 上传用户ID
     */
    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;

    /**
     * 上传用户名
     */
    @Column(name = "uploaded_by_name", nullable = false, length = 50)
    private String uploadedByName;

    /**
     * 上传时间
     */
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    /**
     * 文件描述或说明
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 是否为主文件
     */
    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = false;

    /**
     * 文件哈希值（用于去重）
     */
    @Column(name = "file_hash", length = 64)
    private String fileHash;

    /**
     * 病毒扫描结果
     */
    @Column(name = "virus_scan_result", length = 20)
    @Builder.Default
    private String virusScanResult = "PENDING";

    /**
     * 检查用户是否有权限上传材料
     */
    public boolean canBeUploadedBy(Long userId) {
        return userId != null && uploadedBy.equals(userId);
    }

    /**
     * 检查文件是否可以安全下载
     */
    public boolean canBeDownloaded() {
        return status == MaterialStatus.UPLOADED && 
               "CLEAN".equals(virusScanResult);
    }

    /**
     * 检查是否为图片文件
     */
    public boolean isImageFile() {
        String lowerFileType = fileType.toLowerCase();
        return lowerFileType.startsWith("image/") || 
               lowerFileType.contains("jpg") || 
               lowerFileType.contains("jpeg") || 
               lowerFileType.contains("png") || 
               lowerFileType.contains("gif") || 
               lowerFileType.contains("webp");
    }

    /**
     * 检查是否为文档文件
     */
    public boolean isDocumentFile() {
        String lowerFileType = fileType.toLowerCase();
        return lowerFileType.contains("pdf") || 
               lowerFileType.contains("doc") || 
               lowerFileType.contains("docx") || 
               lowerFileType.contains("txt") || 
               lowerFileType.contains("xls") || 
               lowerFileType.contains("xlsx");
    }

    /**
     * 获取用户友好的文件大小显示
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "未知大小";
        
        long size = fileSize;
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
        return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
    }

    /**
     * 上传时自动设置上传时间和状态
     */
    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            this.uploadedAt = LocalDateTime.now();
        }
    }
}
