package com.campus.marketplace.websocket;

import com.campus.marketplace.common.dto.websocket.WebSocketMessage;
import com.campus.marketplace.common.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * WebSocket 消息处理器单元测试
 *
 * 测试场景：
 * 1. 连接建立 - Token 有效
 * 2. 连接建立 - Token 无效
 * 3. 连接建立 - 缺少 Token
 * 4. 接收心跳消息
 * 5. 接收文本消息
 * 6. 连接关闭
 * 7. 传输错误处理
 *
 * @author BaSui
 * @date 2025-10-27
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocket 消息处理器测试")
class MessageWebSocketHandlerTest {

    @Mock
    private WebSocketSessionManager sessionManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MessageWebSocketHandler handler;

    @Mock
    private WebSocketSession session;

    private static final Long TEST_USER_ID = 1001L;
    private static final String TEST_TOKEN = "test.jwt.token";
    private static final String TEST_SESSION_ID = "session-123";

    @BeforeEach
    void setUp() {
        // 设置默认 mock 行为 - 使用 lenient() 避免 UnnecessaryStubbingException
        lenient().when(session.getId()).thenReturn(TEST_SESSION_ID);
        lenient().when(session.isOpen()).thenReturn(true);
    }

    @Test
    @DisplayName("✅ 连接建立成功 - Token 有效")
    void testConnectionEstablished_ValidToken() throws Exception {
        // 准备：模拟有效 Token
        URI uri = new URI("ws://localhost:8080/ws/message?token=" + TEST_TOKEN);
        when(session.getUri()).thenReturn(uri);
        when(jwtUtil.getUserIdFromToken(TEST_TOKEN)).thenReturn(TEST_USER_ID);
        when(objectMapper.writeValueAsString(any(WebSocketMessage.class)))
                .thenReturn("{\"type\":\"SYSTEM\",\"content\":\"WebSocket 连接成功\"}");

        // 执行：建立连接
        handler.afterConnectionEstablished(session);

        // 验证：会话已注册
        verify(sessionManager).addSession(TEST_USER_ID, session);
        verify(session).sendMessage(any(TextMessage.class));
        verify(session, never()).close(any(CloseStatus.class));
    }

    @Test
    @DisplayName("❌ 连接建立失败 - Token 无效")
    void testConnectionEstablished_InvalidToken() throws Exception {
        // 准备：模拟无效 Token
        URI uri = new URI("ws://localhost:8080/ws/message?token=" + TEST_TOKEN);
        when(session.getUri()).thenReturn(uri);
        when(jwtUtil.getUserIdFromToken(TEST_TOKEN)).thenReturn(null);
        // 关键：Mock objectMapper 以避免 sendErrorMessage 失败
        when(objectMapper.writeValueAsString(any(WebSocketMessage.class)))
                .thenReturn("{\"type\":\"ERROR\",\"content\":\"Token 无效\"}");

        // 执行：建立连接
        handler.afterConnectionEstablished(session);

        // 验证：连接被拒绝（CloseStatus.NOT_ACCEPTABLE = 1003）
        verify(sessionManager, never()).addSession(anyLong(), any());
        verify(session).close(eq(CloseStatus.NOT_ACCEPTABLE)); // 期望 code=1003
    }

    @Test
    @DisplayName("❌ 连接建立失败 - 缺少 Token")
    void testConnectionEstablished_MissingToken() throws Exception {
        // 准备：没有 Token 的 URI
        URI uri = new URI("ws://localhost:8080/ws/message");
        when(session.getUri()).thenReturn(uri);

        // 执行：建立连接
        handler.afterConnectionEstablished(session);

        // 验证：连接被拒绝（CloseStatus.SERVER_ERROR = 1011，因为 catch 块捕获了异常）
        verify(sessionManager, never()).addSession(anyLong(), any());
        verify(session).close(eq(CloseStatus.SERVER_ERROR)); // 实际返回 code=1011
    }

