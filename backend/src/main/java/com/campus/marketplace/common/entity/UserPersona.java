package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户画像实体
 *
 * 基于用户行为数据构建的用户画像，包含兴趣标签、价格偏好、活跃时段等信息。
 * 用于个性化推荐、精准营销和用户分群。
 *
 * 关键特性：
 * - userId 唯一索引，每个用户只有一个画像
 * - 使用 JSONB 存储兴趣标签、价格偏好等灵活数据
 * - 数据每天更新一次，减少计算开销
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Entity
@Table(name = "t_user_persona", indexes = {
        @Index(name = "idx_persona_user", columnList = "user_id", unique = true),
        @Index(name = "idx_persona_segment", columnList = "user_segment")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted = false")
public class UserPersona extends BaseEntity {

    /**
     * 用户ID（唯一索引）
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /**
     * 兴趣标签及权重（JSON存储）
     *
     * 示例：{"电子产品": 0.8, "图书": 0.6, "运动器材": 0.3}
     * - 键：标签名称
     * - 值：兴趣权重（0.0-1.0）
     *
     * 权重根据用户行为频次动态调整
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "interest_tags")
    private Map<String, Double> interestTags;

    /**
     * 价格偏好（JSON存储）
     *
     * 示例：{"preferredRange": "50-200", "avgSpending": 150, "maxSpending": 500}
     * - preferredRange: 偏好价格区间
     * - avgSpending: 平均消费金额
     * - maxSpending: 最高消费金额
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "price_preference")
    private Map<String, Object> pricePreference;

    /**
     * 活跃时段（JSON存储）
     *
     * 示例：["08:00-12:00", "18:00-22:00"]
     * 表示用户通常在早上8-12点和晚上6-10点活跃
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "active_time_slots")
    private List<String> activeTimeSlots;

    /**
     * 校区偏好（本部/东校区/西校区）
     */
    @Column(name = "campus_preference", length = 50)
    private String campusPreference;

    /**
     * 偏好分类（JSON存储）
     *
     * 示例：["电子产品", "图书", "运动器材"]
     * 根据浏览和购买历史统计
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "favorite_categories")
    private List<String> favoriteCategories;

    /**
     * 偏好品牌（JSON存储）
     *
     * 示例：["Apple", "华为", "小米"]
     * 根据浏览和购买历史统计
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "favorite_brands")
    private List<String> favoriteBrands;

    /**
     * 用户分群（高价值用户/活跃用户/沉睡用户/新用户/潜在流失用户）
     */
    @Column(name = "user_segment", length = 50)
    private String userSegment;

    /**
     * 最后更新时间（用于判断是否需要重新计算画像）
     */
    @Column(name = "last_updated_time")
    private LocalDateTime lastUpdatedTime;
}
