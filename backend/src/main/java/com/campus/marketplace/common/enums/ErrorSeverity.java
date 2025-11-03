package com.campus.marketplace.common.enums;

/**
 * 错误严重程度枚举
 * 
 * @author BaSui
 * @date 2025-11-03
 */
public enum ErrorSeverity {
    /**
     * 低级 - 不影响功能的警告性错误
     */
    LOW("低级"),

    /**
     * 中级 - 影响部分功能但系统仍可用
     */
    MEDIUM("中级"),

    /**
     * 高级 - 影响核心功能，需要立即处理
     */
    HIGH("高级"),

    /**
     * 严重 - 系统崩溃或不可用，需要紧急处理
     */
    CRITICAL("严重");

    private final String displayName;

    ErrorSeverity(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 根据异常类型判断严重程度
     */
    public static ErrorSeverity fromException(Throwable exception) {
        if (exception == null) {
            return LOW;
        }

        String exceptionName = exception.getClass().getSimpleName();

        // 严重错误
        if (exceptionName.contains("OutOfMemory") ||
            exceptionName.contains("StackOverflow") ||
            exceptionName.contains("DatabaseConnection") ||
            exceptionName.contains("Fatal")) {
            return CRITICAL;
        }

        // 高级错误
        if (exceptionName.contains("SQL") ||
            exceptionName.contains("IO") ||
            exceptionName.contains("Timeout") ||
            exceptionName.contains("Security")) {
            return HIGH;
        }

        // 中级错误
        if (exceptionName.contains("IllegalArgument") ||
            exceptionName.contains("IllegalState") ||
            exceptionName.contains("NullPointer")) {
            return MEDIUM;
        }

        // 默认低级
        return LOW;
    }
}
