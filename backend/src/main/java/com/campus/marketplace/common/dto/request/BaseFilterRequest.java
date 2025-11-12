package com.campus.marketplace.common.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 通用筛选请求基类
 *
 * @author BaSui 😎
 * @date 2025-11-10
 * @description 所有列表查询接口的通用筛选参数基类，提供统一的分页、排序、搜索功能
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通用筛选请求参数")
public class BaseFilterRequest {

    /**
     * 关键词搜索（支持标题、描述等字段）
     */
    @Schema(description = "搜索关键词", example = "苹果笔记本")
    private String keyword;

    /**
     * 页码（从 0 开始）
     */
    @Schema(description = "页码（从 0 开始）", example = "0", defaultValue = "0")
    @lombok.Builder.Default
    private Integer page = 0;

    /**
     * 每页数量
     */
    @Schema(description = "每页数量", example = "20", defaultValue = "20")
    @lombok.Builder.Default
    private Integer size = 20;

    /**
     * 排序字段（如：createdAt、price、viewCount）
     */
    @Schema(description = "排序字段", example = "createdAt", defaultValue = "createdAt")
    @lombok.Builder.Default
    private String sortBy = "createdAt";

    /**
     * 排序方向（ASC/DESC）
     */
    @Schema(description = "排序方向", example = "DESC", defaultValue = "DESC", allowableValues = {"ASC", "DESC"})
    @lombok.Builder.Default
    private String sortDirection = "DESC";

    /**
     * 开始时间（时间范围筛选 - 起始）
     */
    @Schema(description = "开始时间", example = "2025-01-01T00:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    /**
     * 结束时间（时间范围筛选 - 结束）
     */
    @Schema(description = "结束时间", example = "2025-12-31T23:59:59")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;

    /**
     * 获取页码（确保不为 null）
     */
    public int getPageOrDefault() {
        return page != null ? page : 0;
    }

    /**
     * 获取每页数量（确保不为 null）
     */
    public int getSizeOrDefault() {
        return size != null ? size : 20;
    }

    /**
     * 获取排序字段（确保不为 null）
     */
    public String getSortByOrDefault() {
        return sortBy != null && !sortBy.isEmpty() ? sortBy : "createdAt";
    }

    /**
     * 获取排序方向（确保不为 null）
     */
    public String getSortDirectionOrDefault() {
        return sortDirection != null && !sortDirection.isEmpty() ? sortDirection : "DESC";
    }
}
