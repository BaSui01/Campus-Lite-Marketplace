package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.ReviewSentiment;
import com.campus.marketplace.common.enums.SentimentType;
import com.campus.marketplace.repository.ReviewSentimentRepository;
import com.campus.marketplace.service.impl.ReviewSentimentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 评价情感分析服务测试
 *
 * @author BaSui 😎 - 测试情感分析算法的准确性！
 * @since 2025-11-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("评价情感分析服务测试")
class ReviewSentimentServiceTest {

    @Mock
    private ReviewSentimentRepository reviewSentimentRepository;

    @InjectMocks
    private ReviewSentimentServiceImpl reviewSentimentService;

    @Test
    @DisplayName("情感分析 - 积极评价")
    void analyzeSentiment_PositiveReview() {
        Long reviewId = 1L;
        String content = "商品质量很好，服务态度也不错，发货很快，非常满意，值得推荐！";

        when(reviewSentimentRepository.save(any(ReviewSentiment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReviewSentiment sentiment = reviewSentimentService.analyzeAndSaveSentiment(reviewId, content);

        assertThat(sentiment).isNotNull();
        assertThat(sentiment.getReviewId()).isEqualTo(reviewId);
        assertThat(sentiment.getSentimentType()).isEqualTo(SentimentType.POSITIVE);
        assertThat(sentiment.getSentimentScore()).isGreaterThanOrEqualTo(0.6);
        assertThat(sentiment.getPositiveWordCount()).isGreaterThan(0);

        verify(reviewSentimentRepository).save(any(ReviewSentiment.class));
    }

    @Test
    @DisplayName("情感分析 - 消极评价")
    void analyzeSentiment_NegativeReview() {
        Long reviewId = 2L;
        String content = "商品质量很差，服务态度不好，发货慢，非常失望，不推荐购买！";

        when(reviewSentimentRepository.save(any(ReviewSentiment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReviewSentiment sentiment = reviewSentimentService.analyzeAndSaveSentiment(reviewId, content);

        assertThat(sentiment).isNotNull();
        assertThat(sentiment.getSentimentType()).isEqualTo(SentimentType.NEGATIVE);
        assertThat(sentiment.getSentimentScore()).isLessThan(0.4);
        assertThat(sentiment.getNegativeWordCount()).isGreaterThan(0);

        verify(reviewSentimentRepository).save(any(ReviewSentiment.class));
    }

    @Test
    @DisplayName("情感分析 - 中性评价")
    void analyzeSentiment_NeutralReview() {
        Long reviewId = 3L;
        String content = "商品已收到，质量一般，价格也一般。";

        when(reviewSentimentRepository.save(any(ReviewSentiment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReviewSentiment sentiment = reviewSentimentService.analyzeAndSaveSentiment(reviewId, content);

        assertThat(sentiment).isNotNull();
        assertThat(sentiment.getSentimentType()).isEqualTo(SentimentType.NEUTRAL);
        assertThat(sentiment.getSentimentScore()).isBetween(0.4, 0.6);

        verify(reviewSentimentRepository).save(any(ReviewSentiment.class));
    }

    @Test
    @DisplayName("情感分析 - 空内容返回中性")
    void analyzeSentiment_EmptyContent_ReturnsNeutral() {
        Long reviewId = 4L;
        String content = "";

        when(reviewSentimentRepository.save(any(ReviewSentiment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReviewSentiment sentiment = reviewSentimentService.analyzeAndSaveSentiment(reviewId, content);

        assertThat(sentiment).isNotNull();
        assertThat(sentiment.getSentimentType()).isEqualTo(SentimentType.NEUTRAL);
        assertThat(sentiment.getSentimentScore()).isEqualTo(0.5);
        assertThat(sentiment.getPositiveWordCount()).isZero();
        assertThat(sentiment.getNegativeWordCount()).isZero();

        verify(reviewSentimentRepository).save(any(ReviewSentiment.class));
    }

    @Test
    @DisplayName("情感分析 - null内容返回中性")
    void analyzeSentiment_NullContent_ReturnsNeutral() {
        Long reviewId = 5L;

        when(reviewSentimentRepository.save(any(ReviewSentiment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReviewSentiment sentiment = reviewSentimentService.analyzeAndSaveSentiment(reviewId, null);

        assertThat(sentiment).isNotNull();
        assertThat(sentiment.getSentimentType()).isEqualTo(SentimentType.NEUTRAL);
        assertThat(sentiment.getSentimentScore()).isEqualTo(0.5);

        verify(reviewSentimentRepository).save(any(ReviewSentiment.class));
    }

    @Test
    @DisplayName("情感分析 - 混合情感（积极词多于消极词）")
    void analyzeSentiment_MixedPositive() {
        Long reviewId = 6L;
        String content = "商品质量好，价格实惠，推荐购买，但是发货有点慢。";

        when(reviewSentimentRepository.save(any(ReviewSentiment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReviewSentiment sentiment = reviewSentimentService.analyzeAndSaveSentiment(reviewId, content);

        assertThat(sentiment).isNotNull();
        assertThat(sentiment.getPositiveWordCount()).isGreaterThan(sentiment.getNegativeWordCount());
        assertThat(sentiment.getSentimentScore()).isGreaterThan(0.5);

        verify(reviewSentimentRepository).save(any(ReviewSentiment.class));
    }

    @Test
    @DisplayName("情感分析 - 混合情感（消极词多于积极词）")
    void analyzeSentiment_MixedNegative() {
        Long reviewId = 7L;
        String content = "发货慢，包装差，质量不好，唯一的优点是价格便宜。";

        when(reviewSentimentRepository.save(any(ReviewSentiment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReviewSentiment sentiment = reviewSentimentService.analyzeAndSaveSentiment(reviewId, content);

        assertThat(sentiment).isNotNull();
        assertThat(sentiment.getNegativeWordCount()).isGreaterThan(sentiment.getPositiveWordCount());
        assertThat(sentiment.getSentimentScore()).isLessThan(0.5);

        verify(reviewSentimentRepository).save(any(ReviewSentiment.class));
    }

    @Test
    @DisplayName("获取情感分析结果")
    void getSentimentByReviewId() {
        Long reviewId = 8L;
        ReviewSentiment mockSentiment = ReviewSentiment.builder()
                .reviewId(reviewId)
                .sentimentType(SentimentType.POSITIVE)
                .sentimentScore(0.75)
                .build();

        when(reviewSentimentRepository.findByReviewId(reviewId))
                .thenReturn(Optional.of(mockSentiment));

        Optional<ReviewSentiment> sentiment = reviewSentimentService.getSentimentByReviewId(reviewId);

        assertThat(sentiment).isPresent();
        assertThat(sentiment.get().getReviewId()).isEqualTo(reviewId);
        assertThat(sentiment.get().getSentimentType()).isEqualTo(SentimentType.POSITIVE);

        verify(reviewSentimentRepository).findByReviewId(reviewId);
    }

    @Test
    @DisplayName("删除情感分析结果")
    void deleteSentimentByReviewId() {
        Long reviewId = 9L;

        reviewSentimentService.deleteSentimentByReviewId(reviewId);

        verify(reviewSentimentRepository).deleteByReviewId(reviewId);
    }

    @Test
    @DisplayName("重新分析情感")
    void reanalyzeSentiment() {
        Long reviewId = 10L;
        String content = "质量很好，非常满意！";

        when(reviewSentimentRepository.save(any(ReviewSentiment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReviewSentiment sentiment = reviewSentimentService.reanalyzeSentiment(reviewId, content);

        assertThat(sentiment).isNotNull();
        assertThat(sentiment.getSentimentType()).isEqualTo(SentimentType.POSITIVE);

        verify(reviewSentimentRepository).deleteByReviewId(reviewId);
        verify(reviewSentimentRepository).save(any(ReviewSentiment.class));
    }

    @Test
    @DisplayName("情感得分计算 - 全积极")
    void sentimentScore_AllPositive() {
        Long reviewId = 11L;
        String content = "好好好好好好";

        when(reviewSentimentRepository.save(any(ReviewSentiment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReviewSentiment sentiment = reviewSentimentService.analyzeAndSaveSentiment(reviewId, content);

        assertThat(sentiment.getSentimentScore()).isEqualTo(1.0);
        assertThat(sentiment.getSentimentType()).isEqualTo(SentimentType.POSITIVE);

        verify(reviewSentimentRepository).save(any(ReviewSentiment.class));
    }

    @Test
    @DisplayName("情感得分计算 - 全消极")
    void sentimentScore_AllNegative() {
        Long reviewId = 12L;
        String content = "差差差差差差";

        when(reviewSentimentRepository.save(any(ReviewSentiment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReviewSentiment sentiment = reviewSentimentService.analyzeAndSaveSentiment(reviewId, content);

        assertThat(sentiment.getSentimentScore()).isEqualTo(0.0);
        assertThat(sentiment.getSentimentType()).isEqualTo(SentimentType.NEGATIVE);

        verify(reviewSentimentRepository).save(any(ReviewSentiment.class));
    }
}
