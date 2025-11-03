package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CreateBatchTaskRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.BatchTaskProgressResponse;
import com.campus.marketplace.common.dto.response.BatchTaskResponse;
import com.campus.marketplace.common.enums.BatchTaskStatus;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.service.BatchOperationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 批量操作控制器
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Slf4j
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchOperationController {

    private final BatchOperationService batchOperationService;

    /**
     * 创建批量任务
     */
    @PostMapping("/tasks")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Long> createBatchTask(@Valid @RequestBody CreateBatchTaskRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        Long taskId = batchOperationService.createBatchTask(request, userId);
        
        // 立即触发执行
        batchOperationService.executeBatchTask(taskId);
        
        return ApiResponse.success(taskId);
    }

    /**
     * 查询批量任务列表
     */
    @GetMapping("/tasks")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Page<BatchTaskResponse>> getBatchTasks(
            @RequestParam(required = false) BatchTaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long userId = SecurityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BatchTaskResponse> tasks = batchOperationService.getUserBatchTasks(userId, status, pageable);
        
        return ApiResponse.success(tasks);
    }

    /**
     * 查询批量任务详情
     */
    @GetMapping("/tasks/{taskId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<BatchTaskResponse> getBatchTaskDetail(@PathVariable Long taskId) {
        Long userId = SecurityUtil.getCurrentUserId();
        BatchTaskResponse response = batchOperationService.getBatchTaskDetail(taskId, userId);
        return ApiResponse.success(response);
    }

    /**
     * 查询批量任务进度
     */
    @GetMapping("/tasks/{taskId}/progress")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<BatchTaskProgressResponse> getTaskProgress(@PathVariable Long taskId) {
        BatchTaskProgressResponse progress = batchOperationService.getTaskProgress(taskId);
        return ApiResponse.success(progress);
    }

    /**
     * 取消批量任务
     */
    @PostMapping("/tasks/{taskId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> cancelBatchTask(@PathVariable Long taskId) {
        Long userId = SecurityUtil.getCurrentUserId();
        batchOperationService.cancelBatchTask(taskId, userId);
        return ApiResponse.success();
    }
}
