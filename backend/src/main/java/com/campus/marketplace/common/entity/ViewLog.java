package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 浏览足迹实体
 * 
 * 记录用户浏览商品的历史记录
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Entity
@Table(name = "t_view_log", indexes = {
    @Index(name = "idx_view_log_user", columnList = "user_id"),
    @Index(name = "idx_view_log_goods", columnList = "goods_id"),
    @Index(name = "idx_view_log_time", columnList = "created_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewLog extends BaseEntity {

    /**
     * 用户 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 商品 ID
     */
    @Column(name = "goods_id", nullable = false)
    private Long goodsId;

    /**
     * 浏览时长（秒）
     */
    @Column(name = "view_duration")
    private Integer viewDuration;

    /**
     * 来源页面
     */
    @Column(name = "source_page", length = 100)
    private String sourcePage;

    /**
     * 用户（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 商品（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", insertable = false, updatable = false)
    private Goods goods;
}
