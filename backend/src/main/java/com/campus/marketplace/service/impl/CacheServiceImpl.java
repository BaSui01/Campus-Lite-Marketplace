package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.CategoryCacheDTO;
import com.campus.marketplace.common.dto.GoodsCacheDTO;
import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.lock.DistributedLockManager;
import com.campus.marketplace.common.utils.RedisUtil;
import com.campus.marketplace.service.CacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 缓存服务实现类
 *
 * 功能：
 * 1. 缓存数据的 CRUD 操作
 * 2. 缓存穿透防护（空值缓存）
 * 3. 缓存击穿防护（分布式锁）
 * 4. 缓存预热和失效
 * 5. 支持实体的 DTO 转换缓存（避免 Hibernate 懒加载序列化问题）💪
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final RedisUtil redisUtil;
    private final DistributedLockManager lockManager;
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
        try (DistributedLockManager.LockHandle lock = lockManager.tryLock(lockKey, 3, 10, TimeUnit.SECONDS)) {
            if (!lock.acquired()) {
                log.warn("⚠️ 获取分布式锁失败: key={}", key);
                return dataLoader.get();
            }

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
        } catch (Exception e) {
            log.error("❌ 分布式锁执行失败: key={}, error={}", key, e.getMessage());
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
    public void setGoodsList(String key, List<Goods> goodsList, long timeout, TimeUnit unit) {
        try {
            if (goodsList == null || goodsList.isEmpty()) {
                log.warn("⚠️ Goods 列表为空，跳过缓存: key={}", key);
                return;
            }

            // 🎯 关键：将 Goods 实体列表转换为 GoodsCacheDTO 列表
            List<GoodsCacheDTO> dtoList = goodsList.stream()
                    .map(GoodsCacheDTO::from)
                    .collect(Collectors.toList());

            // 缓存 DTO 列表（不会有懒加载序列化问题）
            redisUtil.set(key, dtoList, timeout, unit);

            log.info("✅ Goods 列表缓存成功: key={}, size={}, ttl={}{}",
                    key, dtoList.size(), timeout, unit);
        } catch (Exception e) {
            log.error("❌ Goods 列表缓存失败: key={}, error={}", key, e.getMessage(), e);
        }
    }

    @Override
    public List<?> getGoodsList(String key) {
        try {
            Object value = redisUtil.get(key);
            if (value == null) {
                cacheMisses.incrementAndGet();
                log.debug("⚠️ Goods 列表缓存未命中: key={}", key);
                return null;
            }

            cacheHits.incrementAndGet();

            // 检查是否是空值标记
            if (NULL_CACHE_VALUE.equals(value.toString())) {
                return null;
            }

            // 返回缓存的 DTO 列表
            if (value instanceof List) {
                log.debug("✅ Goods 列表缓存命中: key={}, size={}", key, ((List<?>) value).size());
                return (List<?>) value;
            }

            log.warn("⚠️ Goods 列表缓存类型错误: key={}, actualType={}", key, value.getClass());
            return null;
        } catch (Exception e) {
            log.error("❌ Goods 列表缓存获取失败: key={}, error={}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public void setCategoryList(String key, List<Category> categoryList, long timeout, TimeUnit unit) {
        try {
            if (categoryList == null || categoryList.isEmpty()) {
                log.warn("⚠️ Category 列表为空，跳过缓存: key={}", key);
                return;
            }

            // 🎯 关键：将 Category 实体列表转换为 CategoryCacheDTO 列表
            List<CategoryCacheDTO> dtoList = categoryList.stream()
                    .map(CategoryCacheDTO::from)
                    .collect(Collectors.toList());

            // 缓存 DTO 列表（不会有懒加载序列化问题）
            redisUtil.set(key, dtoList, timeout, unit);

            log.info("✅ Category 列表缓存成功: key={}, size={}, ttl={}{}",
                    key, dtoList.size(), timeout, unit);
        } catch (Exception e) {
            log.error("❌ Category 列表缓存失败: key={}, error={}", key, e.getMessage(), e);
        }
    }

    @Override
    public List<?> getCategoryList(String key) {
        try {
            Object value = redisUtil.get(key);
            if (value == null) {
                cacheMisses.incrementAndGet();
                log.debug("⚠️ Category 列表缓存未命中: key={}", key);
                return null;
            }

            cacheHits.incrementAndGet();

            // 检查是否是空值标记
            if (NULL_CACHE_VALUE.equals(value.toString())) {
                return null;
            }

            // 返回缓存的 DTO 列表
            if (value instanceof List) {
                log.debug("✅ Category 列表缓存命中: key={}, size={}", key, ((List<?>) value).size());
                return (List<?>) value;
            }

            log.warn("⚠️ Category 列表缓存类型错误: key={}, actualType={}", key, value.getClass());
            return null;
        } catch (Exception e) {
            log.error("❌ Category 列表缓存获取失败: key={}, error={}", key, e.getMessage());
            return null;
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
            // ✅ 真实实现：使用 SCAN 命令批量删除匹配的键
            Long deletedCount = redisUtil.deleteByPattern(pattern);
            log.debug("✅ 批量删除缓存成功: pattern={}, deletedCount={}", pattern, deletedCount);
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
