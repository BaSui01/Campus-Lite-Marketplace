package com.campus.marketplace.common.security;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 默认内置角色定义。后端可在 Seeder 阶段根据此映射创建并初始化角色。
 *
 * @author BaSui
 * @date 2025-10-29
 */
public enum RoleDefinition {

    SUPER_ADMIN("ROLE_SUPER_ADMIN", "超级管理员", PermissionCodes.allCodes()),
    ADMIN("ROLE_ADMIN", "系统管理员", PermissionCodes.adminDefaultCodes()),
    SECURITY_MANAGER("ROLE_SECURITY_MANAGER", "安全管理员", Set.of(
            PermissionCodes.SYSTEM_USER_VIEW,
            PermissionCodes.SYSTEM_USER_CREATE,
            PermissionCodes.SYSTEM_USER_UPDATE,
            PermissionCodes.SYSTEM_USER_DELETE,
            PermissionCodes.SYSTEM_USER_BAN,
            PermissionCodes.SYSTEM_ROLE_ASSIGN
    )),
    CONTENT_MANAGER("ROLE_CONTENT_MANAGER", "内容审核员", Set.of(
            PermissionCodes.SYSTEM_GOODS_APPROVE,
            PermissionCodes.SYSTEM_GOODS_DELETE,
            PermissionCodes.SYSTEM_POST_APPROVE,
            PermissionCodes.SYSTEM_POST_DELETE,
            PermissionCodes.SYSTEM_REPORT_HANDLE
    )),
    OPERATION_MANAGER("ROLE_OPERATION_MANAGER", "运营经理", Set.of(
            PermissionCodes.MARKET_GOODS_APPROVE,
            PermissionCodes.MARKET_ORDER_REFUND,
            PermissionCodes.SYSTEM_STATISTICS_VIEW,
            PermissionCodes.SYSTEM_CONFIG_VIEW
    )),
    COMPLIANCE_OFFICER("ROLE_COMPLIANCE_OFFICER", "合规官", Set.of(
            PermissionCodes.SYSTEM_COMPLIANCE_REVIEW,
            PermissionCodes.SYSTEM_AUDIT_VIEW
    )),
    CAMPUS_MANAGER("ROLE_CAMPUS_MANAGER", "校区管理员", Set.of(
            PermissionCodes.SYSTEM_CAMPUS_MANAGE,
            PermissionCodes.SYSTEM_CAMPUS_CROSS
    )),
    CATEGORY_MANAGER("ROLE_CATEGORY_MANAGER", "分类管理员", Set.of(
            PermissionCodes.SYSTEM_CATEGORY_MANAGE,
            PermissionCodes.SYSTEM_TAG_MANAGE
    )),
    RATE_LIMIT_MANAGER("ROLE_RATE_LIMIT_MANAGER", "限流管理员", Set.of(
            PermissionCodes.SYSTEM_RATE_LIMIT_MANAGE
    )),
    ANALYST("ROLE_ANALYST", "数据分析员", Set.of(
            PermissionCodes.SYSTEM_STATISTICS_VIEW,
            PermissionCodes.SYSTEM_CONFIG_VIEW
    )),
    SUPPORT_AGENT("ROLE_SUPPORT_AGENT", "客服专员", Set.of(
            PermissionCodes.SYSTEM_USER_VIEW,
            PermissionCodes.SYSTEM_USER_BAN,
            PermissionCodes.SYSTEM_REPORT_HANDLE
    )),
    APPEAL_MANAGER("ROLE_APPEAL_MANAGER", "申诉专员", Set.of(
            PermissionCodes.SYSTEM_USER_APPEAL_HANDLE,
            PermissionCodes.SYSTEM_DISPUTE_HANDLE,
            PermissionCodes.SYSTEM_DISPUTE_VIEW
    )),
    BATCH_MANAGER("ROLE_BATCH_MANAGER", "批量操作管理员", Set.of(
            PermissionCodes.SYSTEM_BATCH_GOODS_OFFLINE,
            PermissionCodes.SYSTEM_BATCH_GOODS_UPDATE,
            PermissionCodes.SYSTEM_BATCH_GOODS_DELETE,
            PermissionCodes.SYSTEM_BATCH_USERS_NOTIFY
    )),
    DATA_MANAGER("ROLE_DATA_MANAGER", "数据管理员", Set.of(
            PermissionCodes.SYSTEM_DATA_TRACK,
            PermissionCodes.SYSTEM_DATA_REVERT,
            PermissionCodes.SYSTEM_AUDIT_VIEW
    )),
    REVIEW_MANAGER("ROLE_REVIEW_MANAGER", "评价管理员", Set.of(
            PermissionCodes.SYSTEM_REVIEW_MANAGE,
            PermissionCodes.SYSTEM_REVIEW_DELETE
    )),
    LOGISTICS_MANAGER("ROLE_LOGISTICS_MANAGER", "物流管理员", Set.of(
            PermissionCodes.SYSTEM_LOGISTICS_MANAGE,
            PermissionCodes.SYSTEM_LOGISTICS_VIEW
    )),
    SYSTEM_SCHEDULER("ROLE_SYSTEM_SCHEDULER", "系统调度员", Set.of(
            PermissionCodes.SYSTEM_SCHEDULE_VIEW,
            PermissionCodes.SYSTEM_SCHEDULE_MANAGE,
            PermissionCodes.SYSTEM_SCHEDULE_EXECUTE,
            PermissionCodes.SYSTEM_BROADCAST
    )),
    CACHE_MANAGER("ROLE_CACHE_MANAGER", "缓存管理员", Set.of(
            PermissionCodes.SYSTEM_CACHE_VIEW,
            PermissionCodes.SYSTEM_CACHE_MANAGE
    )),
    PLUGIN_MANAGER("ROLE_PLUGIN_MANAGER", "插件管理员", Set.of(
            PermissionCodes.SYSTEM_PLUGIN_VIEW,
            PermissionCodes.SYSTEM_PLUGIN_MANAGE
    )),
    WEBSOCKET_MANAGER("ROLE_WEBSOCKET_MANAGER", "WebSocket管理员", Set.of(
            PermissionCodes.SYSTEM_WEBSOCKET_CONNECT,
            PermissionCodes.SYSTEM_CHAT_GROUP
    )),
    RECOMMENDATION_ENGINEER("ROLE_RECOMMENDATION_ENGINEER", "推荐算法师", Set.of(
            PermissionCodes.SYSTEM_RECOMMENDATION_VIEW,
            PermissionCodes.SYSTEM_RECOMMENDATION_MANAGE,
            PermissionCodes.SYSTEM_STATISTICS_VIEW
    )),
    USER("ROLE_USER", "普通用户", Collections.emptySet()),
    STUDENT("ROLE_STUDENT", "学生用户", Collections.emptySet());

    private final String roleName;
    private final String description;
    private final Set<String> permissions;

    RoleDefinition(String roleName, String description, Set<String> permissions) {
        this.roleName = roleName;
        this.description = description;
        if (permissions == PermissionCodes.allCodes() || permissions == PermissionCodes.adminDefaultCodes()) {
            // 已经是不可变集合
            this.permissions = permissions;
        } else {
            this.permissions = Collections.unmodifiableSet(new LinkedHashSet<>(permissions));
        }
    }

    public String getRoleName() {
        return roleName;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getPermissions() {
        return permissions;
    }
}
