package com.campus.marketplace.integration;

import com.campus.marketplace.common.entity.Review;
import com.campus.marketplace.common.entity.ReviewLike;
import com.campus.marketplace.repository.ReviewLikeRepository;
import com.campus.marketplace.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ReviewLike 集成测试
 *
 * Spec #7：点赞功能集成测试
 *
 * @author BaSui 😎 - 测试点赞、取消、切换完整流程！
 * @since 2025-11-03
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("评价点赞集成测试")
class ReviewLikeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewLikeRepository reviewLikeRepository;

    private Review testReview;
    private Long userId = 100L;

    @BeforeEach
    void setUp() {
        // 创建测试评价
        testReview = Review.builder()
                .orderId(1L)
                .buyerId(userId)
                .sellerId(200L)
                .rating(5)
                .content("测试评价")
                .build();
        testReview = reviewRepository.save(testReview);
    }

    @Test
    @DisplayName("完整流程：点赞 → 查询状态 → 取消点赞")
    void testCompleteLikeFlow() throws Exception {
        // 1. 点赞评价
        mockMvc.perform(post("/api/reviews/{reviewId}/like", testReview.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证数据库
        assertThat(reviewLikeRepository.countByReviewIdAndIsActive(testReview.getId(), true)).isEqualTo(1);

        // 2. 查询点赞状态
        mockMvc.perform(get("/api/reviews/{reviewId}/like/status", testReview.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));

        // 3. 查询点赞数量
        mockMvc.perform(get("/api/reviews/{reviewId}/likes/count", testReview.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(1));

        // 4. 取消点赞
        mockMvc.perform(delete("/api/reviews/{reviewId}/like", testReview.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证点赞数量变为0
        assertThat(reviewLikeRepository.countByReviewIdAndIsActive(testReview.getId(), true)).isEqualTo(0);

        // 5. 再次查询点赞状态应为false
        mockMvc.perform(get("/api/reviews/{reviewId}/like/status", testReview.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("切换点赞状态测试")
    void testToggleLike() throws Exception {
        // 第一次切换：未点赞 → 点赞
        mockMvc.perform(post("/api/reviews/{reviewId}/like/toggle", testReview.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));

        // 验证已点赞
        assertThat(reviewLikeRepository.existsByReviewIdAndUserIdAndIsActive(
                testReview.getId(), userId, true)).isTrue();

        // 第二次切换：点赞 → 取消点赞
        mockMvc.perform(post("/api/reviews/{reviewId}/like/toggle", testReview.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));

        // 验证已取消
        assertThat(reviewLikeRepository.existsByReviewIdAndUserIdAndIsActive(
                testReview.getId(), userId, true)).isFalse();

        // 第三次切换：取消点赞 → 点赞
        mockMvc.perform(post("/api/reviews/{reviewId}/like/toggle", testReview.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("重复点赞测试（软删除恢复）")
    void testRepeatedLike() throws Exception {
        // 第一次点赞
        mockMvc.perform(post("/api/reviews/{reviewId}/like", testReview.getId()))
                .andExpect(status().isOk());

        Long firstCount = reviewLikeRepository.countByReviewId(testReview.getId());
        assertThat(firstCount).isEqualTo(1);

        // 取消点赞
        mockMvc.perform(delete("/api/reviews/{reviewId}/like", testReview.getId()))
                .andExpect(status().isOk());

        // 再次点赞（应恢复之前的点赞记录）
        mockMvc.perform(post("/api/reviews/{reviewId}/like", testReview.getId()))
                .andExpect(status().isOk());

        Long secondCount = reviewLikeRepository.countByReviewId(testReview.getId());
        // 记录数量仍然为1（软删除模式）
        assertThat(secondCount).isEqualTo(1);

        // 但有效点赞数为1
        assertThat(reviewLikeRepository.countByReviewIdAndIsActive(testReview.getId(), true)).isEqualTo(1);
    }

    @Test
    @DisplayName("多用户点赞测试")
    void testMultipleUsersLike() throws Exception {
        // 创建多个用户的点赞
        for (long i = 1; i <= 5; i++) {
            ReviewLike like = ReviewLike.builder()
                    .reviewId(testReview.getId())
                    .userId(100L + i)
                    .isActive(true)
                    .build();
            reviewLikeRepository.save(like);
        }

        // 查询点赞数量
        mockMvc.perform(get("/api/reviews/{reviewId}/likes/count", testReview.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    @DisplayName("点赞后Review实体的likeCount应更新")
    void testReviewLikeCountUpdate() throws Exception {
        // 点赞前likeCount为0
        Review review = reviewRepository.findById(testReview.getId()).orElseThrow();
        assertThat(review.getLikeCount()).isEqualTo(0);

        // 点赞
        mockMvc.perform(post("/api/reviews/{reviewId}/like", testReview.getId()))
                .andExpect(status().isOk());

        // 点赞后likeCount应为1
        review = reviewRepository.findById(testReview.getId()).orElseThrow();
        assertThat(review.getLikeCount()).isEqualTo(1);

        // 取消点赞
        mockMvc.perform(delete("/api/reviews/{reviewId}/like", testReview.getId()))
                .andExpect(status().isOk());

        // 取消后likeCount应为0
        review = reviewRepository.findById(testReview.getId()).orElseThrow();
        assertThat(review.getLikeCount()).isEqualTo(0);
    }
}
