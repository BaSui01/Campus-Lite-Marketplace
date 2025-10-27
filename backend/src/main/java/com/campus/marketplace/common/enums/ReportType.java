package com.campus.marketplace.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 举报类型枚举
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Getter
@RequiredArgsConstructor
public enum ReportType {
    
    /**
     * 举报物品
     */
    GOODS("举报物品"),
    
    /**
     * 举报帖子
     */
    POST("举报帖子"),
    
    /**
     * 举报回复
     */
    REPLY("举报回复"),
    
    /**
     * 举报用户
     */
    USER("举报用户");
    
    private final String description;
}
