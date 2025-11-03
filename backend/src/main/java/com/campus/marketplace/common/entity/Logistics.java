package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.LogisticsCompany;
import com.campus.marketplace.common.enums.LogisticsStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流信息实体
 *
 * 存储订单的物流跟踪信息，支持多家快递公司。
 * 使用 JSONB 存储物流轨迹，支持灵活查询。
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Entity
@Table(name = "t_logistics", indexes = {
        @Index(name = "idx_logistics_order", columnList = "order_id"),
        @Index(name = "idx_logistics_tracking", columnList = "tracking_number"),
        @Index(name = "idx_logistics_status_time", columnList = "status,last_sync_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted = false")
public class Logistics extends BaseEntity {

    /**
     * 关联订单ID
     */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /**
     * 快递单号
     */
    @Column(name = "tracking_number", nullable = false, length = 50)
    private String trackingNumber;

    /**
     * 快递公司（顺丰/中通/圆通等）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "logistics_company", nullable = false, length = 20)
    private LogisticsCompany logisticsCompany;

    /**
     * 物流状态（已揽件/运输中/派送中/已签收）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LogisticsStatus status;

    /**
     * 当前位置
     */
    @Column(name = "current_location", length = 200)
    private String currentLocation;

    /**
     * 预计送达时间
     */
    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    /**
     * 实际送达时间
     */
    @Column(name = "actual_delivery_time")
    private LocalDateTime actualDeliveryTime;

    /**
     * 是否超时
     */
    @Column(name = "is_overtime")
    @Builder.Default
    private Boolean isOvertime = false;

    /**
     * 物流轨迹（JSON存储）
     *
     * 使用 Hibernate 6+ 标准的 @JdbcTypeCode 注解，支持多数据库：
     * - PostgreSQL: 自动使用 jsonb 类型
     * - H2: 自动使用 varchar/clob 类型
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "track_records")
    private List<LogisticsTrackRecord> trackRecords;

    /**
     * 同步次数
     */
    @Column(name = "sync_count")
    @Builder.Default
    private Integer syncCount = 0;

    /**
     * 最后同步时间
     */
    @Column(name = "last_sync_time")
    private LocalDateTime lastSyncTime;
}
