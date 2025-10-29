package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.request.CreateRoleRequest;
import com.campus.marketplace.common.dto.request.UpdateRoleRequest;
import com.campus.marketplace.common.dto.request.UpdateUserRolesRequest;
import com.campus.marketplace.common.dto.response.RoleDetailResponse;
import com.campus.marketplace.common.dto.response.RoleSummaryResponse;
import com.campus.marketplace.common.entity.Permission;
import com.campus.marketplace.common.entity.Role;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.security.RoleDefinition;
import com.campus.marketplace.repository.PermissionRepository;
import com.campus.marketplace.repository.RoleRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色管理服务实现
 *
 * @author BaSui
 * @date 2025-10-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private static final Set<String> BUILT_IN_ROLE_NAMES = Arrays.stream(RoleDefinition.values())
            .map(RoleDefinition::getRoleName)
            .collect(Collectors.toUnmodifiableSet());

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public RoleDetailResponse createRole(CreateRoleRequest request) {
        String roleName = normalizeRoleName(request.name());

        if (roleRepository.existsByName(roleName)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "角色已存在：" + roleName);
        }

        Set<Permission> permissions = resolvePermissions(request.permissions());

        Role role = Role.builder()
                .name(roleName)
                .description(StringUtils.hasText(request.description()) ? request.description().trim() : null)
                .build();

        permissions.forEach(role::addPermission);
        Role saved = roleRepository.save(role);

        return toDetailResponse(saved, 0);
    }

    @Override
    @Transactional
    public RoleDetailResponse updateRole(Long roleId, UpdateRoleRequest request) {
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND, "角色不存在"));

        if (StringUtils.hasText(request.description())) {
            role.setDescription(request.description().trim());
        } else if (request.description() != null) {
            role.setDescription(null);
        }

        if (request.permissions() != null) {
            Set<Permission> targetPermissions = resolvePermissions(request.permissions());
            Set<Permission> current = new LinkedHashSet<>(role.getPermissions());

            for (Permission permission : current) {
                if (!targetPermissions.contains(permission)) {
                    role.removePermission(permission);
                }
            }
            for (Permission permission : targetPermissions) {
                if (!role.getPermissions().contains(permission)) {
                    role.addPermission(permission);
                }
            }
        }

        Role saved = roleRepository.save(role);
        long userCount = userRepository.countByRoles_Name(saved.getName());
        return toDetailResponse(saved, userCount);
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND, "角色不存在"));

        if (BUILT_IN_ROLE_NAMES.contains(role.getName())) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "内置角色不可删除");
        }

        long userCount = userRepository.countByRoles_Name(role.getName());
        if (userCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "仍有用户绑定该角色，无法删除");
        }

        // 清空关联后删除
        new ArrayList<>(role.getPermissions()).forEach(role::removePermission);
        roleRepository.delete(role);
        log.info("删除角色成功: id={}, name={}", role.getId(), role.getName());
    }

    @Override
    public RoleDetailResponse getRole(Long roleId) {
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND, "角色不存在"));
        long userCount = userRepository.countByRoles_Name(role.getName());
        return toDetailResponse(role, userCount);
    }

    @Override
    public List<RoleSummaryResponse> listRoles() {
        List<Role> roles = roleRepository.findAllWithPermissions();
        Map<String, Long> userCounts = roles.stream()
                .collect(Collectors.toMap(Role::getName, r -> userRepository.countByRoles_Name(r.getName())));

        return roles.stream()
                .map(role -> new RoleSummaryResponse(
                        role.getId(),
                        role.getName(),
                        role.getDescription(),
                        role.getPermissions() != null ? role.getPermissions().size() : 0,
                        userCounts.getOrDefault(role.getName(), 0L),
                        BUILT_IN_ROLE_NAMES.contains(role.getName())
                ))
                .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
                .toList();
    }

    @Override
    @Transactional
    public void updateUserRoles(Long userId, UpdateUserRolesRequest request) {
        User user = userRepository.findByIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));

        Set<String> targetRoleNames = request.roles() == null
                ? Set.of()
                : request.roles().stream()
                .filter(Objects::nonNull)
                .map(this::normalizeRoleName)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<Role> targetRoles = new LinkedHashSet<>();
        for (String roleName : targetRoleNames) {
            Role role = roleRepository.findByNameWithPermissions(roleName)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND, "角色不存在：" + roleName));
            targetRoles.add(role);
        }

        user.getRoles().clear();
        targetRoles.forEach(user::addRole);
        userRepository.save(user);

        log.info("更新用户角色成功: userId={}, roles={}", userId, targetRoleNames);
    }

    private Set<Permission> resolvePermissions(Set<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return codes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(code -> permissionRepository.findByName(code)
                        .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PARAMETER, "权限不存在：" + code)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String normalizeRoleName(String rawName) {
        if (!StringUtils.hasText(rawName)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "角色名称不能为空");
        }
        String roleName = rawName.trim().toUpperCase(Locale.ROOT);
        if (!roleName.startsWith("ROLE_")) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "角色名称必须以 ROLE_ 开头");
        }
        if (roleName.length() > 50) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "角色名称长度不能超过 50 个字符");
        }
        return roleName;
    }

    private RoleDetailResponse toDetailResponse(Role role, long userCount) {
        Set<String> permissionCodes = role.getPermissions() == null
                ? Set.of()
                : role.getPermissions().stream()
                .map(Permission::getName)
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        LocalDateTime createdAt = null;
        try {
            createdAt = role.getCreatedAt();
        } catch (Exception ignored) {
        }

        return new RoleDetailResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                permissionCodes,
                userCount,
                createdAt
        );
    }
}

