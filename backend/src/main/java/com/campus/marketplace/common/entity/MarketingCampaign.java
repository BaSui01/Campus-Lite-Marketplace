package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 营销活动实体
 *
 * 支持限时折扣、满减、秒杀等多种营销活动类型。
 *
 * @author BaSui 😎
 * @since 2025-11-04
 */
@Entity
@Table(name = "t_marketing_campaign", indexes = {
        @Index(name = "idx_campaign_merchant", columnList = "merchant_id,status"),
        @Index(name = "idx_campaign_time", columnList = "start_time,end_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted = false")
public class MarketingCampaign extends BaseEntity {

    /**
     * 商家ID
     */
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    /**
     * 活动名称
     */
    @Column(name = "campaign_name", nullable = false, length = 100)
    private String campaignName;

    /**
     * 活动类型：DISCOUNT(折扣)/FULL_REDUCTION(满减)/FLASH_SALE(秒杀)
     */
    @Column(name = "campaign_type", nullable = false, length = 20)
    private String campaignType;

    /**
     * 开始时间
     */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /**
     * 活动状态：PENDING(待审核)/APPROVED(已通过)/RUNNING(进行中)/PAUSED(已暂停)/ENDED(已结束)
     */
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    /**
     * 折扣配置（JSONB）
     * 示例：{"discountType": "PERCENTAGE", "discountValue": 0.8, "threshold": 100}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "discount_config")
    private Map<String, Object> discountConfig;

    /**
     * 参与商品ID列表（JSONB）
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "goods_ids")
    private List<Long> goodsIds;

    /**
     * 库存限制
     */
    @Column(name = "stock_limit")
    private Integer stockLimit;

    /**
     * 剩余库存
     */
    @Column(name = "stock_remaining")
    private Integer stockRemaining;

    /**
     * 参与人数
     */
    @Column(name = "participation_count")
    @Builder.Default
    private Integer participationCount = 0;

    /**
     * 活动销售额
     */
    @Column(name = "sales_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal salesAmount = BigDecimal.ZERO;

    /**
     * 创建人ID
     */
    @Column(name = "created_by")
    private Long createdBy;
}
