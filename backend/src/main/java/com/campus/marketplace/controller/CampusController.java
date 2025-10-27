package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CampusMigrationRequest;
import com.campus.marketplace.common.dto.request.CreateCampusRequest;
import com.campus.marketplace.common.dto.request.UpdateCampusRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.CampusMigrationValidationResponse;
import com.campus.marketplace.common.entity.Campus;
import com.campus.marketplace.service.CampusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/campuses")
@RequiredArgsConstructor
public class CampusController {

    private final CampusService campusService;

    @GetMapping
    @PreAuthorize("hasAuthority('system:campus:manage')")
    public ApiResponse<List<Campus>> list() {
        return ApiResponse.success(campusService.listAll());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:campus:manage')")
    public ApiResponse<Campus> create(@Valid @RequestBody CreateCampusRequest req) {
        Campus campus = campusService.create(req.getCode(), req.getName());
        return ApiResponse.success(campus);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:campus:manage')")
    public ApiResponse<Campus> update(@PathVariable Long id, @Valid @RequestBody UpdateCampusRequest req) {
        Campus campus = campusService.update(id, req.getName(), req.getStatus());
        return ApiResponse.success(campus);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:campus:manage')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        campusService.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/migrate-users/validate")
    @PreAuthorize("hasAuthority('system:campus:manage')")
    public ApiResponse<CampusMigrationValidationResponse> validateMigration(@Valid @RequestBody CampusMigrationRequest req) {
        CampusMigrationValidationResponse res = campusService.validateUserMigration(req.getFromCampusId(), req.getToCampusId());
        return ApiResponse.success(res);
    }

    @PostMapping("/migrate-users")
    @PreAuthorize("hasAuthority('system:campus:manage')")
    public ApiResponse<Integer> migrateUsers(@Valid @RequestBody CampusMigrationRequest req) {
        int moved = campusService.migrateUsers(req.getFromCampusId(), req.getToCampusId());
        return ApiResponse.success(moved);
    }
}
