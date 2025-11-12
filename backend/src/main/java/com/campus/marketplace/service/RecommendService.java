package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.RecommendConfigDTO;
import com.campus.marketplace.common.dto.RecommendStatisticsDTO;
import com.campus.marketplace.common.dto.response.GoodsResponse;

import java.util.List;

/**
 * 推荐与榜单服务
 *
 * - 热门榜单（按校区）
 * - 个性化推荐（协同过滤 + 基于内容 + 用户画像）
 *
 * @author BaSui
 * @date 2025-10-29
 * @enhanced 2025-11-04 - 添加协同过滤和用户画像集成
 */

public interface RecommendService {

    /**
     * 刷新指定校区的热门榜单并写入 Redis
     *
     * @param campusId 校区ID（可为空表示全局）
     * @param topN     TopN 数量
     */
    void refreshHotRanking(Long campusId, int topN);

    /**
     * 获取热门榜单（优先读缓存，降级到数据库）
     *
     * @param campusId 校区ID（可为空表示全局）
     * @param size     返回数量
     * @return 热门物品列表
     */
    List<GoodsResponse> getHotList(Long campusId, int size);

    /**
     * 获取个性化推荐（基于用户最近行为），无行为回退热门榜单
     *
     * @param size 数量
     * @return 推荐物品列表
     */
    List<GoodsResponse> getPersonalRecommendations(int size);

    /**
     * 协同过滤推荐（基于相似用户的行为）
     *
     * @param userId 用户ID
     * @param size   返回数量
     * @return 推荐商品列表
     */
    List<GoodsResponse> getCollaborativeFilteringRecommendations(Long userId, int size);

    /**
     * 基于内容的推荐（相似商品）
     *
     * @param goodsId 商品ID
     * @param size    返回数量
     * @return 相似商品列表
     */
    List<GoodsResponse> getSimilarGoods(Long goodsId, int size);

    /**
     * 混合推荐（结合协同过滤、内容推荐和用户画像）
     *
     * @param userId 用户ID
     * @param size   返回数量
     * @return 推荐商品列表
     */
    List<GoodsResponse> getHybridRecommendations(Long userId, int size);

    /**
     * 计算用户相似度（批量，定时任务调用）
     */
    void calculateUserSimilarities();

    /**
     * 预计算推荐结果（缓存预热，定时任务调用）
     */
    void precomputeRecommendations();

    /**
     * 🎯 BaSui 新增：获取推荐配置
     *
     * @return 推荐配置DTO
     */
    RecommendConfigDTO getRecommendConfig();

    /**
     * 🎯 BaSui 新增：更新推荐配置
     *
     * @param configDTO 推荐配置DTO
     */
    void updateRecommendConfig(RecommendConfigDTO configDTO);

    /**
     * 🎯 BaSui 新增：获取推荐统计
     *
     * @return 推荐统计DTO
     */
    RecommendStatisticsDTO getRecommendStatistics();
}
