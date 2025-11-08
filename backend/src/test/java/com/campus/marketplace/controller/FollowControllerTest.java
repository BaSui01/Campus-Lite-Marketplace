package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.dto.response.FollowResponse;
import com.campus.marketplace.service.FollowService;
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
        controllers = FollowController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("FollowController MockMvc 测试")
class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FollowService followService;

    @Test
    @DisplayName("关注卖家成功")
    @WithMockUser(roles = "STUDENT")
    void follow_success() throws Exception {
        mockMvc.perform(post("/follow/{sellerId}", 5001L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(followService).followSeller(5001L);
    }

    @Test
    @DisplayName("非学生角色关注卖家返回403")
    @WithMockUser(roles = "TEACHER")
    void follow_forbidden() throws Exception {
        mockMvc.perform(post("/follow/{sellerId}", 6002L))
                .andExpect(status().isForbidden());

        verify(followService, never()).followSeller(any());
    }

    @Test
    @DisplayName("取消关注成功")
    @WithMockUser(roles = "STUDENT")
    void unfollow_success() throws Exception {
        mockMvc.perform(delete("/follow/{sellerId}", 7003L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(followService).unfollowSeller(7003L);
    }

    @Test
    @DisplayName("查询关注列表返回内容")
    @WithMockUser(roles = "STUDENT")
    void listFollowings_success() throws Exception {
        FollowResponse item = FollowResponse.builder()
                .sellerId(8004L)
                .sellerName("sellerA")
                .sellerAvatar("https://cdn/avatar.png")
                .followedAt(LocalDateTime.parse("2025-10-29T11:11:00"))
                .build();
        when(followService.listFollowings()).thenReturn(List.of(item));

        mockMvc.perform(get("/following").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].sellerId").value(8004))
                .andExpect(jsonPath("$.data[0].sellerName").value("sellerA"));
    }
}
