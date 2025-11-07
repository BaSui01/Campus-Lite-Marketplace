package com.campus.marketplace.common.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 黑名单统计响应
 *
 * @author BaSui 😎
 * @date 2025-11-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "黑名单统计数据")
public class BlacklistStatsResponse {

    @Schema(description = "黑名单总数", example = "1250")
    private long totalBlacklists;

    @Schema(description = "活跃拉黑者数量（有拉黑行为的用户数）", example = "320")
    private long activeBlockers;

    @Schema(description = "被拉黑最多的用户ID", example = "10086")
    private long mostBlockedUserId;

    @Schema(description = "被拉黑最多的用户被拉黑次数", example = "45")
    private long mostBlockedCount;
}
