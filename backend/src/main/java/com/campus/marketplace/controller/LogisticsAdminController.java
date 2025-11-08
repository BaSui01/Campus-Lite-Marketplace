package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.LogisticsDTO;
import com.campus.marketplace.common.enums.LogisticsStatus;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.service.LogisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    @Operation(summary = "分页查询物流列表", description = "管理员查看所有物流信息，支持关键词搜索和状态筛选")
    public ApiResponse<Page<LogisticsDTO>> listLogistics(
            @Parameter(description = "关键词（订单ID/快递单号）", example = "SF1234567890")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "物流状态", example = "IN_TRANSIT")
            @RequestParam(required = false) LogisticsStatus status,

            @Parameter(description = "页码（从0开始）", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "排序字段", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "排序方向", example = "DESC")
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.info("🎯 BaSui：管理端查询物流列表 - keyword={}, status={}, page={}, size={}",
                keyword, status, page, size);

        // 构建分页参数
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 调用服务层查询
        Page<LogisticsDTO> logisticsPage = logisticsService.listLogistics(keyword, status, pageable);

        log.info("✅ BaSui：查询成功 - 共 {} 条记录，当前第 {} 页",
                logisticsPage.getTotalElements(), page + 1);

        return ApiResponse.success(logisticsPage);
    }
}
