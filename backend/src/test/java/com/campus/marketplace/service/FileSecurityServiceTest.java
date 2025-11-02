package com.campus.marketplace.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.*;

/**
 * 文件安全检查服务测试类
 * 
 * TDD测试驱动开发：先写失败的测试，然后实现功能让测试通过
 * 
 * @author BaSui 😎
 * @date 2025-11-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("文件安全检查服务测试")
class FileSecurityServiceTest {

    private FileSecurityService fileSecurityService;

    @BeforeEach
    void setUp() {
        // 注入实现类
        fileSecurityService = new com.campus.marketplace.service.impl.FileSecurityServiceImpl();
    }

    @Test
    @DisplayName("应该验证合法的图片文件类型")
    void shouldValidateLegalImageFileType() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        // Act & Assert
        assertThatCode(() -> fileSecurityService.validateFileType(file))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("应该拒绝非法的文件类型")
    void shouldRejectIllegalFileType() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "malicious.exe",
            "application/x-msdownload",
            "malicious content".getBytes()
        );

        // Act & Assert
        assertThatThrownBy(() -> fileSecurityService.validateFileType(file))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("不支持的文件类型");
    }

    @Test
    @DisplayName("应该验证文件大小在限制范围内")
    void shouldValidateFileSizeWithinLimit() {
        // Arrange - 5MB文件
        byte[] content = new byte[5 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            content
        );

        // Act & Assert
        assertThatCode(() -> fileSecurityService.validateFileSize(file, 10 * 1024 * 1024L))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("应该拒绝超过大小限制的文件")
    void shouldRejectOversizedFile() {
        // Arrange - 15MB文件
        byte[] content = new byte[15 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "large.jpg",
            "image/jpeg",
            content
        );

        // Act & Assert
        assertThatThrownBy(() -> fileSecurityService.validateFileSize(file, 10 * 1024 * 1024L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("文件大小超过限制");
    }

    @Test
    @DisplayName("应该检测文件名中的危险字符")
    void shouldDetectDangerousCharactersInFileName() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "../../../etc/passwd",
            "text/plain",
            "content".getBytes()
        );

        // Act & Assert
        assertThatThrownBy(() -> fileSecurityService.validateFileName(file))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("文件名包含非法字符");
    }

    @Test
    @DisplayName("应该检测空文件")
    void shouldDetectEmptyFile() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.txt",
            "text/plain",
            new byte[0]
        );

        // Act & Assert
        assertThatThrownBy(() -> fileSecurityService.validateNotEmpty(file))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("文件不能为空");
    }

    @Test
    @DisplayName("应该验证文件扩展名与MIME类型匹配")
    void shouldValidateExtensionMatchesMimeType() {
        // Arrange - 扩展名与MIME类型不匹配
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "fake.jpg",
            "application/pdf",  // MIME是PDF，但扩展名是jpg
            "content".getBytes()
        );

        // Act & Assert
        assertThatThrownBy(() -> fileSecurityService.validateExtensionMatchesMimeType(file))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("文件扩展名与实际类型不匹配");
    }

    @Test
    @DisplayName("应该通过完整的安全检查")
    void shouldPassCompleteSecurity检查() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "valid image content".getBytes()
        );

        // Act & Assert
        assertThatCode(() -> fileSecurityService.performSecurityCheck(file))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("应该计算文件的SHA256哈希值")
    void shouldCalculateFileSHA256Hash() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "test content".getBytes()
        );

        // Act
        String hash = fileSecurityService.calculateFileHash(file);

        // Assert
        assertThat(hash)
            .isNotNull()
            .isNotEmpty()
            .hasSize(64); // SHA-256 produces 64 hex characters
    }

    @Test
    @DisplayName("应该模拟病毒扫描并返回清洁结果")
    void shouldPerformVirusScanAndReturnClean() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "clean.txt",
            "text/plain",
            "clean content".getBytes()
        );

        // Act
        String scanResult = fileSecurityService.scanForVirus(file);

        // Assert
        assertThat(scanResult).isEqualTo("CLEAN");
    }

    @Test
    @DisplayName("应该检测恶意文件名并返回感染结果")
    void shouldDetectMaliciousFileNameAndReturnInfected() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "virus_detected.exe",
            "application/x-msdownload",
            "malicious content".getBytes()
        );

        // Act
        String scanResult = fileSecurityService.scanForVirus(file);

        // Assert
        assertThat(scanResult).isEqualTo("INFECTED");
    }
}
