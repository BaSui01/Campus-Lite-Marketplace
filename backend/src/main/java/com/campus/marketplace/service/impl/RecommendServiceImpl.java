package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.response.GoodsResponse;
import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.lock.DistributedLockManager;
import com.campus.marketplace.common.utils.RedisUtil;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.repository.FavoriteRepository;
import com.campus.marketplace.repository.ViewLogRepository;
import com.campus.marketplace.service.RecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 推荐与榜单服务实现
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

    private final GoodsRepository goodsRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ViewLogRepository viewLogRepository;
    private final FavoriteRepository favoriteRepository;
    private final RedisUtil redis;
    private final DistributedLockManager lockManager;

    private static final String HOT_KEY_PREFIX = "goods:rank:"; // goods:rank:{campus}
    private static final long BASE_HOT_TTL_SECONDS = Duration.ofMinutes(5).toSeconds();

    @Override
    public void refreshHotRanking(Long campusId, int topN) {
        String key = buildHotKey(campusId);
        String lockKey = "lock:" + key;

        try (DistributedLockManager.LockHandle lock = lockManager.tryLock(lockKey, 3, 10, TimeUnit.SECONDS)) {
            if (!lock.acquired()) {
                log.warn("获取热榜计算锁失败: campusId={}", campusId);
                return;
            }
            Pageable pageable = PageRequest.of(0, Math.max(topN, 20));
            List<Goods> goodsList = goodsRepository.findHotGoodsByCampus(GoodsStatus.APPROVED, campusId, pageable);

            // 使用 ZSET 写入排名：score = view*0.7 + fav*0.3
            // 先清空旧 ZSET
            redis.delete(key);
            long rank = 0;
            for (Goods g : goodsList) {
                // 新增时间衰减：越新权重大（简单平滑：24小时半衰）
                double hours = 0d;
                if (g.getCreatedAt() != null) {
                    hours = java.time.Duration.between(g.getCreatedAt(), java.time.LocalDateTime.now()).toHours();
                    if (hours < 0) hours = 0;
                }
                double timeDecay = 1.0 / (1.0 + (hours / 24.0));
                double score = g.getViewCount() * 0.6 + g.getFavoriteCount() * 0.3 + 100 * timeDecay;
                redis.zAdd(key, g.getId(), score);
                rank++;
                if (rank >= topN) break;
            }
            // 设置TTL
            long recentViewTotal;
            try { recentViewTotal = Math.max(0L, viewLogRepository.count()); } catch (Exception ignore) { recentViewTotal = 0L; }
            long ttl = computeHotTtlSeconds(recentViewTotal);
            redis.expire(key, ttl, TimeUnit.SECONDS);
            log.info("热榜刷新完成: campusId={}, size={}", campusId, Math.min(goodsList.size(), topN));
        } catch (Exception e) {
            log.error("热榜刷新失败: campusId={}", campusId, e);
        }
    }

    @Override
    public List<GoodsResponse> getHotList(Long campusId, int size) {
        String key = buildHotKey(campusId);
        try {
            // 优先从缓存读取 ZSET 倒序排名
            Set<Object> ids = redis.zReverseRange(key, 0, size - 1);
            if (ids != null && !ids.isEmpty()) {
                return toGoodsResponses(ids);
            }
        } catch (Exception e) {
            log.warn("读取热榜缓存失败，降级到数据库: key={}", key);
        }

        // 降级：直接查询数据库并回填缓存
        refreshHotRanking(campusId, size);
        Set<Object> ids = redis.zReverseRange(key, 0, size - 1);
        if (ids != null && !ids.isEmpty()) {
            return toGoodsResponses(ids);
        }

        // 双重降级：直接查库返回
        Pageable pageable = PageRequest.of(0, size);
        List<Goods> goodsList = goodsRepository.findHotGoodsByCampus(GoodsStatus.APPROVED, campusId, pageable);
        return goodsList.stream().map(this::toResponse).toList();
    }

    @Override
    public List<GoodsResponse> getPersonalRecommendations(int size) {
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Long campusId = user.getCampusId();
        String cacheKey = "recommend:user:" + user.getId();

        try {
            List<Object> cachedIds = redis.lRange(cacheKey, 0, size - 1);
            if (cachedIds != null && !cachedIds.isEmpty()) {
                return toGoodsResponses(new java.util.LinkedHashSet<>(cachedIds));
            }
        } catch (Exception e) {
            log.warn("读取个性化缓存失败，降级计算: userId={}", user.getId());
        }

        // 召回：根据收藏最多的分类
        List<Object[]> topCats = favoriteRepository.findTopCategoryIdsByUserFavorites(user.getId());
        List<Long> categoryIds = new java.util.ArrayList<>();
        for (Object[] row : topCats) {
            if (row != null && row.length > 0 && row[0] != null) {
                if (row[0] instanceof Long l) categoryIds.add(l);
                else if (row[0] instanceof Integer i) categoryIds.add(i.longValue());
            }
            if (categoryIds.size() >= 5) break; // 取前5个分类
        }

        List<Goods> recGoods;
        if (!categoryIds.isEmpty()) {
            Pageable pageable = PageRequest.of(0, size);
            recGoods = goodsRepository.findHotGoodsByCampusAndCategories(GoodsStatus.APPROVED, campusId, categoryIds, pageable);
        } else {
            // 冷启动：回退热榜
            List<GoodsResponse> hot = getHotList(campusId, size);
            return hot;
        }

        // 回填缓存
        try {
            redis.delete(cacheKey);
            for (Goods g : recGoods) {
                redis.lPush(cacheKey, g.getId());
            }
            redis.expire(cacheKey, BASE_HOT_TTL_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            log.debug("写入个性化缓存失败: userId={}", user.getId());
        }

        return recGoods.stream().map(this::toResponse).toList();
    }

    private String buildHotKey(Long campusId) {
        return HOT_KEY_PREFIX + (campusId == null ? "global" : campusId);
    }

    private List<GoodsResponse> toGoodsResponses(Set<Object> ids) {
        List<Long> idList = new ArrayList<>();
        for (Object o : ids) {
            if (o instanceof Long l) idList.add(l);
            else if (o instanceof Integer i) idList.add(i.longValue());
            else if (o instanceof String s) idList.add(Long.parseLong(s));
        }

        // 为保持原有顺序，使用 LinkedHashSet
        Set<Long> ordered = new LinkedHashSet<>(idList);
        List<GoodsResponse> result = new ArrayList<>();
        for (Long id : ordered) {
            goodsRepository.findById(id).ifPresent(g -> result.add(toResponse(g)));
        }
        return result;
    }

    private GoodsResponse toResponse(Goods goods) {
        String categoryName = categoryRepository.findById(goods.getCategoryId())
                .map(Category::getName)
                .orElse("未知分类");
        String sellerUsername = userRepository.findById(goods.getSellerId())
                .map(User::getUsername)
                .orElse("未知用户");
        String coverImage = goods.getImages() != null && goods.getImages().length > 0 ? goods.getImages()[0] : null;

        return GoodsResponse.builder()
                .id(goods.getId())
                .title(goods.getTitle())
                .description(truncate(goods.getDescription()))
                .price(goods.getPrice())
                .categoryId(goods.getCategoryId())
                .categoryName(categoryName)
                .sellerId(goods.getSellerId())
                .sellerUsername(sellerUsername)
                .status(goods.getStatus())
                .viewCount(goods.getViewCount())
                .favoriteCount(goods.getFavoriteCount())
                .coverImage(coverImage)
                .createdAt(goods.getCreatedAt())
                .build();
    }

    private String truncate(String text) {
        if (text == null) return null;
        return text.length() > 100 ? text.substring(0, 100) + "..." : text;
    }

    private long computeHotTtlSeconds(long recentViewTotal) {
        // 视图总量越大，刷新更频繁；最低2分钟，最高10分钟
        if (recentViewTotal <= 1000) return BASE_HOT_TTL_SECONDS; // 5 分钟
        if (recentViewTotal >= 100_000) return Duration.ofMinutes(2).toSeconds();
        // 线性插值 1000→5min, 100000→2min
        double ratio = (recentViewTotal - 1000d) / (100000d - 1000d);
        long seconds = (long) (Duration.ofMinutes(5).toSeconds() - ratio * (Duration.ofMinutes(5).toSeconds() - Duration.ofMinutes(2).toSeconds()));
        return Math.max(Duration.ofMinutes(2).toSeconds(), Math.min(Duration.ofMinutes(10).toSeconds(), seconds));
    }
}
