package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 批量任务项状态枚举
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Getter
public enum BatchItemStatus {
    
    /**
     * 待处理
     */
    PENDING("待处理"),
    
    /**
     * 处理中
     */
    PROCESSING("处理中"),
    
    /**
     * 成功
     */
    SUCCESS("成功"),
    
    /**
     * 失败
     */
    FAILED("失败"),
    
    /**
     * 已跳过
     */
    SKIPPED("已跳过");

    private final String description;

    BatchItemStatus(String description) {
        this.description = description;
    }
}
