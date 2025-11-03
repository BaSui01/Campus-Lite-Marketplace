package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.Review;
import com.campus.marketplace.common.entity.ReviewLike;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.repository.ReviewLikeRepository;
import com.campus.marketplace.repository.ReviewRepository;
import com.campus.marketplace.service.impl.ReviewLikeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ReviewLikeService 单元测试
 *
 * Spec #7：点赞功能单元测试
 *
 * @author BaSui 😎 - 测试点赞功能，让互动更有爱！
 * @since 2025-11-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("评价点赞服务测试")
class ReviewLikeServiceTest {

    @Mock
    private ReviewLikeRepository reviewLikeRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewLikeServiceImpl reviewLikeService;

    private Long reviewId;
    private Long userId;
    private Review mockReview;

    @BeforeEach
    void setUp() {
        reviewId = 1L;
        userId = 100L;

        mockReview = Review.builder()
                .orderId(10L)
                .sellerId(200L)
                .buyerId(userId)
                .rating(5)
                .content("商品很好")
                .build();
        mockReview.setId(reviewId);
    }

    @Test
    @DisplayName("首次点赞评价成功")
    void testLikeReview_FirstTime_Success() {
        // Arrange
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));
        when(reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId))
                .thenReturn(Optional.empty()); // 没有点赞记录
        when(reviewLikeRepository.save(any(ReviewLike.class)))
                .thenAnswer(invocation -> {
                    ReviewLike like = invocation.getArgument(0);
                    like.setId(1L);
                    return like;
                });
        when(reviewLikeRepository.countByReviewIdAndIsActive(reviewId, true)).thenReturn(1L);

        // Act
        ReviewLike result = reviewLikeService.likeReview(reviewId, userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getReviewId()).isEqualTo(reviewId);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getIsActive()).isTrue();

        verify(reviewLikeRepository, times(1)).save(any(ReviewLike.class));
        verify(reviewRepository, times(1)).save(mockReview);
    }

    @Test
    @DisplayName("恢复点赞（软删除恢复）")
    void testLikeReview_RestoreDeleted_Success() {
        // Arrange
        ReviewLike existingLike = createMockLike(1L, false); // 之前取消过点赞
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));
        when(reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId))
                .thenReturn(Optional.of(existingLike));
        when(reviewLikeRepository.countByReviewIdAndIsActive(reviewId, true)).thenReturn(1L);

        // Act
        ReviewLike result = reviewLikeService.likeReview(reviewId, userId);

        // Assert
        assertThat(result.getIsActive()).isTrue();
        verify(reviewLikeRepository, times(1)).save(existingLike);
    }

    @Test
    @DisplayName("重复点赞不创建新记录")
    void testLikeReview_AlreadyLiked_NoNewRecord() {
        // Arrange
        ReviewLike existingLike = createMockLike(1L, true); // 已经点赞
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));
        when(reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId))
                .thenReturn(Optional.of(existingLike));

        // Act
        ReviewLike result = reviewLikeService.likeReview(reviewId, userId);

        // Assert
        assertThat(result).isSameAs(existingLike);
        verify(reviewLikeRepository, never()).save(any()); // 不应保存
    }

    @Test
    @DisplayName("评价不存在时点赞应抛出异常")
    void testLikeReview_ReviewNotFound_ShouldThrowException() {
        // Arrange
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reviewLikeService.likeReview(999L, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("评价不存在");

        verify(reviewLikeRepository, never()).save(any());
    }

    @Test
    @DisplayName("取消点赞成功（软删除）")
    void testUnlikeReview_Success() {
        // Arrange
        ReviewLike existingLike = createMockLike(1L, true);
        when(reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId))
                .thenReturn(Optional.of(existingLike));
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));
        when(reviewLikeRepository.countByReviewIdAndIsActive(reviewId, true)).thenReturn(0L);

        // Act
        reviewLikeService.unlikeReview(reviewId, userId);

        // Assert
        assertThat(existingLike.getIsActive()).isFalse();
        verify(reviewLikeRepository, times(1)).save(existingLike);
        verify(reviewRepository, times(1)).save(mockReview);
    }

    @Test
    @DisplayName("未点赞时取消点赞应抛出异常")
    void testUnlikeReview_NotLiked_ShouldThrowException() {
        // Arrange
        when(reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reviewLikeService.unlikeReview(reviewId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未找到点赞记录");

        verify(reviewLikeRepository, never()).save(any());
    }

    @Test
    @DisplayName("切换点赞状态 - 从未点赞到点赞")
    void testToggleLike_FromUnlikedToLiked() {
        // Arrange
        when(reviewLikeRepository.existsByReviewIdAndUserIdAndIsActive(reviewId, userId, true))
                .thenReturn(false); // 当前未点赞
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));
        when(reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId))
                .thenReturn(Optional.empty());
        when(reviewLikeRepository.save(any(ReviewLike.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(reviewLikeRepository.countByReviewIdAndIsActive(reviewId, true)).thenReturn(1L);

        // Act
        boolean result = reviewLikeService.toggleLike(reviewId, userId);

        // Assert
        assertThat(result).isTrue(); // 点赞成功
        verify(reviewLikeRepository, times(1)).save(any(ReviewLike.class));
    }

    @Test
    @DisplayName("切换点赞状态 - 从点赞到取消点赞")
    void testToggleLike_FromLikedToUnliked() {
        // Arrange
        ReviewLike existingLike = createMockLike(1L, true);
        when(reviewLikeRepository.existsByReviewIdAndUserIdAndIsActive(reviewId, userId, true))
                .thenReturn(true); // 当前已点赞
        when(reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId))
                .thenReturn(Optional.of(existingLike));
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));
        when(reviewLikeRepository.countByReviewIdAndIsActive(reviewId, true)).thenReturn(0L);

        // Act
        boolean result = reviewLikeService.toggleLike(reviewId, userId);

        // Assert
        assertThat(result).isFalse(); // 取消点赞
        assertThat(existingLike.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("检查是否点赞")
    void testHasLiked_Success() {
        // Arrange
        when(reviewLikeRepository.existsByReviewIdAndUserIdAndIsActive(reviewId, userId, true))
                .thenReturn(true);

        // Act
        boolean result = reviewLikeService.hasLiked(reviewId, userId);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("获取评价的所有有效点赞")
    void testGetReviewLikes_Success() {
        // Arrange
        ReviewLike like1 = createMockLike(1L, true);
        ReviewLike like2 = createMockLike(2L, true);
        when(reviewLikeRepository.findByReviewIdAndIsActive(reviewId, true))
                .thenReturn(List.of(like1, like2));

        // Act
        List<ReviewLike> result = reviewLikeService.getReviewLikes(reviewId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(ReviewLike::getIsActive);
    }

    @Test
    @DisplayName("获取用户点赞过的评价列表")
    void testGetUserLikes_Success() {
        // Arrange
        ReviewLike like1 = createMockLike(1L, true);
        ReviewLike like2 = createMockLike(2L, true);
        when(reviewLikeRepository.findByUserIdAndIsActive(userId, true))
                .thenReturn(List.of(like1, like2));

        // Act
        List<ReviewLike> result = reviewLikeService.getUserLikes(userId);

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("统计评价的有效点赞数量")
    void testCountReviewLikes_Success() {
        // Arrange
        when(reviewLikeRepository.countByReviewIdAndIsActive(reviewId, true))
                .thenReturn(5L);

        // Act
        long count = reviewLikeService.countReviewLikes(reviewId);

        // Assert
        assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("删除评价的所有点赞")
    void testDeleteAllLikesByReviewId_Success() {
        // Arrange
        when(reviewLikeRepository.countByReviewId(reviewId)).thenReturn(3L);
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));
        when(reviewLikeRepository.countByReviewIdAndIsActive(reviewId, true)).thenReturn(0L);

        // Act
        reviewLikeService.deleteAllLikesByReviewId(reviewId);

        // Assert
        verify(reviewLikeRepository, times(1)).deleteByReviewId(reviewId);
        verify(reviewRepository, times(1)).save(mockReview);
    }

    @Test
    @DisplayName("参数校验 - reviewId为空")
    void testValidation_NullReviewId_ShouldThrowException() {
        // Act & Assert
        assertThatThrownBy(() -> reviewLikeService.likeReview(null, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("评价ID不能为空");
    }

    @Test
    @DisplayName("参数校验 - userId为空")
    void testValidation_NullUserId_ShouldThrowException() {
        // Act & Assert
        assertThatThrownBy(() -> reviewLikeService.likeReview(reviewId, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户ID不能为空");
    }

    /**
     * 创建模拟的ReviewLike对象
     */
    private ReviewLike createMockLike(Long id, boolean isActive) {
        ReviewLike like = ReviewLike.builder()
                .reviewId(reviewId)
                .userId(userId)
                .isActive(isActive)
                .build();
        like.setId(id);
        return like;
    }
}
