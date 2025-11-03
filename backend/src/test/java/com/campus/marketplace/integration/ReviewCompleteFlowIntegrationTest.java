package com.campus.marketplace.integration;

import com.campus.marketplace.common.entity.Review;
import com.campus.marketplace.repository.ReviewLikeRepository;
import com.campus.marketplace.repository.ReviewMediaRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 评价系统完整流程集成测试
 *
 * Spec #7：测试从创建评价到上传媒体、添加回复、点赞的完整业务流程
 *
 * @author BaSui 😎 - 全流程测试，确保各功能无缝衔接！
 * @since 2025-11-03
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("评价系统完整流程集成测试")
class ReviewCompleteFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewMediaRepository reviewMediaRepository;

    @Autowired
    private ReviewReplyRepository reviewReplyRepository;

    @Autowired
    private ReviewLikeRepository reviewLikeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Review testReview;
    private Long buyerId = 100L;
    private Long sellerId = 200L;

    @BeforeEach
    void setUp() {
        // 创建测试评价
        testReview = Review.builder()
                .orderId(1L)
                .buyerId(buyerId)
                .sellerId(sellerId)
                .rating(5)
                .content("这件商品很好，质量不错！")
                .build();
        testReview = reviewRepository.save(testReview);
    }

    @Test
    @DisplayName("完整场景：买家评价 → 上传晒单图 → 卖家回复 → 其他用户点赞")
    void testCompleteReviewScenario() throws Exception {
        // 第1步：买家上传晒单图
        for (int i = 1; i <= 3; i++) {
            MockMultipartFile imageFile = new MockMultipartFile(
                    "file",
                    "product-photo-" + i + ".jpg",
                    "image/jpeg",
                    ("晒单图片" + i).getBytes()
            );

            mockMvc.perform(multipart("/api/reviews/{reviewId}/media", testReview.getId())
                            .file(imageFile)
                            .param("mediaType", "IMAGE")
                            .param("sortOrder", String.valueOf(i)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        assertThat(reviewMediaRepository.countByReviewId(testReview.getId())).isEqualTo(3);

        // 第2步：卖家回复
        Map<String, Object> sellerReply = new HashMap<>();
        sellerReply.put("replyType", "SELLER_REPLY");
        sellerReply.put("content", "感谢您的五星好评！");
        sellerReply.put("targetUserId", buyerId);

        mockMvc.perform(post("/api/reviews/{reviewId}/replies", testReview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sellerReply)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.replyType").value("SELLER_REPLY"));

        assertThat(reviewReplyRepository.countByReviewId(testReview.getId())).isEqualTo(1);

        // 第3步：其他用户点赞
        mockMvc.perform(post("/api/reviews/{reviewId}/like", testReview.getId()))
                .andExpect(status().isOk());

        Long likeCount = reviewLikeRepository.countByReviewIdAndIsActive(testReview.getId(), true);
        assertThat(likeCount).isGreaterThanOrEqualTo(1);

        // 第4步：查询完整信息
        mockMvc.perform(get("/api/reviews/{reviewId}/media", testReview.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));

        mockMvc.perform(get("/api/reviews/{reviewId}/replies", testReview.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        // 第5步：验证统计数据
        Review updatedReview = reviewRepository.findById(testReview.getId()).orElseThrow();
        assertThat(updatedReview.getReplyCount()).isGreaterThanOrEqualTo(1);
        assertThat(updatedReview.getLikeCount()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("场景2：管理员回复 → 买家标记已读")
    void testAdminReplyScenario() throws Exception {
        Map<String, Object> adminReply = new HashMap<>();
        adminReply.put("replyType", "ADMIN_REPLY");
        adminReply.put("content", "感谢反馈");
        adminReply.put("targetUserId", buyerId);

        mockMvc.perform(post("/api/reviews/{reviewId}/replies", testReview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminReply)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reviews/replies/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(put("/api/reviews/replies/read/all"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reviews/replies/unread/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(0));
    }

    @Test
    @DisplayName("场景3：图文评价 → 追加视频 → 删除媒体")
    void testMediaManagementScenario() throws Exception {
        // 上传2张图片
        for (int i = 1; i <= 2; i++) {
            MockMultipartFile imageFile = new MockMultipartFile(
                    "file", "image" + i + ".jpg", "image/jpeg", "content".getBytes()
            );
            mockMvc.perform(multipart("/api/reviews/{reviewId}/media", testReview.getId())
                            .file(imageFile)
                            .param("mediaType", "IMAGE"))
                    .andExpect(status().isOk());
        }

        assertThat(reviewMediaRepository.countByReviewId(testReview.getId())).isEqualTo(2);

        // 追加1个视频
        MockMultipartFile videoFile = new MockMultipartFile(
                "file", "video.mp4", "video/mp4", "video content".getBytes()
        );
        mockMvc.perform(multipart("/api/reviews/{reviewId}/media", testReview.getId())
                        .file(videoFile)
                        .param("mediaType", "VIDEO"))
                .andExpect(status().isOk());

        assertThat(reviewMediaRepository.countByReviewId(testReview.getId())).isEqualTo(3);

        // 删除所有媒体
        mockMvc.perform(delete("/api/reviews/{reviewId}/media", testReview.getId()))
                .andExpect(status().isOk());

        assertThat(reviewMediaRepository.countByReviewId(testReview.getId())).isEqualTo(0);
    }

    @Test
    @DisplayName("场景4：高互动评价 - 多次点赞 + 多条回复")
    void testHighInteractionScenario() throws Exception {
        // 多次切换点赞
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/reviews/{reviewId}/like/toggle", testReview.getId()))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/reviews/{reviewId}/like/status", testReview.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        // 卖家和管理员都回复
        Map<String, Object> sellerReply = new HashMap<>();
        sellerReply.put("replyType", "SELLER_REPLY");
        sellerReply.put("content", "感谢支持");
        sellerReply.put("targetUserId", buyerId);

        mockMvc.perform(post("/api/reviews/{reviewId}/replies", testReview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sellerReply)))
                .andExpect(status().isOk());

        Map<String, Object> adminReply = new HashMap<>();
        adminReply.put("replyType", "ADMIN_REPLY");
        adminReply.put("content", "核实无误");
        adminReply.put("targetUserId", buyerId);

        mockMvc.perform(post("/api/reviews/{reviewId}/replies", testReview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminReply)))
                .andExpect(status().isOk());

        assertThat(reviewReplyRepository.countByReviewId(testReview.getId())).isEqualTo(2);

        Review review = reviewRepository.findById(testReview.getId()).orElseThrow();
        assertThat(review.getReplyCount()).isEqualTo(2);
        assertThat(review.getLikeCount()).isGreaterThanOrEqualTo(1);
    }
}
