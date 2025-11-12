package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CreateTagRequest;
import com.campus.marketplace.common.dto.request.MergeTagRequest;
import com.campus.marketplace.common.dto.request.UpdateTagRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.TagResponse;
import com.campus.marketplace.common.dto.response.TagStatisticsResponse;
import com.campus.marketplace.common.entity.Tag;
import com.campus.marketplace.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🏷️ BaSui 的标签管理控制器 - 管理商品标签！😎
 *
 * 功能范围：
 * - 🏷️ 标签管理：查询、创建、编辑、删除、启用/禁用
 * - 🔀 标签合并：将重复标签合并到目标标签
 * - 📊 统计分析：标签关联商品统计
 *
 * ⚠️ 注意：
 * - 所有接口仅管理员可访问
 * - 删除标签前需确保无关联商品
 * - 合并标签会自动处理重复绑定
 *
 * @author BaSui
 * @date 2025-11-07
 */
@RestController
@RequestMapping("/admin/tags")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "标签管理", description = "管理员后台标签管理相关接口")
public class TagAdminController {

    private final TagService tagService;

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_TAG_VIEW)")
    @Operation(summary = "查询标签列表", description = "支持分页和筛选的标签列表")
    public ApiResponse<org.springframework.data.domain.Page<TagResponse>> listTags(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "启用状态") @RequestParam(required = false) Boolean enabled,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size
    ) {
        org.springframework.data.domain.Page<TagResponse> tags = tagService.listTags(keyword, enabled, page, size);
        return ApiResponse.success(tags);
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_TAG_CREATE)")
    @Operation(summary = "创建标签", description = "创建新的商品标签")
    public ApiResponse<Long> createTag(@Valid @RequestBody CreateTagRequest request) {
        Long tagId = tagService.createTag(request);
        return ApiResponse.success(tagId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_TAG_VIEW)")
    @Operation(summary = "获取标签详情", description = "根据ID获取标签详细信息")
    public ApiResponse<Tag> getTagById(
            @Parameter(description = "标签ID", example = "1") @PathVariable Long id
    ) {
        Tag tag = tagService.getById(id);
        return ApiResponse.success(tag);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_TAG_EDIT)")
    @Operation(summary = "编辑标签", description = "更新标签信息")
    public ApiResponse<Void> updateTag(
            @Parameter(description = "标签ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody UpdateTagRequest request
    ) {
        tagService.updateTag(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_TAG_DELETE)")
    @Operation(summary = "删除标签", description = "删除指定标签（需确保无关联商品）")
    public ApiResponse<Void> deleteTag(
            @Parameter(description = "标签ID", example = "1") @PathVariable Long id
    ) {
        tagService.deleteTag(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_TAG_EDIT)")
    @Operation(summary = "切换启用状态", description = "切换标签的启用/禁用状态")
    public ApiResponse<Void> toggleEnabled(
            @Parameter(description = "标签ID", example = "1") @PathVariable Long id
    ) {
        tagService.toggleEnabled(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/batch/delete")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_TAG_DELETE)")
    @Operation(summary = "批量删除标签", description = "批量删除多个标签（跳过有关联商品的标签）")
    public ApiResponse<Integer> batchDeleteTags(
            @Parameter(description = "标签ID列表") @RequestBody List<Long> ids
    ) {
        int count = tagService.batchDelete(ids);
        return ApiResponse.success(count);
    }

    @PostMapping("/merge")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_TAG_EDIT)")
    @Operation(summary = "合并标签", description = "将源标签合并到目标标签（自动处理重复绑定）")
    public ApiResponse<Void> mergeTags(@Valid @RequestBody MergeTagRequest request) {
        tagService.mergeTag(request);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_TAG_VIEW)")
    @Operation(summary = "标签统计", description = "获取标签关联商品统计数据")
    public ApiResponse<TagStatisticsResponse> getTagStatistics(
            @Parameter(description = "标签ID", example = "1") @PathVariable Long id
    ) {
        TagStatisticsResponse statistics = tagService.getStatistics(id);
        return ApiResponse.success(statistics);
    }

    @GetMapping("/hot")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_TAG_VIEW)")
    @Operation(summary = "热门标签", description = "获取热门标签列表（按使用次数排序）")
    public ApiResponse<List<TagStatisticsResponse>> getHotTags(
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "20") int limit
    ) {
        List<TagStatisticsResponse> hotTags = tagService.getHotTags(limit);
        return ApiResponse.success(hotTags);
    }
}
