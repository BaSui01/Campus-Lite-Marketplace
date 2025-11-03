package com.campus.marketplace.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 用户行为类型枚举
 *
 * 用于记录用户在平台上的各种行为，用于构建用户画像和个性化推荐。
 * 不同行为类型具有不同的权重，用于计算用户对商品的兴趣度。
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Getter
@RequiredArgsConstructor
public enum BehaviorType {

    /**
     * 浏览商品（权重1）
     */
    VIEW("浏览", 1, false),

    /**
     * 搜索商品（权重2）
     */
    SEARCH("搜索", 2, false),

    /**
     * 收藏商品（权重5）
     */
    FAVORITE("收藏", 5, true),

    /**
     * 购买商品（权重10）
     */
    PURCHASE("购买", 10, true),

    /**
     * 点击推荐位（权重2）
     */
    CLICK("点击", 2, false),

    /**
     * 分享商品（权重3）
     */
    SHARE("分享", 3, false),

    /**
     * 评论商品（权重3）
     */
    COMMENT("评论", 3, false),

    /**
     * 点赞商品（权重1）
     */
    LIKE("点赞", 1, false);

    /**
     * 行为类型显示名称
     */
    private final String displayName;

    /**
     * 行为权重（用于计算兴趣度）
     */
    private final int weight;

    /**
     * 是否为高价值行为（购买和收藏）
     */
    private final boolean highValue;
}
