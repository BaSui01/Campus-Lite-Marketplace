package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 通知类型枚举 - 区分不同的通知场景
 *
 * @author BaSui 😎
 * @since 2025-10-27
 */
@Getter
public enum NotificationType {
    /**
     * 订单相关通知
     */
    ORDER_CREATED("订单创建"),
    ORDER_PAID("订单支付成功"),
    ORDER_CANCELLED("订单取消"),
    ORDER_COMPLETED("订单完成"),

    /**
     * 商品相关通知
     */
    GOODS_APPROVED("商品审核通过"),
    GOODS_REJECTED("商品审核拒绝"),
    GOODS_SOLD("商品已售出"),

    /**
     * 社交互动通知
     */
    POST_REPLIED("帖子被回复"),
    POST_MENTIONED("被@提及"),
    MESSAGE_RECEIVED("收到新消息"),
    FOLLOW_SELLER_NEW_GOODS("关注卖家上架新品"),
    SUBSCRIPTION_MATCHED_GOODS("关注关键词有新内容"),
    REVIEW_REPLIED("评价被回复"),

    /**
     * 纠纷相关通知
     */
    DISPUTE_SUBMITTED("纠纷已提交"),
    DISPUTE_ESCALATED("纠纷已升级"),
    DISPUTE_CLOSED("纠纷已关闭"),
    DISPUTE_RESOLVED("纠纷已解决"),  
    /**
     * 系统通知
     */
    SYSTEM_ANNOUNCEMENT("系统公告"),
    USER_BANNED("用户被封禁"),
    USER_UNBANNED("用户解封");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }
}
