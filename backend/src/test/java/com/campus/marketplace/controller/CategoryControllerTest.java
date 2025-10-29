package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.config.TestSecurityConfig;
import com.campus.marketplace.common.dto.request.CreateCategoryRequest;
import com.campus.marketplace.common.dto.request.UpdateCategoryRequest;
import com.campus.marketplace.common.dto.response.CategoryNodeResponse;
import com.campus.marketplace.service.CategoryService;
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
        controllers = CategoryController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("CategoryController MockMvc 测试")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @Test
    @DisplayName("获取分类树返回完整数据")
    void getCategoryTree_success() throws Exception {
        CategoryNodeResponse child = CategoryNodeResponse.builder()
                .id(2L)
                .name("键鼠")
                .description("外设")
                .parentId(1L)
                .sortOrder(5)
                .createdAt(LocalDateTime.parse("2025-10-29T08:00:00"))
                .children(List.of())
                .build();
        CategoryNodeResponse root = CategoryNodeResponse.builder()
                .id(1L)
                .name("数码")
                .description("数码产品")
                .sortOrder(1)
                .createdAt(LocalDateTime.parse("2025-10-29T07:00:00"))
                .children(List.of(child))
                .build();
        when(categoryService.getCategoryTree()).thenReturn(List.of(root));

        mockMvc.perform(get("/api/categories/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].children[0].name").value("键鼠"));
    }

    @Test
    @DisplayName("管理员创建分类成功")
    @WithMockUser(authorities = "system:category:manage")
    void createCategory_success() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest("数码配件", "外设配件", 10L, 3);
        when(categoryService.createCategory(any(CreateCategoryRequest.class))).thenReturn(999L);

        mockMvc.perform(post("/api/admin/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(999));

        verify(categoryService).createCategory(eq(request));
    }

    @Test
    @DisplayName("无权限创建分类被拒绝")
    @WithMockUser(roles = "STUDENT")
    void createCategory_forbidden() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest("测试", "描述", null, 1);

        mockMvc.perform(post("/api/admin/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(categoryService, never()).createCategory(any());
    }

    @Test
    @DisplayName("更新分类成功")
    @WithMockUser(authorities = "system:category:manage")
    void updateCategory_success() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest("改名", "新描述", null, 2);

        mockMvc.perform(put("/api/admin/categories/{id}", 321L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(categoryService).updateCategory(321L, request);
    }

    @Test
    @DisplayName("删除分类成功")
    @WithMockUser(authorities = "system:category:manage")
    void deleteCategory_success() throws Exception {
        mockMvc.perform(delete("/api/admin/categories/{id}", 555L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(categoryService).deleteCategory(555L);
    }
}
