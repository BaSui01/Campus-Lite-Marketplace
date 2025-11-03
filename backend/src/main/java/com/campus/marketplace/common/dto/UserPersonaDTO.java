package com.campus.marketplace.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户画像DTO
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPersonaDTO {

    /**
     * 画像ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 兴趣标签及权重
     * 示例：{"电子产品": 0.8, "图书": 0.6, "运动器材": 0.3}
     */
    private Map<String, Double> interestTags;

    /**
     * 价格偏好
     * 示例：{"preferredRange": "50-200", "avgSpending": 150, "maxSpending": 500}
     */
    private Map<String, Object> pricePreference;

    /**
     * 活跃时段
     * 示例：["08:00-12:00", "18:00-22:00"]
     */
    private List<String> activeTimeSlots;

    /**
     * 校区偏好
     */
    private String campusPreference;

    /**
     * 偏好分类
     */
    private List<String> favoriteCategories;

    /**
     * 偏好品牌
     */
    private List<String> favoriteBrands;

    /**
     * 用户分群（高价值用户/活跃用户/沉睡用户/新用户/潜在流失用户）
     */
    private String userSegment;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdatedTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
