package com.campus.marketplace.common.config;

import com.campus.marketplace.websocket.MessageWebSocketHandler;
import com.campus.marketplace.websocket.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置类
 *
 * 功能说明：
 * 1. 启用 WebSocket 支持
 * 2. 注册 WebSocket 处理器
 * 3. 配置 WebSocket 端点和跨域
 * 4. 支持 SockJS 降级（用于不支持 WebSocket 的浏览器）
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final MessageWebSocketHandler messageWebSocketHandler;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    /**
     * 注册 WebSocket 处理器
     *
     * 端点说明：
     * - /ws/message: 私信 WebSocket 端点
     * - 允许跨域：setAllowedOrigins("*")
     * - 启用 SockJS 降级：withSockJS()
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(messageWebSocketHandler, "/ws/message")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("*") // 允许所有来源（生产环境应限制）
                .withSockJS(); // 启用 SockJS 降级支持
    }
}
