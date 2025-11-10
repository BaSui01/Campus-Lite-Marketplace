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
            // 💡 学生用户（DEFAULT校区） - 普通活跃用户
            new DefaultUserSeed("student1", "student1@basui12.shop", "20210001", "13800001011", 120, "DEFAULT"),
            new DefaultUserSeed("student2", "student2@basui12.shop", "20210002", "13800001012", 100, "DEFAULT"),
            new DefaultUserSeed("student3", "student3@basui12.shop", "20210003", "13800001013", 80, "DEFAULT"),
            new DefaultUserSeed("student4", "student4@basui12.shop", "20210004", "13800001014", 150, "DEFAULT"),
            new DefaultUserSeed("student5", "student5@basui12.shop", "20210005", "13800001015", 200, "DEFAULT"),

            // 🏪 卖家用户（NORTH校区） - 活跃卖家
            new DefaultUserSeed("seller_north", "seller_north@basui12.shop", "20200001", "13800001001", 320, "NORTH"),
            new DefaultUserSeed("seller_north2", "seller_north2@basui12.shop", "20200003", "13800001004", 280, "NORTH"),
            new DefaultUserSeed("seller_north3", "seller_north3@basui12.shop", "20200005", "13800001006", 400, "NORTH"),

            // 🏪 卖家用户（SOUTH校区） - 活跃卖家
            new DefaultUserSeed("seller_south", "seller_south@basui12.shop", "20200002", "13800001002", 210, "SOUTH"),
            new DefaultUserSeed("seller_south2", "seller_south2@basui12.shop", "20200004", "13800001005", 350, "SOUTH"),
            new DefaultUserSeed("seller_south3", "seller_south3@basui12.shop", "20200006", "13800001007", 180, "SOUTH"),

            // 🎓 毕业生买家（DEFAULT校区） - 高信誉买家
            new DefaultUserSeed("buyer_grad", "buyer_grad@basui12.shop", "20190001", "13800001003", 560, "DEFAULT"),
            new DefaultUserSeed("buyer_grad2", "buyer_grad2@basui12.shop", "20190002", "13800001008", 420, "DEFAULT"),

            // 🛡️ 管理员用户 - 各职能角色
            new DefaultUserSeed("security_manager", "security_manager@basui12.shop", null, null, 0, "DEFAULT"),
            new DefaultUserSeed("content_manager", "content_manager@basui12.shop", null, null, 0, "DEFAULT"),
            new DefaultUserSeed("operation_manager", "operation_manager@basui12.shop", null, null, 0, "DEFAULT"),
            new DefaultUserSeed("compliance_officer", "compliance_officer@basui12.shop", null, null, 0, "DEFAULT"),
            new DefaultUserSeed("campus_manager", "campus_manager@basui12.shop", null, null, 0, "NORTH"),
            new DefaultUserSeed("category_manager", "category_manager@basui12.shop", null, null, 0, "DEFAULT"),
            new DefaultUserSeed("rate_limit_manager", "rate_limit_manager@basui12.shop", null, null, 0, "DEFAULT"),
            new DefaultUserSeed("analyst", "analyst@basui12.shop", null, null, 0, "DEFAULT"),
            new DefaultUserSeed("support_agent", "support_agent@basui12.shop", null, null, 0, "DEFAULT")
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

    // ==================== 🎉 BaSui 新增：关联数据插入方法 ====================

    /**
     * 初始化商品数据
     * 🛍️ 为每个卖家创建不同分类的商品
     *
     * @param campuses 校区映射
     */
    private void seedGoods(Map<String, Campus> campuses) {
        // 🚫 幂等性检查：如果已有商品数据，跳过
        if (goodsRepository.count() > 0) {
            log.info("商品数据已存在，跳过插入");
            return;
        }

        // 📋 获取分类
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            log.warn("分类数据不存在，无法创建商品");
            return;
        }

        int createdCount = 0;

        // 📱 DEFAULT校区 - student1的商品
        createdCount += createGoodsForUser("student1", categories.get(0).getId(), campuses.get("DEFAULT").getId(),
                "全新iPhone 14 Pro 256GB", "全新未拆封，深空黑色，支持当面交易", new BigDecimal("5999.00"), GoodsStatus.APPROVED);
        createdCount += createGoodsForUser("student1", categories.get(1).getId(), campuses.get("DEFAULT").getId(),
                "高等数学教材（第七版）", "九成新，无笔记无划痕，适合大一新生", new BigDecimal("25.00"), GoodsStatus.APPROVED);

        // 📱 NORTH校区 - seller_north的商品
        createdCount += createGoodsForUser("seller_north", categories.get(0).getId(), campuses.get("NORTH").getId(),
                "MacBook Air M2 2023款", "自用半年，99新，附赠鼠标键盘", new BigDecimal("7800.00"), GoodsStatus.APPROVED);
        createdCount += createGoodsForUser("seller_north", categories.get(2).getId(), campuses.get("NORTH").getId(),
                "羽毛球拍（双拍）", "李宁正品，八成新，送12个球", new BigDecimal("180.00"), GoodsStatus.APPROVED);
        createdCount += createGoodsForUser("seller_north", categories.get(3).getId(), campuses.get("NORTH").getId(),
                "Nike Air Max 270 运动鞋", "42码，黑白配色，九成新", new BigDecimal("450.00"), GoodsStatus.PENDING);

        // 📱 SOUTH校区 - seller_south的商品
        createdCount += createGoodsForUser("seller_south", categories.get(0).getId(), campuses.get("SOUTH").getId(),
                "iPad Pro 11寸 2022款", "128GB，配Apple Pencil 2代", new BigDecimal("4500.00"), GoodsStatus.APPROVED);
        createdCount += createGoodsForUser("seller_south", categories.get(1).getId(), campuses.get("SOUTH").getId(),
                "大学英语四级真题集", "全新未使用，包含近5年真题", new BigDecimal("35.00"), GoodsStatus.APPROVED);
        createdCount += createGoodsForUser("seller_south", categories.get(4).getId(), campuses.get("SOUTH").getId(),
                "兰蔻小黑瓶精华液", "专柜正品，七成满，保质期至2026年", new BigDecimal("280.00"), GoodsStatus.APPROVED);

        // 📱 更多卖家的商品
        createdCount += createGoodsForUser("seller_north2", categories.get(5).getId(), campuses.get("NORTH").getId(),
                "宿舍小冰箱", "海尔品牌，使用1年，制冷效果好", new BigDecimal("320.00"), GoodsStatus.APPROVED);
        createdCount += createGoodsForUser("seller_south2", categories.get(2).getId(), campuses.get("SOUTH").getId(),
                "山地自行车", "捷安特，21速变速，适合校园代步", new BigDecimal("580.00"), GoodsStatus.APPROVED);

        log.info("已创建 {} 件商品", createdCount);
    }

    /**
     * 为指定用户创建商品
     * 🎯 辅助方法，减少重复代码
     */
    private int createGoodsForUser(String username, Long categoryId, Long campusId,
                                   String title, String description, BigDecimal price, GoodsStatus status) {
        return userRepository.findByUsername(username).map(seller -> {
            Goods goods = Goods.builder()
                    .title(title)
                    .description(description)
                    .price(price)
                    .categoryId(categoryId)
                    .sellerId(seller.getId())
                    .campusId(campusId)
                    .status(status)
                    .viewCount(0)
                    .favoriteCount(0)
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

        log.info("已创建 {} 条评价", createdCount);
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

