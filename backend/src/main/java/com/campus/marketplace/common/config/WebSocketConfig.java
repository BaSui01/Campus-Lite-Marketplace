package com.campus.marketplace.common.config;

import com.campus.marketplace.websocket.MessageWebSocketHandler;
import com.campus.marketplace.websocket.DisputeWebSocketHandler;
import com.campus.marketplace.websocket.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket Configuration
 *
 * Responsibilities:
 * 1. Enable WebSocket support
 * 2. Register WebSocket handlers
 * 3. Configure WebSocket endpoints and CORS
 * 4. Support SockJS fallback (for browsers that don't support WebSocket)
 *
 * Endpoints:
 * - /ws/message: Private message WebSocket endpoint
 * - /ws/dispute: Dispute system WebSocket endpoint
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final MessageWebSocketHandler messageWebSocketHandler;
    private final DisputeWebSocketHandler disputeWebSocketHandler;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    /**
     * Register WebSocket handlers
     *
     * Endpoints:
     * - /ws/message: Private message WebSocket endpoint
     * - /ws/dispute: Dispute system WebSocket endpoint
     * - Allow CORS: setAllowedOrigins("*")
     * - Enable SockJS fallback: withSockJS()
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Private message WebSocket endpoint
        registry.addHandler(messageWebSocketHandler, "/ws/message")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("*") // Allow all origins (should be restricted in production)
                .withSockJS(); // Enable SockJS fallback support

        // Dispute system WebSocket endpoint
        registry.addHandler(disputeWebSocketHandler, "/ws/dispute")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("*") // Allow all origins (should be restricted in production)
                .withSockJS(); // Enable SockJS fallback support
    }
}
