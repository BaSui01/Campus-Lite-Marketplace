package com.campus.marketplace.common.config;

import com.campus.marketplace.websocket.DisputeWebSocketHandler;
import com.campus.marketplace.websocket.MessageWebSocketHandler;
import com.campus.marketplace.websocket.WebSocketAuthInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("WebSocketConfig Test")
class WebSocketConfigTest {

    @Test
    @DisplayName("Register both message and dispute handlers without manual context-path prefix")
    void registerWebSocketHandlers_success() {
        MessageWebSocketHandler messageHandler = mock(MessageWebSocketHandler.class);
        DisputeWebSocketHandler disputeHandler = mock(DisputeWebSocketHandler.class);
        WebSocketAuthInterceptor interceptor = mock(WebSocketAuthInterceptor.class);
        ServerProperties serverProperties = mock(ServerProperties.class);
        ServerProperties.Servlet servlet = mock(ServerProperties.Servlet.class);
        WebSocketHandlerRegistry registry = mock(WebSocketHandlerRegistry.class);
        WebSocketHandlerRegistration messageRegistration = mock(WebSocketHandlerRegistration.class);
        WebSocketHandlerRegistration disputeRegistration = mock(WebSocketHandlerRegistration.class);

        // Mock context-path = "/api"（仅用于日志，不应参与路径拼接）
        when(serverProperties.getServlet()).thenReturn(servlet);
        when(servlet.getContextPath()).thenReturn("/api");

        // 期望：注册应用内路径（不包含 context-path）
        when(registry.addHandler(messageHandler, "/ws/message")).thenReturn(messageRegistration);
        when(messageRegistration.addInterceptors(interceptor)).thenReturn(messageRegistration);
        when(messageRegistration.setAllowedOriginPatterns("*")).thenReturn(messageRegistration);

        when(registry.addHandler(disputeHandler, "/ws/dispute")).thenReturn(disputeRegistration);
        when(disputeRegistration.addInterceptors(interceptor)).thenReturn(disputeRegistration);
        when(disputeRegistration.setAllowedOriginPatterns("*")).thenReturn(disputeRegistration);

        WebSocketConfig config = new WebSocketConfig(messageHandler, disputeHandler, interceptor, serverProperties);
        config.registerWebSocketHandlers(registry);

        verify(registry).addHandler(messageHandler, "/ws/message");
        verify(messageRegistration).addInterceptors(interceptor);
        verify(messageRegistration).setAllowedOriginPatterns("*");

        verify(registry).addHandler(disputeHandler, "/ws/dispute");
        verify(disputeRegistration).addInterceptors(interceptor);
        verify(disputeRegistration).setAllowedOriginPatterns("*");
    }
}
