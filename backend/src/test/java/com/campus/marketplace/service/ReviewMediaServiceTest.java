package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.ReviewMedia;
import com.campus.marketplace.common.enums.MediaType;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.repository.ReviewMediaRepository;
import com.campus.marketplace.service.impl.ReviewMediaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ReviewMediaService 单元测试
 *
 * Spec #7：图文视频管理单元测试
 *
 * @author BaSui 😎 - 测试文件上传，让bug无处藏身！
 * @since 2025-11-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("评价媒体服务测试")
class ReviewMediaServiceTest {

    @Mock
    private ReviewMediaRepository reviewMediaRepository;

    @InjectMocks
    private ReviewMediaServiceImpl reviewMediaService;

    private Long reviewId;
    private MockMultipartFile validImageFile;
    private MockMultipartFile validVideoFile;
    private MockMultipartFile oversizedImageFile;
    private MockMultipartFile invalidFormatFile;

    @BeforeEach
    void setUp() {
        reviewId = 1L;

        // 创建有效的图片文件（2MB）
        byte[] imageContent = new byte[2 * 1024 * 1024];
        validImageFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                imageContent
        );

        // 创建有效的视频文件（50MB）
        byte[] videoContent = new byte[50 * 1024 * 1024];
        validVideoFile = new MockMultipartFile(
                "file",
                "test-video.mp4",
                "video/mp4",
                videoContent
        );

        // 创建超大图片文件（10MB，超过5MB限制）
        byte[] oversizedContent = new byte[10 * 1024 * 1024];
        oversizedImageFile = new MockMultipartFile(
                "file",
                "oversized-image.jpg",
                "image/jpeg",
                oversizedContent
        );

