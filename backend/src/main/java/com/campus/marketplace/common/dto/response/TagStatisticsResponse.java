package com.campus.marketplace.common.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 🏷️ BaSui 的标签统计响应 - 查看标签数据！😎
 *
 * @author BaSui
 * @date 2025-11-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "标签统计数据")
public class TagStatisticsResponse {

    @Schema(description = "标签ID", example = "1")
    private Long tagId;

    @Schema(description = "标签名称", example = "数码产品")
    private String tagName;

    @Schema(description = "关联商品总数", example = "128")
    private Long goodsCount;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;
}
