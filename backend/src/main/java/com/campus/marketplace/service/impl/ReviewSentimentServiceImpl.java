package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.ReviewSentiment;
import com.campus.marketplace.common.enums.SentimentType;
import com.campus.marketplace.repository.ReviewSentimentRepository;
import com.campus.marketplace.service.ReviewSentimentService;
import com.huaban.analysis.jieba.JiebaSegmenter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 评价情感分析服务实现
 *
 * Spec #7 NLP集成：使用情感词典分析评价内容的情感倾向
 *
 * @author BaSui 😎 - 情感词典匹配，算出用户满意度！
 * @since 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewSentimentServiceImpl implements ReviewSentimentService {

    private final ReviewSentimentRepository reviewSentimentRepository;
    private final JiebaSegmenter jiebaSegmenter = new JiebaSegmenter();

    /**
     * 积极情感词词典
     */
    private static final Set<String> POSITIVE_WORDS = Set.of(
            "好", "好好", "不错", "满意", "喜欢", "推荐", "优秀", "棒", "赞", "完美", "惊喜",
            "值得", "超值", "实惠", "划算", "快", "及时", "迅速", "热情", "耐心",
            "专业", "精致", "精美", "漂亮", "美观", "舒适", "方便", "实用", "耐用",
            "新", "崭新", "完好", "完整", "齐全", "周到", "细心", "负责", "礼貌"
    );

    /**
     * 消极情感词词典
     */
    private static final Set<String> NEGATIVE_WORDS = Set.of(
            "差", "差差", "不好", "失望", "不满", "后悔", "垃圾", "烂", "坑", "假", "次",
            "贵", "坑爹", "骗人", "慢", "延迟", "迟", "冷淡", "态度差", "不耐烦",
            "敷衍", "粗心", "破损", "瑕疵", "假货", "陈旧", "脏", "臭", "难", "麻烦",
            "问题", "退货", "投诉", "举报", "骗", "黑", "坏", "劣质", "粗糙"
    );

    @Override
    @Transactional
    public ReviewSentiment analyzeAndSaveSentiment(Long reviewId, String content) {
        if (content == null || content.trim().isEmpty()) {
            log.warn("评价{}内容为空，无法进行情感分析", reviewId);
            return createDefaultSentiment(reviewId);
        }

        log.info("开始为评价{}进行情感分析", reviewId);

        // 使用jieba分词
        List<String> words = jiebaSegmenter.sentenceProcess(content);

        // 统计情感词
        int positiveCount = 0;
        int negativeCount = 0;
        int neutralCount = 0;

        for (String word : words) {
            if (POSITIVE_WORDS.contains(word)) {
                positiveCount++;
            } else if (NEGATIVE_WORDS.contains(word)) {
                negativeCount++;
            } else {
                neutralCount++;
            }
        }

        // 计算情感得分
        double sentimentScore = calculateSentimentScore(positiveCount, negativeCount);

        // 判断情感类型
        SentimentType sentimentType = classifySentimentType(sentimentScore);

        log.info("评价{}情感分析完成：积极词{}个，消极词{}个，中性词{}个，得分{}，类型{}",
                reviewId, positiveCount, negativeCount, neutralCount, sentimentScore, sentimentType);

        // 保存情感分析结果
        ReviewSentiment sentiment = ReviewSentiment.builder()
                .reviewId(reviewId)
                .sentimentType(sentimentType)
                .sentimentScore(sentimentScore)
                .positiveWordCount(positiveCount)
                .negativeWordCount(negativeCount)
                .neutralWordCount(neutralCount)
                .build();

        return reviewSentimentRepository.save(sentiment);
    }

    @Override
    public Optional<ReviewSentiment> getSentimentByReviewId(Long reviewId) {
        return reviewSentimentRepository.findByReviewId(reviewId);
    }

    @Override
    @Transactional
    public void deleteSentimentByReviewId(Long reviewId) {
        reviewSentimentRepository.deleteByReviewId(reviewId);
        log.info("删除评价{}的情感分析结果", reviewId);
    }

    @Override
    @Transactional
    public ReviewSentiment reanalyzeSentiment(Long reviewId, String content) {
        log.info("重新分析评价{}的情感倾向", reviewId);

        // 删除旧的情感分析
        deleteSentimentByReviewId(reviewId);

        // 重新分析并保存
        return analyzeAndSaveSentiment(reviewId, content);
    }

    /**
     * 计算情感得分（0.0~1.0）
     *
     * 公式：positiveCount / (positiveCount + negativeCount)
     * 特殊情况：如果都为0，返回0.5（中性）
     *
     * @param positiveCount 积极词数量
     * @param negativeCount 消极词数量
     * @return 情感得分
     */
    private double calculateSentimentScore(int positiveCount, int negativeCount) {
        int totalSentimentWords = positiveCount + negativeCount;

        // 特殊情况：没有情感词，返回中性得分
        if (totalSentimentWords == 0) {
            return 0.5;
        }

        // 计算得分
        double score = (double) positiveCount / totalSentimentWords;

        // 保留两位小数
        return Math.round(score * 100.0) / 100.0;
    }

    /**
     * 根据情感得分分类情感类型
     *
     * 分类规则：
     * - 0.0 ~ 0.4：消极（NEGATIVE）
     * - 0.4 ~ 0.6：中性（NEUTRAL）
     * - 0.6 ~ 1.0：积极（POSITIVE）
     *
     * @param sentimentScore 情感得分
     * @return 情感类型
     */
    private SentimentType classifySentimentType(double sentimentScore) {
        if (sentimentScore < 0.4) {
            return SentimentType.NEGATIVE;
        } else if (sentimentScore >= 0.6) {
            return SentimentType.POSITIVE;
        } else {
            return SentimentType.NEUTRAL;
        }
    }

    /**
     * 创建默认情感分析结果（用于内容为空的情况）
     *
     * @param reviewId 评价ID
     * @return 默认情感分析（中性）
     */
    private ReviewSentiment createDefaultSentiment(Long reviewId) {
        ReviewSentiment sentiment = ReviewSentiment.builder()
                .reviewId(reviewId)
                .sentimentType(SentimentType.NEUTRAL)
                .sentimentScore(0.5)
                .positiveWordCount(0)
                .negativeWordCount(0)
                .neutralWordCount(0)
                .build();

        return reviewSentimentRepository.save(sentiment);
    }
}
