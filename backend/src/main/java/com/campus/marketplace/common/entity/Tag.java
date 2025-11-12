package com.campus.marketplace.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 标签实体
 *
 * 用于对商品（及后续帖子）打标签，支持启停用与去重合并
 *
 * @author BaSui
 * @date 2025-10-27
 * @since 2025-10-28
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "t_tag", indexes = {
        @Index(name = "idx_tag_enabled", columnList = "enabled")
})
@SQLRestriction("deleted = false")
public class Tag extends BaseEntity {

    /**
     * 标签名称（唯一，小写存储便于去重）
     */
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    /**
     * 标签说明
     */
    @Column(name = "description", length = 200)
    private String description;

    /**
     * 是否启用
     *
     * 默认启用，便于新标签立即生效！😎
     */
    @Column(name = "enabled", nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean enabled = Boolean.TRUE;
}
