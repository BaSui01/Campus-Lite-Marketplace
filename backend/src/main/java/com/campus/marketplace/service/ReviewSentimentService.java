package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.ReviewSentiment;

import java.util.Optional;

/**
 * 评价情感分析服务接口
 *
 * Spec #7 NLP集成：使用情感词典分析评价的情感倾向
 *
 * @author BaSui 😎 - 情感分析，判断用户是满意还是失望！
 * @since 2025-11-03
 */
public interface ReviewSentimentService {

    /**
     * 分析评价内容的情感倾向
     *
     * @param reviewId 评价ID
     * @param content 评价内容
     * @return 情感分析结果
     */
    ReviewSentiment analyzeAndSaveSentiment(Long reviewId, String content);

    /**
     * 获取评价的情感分析结果
     *
     * @param reviewId 评价ID
     * @return 情感分析结果（Optional）
     */
    Optional<ReviewSentiment> getSentimentByReviewId(Long reviewId);

    /**
     * 删除评价的情感分析
     *
     * @param reviewId 评价ID
     */
    void deleteSentimentByReviewId(Long reviewId);

    /**
     * 重新分析评价的情感倾向
     *
     * @param reviewId 评价ID
     * @param content 评价内容
     * @return 更新后的情感分析结果
     */
    ReviewSentiment reanalyzeSentiment(Long reviewId, String content);
}
