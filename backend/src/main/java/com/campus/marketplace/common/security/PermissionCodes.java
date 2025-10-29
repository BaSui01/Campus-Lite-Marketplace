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

    // ===== 市场运营 =====
    public static final String MARKET_GOODS_APPROVE = "market:goods:approve";
    public static final String MARKET_ORDER_REFUND = "market:order:refund";

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
                MARKET_GOODS_APPROVE,
                MARKET_ORDER_REFUND
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
                MARKET_GOODS_APPROVE,
                MARKET_ORDER_REFUND
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

        descriptions.put(MARKET_GOODS_APPROVE, "运营商品审核");
        descriptions.put(MARKET_ORDER_REFUND, "处理订单退款");

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
