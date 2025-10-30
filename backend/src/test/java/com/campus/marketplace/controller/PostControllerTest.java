package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.config.TestSecurityConfig;
import com.campus.marketplace.common.dto.request.CreatePostRequest;
import com.campus.marketplace.common.dto.request.UpdatePostRequest;
import com.campus.marketplace.common.dto.response.PostResponse;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PostController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("PostController MockMvc 测试")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @Test
    @DisplayName("学生角色发布帖子成功返回帖子ID")
    @WithMockUser(roles = "STUDENT")
    void createPost_success() throws Exception {
        CreatePostRequest request = new CreatePostRequest(
                "标题",
                "帖子内容",
                List.of("https://img1.png")
        );
        when(postService.createPost(any(CreatePostRequest.class))).thenReturn(321L);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(321));

        verify(postService).createPost(eq(request));
    }

    @Test
    @DisplayName("发布帖子缺少标题触发校验失败")
    @WithMockUser(roles = "STUDENT")
    void createPost_validationError() throws Exception {
        String invalid = """
                {
                  "title": "",
                  "content": "hello"
                }
                """;

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        verify(postService, never()).createPost(any());
    }

    @Test
    @DisplayName("更新帖子成功")
    @WithMockUser(roles = "STUDENT")
    void updatePost_success() throws Exception {
        UpdatePostRequest request = new UpdatePostRequest(
                "新标题",
                "新内容",
                List.of("https://img2.png")
        );

        mockMvc.perform(put("/api/posts/{id}", 111L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(postService).updatePost(111L, request);
    }

    @Test
    @DisplayName("分页获取帖子列表成功")
    void listPosts_success() throws Exception {
        PostResponse resp = PostResponse.builder()
                .id(201L)
                .title("帖子1")
                .content("内容1")
                .status(GoodsStatus.APPROVED)
                .createdAt(LocalDateTime.parse("2025-10-29T12:10:00"))
                .build();
        when(postService.listPosts(0, 20, "createdAt", "DESC"))
                .thenReturn(new PageImpl<>(List.of(resp)));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].id").value(201));
    }

    @Test
    @DisplayName("搜索帖子成功")
    void searchPosts_success() throws Exception {
        when(postService.searchPosts("耳机", 0, 10))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/posts/search")
                        .param("keyword", "耳机")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(postService).searchPosts("耳机", 0, 10);
    }

    @Test
    @DisplayName("按作者查询帖子返回分页")
    void listPostsByAuthor_success() throws Exception {
        when(postService.listPostsByAuthor(77L, 1, 5))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/posts/user/{authorId}", 77L)
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(postService).listPostsByAuthor(77L, 1, 5);
    }

    @Test
    @DisplayName("获取帖子详情成功")
    void getPostDetail_success() throws Exception {
        PostResponse response = PostResponse.builder()
                .id(909L)
                .title("详情")
                .content("详情内容")
                .authorId(12L)
                .status(GoodsStatus.APPROVED)
                .build();
        when(postService.getPostDetail(909L)).thenReturn(response);

        mockMvc.perform(get("/api/posts/{id}", 909L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(909));

        verify(postService).getPostDetail(909L);
    }

    @Test
    @DisplayName("管理员审核帖子成功")
    @WithMockUser(authorities = "system:post:approve")
    void approvePost_success() throws Exception {
        mockMvc.perform(post("/api/posts/{id}/approve", 808L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(postService).approvePost(808L, true, null);
    }

    @Test
    @DisplayName("学生角色删除帖子成功")
    @WithMockUser(roles = "STUDENT")
    void deletePost_success() throws Exception {
        mockMvc.perform(delete("/api/posts/{id}", 606L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(postService).deletePost(606L);
    }
}
