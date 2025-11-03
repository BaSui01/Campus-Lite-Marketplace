package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.ReviewTag;
import com.campus.marketplace.common.enums.TagSource;
import com.campus.marketplace.common.enums.TagType;
import com.campus.marketplace.repository.ReviewTagRepository;
import com.campus.marketplace.service.ReviewTagService;
import com.huaban.analysis.jieba.JiebaSegmenter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评价标签服务实现
 *
 * Spec #7 NLP集成：使用jieba分词提取关键词，自动分类标签类型
 *
 * @author BaSui 😎 - jieba分词+智能分类，标签自动提取不是梦！
 * @since 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewTagServiceImpl implements ReviewTagService {

    private final ReviewTagRepository reviewTagRepository;
    private final JiebaSegmenter jiebaSegmenter = new JiebaSegmenter();

    /**
     * 停用词列表（过滤无意义的词）
     */
    private static final Set<String> STOP_WORDS = Set.of(
            "的", "了", "是", "在", "有", "和", "就", "不", "人", "都", "一", "一个",
            "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看",
            "好", "自己", "这", "我", "那", "里", "就是", "还", "可以", "这个", "但是"
    );

    /**
     * 预设标签词典（用于分类标签类型）
     */
    private static final Map<TagType, Set<String>> TAG_DICTIONARY = Map.of(
            // 物品质量相关
            TagType.QUALITY, Set.of(
                    "质量好", "做工精细", "品质", "正品", "假货", "瑕疵", "破损", "新旧",
                    "材质", "耐用", "结实", "粗糙", "精致", "完好", "崭新", "陈旧"
            ),
            // 服务态度相关
            TagType.SERVICE, Set.of(
                    "服务好", "态度好", "热情", "耐心", "专业", "冷淡", "态度差", "不耐烦",
                    "礼貌", "客气", "负责", "推卸", "敷衍", "周到", "细心", "粗心"
            ),
            // 物流速度相关
            TagType.DELIVERY, Set.of(
                    "发货快", "物流快", "配送快", "及时", "迅速", "发货慢", "物流慢", "延迟",
                    "包装好", "包装差", "破损", "完整", "保护", "快递", "送达", "签收"
            ),
            // 性价比相关
            TagType.PRICE, Set.of(
                    "性价比高", "划算", "实惠", "便宜", "贵", "值", "超值", "物美价廉",
                    "价格合理", "价格高", "不值", "亏", "优惠", "折扣", "省钱", "浪费"
            )
    );

    @Override
    public List<String> extractTags(String content) {
        if (content == null || content.trim().isEmpty()) {
            return Collections.emptyList();
        }

        log.debug("开始使用jieba分词提取标签，内容长度：{}", content.length());

        // 使用jieba分词
        List<String> words = jiebaSegmenter.sentenceProcess(content);

        // 过滤并去重
        List<String> tags = words.stream()
                .filter(word -> word.length() >= 2) // 至少2个字
                .filter(word -> !STOP_WORDS.contains(word)) // 过滤停用词
                .distinct() // 去重
                .limit(10) // 最多保留10个标签
                .collect(Collectors.toList());

        log.debug("jieba分词提取标签完成，共{}个：{}", tags.size(), tags);
        return tags;
    }

    @Override
    @Transactional
    public List<ReviewTag> saveTagsForReview(Long reviewId, List<String> tagNames, TagSource source) {
        if (tagNames == null || tagNames.isEmpty()) {
            return Collections.emptyList();
        }

        log.info("为评价{}保存{}个标签，来源：{}", reviewId, tagNames.size(), source);

        List<ReviewTag> tags = new ArrayList<>();
        for (String tagName : tagNames) {
            TagType tagType = classifyTagType(tagName);
            Double weight = calculateTagWeight(tagName, tagType);

            ReviewTag tag = ReviewTag.builder()
                    .reviewId(reviewId)
                    .tagName(tagName)
                    .tagType(tagType)
                    .tagSource(source)
                    .weight(weight)
                    .build();

            tags.add(reviewTagRepository.save(tag));
        }

        return tags;
    }

    @Override
    public List<ReviewTag> getTagsByReviewId(Long reviewId) {
        return reviewTagRepository.findByReviewId(reviewId);
    }

    @Override
    @Transactional
    public void deleteTagsByReviewId(Long reviewId) {
        reviewTagRepository.deleteByReviewId(reviewId);
        log.info("删除评价{}的所有标签", reviewId);
    }

    @Override
    @Transactional
    public List<ReviewTag> analyzeAndSaveTags(Long reviewId, String content) {
        log.info("开始为评价{}自动分析并保存标签", reviewId);

        // 提取标签
        List<String> extractedTags = extractTags(content);

        // 保存标签（系统自动提取）
        return saveTagsForReview(reviewId, extractedTags, TagSource.SYSTEM);
    }

    /**
     * 分类标签类型
     *
     * 根据预设词典匹配标签所属类型
     *
     * @param tagName 标签名称
     * @return 标签类型
     */
    private TagType classifyTagType(String tagName) {
        for (Map.Entry<TagType, Set<String>> entry : TAG_DICTIONARY.entrySet()) {
            // 精确匹配
            if (entry.getValue().contains(tagName)) {
                return entry.getKey();
            }
            // 模糊匹配（标签包含词典中的词）
            for (String keyword : entry.getValue()) {
                if (tagName.contains(keyword) || keyword.contains(tagName)) {
                    return entry.getKey();
                }
            }
        }
        return TagType.OTHER;
    }

    /**
     * 计算标签权重
     *
     * 根据标签类型和词频计算权重（0.0~1.0）
     *
     * @param tagName 标签名称
     * @param tagType 标签类型
     * @return 权重值
     */
    private Double calculateTagWeight(String tagName, TagType tagType) {
        // 基础权重：根据标签类型
        double baseWeight = tagType == TagType.OTHER ? 0.5 : 0.8;

        // 长度权重：越长的标签权重越高（更具体）
        double lengthWeight = Math.min(tagName.length() / 10.0, 0.2);

        return Math.min(baseWeight + lengthWeight, 1.0);
    }
}
