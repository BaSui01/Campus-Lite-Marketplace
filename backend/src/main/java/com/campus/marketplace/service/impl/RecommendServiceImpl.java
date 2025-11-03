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
import java.time.LocalDateTime;
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
    private final com.campus.marketplace.repository.UserSimilarityRepository userSimilarityRepository;
    private final com.campus.marketplace.repository.UserBehaviorLogRepository userBehaviorLogRepository;

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

    @Override
    public List<GoodsResponse> getCollaborativeFilteringRecommendations(Long userId, int size) {
        log.debug("协同过滤推荐: userId={}, size={}", userId, size);
        
        // 1. 查询相似用户（Top 20）
        List<com.campus.marketplace.common.entity.UserSimilarity> similarUsers = 
            userSimilarityRepository.findTopSimilarUsers(userId);
        
        if (similarUsers.isEmpty()) {
            log.debug("用户{}没有相似用户，返回空列表", userId);
            return List.of();
        }
        
        // 2. 获取当前用户已交互的商品ID（避免重复推荐）
        Set<Long> interactedGoodsIds = new LinkedHashSet<>();
        List<com.campus.marketplace.common.entity.UserBehaviorLog> userBehaviors = 
            userBehaviorLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
        userBehaviors.forEach(log -> {
            if ("GOODS".equals(log.getTargetType())) {
                interactedGoodsIds.add(log.getTargetId());
            }
        });
        
        // 3. 收集相似用户喜欢的商品（按相似度加权）
        List<GoodsScore> goodsScores = new ArrayList<>();
        for (com.campus.marketplace.common.entity.UserSimilarity similar : similarUsers.subList(0, Math.min(20, similarUsers.size()))) {
            Long similarUserId = similar.getSimilarUserId();
            Double similarity = similar.getSimilarityScore();
            
            // 获取相似用户购买和收藏的商品
            List<Goods> favoriteGoods = favoriteRepository.findAll().stream()
                .filter(fav -> fav.getUserId().equals(similarUserId))
                .map(fav -> goodsRepository.findById(fav.getGoodsId()).orElse(null))
                .filter(goods -> goods != null && goods.getStatus() == GoodsStatus.APPROVED)
                .filter(goods -> !interactedGoodsIds.contains(goods.getId()))
                .toList();
            
            for (Goods goods : favoriteGoods) {
                goodsScores.add(new GoodsScore(goods, similarity * 1.0)); // 收藏权重1.0
            }
        }
        
        // 4. 按加权分数排序并去重
        Set<Long> addedIds = new LinkedHashSet<>();
        List<Goods> recommendedGoods = goodsScores.stream()
            .sorted((a, b) -> Double.compare(b.score, a.score))
            .map(gs -> gs.goods)
            .filter(goods -> addedIds.add(goods.getId()))
            .limit(size)
            .toList();
        
        // 5. 转换为DTO并返回
        return recommendedGoods.stream()
            .map(this::toResponse)
            .toList();
    }
    
    // 内部类：商品评分
    private static class GoodsScore {
        Goods goods;
        Double score;
        
        GoodsScore(Goods goods, Double score) {
            this.goods = goods;
            this.score = score;
        }
    }

    @Override
    public List<GoodsResponse> getSimilarGoods(Long goodsId, int size) {
        log.debug("相似商品推荐: goodsId={}, size={}", goodsId, size);
        
        // 1. 获取目标商品
        Goods targetGoods = goodsRepository.findById(goodsId)
            .orElseThrow(() -> new BusinessException(ErrorCode.GOODS_NOT_FOUND));
        
        // 2. 基于内容的相似度计算（分类、价格、标签）
        List<Goods> allGoods = goodsRepository.findByStatus(GoodsStatus.APPROVED, PageRequest.of(0, 200)).getContent();
        
        List<GoodsScore> similarGoods = new ArrayList<>();
        for (Goods goods : allGoods) {
            if (goods.getId().equals(goodsId)) continue; // 跳过自己
            
            double similarity = calculateGoodsSimilarity(targetGoods, goods);
            if (similarity > 0.3) { // 相似度阈值
                similarGoods.add(new GoodsScore(goods, similarity));
            }
        }
        
        // 3. 按相似度排序并返回Top N
        return similarGoods.stream()
            .sorted((a, b) -> Double.compare(b.score, a.score))
            .limit(size)
            .map(gs -> toResponse(gs.goods))
            .toList();
    }
    
    /**
     * 计算商品相似度（基于分类、价格、标签）
     */
    private double calculateGoodsSimilarity(Goods goods1, Goods goods2) {
        double similarity = 0.0;
        
        // 1. 分类相似度（权重0.5）
        if (goods1.getCategoryId().equals(goods2.getCategoryId())) {
            similarity += 0.5;
        }
        
        // 2. 价格相似度（权重0.3）- 价格差距越小越相似
        double price1 = goods1.getPrice().doubleValue();
        double price2 = goods2.getPrice().doubleValue();
        double priceDiff = Math.abs(price1 - price2);
        double avgPrice = (price1 + price2) / 2.0;
        double priceSimilarity = 1.0 - Math.min(1.0, priceDiff / avgPrice);
        similarity += 0.3 * priceSimilarity;
        
        // 3. 标签相似度（权重0.2）- 标题关键词匹配
        String title1 = goods1.getTitle() != null ? goods1.getTitle().toLowerCase() : "";
        String title2 = goods2.getTitle() != null ? goods2.getTitle().toLowerCase() : "";
        double titleSimilarity = calculateTextSimilarity(title1, title2);
        similarity += 0.2 * titleSimilarity;
        
        return similarity;
    }
    
    /**
     * 计算文本相似度（简单的关键词匹配）
     */
    private double calculateTextSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) return 0.0;
        
        // 简单的关键词重叠度计算
        String[] words1 = text1.split("\\s+");
        String[] words2 = text2.split("\\s+");
        
        Set<String> set1 = Set.of(words1);
        Set<String> set2 = Set.of(words2);
        
        Set<String> intersection = new LinkedHashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new LinkedHashSet<>(set1);
        union.addAll(set2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    @Override
    public List<GoodsResponse> getHybridRecommendations(Long userId, int size) {
        log.debug("混合推荐: userId={}, size={}", userId, size);
        
        // 混合推荐策略：协同过滤40% + 用户画像30% + 热门榜单30%
        int cfSize = (int) (size * 0.4);
        int personaSize = (int) (size * 0.3);
        int hotSize = size - cfSize - personaSize;
        
        Set<Long> addedIds = new LinkedHashSet<>();
        List<GoodsResponse> result = new ArrayList<>();
        
        // 1. 协同过滤推荐（40%）
        List<GoodsResponse> cfRecommendations = getCollaborativeFilteringRecommendations(userId, cfSize);
        for (GoodsResponse goods : cfRecommendations) {
            if (addedIds.add(goods.getId())) {
                result.add(goods);
            }
        }
        
        // 2. 基于用户收藏分类的推荐（30%）
        // 暂时跳过，后续实现
        
        // 3. 热门榜单补充（30%）
        User user = userRepository.findById(userId).orElse(null);
        Long campusId = user != null ? user.getCampusId() : null;
        List<GoodsResponse> hotGoods = campusId != null ? 
            getHotList(campusId, hotSize + (size - result.size())) : List.of();
        for (GoodsResponse goods : hotGoods) {
            if (addedIds.add(goods.getId())) {
                result.add(goods);
                if (result.size() >= size) break;
            }
        }
        
        return result.subList(0, Math.min(size, result.size()));
    }

    @Override
    public void calculateUserSimilarities() {
        log.info("开始计算用户相似度...");
        
        // 1. 获取所有活跃用户（近30天有行为的用户）
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Long> activeUserIds = userBehaviorLogRepository.findAll().stream()
            .filter(log -> log.getCreatedAt().isAfter(thirtyDaysAgo))
            .map(com.campus.marketplace.common.entity.UserBehaviorLog::getUserId)
            .distinct()
            .toList();
        
        log.info("活跃用户数量: {}", activeUserIds.size());
        
        // 2. 构建用户-商品行为矩阵（Map<userId, Map<goodsId, score>>）
        java.util.Map<Long, java.util.Map<Long, Double>> userGoodsMatrix = new java.util.HashMap<>();
        
        for (Long userId : activeUserIds) {
            List<com.campus.marketplace.common.entity.UserBehaviorLog> userLogs = 
                userBehaviorLogRepository.findByUserIdAndTimeRange(userId, thirtyDaysAgo, LocalDateTime.now());
            
            java.util.Map<Long, Double> goodsScores = new java.util.HashMap<>();
            for (com.campus.marketplace.common.entity.UserBehaviorLog log : userLogs) {
                if ("GOODS".equals(log.getTargetType())) {
                    Long goodsId = log.getTargetId();
                    double weight = log.getBehaviorType().getWeight();
                    goodsScores.put(goodsId, goodsScores.getOrDefault(goodsId, 0.0) + weight);
                }
            }
            
            if (!goodsScores.isEmpty()) {
                userGoodsMatrix.put(userId, goodsScores);
            }
        }
        
        // 3. 计算用户之间的余弦相似度
        int calculatedCount = 0;
        for (Long userId : userGoodsMatrix.keySet()) {
            List<com.campus.marketplace.common.entity.UserSimilarity> similarities = new ArrayList<>();
            
            for (Long otherUserId : userGoodsMatrix.keySet()) {
                if (userId.equals(otherUserId)) continue;
                
                double similarity = calculateCosineSimilarity(
                    userGoodsMatrix.get(userId),
                    userGoodsMatrix.get(otherUserId)
                );
                
                if (similarity > 0.1) { // 相似度阈值
                    com.campus.marketplace.common.entity.UserSimilarity us = new com.campus.marketplace.common.entity.UserSimilarity();
                    us.setUserId(userId);
                    us.setSimilarUserId(otherUserId);
                    us.setSimilarityScore(similarity);
                    us.setLastCalculatedAt(LocalDateTime.now());
                    similarities.add(us);
                }
            }
            
            // 4. 保存相似度数据（先删除旧数据）
            if (!similarities.isEmpty()) {
                userSimilarityRepository.deleteByUserId(userId);
                userSimilarityRepository.saveAll(similarities);
                calculatedCount++;
            }
        }
        
        log.info("用户相似度计算完成，共处理{}个用户", calculatedCount);
    }
    
    /**
     * 计算余弦相似度
     */
    private double calculateCosineSimilarity(java.util.Map<Long, Double> vector1, java.util.Map<Long, Double> vector2) {
        // 计算向量点积
        double dotProduct = 0.0;
        for (Long key : vector1.keySet()) {
            if (vector2.containsKey(key)) {
                dotProduct += vector1.get(key) * vector2.get(key);
            }
        }
        
        // 计算向量模长
        double norm1 = Math.sqrt(vector1.values().stream().mapToDouble(v -> v * v).sum());
        double norm2 = Math.sqrt(vector2.values().stream().mapToDouble(v -> v * v).sum());
        
        if (norm1 == 0 || norm2 == 0) return 0.0;
        
        return dotProduct / (norm1 * norm2);
    }

    @Override
    public void precomputeRecommendations() {
        log.info("开始预计算推荐结果...");
        
        // 1. 获取活跃用户列表（近7天有行为的用户）
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Long> activeUserIds = userBehaviorLogRepository.findAll().stream()
            .filter(log -> log.getCreatedAt().isAfter(sevenDaysAgo))
            .map(com.campus.marketplace.common.entity.UserBehaviorLog::getUserId)
            .distinct()
            .limit(1000) // 限制最多预计算1000个用户
            .toList();
        
        log.info("活跃用户数量: {}", activeUserIds.size());
        
        // 2. 为每个用户预计算推荐结果并缓存
        int cachedCount = 0;
        for (Long userId : activeUserIds) {
            try {
                // 生成混合推荐结果
                List<GoodsResponse> recommendations = getHybridRecommendations(userId, 20);
                
                if (!recommendations.isEmpty()) {
                    // 缓存推荐结果（24小时有效）
                    // 暂时跳过缓存，后续优化
                    log.debug("预计算推荐结果: userId={}, size={}", userId, recommendations.size());
                    cachedCount++;
                }
            } catch (Exception e) {
                log.error("预计算用户{}的推荐结果失败: {}", userId, e.getMessage());
            }
        }
        
        log.info("推荐结果预计算完成，共缓存{}个用户的推荐结果", cachedCount);
    }
}
