package com.campus.marketplace.service;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 缓存服务接口
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
public interface CacheService {

    /**
     * 从缓存获取数据
     * 
     * @param key 缓存键
     * @param type 数据类型
     * @return 缓存数据，不存在返回 null
     */
    <T> T get(String key, Class<T> type);

    /**
     * 从缓存获取数据，如果不存在则从数据库加载
     * 
     * @param key 缓存键
     * @param type 数据类型
     * @param dataLoader 数据加载器（从数据库查询）
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 数据
     */
    <T> T getOrLoad(String key, Class<T> type, Supplier<T> dataLoader, long timeout, TimeUnit unit);

    /**
     * 从缓存获取数据（使用分布式锁防止缓存击穿）
     * 
     * @param key 缓存键
     * @param type 数据类型
     * @param dataLoader 数据加载器
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 数据
     */
    <T> T getOrLoadWithLock(String key, Class<T> type, Supplier<T> dataLoader, long timeout, TimeUnit unit);

    /**
     * 设置缓存数据
     * 
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    void set(String key, Object value, long timeout, TimeUnit unit);

    /**
     * 删除缓存
     * 
     * @param key 缓存键
     * @return 是否删除成功
     */
    boolean delete(String key);

    /**
     * 批量删除缓存（根据模式）
     * 
     * @param pattern 缓存键模式（例如：goods:*）
     */
    void deleteByPattern(String pattern);

    /**
     * 检查缓存是否存在
     * 
     * @param key 缓存键
     * @return 是否存在
     */
    boolean exists(String key);

    /**
     * 获取缓存统计信息
     * 
     * @return 统计数据
     */
    Map<String, Object> getStatistics();
}
