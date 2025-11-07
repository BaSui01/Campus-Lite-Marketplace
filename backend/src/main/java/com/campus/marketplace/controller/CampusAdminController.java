package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CampusCreateRequest;
import com.campus.marketplace.common.dto.request.CampusMigrationRequest;
import com.campus.marketplace.common.dto.request.CampusUpdateRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.CampusMigrationValidationResponse;
import com.campus.marketplace.common.dto.response.CampusStatisticsResponse;
import com.campus.marketplace.common.entity.Campus;
import com.campus.marketplace.service.CampusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🏫 BaSui 的校园管理控制器 - 管理校园信息！😎
 *
 * 功能范围：
 * - 🏫 校园管理：查询、创建、编辑、删除、批量删除
 * - 📊 统计分析：校园用户/商品/订单统计
 * - 🔄 用户迁移：校园间用户迁移功能
 *
 * ⚠️ 注意：
 * - 所有接口仅管理员可访问
 * - 删除校园前需检查关联数据
 *
 * @author BaSui
 * @date 2025-11-07
 */
@RestController
@RequestMapping("/admin/campuses")
@RequiredArgsConstructor
@Tag(name = "校园管理", description = "管理员后台校园管理相关接口")
public class CampusAdminController {

    private final CampusService campusService;

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CAMPUS_VIEW)")
    @Operation(summary = "查询校园列表", description = "获取所有校园列表")
    public ApiResponse<List<Campus>> listCampuses() {
        List<Campus> campuses = campusService.listAll();
        return ApiResponse.success(campuses);
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CAMPUS_CREATE)")
    @Operation(summary = "创建校园", description = "创建新的校园")
    public ApiResponse<Campus> createCampus(@Valid @RequestBody CampusCreateRequest request) {
        Campus campus = campusService.create(request.getCode(), request.getName());
        return ApiResponse.success(campus);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CAMPUS_VIEW)")
    @Operation(summary = "获取校园详情", description = "根据ID获取校园详细信息")
    public ApiResponse<Campus> getCampusById(
            @Parameter(description = "校园ID", example = "1") @PathVariable Long id
    ) {
        Campus campus = campusService.getById(id);
        return ApiResponse.success(campus);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CAMPUS_EDIT)")
    @Operation(summary = "编辑校园", description = "更新校园信息")
    public ApiResponse<Campus> updateCampus(
            @Parameter(description = "校园ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody CampusUpdateRequest request
    ) {
        Campus campus = campusService.update(id, request.getName(), request.getStatus());
        return ApiResponse.success(campus);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CAMPUS_DELETE)")
    @Operation(summary = "删除校园", description = "删除指定校园（软删除）")
    public ApiResponse<Void> deleteCampus(
            @Parameter(description = "校园ID", example = "1") @PathVariable Long id
    ) {
        campusService.delete(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CAMPUS_VIEW)")
    @Operation(summary = "校园统计", description = "获取校园用户/商品/订单统计数据")
    public ApiResponse<CampusStatisticsResponse> getCampusStatistics(
            @Parameter(description = "校园ID", example = "1") @PathVariable Long id
    ) {
        CampusStatisticsResponse statistics = campusService.getStatistics(id);
        return ApiResponse.success(statistics);
    }

    @PostMapping("/batch/delete")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CAMPUS_DELETE)")
    @Operation(summary = "批量删除校园", description = "批量删除多个校园（软删除）")
    public ApiResponse<Integer> batchDeleteCampuses(
            @Parameter(description = "校园ID列表") @RequestBody List<Long> ids
    ) {
        int count = campusService.batchDelete(ids);
        return ApiResponse.success(count);
    }

    @PostMapping("/migrate-users/validate")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CAMPUS_MANAGE)")
    @Operation(summary = "校区迁移验证", description = "迁移前的影响评估与校验")
    public ApiResponse<CampusMigrationValidationResponse> validateMigration(@Valid @RequestBody CampusMigrationRequest req) {
        CampusMigrationValidationResponse res = campusService.validateUserMigration(req.getFromCampusId(), req.getToCampusId());
        return ApiResponse.success(res);
    }

    @PostMapping("/migrate-users")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CAMPUS_MANAGE)")
    @Operation(summary = "执行校区迁移", description = "将用户从源校区迁移至目标校区")
    public ApiResponse<Integer> migrateUsers(@Valid @RequestBody CampusMigrationRequest req) {
        int moved = campusService.migrateUsers(req.getFromCampusId(), req.getToCampusId());
        return ApiResponse.success(moved);
    }
}
