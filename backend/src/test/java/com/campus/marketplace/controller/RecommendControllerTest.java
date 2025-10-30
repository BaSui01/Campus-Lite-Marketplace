package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.config.TestSecurityConfig;
import com.campus.marketplace.common.dto.response.GoodsResponse;
import com.campus.marketplace.service.RecommendService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = RecommendController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("RecommendController MockMvc 测试")
class RecommendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecommendService recommendService;

    @Test
    @DisplayName("获取热门榜单成功并带参数")
    void hot_success() throws Exception {
        GoodsResponse goods = GoodsResponse.builder()
                .id(101L)
                .title("热门物品")
                .price(new BigDecimal("199.99"))
                .createdAt(LocalDateTime.parse("2025-10-29T12:00:00"))
                .build();
        when(recommendService.getHotList(1L, 5)).thenReturn(List.of(goods));

        mockMvc.perform(get("/api/recommend/hot")
                        .param("campusId", "1")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(101))
                .andExpect(jsonPath("$.data[0].title").value("热门物品"));

        verify(recommendService).getHotList(1L, 5);
    }

    @Test
    @DisplayName("个性化推荐需要已登录角色")
    @WithMockUser(roles = "STUDENT")
    void personal_success() throws Exception {
        when(recommendService.getPersonalRecommendations(3))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/recommend/personal").param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(recommendService).getPersonalRecommendations(3);
    }

    @Test
    @DisplayName("管理员刷新热门榜单成功")
    @WithMockUser(roles = "ADMIN")
    void refreshHot_success() throws Exception {
        mockMvc.perform(post("/api/recommend/admin/hot/refresh")
                        .param("campusId", "2")
                        .param("topN", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(recommendService).refreshHotRanking(2L, 30);
    }
}
