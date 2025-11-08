package com.campus.marketplace.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息搜索建议DTO
 *
 * @author BaSui 😎
 * @date 2025-11-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSearchSuggestion {

    /**
     * 建议文本
     */
    private String text;

    /**
     * 建议类型: keyword, user, date
     */
    private String type;

    /**
     * 建议描述
     */
    private String description;

    /**
     * 建议图标
     */
    private String icon;

    /**
     * 用户ID（用户类型建议时使用）
     */
    private Long userId;
}