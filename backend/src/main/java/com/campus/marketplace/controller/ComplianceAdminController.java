package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.entity.ComplianceWhitelist;
import com.campus.marketplace.common.entity.ComplianceAuditLog;
import com.campus.marketplace.repository.ComplianceWhitelistRepository;
import com.campus.marketplace.repository.ComplianceAuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Compliance Admin Controller
 *
 * @author BaSui
 * @date 2025-10-29
 */

@RestController
@RequestMapping("/admin/compliance")
@RequiredArgsConstructor
@Tag(name = "合规管理", description = "灰度白名单与审计日志管理")
public class ComplianceAdminController {

    private final ComplianceWhitelistRepository whitelistRepository;
    private final ComplianceAuditLogRepository auditRepository;

    @PostMapping("/whitelist")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_COMPLIANCE_REVIEW)")
    @Operation(summary = "添加白名单")
    public ApiResponse<ComplianceWhitelist> addWhitelist(@RequestParam String type,
                                                         @RequestParam Long targetId) {
        ComplianceWhitelist wl = ComplianceWhitelist.builder().type(type).targetId(targetId).build();
        return ApiResponse.success(whitelistRepository.save(wl));
    }

    @DeleteMapping("/whitelist/{id}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_COMPLIANCE_REVIEW)")
    @Operation(summary = "移除白名单")
    public ApiResponse<Void> removeWhitelist(@PathVariable Long id) {
        whitelistRepository.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/audit")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_COMPLIANCE_REVIEW)")
    @Operation(summary = "查询审计日志")
    public ApiResponse<Page<ComplianceAuditLog>> listAudit(@RequestParam String targetType,
                                                           @RequestParam Long targetId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(auditRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
                targetType, targetId, PageRequest.of(page, size)));
    }
}
