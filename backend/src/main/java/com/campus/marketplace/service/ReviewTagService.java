package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.ReviewTag;
import com.campus.marketplace.common.enums.TagSource;

import java.util.List;

/**
 * 评价标签服务接口
 *
 * Spec #7 NLP集成：使用jieba分词提取关键词并生成标签
 *
 * @author BaSui 😎 - AI自动提取标签，还能手动添加！
 * @since 2025-11-03
 */
public interface ReviewTagService {

    /**
     * 从评价内容中提取标签（使用jieba分词）
     *
     * @param content 评价内容
     * @return 提取的标签列表（已去重、过滤停用词）
     */
    List<String> extractTags(String content);

    /**
     * 为评价保存标签
     *
     * @param reviewId 评价ID
     * @param tagNames 标签名称列表
     * @param source 标签来源（系统提取/用户输入）
     * @return 保存的标签实体列表
     */
    List<ReviewTag> saveTagsForReview(Long reviewId, List<String> tagNames, TagSource source);

    /**
     * 获取评价的所有标签
     *
     * @param reviewId 评价ID
     * @return 标签列表
     */
    List<ReviewTag> getTagsByReviewId(Long reviewId);

    /**
     * 删除评价的所有标签
     *
     * @param reviewId 评价ID
     */
    void deleteTagsByReviewId(Long reviewId);

    /**
     * 自动分析并保存评价标签（系统自动调用）
     *
     * @param reviewId 评价ID
     * @param content 评价内容
     * @return 生成的标签列表
     */
    List<ReviewTag> analyzeAndSaveTags(Long reviewId, String content);
}
