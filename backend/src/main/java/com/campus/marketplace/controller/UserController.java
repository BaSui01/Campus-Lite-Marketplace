package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.UpdatePasswordRequest;
import com.campus.marketplace.common.dto.request.UpdateProfileRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.UserProfileResponse;
import com.campus.marketplace.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@Tag(name = "用户管理", description = "用户资料与账户设置相关接口")
public class UserController {

    private final UserService userService;

    /**
     * 获取当前登录用户资料
     * 
     * GET /api/users/profile
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "获取当前登录用户资料",
            description = "需要用户已登录，返回当前账户的基础信息、状态、积分及角色等数据。"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "成功响应",
                                    value = """
                                            {
                                              "code": 0,
                                              "message": "操作成功",
                                              "data": {
                                                "id": 10001,
                                                "username": "alice",
                                                "email": "a***@example.com",
                                                "phone": "138****1234",
                                                "studentId": "20240001",
                                                "avatar": "https://cdn.campus.com/avatar/u001.png",
                                                "status": "ACTIVE",
                                                "points": 1200,
                                                "roles": ["ROLE_USER"],
                                                "createdAt": "2024-03-01T12:00:00"
                                              }
                                            }"""
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录或 Token 失效")
    })
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
    @Operation(
            summary = "获取指定用户资料",
            description = "根据用户 ID 查询公开资料，可用于展示用户主页信息。"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "成功响应",
                                    value = """
                                            {
                                              "code": 0,
                                              "message": "操作成功",
                                              "data": {
                                                "id": 10002,
                                                "username": "bob",
                                                "email": "b***@example.com",
                                                "phone": "139****5678",
                                                "studentId": "20240002",
                                                "avatar": "https://cdn.campus.com/avatar/u002.png",
                                                "status": "ACTIVE",
                                                "points": 520,
                                                "roles": ["ROLE_SELLER"],
                                                "createdAt": "2024-04-15T10:30:00"
                                              }
                                            }"""
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @Parameter(name = "userId", description = "用户 ID", example = "10002")
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
    @Operation(
            summary = "更新当前用户资料",
            description = "需要用户已登录，可更新邮箱、手机号、学号和头像等信息。"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "更新资料的 JSON 请求体",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UpdateProfileRequest.class),
                    examples = @ExampleObject(
                            name = "请求示例",
                            value = """
                                    {
                                      "email": "alice@example.com",
                                      "phone": "13812345678",
                                      "studentId": "20240001",
                                      "avatar": "https://cdn.campus.com/avatar/u001.png"
                                    }"""
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "更新成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数校验失败"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录或 Token 失效")
    })
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
    @Operation(
            summary = "修改密码",
            description = "需要用户已登录，提交旧密码与新密码完成密码更新。"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "修改密码的 JSON 请求体",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UpdatePasswordRequest.class),
                    examples = @ExampleObject(
                            name = "请求示例",
                            value = """
                                    {
                                      "oldPassword": "OldPass#123",
                                      "newPassword": "NewPass#456"
                                    }"""
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "修改成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数校验失败"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录或 Token 失效")
    })
    public ApiResponse<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        log.info("修改密码");
        userService.updatePassword(request);
        return ApiResponse.success("密码修改成功", null);
    }
}
