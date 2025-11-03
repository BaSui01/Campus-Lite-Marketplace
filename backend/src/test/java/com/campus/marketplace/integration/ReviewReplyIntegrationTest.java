package com.campus.marketplace.integration;

import com.campus.marketplace.common.entity.Review;
import com.campus.marketplace.common.entity.ReviewReply;
import com.campus.marketplace.common.enums.ReplyType;
import com.campus.marketplace.repository.ReviewReplyRepository;
import com.campus.marketplace.repository.ReviewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ReviewReply 集成测试
 *
 * Spec #7：回复功能集成测试
 *
 * @author BaSui 😎 - 测试卖家回复、已读标记完整流程！
 * @since 2025-11-03
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("评价回复集成测试")
class ReviewReplyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewReplyRepository reviewReplyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Review testReview;
    private Long sellerId = 200L;
    private Long buyerId = 100L;

    @BeforeEach
    void setUp() {
        // 创建测试评价
        testReview = Review.builder()
                .orderId(1L)
                .buyerId(buyerId)
                .sellerId(sellerId)
                .rating(5)
                .content("测试评价")
                .build();
        testReview = reviewRepository.save(testReview);
    }

    @Test
    @DisplayName("完整流程：创建回复 → 查询 → 标记已读 → 删除")
    void testCompleteReplyFlow() throws Exception {
        // 1. 创建卖家回复
        Map<String, Object> request = new HashMap<>();
        request.put("replyType", "SELLER_REPLY");
        request.put("content", "感谢您的好评！");
        request.put("targetUserId", buyerId);

        mockMvc.perform(post("/api/reviews/{reviewId}/replies", testReview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.replyType").value("SELLER_REPLY"))
                .andExpect(jsonPath("$.data.content").value("感谢您的好评！"));

        // 验证数据库
        assertThat(reviewReplyRepository.countByReviewId(testReview.getId())).isEqualTo(1);

        // 2. 查询回复列表
        mockMvc.perform(get("/api/reviews/{reviewId}/replies", testReview.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].content").value("感谢您的好评！"));

        // 3. 标记为已读
        ReviewReply reply = reviewReplyRepository.findByReviewIdOrderByCreatedAtAsc(testReview.getId()).get(0);
        mockMvc.perform(put("/api/reviews/replies/{replyId}/read", reply.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证已读状态
        ReviewReply updatedReply = reviewReplyRepository.findById(reply.getId()).orElseThrow();
        assertThat(updatedReply.getIsRead()).isTrue();

        // 4. 删除回复
        mockMvc.perform(delete("/api/reviews/replies/{replyId}", reply.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证已删除
        assertThat(reviewReplyRepository.countByReviewId(testReview.getId())).isEqualTo(0);
    }

    @Test
    @DisplayName("管理员回复测试")
    void testAdminReply() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("replyType", "ADMIN_REPLY");
        request.put("content", "此评价已核实，感谢反馈");
        request.put("targetUserId", buyerId);

        mockMvc.perform(post("/api/reviews/{reviewId}/replies", testReview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.replyType").value("ADMIN_REPLY"));
    }

    @Test
    @DisplayName("获取未读回复测试")
    void testGetUnreadReplies() throws Exception {
        // 创建多个未读回复
        for (int i = 0; i < 3; i++) {
            ReviewReply reply = ReviewReply.builder()
                    .reviewId(testReview.getId())
                    .replierId(sellerId)
                    .replyType(ReplyType.SELLER_REPLY)
                    .content("回复内容" + i)
                    .targetUserId(buyerId)
                    .isRead(false)
                    .build();
            reviewReplyRepository.save(reply);
        }

        // 查询未读回复
        mockMvc.perform(get("/api/reviews/replies/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3));

        // 查询未读数量
        mockMvc.perform(get("/api/reviews/replies/unread/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(3));
    }

    @Test
    @DisplayName("批量标记所有回复为已读")
    void testMarkAllRepliesAsRead() throws Exception {
        // 创建多个未读回复
        for (int i = 0; i < 5; i++) {
            ReviewReply reply = ReviewReply.builder()
                    .reviewId(testReview.getId())
                    .replierId(sellerId)
                    .replyType(ReplyType.SELLER_REPLY)
                    .content("回复内容" + i)
                    .targetUserId(buyerId)
                    .isRead(false)
                    .build();
            reviewReplyRepository.save(reply);
        }

        assertThat(reviewReplyRepository.countByTargetUserIdAndIsRead(buyerId, false)).isEqualTo(5);

        // 批量标记为已读
        mockMvc.perform(put("/api/reviews/replies/read/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证全部已读
        assertThat(reviewReplyRepository.countByTargetUserIdAndIsRead(buyerId, false)).isEqualTo(0);
    }

    @Test
    @DisplayName("空内容回复应返回错误")
    void testCreateReplyWithEmptyContent() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("replyType", "SELLER_REPLY");
        request.put("content", "");
        request.put("targetUserId", buyerId);

        mockMvc.perform(post("/api/reviews/{reviewId}/replies", testReview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }
}
