package com.campus.marketplace.common.aspect;

import com.campus.marketplace.common.annotation.RateLimit;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.component.RateLimitRuleManager;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletResponse;

import java.util.concurrent.TimeUnit;

/**
 * 接口限流切面
 * 
 * 使用 Redis + 滑动窗口算法实现接口频率限制
 * 
 * 限流算法：
 * 1. 使用 Redis ZSET 存储请求时间戳
 * 2. 使用当前时间作为 score，请求 ID 作为 member
 * 3. 定期清理过期数据（时间窗口外的请求）
 * 4. 统计时间窗口内的请求次数，超过阈值则拒绝
 * 
 * @author BaSui 😎
 * @date 2025-10-27
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate redisTemplate;
    private final RateLimitRuleManager ruleManager;
    private final UserRepository userRepository;

    /**
     * 限流键前缀
     */
    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";

    /**
     * 环绕通知：在方法执行前检查限流
     */
    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        // 构建限流键
        String limitKey = buildLimitKey(joinPoint, rateLimit);

        long currentTimeMs = System.currentTimeMillis();

        // 请求与响应对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = attributes != null ? attributes.getResponse() : null;

        // 黑/白名单与总开关
        Long uid = safeGetCurrentUserId();
        String clientIp = getClientIp();
        if (!ruleManager.isEnabled()) {
            return joinPoint.proceed();
        }
        if (ruleManager.isWhitelisted(uid, clientIp)) {
            return joinPoint.proceed();
        }
        if (ruleManager.isBlacklisted(clientIp)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "访问已被禁止");
        }

        try {
            if (rateLimit.algorithm() == RateLimit.Algorithm.TOKEN_BUCKET) {
                return applyTokenBucket(joinPoint, rateLimit, limitKey, currentTimeMs, response);
            } else {
                return applySlidingWindow(joinPoint, rateLimit, limitKey, currentTimeMs, response);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ 限流检查失败: key={}, error={}", limitKey, e.getMessage(), e);
            return joinPoint.proceed();
        }
    }

    /**
     * 构建限流键
     * 
     * 格式：rate_limit:{limitType}:{key}
     * 例如：rate_limit:USER:1001:sendMessage
     */
    private String buildLimitKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        // 获取限流键
        String key = rateLimit.key();
        if (key.isEmpty()) {
            // 默认使用 "类名:方法名"
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String className = signature.getDeclaringType().getSimpleName();
            String methodName = signature.getName();
            key = className + ":" + methodName;
        }

        // 根据限流类型构建完整键
        String limitKey = RATE_LIMIT_KEY_PREFIX;
        switch (rateLimit.limitType()) {
            case GLOBAL:
                // 全局限流：所有用户共享
                limitKey += "GLOBAL:" + key;
                break;
            case USER:
                // 用户级别限流：每个用户独立
                Long userId = safeGetCurrentUserId();
                limitKey += "USER:" + (userId != null ? userId : "anonymous") + ":" + key;
                break;
            case IP:
                // IP 级别限流：每个 IP 独立
                String ip = getClientIp();
                limitKey += "IP:" + ip + ":" + key;
                break;
        }

        return limitKey;
    }

    /**
     * 获取客户端 IP 地址
     * 
     * 优先从 X-Forwarded-For、X-Real-IP 等头部获取，避免代理影响
     */
    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }

        HttpServletRequest request = attributes.getRequest();

        // 尝试从 X-Forwarded-For 获取
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 多级代理时取第一个 IP
            int index = ip.indexOf(',');
            if (index != -1) {
                ip = ip.substring(0, index);
            }
            return ip.trim();
        }

        // 尝试从 X-Real-IP 获取
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }

        // 直接获取 RemoteAddr
        ip = request.getRemoteAddr();
        return ip != null ? ip : "unknown";
    }

    private void setRateLimitHeaders(HttpServletResponse response, long limit, long remaining, long resetSeconds) {
        if (response == null) return;
        response.setHeader("RateLimit-Limit", String.valueOf(limit));
        response.setHeader("RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("RateLimit-Reset", String.valueOf(resetSeconds));
    }

    private long secondsUntilReset(long nowMs, long windowStartMs, long windowSizeMs) {
        long elapsed = nowMs - windowStartMs;
        long left = windowSizeMs - elapsed;
        if (left < 0) left = 0;
        return (long) Math.ceil(left / 1000.0);
    }

    private Object applySlidingWindow(ProceedingJoinPoint joinPoint,
                                      RateLimit rateLimit,
                                      String limitKey,
                                      long currentTimeMs,
                                      HttpServletResponse response) throws Throwable {
        long timeWindowMs = rateLimit.timeUnit().toMillis(rateLimit.timeWindow());
        long windowStartMs = currentTimeMs - timeWindowMs;

        // 1. 清理时间窗口外的过期数据
        redisTemplate.opsForZSet().removeRangeByScore(limitKey, 0, windowStartMs);

        // 2. 统计时间窗口内的请求次数
        Long count = redisTemplate.opsForZSet().count(limitKey, windowStartMs, currentTimeMs);
        if (count == null) count = 0L;

        // 3. 检查是否超过限流阈值
        if (count >= rateLimit.maxRequests()) {
            log.warn("🚫 接口限流触发(SW): key={}, count={}, max={}", limitKey, count, rateLimit.maxRequests());
            setRateLimitHeaders(response, rateLimit.maxRequests(), 0L,
                    secondsUntilReset(currentTimeMs, windowStartMs, timeWindowMs));
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
        }

        // 4. 添加当前请求到 ZSET
        String requestId = currentTimeMs + ":" + Thread.currentThread().threadId();
        redisTemplate.opsForZSet().add(limitKey, requestId, currentTimeMs);

        // 5. 设置过期时间（时间窗口 * 2，确保数据能被清理）
        redisTemplate.expire(limitKey, timeWindowMs * 2, TimeUnit.MILLISECONDS);

        log.debug("✅ 限流检查通过(SW): key={}, count={}/{}", limitKey, count + 1, rateLimit.maxRequests());

        setRateLimitHeaders(response, rateLimit.maxRequests(),
                Math.max(0, (long) rateLimit.maxRequests() - (count + 1)),
                secondsUntilReset(currentTimeMs, windowStartMs, timeWindowMs));

        return joinPoint.proceed();
    }

    private Object applyTokenBucket(ProceedingJoinPoint joinPoint,
                                    RateLimit rateLimit,
                                    String limitKey,
                                    long nowMs,
                                    HttpServletResponse response) throws Throwable {
        int capacity = rateLimit.tokenBucketCapacity() > 0 ? rateLimit.tokenBucketCapacity() : rateLimit.maxRequests();
        int refill = rateLimit.refillTokens() > 0 ? rateLimit.refillTokens() : capacity;
        long intervalMs = (rateLimit.refillInterval() > 0 ? rateLimit.refillInterval() : rateLimit.timeWindow());
        intervalMs = rateLimit.timeUnit().toMillis(intervalMs);

        // 使用 Redis HASH 存储：{ tokens, ts }
        String tbKey = limitKey + ":tb";

        // Lua 原子脚本（Token Bucket）
        String script = """
                local key = KEYS[1]
                local now = tonumber(ARGV[1])
                local capacity = tonumber(ARGV[2])
                local refill = tonumber(ARGV[3])
                local interval = tonumber(ARGV[4])
                local data = redis.call('HMGET', key, 'tokens', 'ts')
                local tokens = tonumber(data[1])
                local ts = tonumber(data[2])
                if tokens == nil or ts == nil then
                  tokens = capacity
                  ts = now
                else
                  if now > ts then
                    local elapsed = now - ts
                    local refillCount = math.floor(elapsed / interval)
                    if refillCount > 0 then
                      tokens = math.min(capacity, tokens + refillCount * refill)
                      ts = ts + refillCount * interval
                    end
                  end
                end
                if tokens <= 0 then
                  redis.call('HMSET', key, 'tokens', tokens, 'ts', ts)
                  return {-1, ts}
                else
                  tokens = tokens - 1
                  redis.call('HMSET', key, 'tokens', tokens, 'ts', ts)
                  return {tokens, ts}
                end
                """;

        org.springframework.data.redis.core.script.DefaultRedisScript<java.util.List<Object>> redisScript =
                new org.springframework.data.redis.core.script.DefaultRedisScript<>();
        redisScript.setScriptText(script);
        // 泛型在运行期会被擦除，使用 List.class 作为结果类型
        @SuppressWarnings("unchecked")
        Class<java.util.List<Object>> listClass = (Class<java.util.List<Object>>) (Class<?>) java.util.List.class;
        redisScript.setResultType(listClass);

        java.util.List<Object> result = redisTemplate.execute(
                redisScript,
                java.util.Collections.singletonList(tbKey),
                String.valueOf(nowMs),
                String.valueOf(capacity),
                String.valueOf(refill),
                String.valueOf(intervalMs)
        );

        long remaining;
        long ts;
        if (result != null && result.size() >= 2) {
            Object r0 = result.get(0);
            Object r1 = result.get(1);
            remaining = toLong(r0);
            ts = toLong(r1);
        } else {
            // 脚本失败时放行
            return joinPoint.proceed();
        }

        long resetSeconds = tokenBucketResetSeconds(nowMs, ts, intervalMs);
        if (remaining < 0) {
            log.warn("🚫 接口限流触发(TB): key={}, capacity={}, refill={}/{}ms", limitKey, capacity, refill, intervalMs);
            setRateLimitHeaders(response, capacity, 0L, resetSeconds);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
        }

        // 设置过期时间（两倍从空到满的时间）
        long ttlMs = (long) Math.ceil((double) capacity / Math.max(1, refill)) * intervalMs * 2;
        redisTemplate.expire(tbKey, ttlMs, TimeUnit.MILLISECONDS);

        setRateLimitHeaders(response, capacity, Math.max(0, remaining), resetSeconds);
        return joinPoint.proceed();
    }

    private Long safeGetCurrentUserId() {
        try {
            return SecurityUtil.getCurrentUserId();
        } catch (BusinessException ex) {
            Long fallbackId = resolveUserIdByUsername();
            if (fallbackId != null) {
                return fallbackId;
            }
            if (log.isDebugEnabled()) {
                log.debug("未能获取登录用户ID，将按匿名用户处理限流: {}", ex.getMessage());
            }
            return null;
        }
    }

    private Long resolveUserIdByUsername() {
        try {
            if (!SecurityUtil.isAuthenticated()) {
                return null;
            }
            String username = SecurityUtil.getCurrentUsername();
            return userRepository.findByUsername(username)
                    .map(com.campus.marketplace.common.entity.User::getId)
                    .orElse(null);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("通过用户名解析用户ID失败，将继续按匿名处理: {}", e.getMessage());
            }
            return null;
        }
    }

    private long tokenBucketResetSeconds(long nowMs, long lastRefillTs, long intervalMs) {
        long elapsed = nowMs - lastRefillTs;
        long left = intervalMs - (elapsed % intervalMs);
        if (left < 0) left = 0;
        return (long) Math.ceil(left / 1000.0);
    }

    private long toLong(Object obj) {
        if (obj == null) return 0L;
        if (obj instanceof Long l) return l;
        if (obj instanceof Integer i) return i.longValue();
        if (obj instanceof byte[] b) {
            try { return Long.parseLong(new String(b)); } catch (Exception ignored) { return 0L; }
        }
        try { return Long.parseLong(String.valueOf(obj)); } catch (Exception e) { return 0L; }
    }
}
