package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.UpdatePasswordRequest;
import com.campus.marketplace.common.dto.request.UpdateProfileRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.UserProfileResponse;
import com.campus.marketplace.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 
 * 处理用户资料相关请求
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取当前登录用户资料
     * 
     * GET /api/users/profile
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserProfileResponse> getCurrentUserProfile() {
        log.info("获取当前用户资料");
        UserProfileResponse profile = userService.getCurrentUserProfile();
        return ApiResponse.success(profile);
    }

    /**
     * 获取指定用户资料
     * 
     * GET /api/users/{userId}
     */
    @GetMapping("/{userId}")
    public ApiResponse<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        log.info("获取用户资料: userId={}", userId);
        UserProfileResponse profile = userService.getUserProfile(userId);
        return ApiResponse.success(profile);
    }

    /**
     * 更新用户资料
     * 
     * PUT /api/users/profile
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        log.info("更新用户资料");
        userService.updateProfile(request);
        return ApiResponse.success("资料更新成功", null);
    }

    /**
     * 修改密码
     * 
     * PUT /api/users/password
     */
    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        log.info("修改密码");
        userService.updatePassword(request);
        return ApiResponse.success("密码修改成功", null);
    }
}
