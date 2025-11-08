package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.dto.response.AuditLogResponse;
import com.campus.marketplace.common.enums.AuditActionType;
import com.campus.marketplace.service.AuditLogService;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuditLogController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("AuditLogController MockMvc 测试")
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditLogService auditLogService;

    @Test
    @DisplayName("具备权限的管理员可以查询审计日志")
    @WithMockUser(authorities = "system:audit:view")
    void listAuditLogs_success() throws Exception {
        AuditLogResponse log = AuditLogResponse.builder()
                .id(1L)
                .operatorId(200L)
                .operatorName("admin")
                .actionType(AuditActionType.POST_DELETE)
                .targetType("POST")
                .targetId(300L)
                .details("删除帖子")
                .result("SUCCESS")
                .ipAddress("127.0.0.1")
                .createdAt(LocalDateTime.parse("2025-10-29T10:00:00"))
                .build();
        when(auditLogService.listAuditLogs(100L, AuditActionType.POST_DELETE,
                LocalDateTime.parse("2025-10-01T00:00:00"),
                LocalDateTime.parse("2025-10-31T23:59:59"), 1, 10))
                .thenReturn(new PageImpl<>(List.of(log)));

        mockMvc.perform(get("/audit-logs")
                        .param("operatorId", "100")
                        .param("actionType", "POST_DELETE")
                        .param("startTime", "2025-10-01T00:00:00")
                        .param("endTime", "2025-10-31T23:59:59")
                        .param("page", "1")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].operatorName").value("admin"))
                .andExpect(jsonPath("$.data.content[0].actionType").value("POST_DELETE"));

        verify(auditLogService).listAuditLogs(100L, AuditActionType.POST_DELETE,
                LocalDateTime.parse("2025-10-01T00:00:00"),
                LocalDateTime.parse("2025-10-31T23:59:59"), 1, 10);
    }

    @Test
    @DisplayName("无审计权限的用户查询日志被拒绝")
    @WithMockUser(roles = "USER")
    void listAuditLogs_forbidden() throws Exception {
        mockMvc.perform(get("/audit-logs"))
                .andExpect(status().isForbidden());

        verify(auditLogService, never()).listAuditLogs(any(), any(), any(), any(), anyInt(), anyInt());
    }
}
