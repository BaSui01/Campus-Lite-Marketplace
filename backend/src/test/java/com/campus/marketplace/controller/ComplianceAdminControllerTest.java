package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.entity.ComplianceAuditLog;
import com.campus.marketplace.common.entity.ComplianceWhitelist;
import com.campus.marketplace.common.enums.ComplianceAction;
import com.campus.marketplace.repository.ComplianceAuditLogRepository;
import com.campus.marketplace.repository.ComplianceWhitelistRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
        controllers = ComplianceAdminController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("ComplianceAdminController MockMvc 测试")
class ComplianceAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ComplianceWhitelistRepository whitelistRepository;

    @MockBean
    private ComplianceAuditLogRepository auditLogRepository;

    @Test
    @DisplayName("具备合规权限可新增白名单")
    @WithMockUser(authorities = "system:compliance:review")
    void addWhitelist_success() throws Exception {
        ComplianceWhitelist saved = ComplianceWhitelist.builder()
                .id(12L)
                .type("USER")
                .targetId(500L)
                .build();
        when(whitelistRepository.save(any(ComplianceWhitelist.class))).thenReturn(saved);

        mockMvc.perform(post("/admin/compliance/whitelist")
                        .param("type", "USER")
                        .param("targetId", "500")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(12))
                .andExpect(jsonPath("$.data.type").value("USER"))
                .andExpect(jsonPath("$.data.targetId").value(500));

        ArgumentCaptor<ComplianceWhitelist> captor = ArgumentCaptor.forClass(ComplianceWhitelist.class);
        verify(whitelistRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo("USER");
        assertThat(captor.getValue().getTargetId()).isEqualTo(500L);
    }

    @Test
    @DisplayName("无权限无法新增白名单")
    @WithMockUser(roles = "USER")
    void addWhitelist_forbidden() throws Exception {
        mockMvc.perform(post("/admin/compliance/whitelist")
                        .param("type", "USER")
                        .param("targetId", "1"))
                .andExpect(status().isForbidden());

        verify(whitelistRepository, never()).save(any());
    }

    @Test
    @DisplayName("移除白名单成功")
    @WithMockUser(authorities = "system:compliance:review")
    void removeWhitelist_success() throws Exception {
        mockMvc.perform(delete("/admin/compliance/whitelist/{id}", 20L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(whitelistRepository).deleteById(20L);
    }

    @Test
    @DisplayName("查询合规审计日志返回分页")
    @WithMockUser(authorities = "system:compliance:review")
    void listAudit_success() throws Exception {
        ComplianceAuditLog log = ComplianceAuditLog.builder()
                .id(1L)
                .scene("POST_CONTENT")
                .action(ComplianceAction.REVIEW)
                .targetType("POST")
                .targetId(10L)
                .createdAt(LocalDateTime.parse("2025-10-29T09:30:00"))
                .build();
        when(auditLogRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
                eq("POST"), eq(10L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(log)));

        mockMvc.perform(get("/admin/compliance/audit")
                        .param("targetType", "POST")
                        .param("targetId", "10")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].scene").value("POST_CONTENT"));

        verify(auditLogRepository).findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
                eq("POST"), eq(10L), eq(PageRequest.of(1, 5)));
    }
}
