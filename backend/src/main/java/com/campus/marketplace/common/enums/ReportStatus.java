package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 举报状态枚举
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Getter
public enum ReportStatus {
    
    /**
     * 待处理
     */
    PENDING("待处理"),
    
    /**
     * 已处理
     */
    HANDLED("已处理"),
    
    /**
     * 已驳回
     */
    REJECTED("已驳回");
    
    private final String description;

    ReportStatus(String description) {
        this.description = description;
    }
}
