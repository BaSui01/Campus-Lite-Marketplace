package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

/**
 * 商品与标签关联实体。
 *
 * @author BaSui
 * @date 2025-10-28
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "t_goods_tag",
        uniqueConstraints = @UniqueConstraint(name = "uk_goods_tag", columnNames = {"goods_id", "tag_id"}),
        indexes = {
                @Index(name = "idx_goods_tag_goods", columnList = "goods_id"),
                @Index(name = "idx_goods_tag_tag", columnList = "tag_id")
        })
@SQLRestriction("deleted = false")
public class GoodsTag extends BaseEntity {

    /**
     * 商品 ID
     */
    @Column(name = "goods_id", nullable = false)
    private Long goodsId;

    /**
     * 标签 ID
     */
    @Column(name = "tag_id", nullable = false)
    private Long tagId;

    /**
     * 商品实体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", insertable = false, updatable = false)
    private Goods goods;

    /**
     * 标签实体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", insertable = false, updatable = false)
    private Tag tag;
}
