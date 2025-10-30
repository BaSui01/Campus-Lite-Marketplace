package com.campus.marketplace.common.component;

import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热组件
 * 
 * 功能：
 * 1. 系统启动时预热热门数据到 Redis
 * 2. 定时刷新缓存数据
 * 3. 提供主动失效缓存的方法
 * 
 * 预热数据：
 * - 热门物品列表（按浏览量排序前 100 条）
 * - 分类列表（所有分类）
 * - 系统配置（待扩展）
 * 
 * @author BaSui 😎
 * @date 2025-10-27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheWarmer {

    private final CacheService cacheService;
    private final GoodsRepository goodsRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 缓存键前缀
     */
    private static final String HOT_GOODS_CACHE_KEY = "hot:goods:list";
    private static final String CATEGORY_LIST_CACHE_KEY = "category:list";
    private static final String CATEGORY_TREE_CACHE_KEY = "category:tree";

    /**
     * 缓存过期时间（1小时）
     */
    private static final long CACHE_TIMEOUT = 1;
    private static final TimeUnit CACHE_TIMEOUT_UNIT = TimeUnit.HOURS;

    /**
     * 系统启动时预热缓存
     * 
     * 监听 ApplicationReadyEvent 事件，在 Spring Boot 完全启动后执行
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmUpCacheOnStartup() {
        log.info("🔥 开始预热缓存...");
        long startTime = System.currentTimeMillis();

        try {
            // 预热热门物品列表
            warmUpHotGoods();

            // 预热分类列表
            warmUpCategories();

            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ 缓存预热完成！耗时: {}ms", duration);
        } catch (Exception e) {
            log.error("❌ 缓存预热失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 定时刷新缓存（每小时执行一次）
     * 
     * cron 表达式: 秒 分 时 日 月 周
     * 0 0 * * * * = 每小时的第 0 分 0 秒执行
     */
    @Scheduled(cron = "0 0 * * * *")
    public void refreshCacheScheduled() {
        log.info("⏰ 定时刷新缓存...");
        try {
            warmUpHotGoods();
            warmUpCategories();
            log.info("✅ 定时刷新缓存完成！");
        } catch (Exception e) {
            log.error("❌ 定时刷新缓存失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 预热热门物品列表
     * 
     * 查询浏览量最高的 100 个已审核物品
     */
    private void warmUpHotGoods() {
        try {
            // 查询热门物品（按浏览量降序，取前 100 条）
            PageRequest pageRequest = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "viewCount"));
            List<Goods> hotGoods = goodsRepository.findByStatus(GoodsStatus.APPROVED, pageRequest).getContent();

            // 缓存到 Redis
            cacheService.set(HOT_GOODS_CACHE_KEY, hotGoods, CACHE_TIMEOUT, CACHE_TIMEOUT_UNIT);

            log.info("✅ 预热热门物品列表成功: {}条", hotGoods.size());
        } catch (Exception e) {
            log.error("❌ 预热热门物品列表失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 预热分类列表
     * 
     * 查询所有分类并缓存
     */
    private void warmUpCategories() {
        try {
            // 查询所有分类
            List<Category> allCategories = categoryRepository.findAll();
            cacheService.set(CATEGORY_LIST_CACHE_KEY, allCategories, CACHE_TIMEOUT, CACHE_TIMEOUT_UNIT);

            // 查询顶级分类（用于前端菜单渲染）
            List<Category> topCategories = categoryRepository.findByParentIdIsNullOrderBySortOrder();
            cacheService.set(CATEGORY_TREE_CACHE_KEY, topCategories, CACHE_TIMEOUT, CACHE_TIMEOUT_UNIT);

            log.info("✅ 预热分类列表成功: 总计{}条，顶级{}条", allCategories.size(), topCategories.size());
        } catch (Exception e) {
            log.error("❌ 预热分类列表失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 主动失效热门物品缓存
     * 
     * 场景：物品审核通过后调用
     */
    public void invalidateHotGoodsCache() {
        try {
            cacheService.delete(HOT_GOODS_CACHE_KEY);
            log.debug("✅ 失效热门物品缓存成功");
        } catch (Exception e) {
            log.error("❌ 失效热门物品缓存失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 主动失效分类缓存
     * 
     * 场景：分类新增/修改/删除后调用
     */
    public void invalidateCategoriesCache() {
        try {
            cacheService.delete(CATEGORY_LIST_CACHE_KEY);
            cacheService.delete(CATEGORY_TREE_CACHE_KEY);
            log.debug("✅ 失效分类缓存成功");
        } catch (Exception e) {
            log.error("❌ 失效分类缓存失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 主动刷新所有缓存
     * 
     * 场景：管理员手动触发
     */
    public void refreshAllCache() {
        log.info("🔄 手动刷新所有缓存...");
        warmUpHotGoods();
        warmUpCategories();
        log.info("✅ 手动刷新所有缓存完成！");
    }
}
