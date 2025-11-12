package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.UserFollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户关注控制器
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "用户关注", description = "用户关注相关接口")
public class UserFollowController {

    private final UserFollowService userFollowService;
    private final UserRepository userRepository;

    @PostMapping("/{userId}/follow")
    @Operation(summary = "关注用户")
    public ApiResponse<Void> followUser(@Parameter(description = "用户ID") @PathVariable Long userId) {
        Long followerId = getCurrentUserId();
        userFollowService.followUser(followerId, userId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{userId}/follow")
    @Operation(summary = "取消关注用户")
    public ApiResponse<Void> unfollowUser(@Parameter(description = "用户ID") @PathVariable Long userId) {
        Long followerId = getCurrentUserId();
        userFollowService.unfollowUser(followerId, userId);
        return ApiResponse.success();
    }

    @GetMapping("/following")
    @Operation(summary = "获取我关注的用户")
    public ApiResponse<List<User>> getFollowingList() {
        Long userId = getCurrentUserId();
        List<User> users = userFollowService.getFollowingList(userId);
        return ApiResponse.success(users);
    }

    @GetMapping("/followers")
    @Operation(summary = "获取我的粉丝")
    public ApiResponse<List<User>> getFollowerList() {
        Long userId = getCurrentUserId();
        List<User> users = userFollowService.getFollowerList(userId);
        return ApiResponse.success(users);
    }

    @GetMapping("/{userId}/following")
    @Operation(summary = "检查是否已关注")
    public ApiResponse<Boolean> checkFollowing(@Parameter(description = "用户ID") @PathVariable Long userId) {
        Long followerId = getCurrentUserId();
        boolean following = userFollowService.isFollowing(followerId, userId);
        return ApiResponse.success(following);
    }

    @GetMapping("/{userId}/following/count")
    @Operation(summary = "获取关注数")
    public ApiResponse<Long> getFollowingCount(@Parameter(description = "用户ID") @PathVariable Long userId) {
        long count = userFollowService.getFollowingCount(userId);
        return ApiResponse.success(count);
    }

    @GetMapping("/{userId}/followers/count")
    @Operation(summary = "获取粉丝数")
    public ApiResponse<Long> getFollowerCount(@Parameter(description = "用户ID") @PathVariable Long userId) {
        long count = userFollowService.getFollowerCount(userId);
        return ApiResponse.success(count);
    }

    private Long getCurrentUserId() {
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }
}
