package com.campus.marketplace.common.config.init;

import com.campus.marketplace.common.entity.*;
import com.campus.marketplace.common.enums.*;
import com.campus.marketplace.common.security.PermissionCodes;
import com.campus.marketplace.common.security.RoleDefinition;
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

import java.math.BigDecimal;
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

            // 💡 学生用户（DEFAULT校区）
            Map.entry("student1", Set.of(RoleDefinition.STUDENT.getRoleName(), RoleDefinition.USER.getRoleName())),
            Map.entry("student2", Set.of(RoleDefinition.STUDENT.getRoleName(), RoleDefinition.USER.getRoleName())),
            Map.entry("student3", Set.of(RoleDefinition.STUDENT.getRoleName(), RoleDefinition.USER.getRoleName())),
            Map.entry("student4", Set.of(RoleDefinition.STUDENT.getRoleName(), RoleDefinition.USER.getRoleName())),
            Map.entry("student5", Set.of(RoleDefinition.STUDENT.getRoleName(), RoleDefinition.USER.getRoleName())),

            // 🏪 卖家用户（NORTH校区）
            Map.entry("seller_north", Set.of(RoleDefinition.STUDENT.getRoleName(), RoleDefinition.USER.getRoleName())),
            Map.entry("seller_north2", Set.of(RoleDefinition.STUDENT.getRoleName(), RoleDefinition.USER.getRoleName())),
            Map.entry("seller_north3", Set.of(RoleDefinition.STUDENT.getRoleName(), RoleDefinition.USER.getRoleName())),

            // 🏪 卖家用户（SOUTH校区）
            Map.entry("seller_south", Set.of(RoleDefinition.STUDENT.getRoleName(), RoleDefinition.USER.getRoleName())),
            Map.entry("seller_south2", Set.of(RoleDefinition.STUDENT.getRoleName(), RoleDefinition.USER.getRoleName())),
            Map.entry("seller_south3", Set.of(RoleDefinition.STUDENT.getRoleName(), RoleDefinition.USER.getRoleName())),

            // 🎓 毕业生买家（DEFAULT校区）
            Map.entry("buyer_grad", Set.of(RoleDefinition.STUDENT.getRoleName(), RoleDefinition.USER.getRoleName())),
            Map.entry("buyer_grad2", Set.of(RoleDefinition.STUDENT.getRoleName(), RoleDefinition.USER.getRoleName())),

            // 🛡️ 管理员用户
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
            // 💡 学生用户（DEFAULT校区） - 普通活跃用户（使用真实头像）
            new DefaultUserSeed("student1", "student1@basui12.shop", "20210001", "13800001011", 120, "DEFAULT",
                    "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=400&h=400&fit=crop&q=80"),
            new DefaultUserSeed("student2", "student2@basui12.shop", "20210002", "13800001012", 100, "DEFAULT",
                    "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&h=400&fit=crop&q=80"),
            new DefaultUserSeed("student3", "student3@basui12.shop", "20210003", "13800001013", 80, "DEFAULT",
                    "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&h=400&fit=crop&q=80"),
            new DefaultUserSeed("student4", "student4@basui12.shop", "20210004", "13800001014", 150, "DEFAULT",
                    "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=400&h=400&fit=crop&q=80"),
            new DefaultUserSeed("student5", "student5@basui12.shop", "20210005", "13800001015", 200, "DEFAULT",
                    "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&h=400&fit=crop&q=80"),

            // 🏪 卖家用户（NORTH校区） - 活跃卖家（使用真实头像）
            new DefaultUserSeed("seller_north", "seller_north@basui12.shop", "20200001", "13800001001", 320, "NORTH",
                    "https://images.unsplash.com/photo-1519345182560-3f2917c472ef?w=400&h=400&fit=crop&q=80"),
            new DefaultUserSeed("seller_north2", "seller_north2@basui12.shop", "20200003", "13800001004", 280, "NORTH",
                    "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=400&h=400&fit=crop&q=80"),
            new DefaultUserSeed("seller_north3", "seller_north3@basui12.shop", "20200005", "13800001006", 400, "NORTH",
                    "https://images.unsplash.com/photo-1522529599102-193c0d76b5b6?w=400&h=400&fit=crop&q=80"),

            // 🏪 卖家用户（SOUTH校区） - 活跃卖家（使用真实头像）
            new DefaultUserSeed("seller_south", "seller_south@basui12.shop", "20200002", "13800001002", 210, "SOUTH",
                    "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=400&h=400&fit=crop&q=80"),
            new DefaultUserSeed("seller_south2", "seller_south2@basui12.shop", "20200004", "13800001005", 350, "SOUTH",
                    "https://images.unsplash.com/photo-1531123897727-8f129e1688ce?w=400&h=400&fit=crop&q=80"),
            new DefaultUserSeed("seller_south3", "seller_south3@basui12.shop", "20200006", "13800001007", 180, "SOUTH",
                    "https://images.unsplash.com/photo-1489424731084-a5d8b219a5bb?w=400&h=400&fit=crop&q=80"),

            // 🎓 毕业生买家（DEFAULT校区） - 高信誉买家（使用真实头像）
            new DefaultUserSeed("buyer_grad", "buyer_grad@basui12.shop", "20190001", "13800001003", 560, "DEFAULT",
                    "https://images.unsplash.com/photo-1463453091185-61582044d556?w=400&h=400&fit=crop&q=80"),
            new DefaultUserSeed("buyer_grad2", "buyer_grad2@basui12.shop", "20190002", "13800001008", 420, "DEFAULT",
                    "https://images.unsplash.com/photo-1506277886164-e25aa3f4ef7f?w=400&h=400&fit=crop&q=80"),

            // 🛡️ 管理员用户 - 各职能角色（使用真实头像）
            new DefaultUserSeed("security_manager", "security_manager@basui12.shop", null, null, 0, "DEFAULT",
                    "https://images.unsplash.com/photo-1556157382-97eda2d62296?w=400&h=400&fit=crop&q=80"),
            new DefaultUserSeed("content_manager", "content_manager@basui12.shop", null, null, 0, "DEFAULT",
                    "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=400&h=400&fit=crop&q=80"),
            new DefaultUserSeed("operation_manager", "operation_manager@basui12.shop", null, null, 0, "DEFAULT",
                    "https://images.unsplash.com/photo-1560250097-0b93528c311a?w=400&h=400&fit=crop&q=80"),
            new DefaultUserSeed("compliance_officer", "compliance_officer@basui12.shop", null, null, 0, "DEFAULT",
                    "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=400&h=400&fit=crop&q=80"),
            new DefaultUserSeed("campus_manager", "campus_manager@basui12.shop", null, null, 0, "NORTH",
                    "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=400&h=400&fit=crop&q=80"),
            new DefaultUserSeed("category_manager", "category_manager@basui12.shop", null, null, 0, "DEFAULT",
                    "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=400&h=400&fit=crop&q=80"),
            new DefaultUserSeed("rate_limit_manager", "rate_limit_manager@basui12.shop", null, null, 0, "DEFAULT",
                    "https://images.unsplash.com/photo-1507591064344-4c6ce005b128?w=400&h=400&fit=crop&q=80"),
            new DefaultUserSeed("analyst", "analyst@basui12.shop", null, null, 0, "DEFAULT",
                    "https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?w=400&h=400&fit=crop&q=80"),
            new DefaultUserSeed("support_agent", "support_agent@basui12.shop", null, null, 0, "DEFAULT",
                    "https://images.unsplash.com/photo-1554151228-14d9def656e4?w=400&h=400&fit=crop&q=80")
    );

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final CampusRepository campusRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;

    // 💡 新增 Repository 依赖（用于插入关联数据）
    private final GoodsRepository goodsRepository;
    private final OrderRepository orderRepository;
    private final FavoriteRepository favoriteRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final PostRepository postRepository;
    private final ReviewRepository reviewRepository;

    // 🎯 方案二：核心业务实体 Repository（BaSui 新增！）
    private final TagRepository tagRepository;
    private final GoodsTagRepository goodsTagRepository;
    private final PostTagRepository postTagRepository;
    private final ReplyRepository replyRepository;
    private final UserFollowRepository userFollowRepository;
    private final NotificationRepository notificationRepository;
    private final ReportRepository reportRepository;
    private final BanLogRepository banLogRepository;
    private final PointsLogRepository pointsLogRepository;
    private final RefundRequestRepository refundRequestRepository;

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

            // 🎉 插入关联数据（BaSui 新增！）
            log.info("开始插入关联数据（商品、订单、收藏、消息、帖子、评价）...");
            seedGoods(campuses);
            seedOrders();
            seedFavorites();
            seedConversationsAndMessages();
            seedPosts(campuses);
            seedReviews();
            log.info("关联数据插入完成！🎊");

            // 🚀 方案二：核心业务实体初始化（BaSui 新增！）
            log.info("开始插入核心业务数据（标签、关注、通知、举报、封禁、积分、退款）...");
            seedTags();
            seedGoodsTags();
            seedPostTags();
            seedReplies();
            seedFollows();
            seedNotifications();
            seedReports();
            seedBanLogs();
            seedPointsLogs();
            seedRefundRequests();
            log.info("核心业务数据插入完成！🎉");

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
                .email("admin@basui12.shop")
                .nickname("系统管理员")
                .avatar("https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=400&h=400&fit=crop&q=80") // 🖼️ 管理员头像
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
                    .avatar(seed.avatar()) // 🖼️ 头像URL
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
     * @param avatar      头像URL（可选）
     */
    private record DefaultUserSeed(
            String username,
            String email,
            String studentId,
            String phone,
            Integer points,
            String campusCode,
            String avatar
    ) {
    }

    // ==================== 🎉 BaSui 新增：关联数据插入方法 ====================

    /**
     * 初始化商品数据
     * 🛍️ 为每个卖家创建不同分类的商品（使用真实互联网图片）
     *
     * @param campuses 校区映射
     */
    private void seedGoods(Map<String, Campus> campuses) {
        // 🚫 幂等性检查：如果已有商品数据，跳过
        if (goodsRepository.count() > 0) {
            log.info("商品数据已存在，跳过插入");
            return;
        }

        // 📋 获取分类（假设顺序：数码电子、图书教材、运动户外、服饰鞋包、美妆个护、生活用品）
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            log.warn("分类数据不存在，无法创建商品");
            return;
        }

        int createdCount = 0;

        // ==================== 📱 数码电子类商品 ====================
        // DEFAULT校区 - student1
        createdCount += createGoodsForUser("student1", categories.get(0).getId(), campuses.get("DEFAULT").getId(),
                "全新iPhone 14 Pro 256GB", "全新未拆封，深空黑色，支持当面交易，原装配件齐全", new BigDecimal("5999.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1678652197950-91265b8005f3?w=800&h=800&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1678652197950-91265b8005f3?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1678685888221-cda773a3dcdb?w=800&h=600&fit=crop&q=80"
                });
        
        // NORTH校区 - seller_north
        createdCount += createGoodsForUser("seller_north", categories.get(0).getId(), campuses.get("NORTH").getId(),
                "MacBook Air M2 2023款", "自用半年，99新，附赠鼠标键盘，性能强劲", new BigDecimal("7800.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1484788984921-03950022c9ef?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("seller_south", categories.get(0).getId(), campuses.get("SOUTH").getId(),
                "iPad Pro 11寸 2022款", "128GB，配Apple Pencil 2代，适合学习办公", new BigDecimal("4500.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1585790050230-5dd28404f05d?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1561154464-82e9adf32764?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("seller_north2", categories.get(0).getId(), campuses.get("NORTH").getId(),
                "华为 MatePad Pro 12.6", "8GB+256GB，鸿蒙系统，学习娱乐两不误", new BigDecimal("3200.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1585790050230-5dd28404f05d?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("student2", categories.get(0).getId(), campuses.get("DEFAULT").getId(),
                "小米13 Ultra 白色512GB", "八成新，徕卡三摄拍照神器，电池健康度95%", new BigDecimal("4200.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("student3", categories.get(0).getId(), campuses.get("SOUTH").getId(),
                "AirPods Pro 2代", "九成新，主动降噪，附赠硅胶保护套", new BigDecimal("1280.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1606220945770-b5b6c2c55bf1?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1572569511254-d8f925fe2cbb?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("seller_south2", categories.get(0).getId(), campuses.get("SOUTH").getId(),
                "戴尔 XPS 13 笔记本", "i7-1165G7/16GB/512GB，轻薄便携，适合编程", new BigDecimal("5600.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1587614382346-4ec70e388b28?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800&h=600&fit=crop&q=80"
                });

        // ==================== 📚 图书教材类商品 ====================
        createdCount += createGoodsForUser("student1", categories.get(1).getId(), campuses.get("DEFAULT").getId(),
                "高等数学教材（第七版）", "九成新，无笔记无划痕，适合大一新生", new BigDecimal("25.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1532012197267-da84d127e765?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("seller_south", categories.get(1).getId(), campuses.get("SOUTH").getId(),
                "大学英语四级真题集", "全新未使用，包含近5年真题，送词汇手册", new BigDecimal("35.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("student2", categories.get(1).getId(), campuses.get("DEFAULT").getId(),
                "数据结构与算法分析（Java版）", "经典教材，有少量笔记批注，不影响阅读", new BigDecimal("42.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1519682337058-a94d519337bc?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("seller_north", categories.get(1).getId(), campuses.get("NORTH").getId(),
                "经济学原理（曼昆第7版）", "八成新，配套习题册一起出售", new BigDecimal("68.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1507842217343-583bb7270b66?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("student3", categories.get(1).getId(), campuses.get("SOUTH").getId(),
                "计算机网络（谢希仁第8版）", "全新，买错版本了，适合计算机专业学生", new BigDecimal("38.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=800&h=600&fit=crop&q=80"
                });

        // ==================== 🏃 运动户外类商品 ====================
        createdCount += createGoodsForUser("seller_north", categories.get(2).getId(), campuses.get("NORTH").getId(),
                "羽毛球拍（双拍）", "李宁正品，八成新，送12个球，适合校内运动", new BigDecimal("180.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1593786481097-5b5e2e6d3b1f?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("seller_south2", categories.get(2).getId(), campuses.get("SOUTH").getId(),
                "山地自行车", "捷安特，21速变速，适合校园代步和周末骑行", new BigDecimal("580.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1571068316344-75bc76f77890?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1576435728678-68d0fbf94e91?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("student1", categories.get(2).getId(), campuses.get("DEFAULT").getId(),
                "瑜伽垫套装", "8mm加厚防滑，送瑜伽砖和拉力带", new BigDecimal("88.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1599901860904-17e6ed7083a0?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("seller_north2", categories.get(2).getId(), campuses.get("NORTH").getId(),
                "哑铃套装 20KG", "可调节重量，宿舍健身神器", new BigDecimal("150.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1434682881908-b43d0467b798?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("student2", categories.get(2).getId(), campuses.get("DEFAULT").getId(),
                "篮球 斯伯丁74-602Y", "九成新，室内外通用，手感很好", new BigDecimal("120.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1546519638-68e109498ffc?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1608245449230-4ac19066d2d0?w=800&h=600&fit=crop&q=80"
                });

        // ==================== 👕 服饰鞋包类商品 ====================
        createdCount += createGoodsForUser("seller_north", categories.get(3).getId(), campuses.get("NORTH").getId(),
                "Nike Air Max 270 运动鞋", "42码，黑白配色，九成新，适合日常穿搭", new BigDecimal("450.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1491553895911-0055eca6402d?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("student3", categories.get(3).getId(), campuses.get("SOUTH").getId(),
                "Adidas 三叶草卫衣", "M码，黑色经典款，99新基本没穿", new BigDecimal("280.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1509631179647-0177331693ae?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("seller_south", categories.get(3).getId(), campuses.get("SOUTH").getId(),
                "New Balance 574 复古跑鞋", "US8码，灰色经典款，八成新", new BigDecimal("320.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1539185441755-769473a23570?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("student1", categories.get(3).getId(), campuses.get("DEFAULT").getId(),
                "优衣库羽绒服", "女款S码，红色，保暖效果好", new BigDecimal("380.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1539533018447-63fcce2678e3?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1548883354-7622d03aca27?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("seller_north2", categories.get(3).getId(), campuses.get("NORTH").getId(),
                "双肩包 北面背包", "30L容量，适合通勤和短途旅行", new BigDecimal("420.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1622560480605-d83c853bc5c3?w=800&h=600&fit=crop&q=80"
                });

        // ==================== 💄 美妆个护类商品 ====================
        createdCount += createGoodsForUser("seller_south", categories.get(4).getId(), campuses.get("SOUTH").getId(),
                "兰蔻小黑瓶精华液", "专柜正品，七成满，保质期至2026年", new BigDecimal("280.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1596755389378-c31d21fd1273?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("student2", categories.get(4).getId(), campuses.get("DEFAULT").getId(),
                "雅诗兰黛红石榴套装", "洗面奶+水+乳液，九成新", new BigDecimal("480.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1556228578-8c89e6adf883?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1571781926291-c477ebfd024b?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("student3", categories.get(4).getId(), campuses.get("SOUTH").getId(),
                "飞利浦电动牙刷", "钻石系列，使用半年，送4个刷头", new BigDecimal("350.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1607613009820-a29f7bb81c04?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1598256989800-fe5f95da9787?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("seller_north", categories.get(4).getId(), campuses.get("NORTH").getId(),
                "MAC口红套装", "5支正品口红，各色号齐全，八成新", new BigDecimal("520.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1631214524020-7e18db9a8f92?w=800&h=600&fit=crop&q=80"
                });

        // ==================== 🏠 生活用品类商品 ====================
        createdCount += createGoodsForUser("seller_north2", categories.get(5).getId(), campuses.get("NORTH").getId(),
                "宿舍小冰箱", "海尔品牌，使用1年，制冷效果好，静音", new BigDecimal("320.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1571175443880-49e1d25b2bc5?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1584568694244-14fbdf83bd30?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("student1", categories.get(5).getId(), campuses.get("DEFAULT").getId(),
                "小米台灯 1S", "护眼台灯，三档调光，适合学习", new BigDecimal("98.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1541280910158-c4e14f9c94a3?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("seller_south2", categories.get(5).getId(), campuses.get("SOUTH").getId(),
                "蓝牙音箱 JBL Flip5", "防水便携，音质出色，户外必备", new BigDecimal("480.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1545454675-3531b543be5d?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("student2", categories.get(5).getId(), campuses.get("DEFAULT").getId(),
                "美的电热水壶", "1.7L大容量，304不锈钢，快速烧水", new BigDecimal("65.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1595981234030-d316fc2c8161?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("seller_north", categories.get(5).getId(), campuses.get("NORTH").getId(),
                "宜家收纳箱套装", "3个大号收纳箱，宿舍整理必备", new BigDecimal("120.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1631679706909-1844bbd07221?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1600096194534-95cf5ece04cf?w=800&h=600&fit=crop&q=80"
                });
        
        createdCount += createGoodsForUser("student3", categories.get(5).getId(), campuses.get("SOUTH").getId(),
                "戴森吹风机 HD03", "九成新，三档风速，护发不毛躁", new BigDecimal("1580.00"), GoodsStatus.APPROVED,
                new String[]{
                        "https://images.unsplash.com/photo-1522337660859-02fbefca4702?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1626806787461-102c1bfaaea1?w=800&h=600&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1621609764180-2ca554a9d6f2?w=800&h=600&fit=crop&q=80"
                });

        log.info("已创建 {} 件商品（包含真实互联网图片URL）", createdCount);
    }

    /**
     * 为指定用户创建商品（支持图片）
     * 🎯 辅助方法，减少重复代码
     */
    private int createGoodsForUser(String username, Long categoryId, Long campusId,
                                   String title, String description, BigDecimal price, GoodsStatus status,
                                   String[] images) {
        return userRepository.findByUsername(username).map(seller -> {
            // 🎯 计算原价（如果有折扣）
            BigDecimal originalPrice = null;
            if (price.compareTo(new BigDecimal("1000")) > 0) {
                // 大于1000的商品，设置原价为当前价格的1.2倍（8折优惠）
                originalPrice = price.multiply(new BigDecimal("1.2")).setScale(2, java.math.RoundingMode.HALF_UP);
            }

            // 🎯 根据价格设置库存（模拟真实场景）
            int stock = price.compareTo(new BigDecimal("500")) > 0 ? 1 : (int)(Math.random() * 5) + 1;

            // 🎯 根据状态设置销量（已审核的商品可能有销量）
            int soldCount = status == GoodsStatus.APPROVED ? (int)(Math.random() * 10) : 0;

            // 🎯 根据标题和描述判断商品成色（前端需要）
            String condition;
            if (title.contains("全新") || description.contains("未拆封")) {
                condition = "BRAND_NEW";  // 全新
            } else if (description.contains("99新") || description.contains("九成新")) {
                condition = "LIKE_NEW";  // 几乎全新
            } else if (description.contains("八成新")) {
                condition = "LIGHTLY_USED";  // 轻微使用痕迹
            } else {
                condition = "WELL_USED";  // 明显使用痕迹
            }

            // 🎯 根据描述判断交易方式（前端需要）
            String deliveryMethod;
            if (description.contains("当面交易") || description.contains("面交") || description.contains("自提")) {
                deliveryMethod = "MEET";  // 校园面交
            } else if (description.contains("包邮") || description.contains("邮寄")) {
                deliveryMethod = "MAIL";  // 快递邮寄
            } else {
                deliveryMethod = "MEET,MAIL";  // 两种方式都支持
            }

            Goods goods = Goods.builder()
                    .title(title)
                    .description(description)
                    .price(price)
                    .originalPrice(originalPrice)  // ✅ 新增：原价
                    .condition(condition)  // 🆕 商品成色
                    .deliveryMethod(deliveryMethod)  // 🆕 交易方式
                    .categoryId(categoryId)
                    .sellerId(seller.getId())
                    .campusId(campusId)
                    .status(status)
                    .images(images)  // 🖼️ 商品图片URL数组
                    .viewCount(0)
                    .favoriteCount(0)
                    .stock(stock)  // ✅ 新增：库存
                    .soldCount(soldCount)  // ✅ 新增：销量
                    .build();
            goodsRepository.save(goods);
            return 1;
        }).orElse(0);
    }

    /**
     * 初始化订单数据
     * 📦 创建买卖双方的订单记录
     */
    private void seedOrders() {
        // 🚫 幂等性检查
        if (orderRepository.count() > 0) {
            log.info("订单数据已存在，跳过插入");
            return;
        }

        List<Goods> approvedGoods = goodsRepository.findByStatusWithSeller(GoodsStatus.APPROVED);
        if (approvedGoods.size() < 3) {
            log.warn("商品数量不足，无法创建订单");
            return;
        }

        int createdCount = 0;

        // 📦 buyer_grad 购买 student1 的商品
        createdCount += createOrder(approvedGoods.get(0), "buyer_grad", OrderStatus.COMPLETED);

        // 📦 student2 购买 seller_north 的商品
        createdCount += createOrder(approvedGoods.get(1), "student2", OrderStatus.PAID);

        // 📦 student3 购买 seller_south 的商品
        createdCount += createOrder(approvedGoods.get(2), "student3", OrderStatus.PENDING_PAYMENT);

        log.info("已创建 {} 个订单", createdCount);
    }

    /**
     * 为指定商品和买家创建订单
     */
    private int createOrder(Goods goods, String buyerUsername, OrderStatus status) {
        return userRepository.findByUsername(buyerUsername).map(buyer -> {
            com.campus.marketplace.common.entity.Order order = com.campus.marketplace.common.entity.Order.builder()
                    .orderNo("ORD" + System.currentTimeMillis() + (int)(Math.random() * 1000))
                    .goodsId(goods.getId())
                    .buyerId(buyer.getId())
                    .sellerId(goods.getSellerId())
                    .campusId(goods.getCampusId())
                    .amount(goods.getPrice())
                    .actualAmount(goods.getPrice())
                    .status(status)
                    .build();
            orderRepository.save(order);
            return 1;
        }).orElse(0);
    }

    /**
     * 初始化收藏数据
     * ❤️ 用户收藏自己喜欢的商品
     */
    private void seedFavorites() {
        // 🚫 幂等性检查
        if (favoriteRepository.count() > 0) {
            log.info("收藏数据已存在，跳过插入");
            return;
        }

        List<Goods> goods = goodsRepository.findAll();
        if (goods.size() < 3) {
            log.warn("商品数量不足，无法创建收藏");
            return;
        }

        int createdCount = 0;

        // ❤️ student1 收藏 seller_north 的商品
        createdCount += createFavorite("student1", goods.get(2).getId());

        // ❤️ student2 收藏 seller_south 的商品
        createdCount += createFavorite("student2", goods.get(5).getId());

        // ❤️ buyer_grad 收藏多个商品
        createdCount += createFavorite("buyer_grad", goods.get(0).getId());
        createdCount += createFavorite("buyer_grad", goods.get(1).getId());

        log.info("已创建 {} 条收藏记录", createdCount);
    }

    /**
     * 为指定用户和商品创建收藏
     */
    private int createFavorite(String username, Long goodsId) {
        return userRepository.findByUsername(username).map(user -> {
            if (!favoriteRepository.existsByUserIdAndGoodsId(user.getId(), goodsId)) {
                Favorite favorite = Favorite.builder()
                        .userId(user.getId())
                        .goodsId(goodsId)
                        .build();
                favoriteRepository.save(favorite);
                return 1;
            }
            return 0;
        }).orElse(0);
    }

    /**
     * 初始化会话和消息数据
     * 💬 用户之间的聊天记录
     */
    private void seedConversationsAndMessages() {
        // 🚫 幂等性检查
        if (conversationRepository.count() > 0) {
            log.info("会话数据已存在，跳过插入");
            return;
        }

        int conversationCount = 0;
        int messageCount = 0;

        // 💬 student1 和 seller_north 的会话
        Conversation conv1 = createConversation("student1", "seller_north");
        if (conv1 != null) {
            conversationCount++;
            messageCount += createMessage(conv1.getId(), "student1", "seller_north", "你好，MacBook还在吗？");
            messageCount += createMessage(conv1.getId(), "seller_north", "student1", "在的，可以当面交易");
        }

        // 💬 buyer_grad 和 seller_south 的会话
        Conversation conv2 = createConversation("buyer_grad", "seller_south");
        if (conv2 != null) {
            conversationCount++;
            messageCount += createMessage(conv2.getId(), "buyer_grad", "seller_south", "iPad能便宜点吗？");
            messageCount += createMessage(conv2.getId(), "seller_south", "buyer_grad", "已经很优惠了，包邮哦");
        }

        log.info("已创建 {} 个会话，{} 条消息", conversationCount, messageCount);
    }

    /**
     * 创建会话
     */
    private Conversation createConversation(String user1Name, String user2Name) {
        Optional<User> user1Opt = userRepository.findByUsername(user1Name);
        Optional<User> user2Opt = userRepository.findByUsername(user2Name);

        if (user1Opt.isPresent() && user2Opt.isPresent()) {
            Conversation conversation = Conversation.builder()
                    .user1Id(user1Opt.get().getId())
                    .user2Id(user2Opt.get().getId())
                    .build();
            return conversationRepository.save(conversation);
        }
        return null;
    }

    /**
     * 创建消息
     */
    private int createMessage(Long conversationId, String senderName, String receiverName, String content) {
        Optional<User> senderOpt = userRepository.findByUsername(senderName);
        Optional<User> receiverOpt = userRepository.findByUsername(receiverName);

        if (senderOpt.isPresent() && receiverOpt.isPresent()) {
            Message message = Message.builder()
                    .conversationId(conversationId)
                    .senderId(senderOpt.get().getId())
                    .receiverId(receiverOpt.get().getId())
                    .content(content)
                    .messageType(MessageType.TEXT)
                    .status(MessageStatus.UNREAD)
                    .build();
            messageRepository.save(message);
            return 1;
        }
        return 0;
    }

    /**
     * 初始化帖子数据
     * 📝 校园社区的帖子
     */
    private void seedPosts(Map<String, Campus> campuses) {
        // 🚫 幂等性检查
        if (postRepository.count() > 0) {
            log.info("帖子数据已存在，跳过插入");
            return;
        }

        int createdCount = 0;

        // 📝 student1 的帖子
        createdCount += createPost("student1", campuses.get("DEFAULT").getId(),
                "求购二手自行车", "本人急需一辆自行车代步，预算500左右，有的同学请联系我！");

        // 📝 seller_north 的帖子
        createdCount += createPost("seller_north", campuses.get("NORTH").getId(),
                "北校区跳蚤市场开张啦", "本周六上午9点，北校区操场举办跳蚤市场，欢迎大家来淘宝！");

        // 📝 buyer_grad 的帖子
        createdCount += createPost("buyer_grad", campuses.get("DEFAULT").getId(),
                "毕业季大甩卖", "即将毕业，宿舍物品大甩卖，有需要的同学私聊我");

        log.info("已创建 {} 个帖子", createdCount);
    }

    /**
     * 创建帖子
     */
    private int createPost(String authorName, Long campusId, String title, String content) {
        return userRepository.findByUsername(authorName).map(author -> {
            Post post = Post.builder()
                    .title(title)
                    .content(content)
                    .authorId(author.getId())
                    .campusId(campusId)
                    .status(GoodsStatus.APPROVED)
                    .viewCount(0)
                    .replyCount(0)
                    .likeCount(0)
                    .build();
            postRepository.save(post);
            return 1;
        }).orElse(0);
    }

    /**
     * 初始化评价数据
     * ⭐ 买家对已完成订单的评价
     * 🎯 为卖家创建不同评分的评价，让评分更真实
     */
    private void seedReviews() {
        // 🚫 幂等性检查
        if (reviewRepository.count() > 0) {
            log.info("评价数据已存在，跳过插入");
            return;
        }

        // 查找已完成的订单
        List<com.campus.marketplace.common.entity.Order> completedOrders = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .toList();

        if (completedOrders.isEmpty()) {
            log.warn("没有已完成的订单，无法创建评价");
            return;
        }

        int createdCount = 0;

        // 🎯 为已完成的订单创建评价（使用不同的评分）
        for (com.campus.marketplace.common.entity.Order order : completedOrders) {
            Review review = Review.builder()
                    .orderId(order.getId())
                    .buyerId(order.getBuyerId())
                    .sellerId(order.getSellerId())
                    .rating(5)
                    .qualityScore(5)
                    .serviceScore(5)
                    .deliveryScore(5)
                    .content("非常满意，卖家态度好，物品质量也很棒！")
                    .status(ReviewStatus.NORMAL)
                    .build();
            reviewRepository.save(review);
            createdCount++;
        }

        // 🎯 为主要卖家添加更多评价数据（让评分更真实）
        // seller_north: 4.8分（5条评价：4个5分，1个4分）
        createdCount += createReviewForSeller("seller_north", "student1", 5, "商品质量很好，卖家服务态度也很棒！");
        createdCount += createReviewForSeller("seller_north", "student2", 5, "物品和描述一致，非常满意！");
        createdCount += createReviewForSeller("seller_north", "student3", 5, "卖家很靠谱，推荐！");
        createdCount += createReviewForSeller("seller_north", "buyer_grad", 4, "商品不错，但交易时间有点晚");

        // seller_south: 4.6分（5条评价：3个5分，2个4分）
        createdCount += createReviewForSeller("seller_south", "student1", 5, "iPad很新，卖家人很好！");
        createdCount += createReviewForSeller("seller_south", "student2", 4, "商品还行，但包装有点简陋");
        createdCount += createReviewForSeller("seller_south", "student4", 5, "非常满意，下次还来！");
        createdCount += createReviewForSeller("seller_south", "buyer_grad", 4, "物品质量可以，价格稍贵");

        // student1: 4.5分（2条评价：1个5分，1个4分）
        createdCount += createReviewForSeller("student1", "student2", 5, "教材很新，价格实惠！");
        createdCount += createReviewForSeller("student1", "student3", 4, "书还不错，就是有点旧");

        log.info("已创建 {} 条评价", createdCount);
    }

    /**
     * 为指定卖家创建评价
     * 🎯 辅助方法，用于创建测试评价数据
     */
    private int createReviewForSeller(String sellerName, String buyerName, int rating, String content) {
        Optional<User> sellerOpt = userRepository.findByUsername(sellerName);
        Optional<User> buyerOpt = userRepository.findByUsername(buyerName);

        if (sellerOpt.isEmpty() || buyerOpt.isEmpty()) {
            return 0;
        }

        // 创建一个虚拟订单ID（用于评价关联）
        // 注意：这里为了简化，使用sellerId作为orderId的一部分
        // 实际生产环境中应该关联真实的订单
        Long virtualOrderId = sellerOpt.get().getId() * 1000 + buyerOpt.get().getId();

        Review review = Review.builder()
                .orderId(virtualOrderId)
                .buyerId(buyerOpt.get().getId())
                .sellerId(sellerOpt.get().getId())
                .rating(rating)
                .qualityScore(rating)
                .serviceScore(rating)
                .deliveryScore(rating)
                .content(content)
                .status(ReviewStatus.NORMAL)
                .build();
        reviewRepository.save(review);
        return 1;
    }

    // ==================== 🚀 方案二：核心业务实体初始化方法 ====================

    /**
     * 初始化标签数据
     * 🏷️ 创建常用的商品和帖子标签
     */
    private void seedTags() {
        // 🚫 幂等性检查
        if (tagRepository.count() > 0) {
            log.info("标签数据已存在，跳过插入");
            return;
        }

        List<String> tagNames = List.of(
                // 商品相关标签
                "全新", "九成新", "八成新", "包邮", "急售", "可议价",
                "自提", "面交", "二手", "正品", "原装",
                // 帖子相关标签
                "求购", "出售", "交流", "求助", "分享", "活动", "校园", "学习", "生活"
        );

        int createdCount = 0;
        for (String tagName : tagNames) {
            Tag tag = Tag.builder()
                    .name(tagName)
                    .description("系统预置标签：" + tagName)
                    .enabled(true)
                    .build();
            tagRepository.save(tag);
            createdCount++;
        }

        log.info("已创建 {} 个标签", createdCount);
    }

    /**
     * 初始化商品-标签关联
     * 🔗 为商品添加标签
     */
    private void seedGoodsTags() {
        // 🚫 幂等性检查
        if (goodsTagRepository.count() > 0) {
            log.info("商品-标签关联数据已存在，跳过插入");
            return;
        }

        List<Goods> goods = goodsRepository.findAll();
        List<Tag> tags = tagRepository.findAll();

        if (goods.isEmpty() || tags.isEmpty()) {
            log.warn("商品或标签数据不足，无法创建关联");
            return;
        }

        int createdCount = 0;

        // 为每个商品添加1-3个标签
        for (Goods g : goods) {
            // 根据商品状态和价格分配标签
            List<String> applicableTags = new java.util.ArrayList<>();

            if (g.getStatus() == GoodsStatus.APPROVED) {
                applicableTags.add("正品");
            }
            if (g.getPrice().compareTo(new BigDecimal("1000")) < 0) {
                applicableTags.add("可议价");
            }
            if (g.getTitle().contains("全新") || g.getDescription().contains("未拆封")) {
                applicableTags.add("全新");
            } else {
                applicableTags.add("二手");
            }

            // 创建关联
            for (String tagName : applicableTags) {
                Tag tag = tags.stream()
                        .filter(t -> t.getName().equals(tagName))
                        .findFirst()
                        .orElse(null);

                if (tag != null) {
                    GoodsTag goodsTag = GoodsTag.builder()
                            .goodsId(g.getId())
                            .tagId(tag.getId())
                            .build();
                    goodsTagRepository.save(goodsTag);
                    createdCount++;
                }
            }
        }

        log.info("已创建 {} 条商品-标签关联", createdCount);
    }

    /**
     * 初始化帖子-标签关联
     * 🔗 为帖子添加标签
     */
    private void seedPostTags() {
        // 🚫 幂等性检查
        if (postTagRepository.count() > 0) {
            log.info("帖子-标签关联数据已存在，跳过插入");
            return;
        }

        List<Post> posts = postRepository.findAll();
        List<Tag> tags = tagRepository.findAll();

        if (posts.isEmpty() || tags.isEmpty()) {
            log.warn("帖子或标签数据不足，无法创建关联");
            return;
        }

        int createdCount = 0;

        // 为每个帖子添加标签
        for (Post post : posts) {
            List<String> applicableTags = new java.util.ArrayList<>();

            if (post.getTitle().contains("求购")) {
                applicableTags.add("求购");
            } else if (post.getTitle().contains("出售") || post.getTitle().contains("甩卖")) {
                applicableTags.add("出售");
            }

            applicableTags.add("校园");
            applicableTags.add("生活");

            // 创建关联
            for (String tagName : applicableTags) {
                Tag tag = tags.stream()
                        .filter(t -> t.getName().equals(tagName))
                        .findFirst()
                        .orElse(null);

                if (tag != null) {
                    PostTag postTag = PostTag.builder()
                            .postId(post.getId())
                            .tagId(tag.getId())
                            .build();
                    postTagRepository.save(postTag);
                    createdCount++;
                }
            }
        }

        log.info("已创建 {} 条帖子-标签关联", createdCount);
    }

    /**
     * 初始化帖子回复数据
     * 💬 为帖子添加回复
     */
    private void seedReplies() {
        // 🚫 幂等性检查
        if (replyRepository.count() > 0) {
            log.info("回复数据已存在，跳过插入");
            return;
        }

        List<Post> posts = postRepository.findAll();
        if (posts.isEmpty()) {
            log.warn("帖子数据不足，无法创建回复");
            return;
        }

        int createdCount = 0;

        // 为第一个帖子添加回复
        if (posts.size() > 0) {
            Post post1 = posts.get(0);
            createdCount += createReply(post1.getId(), "student2", "我这里有一辆自行车，八成新，要吗？", null, null);
            createdCount += createReply(post1.getId(), post1.getAuthorId(), "可以啊，什么时候方便看车？",
                    findReplyByContent("我这里有一辆自行车"), "student2");
        }

        // 为第二个帖子添加回复
        if (posts.size() > 1) {
            Post post2 = posts.get(1);
            createdCount += createReply(post2.getId(), "buyer_grad", "时间地点收藏了！", null, null);
            createdCount += createReply(post2.getId(), "student3", "期待，会去逛逛！", null, null);
        }

        log.info("已创建 {} 条回复", createdCount);
    }

    /**
     * 创建回复
     */
    private int createReply(Long postId, Object authorIdentifier, String content, Long parentId, Object toUserIdentifier) {
        Long authorId;
        if (authorIdentifier instanceof String) {
            authorId = userRepository.findByUsername((String) authorIdentifier)
                    .map(User::getId)
                    .orElse(null);
        } else {
            authorId = (Long) authorIdentifier;
        }

        if (authorId == null) {
            return 0;
        }

        Long toUserId = null;
        if (toUserIdentifier instanceof String) {
            toUserId = userRepository.findByUsername((String) toUserIdentifier)
                    .map(User::getId)
                    .orElse(null);
        }

        Reply reply = Reply.builder()
                .postId(postId)
                .content(content)
                .authorId(authorId)
                .parentId(parentId)
                .toUserId(toUserId)
                .likeCount(0)
                .build();
        replyRepository.save(reply);
        return 1;
    }

    /**
     * 根据内容查找回复ID
     */
    private Long findReplyByContent(String contentKeyword) {
        return replyRepository.findAll().stream()
                .filter(r -> r.getContent().contains(contentKeyword))
                .findFirst()
                .map(Reply::getId)
                .orElse(null);
    }

    /**
     * 初始化用户关注数据
     * 👥 创建用户之间的关注关系
     */
    private void seedFollows() {
        // 🚫 幂等性检查
        if (userFollowRepository.count() > 0) {
            log.info("关注数据已存在，跳过插入");
            return;
        }

        int createdCount = 0;

        // student1 关注 seller_north 和 seller_south
        createdCount += createFollow("student1", "seller_north");
        createdCount += createFollow("student1", "seller_south");

        // buyer_grad 关注 seller_north
        createdCount += createFollow("buyer_grad", "seller_north");

        // student2 关注 buyer_grad
        createdCount += createFollow("student2", "buyer_grad");

        log.info("已创建 {} 条关注关系", createdCount);
    }

    /**
     * 创建关注关系
     */
    private int createFollow(String followerName, String followingName) {
        Optional<User> followerOpt = userRepository.findByUsername(followerName);
        Optional<User> followingOpt = userRepository.findByUsername(followingName);

        if (followerOpt.isPresent() && followingOpt.isPresent()) {
            UserFollow follow = UserFollow.builder()
                    .followerId(followerOpt.get().getId())
                    .followingId(followingOpt.get().getId())
                    .build();
            userFollowRepository.save(follow);
            return 1;
        }
        return 0;
    }

    /**
     * 初始化系统通知数据
     * 🔔 为用户创建系统通知
     */
    private void seedNotifications() {
        // 🚫 幂等性检查
        if (notificationRepository.count() > 0) {
            log.info("通知数据已存在,跳过插入");
            return;
        }

        int createdCount = 0;

        // 🔔 欢迎通知（发给所有新用户）
        createdCount += createNotification("student1", "欢迎加入校园轻享集市！",
                "您的账号已激活，快来发布您的第一个商品吧！", NotificationType.SYSTEM_ANNOUNCEMENT, null);

        // 🔔 订单通知
        createdCount += createNotification("buyer_grad", "订单支付成功",
                "您的订单已支付成功，卖家将尽快发货", NotificationType.ORDER_PAID, null);

        // 🔔 商品审核通知
        createdCount += createNotification("seller_north", "商品审核通过",
                "您发布的商品「MacBook Air M2」已审核通过！", NotificationType.GOODS_APPROVED, null);

        log.info("已创建 {} 条通知", createdCount);
    }

    /**
     * 创建通知
     */
    private int createNotification(String username, String title, String content,
                                   NotificationType type, Long relatedId) {
        return userRepository.findByUsername(username).map(user -> {
            Notification notification = Notification.builder()
                    .receiverId(user.getId())
                    .title(title)
                    .content(content)
                    .type(type)
                    .relatedId(relatedId)
                    .status(NotificationStatus.UNREAD)
                    .build();
            notificationRepository.save(notification);
            return 1;
        }).orElse(0);
    }

    /**
     * 初始化举报记录数据
     * 🚨 创建示例举报记录
     */
    private void seedReports() {
        // 🚫 幂等性检查
        if (reportRepository.count() > 0) {
            log.info("举报数据已存在,跳过插入");
            return;
        }

        List<Goods> goods = goodsRepository.findAll();
        List<Post> posts = postRepository.findAll();

        if (goods.isEmpty() && posts.isEmpty()) {
            log.warn("商品和帖子数据不足,无法创建举报");
            return;
        }

        int createdCount = 0;

        // 🚨 举报商品（假货举报）
        if (goods.size() > 0) {
            createdCount += createReport("student3", ReportType.GOODS, goods.get(0).getId(),
                    "疑似假货", "商品图片与描述不符，怀疑是假货", ReportStatus.PENDING);
        }

        // 🚨 举报帖子（广告举报）
        if (posts.size() > 0) {
            createdCount += createReport("student4", ReportType.POST, posts.get(0).getId(),
                    "广告内容", "帖子包含大量广告链接", ReportStatus.PENDING);
        }

        log.info("已创建 {} 条举报记录", createdCount);
    }

    /**
     * 创建举报记录
     */
    private int createReport(String reporterName, ReportType targetType, Long targetId,
                           String reason, String description, ReportStatus status) {
        return userRepository.findByUsername(reporterName).map(reporter -> {
            Report report = Report.builder()
                    .reporterId(reporter.getId())
                    .targetType(targetType)
                    .targetId(targetId)
                    .reason(reason)
                    .status(status)
                    .build();
            reportRepository.save(report);
            return 1;
        }).orElse(0);
    }

    /**
     * 初始化封禁记录数据
     * 🛡️ 不插入初始封禁记录（封禁是管理员主动操作）
     */
    private void seedBanLogs() {
        // 🚫 幂等性检查
        if (banLogRepository.count() > 0) {
            log.info("封禁记录已存在,跳过插入");
            return;
        }

        // ⚠️ 封禁记录通常不需要初始化数据
        // 封禁是管理员主动操作，不应预先插入
        log.info("封禁记录无需初始化，跳过");
    }

    /**
     * 初始化积分记录数据
     * 💰 为用户创建积分获得/消费记录
     */
    private void seedPointsLogs() {
        // 🚫 幂等性检查
        if (pointsLogRepository.count() > 0) {
            log.info("积分记录已存在,跳过插入");
            return;
        }

        int createdCount = 0;

        // 💰 注册奖励积分
        createdCount += createPointsLog("student1", PointsType.REGISTER, 100,
                "注册奖励", null);

        // 💰 发布商品获得积分
        createdCount += createPointsLog("seller_north", PointsType.PUBLISH_GOODS, 50,
                "发布商品", null);

        // 💰 完成交易获得积分
        createdCount += createPointsLog("buyer_grad", PointsType.COMPLETE_ORDER_BUYER, 30,
                "购买商品", null);

        log.info("已创建 {} 条积分记录", createdCount);
    }

    /**
     * 创建积分记录
     */
    private int createPointsLog(String username, PointsType type, Integer points,
                               String description, Long relatedId) {
        return userRepository.findByUsername(username).map(user -> {
            PointsLog pointsLog = PointsLog.builder()
                    .userId(user.getId())
                    .type(type)
                    .points(points)
                    .balance(user.getPoints() + points)
                    .description(description)
                    .build();
            pointsLogRepository.save(pointsLog);
            return 1;
        }).orElse(0);
    }

    /**
     * 初始化退款请求数据
     * 💸 创建示例退款申请
     */
    private void seedRefundRequests() {
        // 🚫 幂等性检查
        if (refundRequestRepository.count() > 0) {
            log.info("退款请求已存在,跳过插入");
            return;
        }

        // 查找已支付的订单
        List<com.campus.marketplace.common.entity.Order> paidOrders = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.PAID)
                .toList();

        if (paidOrders.isEmpty()) {
            log.warn("没有已支付订单,无法创建退款请求");
            return;
        }

        int createdCount = 0;

        // 💸 创建一个退款申请
        com.campus.marketplace.common.entity.Order order = paidOrders.get(0);
        RefundRequest refundRequest = RefundRequest.builder()
                .refundNo("REF" + System.currentTimeMillis())
                .orderNo(order.getOrderNo())
                .applicantId(order.getBuyerId())
                .reason("商品与描述不符")
                .amount(order.getActualAmount())
                .status(RefundStatus.APPLIED)
                .build();
        refundRequestRepository.save(refundRequest);
        createdCount++;

        log.info("已创建 {} 条退款请求", createdCount);
    }
}

