package com.campus.marketplace.websocket.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 消息封装类
 *
 * 统一的 WebSocket 消息格式
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage<T> {

    /**
     * 消息类型
     */
    private WebSocketMessageType type;

    /**
     * 消息数据（泛型）
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 消息ID（可选）
     */
    private String messageId;

    /**
     * 发送者ID（可选）
     */
    private Long senderId;

    /**
     * 接收者ID（可选）
     */
    private Long receiverId;
}
