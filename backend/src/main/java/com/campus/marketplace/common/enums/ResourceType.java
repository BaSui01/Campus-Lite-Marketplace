package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 资源类型枚举
 * 
 * @author BaSui 😎
 * @date 2025-11-11
 */
@Getter
public enum ResourceType {
    DOCUMENT("文档"),
    VIDEO("视频"),
    AUDIO("音频"),
    LINK("链接"),
    CODE("代码"),
    OTHER("其他");

    private final String description;

    ResourceType(String description) {
        this.description = description;
    }
}
