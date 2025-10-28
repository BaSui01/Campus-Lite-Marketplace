package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

/**
 * 物品分类实体，支持软删除。
 *
 * @author BaSui
 * @date 2025-10-28
 */
@Entity
@Table(name = "t_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted = false")
public class Category extends BaseEntity {

    /**
     * 分类名称（唯一）
     */
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    /**
     * 分类描述
     */
    @Column(name = "description", length = 200)
    private String description;

    /**
     * 父级分类 ID
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 排序权重（数字越大越靠前）
     */
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

}
