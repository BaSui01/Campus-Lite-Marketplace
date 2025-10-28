package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CompletePrivacyRequest;
import com.campus.marketplace.common.dto.request.CreatePrivacyRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.PrivacyRequestResponse;
import com.campus.marketplace.service.PrivacyService;
import io.swagger.v3.oas.annotations.Operation;
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
 * @date 2025-10-27
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "隐私合规", description = "数据导出与注销申请接口")
public class PrivacyController {

    private final PrivacyService privacyService;

    @PostMapping("/api/privacy")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "创建隐私请求", description = "提交数据导出或删除请求，系统会异步处理")
    public ApiResponse<Long> createPrivacyRequest(@Valid @RequestBody CreatePrivacyRequest request) {
        return ApiResponse.success(privacyService.createRequest(request));
    }

    @GetMapping("/api/privacy")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "查看我的隐私请求", description = "查看历史隐私请求及处理进度")
    public ApiResponse<List<PrivacyRequestResponse>> listMyRequests() {
        return ApiResponse.success(privacyService.listMyRequests());
    }

    @GetMapping("/api/admin/privacy/requests")
    @PreAuthorize("hasAuthority('system:compliance:review')")
    @Operation(summary = "查看待处理隐私请求", description = "管理员查看等待处理的隐私请求")
    public ApiResponse<List<PrivacyRequestResponse>> listPendingRequests() {
        return ApiResponse.success(privacyService.listPendingRequests());
    }

    @PostMapping("/api/admin/privacy/requests/{id}/complete")
    @PreAuthorize("hasAuthority('system:compliance:review')")
    @Operation(summary = "标记隐私请求完成", description = "管理员处理完成后上传结果路径")
    public ApiResponse<Void> completeRequest(@PathVariable Long id,
                                             @Valid @RequestBody CompletePrivacyRequest request) {
        privacyService.markCompleted(id, request.resultPath());
        return ApiResponse.success(null);
    }
}
