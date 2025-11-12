package com.campus.marketplace.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息搜索响应DTO
 *
 * @author BaSui 😎
 * @date 2025-11-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSearchResponse {

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 发送者名称
     */
    private String senderName;

    /**
     * 发送者角色
     */
    private String senderRole;

    /**
     * 消息时间
     */
    private LocalDateTime timestamp;

    /**
     * 相关性得分
     */
    private Double relevanceScore;

    /**
     * 是否为自己的消息
     */
    private Boolean isOwn;

    /**
     * 是否已撤回
     */
    private Boolean isRecalled;

    /**
     * 文本高亮信息
     */
    private List<TextHighlight> highlights;

    /**
     * 匹配的关键词
     */
    private List<String> matchedKeywords;

    /**
     * 文本高亮
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextHighlight {
        /**
         * 文本内容
         */
        private String text;

        /**
         * 是否为匹配文本
         */
        private Boolean isMatch;

        /**
         * 匹配的关键词
         */
        private String keyword;
    }
}