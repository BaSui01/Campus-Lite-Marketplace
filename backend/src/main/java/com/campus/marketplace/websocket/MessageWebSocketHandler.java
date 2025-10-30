package com.campus.marketplace.websocket;

import com.campus.marketplace.common.dto.request.SendMessageRequest;
import com.campus.marketplace.common.dto.websocket.WebSocketMessage;
import com.campus.marketplace.common.enums.MessageType;
import com.campus.marketplace.common.utils.JwtUtil;
import com.campus.marketplace.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;

/**
 * WebSocket 消息处理器
 *
 * 功能说明：
 * 1. 处理 WebSocket 连接建立、断开
 * 2. 处理客户端发送的消息
 * 3. 验证 JWT Token 身份
 * 4. 处理心跳消息
 * 5. 转发私信消息
 *
 * 技术亮点：
 * - 继承 TextWebSocketHandler 处理文本消息
 * - JWT Token 认证（从查询参数获取）
 * - JSON 消息格式（Jackson）
 * - 异常处理和日志记录
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    /**
     * WebSocket 连接建立后调用
     *
     * 流程：
     * 1. 从 URL 查询参数获取 JWT Token
     * 2. 验证 Token 有效性
     * 3. 提取用户 ID
     * 4. 注册会话到会话管理器
     * 5. 发送连接成功消息
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            // 1. 获取 JWT Token（从查询参数 ?token=xxx）
            String token = getTokenFromSession(session);
            if (token == null || token.isEmpty()) {
                log.warn("⚠️ WebSocket 连接失败：缺少 Token，会话 ID={}", session.getId());
                sendErrorMessage(session, "缺少认证 Token");
                session.close(CloseStatus.NOT_ACCEPTABLE);
                return;
            }

            // 2. 验证 Token 并提取用户 ID
            Long userId;
            try {
                userId = jwtUtil.getUserIdFromToken(token);
                if (userId == null) {
                    log.warn("⚠️ WebSocket 连接失败：Token 无效，会话 ID={}", session.getId());
                    sendErrorMessage(session, "Token 无效");
                    session.close(CloseStatus.NOT_ACCEPTABLE); // 1003
                    return;
                }
            } catch (Exception e) {
                log.error("❌ WebSocket 连接失败：Token 解析异常，会话 ID={}", session.getId(), e);
                sendErrorMessage(session, "Token 解析失败");
                session.close(CloseStatus.NOT_ACCEPTABLE); // 1003
                return;
            }

            // 3. 注册会话
            sessionManager.addSession(userId, session);

            // 4. 发送连接成功消息
            WebSocketMessage successMessage = WebSocketMessage.builder()
                    .type(WebSocketMessage.TYPE_SYSTEM)
                    .content("WebSocket 连接成功")
                    .timestamp(System.currentTimeMillis())
                    .build();
            sendMessage(session, successMessage);

            log.info("✅ WebSocket 连接成功：用户 ID={}, 会话 ID={}", userId, session.getId());

        } catch (Exception e) {
            log.error("❌ WebSocket 连接异常", e);
            session.close(CloseStatus.SERVER_ERROR); // 1011
        }
    }

    /**
     * 接收到客户端消息时调用
     *
     * 消息类型处理：
     * - HEARTBEAT: 心跳消息，回复 PONG
     * - TEXT: 文本消息，转发给目标用户（任务22实现）
     * - IMAGE: 图片消息，转发给目标用户（任务22实现）
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            // 1. 解析消息
            String payload = message.getPayload();
            WebSocketMessage wsMessage = objectMapper.readValue(payload, WebSocketMessage.class);

            // 2. 获取当前用户 ID
            Long userId = sessionManager.getUserId(session.getId());
            if (userId == null) {
                log.warn("⚠️ 收到未认证会话的消息：会话 ID={}", session.getId());
                sendErrorMessage(session, "未认证");
                return;
            }

            log.debug("📨 收到消息：用户 ID={}, 类型={}, 内容={}", userId, wsMessage.getType(), wsMessage.getContent());

            // 3. 根据消息类型处理
            switch (wsMessage.getType()) {
                case WebSocketMessage.TYPE_HEARTBEAT:
                    // 心跳消息：回复 PONG
                    handleHeartbeat(session, userId);
                    break;

                case WebSocketMessage.TYPE_TEXT:
                case WebSocketMessage.TYPE_IMAGE:
                    // 私信消息：保存到数据库并转发给目标用户
                    handlePrivateMessage(session, userId, wsMessage);
                    break;

                default:
                    log.warn("⚠️ 未知的消息类型：{}", wsMessage.getType());
                    sendErrorMessage(session, "未知的消息类型");
            }

        } catch (Exception e) {
            log.error("❌ 处理 WebSocket 消息异常", e);
            sendErrorMessage(session, "消息处理失败");
        }
    }

    /**
     * WebSocket 连接关闭时调用
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 移除会话
        sessionManager.removeSessionBySessionId(session.getId());
        log.info("👋 WebSocket 连接关闭：会话 ID={}, 状态={}", session.getId(), status);
    }

    /**
     * WebSocket 传输错误时调用
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("❌ WebSocket 传输错误：会话 ID={}", session.getId(), exception);
        sessionManager.removeSessionBySessionId(session.getId());
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    /**
     * 处理心跳消息
     */
    private void handleHeartbeat(WebSocketSession session, Long userId) throws IOException {
        // 回复 PONG
        WebSocketMessage pongMessage = WebSocketMessage.builder()
                .type(WebSocketMessage.TYPE_HEARTBEAT)
                .content("PONG")
                .timestamp(System.currentTimeMillis())
                .build();
        sendMessage(session, pongMessage);
        log.debug("💓 心跳回复：用户 ID={}", userId);
    }

    /**
     * 处理私信消息
     * 
     * 流程：
     * 1. 验证接收者ID
     * 2. 调用 MessageService 保存消息到数据库
     * 3. 转发消息给接收者（如果在线）
     * 4. 发送确认消息给发送者
     */
    private void handlePrivateMessage(WebSocketSession session, Long senderId, WebSocketMessage wsMessage) {
        try {
            // 1. 验证接收者ID
            if (wsMessage.getToUserId() == null) {
                log.warn("⚠️ 私信消息缺少接收者ID：发送者={}", senderId);
                sendErrorMessage(session, "接收者ID不能为空");
                return;
            }

            // 2. 转换消息类型
            MessageType messageType = switch (wsMessage.getType()) {
                case WebSocketMessage.TYPE_TEXT -> MessageType.TEXT;
                case WebSocketMessage.TYPE_IMAGE -> MessageType.IMAGE;
                default -> {
                    log.warn("⚠️ 不支持的私信类型：{}", wsMessage.getType());
                    sendErrorMessage(session, "不支持的消息类型");
                    yield null;
                }
            };

            if (messageType == null) {
                return;
            }

            // 3. 保存消息到数据库
            SendMessageRequest messageRequest = new SendMessageRequest(
                    wsMessage.getToUserId(),
                    messageType,
                    wsMessage.getContent()
            );

            Long messageId = messageService.sendMessage(messageRequest);
            log.info("💬 私信已保存：发送者={}, 接收者={}, 类型={}, 消息ID={}", 
                    senderId, wsMessage.getToUserId(), messageType, messageId);

            // 4. 构建转发消息（包含消息ID和时间戳）
            WebSocketMessage forwardMessage = WebSocketMessage.builder()
                    .messageId(messageId)
                    .fromUserId(senderId)
                    .toUserId(wsMessage.getToUserId())
                    .type(wsMessage.getType())
                    .content(wsMessage.getContent())
                    .timestamp(System.currentTimeMillis())
                    .build();

            // 5. 转发给接收者（如果在线）
            sendMessageToUser(wsMessage.getToUserId(), forwardMessage);

            // 6. 发送确认消息给发送者
            WebSocketMessage ackMessage = WebSocketMessage.builder()
                    .type(WebSocketMessage.TYPE_SYSTEM)
                    .content("消息发送成功")
                    .messageId(messageId)
                    .timestamp(System.currentTimeMillis())
                    .build();
            sendMessage(session, ackMessage);

        } catch (Exception e) {
            log.error("❌ 处理私信消息失败：发送者={}, 接收者={}", senderId, wsMessage.getToUserId(), e);
            sendErrorMessage(session, "消息发送失败：" + e.getMessage());
        }
    }

    /**
     * 从会话中获取 JWT Token
     *
     * Token 传递方式：
     * 1. 查询参数：ws://localhost:8080/ws/message?token=xxx
     * 2. SockJS：ws://localhost:8080/ws/message/websocket?token=xxx
     */
    private String getTokenFromSession(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            return null;
        }

        String query = uri.getQuery();
        if (query == null || query.isEmpty()) {
            return null;
        }

        // 解析查询参数 token=xxx
        for (String param : query.split("&")) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && "token".equals(keyValue[0])) {
                return keyValue[1];
            }
        }

        return null;
    }

    /**
     * 发送消息到指定会话
     */
    private void sendMessage(WebSocketSession session, WebSocketMessage message) throws IOException {
        if (session.isOpen()) {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        }
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String errorMsg) {
        try {
            // 防止 null 错误消息导致 TextMessage 构造失败
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = "未知错误";
            }
            
            WebSocketMessage errorMessage = WebSocketMessage.builder()
                    .type(WebSocketMessage.TYPE_ERROR)
                    .content(errorMsg)
                    .timestamp(System.currentTimeMillis())
                    .build();
            sendMessage(session, errorMessage);
        } catch (IOException e) {
            log.error("❌ 发送错误消息失败：errorMsg={}", errorMsg, e);
        }
    }

    /**
     * 发送消息给指定用户（由 MessageService 调用，任务22实现）
     *
     * @param userId  用户 ID
     * @param message 消息内容
     */
    public void sendMessageToUser(Long userId, WebSocketMessage message) {
        WebSocketSession session = sessionManager.getSession(userId);
        if (session != null && session.isOpen()) {
            try {
                sendMessage(session, message);
                log.debug("✅ 消息已推送：用户 ID={}", userId);
            } catch (IOException e) {
                log.error("❌ 推送消息失败：用户 ID={}", userId, e);
            }
        } else {
            log.debug("⚠️ 用户离线，无法推送消息：用户 ID={}", userId);
        }
    }
}
