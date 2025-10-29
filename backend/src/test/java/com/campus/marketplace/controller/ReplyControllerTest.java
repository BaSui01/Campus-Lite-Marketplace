package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.config.TestSecurityConfig;
import com.campus.marketplace.common.dto.request.CreateReplyRequest;
import com.campus.marketplace.common.dto.response.ReplyResponse;
import com.campus.marketplace.service.ReplyService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ReplyController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("ReplyController MockMvc 测试")
class ReplyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReplyService replyService;

    @Test
    @DisplayName("学生创建回复成功")
    @WithMockUser(roles = "STUDENT")
    void createReply_success() throws Exception {
        CreateReplyRequest request = new CreateReplyRequest(100L, "不错", null, null);
        when(replyService.createReply(request)).thenReturn(321L);

        mockMvc.perform(post("/api/replies")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(321));

        verify(replyService).createReply(request);
    }

    @Test
    @DisplayName("非学生角色创建回复被拒绝")
    @WithMockUser(roles = "USER")
    void createReply_forbidden() throws Exception {
        CreateReplyRequest request = new CreateReplyRequest(101L, "评论", null, null);

        mockMvc.perform(post("/api/replies")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isForbidden());

        verify(replyService, never()).createReply(any());
    }

    @Test
    @DisplayName("查询帖子回复列表返回分页数据")
    void listReplies_success() throws Exception {
        ReplyResponse resp = ReplyResponse.builder()
                .id(1L)
                .postId(200L)
                .content("回复内容")
                .authorId(2L)
                .createdAt(LocalDateTime.parse("2025-10-29T12:00:00"))
                .build();
        when(replyService.listReplies(200L, 1, 5))
                .thenReturn(new PageImpl<>(List.of(resp)));

        mockMvc.perform(get("/api/replies/post/{postId}", 200L)
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].content").value("回复内容"));

        verify(replyService).listReplies(200L, 1, 5);
    }

    @Test
    @DisplayName("查询子回复列表")
    void listSubReplies_success() throws Exception {
        ReplyResponse child = ReplyResponse.builder()
                .id(2L)
                .postId(200L)
                .parentId(1L)
                .content("子回复")
                .createdAt(LocalDateTime.parse("2025-10-29T13:00:00"))
                .build();
        when(replyService.listSubReplies(1L)).thenReturn(List.of(child));

        mockMvc.perform(get("/api/replies/{parentId}/sub", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].content").value("子回复"));

        verify(replyService).listSubReplies(1L);
    }

    @Test
    @DisplayName("学生删除回复成功")
    @WithMockUser(roles = "STUDENT")
    void deleteReply_success() throws Exception {
        mockMvc.perform(delete("/api/replies/{id}", 88L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(replyService).deleteReply(88L);
    }
}
