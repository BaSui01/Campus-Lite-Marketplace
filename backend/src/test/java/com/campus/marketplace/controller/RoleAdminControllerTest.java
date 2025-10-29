package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CreateRoleRequest;
import com.campus.marketplace.common.dto.request.UpdateRoleRequest;
import com.campus.marketplace.common.dto.request.UpdateUserRolesRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.RoleDetailResponse;
import com.campus.marketplace.common.dto.response.RoleSummaryResponse;
import com.campus.marketplace.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 角色管理控制器单元测试
 *
 * @author BaSui
 * @date 2025-10-29
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RoleAdminController 测试")
class RoleAdminControllerTest {

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleAdminController controller;

    private RoleDetailResponse sampleDetail;
    private RoleSummaryResponse sampleSummary;

    @BeforeEach
    void setUp() {
        sampleDetail = new RoleDetailResponse(
                10L,
                "ROLE_TEST",
                "示例角色",
                Set.of("system:user:view"),
                2L,
                LocalDateTime.parse("2025-10-01T10:15:30")
        );
        sampleSummary = new RoleSummaryResponse(10L, "ROLE_TEST", "示例角色", 1, 2L, false);
    }

    @Test
    @DisplayName("列出角色成功")
    void listRoles_success() {
        when(roleService.listRoles()).thenReturn(List.of(sampleSummary));

        ApiResponse<List<RoleSummaryResponse>> response = controller.listRoles();

        assertEquals(200, response.getCode());
        assertEquals(1, response.getData().size());
        verify(roleService, times(1)).listRoles();
    }

    @Test
    @DisplayName("获取角色详情成功")
    void getRole_success() {
        when(roleService.getRole(10L)).thenReturn(sampleDetail);

        ApiResponse<RoleDetailResponse> response = controller.getRole(10L);

        assertEquals(200, response.getCode());
        assertEquals("ROLE_TEST", response.getData().name());
        verify(roleService).getRole(10L);
    }

    @Test
    @DisplayName("创建角色调用服务层")
    void createRole_delegatesToService() {
        CreateRoleRequest request = new CreateRoleRequest("ROLE_NEW", "新角色", Set.of("system:user:view"));
        when(roleService.createRole(request)).thenReturn(sampleDetail);

        ApiResponse<RoleDetailResponse> response = controller.createRole(request);

        assertEquals("ROLE_TEST", response.getData().name());
        verify(roleService).createRole(request);
    }

    @Test
    @DisplayName("更新角色调用服务层")
    void updateRole_delegatesToService() {
        UpdateRoleRequest request = new UpdateRoleRequest("更新描述", Set.of("system:user:update"));
        when(roleService.updateRole(10L, request)).thenReturn(sampleDetail);

        ApiResponse<RoleDetailResponse> response = controller.updateRole(10L, request);

        assertEquals(200, response.getCode());
        verify(roleService).updateRole(10L, request);
    }

    @Test
    @DisplayName("删除角色调用服务层")
    void deleteRole_delegatesToService() {
        ApiResponse<Void> response = controller.deleteRole(10L);

        assertEquals(200, response.getCode());
        verify(roleService).deleteRole(10L);
    }

    @Test
    @DisplayName("更新用户角色调用服务层")
    void updateUserRoles_delegatesToService() {
        UpdateUserRolesRequest request = new UpdateUserRolesRequest(Set.of("ROLE_USER"));

        ApiResponse<Void> response = controller.updateUserRoles(5L, request);

        assertEquals(200, response.getCode());
        verify(roleService).updateUserRoles(5L, request);
    }
}

