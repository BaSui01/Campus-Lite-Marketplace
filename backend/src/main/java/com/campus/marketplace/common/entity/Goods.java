package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.GoodsStatus;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 物品实体
 * 
 * 存储二手物品信息
 * 使用 JSONB 存储扩展属性
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Entity
@Table(name = "t_goods", indexes = {
        @Index(name = "idx_goods_seller", columnList = "seller_id"),
        @Index(name = "idx_goods_status", columnList = "status"),
        @Index(name = "idx_goods_category", columnList = "category_id"),
        @Index(name = "idx_goods_created_at", columnList = "created_at"),
        @Index(name = "idx_goods_campus", columnList = "campus_id"),
        @Index(name = "idx_goods_price", columnList = "price")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted = false")
public class Goods extends BaseEntity {

    /**
     * 物品标题
     */
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    /**
     * 物品描述
     */
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * 价格
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * 分类 ID
     */
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    /**
     * 分类（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;

    /**
     * 卖家 ID
     */
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    /**
     * 卖家（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", insertable = false, updatable = false)
    private User seller;

    /**
     * 校区 ID
     */
    @Column(name = "campus_id")
    private Long campusId;

    /**
     * 校区（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id", insertable = false, updatable = false)
    private Campus campus;

    /**
     * 物品状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private GoodsStatus status = GoodsStatus.PENDING;

    /**
     * 浏览量
     */
    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    /**
     * 收藏量
     */
    @Column(name = "favorite_count", nullable = false)
    @Builder.Default
    private Integer favoriteCount = 0;

    /**
     * 库存数量（前端需要）
     */
    @Column(name = "stock")
    @Builder.Default
    private Integer stock = 1;

    /**
     * 已售数量（前端需要）
     */
    @Column(name = "sold_count")
    @Builder.Default
    private Integer soldCount = 0;

    /**
     * 原价（用于显示折扣，前端需要）
     */
    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    /**
     * 商品成色（前端需要）
     * BRAND_NEW: 全新
     * LIKE_NEW: 几乎全新
     * LIGHTLY_USED: 轻微使用痕迹
     * WELL_USED: 明显使用痕迹
     * HEAVILY_USED: 重度使用痕迹
     */
    @Column(name = "condition", length = 50)
    private String condition;

    /**
     * 交易方式（前端需要）
     * MEET: 校园面交
     * MAIL: 快递邮寄
     * 可以是多个，用逗号分隔：MEET,MAIL
     */
    @Column(name = "delivery_method", length = 100)
    private String deliveryMethod;

    /**
     * 图片 URL 数组
     */
    @JdbcTypeCode(SqlTypes.ARRAY) // ✅ 修复：明确映射 PostgreSQL text[] → Java String[]
    @Column(name = "images", columnDefinition = "TEXT[]")
    private String[] images;

    /**
     * 扩展属性（JSONB）
     * 用于存储不同分类的特殊属性
     */
    @Type(JsonBinaryType.class)
    @Column(name = "extra_attrs", columnDefinition = "jsonb")
    private Map<String, Object> extraAttrs;

    /**
     * 标签绑定列表（懒加载）
     */
    @OneToMany(mappedBy = "goods", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GoodsTag> goodsTags = new ArrayList<>();

    /**
     * 增加浏览量
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 增加收藏量
     */
    public void incrementFavoriteCount() {
        this.favoriteCount++;
    }

    /**
     * 减少收藏量
     */
    public void decrementFavoriteCount() {
        if (this.favoriteCount > 0) {
            this.favoriteCount--;
        }
    }

    /**
     * 检查是否已售出
     */
    public boolean isSold() {
        return this.status == GoodsStatus.SOLD;
    }

    /**
     * 检查是否已审核通过
     */
    public boolean isApproved() {
        return this.status == GoodsStatus.APPROVED;
    }

    /**
     * 减少库存（下单时调用）
     */
    public void decreaseStock(int quantity) {
        if (this.stock != null && this.stock >= quantity) {
            this.stock -= quantity;
        }
    }

    /**
     * 增加库存（取消订单时调用）
     */
    public void increaseStock(int quantity) {
        if (this.stock == null) {
            this.stock = quantity;
        } else {
            this.stock += quantity;
        }
    }

    /**
     * 增加销量（订单完成时调用）
     */
    public void incrementSoldCount() {
        if (this.soldCount == null) {
            this.soldCount = 1;
        } else {
            this.soldCount++;
        }
    }
}
