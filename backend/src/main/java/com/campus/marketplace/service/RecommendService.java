package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.GoodsResponse;

import java.util.List;

/**
 * 推荐与榜单服务
 *
 * - 热门榜单（按校区）
 * - 个性化推荐（基于浏览/收藏召回，冷启动回退热榜）
 *
 * @author BaSui
 * @date 2025-10-29
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
}
