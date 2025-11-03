package com.campus.marketplace.websocket;

import com.campus.marketplace.common.dto.websocket.WebSocketMessage;
import com.campus.marketplace.common.utils.JwtUtil;
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
 * Dispute WebSocket Handler
 *
 * Responsibilities:
 * 1. Handle WebSocket connection establishment and disconnection
 * 2. Handle client heartbeat messages
 * 3. Send real-time dispute notifications to users
 * 4. Validate JWT Token authentication
 *
 * Technical Highlights:
 * - Extends TextWebSocketHandler for text message processing
 * - JWT Token authentication (from query parameter)
 * - JSON message format (Jackson)
 * - Exception handling and logging
 *
 * @author BaSui
 * @since 2025-11-03
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DisputeWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    /**
     * Called after WebSocket connection is established
     *
     * Flow:
     * 1. Get JWT Token from URL query parameter
     * 2. Validate Token validity
     * 3. Extract user ID
     * 4. Register session to session manager
     * 5. Send connection success message
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            // 1. Get JWT Token (from query parameter ?token=xxx)
            String token = getTokenFromSession(session);
            if (token == null || token.isEmpty()) {
                log.warn("⚠️ Dispute WebSocket connection failed: missing Token, session ID={}", session.getId());
                sendErrorMessage(session, "Missing authentication Token");
                session.close(CloseStatus.NOT_ACCEPTABLE);
                return;
            }

            // 2. Validate Token and extract user ID
            Long userId;
            try {
                userId = jwtUtil.getUserIdFromToken(token);
                if (userId == null) {
                    log.warn("⚠️ Dispute WebSocket connection failed: invalid Token, session ID={}", session.getId());
                    sendErrorMessage(session, "Invalid Token");
                    session.close(CloseStatus.NOT_ACCEPTABLE); // 1003
                    return;
                }
            } catch (Exception e) {
                log.error("❌ Dispute WebSocket connection failed: Token parsing error, session ID={}", session.getId(), e);
                sendErrorMessage(session, "Token parsing failed");
                session.close(CloseStatus.NOT_ACCEPTABLE); // 1003
                return;
            }

            // 3. Register session
            sessionManager.addSession(userId, session);

            // 4. Send connection success message
            WebSocketMessage successMessage = WebSocketMessage.builder()
                    .type(WebSocketMessage.TYPE_SYSTEM)
                    .content("Dispute WebSocket connected successfully")
                    .timestamp(System.currentTimeMillis())
                    .build();
            sendMessage(session, successMessage);

            log.info("✅ Dispute WebSocket connected: user ID={}, session ID={}", userId, session.getId());

        } catch (Exception e) {
            log.error("❌ Dispute WebSocket connection exception", e);
            session.close(CloseStatus.SERVER_ERROR); // 1011
        }
    }

    /**
     * Called when receiving client message
     *
     * Message type handling:
     * - HEARTBEAT: Heartbeat message, reply PONG
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            // 1. Parse message
            String payload = message.getPayload();
            WebSocketMessage wsMessage = objectMapper.readValue(payload, WebSocketMessage.class);

            // 2. Get current user ID
            Long userId = sessionManager.getUserId(session.getId());
            if (userId == null) {
                log.warn("⚠️ Received message from unauthenticated session: session ID={}", session.getId());
                sendErrorMessage(session, "Unauthenticated");
                return;
            }

            log.debug("📨 Dispute message received: user ID={}, type={}, content={}", userId, wsMessage.getType(), wsMessage.getContent());

            // 3. Handle message based on type
            if (WebSocketMessage.TYPE_HEARTBEAT.equals(wsMessage.getType())) {
                // Heartbeat message: reply PONG
                handleHeartbeat(session, userId);
            } else {
                log.warn("⚠️ Unknown dispute message type: {}", wsMessage.getType());
                sendErrorMessage(session, "Unknown message type");
            }

        } catch (Exception e) {
            log.error("❌ Dispute WebSocket message processing exception", e);
            sendErrorMessage(session, "Message processing failed");
        }
    }

    /**
     * Called when WebSocket connection is closed
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Remove session
        sessionManager.removeSessionBySessionId(session.getId());
        log.info("👋 Dispute WebSocket connection closed: session ID={}, status={}", session.getId(), status);
    }

    /**
     * Called when WebSocket transport error occurs
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("❌ Dispute WebSocket transport error: session ID={}", session.getId(), exception);
        sessionManager.removeSessionBySessionId(session.getId());
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    /**
     * Handle heartbeat message
     */
    private void handleHeartbeat(WebSocketSession session, Long userId) throws IOException {
        // Reply PONG
        WebSocketMessage pongMessage = WebSocketMessage.builder()
                .type(WebSocketMessage.TYPE_HEARTBEAT)
                .content("PONG")
                .timestamp(System.currentTimeMillis())
                .build();
        sendMessage(session, pongMessage);
        log.debug("💓 Heartbeat replied: user ID={}", userId);
    }

    /**
     * Get JWT Token from session
     *
     * Token passing methods:
     * 1. Query parameter: ws://localhost:8080/ws/dispute?token=xxx
     * 2. SockJS: ws://localhost:8080/ws/dispute/websocket?token=xxx
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

        // Parse query parameter token=xxx
        for (String param : query.split("&")) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && "token".equals(keyValue[0])) {
                return keyValue[1];
            }
        }

        return null;
    }

    /**
     * Send message to specified session
     */
    private void sendMessage(WebSocketSession session, WebSocketMessage message) throws IOException {
        if (session.isOpen()) {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        }
    }

    /**
     * Send error message
     */
    private void sendErrorMessage(WebSocketSession session, String errorMsg) {
        try {
            // Prevent null error message causing TextMessage construction failure
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = "Unknown error";
            }

            WebSocketMessage errorMessage = WebSocketMessage.builder()
                    .type(WebSocketMessage.TYPE_ERROR)
                    .content(errorMsg)
                    .timestamp(System.currentTimeMillis())
                    .build();
            sendMessage(session, errorMessage);
        } catch (IOException e) {
            log.error("❌ Failed to send error message: errorMsg={}", errorMsg, e);
        }
    }

    /**
     * Send dispute notification to user (called by DisputeService)
     *
     * @param userId  user ID
     * @param message notification message
     */
    public void sendDisputeNotification(Long userId, WebSocketMessage message) {
        WebSocketSession session = sessionManager.getSession(userId);
        if (session != null && session.isOpen()) {
            try {
                sendMessage(session, message);
                log.debug("✅ Dispute notification sent: user ID={}, type={}", userId, message.getType());
            } catch (IOException e) {
                log.error("❌ Failed to send dispute notification: user ID={}", userId, e);
            }
        } else {
            log.debug("⚠️ User offline, cannot send dispute notification: user ID={}", userId);
        }
    }
}
