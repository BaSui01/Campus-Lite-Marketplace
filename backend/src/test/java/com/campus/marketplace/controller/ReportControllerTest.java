package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.dto.request.CreateReportRequest;
import com.campus.marketplace.common.dto.response.ReportResponse;
import com.campus.marketplace.common.enums.ReportStatus;
import com.campus.marketplace.common.enums.ReportType;
import com.campus.marketplace.service.ReportService;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ReportController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("ReportController MockMvc 测试")
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReportService reportService;

    @Test
    @DisplayName("学生创建举报成功")
    @WithMockUser(roles = "STUDENT")
    void createReport_success() throws Exception {
        CreateReportRequest request = new CreateReportRequest(ReportType.POST, 888L, "涉嫌广告");
        when(reportService.createReport(request)).thenReturn(1001L);

        mockMvc.perform(post("/reports")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1001));

        verify(reportService).createReport(request);
    }

    @Test
    @DisplayName("非学生角色创建举报被拒绝")
    @WithMockUser(roles = "USER")
    void createReport_forbidden() throws Exception {
        CreateReportRequest request = new CreateReportRequest(ReportType.USER, 123L, "恶意骚扰");

        mockMvc.perform(post("/reports")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isForbidden());

        verify(reportService, never()).createReport(any());
    }

    @Test
    @DisplayName("管理员查询待处理举报列表成功")
    @WithMockUser(authorities = "system:report:handle")
    void listPendingReports_success() throws Exception {
        ReportResponse pending = ReportResponse.builder()
                .id(1L)
                .reporterId(2L)
                .reporterName("alice")
                .targetType(ReportType.POST)
                .targetId(777L)
                .reason("虚假信息")
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.parse("2025-10-29T10:00:00"))
                .build();
        when(reportService.listPendingReports(0, 20))
                .thenReturn(new PageImpl<>(List.of(pending)));

        mockMvc.perform(get("/reports/pending")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].reason").value("虚假信息"));

        verify(reportService).listPendingReports(0, 20);
    }

    @Test
    @DisplayName("学生查询自己的举报记录成功")
    @WithMockUser(roles = "STUDENT")
    void listMyReports_success() throws Exception {
        ReportResponse myReport = ReportResponse.builder()
                .id(3L)
                .reporterId(5L)
                .reporterName("bob")
                .targetType(ReportType.USER)
                .targetId(9L)
                .reason("恶意骚扰")
                .status(ReportStatus.HANDLED)
                .createdAt(LocalDateTime.parse("2025-10-28T12:30:00"))
                .build();
        when(reportService.listMyReports(1, 10))
                .thenReturn(new PageImpl<>(List.of(myReport)));

        mockMvc.perform(get("/reports/my")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].status").value("HANDLED"));

        verify(reportService).listMyReports(1, 10);
    }

    @Test
    @DisplayName("管理员处理举报成功")
    @WithMockUser(authorities = "system:report:handle")
    void handleReport_success() throws Exception {
        mockMvc.perform(post("/reports/{id}/handle", 456L)
                        .param("approved", "true")
                        .param("handleResult", "删除帖子并警告用户"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"));

        verify(reportService).handleReport(456L, true, "删除帖子并警告用户");
    }

    @Test
    @DisplayName("无权限处理举报被拒绝")
    @WithMockUser(roles = "STUDENT")
    void handleReport_forbidden() throws Exception {
        mockMvc.perform(post("/reports/{id}/handle", 789L)
                        .param("approved", "false")
                        .param("handleResult", "无违规"))
                .andExpect(status().isForbidden());

        verify(reportService, never()).handleReport(anyLong(), anyBoolean(), anyString());
    }
}
