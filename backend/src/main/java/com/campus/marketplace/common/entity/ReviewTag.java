package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.TagSource;
import com.campus.marketplace.common.enums.TagType;
import jakarta.persistence.*;
import lombok.*;

/**
 * 评价标签实体
 *
 * Spec #7 NLP集成：通过jieba分词自动提取关键词，或用户手动输入标签
 *
 * @author BaSui 😎 - AI自动提取标签，让评价一目了然！
 * @since 2025-11-03
 */
@Entity
@Table(name = "t_review_tag", indexes = {
        @Index(name = "idx_review_tag_review", columnList = "review_id"),
        @Index(name = "idx_review_tag_type", columnList = "tag_type"),
        @Index(name = "idx_review_tag_source", columnList = "tag_source")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewTag extends BaseEntity {

    /**
     * 评价ID（外键）
     * 关联到t_review表
     */
    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    /**
     * 标签名称
     * 例如："质量好"、"发货快"、"服务态度差"等
     * 最长50字
     */
    @Column(name = "tag_name", nullable = false, length = 50)
    private String tagName;

    /**
     * 标签类型
     * 分类标签到五大维度：物品质量/服务态度/物流速度/性价比/其他
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tag_type", nullable = false, length = 20)
    @Builder.Default
    private TagType tagType = TagType.OTHER;

    /**
     * 标签来源
     * SYSTEM=通过NLP自动提取，USER_INPUT=用户手动输入
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tag_source", nullable = false, length = 20)
    @Builder.Default
    private TagSource tagSource = TagSource.SYSTEM;

    /**
     * 标签权重
     * 用于NLP算法计算标签重要性，范围0.0~1.0
     * 权重越高，标签越重要（在前端优先显示）
     */
    @Column(name = "weight", nullable = false)
    @Builder.Default
    private Double weight = 1.0;

    /**
     * 关联到Review实体（可选，用于ORM查询）
     * 使用@ManyToOne懒加载，避免N+1问题
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", insertable = false, updatable = false)
    private Review review;
}
