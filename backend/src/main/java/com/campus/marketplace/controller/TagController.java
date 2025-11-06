package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CreateTagRequest;
import com.campus.marketplace.common.dto.request.MergeTagRequest;
import com.campus.marketplace.common.dto.request.UpdateTagRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.TagResponse;
import com.campus.marketplace.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
 * @date 2025-10-29
 */

@RestController
@RequiredArgsConstructor
@Tag(name = "标签管理", description = "标签查询、创建、更新与合并接口")
public class TagController {

    private final TagService tagService;

    @GetMapping("/tags")
    @Operation(summary = "获取标签列表", description = "返回系统中全部标签")
    public ApiResponse<List<TagResponse>> listTags() {
        return ApiResponse.success(tagService.listAllTags());
    }

    @PostMapping("/admin/tags")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_TAG_MANAGE)")
    @Operation(summary = "创建标签", description = "管理员新增标签")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateTagRequest.class),
                    examples = @ExampleObject(
                            name = "请求示例",
                            value = """
                                    {
                                      \"name\": \"数码\",
                                      \"description\": \"电子数码相关\",
                                      \"enabled\": true
                                    }
                                    """
                    )
            )
    )
    public ApiResponse<Long> createTag(@Valid @RequestBody CreateTagRequest request) {
        return ApiResponse.success(tagService.createTag(request));
    }

    @PutMapping("/admin/tags/{id}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_TAG_MANAGE)")
    @Operation(summary = "更新标签", description = "管理员修改标签名称或状态")
    public ApiResponse<Void> updateTag(@Parameter(description = "标签ID", example = "101") @PathVariable Long id,
                                       @Valid @RequestBody UpdateTagRequest request) {
        tagService.updateTag(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/admin/tags/{id}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_TAG_MANAGE)")
    @Operation(summary = "删除标签", description = "管理员删除标签，删除前需确保无绑定")
    public ApiResponse<Void> deleteTag(@Parameter(description = "标签ID", example = "101") @PathVariable Long id) {
        tagService.deleteTag(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/admin/tags/merge")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_TAG_MANAGE)")
    @Operation(summary = "合并标签", description = "将来源标签合并至目标标签")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MergeTagRequest.class),
                    examples = @ExampleObject(
                            name = "请求示例",
                            value = """
                                    {
                                      \"sourceTagId\": 10,
                                      \"targetTagId\": 3
                                    }
                                    """
                    )
            )
    )
    public ApiResponse<Void> mergeTag(@Valid @RequestBody MergeTagRequest request) {
        tagService.mergeTag(request);
        return ApiResponse.success(null);
    }
}
