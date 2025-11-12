package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.entity.Resource;
import com.campus.marketplace.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 学习资源控制器
 * 
 * @author BaSui 😎
 * @date 2025-11-11
 */
@Slf4j
@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
@Tag(name = "学习资源", description = "学习资源查询、下载相关接口")
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    @Operation(summary = "获取资源列表", description = "分页查询学习资源列表，支持按类型、分类、关键词筛选")
    public ApiResponse<Page<Resource>> listResources(
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "资源类型(DOCUMENT/VIDEO/AUDIO/LINK/CODE/OTHER)") @RequestParam(required = false) String type,
            @Parameter(description = "资源分类") @RequestParam(required = false) String category,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword
    ) {
        Page<Resource> resources = resourceService.listResources(page, size, type, category, keyword);
        return ApiResponse.success(resources);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取资源详情", description = "根据资源ID查询资源详细信息")
    public ApiResponse<Resource> getResourceDetail(
            @Parameter(description = "资源ID", required = true) @PathVariable Long id
    ) {
        Resource resource = resourceService.getResourceDetail(id);
        return ApiResponse.success(resource);
    }

    @PostMapping("/{id}/download")
    @Operation(summary = "记录下载", description = "记录资源下载次数")
    public ApiResponse<Void> recordDownload(
            @Parameter(description = "资源ID", required = true) @PathVariable Long id
    ) {
        resourceService.recordDownload(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/hot")
    @Operation(summary = "热门资源", description = "获取热门下载资源列表")
    public ApiResponse<Page<Resource>> getHotResources(
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        Page<Resource> resources = resourceService.getHotResources(page, size);
        return ApiResponse.success(resources);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "我的资源", description = "查询当前用户上传的资源")
    public ApiResponse<Page<Resource>> getMyResources(
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        Page<Resource> resources = resourceService.getMyResources(page, size);
        return ApiResponse.success(resources);
    }
}
