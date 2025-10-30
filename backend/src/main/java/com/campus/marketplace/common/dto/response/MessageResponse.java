package com.campus.marketplace.common.dto.response;

import com.campus.marketplace.common.enums.MessageStatus;
import com.campus.marketplace.common.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息响应DTO
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 发送者用户名
     */
    private String senderUsername;

    /**
     * 发送者头像
     */
    private String senderAvatar;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 接收者用户名
     */
    private String receiverUsername;

    /**
     * 消息类型
     */
    private MessageType messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息状态
     */
    private MessageStatus status;

    /**
     * 是否已撤回
     */
    private Boolean isRecalled;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 已读时间
     */
    private LocalDateTime readAt;
}
