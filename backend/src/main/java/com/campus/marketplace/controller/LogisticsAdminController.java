package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.LogisticsDTO;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.service.LogisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 🚚 BaSui 的物流管理控制器 - 管理端物流列表查询！😎
 *
 * 功能范围：
 * - 📋 物流列表：分页查询、关键词搜索、状态筛选
 *
 * ⚠️ 注意：
 * - 所有接口仅管理员可访问
 * - 支持搜索订单ID和快递单号
 *
 * @author BaSui 😎
 * @date 2025-11-08
 */
@Slf4j
@RestController
@RequestMapping("/admin/logistics")
@RequiredArgsConstructor
@Tag(name = "物流管理（管理端）", description = "管理员后台物流管理相关接口")
public class LogisticsAdminController {

    private final LogisticsService logisticsService;

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_STATISTICS_VIEW)")
    @Operation(summary = "分页查询物流列表", description = "管理员查看所有物流信息，支持分页、筛选、排序")
    public ApiResponse<Page<LogisticsDTO>> listLogistics(com.campus.marketplace.common.dto.request.LogisticsFilterRequest filterRequest) {
        log.info("🎯 BaSui：管理端查询物流列表 - keyword={}, status={}, page={}, size={}",
                filterRequest.getKeyword(), filterRequest.getStatus(), filterRequest.getPage(), filterRequest.getSize());

        // 调用服务层查询
        Page<LogisticsDTO> logisticsPage = logisticsService.listLogistics(filterRequest);

        log.info("✅ BaSui：查询成功 - 共 {} 条记录，当前第 {} 页",
                logisticsPage.getTotalElements(), filterRequest.getPage() + 1);

        return ApiResponse.success(logisticsPage);
    }
}
