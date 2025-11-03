package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.SentimentType;
import jakarta.persistence.*;
import lombok.*;

/**
 * 评价情感分析实体
 *
 * Spec #7 NLP集成：通过情感词典对评价内容进行情感分析
 *
 * @author BaSui 😎 - 情感分析，判断用户是满意还是失望！
 * @since 2025-11-03
 */
@Entity
@Table(name = "t_review_sentiment", indexes = {
        @Index(name = "idx_review_sentiment_review", columnList = "review_id"),
        @Index(name = "idx_review_sentiment_type", columnList = "sentiment_type"),
        @Index(name = "idx_review_sentiment_score", columnList = "sentiment_score")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSentiment extends BaseEntity {

    /**
     * 评价ID（外键，一对一关系）
     * 每个评价只有一个情感分析结果
     */
    @Column(name = "review_id", nullable = false, unique = true)
    private Long reviewId;

    /**
     * 情感类型
     * POSITIVE=积极情感（好评），NEUTRAL=中性情感（中评），NEGATIVE=消极情感（差评）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment_type", nullable = false, length = 20)
    @Builder.Default
    private SentimentType sentimentType = SentimentType.NEUTRAL;

    /**
     * 情感得分（0.0~1.0）
     * 计算公式：positiveWordCount / (positiveWordCount + negativeWordCount)
     * 0.0~0.4=消极，0.4~0.6=中性，0.6~1.0=积极
     */
    @Column(name = "sentiment_score", nullable = false)
    @Builder.Default
    private Double sentimentScore = 0.5;

    /**
     * 积极词数量
     * 统计评价内容中的积极情感词（如"好"、"满意"、"推荐"等）
     */
    @Column(name = "positive_word_count", nullable = false)
    @Builder.Default
    private Integer positiveWordCount = 0;

    /**
     * 消极词数量
     * 统计评价内容中的消极情感词（如"差"、"失望"、"不满"等）
     */
    @Column(name = "negative_word_count", nullable = false)
    @Builder.Default
    private Integer negativeWordCount = 0;

    /**
     * 中性词数量
     * 统计评价内容中的中性词（既不积极也不消极）
     */
    @Column(name = "neutral_word_count", nullable = false)
    @Builder.Default
    private Integer neutralWordCount = 0;

    /**
     * 关联到Review实体（可选，用于ORM查询）
     * 使用@OneToOne懒加载，避免N+1问题
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", insertable = false, updatable = false)
    private Review review;
}
