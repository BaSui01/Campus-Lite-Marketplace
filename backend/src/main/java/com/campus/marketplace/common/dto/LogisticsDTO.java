package com.campus.marketplace.common.dto;

import com.campus.marketplace.common.entity.LogisticsTrackRecord;
import com.campus.marketplace.common.enums.LogisticsCompany;
import com.campus.marketplace.common.enums.LogisticsStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流信息DTO
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsDTO {

    /**
     * 物流ID
     */
    private Long id;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 快递单号
     */
    private String trackingNumber;

    /**
     * 快递公司
     */
    private LogisticsCompany logisticsCompany;

    /**
     * 物流状态
     */
    private LogisticsStatus status;

    /**
     * 当前位置
     */
    private String currentLocation;

    /**
     * 预计送达时间
     */
    private LocalDateTime estimatedDeliveryTime;

    /**
     * 实际送达时间
     */
    private LocalDateTime actualDeliveryTime;

    /**
     * 是否超时
     */
    private Boolean isOvertime;

    /**
     * 物流轨迹列表
     */
    private List<LogisticsTrackRecord> trackRecords;

    /**
     * 同步次数
     */
    private Integer syncCount;

    /**
     * 最后同步时间
     */
    private LocalDateTime lastSyncTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
