package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CreateReportRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.ReportResponse;
import com.campus.marketplace.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Report Controller
 *
 * @author BaSui
 * @date 2025-10-29
 */

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "举报管理", description = "内容举报相关接口")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "创建举报", description = "举报违规内容")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "创建举报请求体",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateReportRequest.class),
                    examples = @ExampleObject(
                            name = "请求示例",
                            value = """
                                    {
                                      \"targetType\": \"POST\",
                                      \"targetId\": 98765,
                                      \"reason\": \"涉嫌广告/作弊\"
                                    }
                                    """
                    )
            )
    )
    public ApiResponse<Long> createReport(@Valid @RequestBody CreateReportRequest request) {
        Long reportId = reportService.createReport(request);
        return ApiResponse.success(reportId);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_REPORT_HANDLE)")
    @Operation(summary = "查询待处理举报列表", description = "管理员查询所有待处理的举报")
    public ApiResponse<Page<ReportResponse>> listPendingReports(
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        Page<ReportResponse> result = reportService.listPendingReports(page, size);
        return ApiResponse.success(result);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "查询我的举报记录", description = "用户查询自己的举报记录")
    public ApiResponse<Page<ReportResponse>> listMyReports(
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        Page<ReportResponse> result = reportService.listMyReports(page, size);
        return ApiResponse.success(result);
    }

    @PostMapping("/{id}/handle")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_REPORT_HANDLE)")
    @Operation(summary = "处理举报", description = "管理员处理举报")
    public ApiResponse<Void> handleReport(
            @Parameter(description = "举报 ID", example = "555") @PathVariable Long id,
            @Parameter(description = "是否通过", example = "true") @RequestParam boolean approved,
            @Parameter(description = "处理结果", example = "删除违规帖子并警告用户") @RequestParam String handleResult
    ) {
        reportService.handleReport(id, approved, handleResult);
        return ApiResponse.success(null);
    }
}
