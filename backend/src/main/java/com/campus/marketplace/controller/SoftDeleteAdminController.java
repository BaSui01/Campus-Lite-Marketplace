package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.service.SoftDeleteAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端软删除治理接口
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@RestController
@RequestMapping("/api/admin/soft-delete")
@RequiredArgsConstructor
@Tag(name = "软删除治理", description = "管理端恢复与彻底删除接口")
public class SoftDeleteAdminController {

    private final SoftDeleteAdminService softDeleteAdminService;

    @GetMapping("/targets")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "支持的实体列表", description = "返回可执行软删除治理的实体标识")
    public ApiResponse<List<String>> listTargets() {
        return ApiResponse.success(softDeleteAdminService.listTargets());
    }

    @PostMapping("/{entity}/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "恢复软删除记录", description = "恢复指定实体的软删除记录")
    public ApiResponse<Void> restore(
            @Parameter(description = "实体标识", example = "post") @PathVariable String entity,
            @Parameter(description = "主键ID", example = "1001") @PathVariable Long id
    ) {
        log.info("管理员恢复软删除记录 entity={} id={}", entity, id);
        softDeleteAdminService.restore(entity, id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{entity}/{id}/purge")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "彻底删除记录", description = "绕过软删除直接删除底层数据")
    public ApiResponse<Void> purge(
            @Parameter(description = "实体标识", example = "post") @PathVariable String entity,
            @Parameter(description = "主键ID", example = "1001") @PathVariable Long id
    ) {
        log.warn("管理员彻底删除记录 entity={} id={}", entity, id);
        softDeleteAdminService.purge(entity, id);
        return ApiResponse.success(null);
    }
}
