package com.campus.marketplace.common.config;

import com.campus.marketplace.websocket.MessageWebSocketHandler;
import com.campus.marketplace.websocket.WebSocketAuthInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.config.annotation.SockJsServiceRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("WebSocketConfig 测试")
class WebSocketConfigTest {

    @Test
    @DisplayName("注册消息处理器并启用 SockJS 降级")
    void registerWebSocketHandlers_success() {
        MessageWebSocketHandler handler = mock(MessageWebSocketHandler.class);
        WebSocketAuthInterceptor interceptor = mock(WebSocketAuthInterceptor.class);
        WebSocketHandlerRegistry registry = mock(WebSocketHandlerRegistry.class);
        WebSocketHandlerRegistration registration = mock(WebSocketHandlerRegistration.class);
        SockJsServiceRegistration sockJs = mock(SockJsServiceRegistration.class);

        when(registry.addHandler(handler, "/ws/message")).thenReturn(registration);
        when(registration.addInterceptors(interceptor)).thenReturn(registration);
        when(registration.setAllowedOrigins("*")).thenReturn(registration);
        when(registration.withSockJS()).thenReturn(sockJs);

        WebSocketConfig config = new WebSocketConfig(handler, interceptor);
        config.registerWebSocketHandlers(registry);

        verify(registry).addHandler(handler, "/ws/message");
        verify(registration).addInterceptors(interceptor);
        verify(registration).setAllowedOrigins("*");
        verify(registration).withSockJS();
    }
}
