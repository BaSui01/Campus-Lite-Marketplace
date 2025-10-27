package com.campus.marketplace.websocket.message;

/**
 * WebSocket 消息类型枚举
 *
 * @author BaSui
 * @date 2025-10-27
 */
public enum WebSocketMessageType {
    /**
     * 系统消息
     */
    SYSTEM,

    /**
     * 普通聊天消息
     */
    MESSAGE,

    /**
     * 已读回执
     */
    READ_RECEIPT,

    /**
     * 正在输入
     */
    TYPING,

    /**
     * 消息撤回
     */
    RECALL,

    /**
     * 心跳包
     */
    HEARTBEAT
}
