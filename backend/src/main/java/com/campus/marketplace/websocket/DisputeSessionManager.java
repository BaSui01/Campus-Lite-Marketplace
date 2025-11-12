package com.campus.marketplace.websocket;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 纠纷端点会话管理器
 *
 * 功能说明：
 * 1. 专门管理 /ws/dispute 端点的 WebSocket 连接
 * 2. 继承 WebSocketSessionManager 的所有功能
 * 3. 与 MessageSessionManager 隔离，避免会话冲突
 *
 * 设计理由：
 * - 原来两个端点共用同一个 SessionManager，导致用户连接第二个端点时会踢掉第一个连接
 * - 分开管理后，用户可以同时连接多个端点
 *
 * @author BaSui
 * @date 2025-11-11
 */
@Component
public class DisputeSessionManager extends WebSocketSessionManager {

    public DisputeSessionManager(RedisTemplate<String, Object> redisTemplate) {
        super(redisTemplate);
    }

    // 继承父类所有功能，无需额外实现
    // 如果未来需要纠纷端点特有的逻辑，可以在这里扩展
}
