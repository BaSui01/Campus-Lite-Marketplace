package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CreateTagRequest;
import com.campus.marketplace.common.dto.request.MergeTagRequest;
import com.campus.marketplace.common.dto.request.UpdateTagRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.TagResponse;
import com.campus.marketplace.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签管理控制器
 *
 * 提供标签的查询、创建、更新、删除与合并接口
 *
 * @author BaSui
 * @date 2025-10-27
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "标签管理", description = "标签查询、创建、更新与合并接口")
public class TagController {

    private final TagService tagService;

    @GetMapping("/api/tags")
    @Operation(summary = "获取标签列表", description = "返回系统中全部标签")
    public ApiResponse<List<TagResponse>> listTags() {
        return ApiResponse.success(tagService.listAllTags());
    }

    @PostMapping("/api/admin/tags")
    @PreAuthorize("hasAuthority('system:tag:manage')")
    @Operation(summary = "创建标签", description = "管理员新增标签")
    public ApiResponse<Long> createTag(@Valid @RequestBody CreateTagRequest request) {
        return ApiResponse.success(tagService.createTag(request));
    }

    @PutMapping("/api/admin/tags/{id}")
    @PreAuthorize("hasAuthority('system:tag:manage')")
    @Operation(summary = "更新标签", description = "管理员修改标签名称或状态")
    public ApiResponse<Void> updateTag(@PathVariable Long id,
                                       @Valid @RequestBody UpdateTagRequest request) {
        tagService.updateTag(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/api/admin/tags/{id}")
    @PreAuthorize("hasAuthority('system:tag:manage')")
    @Operation(summary = "删除标签", description = "管理员删除标签，删除前需确保无绑定")
    public ApiResponse<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/api/admin/tags/merge")
    @PreAuthorize("hasAuthority('system:tag:manage')")
    @Operation(summary = "合并标签", description = "将来源标签合并至目标标签")
    public ApiResponse<Void> mergeTag(@Valid @RequestBody MergeTagRequest request) {
        tagService.mergeTag(request);
        return ApiResponse.success(null);
    }
}
