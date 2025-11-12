package com.campus.marketplace.common.config.init;

import com.campus.marketplace.common.entity.Campus;
import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.common.entity.Permission;
import com.campus.marketplace.common.entity.Role;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.CampusStatus;
import com.campus.marketplace.common.security.RoleDefinition;
import com.campus.marketplace.repository.*;
import com.campus.marketplace.repository.CampusRepository;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.PermissionRepository;
import com.campus.marketplace.repository.RoleRepository;
import com.campus.marketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DatabaseSeeder 核心逻辑测试")
class DatabaseSeederTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private CampusRepository campusRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TransactionTemplate transactionTemplate;

    // 💡 新增 Repository Mock（用于关联数据）
    @Mock
    private GoodsRepository goodsRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private FavoriteRepository favoriteRepository;
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private ReviewRepository reviewRepository;

    // 🎯 核心业务实体 Repository Mock
    @Mock
    private TagRepository tagRepository;
    @Mock
    private GoodsTagRepository goodsTagRepository;
    @Mock
    private PostTagRepository postTagRepository;
    @Mock
    private ReplyRepository replyRepository;
    @Mock
    private UserFollowRepository userFollowRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private ReportRepository reportRepository;
    @Mock
    private BanLogRepository banLogRepository;
    @Mock
    private PointsLogRepository pointsLogRepository;
    @Mock
    private RefundRequestRepository refundRequestRepository;

    private DatabaseSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new DatabaseSeeder(
                roleRepository,
                permissionRepository,
                campusRepository,
                categoryRepository,
                userRepository,
                passwordEncoder,
                transactionTemplate,
                // 💡 新增依赖注入
                goodsRepository,
                orderRepository,
                favoriteRepository,
                conversationRepository,
                messageRepository,
                postRepository,
                reviewRepository,
                // 🎯 核心业务实体依赖
                tagRepository,
                goodsTagRepository,
                postTagRepository,
                replyRepository,
                userFollowRepository,
                notificationRepository,
                reportRepository,
                banLogRepository,
                pointsLogRepository,
                refundRequestRepository
        );
    }

    @Test
    @DisplayName("已有管理员账号时补齐缺失角色并保存")
    void seedAdminUser_shouldAttachMissingRoles() {
        Role adminRole = Role.builder().name(RoleDefinition.ADMIN.getRoleName()).build();
        adminRole.setId(1L);
        Role superAdminRole = Role.builder().name(RoleDefinition.SUPER_ADMIN.getRoleName()).build();
        superAdminRole.setId(2L);
        Map<String, Role> roles = Map.of(
                RoleDefinition.ADMIN.getRoleName(), adminRole,
                RoleDefinition.SUPER_ADMIN.getRoleName(), superAdminRole
        );

        Campus defaultCampus = Campus.builder()
                .code("DEFAULT")
                .name("默认校区")
                .status(CampusStatus.ACTIVE)
                .build();
        defaultCampus.setId(100L);

        User admin = User.builder()
                .username("admin")
                .roles(new HashSet<>())
                .build();

        when(userRepository.findByUsernameWithRoles("admin")).thenReturn(Optional.of(admin));

        ReflectionTestUtils.invokeMethod(seeder, "seedAdminUser", roles, defaultCampus);

        assertThat(admin.getRoles()).containsExactlyInAnyOrder(adminRole, superAdminRole);
        verify(userRepository).save(admin);
    }

    @Test
    @DisplayName("没有管理员账号时创建默认管理员")
    void seedAdminUser_shouldCreateAdminWhenMissing() {
        Role adminRole = Role.builder().name(RoleDefinition.ADMIN.getRoleName()).build();
        adminRole.setId(1L);
        Role superAdminRole = Role.builder().name(RoleDefinition.SUPER_ADMIN.getRoleName()).build();
        superAdminRole.setId(2L);
        Map<String, Role> roles = Map.of(
                RoleDefinition.ADMIN.getRoleName(), adminRole,
                RoleDefinition.SUPER_ADMIN.getRoleName(), superAdminRole
        );

        Campus defaultCampus = Campus.builder()
                .code("DEFAULT")
                .name("默认校区")
                .status(CampusStatus.ACTIVE)
                .build();
        defaultCampus.setId(100L);

        when(userRepository.findByUsernameWithRoles("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("admin123")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ReflectionTestUtils.invokeMethod(seeder, "seedAdminUser", roles, defaultCampus);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User created = captor.getValue();

        assertThat(created.getUsername()).isEqualTo("admin");
        assertThat(created.getPassword()).isEqualTo("ENCODED");
        assertThat(created.getCampusId()).isEqualTo(defaultCampus.getId());
        assertThat(created.getRoles()).contains(adminRole, superAdminRole);
    }

    @Test
    @DisplayName("ensurePermissions 会在描述缺失时更新权限")
    void ensurePermissions_shouldEnsureDescription() {
        when(permissionRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(permissionRepository.save(any(Permission.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Permission> cache = ReflectionTestUtils.invokeMethod(seeder, "ensurePermissions");

        assertThat(cache).isNotEmpty();
        Permission permission = cache.get("system:user:view");
        assertThat(permission).isNotNull();
        assertThat(permission.getDescription()).isEqualTo("查看用户");
        verify(permissionRepository, atLeastOnce()).save(any(Permission.class));
    }

    @Test
    @DisplayName("seedRolesAndPermissions 会为角色补齐权限描述")
    void seedRolesAndPermissions_shouldAssignPermissions() {
        when(permissionRepository.findByName(anyString()))
                .thenAnswer(inv -> Optional.of(Permission.builder()
                        .name(inv.getArgument(0))
                        .description(null)
                        .build()));
        when(permissionRepository.save(any(Permission.class))).thenAnswer(inv -> inv.getArgument(0));
        Role existingRole = Role.builder().name(RoleDefinition.ADMIN.getRoleName()).permissions(new HashSet<>()).build();
        when(roleRepository.findByNameWithPermissions(eq(RoleDefinition.ADMIN.getRoleName())))
                .thenReturn(Optional.of(existingRole));
        when(roleRepository.findByNameWithPermissions(argThat(name -> !name.equals(RoleDefinition.ADMIN.getRoleName()))))
                .thenAnswer(inv -> Optional.of(Role.builder()
                        .name(inv.getArgument(0))
                        .permissions(new HashSet<>())
                        .build()));
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Role> result = ReflectionTestUtils.invokeMethod(seeder, "seedRolesAndPermissions");

        assertThat(result).containsKey(RoleDefinition.ADMIN.getRoleName());
        assertThat(existingRole.getPermissions()).isNotEmpty();
        verify(roleRepository, atLeastOnce()).save(any(Role.class));
    }

    @Test
    @DisplayName("seedCampuses 会创建缺失校区并返回映射")
    void seedCampuses_shouldCreateMissingCampuses() {
        when(campusRepository.findByCode("DEFAULT")).thenReturn(Optional.empty());
        when(campusRepository.findByCode("NORTH")).thenReturn(Optional.empty());
        when(campusRepository.findByCode("SOUTH")).thenReturn(Optional.of(Campus.builder().code("SOUTH").name("South").status(CampusStatus.ACTIVE).build()));
        when(campusRepository.save(any(Campus.class))).thenAnswer(inv -> {
            Campus campus = inv.getArgument(0);
            campus.setId((long) campus.getCode().hashCode());
            return campus;
        });

        Map<String, Campus> campuses = ReflectionTestUtils.invokeMethod(seeder, "seedCampuses");

        assertThat(campuses).containsKeys("DEFAULT", "NORTH", "SOUTH");
        verify(campusRepository, times(2)).save(any(Campus.class));
    }

    @Test
    @DisplayName("seedCategories 会保存默认分类")
    void seedCategories_shouldInsertDefaults() {
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        ReflectionTestUtils.invokeMethod(seeder, "seedCategories");

        verify(categoryRepository, atLeastOnce()).save(any(Category.class));
    }

    @Test
    @DisplayName("seedDefaultUsers 依据校区创建缺失用户并跳过已存在/无校区")
    void seedDefaultUsers_shouldRespectCampusMapping() {
        Campus defaultCampus = Campus.builder().code("DEFAULT").name("默认").status(CampusStatus.ACTIVE).build();
        defaultCampus.setId(1L);
        Campus northCampus = Campus.builder().code("NORTH").name("北校区").status(CampusStatus.ACTIVE).build();
        northCampus.setId(2L);
        Map<String, Campus> campusMap = new HashMap<>();
        campusMap.put("DEFAULT", defaultCampus);
        campusMap.put("NORTH", northCampus);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername("student2")).thenReturn(Optional.of(new User()));
        when(passwordEncoder.encode(anyString())).thenReturn("ENCODED");

        ReflectionTestUtils.invokeMethod(seeder, "seedDefaultUsers", campusMap);

        verify(userRepository, atLeastOnce()).save(any(User.class));
        verify(passwordEncoder, atLeastOnce()).encode(anyString());
    }

    @Test
    @DisplayName("seedDefaultUserRoles 会为用户补齐缺失角色")
    void seedDefaultUserRoles_shouldAttachRoles() {
        Role studentRole = Role.builder().name(RoleDefinition.STUDENT.getRoleName()).build();
        Map<String, Role> readyRoles = Map.of(RoleDefinition.STUDENT.getRoleName(), studentRole);
        User user = User.builder().username("student1").roles(new HashSet<>()).build();

        when(userRepository.findByUsernameWithRoles(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsernameWithRoles("student1")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        ReflectionTestUtils.invokeMethod(seeder, "seedDefaultUserRoles", readyRoles);

        assertThat(user.getRoles()).contains(studentRole);
        verify(userRepository).save(user);
    }
}
