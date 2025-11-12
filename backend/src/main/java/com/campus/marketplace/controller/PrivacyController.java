package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CompletePrivacyRequest;
import com.campus.marketplace.common.dto.request.CreatePrivacyRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.PrivacyRequestResponse;
import com.campus.marketplace.service.PrivacyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 隐私合规控制器
 *
 * 提供隐私请求创建/查询与管理员处理接口
 *
 * @author BaSui
 * @date 2025-10-29
 */

@RestController
@RequiredArgsConstructor
@Tag(name = "隐私合规", description = "数据导出与注销申请接口")
public class PrivacyController {

    private final PrivacyService privacyService;

    @PostMapping("/privacy")
    @Operation(summary = "创建隐私请求", description = "提交数据导出或删除请求，系统会异步处理")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreatePrivacyRequest.class),
                    examples = @ExampleObject(
                            name = "请求示例",
                            value = """
                                    {
                                      \"type\": \"DATA_EXPORT\",
                                      \"reason\": \"毕业离校，需要导出数据\"
                                    }
                                    """
                    )
            )
    )
    public ApiResponse<Long> createPrivacyRequest(@Valid @RequestBody CreatePrivacyRequest request) {
        return ApiResponse.success(privacyService.createRequest(request));
    }

    @GetMapping("/privacy")
    @Operation(summary = "查看我的隐私请求", description = "查看历史隐私请求及处理进度")
    public ApiResponse<List<PrivacyRequestResponse>> listMyRequests() {
        return ApiResponse.success(privacyService.listMyRequests());
    }

    @GetMapping("/admin/privacy/requests")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_COMPLIANCE_REVIEW)")
    @Operation(summary = "查看待处理隐私请求", description = "管理员查看等待处理的隐私请求")
    public ApiResponse<List<PrivacyRequestResponse>> listPendingRequests() {
        return ApiResponse.success(privacyService.listPendingRequests());
    }

    @PostMapping("/admin/privacy/requests/{id}/complete")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_COMPLIANCE_REVIEW)")
    @Operation(summary = "标记隐私请求完成", description = "管理员处理完成后上传结果路径")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CompletePrivacyRequest.class),
                    examples = @ExampleObject(
                            name = "请求示例",
                            value = """
                                    {
                                      \"resultPath\": \"s3://exports/user-1001-privacy-20251027.zip\"
                                    }
                                    """
                    )
            )
    )
    public ApiResponse<Void> completeRequest(@Parameter(description = "请求ID", example = "7001") @PathVariable Long id,
                                             @Valid @RequestBody CompletePrivacyRequest request) {
        privacyService.markCompleted(id, request.resultPath());
        return ApiResponse.success(null);
    }
}
