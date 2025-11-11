package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.CreditHistoryResponse;
import com.campus.marketplace.common.dto.response.UserCreditInfoResponse;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.service.CreditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 信用管理控制器
 * 
 * 提供用户信用信息查询、信用历史记录查询等 REST API
 * 
 * @author BaSui 😎
 * @date 2025-11-11
 */
@Slf4j
@RestController
@RequestMapping("/credit")
@RequiredArgsConstructor
@Tag(name = "信用管理", description = "用户信用等级、信用历史记录查询")
public class CreditController {

    private final CreditService creditService;

    /**
     * 获取当前用户的信用信息
     *
     * GET /api/credit/my
     *
     * @return 当前用户信用信息
     */
    @GetMapping("/my")
    @Operation(summary = "获取当前用户信用信息", description = "查询当前登录用户的信用等级、订单数量、好评率等信息")
    public ApiResponse<UserCreditInfoResponse> getMyCredit() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("查询当前用户信用信息: userId={}", userId);
        UserCreditInfoResponse creditInfo = creditService.getMyCredit(userId);
        return ApiResponse.success(creditInfo);
    }

    /**
     * 获取指定用户的信用信息
     *
     * GET /api/credit/user/{userId}
     *
     * @param userId 用户ID
     * @return 用户信用信息
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取指定用户信用信息", description = "查询指定用户的信用等级和信用评分明细")
    public ApiResponse<UserCreditInfoResponse> getUserCredit(
            @Parameter(description = "用户ID", example = "123") @PathVariable Long userId
    ) {
        log.info("查询指定用户信用信息: userId={}", userId);
        UserCreditInfoResponse creditInfo = creditService.getUserCredit(userId);
        return ApiResponse.success(creditInfo);
    }

    /**
     * 获取当前用户的信用历史记录
     *
     * GET /api/credit/history
     *
     * @param page 页码（从0开始）
     * @param size 每页数量
     * @return 信用历史记录分页
     */
    @GetMapping("/history")
    @Operation(summary = "获取信用历史记录", description = "查询当前用户的信用变动历史（分页）")
    public ApiResponse<Page<CreditHistoryResponse>> getCreditHistory(
            @Parameter(description = "页码（从0开始）", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("查询信用历史: userId={}, page={}, size={}", userId, page, size);
        Page<CreditHistoryResponse> history = creditService.getCreditHistory(userId, page, size);
        return ApiResponse.success(history);
    }
}
