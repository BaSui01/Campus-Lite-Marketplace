package com.campus.marketplace.common.context;

/**
 * Campus 上下文（ThreadLocal）
 *
 * @author BaSui
 * @date 2025-10-29
 */

public final class CampusContextHolder {
    private static final ThreadLocal<Long> CAMPUS_ID = new ThreadLocal<>();

    private CampusContextHolder() {}

    public static void setCampusId(Long campusId) {
        CAMPUS_ID.set(campusId);
    }

    public static Long getCampusId() {
        return CAMPUS_ID.get();
    }

    public static void clear() {
        CAMPUS_ID.remove();
    }
}
