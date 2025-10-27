package com.campus.marketplace.common.dto.response;

import com.campus.marketplace.common.enums.GoodsStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 物品详情响应 DTO
 * 
 * 用于物品详情展示
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsDetailResponse {

    /**
     * 物品 ID
     */
    private Long id;

    /**
     * 物品标题
     */
    private String title;

    /**
     * 物品描述（完整版）
     */
    private String description;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 分类 ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 物品状态
     */
    private GoodsStatus status;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 收藏量
     */
    private Integer favoriteCount;

    /**
     * 所有图片
     */
    private List<String> images;

    /**
     * 标签列表
     */
    private List<TagResponse> tags;

    /**
     * 卖家信息
     */
    private SellerInfo seller;

    /**
     * 发布时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 卖家信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SellerInfo {
        /**
         * 卖家 ID
         */
        private Long id;

        /**
         * 卖家用户名
         */
        private String username;

        /**
         * 卖家头像
         */
        private String avatar;

        /**
         * 卖家积分
         */
        private Integer points;

        /**
         * 卖家手机号（脱敏）
         */
        private String phone;

        /**
         * 卖家邮箱（脱敏）
         */
        private String email;
    }
}
