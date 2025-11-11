package com.campus.marketplace.common.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 撤销统计数据 DTO
 * 
 * @author BaSui 😎
 * @date 2025-11-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "撤销统计数据")
public class RevertStatistics {

    @Schema(description = "待审批数量")
    private Long pendingCount;

    @Schema(description = "今日撤销数量")
    private Long todayRevertCount;

    @Schema(description = "成功率（百分比）")
    private Double successRate;
}
