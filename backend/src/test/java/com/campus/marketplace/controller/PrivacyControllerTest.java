package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.dto.request.CompletePrivacyRequest;
import com.campus.marketplace.common.dto.request.CreatePrivacyRequest;
import com.campus.marketplace.common.dto.response.PrivacyRequestResponse;
import com.campus.marketplace.common.enums.PrivacyRequestStatus;
import com.campus.marketplace.common.enums.PrivacyRequestType;
import com.campus.marketplace.common.security.PermissionCodes;
import com.campus.marketplace.service.PrivacyService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PrivacyController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("PrivacyController MockMvc 测试")
class PrivacyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PrivacyService privacyService;

    @Test
    @DisplayName("学生创建隐私请求成功返回ID")
    @WithMockUser(roles = "STUDENT")
    void createPrivacyRequest_success() throws Exception {
        CreatePrivacyRequest request = new CreatePrivacyRequest(PrivacyRequestType.EXPORT, "导出数据");
        when(privacyService.createRequest(any(CreatePrivacyRequest.class))).thenReturn(123L);

        mockMvc.perform(post("/privacy")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(123));

        verify(privacyService).createRequest(request);
    }

    @Test
    @DisplayName("非学生角色创建隐私请求被拒绝")
    @WithMockUser(roles = "TEACHER")
    void createPrivacyRequest_forbidden() throws Exception {
        CreatePrivacyRequest request = new CreatePrivacyRequest(PrivacyRequestType.DELETE, "删除数据");

        mockMvc.perform(post("/privacy")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(privacyService, never()).createRequest(any());
    }

    @Test
    @DisplayName("学生查询自己的隐私请求列表")
    @WithMockUser(roles = "STUDENT")
    void listMyRequests_success() throws Exception {
        PrivacyRequestResponse response = PrivacyRequestResponse.builder()
                .id(1L)
                .type(PrivacyRequestType.EXPORT)
                .status(PrivacyRequestStatus.PENDING)
                .createdAt(LocalDateTime.parse("2025-10-29T12:00:00"))
                .build();
        when(privacyService.listMyRequests()).thenReturn(List.of(response));

        mockMvc.perform(get("/privacy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].type").value("EXPORT"));

        verify(privacyService).listMyRequests();
    }

    @Test
    @DisplayName("管理员审核队列需要合规权限")
    @WithMockUser(roles = "ADMIN")
    void listPendingRequests_withoutAuthority_forbidden() throws Exception {
        mockMvc.perform(get("/admin/privacy/requests"))
                .andExpect(status().isForbidden());

        verify(privacyService, never()).listPendingRequests();
    }

    @Test
    @DisplayName("具备合规权限的管理员可以查看待处理列表")
    @WithMockUser(authorities = PermissionCodes.SYSTEM_COMPLIANCE_REVIEW)
    void listPendingRequests_success() throws Exception {
        PrivacyRequestResponse response = PrivacyRequestResponse.builder()
                .id(2L)
                .type(PrivacyRequestType.DELETE)
                .status(PrivacyRequestStatus.PENDING)
                .build();
        when(privacyService.listPendingRequests()).thenReturn(List.of(response));

        mockMvc.perform(get("/admin/privacy/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));

        verify(privacyService).listPendingRequests();
    }

    @Test
    @DisplayName("合规管理员可以完成隐私请求")
    @WithMockUser(authorities = PermissionCodes.SYSTEM_COMPLIANCE_REVIEW)
    void completeRequest_success() throws Exception {
        CompletePrivacyRequest request = new CompletePrivacyRequest("s3://exports/file.zip");

        mockMvc.perform(post("/admin/privacy/requests/{id}/complete", 900L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(privacyService).markCompleted(900L, "s3://exports/file.zip");
    }
}
