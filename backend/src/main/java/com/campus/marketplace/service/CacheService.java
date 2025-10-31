package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.common.entity.Goods;

import java.util.List;
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
 * 5. 支持实体的 DTO 转换缓存（避免 Hibernate 懒加载序列化问题）💪
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
     * 缓存 Goods 列表（自动转换为 DTO 避免懒加载问题）
     *
     * 专门处理 Goods 实体的缓存，避免 Hibernate 懒加载序列化异常。
     * 内部会将 Goods 实体列表转换为 GoodsCacheDTO 列表后再缓存。
     *
     * @param key 缓存键
     * @param goodsList Goods 实体列表
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    void setGoodsList(String key, List<Goods> goodsList, long timeout, TimeUnit unit);

    /**
     * 获取缓存的 Goods 列表（DTO 形式）
     *
     * 返回的是 GoodsCacheDTO 列表，不是 Goods 实体。
     * 如果需要 Goods 实体，调用方需要自行转换或重新查询数据库。
     *
     * @param key 缓存键
     * @return GoodsCacheDTO 列表，不存在返回 null
     */
    List<?> getGoodsList(String key);

    /**
     * 缓存 Category 列表（自动转换为 DTO 避免懒加载问题）
     *
     * @param key 缓存键
     * @param categoryList Category 实体列表
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    void setCategoryList(String key, List<Category> categoryList, long timeout, TimeUnit unit);

    /**
     * 获取缓存的 Category 列表（DTO 形式）
     *
     * @param key 缓存键
     * @return CategoryCacheDTO 列表，不存在返回 null
     */
    List<?> getCategoryList(String key);

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
