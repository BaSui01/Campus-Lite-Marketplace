package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.config.TestSecurityConfig;
import com.campus.marketplace.common.dto.response.UserProfileResponse;
import com.campus.marketplace.service.BlacklistService;
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
        controllers = BlacklistController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("BlacklistController MockMvc 测试")
class BlacklistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BlacklistService blacklistService;

    @Test
    @DisplayName("用户成功拉黑目标")
    @WithMockUser(roles = "USER")
    void addToBlacklist_success() throws Exception {
        mockMvc.perform(post("/api/blacklist/block/{blockedUserId}", 88L)
                        .param("reason", "spam")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(blacklistService).addToBlacklist(88L, "spam");
    }

    @Test
    @DisplayName("无 USER 角色无法拉黑")
    @WithMockUser(roles = "TEACHER")
    void addToBlacklist_forbidden() throws Exception {
        mockMvc.perform(post("/api/blacklist/block/{blockedUserId}", 99L))
                .andExpect(status().isForbidden());

        verify(blacklistService, never()).addToBlacklist(any(), any());
    }

    @Test
    @DisplayName("从黑名单移除成功")
    @WithMockUser(roles = "USER")
    void removeFromBlacklist_success() throws Exception {
        mockMvc.perform(delete("/api/blacklist/unblock/{blockedUserId}", 66L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(blacklistService).removeFromBlacklist(66L);
    }

    @Test
    @DisplayName("查询黑名单列表返回分页数据")
    @WithMockUser(roles = "USER")
    void listBlacklist_success() throws Exception {
        UserProfileResponse profile = UserProfileResponse.builder()
                .id(1L)
                .username("blockedUser")
                .build();
        when(blacklistService.listBlacklist(2, 5))
                .thenReturn(new PageImpl<>(List.of(profile)));

        mockMvc.perform(get("/api/blacklist/list")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].username").value("blockedUser"));
    }

    @Test
    @DisplayName("检查是否拉黑返回布尔值")
    @WithMockUser(roles = "USER")
    void isBlocked_success() throws Exception {
        when(blacklistService.isBlocked(77L)).thenReturn(true);

        mockMvc.perform(get("/api/blacklist/check/{blockedUserId}", 77L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(blacklistService).isBlocked(eq(77L));
    }
}
