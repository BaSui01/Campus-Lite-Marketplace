package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.config.TestSecurityConfig;
import com.campus.marketplace.service.FileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = FileController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("FileController MockMvc 测试")
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileService fileService;

    @Test
    @DisplayName("上传图片返回下载地址")
    @WithMockUser
    void uploadFile_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "cat.png", MediaType.IMAGE_PNG_VALUE, "img".getBytes());
        when(fileService.uploadFile(file)).thenReturn("https://cdn/cat.png");

        mockMvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.url").value("https://cdn/cat.png"))
                .andExpect(jsonPath("$.data.filename").value("cat.png"));

        verify(fileService).uploadFile(file);
    }

    @Test
    @DisplayName("上传图片生成缩略图")
    @WithMockUser
    void uploadFileWithThumbnail_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "dog.jpg", MediaType.IMAGE_JPEG_VALUE, "img".getBytes());
        when(fileService.uploadFileWithThumbnail(file)).thenReturn("https://cdn/dog.jpg");

        mockMvc.perform(multipart("/api/files/upload-with-thumbnail").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.url").value("https://cdn/dog.jpg"))
                .andExpect(jsonPath("$.data.thumbnail").value("https://cdn/dog_thumb.jpg"));

        verify(fileService).uploadFileWithThumbnail(file);
    }

    @Test
    @DisplayName("删除文件成功")
    @WithMockUser
    void deleteFile_success() throws Exception {
        when(fileService.deleteFile("https://cdn/file.png")).thenReturn(true);

        mockMvc.perform(delete("/api/files").param("url", "https://cdn/file.png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        verify(fileService).deleteFile("https://cdn/file.png");
    }

    @Test
    @DisplayName("删除文件失败返回错误响应")
    @WithMockUser
    void deleteFile_fail() throws Exception {
        when(fileService.deleteFile("https://cdn/missing.png")).thenReturn(false);

        mockMvc.perform(delete("/api/files").param("url", "https://cdn/missing.png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("文件删除失败"));

        verify(fileService).deleteFile("https://cdn/missing.png");
    }
}
