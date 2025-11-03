package com.campus.marketplace.common.security;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 系统内统一的权限编码常量。
 *
 * <p>所有使用权限字符串的地方（控制器、服务、数据种子等）都应引用此类，
 * 以避免字符串散落造成的遗漏或拼写错误。</p>
 *
 * @author BaSui
 * @date 2025-10-29
 */
public final class PermissionCodes {

    private PermissionCodes() {
    }

    // ===== 用户与角色 =====
    public static final String SYSTEM_USER_VIEW = "system:user:view";
    public static final String SYSTEM_USER_CREATE = "system:user:create";
    public static final String SYSTEM_USER_UPDATE = "system:user:update";
    public static final String SYSTEM_USER_DELETE = "system:user:delete";
    public static final String SYSTEM_USER_BAN = "system:user:ban";
    public static final String SYSTEM_ROLE_ASSIGN = "system:role:assign";

    // ===== 商品与帖子 =====
    public static final String SYSTEM_GOODS_APPROVE = "system:goods:approve";
    public static final String SYSTEM_GOODS_DELETE = "system:goods:delete";
    public static final String SYSTEM_POST_APPROVE = "system:post:approve";
    public static final String SYSTEM_POST_DELETE = "system:post:delete";

    // ===== 系统与配置 =====
    public static final String SYSTEM_CONFIG_UPDATE = "system:config:update";
    public static final String SYSTEM_CONFIG_VIEW = "system:config:view";
    public static final String SYSTEM_STATISTICS_VIEW = "system:statistics:view";
    public static final String SYSTEM_AUDIT_VIEW = "system:audit:view";
    public static final String SYSTEM_RATE_LIMIT_MANAGE = "system:rate-limit:manage";

    // ===== 校区与分类 =====
    public static final String SYSTEM_CAMPUS_MANAGE = "system:campus:manage";
    public static final String SYSTEM_CAMPUS_CROSS = "system:campus:cross";
    public static final String SYSTEM_CATEGORY_MANAGE = "system:category:manage";
    public static final String SYSTEM_TAG_MANAGE = "system:tag:manage";

    // ===== 合规与举报 =====
    public static final String SYSTEM_COMPLIANCE_REVIEW = "system:compliance:review";
    public static final String SYSTEM_REPORT_HANDLE = "system:report:handle";

    // ===== 用户申诉与纠纷 =====
    public static final String SYSTEM_USER_APPEAL = "system:user:appeal";
    public static final String SYSTEM_USER_APPEAL_HANDLE = "system:user:appeal:handle";
    public static final String SYSTEM_DISPUTE_HANDLE = "system:dispute:handle";
    public static final String SYSTEM_DISPUTE_VIEW = "system:dispute:view";

    // ===== 批量操作 =====
    public static final String SYSTEM_BATCH_GOODS_OFFLINE = "system:batch:goods:offline";
    public static final String SYSTEM_BATCH_GOODS_UPDATE = "system:batch:goods:update";
    public static final String SYSTEM_BATCH_GOODS_DELETE = "system:batch:goods:delete";
    public static final String SYSTEM_BATCH_USERS_NOTIFY = "system:batch:users:notify";

    // 批量操作权限（新增）
    public static final String BATCH_GOODS_ONLINE = "batch:goods:online";
    public static final String BATCH_GOODS_OFFLINE = "batch:goods:offline";
    public static final String BATCH_GOODS_DELETE = "batch:goods:delete";
    public static final String BATCH_GOODS_PRICE = "batch:goods:price";
    public static final String BATCH_GOODS_INVENTORY = "batch:goods:inventory";

    // ===== 数据追踪与撤销 =====
    public static final String SYSTEM_DATA_TRACK = "system:data:track";
    public static final String SYSTEM_DATA_REVERT = "system:data:revert";

    // ===== 评价管理 =====
    public static final String SYSTEM_REVIEW_MANAGE = "system:review:manage";
    public static final String SYSTEM_REVIEW_DELETE = "system:review:delete";

    // ===== 物流管理 =====
    public static final String SYSTEM_LOGISTICS_MANAGE = "system:logistics:manage";
    public static final String SYSTEM_LOGISTICS_VIEW = "system:logistics:view";

