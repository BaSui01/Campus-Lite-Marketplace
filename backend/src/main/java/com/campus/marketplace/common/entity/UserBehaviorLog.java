package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.BehaviorType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.util.Map;

/**
 * 用户行为日志实体
 *
 * 记录用户在平台上的各种行为（浏览、搜索、收藏、购买等），
 * 用于用户画像构建、行为分析和个性化推荐。
 *
 * 关键特性：
 * - 使用联合索引 (user_id, behavior_type, created_at) 加速查询
 * - 使用 JSONB 存储扩展数据，避免频繁修改表结构
 * - 数据保留90天，超期归档到冷存储
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Entity
@Table(name = "t_user_behavior_log", indexes = {
        @Index(name = "idx_user_behavior_composite", columnList = "user_id,behavior_type,created_at"),
        @Index(name = "idx_user_behavior_log_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted = false")
public class UserBehaviorLog extends BaseEntity {

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 行为类型（浏览/搜索/收藏/购买/点击/分享/评论/点赞）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "behavior_type", nullable = false, length = 20)
    private BehaviorType behaviorType;

    /**
     * 目标类型（Goods/Post/User）
     */
    @Column(name = "target_type", length = 20)
    private String targetType;

    /**
     * 目标ID（商品ID/帖子ID/用户ID）
     */
    @Column(name = "target_id")
    private Long targetId;

    /**
     * 来源（搜索/推荐/直接访问/商品详情页）
     */
    @Column(name = "source", length = 50)
    private String source;

    /**
     * 浏览时长（秒），仅浏览行为有效
     */
    @Column(name = "duration")
    private Integer duration;

    /**
     * 额外数据（JSON存储）
     *
     * 示例：
     * - 搜索行为：{"keyword": "iPhone 13", "categoryId": 1, "priceRange": "5000-8000"}
     * - 浏览行为：{"scrollDepth": 80, "exitPoint": "商品详情页"}
     * - 购买行为：{"orderId": 123, "amount": 5999}
     *
     * 使用 Hibernate 6+ 标准的 @JdbcTypeCode 注解，支持多数据库
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_data")
    private Map<String, Object> extraData;

    /**
     * 会话ID（用于追踪同一会话的行为序列）
     */
    @Column(name = "session_id", length = 100)
    private String sessionId;

    /**
     * 设备类型（PC/Mobile/Tablet）
     */
    @Column(name = "device_type", length = 20)
    private String deviceType;
}
