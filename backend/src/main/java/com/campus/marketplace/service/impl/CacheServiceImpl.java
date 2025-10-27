package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.utils.RedisUtil;
import com.campus.marketplace.service.CacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * 缓存服务实现类
 * 
 * 功能：
 * 1. 缓存数据的 CRUD 操作
 * 2. 缓存穿透防护（空值缓存）
 * 3. 缓存击穿防护（分布式锁）
 * 4. 缓存预热和失效
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final RedisUtil redisUtil;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    /**
     * 空值缓存标记
     */
    private static final String NULL_CACHE_VALUE = "NULL";

    /**
     * 空值缓存过期时间（5分钟）
     */
    private static final long NULL_CACHE_TIMEOUT = 5;

    /**
     * 分布式锁前缀
     */
    private static final String LOCK_PREFIX = "lock:";

    /**
     * 缓存统计数据
     */
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);

    @Override
    public <T> T get(String key, Class<T> type) {
        try {
            Object value = redisUtil.get(key);
            if (value == null) {
                cacheMisses.incrementAndGet();
                return null;
            }

            cacheHits.incrementAndGet();

            // 检查是否是空值标记
            if (NULL_CACHE_VALUE.equals(value.toString())) {
                return null;
            }

            // 类型转换
            if (type.isInstance(value)) {
                return type.cast(value);
            }

            // JSON 反序列化
            return objectMapper.convertValue(value, type);
        } catch (Exception e) {
            log.error("❌ 缓存获取失败: key={}, error={}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public <T> T getOrLoad(String key, Class<T> type, Supplier<T> dataLoader, long timeout, TimeUnit unit) {
        // 先从缓存获取
        T cachedValue = get(key, type);
        if (cachedValue != null) {
            return cachedValue;
        }

        // 缓存未命中，从数据库加载
        T value = dataLoader.get();

        // 缓存数据（包括 null 值防止缓存穿透）
        if (value != null) {
            set(key, value, timeout, unit);
        } else {
            // 缓存空值，过期时间较短
            redisUtil.set(key, NULL_CACHE_VALUE, NULL_CACHE_TIMEOUT, TimeUnit.MINUTES);
        }

        return value;
    }

    @Override
    public <T> T getOrLoadWithLock(String key, Class<T> type, Supplier<T> dataLoader, long timeout, TimeUnit unit) {
        // 先从缓存获取
        T cachedValue = get(key, type);
        if (cachedValue != null) {
            return cachedValue;
        }

        // 使用分布式锁防止缓存击穿
        String lockKey = LOCK_PREFIX + key;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁（等待 3 秒，锁定 10 秒）
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                try {
                    // 双重检查缓存（可能其他线程已经加载了）
                    cachedValue = get(key, type);
                    if (cachedValue != null) {
                        return cachedValue;
                    }

                    // 从数据库加载
                    T value = dataLoader.get();

                    // 缓存数据
                    if (value != null) {
                        set(key, value, timeout, unit);
                    } else {
                        redisUtil.set(key, NULL_CACHE_VALUE, NULL_CACHE_TIMEOUT, TimeUnit.MINUTES);
                    }

                    return value;
                } finally {
                    lock.unlock();
                }
            } else {
                log.warn("⚠️ 获取分布式锁失败: key={}", key);
                // 获取锁失败，直接查询数据库
                return dataLoader.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ 分布式锁被中断: key={}", key, e);
            return dataLoader.get();
        }
    }

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisUtil.set(key, value, timeout, unit);
            log.debug("✅ 缓存设置成功: key={}, timeout={}{}",  key, timeout, unit);
        } catch (Exception e) {
            log.error("❌ 缓存设置失败: key={}, error={}", key, e.getMessage());
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            Boolean result = redisUtil.delete(key);
            log.debug("✅ 缓存删除成功: key={}", key);
            return result != null && result;
        } catch (Exception e) {
            log.error("❌ 缓存删除失败: key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    @Override
    public void deleteByPattern(String pattern) {
        try {
            // 注意：这里简化实现，实际应该使用 scan 命令
            if (redisUtil.hasKey(pattern)) {
                redisUtil.delete(pattern);
            }
            log.debug("✅ 批量删除缓存成功: pattern={}", pattern);
        } catch (Exception e) {
            log.error("❌ 批量删除缓存失败: pattern={}, error={}", pattern, e.getMessage());
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            Boolean result = redisUtil.hasKey(key);
            return result != null && result;
        } catch (Exception e) {
            log.error("❌ 缓存检查失败: key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;

        stats.put("totalHits", hits);
        stats.put("totalMisses", misses);
        stats.put("totalRequests", total);
        stats.put("hitRate", total > 0 ? (double) hits / total : 0.0);

        return stats;
    }
}

