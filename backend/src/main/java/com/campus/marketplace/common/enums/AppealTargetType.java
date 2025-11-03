package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 申诉目标类型枚举
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Getter
public enum AppealTargetType {
    
    USER_BAN("用户封禁"),
    USER_MUTE("用户禁言"),
    GOODS_DELETE("商品删除"),
    GOODS_OFFLINE("商品下架"),
    POST_DELETE("帖子删除"),
    REPLY_DELETE("回复删除"),
    ORDER_CANCEL("订单取消"),
    REPORT_REJECT("举报驳回");
    
    private final String description;
    
    AppealTargetType(String description) {
        this.description = description;
    }
}
