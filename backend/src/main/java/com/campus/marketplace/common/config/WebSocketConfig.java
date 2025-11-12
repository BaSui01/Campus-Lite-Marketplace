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
 * 5. Context-path handling (do NOT prepend manually)
 *
 * Endpoints (application mapping, without servlet context-path):
 * - /ws/message: Private message WebSocket endpoint
 * - /ws/dispute: Dispute system WebSocket endpoint
 *
 * 说明：Servlet 容器会在匹配时自动剥离/附加 context-path（例如 /api），
 * 因此此处注册路径不应手动拼接 context-path，否则会导致实际访问 /api/ws/message
 * 时匹配失败（表现为 GET /api/ws/message 404/无处理器）。
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
     * ⚠️ Important: DO NOT prepend servlet context-path here.
     * HandlerMapping 会基于请求 lookupPath（已去除 context-path）进行匹配，
     * 因此仅注册应用内相对路径（如 /ws/message）。
     *
     * Endpoints (application mapping):
     * - /ws/message: Private message WebSocket endpoint
     * - /ws/dispute: Dispute system WebSocket endpoint
     * - Allow CORS: setAllowedOriginPatterns("*") (supports credentials)
     * - SockJS 可选：withSockJS()（当前关闭，前端使用原生 WebSocket）
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 打印 context-path，仅用于诊断，但不参与路径拼接
        String contextPath = Optional.ofNullable(serverProperties.getServlet())
                .map(servlet -> servlet.getContextPath())
                .orElse("");
        System.out.println("🔧 [WebSocket] Servlet context-path: " + (contextPath == null || contextPath.isBlank() ? "/" : contextPath));
        System.out.println("🔧 [WebSocket] Registering message endpoint: /ws/message (context-path will be applied by container)");
        System.out.println("🔧 [WebSocket] Registering dispute endpoint: /ws/dispute (context-path will be applied by container)");

        // Private message WebSocket endpoint
        // 🔧 BaSui: 暂时禁用 SockJS，使用原生 WebSocket 进行调试
        registry.addHandler(messageWebSocketHandler, "/ws/message")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOriginPatterns("*"); // Allow all origins with credentials support
                // .withSockJS(); // ⚠️ 暂时禁用 SockJS

        // Dispute system WebSocket endpoint
        registry.addHandler(disputeWebSocketHandler, "/ws/dispute")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOriginPatterns("*"); // Allow all origins with credentials support
                // .withSockJS(); // ⚠️ 暂时禁用 SockJS
        
        System.out.println("✅ [WebSocket] Handlers registered successfully");
    }
}
