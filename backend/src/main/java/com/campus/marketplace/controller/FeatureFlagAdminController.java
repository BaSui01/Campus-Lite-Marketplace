package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.FeatureFlagCreateRequest;
import com.campus.marketplace.common.dto.request.FeatureFlagUpdateRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.entity.FeatureFlag;
import com.campus.marketplace.service.FeatureFlagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🚩 BaSui 的功能开关管理控制器 - 管理功能开关！😎
 *
 * 功能范围：
 * - 🚩 开关管理：查询、创建、编辑、删除、启用/禁用
 * - 🔄 缓存刷新：刷新单个或全部功能开关缓存
 * - 🎯 灰度发布：支持按环境、用户、校区等维度控制功能发布
 *
 * ⚠️ 注意：
 * - 所有接口仅管理员可访问
 * - 修改开关后会自动刷新缓存
 * - 规则JSON格式需符合 FeatureFlagRules 规范
 *
 * @author BaSui
 * @date 2025-11-07
 */
@RestController
@RequestMapping("/admin/feature-flags")
@RequiredArgsConstructor
@Tag(name = "功能开关管理", description = "管理员后台功能开关管理相关接口")
public class FeatureFlagAdminController {

    private final FeatureFlagService featureFlagService;

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_FEATURE_FLAG_VIEW)")
    @Operation(summary = "查询功能开关列表", description = "获取所有功能开关列表")
    public ApiResponse<List<FeatureFlag>> listFeatureFlags() {
        List<FeatureFlag> flags = featureFlagService.listAll();
        return ApiResponse.success(flags);
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_FEATURE_FLAG_CREATE)")
    @Operation(summary = "创建功能开关", description = "创建新的功能开关")
    public ApiResponse<Long> createFeatureFlag(@Valid @RequestBody FeatureFlagCreateRequest request) {
        Long id = featureFlagService.create(
                request.getKey(),
                request.getDescription(),
                request.getEnabled() != null ? request.getEnabled() : false,
                request.getRulesJson()
        );
        return ApiResponse.success(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_FEATURE_FLAG_VIEW)")
    @Operation(summary = "获取功能开关详情", description = "根据ID获取功能开关详细信息")
    public ApiResponse<FeatureFlag> getFeatureFlagById(
            @Parameter(description = "功能开关ID", example = "1") @PathVariable Long id
    ) {
        FeatureFlag flag = featureFlagService.getById(id);
        return ApiResponse.success(flag);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_FEATURE_FLAG_EDIT)")
    @Operation(summary = "编辑功能开关", description = "更新功能开关配置（自动刷新缓存）")
    public ApiResponse<Void> updateFeatureFlag(
            @Parameter(description = "功能开关ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody FeatureFlagUpdateRequest request
    ) {
        featureFlagService.update(
                id,
                request.getDescription(),
                request.getEnabled() != null ? request.getEnabled() : false,
                request.getRulesJson()
        );
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_FEATURE_FLAG_DELETE)")
    @Operation(summary = "删除功能开关", description = "删除指定功能开关（自动刷新缓存）")
    public ApiResponse<Void> deleteFeatureFlag(
            @Parameter(description = "功能开关ID", example = "1") @PathVariable Long id
    ) {
        featureFlagService.delete(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_FEATURE_FLAG_EDIT)")
    @Operation(summary = "切换启用状态", description = "切换功能开关的启用/禁用状态（自动刷新缓存）")
    public ApiResponse<Void> toggleEnabled(
            @Parameter(description = "功能开关ID", example = "1") @PathVariable Long id
    ) {
        featureFlagService.toggleEnabled(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/refresh")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_FEATURE_FLAG_EDIT)")
    @Operation(summary = "刷新全部缓存", description = "刷新所有功能开关的本地缓存")
    public ApiResponse<Void> refreshAllCache() {
        featureFlagService.refreshAll();
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/refresh")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_FEATURE_FLAG_EDIT)")
    @Operation(summary = "刷新单个缓存", description = "刷新指定功能开关的本地缓存")
    public ApiResponse<Void> refreshCache(
            @Parameter(description = "功能开关ID", example = "1") @PathVariable Long id
    ) {
        FeatureFlag flag = featureFlagService.getById(id);
        featureFlagService.refresh(flag.getKey());
        return ApiResponse.success(null);
    }
}
