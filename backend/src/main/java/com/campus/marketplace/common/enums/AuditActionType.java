package com.campus.marketplace.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 审计操作类型枚举
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Getter
@RequiredArgsConstructor
public enum AuditActionType {
    
    USER_LOGIN("用户登录"),
    USER_REGISTER("用户注册"),
    USER_BAN("用户封禁"),
    USER_UNBAN("用户解封"),
    
    GOODS_CREATE("物品发布"),
    GOODS_APPROVE("物品审核"),
    GOODS_DELETE("物品删除"),
    
    POST_CREATE("发布帖子"),
    POST_APPROVE("帖子审核"),
    POST_DELETE("删除帖子"),
    
    REPLY_CREATE("发布回复"),
    REPLY_DELETE("删除回复"),
    
    ORDER_CREATE("创建订单"),
    ORDER_PAY("订单支付"),
    ORDER_CANCEL("取消订单"),
    
    REPORT_CREATE("创建举报"),
    REPORT_HANDLE("处理举报"),

    COMPLIANCE_CHECK("合规检查"),
    NOTIFICATION_FAIL("通知发送失败");
    
    private final String description;
}
