package com.campus.marketplace.common.config.init;

import com.campus.marketplace.common.entity.*;
import com.campus.marketplace.common.enums.CampusStatus;
import com.campus.marketplace.common.enums.UserStatus;
import com.campus.marketplace.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * 基础数据种子：在任何环境首次启动时填充必要数据（幂等）。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.init", name = "seed", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder {

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
        return args -> {
            transactionTemplate.execute(status -> {
                seedRolesAndPermissions();
                Campus campus = seedDefaultCampus();
                seedCategories();
                seedAdminUser(campus);
                return null;
            });
        };
    }

    private void seedRolesAndPermissions() {
        Role admin = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").description("系统管理员").build()));
        roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").description("普通用户").build()));

        List<String> perms = List.of(
                "system:user:view",
                "system:user:ban",
                "system:role:assign",
                "market:goods:approve",
                "market:order:refund"
        );
        for (String p : perms) {
            permissionRepository.findByName(p).orElseGet(() -> permissionRepository.save(Permission.builder().name(p).build()));
        }

        // 赋给管理员常用权限
        permissionRepository.findAll().forEach(admin::addPermission);
        roleRepository.save(admin);
    }

    private Campus seedDefaultCampus() {
        return campusRepository.findByCode("DEFAULT")
                .orElseGet(() -> campusRepository.save(Campus.builder()
                        .code("DEFAULT")
                        .name("默认校区")
                        .status(CampusStatus.ACTIVE)
                        .build()));
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

    private void seedAdminUser(Campus campus) {
        if (userRepository.existsByUsername("admin")) return;

        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .email("admin@example.com")
                .nickname("系统管理员")
                .campusId(campus.getId())
                .status(UserStatus.ACTIVE)
                .build();

        // 绑定管理员角色
        roleRepository.findByName("ROLE_ADMIN").ifPresent(admin::addRole);

        userRepository.save(admin);
        log.info("已创建默认管理员账号: username=admin password=admin123");
    }
}