    // ===== 系统广播与调度 =====
    public static final String SYSTEM_BROADCAST = "system:broadcast";
    public static final String SYSTEM_SCHEDULE_VIEW = "system:schedule:view";
    public static final String SYSTEM_SCHEDULE_MANAGE = "system:schedule:manage";
    public static final String SYSTEM_SCHEDULE_EXECUTE = "system:schedule:execute";

    // ===== 安全管理 =====
    public static final String SYSTEM_SECURITY_VIEW = "system:security:view";
    public static final String SYSTEM_SECURITY_MANAGE = "system:security:manage";

    // ===== 缓存管理 =====
    public static final String SYSTEM_CACHE_VIEW = "system:cache:view";
    public static final String SYSTEM_CACHE_MANAGE = "system:cache:manage";

    // ===== 插件管理 =====
    public static final String SYSTEM_PLUGIN_VIEW = "system:plugin:view";
    public static final String SYSTEM_PLUGIN_MANAGE = "system:plugin:manage";

    // ===== 用户管理增强 =====
    public static final String SYSTEM_USER_LOCK = "system:user:lock";
    public static final String SYSTEM_USER_UNLOCK = "system:user:unlock";

    // ===== 实时通信 =====
    public static final String SYSTEM_WEBSOCKET_CONNECT = "system:websocket:connect";
    public static final String SYSTEM_CHAT_GROUP = "system:chat:group";

    // ===== 推荐系统 =====
    public static final String SYSTEM_RECOMMENDATION_VIEW = "system:recommendation:view";
    public static final String SYSTEM_RECOMMENDATION_MANAGE = "system:recommendation:manage";

    // ===== 市场运营 =====
    public static final String MARKET_GOODS_APPROVE = "market:goods:approve";
    public static final String MARKET_ORDER_REFUND = "market:order:refund";
    public static final String MARKET_SELLER_CENTER = "market:seller:center";
    public static final String MARKET_ORDER_MANAGE = "market:order:manage";

    private static final Set<String> ALL_CODES_INTERNAL;
    private static final Set<String> ADMIN_DEFAULT_CODES_INTERNAL;
    private static final Map<String, String> DESCRIPTION_MAP;