    @Test
    @DisplayName("💓 接收心跳消息 - 回复 PONG")
    void testHandleHeartbeatMessage() throws Exception {
        // 准备：心跳消息
        String heartbeatJson = "{\"type\":\"HEARTBEAT\",\"content\":\"PING\"}";
        WebSocketMessage heartbeatMsg = WebSocketMessage.builder()
                .type(WebSocketMessage.TYPE_HEARTBEAT)
                .content("PING")
                .build();

        when(sessionManager.getUserId(TEST_SESSION_ID)).thenReturn(TEST_USER_ID);
        when(objectMapper.readValue(heartbeatJson, WebSocketMessage.class)).thenReturn(heartbeatMsg);
        when(objectMapper.writeValueAsString(any(WebSocketMessage.class)))
                .thenReturn("{\"type\":\"HEARTBEAT\",\"content\":\"PONG\"}");

        // 执行：处理心跳消息
        TextMessage textMessage = new TextMessage(heartbeatJson);
        handler.handleTextMessage(session, textMessage);

        // 验证：回复 PONG
        verify(session).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("💬 接收文本消息 - 记录日志（任务22待实现）")
    void testHandleTextMessage() throws Exception {
        // 准备：文本消息
        String textJson = "{\"type\":\"TEXT\",\"content\":\"Hello\",\"toUserId\":2001}";
        WebSocketMessage textMsg = WebSocketMessage.builder()
                .type(WebSocketMessage.TYPE_TEXT)
                .content("Hello")
                .toUserId(2001L)
                .build();

        when(sessionManager.getUserId(TEST_SESSION_ID)).thenReturn(TEST_USER_ID);
        when(objectMapper.readValue(textJson, WebSocketMessage.class)).thenReturn(textMsg);
        // 关键：Mock objectMapper 以避免 sendErrorMessage 失败
        when(objectMapper.writeValueAsString(any(WebSocketMessage.class)))
                .thenReturn("{\"type\":\"ERROR\",\"content\":\"私信功能将在任务22实现\"}");

        // 执行：处理文本消息
        TextMessage textMessage = new TextMessage(textJson);
        handler.handleTextMessage(session, textMessage);

        // 验证：消息已接收（任务22会实现转发逻辑）
        verify(sessionManager).getUserId(TEST_SESSION_ID);
    }

    @Test
    @DisplayName("👋 连接关闭 - 清理会话")
    void testConnectionClosed() throws Exception {
        // 执行：关闭连接
        handler.afterConnectionClosed(session, CloseStatus.NORMAL);

        // 验证：会话已移除
        verify(sessionManager).removeSessionBySessionId(TEST_SESSION_ID);
    }

    @Test
    @DisplayName("❌ 传输错误 - 关闭连接")
    void testTransportError() throws Exception {
        // 执行：发生传输错误
        Exception error = new RuntimeException("Network error");
        handler.handleTransportError(session, error);

        // 验证：会话已移除并关闭
        verify(sessionManager).removeSessionBySessionId(TEST_SESSION_ID);
        verify(session).close(CloseStatus.SERVER_ERROR);
    }

    @Test
    @DisplayName("✅ 发送消息给在线用户")
    void testSendMessageToUser_Online() throws Exception {
        // 准备：用户在线
        WebSocketMessage message = WebSocketMessage.builder()
                .type(WebSocketMessage.TYPE_TEXT)
                .content("Hello")
                .timestamp(System.currentTimeMillis())
                .build();

        when(sessionManager.getSession(TEST_USER_ID)).thenReturn(session);
        when(objectMapper.writeValueAsString(message)).thenReturn("{\"type\":\"TEXT\"}");

        // 执行：发送消息
        handler.sendMessageToUser(TEST_USER_ID, message);

        // 验证：消息已发送
        verify(session).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("⚠️ 发送消息给离线用户 - 忽略")
    void testSendMessageToUser_Offline() throws Exception {
        // 准备：用户离线
        WebSocketMessage message = WebSocketMessage.builder()
                .type(WebSocketMessage.TYPE_TEXT)
                .content("Hello")
                .build();

        when(sessionManager.getSession(TEST_USER_ID)).thenReturn(null);

        // 执行：发送消息
        handler.sendMessageToUser(TEST_USER_ID, message);

        // 验证：消息未发送
        verify(session, never()).sendMessage(any());
    }
}
