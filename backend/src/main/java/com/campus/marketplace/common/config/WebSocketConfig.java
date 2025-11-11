package com.campus.marketplace.common.config;

import com.campus.marketplace.websocket.MessageWebSocketHandler;
import com.campus.marketplace.websocket.DisputeWebSocketHandler;
import com.campus.marketplace.websocket.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Optional;

/**
 * WebSocket Configuration
 *
 * Responsibilities:
 * 1. Enable WebSocket support
 * 2. Register WebSocket handlers
 * 3. Configure WebSocket endpoints and CORS
 * 4. Support SockJS fallback (for browsers that don't support WebSocket)
 * 5. Auto-detect context-path from application.yml
 *
 * Endpoints (with context-path /api):
 * - /api/ws/message: Private message WebSocket endpoint
 * - /api/ws/dispute: Dispute system WebSocket endpoint
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
    private final ServerProperties serverProperties;

    /**
     * Register WebSocket handlers
     *
     * ⚠️ Important: WebSocket endpoints do NOT automatically inherit servlet context-path!
     * We must manually prepend context-path to match frontend URLs.
     *
     * Endpoints (with context-path /api):
     * - /api/ws/message: Private message WebSocket endpoint
     * - /api/ws/dispute: Dispute system WebSocket endpoint
     * - Allow CORS: setAllowedOriginPatterns("*") (supports credentials)
     * - Enable SockJS fallback: withSockJS()
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Get context-path from application.yml (e.g., "/api")
        // 🔧 BaSui: 添加日志和硬编码后备方案，避免动态获取失败
        String contextPath = Optional.ofNullable(serverProperties.getServlet())
                .map(servlet -> servlet.getContextPath())
                .filter(path -> path != null && !path.isBlank() && !"/".equals(path))
                .map(this::normalizeContextPath)
                .orElse("/api"); // ⚠️ 硬编码后备值：如果获取失败，默认使用 /api

        System.out.println("🔧 [WebSocket] Context-path resolved: " + contextPath);
        System.out.println("🔧 [WebSocket] Registering message endpoint: " + contextPath + "/ws/message");
        System.out.println("🔧 [WebSocket] Registering dispute endpoint: " + contextPath + "/ws/dispute");

        // Private message WebSocket endpoint
        registry.addHandler(messageWebSocketHandler, contextPath + "/ws/message")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOriginPatterns("*") // Allow all origins with credentials support
                .withSockJS(); // Enable SockJS fallback support

        // Dispute system WebSocket endpoint
        registry.addHandler(disputeWebSocketHandler, contextPath + "/ws/dispute")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOriginPatterns("*") // Allow all origins with credentials support
                .withSockJS(); // Enable SockJS fallback support
        
        System.out.println("✅ [WebSocket] Handlers registered successfully");
    }

    /**
     * Normalize context-path: ensure it starts with "/" and doesn't end with "/"
     */
    private String normalizeContextPath(String path) {
        String normalized = path.startsWith("/") ? path : "/" + path;
        return normalized.endsWith("/") && normalized.length() > 1
                ? normalized.substring(0, normalized.length() - 1)
                : normalized;
    }
}
