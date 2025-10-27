package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.PointsType;
import jakarta.persistence.*;
import lombok.*;

/**
 * 积分流水实体
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Entity
@Table(name = "t_points_log", indexes = {
        @Index(name = "idx_points_user", columnList = "user_id"),
        @Index(name = "idx_points_type", columnList = "type"),
        @Index(name = "idx_points_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsLog extends BaseEntity {

    /**
     * 用户 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 积分类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private PointsType type;

    /**
     * 积分变化（正数为增加，负数为减少）
     */
    @Column(name = "points", nullable = false)
    private Integer points;

    /**
     * 变化后的总积分
     */
    @Column(name = "balance", nullable = false)
    private Integer balance;

    /**
     * 描述
     */
    @Column(name = "description", length = 200)
    private String description;
}
