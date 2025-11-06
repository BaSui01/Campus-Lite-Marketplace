package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CampusMigrationRequest;
import com.campus.marketplace.common.dto.request.CreateCampusRequest;
import com.campus.marketplace.common.dto.request.UpdateCampusRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.CampusMigrationValidationResponse;
import com.campus.marketplace.common.entity.Campus;
import com.campus.marketplace.service.CampusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Campus Controller
 *
 * @author BaSui
 * @date 2025-10-29
 */

@RestController
@RequestMapping("/admin/campuses")
@RequiredArgsConstructor
@Tag(name = "校区管理", description = "校区CRUD与用户迁移")
public class CampusController {

    private final CampusService campusService;

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CAMPUS_MANAGE)")
    @Operation(summary = "校区列表", description = "查询全部校区")
    public ApiResponse<List<Campus>> list() {
        return ApiResponse.success(campusService.listAll());
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CAMPUS_MANAGE)")
    @Operation(summary = "创建校区")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateCampusRequest.class),
                    examples = @ExampleObject(
                            name = "请求示例",
                            value = """
                                    {
                                      \"code\": \"HZX\",
                                      \"name\": \"杭州校区\"
                                    }
                                    """
                    )
            )
    )
    public ApiResponse<Campus> create(@Valid @RequestBody CreateCampusRequest req) {
        Campus campus = campusService.create(req.getCode(), req.getName());
        return ApiResponse.success(campus);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CAMPUS_MANAGE)")
    @Operation(summary = "更新校区")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UpdateCampusRequest.class),
                    examples = @ExampleObject(
                            name = "请求示例",
                            value = """
                                    {
                                      \"name\": \"杭州校区(南)\",
                                      \"status\": \"ACTIVE\"
                                    }
                                    """
                    )
            )
    )
    public ApiResponse<Campus> update(@Parameter(description = "校区ID", example = "1") @PathVariable Long id, @Valid @RequestBody UpdateCampusRequest req) {
        Campus campus = campusService.update(id, req.getName(), req.getStatus());
        return ApiResponse.success(campus);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CAMPUS_MANAGE)")
    @Operation(summary = "删除校区")
    public ApiResponse<Void> delete(@Parameter(description = "校区ID", example = "1") @PathVariable Long id) {
        campusService.delete(id);
        return ApiResponse.success(null);
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
