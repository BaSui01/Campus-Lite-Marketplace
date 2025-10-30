package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.config.TestSecurityConfig;
import com.campus.marketplace.common.dto.response.GoodsResponse;
import com.campus.marketplace.service.FavoriteService;
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

import java.math.BigDecimal;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = FavoriteController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("FavoriteController MockMvc 测试")
class FavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FavoriteService favoriteService;

    @Test
    @DisplayName("添加收藏成功返回统一响应")
    @WithMockUser(roles = "STUDENT")
    void addFavorite_success() throws Exception {
        mockMvc.perform(post("/api/favorites/{goodsId}", 1001L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(favoriteService).addFavorite(1001L);
    }

    @Test
    @DisplayName("非学生角色调用添加收藏返回403")
    @WithMockUser(roles = "TEACHER")
    void addFavorite_forbidden() throws Exception {
        mockMvc.perform(post("/api/favorites/{goodsId}", 2002L))
                .andExpect(status().isForbidden());

        verify(favoriteService, never()).addFavorite(any());
    }

    @Test
    @DisplayName("取消收藏成功")
    @WithMockUser(roles = "STUDENT")
    void removeFavorite_success() throws Exception {
        mockMvc.perform(delete("/api/favorites/{goodsId}", 3003L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(favoriteService).removeFavorite(3003L);
    }

    @Test
    @DisplayName("分页查询收藏列表返回数据内容")
    @WithMockUser(roles = "STUDENT")
    void listFavorites_success() throws Exception {
        GoodsResponse response = GoodsResponse.builder()
                .id(9009L)
                .title("MacBook Pro")
                .price(new BigDecimal("9999.00"))
                .createdAt(LocalDateTime.parse("2025-10-29T10:00:00"))
                .build();
        when(favoriteService.listFavorites(0, 20))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].id").value(9009))
                .andExpect(jsonPath("$.data.content[0].title").value("MacBook Pro"));
    }

    @Test
    @DisplayName("检查收藏状态返回布尔值")
    void isFavorited_success() throws Exception {
        when(favoriteService.isFavorited(8080L)).thenReturn(true);

        mockMvc.perform(get("/api/favorites/{goodsId}/check", 8080L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(favoriteService).isFavorited(eq(8080L));
    }
}
