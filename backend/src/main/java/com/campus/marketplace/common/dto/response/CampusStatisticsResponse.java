package com.campus.marketplace.common.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 📊 BaSui 的校园统计响应 - 查看校园数据！😎
 *
 * @author BaSui
 * @date 2025-11-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "校园统计数据")
public class CampusStatisticsResponse {

    @Schema(description = "校园ID", example = "1")
    private Long campusId;

    @Schema(description = "校园名称", example = "北京大学")
    private String campusName;

    @Schema(description = "用户总数", example = "1250")
    private Long userCount;

    @Schema(description = "商品总数", example = "3680")
    private Long goodsCount;

    @Schema(description = "订单总数", example = "5420")
    private Long orderCount;

    @Schema(description = "活跃用户数（30天内）", example = "850")
    private Long activeUserCount;
}
