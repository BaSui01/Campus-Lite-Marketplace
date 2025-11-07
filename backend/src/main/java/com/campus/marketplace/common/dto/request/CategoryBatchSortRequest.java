package com.campus.marketplace.common.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 📦 BaSui 的分类批量排序请求 - 拖拽排序走起！😎
 *
 * @author BaSui
 * @date 2025-11-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分类批量排序请求")
public class CategoryBatchSortRequest {

    @NotEmpty(message = "排序项不能为空")
    @Schema(description = "排序项列表", required = true)
    private List<SortItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "排序项")
    public static class SortItem {

        @NotNull(message = "分类ID不能为空")
        @Schema(description = "分类ID", example = "1", required = true)
        private Long categoryId;

        @NotNull(message = "排序值不能为空")
        @Schema(description = "排序值（数字越大越靠前）", example = "100", required = true)
        private Integer sortOrder;
    }
}
