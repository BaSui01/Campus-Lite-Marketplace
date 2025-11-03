package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 批量任务状态枚举
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Getter
public enum BatchTaskStatus {
    
    /**
     * 待执行
     */
    PENDING("待执行"),
    
    /**
     * 执行中
     */
    PROCESSING("执行中"),
    
    /**
     * 部分成功
     */
    PARTIAL_SUCCESS("部分成功"),
    
    /**
     * 全部成功
     */
    SUCCESS("全部成功"),
    
    /**
     * 失败
     */
    FAILED("失败"),
    
    /**
     * 已取消
     */
    CANCELLED("已取消");

    private final String description;

    BatchTaskStatus(String description) {
        this.description = description;
    }
}
