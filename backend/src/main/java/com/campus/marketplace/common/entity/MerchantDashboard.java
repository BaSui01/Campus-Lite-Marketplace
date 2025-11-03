package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 商家数据看板实体
 *
 * 按日期统计商家的销售数据、访客数据等，用于商家数据看板展示。
 *
 * @author BaSui 😎
 * @since 2025-11-04
 */
@Entity
@Table(name = "t_merchant_dashboard", indexes = {
        @Index(name = "idx_dashboard_merchant_date", columnList = "merchant_id,stat_date", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted = false")
public class MerchantDashboard extends BaseEntity {

    /**
     * 商家ID
     */
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    /**
     * 统计日期
     */
    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    /**
     * 销售额
     */
    @Column(name = "sales_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal salesAmount = BigDecimal.ZERO;

    /**
     * 订单数
     */
    @Column(name = "order_count")
    @Builder.Default
    private Integer orderCount = 0;

    /**
     * 访客数
     */
    @Column(name = "visitor_count")
    @Builder.Default
    private Integer visitorCount = 0;

    /**
     * 新访客数
     */
    @Column(name = "new_visitor_count")
    @Builder.Default
    private Integer newVisitorCount = 0;

    /**
     * 浏览量
     */
    @Column(name = "page_view_count")
    @Builder.Default
    private Integer pageViewCount = 0;

    /**
     * 转化率（购买数/访客数）
     */
    @Column(name = "conversion_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal conversionRate = BigDecimal.ZERO;

    /**
     * 访客来源（JSONB）
     * 示例：{"搜索": 100, "推荐": 50, "直接访问": 30}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "visitor_sources")
    private Map<String, Integer> visitorSources;

    /**
     * 热销商品ID列表（JSONB）
     * Top 10商品
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "top_selling_goods")
    private List<Long> topSellingGoods;
}
