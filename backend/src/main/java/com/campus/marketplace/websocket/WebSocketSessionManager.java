package com.campus.marketplace.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket 会话管理器
 *
 * 功能说明：
 * 1. 管理所有活跃的 WebSocket 连接
 * 2. 支持按用户 ID 查找会话
 * 3. 使用虚拟线程处理心跳检测
 * 4. 管理用户在线状态（Redis）
 * 5. 自动清理过期会话
 *
 * 技术亮点：
 * - 使用 ConcurrentHashMap 保证线程安全
 * - 使用虚拟线程执行心跳检测（Java 21）
 * - 使用 Redis 存储在线状态（支持分布式部署）
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    /**
     * 存储 userId -> WebSocketSession 的映射
     * 使用 ConcurrentHashMap 保证线程安全
     */
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * 存储 sessionId -> userId 的映射
     * 用于断开连接时快速查找用户 ID
     */
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 心跳检测定时任务（使用虚拟线程）
     */
    private final ScheduledExecutorService heartbeatExecutor;

    /**
     * Redis Key 前缀：用户在线状态
     */
    private static final String ONLINE_USER_KEY = "ws:online:";

    /**
     * 在线状态过期时间（秒）- 5分钟无心跳则认为离线
     */
    private static final long ONLINE_EXPIRE_SECONDS = 300;

    public WebSocketSessionManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        // 使用虚拟线程执行心跳检测任务（Java 21 特性！）
        this.heartbeatExecutor = Executors.newScheduledThreadPool(
                1,
                Thread.ofVirtual().factory() // 🚀 虚拟线程工厂
        );
        // 每 60 秒检查一次心跳
        startHeartbeatChecker();
    }

    /**
     * 添加会话
     *
     * @param userId  用户 ID
     * @param session WebSocket 会话
     */
    public void addSession(Long userId, WebSocketSession session) {
        // 如果用户已有连接，先关闭旧连接
        removeSession(userId);

        sessions.put(userId, session);
        sessionUserMap.put(session.getId(), userId);

        // 设置用户在线状态到 Redis
        setUserOnline(userId);

        log.info("🔗 WebSocket 连接建立：用户 ID={}, 会话 ID={}, 当前在线人数={}",
                userId, session.getId(), sessions.size());
    }

    /**
     * 移除会话
     *
     * @param userId 用户 ID
     */
    public void removeSession(Long userId) {
        WebSocketSession session = sessions.remove(userId);
        if (session != null) {
            sessionUserMap.remove(session.getId());
            try {
                if (session.isOpen()) {
                    session.close();
                }
            } catch (IOException e) {
                log.error("❌ 关闭 WebSocket 会话失败：用户 ID={}", userId, e);
            }

            // 移除用户在线状态
            setUserOffline(userId);

            log.info("🔌 WebSocket 连接断开：用户 ID={}, 当前在线人数={}", userId, sessions.size());
        }
    }

    /**
     * 根据会话 ID 移除会话
     *
     * @param sessionId 会话 ID
     */
    public void removeSessionBySessionId(String sessionId) {
        Long userId = sessionUserMap.get(sessionId);
        if (userId != null) {
            removeSession(userId);
        }
    }

    /**
     * 获取用户的会话
     *
     * @param userId 用户 ID
     * @return WebSocket 会话，不存在则返回 null
     */
    public WebSocketSession getSession(Long userId) {
        return sessions.get(userId);
    }

    /**
     * 根据会话 ID 获取用户 ID
     *
     * @param sessionId 会话 ID
     * @return 用户 ID，不存在则返回 null
     */
    public Long getUserId(String sessionId) {
        return sessionUserMap.get(sessionId);
    }

    /**
     * 检查用户是否在线
     *
     * @param userId 用户 ID
     * @return true=在线，false=离线
     */
    public boolean isOnline(Long userId) {
        return sessions.containsKey(userId);
    }

    /**
     * 获取当前在线人数
     *
     * @return 在线人数
     */
    public int getOnlineCount() {
        return sessions.size();
    }

    /**
     * 设置用户在线状态到 Redis
     *
     * @param userId 用户 ID
     */
    private void setUserOnline(Long userId) {
        String key = ONLINE_USER_KEY + userId;
        redisTemplate.opsForValue().set(key, System.currentTimeMillis(), Duration.ofSeconds(ONLINE_EXPIRE_SECONDS));
        log.debug("✅ 用户在线状态已设置：userId={}", userId);
    }

    /**
     * 移除用户在线状态
     *
     * @param userId 用户 ID
     */
    private void setUserOffline(Long userId) {
        String key = ONLINE_USER_KEY + userId;
        redisTemplate.delete(key);
        log.debug("❌ 用户在线状态已移除：userId={}", userId);
    }

    /**
     * 检查用户在线状态（从 Redis）
     *
     * @param userId 用户 ID
     * @return true=在线，false=离线
     */
    public boolean isUserOnlineInRedis(Long userId) {
        String key = ONLINE_USER_KEY + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 启动心跳检测任务
     *
     * 功能说明：
     * 1. 每 60 秒检查一次所有会话
     * 2. 移除已关闭的会话
     * 3. 刷新在线用户的 Redis 过期时间
     */
    private void startHeartbeatChecker() {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                // log.debug("💓 开始心跳检测，当前在线人数={}", sessions.size()); // 注释掉：减少日志输出，降低资源消耗

                sessions.forEach((userId, session) -> {
                    if (!session.isOpen()) {
                        // 会话已关闭，移除
                        log.warn("⚠️ 检测到已关闭的会话，移除：userId={}", userId);
                        removeSession(userId);
                    } else {
                        // 会话正常，刷新在线状态
                        setUserOnline(userId);
                    }
                });

                // log.debug("✅ 心跳检测完成，当前在线人数={}", sessions.size()); // 注释掉：减少日志输出，降低资源消耗
            } catch (Exception e) {
                log.error("❌ 心跳检测异常", e);
            }
        }, 60, 60, TimeUnit.SECONDS); // 初始延迟 60 秒，之后每 60 秒执行一次

        log.info("💓 心跳检测任务已启动（使用虚拟线程）");
    }

    /**
     * 关闭会话管理器
     */
    public void shutdown() {
        heartbeatExecutor.shutdown();
        sessions.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.close();
                }
            } catch (IOException e) {
                log.error("❌ 关闭会话失败", e);
            }
        });
        sessions.clear();
        sessionUserMap.clear();
        log.info("🛑 WebSocket 会话管理器已关闭");
    }
}
