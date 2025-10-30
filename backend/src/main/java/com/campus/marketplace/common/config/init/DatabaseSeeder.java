package com.campus.marketplace.common.config.init;

import com.campus.marketplace.common.entity.Campus;
import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.common.entity.Permission;
import com.campus.marketplace.common.entity.Role;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.CampusStatus;
import com.campus.marketplace.common.enums.UserStatus;
import com.campus.marketplace.common.security.PermissionCodes;
import com.campus.marketplace.common.security.RoleDefinition;
import com.campus.marketplace.repository.CampusRepository;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.PermissionRepository;
import com.campus.marketplace.repository.RoleRepository;
import com.campus.marketplace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基础数据种子：在任何环境首次启动时填充必要数据（幂等）。
 * 
 * @author BaSui
 * @date 2025-10-29
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.init", name = "seed", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder {

    private static final Map<String, Set<String>> DEFAULT_USER_ROLE_ASSIGNMENTS = Map.ofEntries(
            Map.entry("admin", Set.of(RoleDefinition.SUPER_ADMIN.getRoleName(), RoleDefinition.ADMIN.getRoleName())),
            Map.entry("student1", Set.of(RoleDefinition.STUDENT.getRoleName(), RoleDefinition.USER.getRoleName())),
            Map.entry("student2", Set.of(RoleDefinition.STUDENT.getRoleName(), RoleDefinition.USER.getRoleName())),
            Map.entry("seller_north", Set.of(RoleDefinition.STUDENT.getRoleName(), RoleDefinition.USER.getRoleName())),
            Map.entry("seller_south", Set.of(RoleDefinition.STUDENT.getRoleName(), RoleDefinition.USER.getRoleName())),
            Map.entry("buyer_grad", Set.of(RoleDefinition.STUDENT.getRoleName(), RoleDefinition.USER.getRoleName())),
            Map.entry("security_manager", Set.of(RoleDefinition.SECURITY_MANAGER.getRoleName(), RoleDefinition.USER.getRoleName())),
            Map.entry("content_manager", Set.of(RoleDefinition.CONTENT_MANAGER.getRoleName(), RoleDefinition.USER.getRoleName())),
            Map.entry("operation_manager", Set.of(RoleDefinition.OPERATION_MANAGER.getRoleName(), RoleDefinition.USER.getRoleName())),
            Map.entry("compliance_officer", Set.of(RoleDefinition.COMPLIANCE_OFFICER.getRoleName(), RoleDefinition.USER.getRoleName())),
            Map.entry("campus_manager", Set.of(RoleDefinition.CAMPUS_MANAGER.getRoleName(), RoleDefinition.USER.getRoleName())),
            Map.entry("category_manager", Set.of(RoleDefinition.CATEGORY_MANAGER.getRoleName(), RoleDefinition.USER.getRoleName())),
            Map.entry("rate_limit_manager", Set.of(RoleDefinition.RATE_LIMIT_MANAGER.getRoleName(), RoleDefinition.USER.getRoleName())),
            Map.entry("analyst", Set.of(RoleDefinition.ANALYST.getRoleName(), RoleDefinition.USER.getRoleName())),
            Map.entry("support_agent", Set.of(RoleDefinition.SUPPORT_AGENT.getRoleName(), RoleDefinition.USER.getRoleName()))
    );

    private static final String DEFAULT_USER_PASSWORD = "password123";

    private static final List<DefaultUserSeed> DEFAULT_USERS = List.of(
            new DefaultUserSeed("student1", "student1@campus.edu", "20210001", "13800001011", 120, "DEFAULT"),
            new DefaultUserSeed("student2", "student2@campus.edu", "20210002", "13800001012", 100, "DEFAULT"),
            new DefaultUserSeed("seller_north", "seller_north@campus.edu", "20200001", "13800001001", 320, "NORTH"),
            new DefaultUserSeed("seller_south", "seller_south@campus.edu", "20200002", "13800001002", 210, "SOUTH"),
            new DefaultUserSeed("buyer_grad", "buyer_grad@campus.edu", "20190001", "13800001003", 560, "DEFAULT"),
            new DefaultUserSeed("security_manager", "security_manager@campus.edu", null, null, 0, "DEFAULT"),
            new DefaultUserSeed("content_manager", "content_manager@campus.edu", null, null, 0, "DEFAULT"),
            new DefaultUserSeed("operation_manager", "operation_manager@campus.edu", null, null, 0, "DEFAULT"),
            new DefaultUserSeed("compliance_officer", "compliance_officer@campus.edu", null, null, 0, "DEFAULT"),
            new DefaultUserSeed("campus_manager", "campus_manager@campus.edu", null, null, 0, "NORTH"),
            new DefaultUserSeed("category_manager", "category_manager@campus.edu", null, null, 0, "DEFAULT"),
            new DefaultUserSeed("rate_limit_manager", "rate_limit_manager@campus.edu", null, null, 0, "DEFAULT"),
            new DefaultUserSeed("analyst", "analyst@campus.edu", null, null, 0, "DEFAULT"),
            new DefaultUserSeed("support_agent", "support_agent@campus.edu", null, null, 0, "DEFAULT")
    );

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final CampusRepository campusRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;

    @Bean
    @Order(200)
    public ApplicationRunner seedBaseData() {
        return args -> transactionTemplate.execute(status -> {
            Map<String, Role> roles = seedRolesAndPermissions();
            Map<String, Campus> campuses = seedCampuses();
            seedCategories();
            seedAdminUser(roles, campuses.get("DEFAULT"));
            seedDefaultUsers(campuses);
            seedDefaultUserRoles(roles);
            return null;
        });
    }

    private Map<String, Role> seedRolesAndPermissions() {
        Map<String, Permission> permissionCache = ensurePermissions();
        Map<String, Role> roleCache = new LinkedHashMap<>();

        for (RoleDefinition definition : RoleDefinition.values()) {
            Role role = roleRepository.findByNameWithPermissions(definition.getRoleName())
                    .orElseGet(() -> roleRepository.save(
                            Role.builder()
                                    .name(definition.getRoleName())
                                    .description(definition.getDescription())
                                    .build()
                    ));

            boolean updated = false;
            if (role.getDescription() == null && definition.getDescription() != null) {
                role.setDescription(definition.getDescription());
                updated = true;
            }

            Set<String> requiredPermissions = definition.getPermissions();
            if (!requiredPermissions.isEmpty()) {
                Set<String> currentCodes = role.getPermissions().stream()
                        .map(Permission::getName)
                        .collect(Collectors.toSet());
                for (String code : requiredPermissions) {
                    if (currentCodes.contains(code)) {
                        continue;
                    }
                    Permission permission = permissionCache.get(code);
                    if (permission != null) {
                        role.addPermission(permission);
                        updated = true;
                    }
                }
            }

            if (updated) {
                role = roleRepository.save(role);
            }
            roleCache.put(role.getName(), role);
        }

        return roleCache;
    }

    private Map<String, Permission> ensurePermissions() {
        Map<String, Permission> cache = new LinkedHashMap<>();
        for (String code : PermissionCodes.allCodes()) {
            Permission permission = permissionRepository.findByName(code)
                    .orElseGet(() -> permissionRepository.save(
                            Permission.builder()
                                    .name(code)
                                    .description(PermissionCodes.descriptionOf(code))
                                    .build()
                    ));
            String description = PermissionCodes.descriptionOf(code);
            if (description != null && !description.equals(permission.getDescription())) {
                permission.setDescription(description);
                permission = permissionRepository.save(permission);
            }
            cache.put(code, permission);
        }
        return cache;
    }

    /**
     * 初始化校区数据
     * 🎯 创建 DEFAULT、NORTH、SOUTH 三个校区
     *
     * @return 校区代码 -> 校区实体的映射
     */
    private Map<String, Campus> seedCampuses() {
        Map<String, Campus> campusMap = new LinkedHashMap<>();

        // DEFAULT 校区
        Campus defaultCampus = campusRepository.findByCode("DEFAULT")
                .orElseGet(() -> campusRepository.save(Campus.builder()
                        .code("DEFAULT")
                        .name("默认校区")
                        .status(CampusStatus.ACTIVE)
                        .build()));
        campusMap.put("DEFAULT", defaultCampus);

        // NORTH 校区
        Campus northCampus = campusRepository.findByCode("NORTH")
                .orElseGet(() -> campusRepository.save(Campus.builder()
                        .code("NORTH")
                        .name("北校区")
                        .status(CampusStatus.ACTIVE)
                        .build()));
        campusMap.put("NORTH", northCampus);

        // SOUTH 校区
        Campus southCampus = campusRepository.findByCode("SOUTH")
                .orElseGet(() -> campusRepository.save(Campus.builder()
                        .code("SOUTH")
                        .name("南校区")
                        .status(CampusStatus.ACTIVE)
                        .build()));
        campusMap.put("SOUTH", southCampus);

        log.info("已初始化 {} 个校区", campusMap.size());
        return campusMap;
    }

    private void seedCategories() {
        List<String> names = List.of("数码电子", "图书教材", "运动户外", "服饰鞋包", "美妆个护", "生活用品");
        for (int i = 0; i < names.size(); i++) {
            final int sortOrder = i;
            final String n = names.get(i);
            categoryRepository.findByName(n).orElseGet(() -> categoryRepository.save(Category.builder()
                    .name(n)
                    .sortOrder(sortOrder)
                    .build()));
        }
    }

    private void seedAdminUser(Map<String, Role> roles, Campus defaultCampus) {
        Role superAdminRole = roles.get(RoleDefinition.SUPER_ADMIN.getRoleName());
        Role adminRole = roles.get(RoleDefinition.ADMIN.getRoleName());

        Optional<User> adminOptional = userRepository.findByUsernameWithRoles("admin");
        if (adminOptional.isPresent()) {
            User existing = adminOptional.get();
            boolean updated = false;
            if (superAdminRole != null && existing.getRoles().stream().noneMatch(r -> r.getName().equals(superAdminRole.getName()))) {
                existing.addRole(superAdminRole);
                updated = true;
            }
            if (adminRole != null && existing.getRoles().stream().noneMatch(r -> r.getName().equals(adminRole.getName()))) {
                existing.addRole(adminRole);
                updated = true;
            }
            if (updated) {
                userRepository.save(existing);
                log.info("为现有管理员账号补齐权限角色: username=admin");
            }
            return;
        }

        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .email("admin@example.com")
                .nickname("系统管理员")
                .campusId(defaultCampus != null ? defaultCampus.getId() : null)
                .status(UserStatus.ACTIVE)
                .build();

        if (adminRole != null) {
            admin.addRole(adminRole);
        }
        if (superAdminRole != null) {
            admin.addRole(superAdminRole);
        }

        userRepository.save(admin);
        log.info("已创建默认管理员账号: username=admin password=admin123");
    }

    /**
     * 初始化默认用户数据
     * 🎯 根据 DEFAULT_USERS 配置创建测试用户
     *
     * @param campuses 校区映射（用于关联用户到对应校区）
     */
    private void seedDefaultUsers(Map<String, Campus> campuses) {
        int createdCount = 0;
        int skippedCount = 0;

        for (DefaultUserSeed seed : DEFAULT_USERS) {
            // 检查用户是否已存在
            if (userRepository.findByUsername(seed.username()).isPresent()) {
                skippedCount++;
                continue;
            }

            // 获取对应校区
            Campus campus = campuses.get(seed.campusCode());
            if (campus == null) {
                log.warn("校区代码 {} 不存在，跳过创建用户 {}", seed.campusCode(), seed.username());
                skippedCount++;
                continue;
            }

            // 创建用户
            User user = User.builder()
                    .username(seed.username())
                    .password(passwordEncoder.encode(DEFAULT_USER_PASSWORD))
                    .email(seed.email())
                    .nickname(seed.username()) // 默认昵称为用户名
                    .studentId(seed.studentId())
                    .phone(seed.phone())
                    .points(seed.points())
                    .campusId(campus.getId())
                    .status(UserStatus.ACTIVE)
                    .build();

            userRepository.save(user);
            createdCount++;
        }

        log.info("已创建 {} 个默认用户，跳过 {} 个已存在用户", createdCount, skippedCount);
    }

    private void seedDefaultUserRoles(Map<String, Role> roles) {
        for (Map.Entry<String, Set<String>> entry : DEFAULT_USER_ROLE_ASSIGNMENTS.entrySet()) {
            String username = entry.getKey();
            Set<String> roleNames = entry.getValue();
            userRepository.findByUsernameWithRoles(username).ifPresent(user -> {
                boolean updated = false;
                Set<String> current = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
                for (String roleName : roleNames) {
                    if (current.contains(roleName)) {
                        continue;
                    }
                    Role role = roles.get(roleName);
                    if (role != null) {
                        user.addRole(role);
                        updated = true;
                    }
                }
                if (updated) {
                    userRepository.save(user);
                    log.info("为默认用户 {} 补齐角色: {}", username, roleNames);
                }
            });
        }
    }

    /**
     * 默认用户种子数据记录类
     * 🎯 用于简化初始化用户数据的配置
     *
     * @param username    用户名
     * @param email       邮箱
     * @param studentId   学号（可选）
     * @param phone       手机号（可选）
     * @param points      初始积分
     * @param campusCode  校区代码
     */
    private record DefaultUserSeed(
            String username,
            String email,
            String studentId,
            String phone,
            Integer points,
            String campusCode
    ) {
    }
}
