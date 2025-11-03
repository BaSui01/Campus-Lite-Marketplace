package com.campus.marketplace.common.dto;

import com.campus.marketplace.common.enums.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评价媒体响应DTO
 *
 * Spec #7：图文视频响应数据
 *
 * @author BaSui 😎 - 晒单必备，图文并茂！
 * @since 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewMediaDTO {

    /**
     * 媒体ID
     */
    private Long id;

    /**
     * 评价ID
     */
    private Long reviewId;

    /**
     * 媒体类型（IMAGE/VIDEO）
     */
    private MediaType mediaType;

    /**
     * 媒体URL
     */
    private String mediaUrl;

    /**
     * 缩略图URL
     */
    private String thumbnailUrl;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 原始文件名
     */
    private String originalFilename;

    /**
     * 图片宽度（px）
     */
    private Integer width;

    /**
     * 图片高度（px）
     */
    private Integer height;

    /**
     * 视频时长（秒）
     */
    private Integer duration;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 格式化后的文件大小（例如：2.5MB）
     */
    public String getFormattedFileSize() {
        if (fileSize == null) {
            return "0B";
        }
        if (fileSize < 1024) {
            return fileSize + "B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1fKB", fileSize / 1024.0);
        } else {
            return String.format("%.1fMB", fileSize / (1024.0 * 1024));
        }
    }
}
