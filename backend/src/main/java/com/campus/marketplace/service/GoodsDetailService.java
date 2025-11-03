package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.GoodsDetailDTO;
import com.campus.marketplace.common.dto.response.GoodsResponse;

import java.util.List;

/**
 * 商品详情服务接口
 * 
 * 提供商品详情页所需的所有数据
 * 
 * @author BaSui
 * @date 2025-11-03
 */
public interface GoodsDetailService {

    /**
     * 获取商品详情（聚合所有数据）
     * 
     * @param goodsId 商品ID
     * @param userId 用户ID（可选，用于判断是否已收藏）
     * @return 商品详情
     */
    GoodsDetailDTO getGoodsDetail(Long goodsId, Long userId);

    /**
     * 获取相似商品推荐
     * 
     * @param goodsId 商品ID
     * @param limit 返回数量
     * @return 相似商品列表
     */
    List<GoodsResponse> getSimilarGoods(Long goodsId, int limit);

    /**
     * 记录商品浏览
     * 
     * @param goodsId 商品ID
     * @param userId 用户ID
     * @param sourcePage 来源页面
     */
    void recordView(Long goodsId, Long userId, String sourcePage);

    /**
     * 获取用户浏览历史
     * 
     * @param userId 用户ID
     * @param limit 返回数量
     * @return 浏览历史商品列表
     */
    List<GoodsResponse> getUserViewHistory(Long userId, int limit);

    /**
     * 清空用户浏览历史
     * 
     * @param userId 用户ID
     */
    void clearUserViewHistory(Long userId);
}