        // 创建不支持格式的文件
        invalidFormatFile = new MockMultipartFile(
                "file",
                "invalid.exe",
                "application/x-msdownload",
                "test content".getBytes()
        );
    }

    @Test
    @DisplayName("上传图片成功")
    void testUploadImage_Success() {
        // Arrange
        when(reviewMediaRepository.countByReviewIdAndMediaType(reviewId, MediaType.IMAGE))
                .thenReturn(0L);
        when(reviewMediaRepository.save(any(ReviewMedia.class)))
                .thenAnswer(invocation -> {
                    ReviewMedia media = invocation.getArgument(0);
                    media.setId(1L);
                    return media;
                });

        // Act
        ReviewMedia result = reviewMediaService.uploadMedia(reviewId, validImageFile, MediaType.IMAGE, 1);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getReviewId()).isEqualTo(reviewId);
        assertThat(result.getMediaType()).isEqualTo(MediaType.IMAGE);
        assertThat(result.getFileSize()).isEqualTo(validImageFile.getSize());
        assertThat(result.getOriginalFilename()).isEqualTo("test-image.jpg");

        verify(reviewMediaRepository, times(1)).save(any(ReviewMedia.class));
    }

    @Test
    @DisplayName("上传视频成功")
    void testUploadVideo_Success() {
        // Arrange
        when(reviewMediaRepository.countByReviewIdAndMediaType(reviewId, MediaType.VIDEO))
                .thenReturn(0L);
        when(reviewMediaRepository.save(any(ReviewMedia.class)))
                .thenAnswer(invocation -> {
                    ReviewMedia media = invocation.getArgument(0);
                    media.setId(2L);
                    return media;
                });

        // Act
        ReviewMedia result = reviewMediaService.uploadMedia(reviewId, validVideoFile, MediaType.VIDEO, 1);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getMediaType()).isEqualTo(MediaType.VIDEO);
        assertThat(result.getFileSize()).isEqualTo(validVideoFile.getSize());

        verify(reviewMediaRepository, times(1)).save(any(ReviewMedia.class));
    }

    @Test
    @DisplayName("上传空文件应抛出异常")
    void testUploadMedia_EmptyFile_ShouldThrowException() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        // Act & Assert
        assertThatThrownBy(() ->
                reviewMediaService.uploadMedia(reviewId, emptyFile, MediaType.IMAGE, 1)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("文件不能为空");

        verify(reviewMediaRepository, never()).save(any());
    }

    @Test
    @DisplayName("上传超大文件应抛出异常")
    void testUploadMedia_OversizedFile_ShouldThrowException() {
        // Act & Assert
        assertThatThrownBy(() ->
                reviewMediaService.uploadMedia(reviewId, oversizedImageFile, MediaType.IMAGE, 1)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("文件大小超过限制");

        verify(reviewMediaRepository, never()).save(any());
    }

    @Test
    @DisplayName("上传不支持格式应抛出异常")
    void testUploadMedia_InvalidFormat_ShouldThrowException() {
        // Act & Assert
        assertThatThrownBy(() ->
                reviewMediaService.uploadMedia(reviewId, invalidFormatFile, MediaType.IMAGE, 1)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不支持的文件格式");

        verify(reviewMediaRepository, never()).save(any());
    }

    @Test
    @DisplayName("图片数量超过限制应抛出异常")
    void testUploadMedia_ExceedImageLimit_ShouldThrowException() {
        // Arrange
        when(reviewMediaRepository.countByReviewIdAndMediaType(reviewId, MediaType.IMAGE))
                .thenReturn(10L); // 已有10张图片

        // Act & Assert
        assertThatThrownBy(() ->
                reviewMediaService.uploadMedia(reviewId, validImageFile, MediaType.IMAGE, 1)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("图片数量已达上限");

        verify(reviewMediaRepository, never()).save(any());
    }

    @Test
    @DisplayName("视频数量超过限制应抛出异常")
    void testUploadMedia_ExceedVideoLimit_ShouldThrowException() {
        // Arrange
        when(reviewMediaRepository.countByReviewIdAndMediaType(reviewId, MediaType.VIDEO))
                .thenReturn(1L); // 已有1个视频

        // Act & Assert
        assertThatThrownBy(() ->
                reviewMediaService.uploadMedia(reviewId, validVideoFile, MediaType.VIDEO, 1)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("视频数量已达上限");

        verify(reviewMediaRepository, never()).save(any());
    }

    @Test
    @DisplayName("获取评价的所有媒体")
    void testGetReviewMedia_Success() {
        // Arrange
        ReviewMedia media1 = createMockReviewMedia(1L, MediaType.IMAGE, 1);
        ReviewMedia media2 = createMockReviewMedia(2L, MediaType.IMAGE, 2);
        List<ReviewMedia> mockMediaList = List.of(media1, media2);

        when(reviewMediaRepository.findByReviewIdOrderBySortOrderAsc(reviewId))
                .thenReturn(mockMediaList);

        // Act
        List<ReviewMedia> result = reviewMediaService.getReviewMedia(reviewId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSortOrder()).isEqualTo(1);
        assertThat(result.get(1).getSortOrder()).isEqualTo(2);

        verify(reviewMediaRepository, times(1)).findByReviewIdOrderBySortOrderAsc(reviewId);
    }

    @Test
    @DisplayName("按类型获取评价媒体")
    void testGetReviewMediaByType_Success() {
        // Arrange
        ReviewMedia media = createMockReviewMedia(1L, MediaType.VIDEO, 1);
        when(reviewMediaRepository.findByReviewIdAndMediaType(reviewId, MediaType.VIDEO))
                .thenReturn(List.of(media));

        // Act
        List<ReviewMedia> result = reviewMediaService.getReviewMediaByType(reviewId, MediaType.VIDEO);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMediaType()).isEqualTo(MediaType.VIDEO);

        verify(reviewMediaRepository, times(1))
                .findByReviewIdAndMediaType(reviewId, MediaType.VIDEO);
    }

    @Test
    @DisplayName("删除媒体成功")
    void testDeleteMedia_Success() {
        // Arrange
        ReviewMedia media = createMockReviewMedia(1L, MediaType.IMAGE, 1);
        when(reviewMediaRepository.findById(1L)).thenReturn(Optional.of(media));

        // Act
        reviewMediaService.deleteMedia(1L);

        // Assert
        verify(reviewMediaRepository, times(1)).findById(1L);
        verify(reviewMediaRepository, times(1)).delete(media);
    }

    @Test
    @DisplayName("删除不存在的媒体应抛出异常")
    void testDeleteMedia_NotFound_ShouldThrowException() {
        // Arrange
        when(reviewMediaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reviewMediaService.deleteMedia(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("媒体不存在");

        verify(reviewMediaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("统计评价媒体数量")
    void testCountReviewMedia_Success() {
        // Arrange
        when(reviewMediaRepository.countByReviewId(reviewId)).thenReturn(5L);

        // Act
        long count = reviewMediaService.countReviewMedia(reviewId);

        // Assert
        assertThat(count).isEqualTo(5L);
        verify(reviewMediaRepository, times(1)).countByReviewId(reviewId);
    }

    @Test
    @DisplayName("验证媒体数量限制 - 图片")
    void testValidateMediaLimit_Image_Success() {
        // Arrange
        when(reviewMediaRepository.countByReviewIdAndMediaType(reviewId, MediaType.IMAGE))
                .thenReturn(5L);

        // Act
        boolean result = reviewMediaService.validateMediaLimit(reviewId, MediaType.IMAGE, 3);

        // Assert
        assertThat(result).isTrue(); // 5 + 3 = 8 <= 10
    }

    @Test
    @DisplayName("验证媒体数量限制 - 视频")
    void testValidateMediaLimit_Video_Fail() {
        // Arrange
        when(reviewMediaRepository.countByReviewIdAndMediaType(reviewId, MediaType.VIDEO))
                .thenReturn(1L);

        // Act
        boolean result = reviewMediaService.validateMediaLimit(reviewId, MediaType.VIDEO, 1);

        // Assert
        assertThat(result).isFalse(); // 1 + 1 = 2 > 1
    }

    /**
     * 创建模拟的ReviewMedia对象
     */
    private ReviewMedia createMockReviewMedia(Long id, MediaType mediaType, int sortOrder) {
        ReviewMedia media = ReviewMedia.builder()
                .reviewId(reviewId)
                .mediaType(mediaType)
                .mediaUrl("uploads/reviews/" + reviewId + "/test.jpg")
                .fileSize(1024L)
                .sortOrder(sortOrder)
                .originalFilename("test.jpg")
                .build();
        media.setId(id);
        return media;
    }
}
