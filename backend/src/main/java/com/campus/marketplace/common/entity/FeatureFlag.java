package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

/**
 * Feature Flag - 功能开关
 * 
 * 用于灰度发布、A/B测试等场景
 * 支持软删除，便于审计和回滚
 *
 * @author BaSui
 * @date 2025-10-29
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_feature_flag", indexes = {
        @Index(name = "uk_feature_key", columnList = "feature_key", unique = true)
})
@SQLRestriction("deleted = false")
public class FeatureFlag extends BaseEntity {

    /**
     * 功能开关Key（唯一标识）
     */
    @Column(name = "feature_key", nullable = false, unique = true, length = 128)
    private String key;

    /**
     * 是否启用
     */
    @Column(nullable = false)
    private boolean enabled;

    /**
     * 规则配置（JSON格式）
     * 可配置：目标用户、灰度比例、生效时间等
     */
    @Column(columnDefinition = "TEXT")
    private String rulesJson;

    /**
     * 功能描述
     */
    @Column(length = 255)
    private String description;
}
