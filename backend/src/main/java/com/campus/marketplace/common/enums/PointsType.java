package com.campus.marketplace.common.enums;

/**
 * 积分类型枚举
 * 
 * @author BaSui
 * @date 2025-10-27
 */
public enum PointsType {
    
    /**
     * 注册奖励
     */
    REGISTER("注册奖励", 10),
    
    /**
     * 每日登录
     */
    DAILY_LOGIN("每日登录", 5),
    
    /**
     * 发布物品
     */
    PUBLISH_GOODS("发布物品", 2),
    
    /**
     * 完成交易（买家）
     */
    COMPLETE_ORDER_BUYER("完成交易", 5),
    
    /**
     * 完成交易（卖家）
     */
    COMPLETE_ORDER_SELLER("完成交易", 10),
    
    /**
     * 发布帖子
     */
    PUBLISH_POST("发布帖子", 3),
    
    /**
     * 消费积分
     */
    CONSUME("消费积分", 0);

    private final String description;
    private final Integer defaultPoints;

    PointsType(String description, Integer defaultPoints) {
        this.description = description;
        this.defaultPoints = defaultPoints;
    }

    public String getDescription() {
        return description;
    }

    public Integer getDefaultPoints() {
        return defaultPoints;
    }
}
