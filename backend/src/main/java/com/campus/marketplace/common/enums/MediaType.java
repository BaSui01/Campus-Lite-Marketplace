package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 媒体类型枚举
 *
 * 用于评价的图文视频媒体类型
 *
 * @author BaSui 😎 - 图片和视频，晒单必备！
 * @since 2025-11-03
 */
@Getter
public enum MediaType {

    /**
     * 图片类型（支持JPG、PNG、GIF）
     * 最大10张，单张不超过5MB
     */
    IMAGE("图片"),

    /**
     * 视频类型（支持MP4、AVI、MOV）
     * 最多1个，不超过100MB，时长≤60秒
     */
    VIDEO("视频");

    private final String description;

    MediaType(String description) {
        this.description = description;
    }
}
