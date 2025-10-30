package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.config.TestSecurityConfig;
import com.campus.marketplace.common.dto.request.CampusMigrationRequest;
import com.campus.marketplace.common.dto.request.CreateCampusRequest;
import com.campus.marketplace.common.dto.request.UpdateCampusRequest;
import com.campus.marketplace.common.dto.response.CampusMigrationValidationResponse;
import com.campus.marketplace.common.entity.Campus;
import com.campus.marketplace.common.enums.CampusStatus;
import com.campus.marketplace.service.CampusService;
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

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CampusController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("CampusController MockMvc 测试")
class CampusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CampusService campusService;

    @Test
    @DisplayName("具有校区管理权限时可以查询校区列表")
    @WithMockUser(authorities = "system:campus:manage")
    void list_success() throws Exception {
        Campus campus = Campus.builder().code("HZX").name("杭州校区").status(CampusStatus.ACTIVE).build();
        campus.setId(1L);
        when(campusService.listAll()).thenReturn(List.of(campus));

        mockMvc.perform(get("/api/admin/campuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].code").value("HZX"));
    }

    @Test
    @DisplayName("无校区管理权限时查询被禁止")
    @WithMockUser(roles = "USER")
    void list_forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/campuses"))
                .andExpect(status().isForbidden());

        verify(campusService, never()).listAll();
    }

    @Test
    @DisplayName("创建校区成功")
    @WithMockUser(authorities = "system:campus:manage")
    void create_success() throws Exception {
        CreateCampusRequest request = new CreateCampusRequest();
        request.setCode("SZ");
        request.setName("深圳校区");
        Campus created = Campus.builder().code("SZ").name("深圳校区").build();
        created.setId(10L);
        when(campusService.create("SZ", "深圳校区")).thenReturn(created);

        mockMvc.perform(post("/api/admin/campuses")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(10));

        verify(campusService).create("SZ", "深圳校区");
    }

    @Test
    @DisplayName("更新校区成功")
    @WithMockUser(authorities = "system:campus:manage")
    void update_success() throws Exception {
        UpdateCampusRequest request = new UpdateCampusRequest();
        request.setName("苏州校区");
        request.setStatus(CampusStatus.INACTIVE);
        Campus updated = Campus.builder().code("SUZ").name("苏州校区").status(CampusStatus.INACTIVE).build();
        updated.setId(3L);
        when(campusService.update(3L, "苏州校区", CampusStatus.INACTIVE)).thenReturn(updated);

        mockMvc.perform(put("/api/admin/campuses/{id}", 3L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));

        verify(campusService).update(3L, "苏州校区", CampusStatus.INACTIVE);
    }

    @Test
    @DisplayName("删除校区成功")
    @WithMockUser(authorities = "system:campus:manage")
    void delete_success() throws Exception {
        mockMvc.perform(delete("/api/admin/campuses/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(campusService).delete(5L);
    }

    @Test
    @DisplayName("校区迁移验证返回评估结果")
    @WithMockUser(authorities = "system:campus:manage")
    void validateMigration_success() throws Exception {
        CampusMigrationRequest request = new CampusMigrationRequest();
        request.setFromCampusId(1L);
        request.setToCampusId(2L);
        CampusMigrationValidationResponse response = CampusMigrationValidationResponse.builder()
                .fromCampusId(1L)
                .toCampusId(2L)
                .userCount(50)
                .allowed(true)
                .build();
        when(campusService.validateUserMigration(1L, 2L)).thenReturn(response);

        mockMvc.perform(post("/api/admin/campuses/migrate-users/validate")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowed").value(true))
                .andExpect(jsonPath("$.data.userCount").value(50));

        verify(campusService).validateUserMigration(1L, 2L);
    }

    @Test
    @DisplayName("执行校区迁移返回迁移数量")
    @WithMockUser(authorities = "system:campus:manage")
    void migrateUsers_success() throws Exception {
        CampusMigrationRequest request = new CampusMigrationRequest();
        request.setFromCampusId(3L);
        request.setToCampusId(4L);
        when(campusService.migrateUsers(3L, 4L)).thenReturn(25);

        mockMvc.perform(post("/api/admin/campuses/migrate-users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(25));

        verify(campusService).migrateUsers(3L, 4L);
    }
}
