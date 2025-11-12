package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.dto.request.CreateSubscriptionRequest;
import com.campus.marketplace.common.dto.response.SubscriptionResponse;
import com.campus.marketplace.service.SubscriptionService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = SubscriptionController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("SubscriptionController MockMvc 测试")
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubscriptionService subscriptionService;

    @Test
    @DisplayName("学生订阅关键词成功返回ID")
    @WithMockUser(roles = "STUDENT")
    void subscribe_success() throws Exception {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest("Mac", 1L);
        when(subscriptionService.subscribe(any(CreateSubscriptionRequest.class))).thenReturn(88L);

        mockMvc.perform(post("/subscribe")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(88));

        verify(subscriptionService).subscribe(request);
    }

    @Test
    @DisplayName("非学生角色订阅关键词被拒绝")
    @WithMockUser(roles = "ADMIN")
    void subscribe_forbidden() throws Exception {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest("耳机", null);

        mockMvc.perform(post("/subscribe")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(subscriptionService, never()).subscribe(any());
    }

    @Test
    @DisplayName("学生取消订阅成功")
    @WithMockUser(roles = "STUDENT")
    void unsubscribe_success() throws Exception {
        mockMvc.perform(delete("/subscribe/{id}", 55L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(subscriptionService).unsubscribe(55L);
    }

    @Test
    @DisplayName("学生查看订阅列表返回数据")
    @WithMockUser(roles = "STUDENT")
    void listSubscriptions_success() throws Exception {
        SubscriptionResponse response = SubscriptionResponse.builder()
                .id(1L)
                .keyword("ipad")
                .campusId(2L)
                .active(true)
                .createdAt(LocalDateTime.parse("2025-10-29T10:00:00"))
                .build();
        when(subscriptionService.listMySubscriptions()).thenReturn(List.of(response));

        mockMvc.perform(get("/subscribe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].keyword").value("ipad"));

        verify(subscriptionService).listMySubscriptions();
    }

    @Test
    @DisplayName("非学生角色无法查看订阅列表")
    @WithMockUser(roles = "TEACHER")
    void listSubscriptions_forbidden() throws Exception {
        mockMvc.perform(get("/subscribe"))
                .andExpect(status().isForbidden());

        verify(subscriptionService, never()).listMySubscriptions();
    }
}
