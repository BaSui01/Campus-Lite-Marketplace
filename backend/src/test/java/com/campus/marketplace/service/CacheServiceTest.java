package com.campus.marketplace.service;

import com.campus.marketplace.common.utils.RedisUtil;
import com.campus.marketplace.service.impl.CacheServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 缓存服务测试类
 * 
 * TDD 开发：先写测试，定义缓存服务的预期行为
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("缓存服务测试")
class CacheServiceTest {

    @InjectMocks
    private CacheServiceImpl cacheService;

    @Mock
    private RedisUtil redisUtil;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RLock rLock;

    private static final String TEST_KEY = "test:key";
    private static final String TEST_VALUE = "test_value";

    @BeforeEach
    void setUp() {
        // 每个测试前清理 Mock 行为
        reset(redisUtil);
    }

    @Test
    @DisplayName("应该从缓存获取数据（缓存命中）")
    void shouldGetFromCache_whenCacheHit() {
        // Given: 缓存中存在数据
        when(redisUtil.get(TEST_KEY)).thenReturn(TEST_VALUE);

        // When: 获取缓存数据
        String result = cacheService.get(TEST_KEY, String.class);

        // Then: 返回缓存数据，不调用数据库
        assertEquals(TEST_VALUE, result);
        verify(redisUtil, times(1)).get(TEST_KEY);
    }

    @Test
    @DisplayName("应该从数据库加载数据（缓存未命中）")
    void shouldLoadFromDatabase_whenCacheMiss() {
        // Given: 缓存中不存在数据
        when(redisUtil.get(TEST_KEY)).thenReturn(null);

        Supplier<String> dataLoader = () -> "loaded_from_db";

        // When: 获取数据（缓存未命中，从数据库加载）
        String result = cacheService.getOrLoad(TEST_KEY, String.class, dataLoader, 60, TimeUnit.SECONDS);

        // Then: 返回数据库数据，并写入缓存
        assertEquals("loaded_from_db", result);
        verify(redisUtil, times(1)).get(TEST_KEY);
        verify(redisUtil, times(1)).set(eq(TEST_KEY), eq("loaded_from_db"), eq(60L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("应该设置缓存数据（带过期时间）")
    void shouldSetCacheWithExpiration() {
        // When: 设置缓存
        cacheService.set(TEST_KEY, TEST_VALUE, 30, TimeUnit.MINUTES);

        // Then: 调用 Redis 设置方法
        verify(redisUtil, times(1)).set(TEST_KEY, TEST_VALUE, 30L, TimeUnit.MINUTES);
    }

    @Test
    @DisplayName("应该删除缓存数据")
    void shouldDeleteCache() {
        // Given: Mock 删除成功
        when(redisUtil.delete(TEST_KEY)).thenReturn(true);

        // When: 删除缓存
        boolean result = cacheService.delete(TEST_KEY);

        // Then: 删除成功
        assertTrue(result);
        verify(redisUtil, times(1)).delete(TEST_KEY);
    }

    @Test
    @DisplayName("应该批量删除缓存（根据模式）")
    void shouldDeleteCacheByPattern() {
        // Given: RedisUtil 返回删除数量
        when(redisUtil.deleteByPattern("goods:*")).thenReturn(5L);

        // When: 批量删除缓存
        cacheService.deleteByPattern("goods:*");

        // Then: 验证删除操作被调用
        verify(redisUtil).deleteByPattern("goods:*");
    }

    @Test
    @DisplayName("应该检查缓存是否存在")
    void shouldCheckCacheExists() {
        // Given: Mock 缓存存在
        when(redisUtil.hasKey(TEST_KEY)).thenReturn(true);

        // When: 检查缓存
        boolean exists = cacheService.exists(TEST_KEY);

        // Then: 返回 true
        assertTrue(exists);
        verify(redisUtil, times(1)).hasKey(TEST_KEY);
    }

    @Test
    @DisplayName("应该防止缓存穿透（数据库返回 null）")
    void shouldPreventCachePenetration_whenDatabaseReturnsNull() {
        // Given: 缓存未命中，数据库也返回 null
        when(redisUtil.get(TEST_KEY)).thenReturn(null);

        Supplier<String> dataLoader = () -> null;

        // When: 获取数据
        String result = cacheService.getOrLoad(TEST_KEY, String.class, dataLoader, 60, TimeUnit.SECONDS);

        // Then: 返回 null，但缓存空对象（防止穿透）
        assertNull(result);
        verify(redisUtil, times(1)).get(TEST_KEY);
        // 应该缓存一个特殊的空标记，过期时间较短
        verify(redisUtil, times(1)).set(eq(TEST_KEY), any(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("应该支持缓存预热")
    void shouldSupportCacheWarmup() {
        // Given: 准备预热数据
        String warmupKey = "warmup:goods:1";
        String warmupValue = "goods_data";

        // When: 缓存预热
        cacheService.set(warmupKey, warmupValue, 3600, TimeUnit.SECONDS);

        // Then: 数据已写入缓存
        verify(redisUtil, times(1)).set(warmupKey, warmupValue, 3600L, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("应该支持分布式锁（防止缓存击穿）")
    void shouldSupportDistributedLock_toPreventCacheBreakdown() throws InterruptedException {
        // Given: 缓存未命中，多个请求同时访问
        when(redisUtil.get(TEST_KEY)).thenReturn(null);
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        Supplier<String> dataLoader = () -> {
            // 模拟数据库查询耗时
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "loaded_once";
        };

        // When: 使用分布式锁加载数据
        String result = cacheService.getOrLoadWithLock(TEST_KEY, String.class, dataLoader, 60, TimeUnit.SECONDS);

        // Then: 数据只加载一次
        assertEquals("loaded_once", result);
        verify(redisUtil, atLeastOnce()).get(TEST_KEY);
        verify(redisUtil, times(1)).set(eq(TEST_KEY), eq("loaded_once"), eq(60L), eq(TimeUnit.SECONDS));
        verify(rLock, times(1)).unlock();
    }

    @Test
    @DisplayName("应该获取缓存统计信息")
    void shouldGetCacheStatistics() {
        // When: 获取缓存统计信息
        var stats = cacheService.getStatistics();

        // Then: 返回统计数据
        assertNotNull(stats);
        // 等实现后可以验证具体字段
        // assertTrue(stats.containsKey("totalHits"));
    }
}
