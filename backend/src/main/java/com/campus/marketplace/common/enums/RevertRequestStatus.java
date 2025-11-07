package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 撤销请求状态枚举
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Getter
public enum RevertRequestStatus {
    
    PENDING("待处理"),
    APPROVED("已批准"),
    REJECTED("已拒绝"),
    EXECUTED("已执行"),
    FAILED("执行失败"),
    CANCELLED("已取消");
    
    private final String description;

    RevertRequestStatus(String description) {
        this.description = description;
    }
}
