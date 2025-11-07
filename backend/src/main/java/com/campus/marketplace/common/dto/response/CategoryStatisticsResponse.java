package com.campus.marketplace.common.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 📊 BaSui 的分类统计响应 - 查看分类数据！😎
 *
 * @author BaSui
 * @date 2025-11-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分类统计数据")
public class CategoryStatisticsResponse {

    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "分类名称", example = "数码产品")
    private String categoryName;

    @Schema(description = "商品总数", example = "245")
    private Long goodsCount;

    @Schema(description = "子分类数量", example = "5")
    private Long childrenCount;

    @Schema(description = "在售商品数", example = "180")
    private Long onSaleGoodsCount;

    @Schema(description = "已售商品数", example = "65")
    private Long soldGoodsCount;
}
