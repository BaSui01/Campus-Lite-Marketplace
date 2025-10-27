package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.GoodsResponse;
import org.springframework.data.domain.Page;

/**
 * 收藏服务接口
 * 
 * @author BaSui
 * @date 2025-10-27
 */
public interface FavoriteService {

    /**
     * 添加收藏
     * 
     * @param goodsId 物品 ID
     */
    void addFavorite(Long goodsId);

    /**
     * 取消收藏
     * 
     * @param goodsId 物品 ID
     */
    void removeFavorite(Long goodsId);

    /**
     * 查询收藏列表
     * 
     * @param page 页码（从 0 开始）
     * @param size 每页数量
     * @return 收藏的物品列表（分页）
     */
    Page<GoodsResponse> listFavorites(int page, int size);

    /**
     * 检查是否已收藏
     * 
     * @param goodsId 物品 ID
     * @return 是否已收藏
     */
    boolean isFavorited(Long goodsId);
}
