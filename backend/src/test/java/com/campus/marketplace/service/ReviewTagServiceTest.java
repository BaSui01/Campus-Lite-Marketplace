package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.ReviewTag;
import com.campus.marketplace.common.enums.TagSource;
import com.campus.marketplace.common.enums.TagType;
import com.campus.marketplace.repository.ReviewTagRepository;
import com.campus.marketplace.service.impl.ReviewTagServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 评价标签服务测试
 *
 * @author BaSui 😎 - 测试jieba分词和标签提取功能！
 * @since 2025-11-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("评价标签服务测试")
class ReviewTagServiceTest {

    @Mock
    private ReviewTagRepository reviewTagRepository;

    @InjectMocks
    private ReviewTagServiceImpl reviewTagService;

    @Test
    @DisplayName("提取标签 - 正常内容")
    void extractTags_NormalContent() {
        String content = "商品质量很好，发货速度很快，服务态度也不错，值得推荐！";

        List<String> tags = reviewTagService.extractTags(content);

        assertThat(tags).isNotNull();
        assertThat(tags).isNotEmpty();
        assertThat(tags.size()).isLessThanOrEqualTo(10); // 最多10个标签
        assertThat(tags).allMatch(tag -> tag.length() >= 2); // 每个标签至少2个字
    }

    @Test
    @DisplayName("提取标签 - 空内容")
    void extractTags_EmptyContent() {
        String content = "";

        List<String> tags = reviewTagService.extractTags(content);

        assertThat(tags).isNotNull();
        assertThat(tags).isEmpty();
    }

    @Test
    @DisplayName("提取标签 - null内容")
    void extractTags_NullContent() {
        List<String> tags = reviewTagService.extractTags(null);

        assertThat(tags).isNotNull();
        assertThat(tags).isEmpty();
    }

    @Test
    @DisplayName("保存标签 - 系统提取")
    void saveTagsForReview_SystemSource() {
        Long reviewId = 1L;
        List<String> tagNames = Arrays.asList("质量好", "发货快", "服务好");

        when(reviewTagRepository.save(any(ReviewTag.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<ReviewTag> savedTags = reviewTagService.saveTagsForReview(
                reviewId, tagNames, TagSource.SYSTEM);

        assertThat(savedTags).hasSize(3);
        assertThat(savedTags).allMatch(tag -> tag.getReviewId().equals(reviewId));
        assertThat(savedTags).allMatch(tag -> tag.getTagSource() == TagSource.SYSTEM);
        assertThat(savedTags).extracting(ReviewTag::getTagName)
                .containsExactlyInAnyOrder("质量好", "发货快", "服务好");

        verify(reviewTagRepository, times(3)).save(any(ReviewTag.class));
    }

    @Test
    @DisplayName("保存标签 - 用户输入")
    void saveTagsForReview_UserInputSource() {
        Long reviewId = 2L;
        List<String> tagNames = Arrays.asList("物美价廉", "推荐购买");

        when(reviewTagRepository.save(any(ReviewTag.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<ReviewTag> savedTags = reviewTagService.saveTagsForReview(
                reviewId, tagNames, TagSource.USER_INPUT);

        assertThat(savedTags).hasSize(2);
        assertThat(savedTags).allMatch(tag -> tag.getTagSource() == TagSource.USER_INPUT);

        verify(reviewTagRepository, times(2)).save(any(ReviewTag.class));
    }

    @Test
    @DisplayName("保存标签 - 空列表")
    void saveTagsForReview_EmptyList() {
        Long reviewId = 3L;
        List<String> tagNames = List.of();

        List<ReviewTag> savedTags = reviewTagService.saveTagsForReview(
                reviewId, tagNames, TagSource.SYSTEM);

        assertThat(savedTags).isEmpty();
        verify(reviewTagRepository, never()).save(any(ReviewTag.class));
    }

    @Test
    @DisplayName("自动分析并保存标签 - 完整流程")
    void analyzeAndSaveTags_FullWorkflow() {
        Long reviewId = 4L;
        String content = "商品质量很好，物流也很快，推荐购买！";

        when(reviewTagRepository.save(any(ReviewTag.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<ReviewTag> tags = reviewTagService.analyzeAndSaveTags(reviewId, content);

        assertThat(tags).isNotNull();
        assertThat(tags).isNotEmpty();
        assertThat(tags).allMatch(tag -> tag.getReviewId().equals(reviewId));
        assertThat(tags).allMatch(tag -> tag.getTagSource() == TagSource.SYSTEM);
        assertThat(tags).allMatch(tag -> tag.getWeight() != null && tag.getWeight() > 0);

        verify(reviewTagRepository, atLeastOnce()).save(any(ReviewTag.class));
    }

    @Test
    @DisplayName("获取评价标签")
    void getTagsByReviewId() {
        Long reviewId = 5L;
        List<ReviewTag> mockTags = Arrays.asList(
                ReviewTag.builder().reviewId(reviewId).tagName("质量好").tagType(TagType.QUALITY).build(),
                ReviewTag.builder().reviewId(reviewId).tagName("发货快").tagType(TagType.DELIVERY).build()
        );

        when(reviewTagRepository.findByReviewId(reviewId)).thenReturn(mockTags);

        List<ReviewTag> tags = reviewTagService.getTagsByReviewId(reviewId);

        assertThat(tags).hasSize(2);
        verify(reviewTagRepository).findByReviewId(reviewId);
    }

    @Test
    @DisplayName("删除评价标签")
    void deleteTagsByReviewId() {
        Long reviewId = 6L;

        reviewTagService.deleteTagsByReviewId(reviewId);

        verify(reviewTagRepository).deleteByReviewId(reviewId);
    }

    @Test
    @DisplayName("标签分类测试 - 质量标签")
    void tagClassification_QualityTag() {
        Long reviewId = 7L;
        List<String> tagNames = List.of("质量好", "做工精细", "品质");

        when(reviewTagRepository.save(any(ReviewTag.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<ReviewTag> tags = reviewTagService.saveTagsForReview(
                reviewId, tagNames, TagSource.SYSTEM);

        // 验证至少有一个标签被分类为QUALITY类型
        assertThat(tags).anyMatch(tag -> tag.getTagType() == TagType.QUALITY);
    }

    @Test
    @DisplayName("标签分类测试 - 服务标签")
    void tagClassification_ServiceTag() {
        Long reviewId = 8L;
        List<String> tagNames = List.of("服务好", "态度好", "热情");

        when(reviewTagRepository.save(any(ReviewTag.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<ReviewTag> tags = reviewTagService.saveTagsForReview(
                reviewId, tagNames, TagSource.SYSTEM);

        // 验证至少有一个标签被分类为SERVICE类型
        assertThat(tags).anyMatch(tag -> tag.getTagType() == TagType.SERVICE);
    }

    @Test
    @DisplayName("标签分类测试 - 物流标签")
    void tagClassification_DeliveryTag() {
        Long reviewId = 9L;
        List<String> tagNames = List.of("发货快", "物流快", "配送快");

        when(reviewTagRepository.save(any(ReviewTag.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<ReviewTag> tags = reviewTagService.saveTagsForReview(
                reviewId, tagNames, TagSource.SYSTEM);

        // 验证至少有一个标签被分类为DELIVERY类型
        assertThat(tags).anyMatch(tag -> tag.getTagType() == TagType.DELIVERY);
    }
}
