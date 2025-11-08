package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.service.SoftDeleteAdminService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = SoftDeleteAdminController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("SoftDeleteAdminController 权限校验测试")
class SoftDeleteAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SoftDeleteAdminService softDeleteAdminService;

    @Test
    @DisplayName("管理员查询支持的软删除实体列表成功")
    @WithMockUser(roles = "ADMIN")
    void listTargets_adminSuccess() throws Exception {
        when(softDeleteAdminService.listTargets()).thenReturn(List.of("post", "reply"));

        mockMvc.perform(get("/api/admin/soft-delete/targets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0]").value("post"));

        verify(softDeleteAdminService).listTargets();
    }

    @Test
    @DisplayName("非管理员查询实体列表被拒绝")
    @WithMockUser(roles = "STUDENT")
    void listTargets_forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/soft-delete/targets"))
                .andExpect(status().isForbidden());

        verify(softDeleteAdminService, never()).listTargets();
    }

    @Test
    @DisplayName("管理员恢复软删除记录成功")
    @WithMockUser(roles = "ADMIN")
    void restore_adminSuccess() throws Exception {
        mockMvc.perform(post("/api/admin/soft-delete/{entity}/{id}/restore", "post", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(softDeleteAdminService).restore("post", 100L);
    }

    @Test
    @DisplayName("非管理员恢复软删除记录被拒绝")
    @WithMockUser(roles = "STUDENT")
    void restore_forbidden() throws Exception {
        mockMvc.perform(post("/api/admin/soft-delete/{entity}/{id}/restore", "post", 100L))
                .andExpect(status().isForbidden());

        verify(softDeleteAdminService, never()).restore(anyString(), anyLong());
    }

    @Test
    @DisplayName("管理员彻底删除软删除记录成功")
    @WithMockUser(roles = "ADMIN")
    void purge_adminSuccess() throws Exception {
        mockMvc.perform(delete("/api/admin/soft-delete/{entity}/{id}/purge", "post", 200L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(softDeleteAdminService).purge("post", 200L);
    }

    @Test
    @DisplayName("非管理员彻底删除记录被拒绝")
    @WithMockUser(roles = "STUDENT")
    void purge_forbidden() throws Exception {
        mockMvc.perform(delete("/api/admin/soft-delete/{entity}/{id}/purge", "post", 200L))
                .andExpect(status().isForbidden());

        verify(softDeleteAdminService, never()).purge(anyString(), anyLong());
    }
}
