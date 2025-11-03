package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.MediaType;
import jakarta.persistence.*;
import lombok.*;

/**
 * 评价媒体实体（图片/视频）
 *
 * Spec #7：支持晒单功能，最多10张图片+1个视频
 *
 * @author BaSui 😎 - 图文并茂，晒单更有说服力！
 * @since 2025-11-03
 */
@Entity
@Table(name = "t_review_media", indexes = {
        @Index(name = "idx_review_media_review", columnList = "review_id"),
        @Index(name = "idx_review_media_type", columnList = "media_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewMedia extends BaseEntity {

    /**
     * 评价ID（外键）
     */
    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    /**
     * 媒体类型（IMAGE图片/VIDEO视频）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    private MediaType mediaType;

    /**
     * 媒体URL
     * 支持OSS/本地存储路径
     */
    @Column(name = "media_url", nullable = false, length = 500)
    private String mediaUrl;

    /**
     * 缩略图URL（仅图片和视频封面）
     * 用于列表展示，提升加载速度
     */
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    /**
     * 文件大小（字节）
     * 用于统计和限制
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 排序顺序（1-10）
     * 用户上传时的顺序，前端按此排序展示
     */
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 1;

    /**
     * 文件原始名称
     * 用于下载时恢复原文件名
     */
    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    /**
     * 媒体宽度（像素，仅图片/视频）
     */
    @Column(name = "width")
    private Integer width;

    /**
     * 媒体高度（像素，仅图片/视频）
     */
    @Column(name = "height")
    private Integer height;

    /**
     * 视频时长（秒，仅视频）
     */
    @Column(name = "duration")
    private Integer duration;

    /**
     * 关联到Review实体（可选，用于ORM查询）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", insertable = false, updatable = false)
    private Review review;
}
