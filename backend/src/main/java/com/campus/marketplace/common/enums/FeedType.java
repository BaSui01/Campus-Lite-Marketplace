package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 动态类型枚举
 * 
 * 定义用户动态流的类型
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Getter
public enum FeedType {
    
    /**
     * 发帖
     */
    POST("发帖"),
    
    /**
     * 评价
     */
    REVIEW("评价"),
    
    /**
     * 收藏
     */
    COLLECT("收藏"),
    
    /**
     * 点赞
     */
    LIKE("点赞");

    private final String description;

    FeedType(String description) {
        this.description = description;
    }
}
