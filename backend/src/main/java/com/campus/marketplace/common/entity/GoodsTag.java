package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 商品与标签关联实体
 *
 * 保存绑定关系并保留审计信息，便于统计与去重
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
@Table(name = "t_goods_tag",
        uniqueConstraints = @UniqueConstraint(name = "uk_goods_tag", columnNames = {"goods_id", "tag_id"}),
        indexes = {
                @Index(name = "idx_goods_tag_goods", columnList = "goods_id"),
                @Index(name = "idx_goods_tag_tag", columnList = "tag_id")
        })
public class GoodsTag extends BaseEntity {

    @Column(name = "goods_id", nullable = false)
    private Long goodsId;

    @Column(name = "tag_id", nullable = false)
    private Long tagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", insertable = false, updatable = false)
    private Goods goods;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", insertable = false, updatable = false)
    private Tag tag;
}
