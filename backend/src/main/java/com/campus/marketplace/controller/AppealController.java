package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CreateAppealRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.AppealDetailResponse;
import com.campus.marketplace.common.entity.Appeal;
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
 * 申诉控制器 - 用户端API
 * 提供申诉提交、查询、取消等功能
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Tag(name = "申诉管理", description = "用户申诉相关接口")
@RestController
@RequestMapping("/api/appeals")
@RequiredArgsConstructor
@Validated
public class AppealController {

    private final AppealService appealService;

    /**
     * 提交申诉
     * 用户对处罚、下架等操作提出申诉
     */
    @Operation(summary = "提交申诉", description = "用户提交申诉申请")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Long> submitAppeal(@Valid @RequestBody CreateAppealRequest request) {
        try {
            // 获取当前用户ID
            Long currentUserId = getCurrentUserId();
            request.setUserId(currentUserId);
            
            // 提交申诉
            Long appealId = appealService.submitAppeal(request);
            
            if (appealId != null) {
                return ApiResponse.success("申诉提交成功", appealId);
            } else {
                return ApiResponse.error(400, "申诉提交失败，可能已存在相同申诉");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "提交申诉时发生错误: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户的申诉列表
     */
    @Operation(summary = "查询我的申诉", description = "获取当前用户的所有申诉记录")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Page<Appeal>> getMyAppeals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long currentUserId = getCurrentUserId();
            Pageable pageable = PageRequest.of(page, size);
            
            Page<Appeal> appeals = appealService.getUserAppeals(currentUserId, pageable);
            return ApiResponse.success(appeals);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询申诉列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取申诉详情（包含材料）
     */
    @Operation(summary = "查询申诉详情", description = "获取申诉的详细信息和材料")
    @GetMapping("/{appealId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AppealDetailResponse> getAppealDetail(@PathVariable Long appealId) {
        try {
            AppealDetailResponse detail = appealService.getAppealDetail(appealId);
            
            // 权限检查：只能查看自己的申诉
            Long currentUserId = getCurrentUserId();
            if (!detail.getAppeal().getUserId().equals(currentUserId)) {
                return ApiResponse.error(403, "无权查看此申诉");
            }
            
            return ApiResponse.success(detail);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ApiResponse.error(404, e.getMessage());
            }
            return ApiResponse.error(500, "查询申诉详情失败: " + e.getMessage());
        }
    }

    /**
     * 取消申诉
     * 用户可以取消状态为"待处理"且未过期的申诉
     */
    @Operation(summary = "取消申诉", description = "取消尚未处理的申诉")
    @PostMapping("/{appealId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> cancelAppeal(@PathVariable Long appealId) {
        try {
            Long currentUserId = getCurrentUserId();
            
            // 验证权限（在Service层会再次验证）
            AppealDetailResponse detail = appealService.getAppealDetail(appealId);
            if (!detail.getAppeal().getUserId().equals(currentUserId)) {
                return ApiResponse.error(403, "无权取消此申诉");
            }
            
            boolean success = appealService.cancelAppeal(appealId);
            
            if (success) {
                return ApiResponse.success("申诉已取消", null);
            } else {
                return ApiResponse.error(400, "取消失败，申诉可能已被处理或已过期");
            }
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ApiResponse.error(404, e.getMessage());
            }
            return ApiResponse.error(500, "取消申诉失败: " + e.getMessage());
        }
    }

    /**
     * 验证申诉资格
     * 检查是否可以对特定目标提交申诉
     */
    @Operation(summary = "验证申诉资格", description = "检查是否可以提交申诉")
    @PostMapping("/validate")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Boolean> validateAppealEligibility(@Valid @RequestBody CreateAppealRequest request) {
        try {
            Long currentUserId = getCurrentUserId();
            request.setUserId(currentUserId);
            
            boolean eligible = appealService.validateAppealEligibility(request);
            
            if (eligible) {
                return ApiResponse.success("可以提交申诉", true);
            } else {
                return ApiResponse.success("已存在相同的申诉，不能重复提交", false);
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "验证申诉资格失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                // 尝试解析用户ID
                String name = authentication.getName();
                return Long.parseLong(name);
            } catch (NumberFormatException e) {
                // 如果无法解析，返回null
                return null;
            }
        }
        return null;
    }
}
