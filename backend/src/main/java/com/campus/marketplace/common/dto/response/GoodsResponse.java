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
 * 物品响应 DTO
 * 
 * 用于物品列表展示
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsResponse {

    /**
     * 物品 ID
     */
    private Long id;

    /**
     * 物品标题
     */
    private String title;

    /**
     * 物品描述（简短版）
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
     * 卖家 ID
     */
    private Long sellerId;

    /**
     * 卖家用户名
     */
    private String sellerUsername;

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
     * 封面图片（第一张图片）
     */
    private String coverImage;

    /**
     * 发布时间
     */
    private LocalDateTime createdAt;
}
