package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 申诉类型枚举
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Getter
public enum AppealType {
    
    UNJUST_BAN("误封申诉"),
    UNJUST_MUTE("误禁言申诉"),
    UNJUST_DELETE("误删申诉"),
    UNJUST_OFFLINE("误下架申诉"),
    VIOLATION_REPORT("违规举报"),
    SYSTEM_ERROR("系统错误"),
    OTHER("其他原因");
    
    private final String description;

    AppealType(String description) {
        this.description = description;
    }
}
