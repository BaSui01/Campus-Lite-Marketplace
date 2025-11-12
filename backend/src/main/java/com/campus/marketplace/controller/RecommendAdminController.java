package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.RecommendConfigDTO;
import com.campus.marketplace.common.dto.RecommendStatisticsDTO;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.service.RecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 🎯 BaSui 的推荐管理控制器 - 管理推荐配置和统计！😎
 *
 * 功能范围：
 * - ⚙️ 推荐配置：获取/更新推荐算法配置
 * - 📊 推荐统计：获取推荐效果统计数据
 *
 * ⚠️ 注意：
 * - 所有接口仅管理员可访问
 * - 配置更新实时生效（运行时修改）
 *
 * @author BaSui 😎
 * @date 2025-11-08
 */
@Slf4j
@RestController
@RequestMapping("/admin/recommend")
@RequiredArgsConstructor
@Tag(name = "推荐管理（管理端）", description = "管理员后台推荐配置和统计接口")
public class RecommendAdminController {

    private final RecommendService recommendService;

    @GetMapping("/config")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CONFIG_VIEW)")
    @Operation(summary = "获取推荐配置", description = "管理员查看当前推荐算法配置")
    public ApiResponse<RecommendConfigDTO> getRecommendConfig() {
        log.info("🎯 BaSui：管理端查询推荐配置");

        RecommendConfigDTO config = recommendService.getRecommendConfig();

        log.info("✅ BaSui：查询成功 - enabled={}, algorithm={}",
                config.getEnabled(), config.getAlgorithm());

        return ApiResponse.success(config);
    }

    @PutMapping("/config")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_CONFIG_UPDATE)")
    @Operation(summary = "更新推荐配置", description = "管理员更新推荐算法配置（实时生效）")
    public ApiResponse<Void> updateRecommendConfig(@Valid @RequestBody RecommendConfigDTO configDTO) {
        log.info("🎯 BaSui：管理端更新推荐配置 - enabled={}, algorithm={}",
                configDTO.getEnabled(), configDTO.getAlgorithm());

        recommendService.updateRecommendConfig(configDTO);

        log.info("✅ BaSui：更新成功");

        return ApiResponse.success(null);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)")
    @Operation(summary = "获取推荐统计", description = "管理员查看推荐效果统计数据")
    public ApiResponse<RecommendStatisticsDTO> getRecommendStatistics() {
        log.info("🎯 BaSui：管理端查询推荐统计");

        RecommendStatisticsDTO statistics = recommendService.getRecommendStatistics();

        log.info("✅ BaSui：查询成功 - totalRecommendations={}",
                statistics.getTotalRecommendations());

        return ApiResponse.success(statistics);
    }
}
