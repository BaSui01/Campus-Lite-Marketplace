package com.campus.marketplace.common.enums;

/**
 * 健康状态枚举
 * 
 * @author BaSui
 * @date 2025-11-03
 */
public enum HealthStatus {
    /**
     * 健康 - 所有组件正常运行
     */
    HEALTHY("健康"),

    /**
     * 降级 - 部分非核心组件异常，但系统仍可用
     */
    DEGRADED("降级"),

    /**
     * 不健康 - 核心组件异常，系统不可用
     */
    UNHEALTHY("不健康");

    private final String displayName;

    HealthStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
