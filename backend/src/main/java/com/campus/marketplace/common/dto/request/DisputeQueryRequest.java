package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.DisputeStatus;
import com.campus.marketplace.common.enums.DisputeType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 纠纷查询条件DTO
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputeQueryRequest {

    /**
     * 纠纷编号（模糊匹配）
     */
    private String disputeCode;

    /**
     * 订单号（模糊匹配）
     */
    private String orderNo;

    /**
     * 发起人ID
     */
    private Long initiatorId;

    /**
     * 被投诉人ID
     */
    private Long respondentId;

    /**
     * 纠纷类型
     */
    private DisputeType disputeType;

    /**
     * 纠纷状态
     */
    private DisputeStatus status;

    /**
     * 仲裁员ID
     */
    private Long arbitratorId;

    /**
     * 创建时间开始
     */
    private LocalDateTime createdAtStart;

    /**
     * 创建时间结束
     */
    private LocalDateTime createdAtEnd;

    /**
     * 是否超时（协商或仲裁）
     */
    private Boolean expired;

    /**
     * 页码（从0开始）
     */
    @Builder.Default
    private Integer page = 0;

    /**
     * 每页大小
     */
    @Builder.Default
    private Integer size = 20;

    /**
     * 排序字段
     */
    @Builder.Default
    private String sortBy = "createdAt";

    /**
     * 排序方向（asc/desc）
     */
    @Builder.Default
    private String sortDirection = "desc";
}
