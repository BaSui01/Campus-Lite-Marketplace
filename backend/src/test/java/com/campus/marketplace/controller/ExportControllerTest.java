package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.config.TestSecurityConfig;
import com.campus.marketplace.common.entity.ExportJob;
import com.campus.marketplace.service.ExportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ExportController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("ExportController MockMvc 测试")
class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExportService exportService;

    @Test
    @DisplayName("申请导出任务成功返回任务ID")
    void requestExport_success() throws Exception {
        when(exportService.requestExport("orders", "{\"dateFrom\":\"2025-01-01\"}")).thenReturn(321L);

        mockMvc.perform(post("/api/exports")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("type", "orders")
                        .param("params", "{\"dateFrom\":\"2025-01-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(321));

        verify(exportService).requestExport("orders", "{\"dateFrom\":\"2025-01-01\"}");
    }

    @Test
    @DisplayName("查询导出任务列表成功返回数据")
    void listJobs_success() throws Exception {
        ExportJob job = ExportJob.builder()
                .id(10L)
                .type("goods")
                .status("SUCCESS")
                .paramsJson("{\"campusId\":1}")
                .filePath("/tmp/export.csv")
                .fileSize(2048L)
                .requestedBy("alice")
                .createdAt(Instant.parse("2025-10-29T08:00:00Z"))
                .build();
        when(exportService.listMyJobs()).thenReturn(List.of(job));

        mockMvc.perform(get("/api/exports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].type").value("goods"))
                .andExpect(jsonPath("$.data[0].status").value("SUCCESS"));

        verify(exportService).listMyJobs();
    }

    @Test
    @DisplayName("取消导出任务成功")
    void cancelJob_success() throws Exception {
        mockMvc.perform(post("/api/exports/{id}/cancel", 55L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"));

        verify(exportService).cancel(55L);
    }

    @Test
    @DisplayName("下载导出文件成功返回字节流")
    void download_success() throws Exception {
        byte[] bytes = "csv-data".getBytes();
        when(exportService.download("token-abc")).thenReturn(bytes);

        mockMvc.perform(get("/api/exports/download/{token}", "token-abc"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=export.csv"))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(bytes));

        verify(exportService).download("token-abc");
    }
}
