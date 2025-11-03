package com.campus.marketplace.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物流轨迹记录（嵌套类，存储为JSON）
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsTrackRecord implements Serializable {

    /**
     * 时间
     */
    private LocalDateTime time;

    /**
     * 地点
     */
    private String location;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 操作员（可选）
     */
    private String operatorName;
}
