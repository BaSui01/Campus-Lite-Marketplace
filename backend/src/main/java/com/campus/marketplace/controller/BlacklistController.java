package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.UserProfileResponse;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.service.BlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 黑名单控制器
 *
 * 提供拉黑、解除拉黑、查询黑名单等接口
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/blacklist")
@RequiredArgsConstructor
@Tag(name = "黑名单管理", description = "拉黑、解除拉黑、查询黑名单等接口")
public class BlacklistController {

    private final BlacklistService blacklistService;

        @Operation(summary = "添加到黑名单", description = "拉黑指定用户，对方将无法给你发送消息")
    @PostMapping("/block/{blockedUserId}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Void> addToBlacklist(
            @Parameter(description = "被拉黑的用户ID", example = "10086") @PathVariable Long blockedUserId,
            @Parameter(description = "拉黑原因", example = "频繁发送骚扰信息") @RequestParam(required = false) String reason) {

        log.info("添加黑名单：username={}, blockedUserId={}, reason={}",
                SecurityUtil.getCurrentUsername(), blockedUserId, reason);

        blacklistService.addToBlacklist(blockedUserId, reason);

        log.info("添加黑名单成功：blockedUserId={}", blockedUserId);
        return ApiResponse.success();
    }

        @Operation(summary = "从黑名单移除", description = "解除对指定用户的拉黑，对方可以正常发送消息")
    @DeleteMapping("/unblock/{blockedUserId}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Void> removeFromBlacklist(
            @Parameter(description = "被拉黑的用户ID", example = "10086") @PathVariable Long blockedUserId) {

        log.info("移除黑名单：username={}, blockedUserId={}",
                SecurityUtil.getCurrentUsername(), blockedUserId);

        blacklistService.removeFromBlacklist(blockedUserId);

        log.info("移除黑名单成功：blockedUserId={}", blockedUserId);
        return ApiResponse.success();
    }

        @Operation(summary = "查询黑名单列表", description = "获取当前用户的黑名单列表")
    @GetMapping("/list")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Page<UserProfileResponse>> listBlacklist(
            @Parameter(description = "页码（从0开始）", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") int size) {

        log.info("查询黑名单列表：username={}, page={}, size={}",
                SecurityUtil.getCurrentUsername(), page, size);

        Page<UserProfileResponse> blacklist = blacklistService.listBlacklist(page, size);

        log.info("黑名单列表查询成功：total={}", blacklist.getTotalElements());
        return ApiResponse.success(blacklist);
    }

        @Operation(summary = "检查是否拉黑", description = "查询当前用户是否拉黑了指定用户")
    @GetMapping("/check/{blockedUserId}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Boolean> isBlocked(
            @Parameter(description = "被检查的用户ID", example = "10010") @PathVariable Long blockedUserId) {

        log.debug("检查是否拉黑：username={}, blockedUserId={}",
                SecurityUtil.getCurrentUsername(), blockedUserId);

        boolean isBlocked = blacklistService.isBlocked(blockedUserId);

        return ApiResponse.success(isBlocked);
    }
}