    static {
        LinkedHashSet<String> all = new LinkedHashSet<>();
        Collections.addAll(all,
                SYSTEM_USER_VIEW,
                SYSTEM_USER_CREATE,
                SYSTEM_USER_UPDATE,
                SYSTEM_USER_DELETE,
                SYSTEM_USER_BAN,
                SYSTEM_ROLE_ASSIGN,
                SYSTEM_GOODS_APPROVE,
                SYSTEM_GOODS_DELETE,
                SYSTEM_POST_APPROVE,
                SYSTEM_POST_DELETE,
                SYSTEM_CONFIG_UPDATE,
                SYSTEM_CONFIG_VIEW,
                SYSTEM_STATISTICS_VIEW,
                SYSTEM_AUDIT_VIEW,
                SYSTEM_RATE_LIMIT_MANAGE,
                SYSTEM_CAMPUS_MANAGE,
                SYSTEM_CAMPUS_CROSS,
                SYSTEM_CATEGORY_MANAGE,
                SYSTEM_TAG_MANAGE,
                SYSTEM_COMPLIANCE_REVIEW,
                SYSTEM_REPORT_HANDLE,
                SYSTEM_USER_APPEAL,
                SYSTEM_USER_APPEAL_HANDLE,
                SYSTEM_DISPUTE_HANDLE,
                SYSTEM_DISPUTE_VIEW,
                SYSTEM_BATCH_GOODS_OFFLINE,
                SYSTEM_BATCH_GOODS_UPDATE,
                SYSTEM_BATCH_GOODS_DELETE,
                SYSTEM_BATCH_USERS_NOTIFY,
                SYSTEM_DATA_TRACK,
                SYSTEM_DATA_REVERT,
                SYSTEM_REVIEW_MANAGE,
                SYSTEM_REVIEW_DELETE,
                SYSTEM_LOGISTICS_MANAGE,
                SYSTEM_LOGISTICS_VIEW,
                SYSTEM_BROADCAST,
                SYSTEM_SCHEDULE_VIEW,
                SYSTEM_SCHEDULE_MANAGE,
                SYSTEM_SCHEDULE_EXECUTE,
                SYSTEM_SECURITY_VIEW,
                SYSTEM_SECURITY_MANAGE,
                SYSTEM_CACHE_VIEW,
                SYSTEM_CACHE_MANAGE,
                SYSTEM_PLUGIN_VIEW,
                SYSTEM_PLUGIN_MANAGE,
                SYSTEM_USER_LOCK,
                SYSTEM_USER_UNLOCK,
                SYSTEM_WEBSOCKET_CONNECT,
                SYSTEM_CHAT_GROUP,
                SYSTEM_RECOMMENDATION_VIEW,
                SYSTEM_RECOMMENDATION_MANAGE,
                MARKET_GOODS_APPROVE,
                MARKET_ORDER_REFUND,
                MARKET_SELLER_CENTER,
                MARKET_ORDER_MANAGE
        );
        ALL_CODES_INTERNAL = Collections.unmodifiableSet(all);

        LinkedHashSet<String> adminDefaults = new LinkedHashSet<>();
        Collections.addAll(adminDefaults,
                SYSTEM_USER_VIEW,
                SYSTEM_USER_BAN,
                SYSTEM_ROLE_ASSIGN,
                SYSTEM_GOODS_APPROVE,
                SYSTEM_GOODS_DELETE,
                SYSTEM_POST_APPROVE,
                SYSTEM_POST_DELETE,
                SYSTEM_CONFIG_VIEW,
                SYSTEM_CONFIG_UPDATE,
                SYSTEM_STATISTICS_VIEW,
                SYSTEM_AUDIT_VIEW,
                SYSTEM_RATE_LIMIT_MANAGE,
                SYSTEM_CAMPUS_MANAGE,
                SYSTEM_CAMPUS_CROSS,
                SYSTEM_CATEGORY_MANAGE,
                SYSTEM_TAG_MANAGE,
                SYSTEM_COMPLIANCE_REVIEW,
                SYSTEM_REPORT_HANDLE,
                SYSTEM_USER_APPEAL_HANDLE,
                SYSTEM_DISPUTE_HANDLE,
                SYSTEM_DISPUTE_VIEW,
                SYSTEM_BATCH_GOODS_OFFLINE,
                SYSTEM_BATCH_GOODS_UPDATE,
                SYSTEM_BATCH_GOODS_DELETE,
                SYSTEM_DATA_TRACK,
                SYSTEM_REVIEW_MANAGE,
                SYSTEM_LOGISTICS_MANAGE,
                SYSTEM_LOGISTICS_VIEW,
                SYSTEM_BROADCAST,
                SYSTEM_SCHEDULE_VIEW,
                SYSTEM_SCHEDULE_MANAGE,
                SYSTEM_SCHEDULE_EXECUTE,
                SYSTEM_SECURITY_VIEW,
                SYSTEM_SECURITY_MANAGE,
                SYSTEM_CACHE_VIEW,
                SYSTEM_CACHE_MANAGE,
                SYSTEM_PLUGIN_VIEW,
                SYSTEM_PLUGIN_MANAGE,
                SYSTEM_USER_LOCK,
                SYSTEM_USER_UNLOCK,
                SYSTEM_WEBSOCKET_CONNECT,
                SYSTEM_RECOMMENDATION_VIEW,
                MARKET_GOODS_APPROVE,
                MARKET_ORDER_REFUND,
                MARKET_SELLER_CENTER,
                MARKET_ORDER_MANAGE
        );
        ADMIN_DEFAULT_CODES_INTERNAL = Collections.unmodifiableSet(adminDefaults);

        LinkedHashMap<String, String> descriptions = new LinkedHashMap<>();
        descriptions.put(SYSTEM_USER_VIEW, "查看用户");
        descriptions.put(SYSTEM_USER_CREATE, "创建用户");
        descriptions.put(SYSTEM_USER_UPDATE, "更新用户");
        descriptions.put(SYSTEM_USER_DELETE, "删除用户");
        descriptions.put(SYSTEM_USER_BAN, "封禁用户");
        descriptions.put(SYSTEM_ROLE_ASSIGN, "分配角色");

        descriptions.put(SYSTEM_GOODS_APPROVE, "审核物品");
        descriptions.put(SYSTEM_GOODS_DELETE, "删除物品");
        descriptions.put(SYSTEM_POST_APPROVE, "审核帖子");
        descriptions.put(SYSTEM_POST_DELETE, "删除帖子");

        descriptions.put(SYSTEM_CONFIG_UPDATE, "修改系统配置");
        descriptions.put(SYSTEM_CONFIG_VIEW, "查看系统配置");
        descriptions.put(SYSTEM_STATISTICS_VIEW, "查看统计看板");
        descriptions.put(SYSTEM_AUDIT_VIEW, "查看审计日志");
        descriptions.put(SYSTEM_RATE_LIMIT_MANAGE, "管理限流规则");

        descriptions.put(SYSTEM_CAMPUS_MANAGE, "管理校区");
        descriptions.put(SYSTEM_CAMPUS_CROSS, "跨校区访问权限");
        descriptions.put(SYSTEM_CATEGORY_MANAGE, "管理分类");
        descriptions.put(SYSTEM_TAG_MANAGE, "管理标签");

        descriptions.put(SYSTEM_COMPLIANCE_REVIEW, "审核隐私请求");
        descriptions.put(SYSTEM_REPORT_HANDLE, "处理用户举报");

        descriptions.put(SYSTEM_USER_APPEAL, "用户申诉");
        descriptions.put(SYSTEM_USER_APPEAL_HANDLE, "申诉处理");
        descriptions.put(SYSTEM_DISPUTE_HANDLE, "纠纷仲裁");
        descriptions.put(SYSTEM_DISPUTE_VIEW, "纠纷查看");

        descriptions.put(SYSTEM_BATCH_GOODS_OFFLINE, "批量下架商品");
        descriptions.put(SYSTEM_BATCH_GOODS_UPDATE, "批量更新商品");
        descriptions.put(SYSTEM_BATCH_GOODS_DELETE, "批量删除商品");
        descriptions.put(SYSTEM_BATCH_USERS_NOTIFY, "批量通知用户");

        descriptions.put(SYSTEM_DATA_TRACK, "数据追踪");
        descriptions.put(SYSTEM_DATA_REVERT, "数据撤销");

        descriptions.put(SYSTEM_REVIEW_MANAGE, "评价管理");
        descriptions.put(SYSTEM_REVIEW_DELETE, "评价删除");

        descriptions.put(SYSTEM_LOGISTICS_MANAGE, "物流管理");
        descriptions.put(SYSTEM_LOGISTICS_VIEW, "物流查看");

        descriptions.put(SYSTEM_BROADCAST, "系统广播");
        descriptions.put(SYSTEM_SCHEDULE_VIEW, "任务查看");
        descriptions.put(SYSTEM_SCHEDULE_MANAGE, "任务调度管理");
        descriptions.put(SYSTEM_SCHEDULE_EXECUTE, "任务执行");

        descriptions.put(SYSTEM_SECURITY_VIEW, "安全查看");
        descriptions.put(SYSTEM_SECURITY_MANAGE, "安全管理");

        descriptions.put(SYSTEM_CACHE_VIEW, "缓存查看");
        descriptions.put(SYSTEM_CACHE_MANAGE, "缓存管理");

        descriptions.put(SYSTEM_PLUGIN_VIEW, "插件查看");
        descriptions.put(SYSTEM_PLUGIN_MANAGE, "插件管理");

        descriptions.put(SYSTEM_USER_LOCK, "用户锁定");
        descriptions.put(SYSTEM_USER_UNLOCK, "用户解锁");

        descriptions.put(SYSTEM_WEBSOCKET_CONNECT, "WebSocket连接");
        descriptions.put(SYSTEM_CHAT_GROUP, "群组聊天");

        descriptions.put(SYSTEM_RECOMMENDATION_VIEW, "推荐算法查看");
        descriptions.put(SYSTEM_RECOMMENDATION_MANAGE, "推荐算法管理");

        descriptions.put(MARKET_GOODS_APPROVE, "运营商品审核");
        descriptions.put(MARKET_ORDER_REFUND, "处理订单退款");
        descriptions.put(MARKET_SELLER_CENTER, "卖家中心");
        descriptions.put(MARKET_ORDER_MANAGE, "订单管理");

        DESCRIPTION_MAP = Collections.unmodifiableMap(descriptions);
    }

    /**
     * 返回平台内声明过的全部权限编码。
     */
    public static Set<String> allCodes() {
        return ALL_CODES_INTERNAL;
    }

    /**
     * 管理端默认应具备的权限集合。
     */
    public static Set<String> adminDefaultCodes() {
        return ADMIN_DEFAULT_CODES_INTERNAL;
    }

    /**
     * 获取权限描述（如未维护描述则返回 null）。
     */
    public static String descriptionOf(String code) {
        return DESCRIPTION_MAP.get(code);
    }
}
