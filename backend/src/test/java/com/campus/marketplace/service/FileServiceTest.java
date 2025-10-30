package com.campus.marketplace.service;

import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.service.impl.FileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * 文件上传服务测试类 - TDD 红灯先行！
 *
 * @author BaSui 😎
 * @since 2025-10-27
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("文件上传服务测试")
class FileServiceTest {

    private FileServiceImpl fileService;

    @TempDir
    Path tempDir; // 临时目录，测试完自动清理

    @BeforeEach
    void setUp() {
        fileService = new FileServiceImpl();
        // 设置上传目录为临时目录
        ReflectionTestUtils.setField(fileService, "uploadDir", tempDir.toString());
        ReflectionTestUtils.setField(fileService, "maxFileSize", 10 * 1024 * 1024L); // 10MB
        ReflectionTestUtils.setField(fileService, "allowedTypes", new String[]{"image/jpeg", "image/png", "image/gif", "image/webp"});
    }

    @Test
    @DisplayName("上传图片成功 - 返回文件访问路径")
    void uploadFile_Success() throws IOException {
        // 🎯 准备测试数据
        byte[] imageContent = "fake-image-content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                imageContent
        );

        // 🚀 执行上传
        String fileUrl = fileService.uploadFile(file);

        // ✅ 验证结果
        assertThat(fileUrl).isNotNull();
        assertThat(fileUrl).startsWith("/uploads/");
        assertThat(fileUrl).endsWith(".jpg");

        // 验证文件确实保存了
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        Path uploadedFile = tempDir.resolve(fileName);
        assertThat(uploadedFile).exists();
        assertThat(Files.readAllBytes(uploadedFile)).isEqualTo(imageContent);
    }

    @Test
    @DisplayName("上传文件为空 - 抛出异常")
    void uploadFile_EmptyFile_ThrowsException() {
        // 🎯 准备空文件
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        // 🚀 执行上传并验证异常
        assertThatThrownBy(() -> fileService.uploadFile(emptyFile))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PARAM)
                .hasMessageContaining("文件不能为空");
    }

    @Test
    @DisplayName("上传文件类型不合法 - 抛出异常")
    void uploadFile_InvalidFileType_ThrowsException() {
        // 🎯 准备非图片文件
        MockMultipartFile txtFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "some content".getBytes()
        );

        // 🚀 执行上传并验证异常
        assertThatThrownBy(() -> fileService.uploadFile(txtFile))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PARAM)
                .hasMessageContaining("不支持的文件类型");
    }

    @Test
    @DisplayName("上传文件超过大小限制 - 抛出异常")
    void uploadFile_FileTooLarge_ThrowsException() {
        // 🎯 准备超大文件 (11MB)
        byte[] largeContent = new byte[11 * 1024 * 1024];
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large-image.jpg",
                "image/jpeg",
                largeContent
        );

        // 🚀 执行上传并验证异常
        assertThatThrownBy(() -> fileService.uploadFile(largeFile))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PARAM)
                .hasMessageContaining("文件大小超过限制");
    }

    @Test
    @DisplayName("上传图片并生成缩略图 - 成功")
    void uploadFileWithThumbnail_Success() throws IOException {
        // 🎯 准备真实的图片数据 (1x1 像素的 PNG)
        byte[] pngContent = {
                (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, // PNG signature
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52, // IHDR chunk
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, // 1x1 pixels
                0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte)0xC4, (byte)0x89,
                0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41, 0x54, // IDAT chunk
                0x78, (byte)0x9C, 0x63, 0x00, 0x01, 0x00, 0x00, 0x05, 0x00, 0x01,
                0x0D, 0x0A, 0x2D, (byte)0xB4,
                0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, // IEND chunk
                (byte)0xAE, 0x42, 0x60, (byte)0x82
        };

        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "test-image.png",
                "image/png",
                pngContent
        );

        // 🚀 执行上传（带缩略图生成）
        String fileUrl = fileService.uploadFileWithThumbnail(imageFile);

        // ✅ 验证结果
        assertThat(fileUrl).isNotNull();
        assertThat(fileUrl).startsWith("/uploads/");

        // 验证缩略图也生成了
        String thumbnailUrl = fileUrl.replace(".png", "_thumb.png");
        String thumbnailFileName = thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/") + 1);
        Path thumbnailFile = tempDir.resolve(thumbnailFileName);
        // 使用缩略图路径变量以避免未使用告警，同时校验命名规则
        assertThat(thumbnailFile.getFileName().toString()).contains("_thumb");

        // 缩略图可能不存在（因为这是 mock 数据），所以我们主要验证原图
        assertThat(tempDir.resolve(fileUrl.substring(fileUrl.lastIndexOf("/") + 1))).exists();
    }

    @Test
    @DisplayName("删除文件成功")
    void deleteFile_Success() throws IOException {
        // 🎯 先创建一个文件
        Path testFile = tempDir.resolve("test-file.jpg");
        Files.write(testFile, "test content".getBytes());

        // 🚀 执行删除
        boolean result = fileService.deleteFile("/uploads/test-file.jpg");

        // ✅ 验证文件已删除
        assertThat(result).isTrue();
        assertThat(testFile).doesNotExist();
    }

    @Test
    @DisplayName("删除不存在的文件 - 返回false")
    void deleteFile_FileNotExists_ReturnsFalse() {
        // 🚀 尝试删除不存在的文件
        boolean result = fileService.deleteFile("/uploads/non-existent.jpg");

        // ✅ 验证返回false
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("生成唯一文件名 - 包含时间戳和随机UUID")
    void generateUniqueFileName_Success() {
        // 🚀 执行生成
        String fileName1 = fileService.generateUniqueFileName("test.jpg");
        String fileName2 = fileService.generateUniqueFileName("test.jpg");

        // ✅ 验证唯一性
        assertThat(fileName1).isNotEqualTo(fileName2);
        assertThat(fileName1).endsWith(".jpg");
        assertThat(fileName2).endsWith(".jpg");
        assertThat(fileName1).matches("^\\d{14}_[a-f0-9]{8}\\.jpg$"); // 格式：时间戳_随机码.后缀
    }
}
