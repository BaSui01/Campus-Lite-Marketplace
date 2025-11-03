package com.campus.marketplace.integration;

import com.campus.marketplace.common.entity.Review;
import com.campus.marketplace.common.entity.ReviewMedia;
import com.campus.marketplace.common.enums.MediaType;
import com.campus.marketplace.repository.ReviewMediaRepository;
import com.campus.marketplace.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ReviewMedia 集成测试
 *
 * Spec #7：图文视频上传、查询、删除集成测试
 *
 * @author BaSui 😎 - 完整流程测试，确保功能无bug！
 * @since 2025-11-03
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("评价媒体集成测试")
class ReviewMediaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewMediaRepository reviewMediaRepository;

    private Review testReview;

    @BeforeEach
    void setUp() {
        // 创建测试评价
        testReview = Review.builder()
                .orderId(1L)
                .buyerId(100L)
                .sellerId(200L)
                .rating(5)
                .content("测试评价")
                .build();
        testReview = reviewRepository.save(testReview);
    }

    @Test
    @DisplayName("完整流程：上传图片 → 查询 → 删除")
    void testCompleteMediaFlow() throws Exception {
        // 1. 上传图片
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/reviews/{reviewId}/media", testReview.getId())
                        .file(imageFile)
                        .param("mediaType", "IMAGE")
                        .param("sortOrder", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mediaType").value("IMAGE"));

        // 验证数据库
        assertThat(reviewMediaRepository.countByReviewId(testReview.getId())).isEqualTo(1);

        // 2. 查询媒体列表
        mockMvc.perform(get("/api/reviews/{reviewId}/media", testReview.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].mediaType").value("IMAGE"));

        // 3. 删除媒体
        ReviewMedia media = reviewMediaRepository.findByReviewIdOrderBySortOrderAsc(testReview.getId()).get(0);
        mockMvc.perform(delete("/api/reviews/media/{mediaId}", media.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证已删除
        assertThat(reviewMediaRepository.countByReviewId(testReview.getId())).isEqualTo(0);
    }

    @Test
    @DisplayName("批量上传图片测试")
    void testBatchUploadImages() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "files", "image1.jpg", "image/jpeg", "content1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "files", "image2.jpg", "image/jpeg", "content2".getBytes()
        );

        mockMvc.perform(multipart("/api/reviews/{reviewId}/media/batch", testReview.getId())
                        .file(file1)
                        .file(file2)
                        .param("mediaType", "IMAGE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        // 验证数据库
        assertThat(reviewMediaRepository.countByReviewId(testReview.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("按类型查询媒体测试")
    void testGetMediaByType() throws Exception {
        // 创建测试数据
        ReviewMedia image = ReviewMedia.builder()
                .reviewId(testReview.getId())
                .mediaType(MediaType.IMAGE)
                .mediaUrl("uploads/test.jpg")
                .fileSize(1024L)
                .sortOrder(1)
                .build();
        reviewMediaRepository.save(image);

        // 查询图片类型
        mockMvc.perform(get("/api/reviews/{reviewId}/media/{mediaType}", 
                        testReview.getId(), "IMAGE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].mediaType").value("IMAGE"));
    }

    @Test
    @DisplayName("删除评价所有媒体测试")
    void testDeleteAllMediaByReviewId() throws Exception {
        // 创建多个媒体
        for (int i = 0; i < 3; i++) {
            ReviewMedia media = ReviewMedia.builder()
                    .reviewId(testReview.getId())
                    .mediaType(MediaType.IMAGE)
                    .mediaUrl("uploads/test" + i + ".jpg")
                    .fileSize(1024L)
                    .sortOrder(i + 1)
                    .build();
            reviewMediaRepository.save(media);
        }

        assertThat(reviewMediaRepository.countByReviewId(testReview.getId())).isEqualTo(3);

        // 批量删除
        mockMvc.perform(delete("/api/reviews/{reviewId}/media", testReview.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证已全部删除
        assertThat(reviewMediaRepository.countByReviewId(testReview.getId())).isEqualTo(0);
    }

    @Test
    @DisplayName("超过数量限制应返回错误")
    void testUploadExceedLimit() throws Exception {
        // 先创建10张图片
        for (int i = 0; i < 10; i++) {
            ReviewMedia media = ReviewMedia.builder()
                    .reviewId(testReview.getId())
                    .mediaType(MediaType.IMAGE)
                    .mediaUrl("uploads/test" + i + ".jpg")
                    .fileSize(1024L)
                    .sortOrder(i + 1)
                    .build();
            reviewMediaRepository.save(media);
        }

        // 尝试上传第11张图片
        MockMultipartFile file = new MockMultipartFile(
                "file", "image11.jpg", "image/jpeg", "content".getBytes()
        );

        mockMvc.perform(multipart("/api/reviews/{reviewId}/media", testReview.getId())
                        .file(file)
                        .param("mediaType", "IMAGE"))
                .andExpect(status().is4xxClientError());
    }
}
