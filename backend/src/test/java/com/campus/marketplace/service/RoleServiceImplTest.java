package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateRoleRequest;
import com.campus.marketplace.common.dto.request.UpdateRoleRequest;
import com.campus.marketplace.common.dto.request.UpdateUserRolesRequest;
import com.campus.marketplace.common.dto.response.RoleDetailResponse;
import com.campus.marketplace.common.dto.response.RoleSummaryResponse;
import com.campus.marketplace.common.entity.Permission;
import com.campus.marketplace.common.entity.Role;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.repository.PermissionRepository;
import com.campus.marketplace.repository.RoleRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.impl.RoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 角色管理服务单元测试
 *
 * @author BaSui
 * @date 2025-10-29
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RoleServiceImpl 测试")
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Permission viewPermission;
    private Permission editPermission;

    @BeforeEach
    void setUp() {
        viewPermission = Permission.builder().id(1L).name("system:user:view").build();
        editPermission = Permission.builder().id(2L).name("system:user:update").build();
    }

    @Test
    @DisplayName("创建角色成功")
    void createRole_success() {
        CreateRoleRequest request = new CreateRoleRequest("ROLE_AUDITOR", "审核员", Set.of(viewPermission.getName()));

        when(roleRepository.existsByName("ROLE_AUDITOR")).thenReturn(false);
        when(permissionRepository.findByName(viewPermission.getName())).thenReturn(java.util.Optional.of(viewPermission));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            role.setId(100L);
            return role;
        });

        RoleDetailResponse response = roleService.createRole(request);

        assertNotNull(response);
        assertEquals("ROLE_AUDITOR", response.name());
        assertEquals(Set.of(viewPermission.getName()), response.permissions());
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    @DisplayName("创建角色 - 重复名称抛异常")
    void createRole_duplicateName() {
        CreateRoleRequest request = new CreateRoleRequest("ROLE_ADMIN", "重复", Set.of());

        when(roleRepository.existsByName("ROLE_ADMIN")).thenReturn(true);

        assertThrows(BusinessException.class, () -> roleService.createRole(request));
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("更新角色 - 替换权限集合")
    void updateRole_replacePermissions() {
        Role role = Role.builder()
                .id(200L)
                .name("ROLE_EDITOR")
                .description("编辑员")
                .permissions(new java.util.LinkedHashSet<>(Set.of(viewPermission)))
                .build();

        UpdateRoleRequest request = new UpdateRoleRequest("内容编辑员", Set.of(editPermission.getName()));

        when(roleRepository.findByIdWithPermissions(200L)).thenReturn(java.util.Optional.of(role));
        when(permissionRepository.findByName(editPermission.getName())).thenReturn(java.util.Optional.of(editPermission));
        when(roleRepository.save(role)).thenReturn(role);
        when(userRepository.countByRoles_Name("ROLE_EDITOR")).thenReturn(3L);

        RoleDetailResponse response = roleService.updateRole(200L, request);

        assertEquals("内容编辑员", response.description());
        assertEquals(Set.of(editPermission.getName()), response.permissions());
        assertEquals(3L, response.userCount());

        verify(roleRepository).save(role);
    }

    @Test
    @DisplayName("删除角色 - 内置角色禁止删除")
    void deleteRole_builtinRole() {
        Role adminRole = Role.builder()
                .id(1L)
                .name("ROLE_ADMIN")
                .permissions(new java.util.LinkedHashSet<>())
                .build();

        when(roleRepository.findByIdWithPermissions(1L)).thenReturn(java.util.Optional.of(adminRole));

        BusinessException ex = assertThrows(BusinessException.class, () -> roleService.deleteRole(1L));
        assertTrue(ex.getMessage().contains("内置角色不可删除"));
    }

    @Test
    @DisplayName("更新用户角色成功")
    void updateUserRoles_success() {
        Role roleStudent = Role.builder().id(10L).name("ROLE_STUDENT").permissions(new java.util.LinkedHashSet<>()).build();
        User user = User.builder().id(500L).username("student1").roles(new java.util.LinkedHashSet<>()).build();

        when(userRepository.findByIdWithRolesAndPermissions(500L)).thenReturn(java.util.Optional.of(user));
        when(roleRepository.findByNameWithPermissions("ROLE_STUDENT")).thenReturn(java.util.Optional.of(roleStudent));
        when(roleRepository.findByNameWithPermissions("ROLE_USER")).thenReturn(java.util.Optional.of(Role.builder().id(11L).name("ROLE_USER").permissions(new java.util.LinkedHashSet<>()).build()));

        UpdateUserRolesRequest request = new UpdateUserRolesRequest(Set.of("role_student", "ROLE_USER"));
        roleService.updateUserRoles(500L, request);

        assertEquals(2, user.getRoles().size());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("列出角色 - 返回概览信息")
    void listRoles_success() {
        Role roleA = Role.builder()
                .id(1L)
                .name("ROLE_ALPHA")
                .description("Alpha")
                .permissions(new java.util.LinkedHashSet<>(Set.of(viewPermission)))
                .build();
        Role roleB = Role.builder()
                .id(2L)
                .name("ROLE_BETA")
                .permissions(new java.util.LinkedHashSet<>(Set.of(viewPermission, editPermission)))
                .build();

        when(roleRepository.findAllWithPermissions()).thenReturn(List.of(roleA, roleB));
        when(userRepository.countByRoles_Name("ROLE_ALPHA")).thenReturn(1L);
        when(userRepository.countByRoles_Name("ROLE_BETA")).thenReturn(0L);

        List<RoleSummaryResponse> list = roleService.listRoles();

        assertEquals(2, list.size());
        RoleSummaryResponse first = list.get(0);
        assertEquals("ROLE_ALPHA", first.name());
        assertEquals(1, first.permissionCount());
        assertEquals(1L, first.userCount());

        verify(userRepository, times(1)).countByRoles_Name("ROLE_ALPHA");
        verify(userRepository, times(1)).countByRoles_Name("ROLE_BETA");
    }
}
