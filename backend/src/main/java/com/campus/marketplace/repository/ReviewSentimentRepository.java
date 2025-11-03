package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.ReviewSentiment;
import com.campus.marketplace.common.enums.SentimentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 评价情感分析数据访问接口
 *
 * @author BaSui 😎 - 情感分析查询，看用户满意度！
 * @since 2025-11-03
 */
@Repository
public interface ReviewSentimentRepository extends JpaRepository<ReviewSentiment, Long> {

    /**
     * 根据评价ID查询情感分析结果
     *
     * @param reviewId 评价ID
     * @return 情感分析结果（Optional）
     */
    Optional<ReviewSentiment> findByReviewId(Long reviewId);

    /**
     * 检查评价是否已有情感分析
     *
     * @param reviewId 评价ID
     * @return 是否存在
     */
    boolean existsByReviewId(Long reviewId);

    /**
     * 根据情感类型查询所有评价情感分析
     *
     * @param sentimentType 情感类型
     * @return 情感分析列表
     */
    List<ReviewSentiment> findBySentimentType(SentimentType sentimentType);

    /**
     * 删除指定评价的情感分析
     *
     * @param reviewId 评价ID
     */
    void deleteByReviewId(Long reviewId);
}
