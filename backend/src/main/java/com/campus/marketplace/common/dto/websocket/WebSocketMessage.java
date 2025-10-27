package com.campus.marketplace.common.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 消息 DTO
 *
 * 功能说明：
 * 用于 WebSocket 双向通信的消息格式
 *
 * 消息类型：
 * - HEARTBEAT: 心跳消息（客户端 -> 服务器，服务器 -> 客户端）
 * - TEXT: 文本消息（客户端 -> 服务器，服务器 -> 客户端）
 * - IMAGE: 图片消息
 * - SYSTEM: 系统通知（服务器 -> 客户端）
 * - ERROR: 错误消息（服务器 -> 客户端）
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 接收者用户 ID（发送消息时使用）
     */
    private Long toUserId;

    /**
     * 发送者用户 ID（接收消息时使用）
     */
    private Long fromUserId;

    /**
     * 消息 ID（服务器生成）
     */
    private Long messageId;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 额外数据（可选，JSON 格式）
     */
    private String extra;

    // 消息类型常量
    public static final String TYPE_HEARTBEAT = "HEARTBEAT";
    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_IMAGE = "IMAGE";
    public static final String TYPE_SYSTEM = "SYSTEM";
    public static final String TYPE_ERROR = "ERROR";
}
