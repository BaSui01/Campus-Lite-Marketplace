package com.campus.marketplace.common.config;

import com.campus.marketplace.websocket.DisputeWebSocketHandler;
import com.campus.marketplace.websocket.MessageWebSocketHandler;
import com.campus.marketplace.websocket.WebSocketAuthInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.web.socket.config.annotation.SockJsServiceRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("WebSocketConfig Test")
class WebSocketConfigTest {

    @Test
    @DisplayName("Register both message and dispute handlers with context-path")
    void registerWebSocketHandlers_success() {
        MessageWebSocketHandler messageHandler = mock(MessageWebSocketHandler.class);
        DisputeWebSocketHandler disputeHandler = mock(DisputeWebSocketHandler.class);
        WebSocketAuthInterceptor interceptor = mock(WebSocketAuthInterceptor.class);
        ServerProperties serverProperties = mock(ServerProperties.class);
        ServerProperties.Servlet servlet = mock(ServerProperties.Servlet.class);
        WebSocketHandlerRegistry registry = mock(WebSocketHandlerRegistry.class);
        WebSocketHandlerRegistration messageRegistration = mock(WebSocketHandlerRegistration.class);
        WebSocketHandlerRegistration disputeRegistration = mock(WebSocketHandlerRegistration.class);
        SockJsServiceRegistration sockJs = mock(SockJsServiceRegistration.class);

        // Mock context-path = "/api"
        when(serverProperties.getServlet()).thenReturn(servlet);
        when(servlet.getContextPath()).thenReturn("/api");

        when(registry.addHandler(messageHandler, "/api/ws/message")).thenReturn(messageRegistration);
        when(messageRegistration.addInterceptors(interceptor)).thenReturn(messageRegistration);
        when(messageRegistration.setAllowedOriginPatterns("*")).thenReturn(messageRegistration);
        when(messageRegistration.withSockJS()).thenReturn(sockJs);

        when(registry.addHandler(disputeHandler, "/api/ws/dispute")).thenReturn(disputeRegistration);
        when(disputeRegistration.addInterceptors(interceptor)).thenReturn(disputeRegistration);
        when(disputeRegistration.setAllowedOriginPatterns("*")).thenReturn(disputeRegistration);
        when(disputeRegistration.withSockJS()).thenReturn(sockJs);

        WebSocketConfig config = new WebSocketConfig(messageHandler, disputeHandler, interceptor, serverProperties);
        config.registerWebSocketHandlers(registry);

        verify(registry).addHandler(messageHandler, "/api/ws/message");
        verify(messageRegistration).addInterceptors(interceptor);
        verify(messageRegistration).setAllowedOriginPatterns("*");
        verify(messageRegistration).withSockJS();

        verify(registry).addHandler(disputeHandler, "/api/ws/dispute");
        verify(disputeRegistration).addInterceptors(interceptor);
        verify(disputeRegistration).setAllowedOriginPatterns("*");
        verify(disputeRegistration).withSockJS();
    }
}
