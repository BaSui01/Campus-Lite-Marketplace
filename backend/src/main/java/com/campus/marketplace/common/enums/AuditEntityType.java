package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 审计实体类型枚举
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Getter
public enum AuditEntityType {
    
    USER("用户"),
    GOODS("商品"),
    POST("帖子"),
    REPLY("回复"),
    ORDER("订单"),
    REPORT("举报"),
    APPEAL("申诉"),
    DISPUTE("纠纷"),
    BATCH_OPERATION("批量操作"),
    SYSTEM_SETTING("系统设置"),
    ROLE("角色"),
    PERMISSION("权限");
    
    private final String description;

    AuditEntityType(String description) {
        this.description = description;
    }
}
