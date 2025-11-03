package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.Review;
import com.campus.marketplace.common.entity.ReviewReply;
import com.campus.marketplace.common.enums.ReplyType;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.repository.ReviewReplyRepository;
import com.campus.marketplace.repository.ReviewRepository;
import com.campus.marketplace.service.impl.ReviewReplyServiceImpl;
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
 * ReviewReplyService 单元测试
 *
 * Spec #7：回复功能单元测试
 *
 * @author BaSui 😎 - 测试回复功能，让沟通更顺畅！
 * @since 2025-11-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("评价回复服务测试")
class ReviewReplyServiceTest {

    @Mock
    private ReviewReplyRepository reviewReplyRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewReplyServiceImpl reviewReplyService;

    private Long reviewId;
    private Long sellerId;
    private Long buyerId;
    private Long adminId;
    private Review mockReview;

    @BeforeEach
    void setUp() {
        reviewId = 1L;
        sellerId = 100L;
        buyerId = 200L;
        adminId = 300L;

        mockReview = Review.builder()
                .orderId(10L)
                .sellerId(sellerId)
                .buyerId(buyerId)
                .rating(5)
                .content("商品很好")
                .build();
        mockReview.setId(reviewId);
    }

    @Test
    @DisplayName("卖家创建回复成功")
    void testCreateSellerReply_Success() {
        // Arrange
        String content = "感谢您的好评！";
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));
        when(reviewReplyRepository.save(any(ReviewReply.class)))
                .thenAnswer(invocation -> {
                    ReviewReply reply = invocation.getArgument(0);
                    reply.setId(1L);
                    return reply;
                });
        when(reviewReplyRepository.countByReviewId(reviewId)).thenReturn(1L);

        // Act
        ReviewReply result = reviewReplyService.createReply(
                reviewId, sellerId, ReplyType.SELLER_REPLY, content, buyerId
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getReviewId()).isEqualTo(reviewId);
        assertThat(result.getReplierId()).isEqualTo(sellerId);
        assertThat(result.getReplyType()).isEqualTo(ReplyType.SELLER_REPLY);
        assertThat(result.getContent()).isEqualTo(content);
        assertThat(result.getTargetUserId()).isEqualTo(buyerId);
        assertThat(result.getIsRead()).isFalse();

        verify(reviewReplyRepository, times(1)).save(any(ReviewReply.class));
        verify(reviewRepository, times(1)).save(mockReview); // 更新回复计数
    }

    @Test
    @DisplayName("管理员创建回复成功")
    void testCreateAdminReply_Success() {
        // Arrange
        String content = "此评价已核实";
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));
        when(reviewReplyRepository.save(any(ReviewReply.class)))
                .thenAnswer(invocation -> {
                    ReviewReply reply = invocation.getArgument(0);
                    reply.setId(2L);
                    return reply;
                });
        when(reviewReplyRepository.countByReviewId(reviewId)).thenReturn(1L);

        // Act
        ReviewReply result = reviewReplyService.createReply(
                reviewId, adminId, ReplyType.ADMIN_REPLY, content, buyerId
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getReplyType()).isEqualTo(ReplyType.ADMIN_REPLY);
        assertThat(result.getReplierId()).isEqualTo(adminId);

        verify(reviewReplyRepository, times(1)).save(any(ReviewReply.class));
    }

    @Test
    @DisplayName("非卖家创建卖家回复应抛出异常")
    void testCreateSellerReply_NotSeller_ShouldThrowException() {
        // Arrange
        Long fakeSellerId = 999L; // 不是真正的卖家
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));

        // Act & Assert
        assertThatThrownBy(() ->
                reviewReplyService.createReply(
                        reviewId, fakeSellerId, ReplyType.SELLER_REPLY, "假回复", buyerId
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("只有卖家本人可以回复此评价");

        verify(reviewReplyRepository, never()).save(any());
    }

    @Test
    @DisplayName("评价不存在时创建回复应抛出异常")
    void testCreateReply_ReviewNotFound_ShouldThrowException() {
        // Arrange
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                reviewReplyService.createReply(
                        999L, sellerId, ReplyType.SELLER_REPLY, "回复内容", buyerId
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("评价不存在");

        verify(reviewReplyRepository, never()).save(any());
    }

    @Test
    @DisplayName("空内容创建回复应抛出异常")
    void testCreateReply_EmptyContent_ShouldThrowException() {
        // Act & Assert
        assertThatThrownBy(() ->
                reviewReplyService.createReply(
                        reviewId, sellerId, ReplyType.SELLER_REPLY, "   ", buyerId
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("回复内容不能为空");

        verify(reviewReplyRepository, never()).save(any());
    }

    @Test
    @DisplayName("获取评价的所有回复")
    void testGetReviewReplies_Success() {
        // Arrange
        ReviewReply reply1 = createMockReply(1L, ReplyType.SELLER_REPLY, sellerId);
        ReviewReply reply2 = createMockReply(2L, ReplyType.ADMIN_REPLY, adminId);
        List<ReviewReply> mockReplies = List.of(reply1, reply2);

        when(reviewReplyRepository.findByReviewIdOrderByCreatedAtAsc(reviewId))
                .thenReturn(mockReplies);

        // Act
        List<ReviewReply> result = reviewReplyService.getReviewReplies(reviewId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getReplyType()).isEqualTo(ReplyType.SELLER_REPLY);
        assertThat(result.get(1).getReplyType()).isEqualTo(ReplyType.ADMIN_REPLY);

        verify(reviewReplyRepository, times(1)).findByReviewIdOrderByCreatedAtAsc(reviewId);
    }

    @Test
    @DisplayName("按类型获取评价回复")
    void testGetReviewRepliesByType_Success() {
        // Arrange
        ReviewReply reply = createMockReply(1L, ReplyType.SELLER_REPLY, sellerId);
        when(reviewReplyRepository.findByReviewIdAndReplyType(reviewId, ReplyType.SELLER_REPLY))
                .thenReturn(List.of(reply));

        // Act
        List<ReviewReply> result = reviewReplyService.getReviewRepliesByType(
                reviewId, ReplyType.SELLER_REPLY
        );

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReplyType()).isEqualTo(ReplyType.SELLER_REPLY);
    }

    @Test
    @DisplayName("获取用户未读回复")
    void testGetUnreadReplies_Success() {
        // Arrange
        ReviewReply reply1 = createMockReply(1L, ReplyType.SELLER_REPLY, sellerId);
        reply1.setIsRead(false);
        ReviewReply reply2 = createMockReply(2L, ReplyType.ADMIN_REPLY, adminId);
        reply2.setIsRead(false);

        when(reviewReplyRepository.findByTargetUserIdAndIsRead(buyerId, false))
                .thenReturn(List.of(reply1, reply2));

        // Act
        List<ReviewReply> result = reviewReplyService.getUnreadReplies(buyerId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(reply -> !reply.getIsRead());
    }

    @Test
    @DisplayName("统计用户未读回复数量")
    void testCountUnreadReplies_Success() {
        // Arrange
        when(reviewReplyRepository.countByTargetUserIdAndIsRead(buyerId, false))
                .thenReturn(3L);

        // Act
        long count = reviewReplyService.countUnreadReplies(buyerId);

        // Assert
        assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("标记回复为已读")
    void testMarkReplyAsRead_Success() {
        // Arrange
        ReviewReply reply = createMockReply(1L, ReplyType.SELLER_REPLY, sellerId);
        reply.setIsRead(false);

        when(reviewReplyRepository.findById(1L)).thenReturn(Optional.of(reply));

        // Act
        reviewReplyService.markReplyAsRead(1L);

        // Assert
        assertThat(reply.getIsRead()).isTrue();
        verify(reviewReplyRepository, times(1)).save(reply);
    }

    @Test
    @DisplayName("批量标记用户所有回复为已读")
    void testMarkAllRepliesAsRead_Success() {
        // Arrange
        ReviewReply reply1 = createMockReply(1L, ReplyType.SELLER_REPLY, sellerId);
        reply1.setIsRead(false);
        ReviewReply reply2 = createMockReply(2L, ReplyType.ADMIN_REPLY, adminId);
        reply2.setIsRead(false);

        when(reviewReplyRepository.findByTargetUserIdAndIsRead(buyerId, false))
                .thenReturn(List.of(reply1, reply2));

        // Act
        reviewReplyService.markAllRepliesAsRead(buyerId);

        // Assert
        assertThat(reply1.getIsRead()).isTrue();
        assertThat(reply2.getIsRead()).isTrue();
        verify(reviewReplyRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("删除回复成功")
    void testDeleteReply_Success() {
        // Arrange
        ReviewReply reply = createMockReply(1L, ReplyType.SELLER_REPLY, sellerId);
        when(reviewReplyRepository.findById(1L)).thenReturn(Optional.of(reply));
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));
        when(reviewReplyRepository.countByReviewId(reviewId)).thenReturn(0L);

        // Act
        reviewReplyService.deleteReply(1L);

        // Assert
        verify(reviewReplyRepository, times(1)).delete(reply);
        verify(reviewRepository, times(1)).save(mockReview);
    }

    @Test
    @DisplayName("检查评价是否有指定类型回复")
    void testHasReply_Success() {
        // Arrange
        when(reviewReplyRepository.existsByReviewIdAndReplyType(reviewId, ReplyType.SELLER_REPLY))
                .thenReturn(true);

        // Act
        boolean result = reviewReplyService.hasReply(reviewId, ReplyType.SELLER_REPLY);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("统计评价的回复数量")
    void testCountReviewReplies_Success() {
        // Arrange
        when(reviewReplyRepository.countByReviewId(reviewId)).thenReturn(2L);

        // Act
        long count = reviewReplyService.countReviewReplies(reviewId);

        // Assert
        assertThat(count).isEqualTo(2L);
    }

    /**
     * 创建模拟的ReviewReply对象
     */
    private ReviewReply createMockReply(Long id, ReplyType replyType, Long replierId) {
        ReviewReply reply = ReviewReply.builder()
                .reviewId(reviewId)
                .replierId(replierId)
                .replyType(replyType)
                .content("回复内容")
                .targetUserId(buyerId)
                .isRead(false)
                .build();
        reply.setId(id);
        return reply;
    }
}
