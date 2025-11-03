package com.campus.marketplace.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 申诉状态枚举
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Getter
@RequiredArgsConstructor
public enum AppealStatus {
    
    PENDING("待处理"),
    REVIEWING("审核中"),
    APPROVED("申诉成功"),
    REJECTED("申诉驳回"),
    CANCELLED("用户取消"),
    EXPIRED("已过期");
    
    private final String description;
}
