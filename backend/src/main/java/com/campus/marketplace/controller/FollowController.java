package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.FollowResponse;
import com.campus.marketplace.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 关注订阅控制器（关注卖家）
 *
 * 提供关注/取关与关注列表接口
 *
 * @author BaSui
 * @date 2025-10-27
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("")
@Tag(name = "关注订阅", description = "关注卖家与取消关注接口")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/follow/{sellerId}")
    @Operation(summary = "关注卖家", description = "关注指定卖家，后续有新上架会收到通知")
    public ApiResponse<Void> follow(@Parameter(description = "卖家ID", example = "20002") @PathVariable Long sellerId) {
        followService.followSeller(sellerId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/follow/{sellerId}")
    @Operation(summary = "取消关注", description = "取消对卖家的关注")
    public ApiResponse<Void> unfollow(@Parameter(description = "卖家ID", example = "20002") @PathVariable Long sellerId) {
        followService.unfollowSeller(sellerId);
        return ApiResponse.success(null);
    }

    @GetMapping("/following")
    @Operation(summary = "关注列表", description = "查看我关注的卖家列表")
    public ApiResponse<List<FollowResponse>> listFollowings() {
        return ApiResponse.success(followService.listFollowings());
    }
}
