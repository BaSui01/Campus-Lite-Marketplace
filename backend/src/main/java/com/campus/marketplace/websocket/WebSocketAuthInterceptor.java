package com.campus.marketplace.websocket;

import com.campus.marketplace.common.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 认证拦截器
 *
 * 在握手阶段验证 JWT Token，提取用户信息
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    /**
     * 握手前拦截
     *
     * 验证 JWT Token 并提取用户ID
     */
    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {

        // 从查询参数中获取 token
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token");

            if (token == null || token.isEmpty()) {
                log.warn("WebSocket 握手失败：缺少 token");
                return false;
            }

            try {
                // 提取用户名
                String username = jwtUtil.getUsernameFromToken(token);

                // 验证 Token 有效性
                if (!jwtUtil.validateToken(token, username)) {
                    log.warn("WebSocket 握手失败：token 无效");
                    return false;
                }

                // 提取用户ID并存入 attributes（后续可在 Handler 中使用）
                Long userId = jwtUtil.getUserIdFromToken(token);

                attributes.put("username", username);
                attributes.put("userId", userId);
                attributes.put("token", token);

                log.info("WebSocket 握手成功：userId={}, username={}", userId, username);
                return true;

            } catch (Exception e) {
                log.error("WebSocket 握手异常：token={}", token, e);
                return false;
            }
        }

        log.warn("WebSocket 握手失败：请求类型不支持");
        return false;
    }

    /**
     * 握手后处理
     */
    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {

        if (exception != null) {
            log.error("WebSocket 握手后异常", exception);
        }
    }
}
