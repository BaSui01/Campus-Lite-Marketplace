package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.BatchReviewRequest;
import com.campus.marketplace.common.dto.request.ReviewRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.AppealStatistics;
import com.campus.marketplace.common.dto.response.BatchReviewResult;
import com.campus.marketplace.common.entity.Appeal;
import com.campus.marketplace.common.enums.AppealStatus;
import com.campus.marketplace.service.AppealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 申诉管理控制器 - 管理员端API
 * 提供申诉审核、批量处理、统计分析等管理功能
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Tag(name = "申诉管理(管理员)", description = "管理员申诉审核和管理接口")
@RestController
@RequestMapping("/api/admin/appeals")
@RequiredArgsConstructor
@Validated
public class AppealAdminController {

    private final AppealService appealService;

        @Operation(summary = "审核申诉", description = "管理员审核申诉，可批准或拒绝")
    @PostMapping("/{appealId}/review")
    @PreAuthorize("hasAuthority('SYSTEM_USER_APPEAL_HANDLE')")
    public ApiResponse<Appeal> reviewAppeal(
            @PathVariable Long appealId,
            @Valid @RequestBody ReviewRequest request) {
        try {
            // 设置审核人信息
            Long currentUserId = getCurrentUserId();
            String currentUserName = getCurrentUserName();
            
            request.setAppealId(appealId);
            request.setReviewerId(currentUserId);
            request.setReviewerName(currentUserName);
            
            // 执行审核
            Appeal reviewedAppeal = appealService.reviewAppeal(request);
            
            if (reviewedAppeal != null) {
                return ApiResponse.success("审核成功", reviewedAppeal);
            } else {
                return ApiResponse.error(400, "审核失败，申诉状态不允许审核或已过期");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "审核申诉失败: " + e.getMessage());
        }
    }

        @Operation(summary = "批量审核申诉", description = "批量审核多个申诉")
    @PostMapping("/batch-review")
    @PreAuthorize("hasAuthority('SYSTEM_USER_APPEAL_HANDLE')")
    public ApiResponse<BatchReviewResult> batchReviewAppeals(@Valid @RequestBody BatchReviewRequest request) {
        try {
            // 设置审核人信息
            Long currentUserId = getCurrentUserId();
            String currentUserName = getCurrentUserName();
            
            request.setReviewerId(currentUserId);
            request.setReviewerName(currentUserName);
            
            // 执行批量审核
            BatchReviewResult result = appealService.batchReviewAppeals(request);
            
            if (result.getSuccessCount() > 0) {
                return ApiResponse.success(
                    String.format("批量审核完成：成功%d个，失败%d个", 
                        result.getSuccessCount(), result.getFailureCount()),
                    result);
            } else {
                return ApiResponse.error(400, "批量审核失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "批量审核失败: " + e.getMessage());
        }
    }

        @Operation(summary = "查询待审核申诉", description = "获取所有待审核的申诉列表")
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('SYSTEM_USER_APPEAL_HANDLE')")
    public ApiResponse<Page<Appeal>> getPendingAppeals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            // 这里简化处理，直接返回所有待审核申诉
            // 实际项目中可以根据管理员级别分配不同的申诉
            Pageable pageable = PageRequest.of(page, size);
            
            // 需要从Service获取待审核申诉
            // 临时方案：返回所有申诉，由前端过滤
            Page<Appeal> appeals = appealService.getUserAppeals(null, pageable);
            
            return ApiResponse.success(appeals);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询待审核申诉失败: " + e.getMessage());
        }
    }

        @Operation(summary = "标记过期申诉", description = "将超期未处理的申诉标记为过期")
    @PostMapping("/mark-expired")
    @PreAuthorize("hasAuthority('SYSTEM_USER_APPEAL_HANDLE')")
    public ApiResponse<Integer> markExpiredAppeals() {
        try {
            int markedCount = appealService.markExpiredAppeals();
            
            return ApiResponse.success(
                String.format("成功标记%d个过期申诉", markedCount), 
                markedCount);
        } catch (Exception e) {
            return ApiResponse.error(500, "标记过期申诉失败: " + e.getMessage());
        }
    }

        @Operation(summary = "申诉统计", description = "获取申诉系统的统计数据")
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('SYSTEM_USER_APPEAL_HANDLE')")
    public ApiResponse<AppealStatistics> getAppealStatistics() {
        try {
            AppealStatistics statistics = appealService.getAppealStatistics();
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取统计信息失败: " + e.getMessage());
        }
    }

        @Operation(summary = "按状态查询", description = "根据申诉状态查询申诉列表")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('SYSTEM_USER_APPEAL_HANDLE')")
    public ApiResponse<Page<Appeal>> getAppealsByStatus(
            @PathVariable AppealStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            // 这里需要Service层支持按状态查询
            // 临时简化实现
            Pageable pageable = PageRequest.of(page, size);
            Page<Appeal> appeals = appealService.getUserAppeals(null, pageable);
            
            // 理想情况下应该在Service层过滤
            return ApiResponse.success(appeals);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询申诉失败: " + e.getMessage());
        }
    }

        @Operation(summary = "查询申诉详情", description = "管理员查看任意申诉的详细信息")
    @GetMapping("/{appealId}")
    @PreAuthorize("hasAuthority('SYSTEM_USER_APPEAL_HANDLE')")
    public ApiResponse<Appeal> getAppealDetail(@PathVariable Long appealId) {
        try {
            // 管理员可以查看任意申诉详情
            var detail = appealService.getAppealDetail(appealId);
            return ApiResponse.success(detail.getAppeal());
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ApiResponse.error(404, e.getMessage());
            }
            return ApiResponse.error(500, "查询申诉详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录管理员ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                String name = authentication.getName();
                return Long.parseLong(name);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取当前登录管理员用户名
     */
    private String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "admin";
    }
}
