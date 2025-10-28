package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

/**
 * 收藏实体
 * 
 * 存储用户收藏的物品信息
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Entity
@Table(name = "t_favorite", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "goods_id"}),
       indexes = {
           @Index(name = "idx_favorite_user", columnList = "user_id"),
           @Index(name = "idx_favorite_goods", columnList = "goods_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted = false")
public class Favorite extends BaseEntity {

    /**
     * 用户 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 用户（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 物品 ID
     */
    @Column(name = "goods_id", nullable = false)
    private Long goodsId;

    /**
     * 物品（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", insertable = false, updatable = false)
    private Goods goods;
}
