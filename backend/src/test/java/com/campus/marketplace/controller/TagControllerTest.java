package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.config.TestSecurityConfig;
import com.campus.marketplace.common.dto.request.CreateTagRequest;
import com.campus.marketplace.common.dto.request.MergeTagRequest;
import com.campus.marketplace.common.dto.request.UpdateTagRequest;
import com.campus.marketplace.common.dto.response.TagResponse;
import com.campus.marketplace.service.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
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
        controllers = TagController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("TagController MockMvc 测试")
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TagService tagService;

    @Test
    @DisplayName("查询标签列表返回数据")
    void listTags_success() throws Exception {
        TagResponse tag = TagResponse.builder()
                .id(11L)
                .name("数码")
                .description("电子产品")
                .enabled(true)
                .createdAt(LocalDateTime.parse("2025-10-29T09:00:00"))
                .build();
        when(tagService.listAllTags()).thenReturn(List.of(tag));

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("数码"));
    }

    @Test
    @DisplayName("管理员创建标签成功")
    @WithMockUser(authorities = "system:tag:manage")
    void createTag_success() throws Exception {
        CreateTagRequest request = new CreateTagRequest("二手", "二手相关", true);
        when(tagService.createTag(any(CreateTagRequest.class))).thenReturn(202L);

        mockMvc.perform(post("/api/admin/tags")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(202));

        verify(tagService).createTag(eq(request));
    }

    @Test
    @DisplayName("无权限创建标签返回403")
    @WithMockUser(roles = "STUDENT")
    void createTag_forbidden() throws Exception {
        CreateTagRequest request = new CreateTagRequest("校内", null, true);

        mockMvc.perform(post("/api/admin/tags")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(tagService, never()).createTag(any());
    }

    @Test
    @DisplayName("更新标签成功")
    @WithMockUser(authorities = "system:tag:manage")
    void updateTag_success() throws Exception {
        UpdateTagRequest request = new UpdateTagRequest("改名", "新描述", false);

        mockMvc.perform(put("/api/admin/tags/{id}", 303L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(tagService).updateTag(303L, request);
    }

    @Test
    @DisplayName("删除标签成功")
    @WithMockUser(authorities = "system:tag:manage")
    void deleteTag_success() throws Exception {
        mockMvc.perform(delete("/api/admin/tags/{id}", 404L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(tagService).deleteTag(404L);
    }

    @Test
    @DisplayName("合并标签成功")
    @WithMockUser(authorities = "system:tag:manage")
    void mergeTag_success() throws Exception {
        MergeTagRequest request = new MergeTagRequest(10L, 3L);

        mockMvc.perform(post("/api/admin/tags/merge")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(tagService).mergeTag(request);
    }
}
