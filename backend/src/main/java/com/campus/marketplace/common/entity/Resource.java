package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.ResourceType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

/**
 * 学习资源实体
 * 
 * @author BaSui 😎
 * @date 2025-11-11
 */
@Entity
@Table(name = "t_resource", indexes = {
    @Index(name = "idx_resource_type", columnList = "type"),
    @Index(name = "idx_resource_category", columnList = "category"),
    @Index(name = "idx_resource_uploader", columnList = "uploader_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted = false")
public class Resource extends BaseEntity {

    /**
     * 资源标题
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 资源描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 资源类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ResourceType type;

    /**
     * 资源分类（课程名称、学科等）
     */
    @Column(name = "category", length = 100)
    private String category;

    /**
     * 文件URL或外链
     */
    @Column(name = "file_url", length = 500)
    private String fileUrl;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 上传者ID
     */
    @Column(name = "uploader_id", nullable = false)
    private Long uploaderId;

    /**
     * 下载次数
     */
    @Column(name = "download_count")
    @Builder.Default
    private Integer downloadCount = 0;

    /**
     * 浏览次数
     */
    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    /**
     * 点赞数
     */
    @Column(name = "like_count")
    @Builder.Default
    private Integer likeCount = 0;

    /**
     * 标签（用逗号分隔）
     */
    @Column(name = "tags", length = 500)
    private String tags;
}
