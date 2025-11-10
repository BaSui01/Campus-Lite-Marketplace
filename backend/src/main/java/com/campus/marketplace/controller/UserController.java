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
     * 获取用户列表（管理端）
     *
     * GET /api/users
     */
    @GetMapping
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_USER_VIEW)")
    @Operation(summary = "获取用户列表", description = "管理端查询用户列表（分页）")
    public ApiResponse<org.springframework.data.domain.Page<UserProfileResponse>> listUsers(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String keyword,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String status,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size) {
        log.info("查询用户列表: keyword={}, status={}, page={}, size={}", keyword, status, page, size);
        org.springframework.data.domain.Page<UserProfileResponse> users = userService.listUsers(keyword, status, page, size);
        return ApiResponse.success(users);
    }

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

    // ==================== 登录设备管理 ====================

    /**
     * 获取登录设备列表
     *
     * GET /api/users/{userId}/devices
     */
    @GetMapping("/{userId}/devices")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "获取用户登录设备列表",
            description = "查看用户的所有登录设备记录，包括设备类型、浏览器、IP 地址等信息。"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录或 Token 失效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权查看其他用户的设备")
    })
    @Parameter(name = "userId", description = "用户 ID", example = "10001")
    public ApiResponse<java.util.List<com.campus.marketplace.dto.LoginDeviceDTO>> getLoginDevices(@PathVariable Long userId) {
        log.info("获取用户登录设备列表: userId={}", userId);
        java.util.List<com.campus.marketplace.dto.LoginDeviceDTO> devices = userService.getLoginDevices(userId);
        return ApiResponse.success(devices);
    }

    /**
     * 踢出登录设备
     *
     * DELETE /api/users/{userId}/devices/{deviceId}
     */
    @org.springframework.web.bind.annotation.DeleteMapping("/{userId}/devices/{deviceId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "踢出登录设备",
            description = "删除指定的登录设备记录，该设备将无法继续访问（需要重新登录）。"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "踢出成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "不能踢出当前设备"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录或 Token 失效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权操作其他用户的设备"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "设备不存在")
    })
    @Parameter(name = "userId", description = "用户 ID", example = "10001")
    @Parameter(name = "deviceId", description = "设备 ID", example = "1")
    public ApiResponse<Void> kickDevice(@PathVariable Long userId, @PathVariable Long deviceId) {
        log.info("踢出登录设备: userId={}, deviceId={}", userId, deviceId);
        userService.kickDevice(userId, deviceId);
        return ApiResponse.success("设备已踢出", null);
    }

    // ==================== 邮箱/手机验证 ====================

    /**
     * 发送邮箱验证码
     *
     * POST /api/users/email/code
     */
    @org.springframework.web.bind.annotation.PostMapping("/email/code")
    @Operation(
            summary = "发送邮箱验证码",
            description = "向指定邮箱发送验证码，用于绑定邮箱。"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "发送成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "邮箱格式错误或发送频率过高")
    })
    public ApiResponse<Void> sendEmailCode(@org.springframework.web.bind.annotation.RequestParam String email) {
        log.info("发送邮箱验证码: email={}", email);
        userService.sendEmailCode(email);
        return ApiResponse.success("验证码已发送", null);
    }

    /**
     * 发送手机验证码
     *
     * POST /api/users/phone/code
     */
    @org.springframework.web.bind.annotation.PostMapping("/phone/code")
    @Operation(
            summary = "发送手机验证码",
            description = "向指定手机号发送验证码，用于绑定手机号。"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "发送成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "手机号格式错误或发送频率过高")
    })
    public ApiResponse<Void> sendPhoneCode(@org.springframework.web.bind.annotation.RequestParam String phone) {
        log.info("发送手机验证码: phone={}", phone);
        userService.sendPhoneCode(phone);
        return ApiResponse.success("验证码已发送", null);
    }

    /**
     * 绑定邮箱
     *
     * POST /api/users/{userId}/email
     */
    @org.springframework.web.bind.annotation.PostMapping("/{userId}/email")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "绑定邮箱",
            description = "使用验证码绑定邮箱到用户账户。"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "绑定邮箱请求",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = com.campus.marketplace.dto.BindEmailRequest.class)
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "绑定成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "验证码错误或邮箱已被使用"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录或 Token 失效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权操作其他用户的邮箱")
    })
    @Parameter(name = "userId", description = "用户 ID", example = "10001")
    public ApiResponse<Void> bindEmail(@PathVariable Long userId, @Valid @RequestBody com.campus.marketplace.dto.BindEmailRequest request) {
        log.info("绑定邮箱: userId={}, email={}", userId, request.getEmail());
        userService.bindEmail(userId, request);
        return ApiResponse.success("邮箱绑定成功", null);
    }

    /**
     * 绑定手机号
     *
     * POST /api/users/{userId}/phone
     */
    @org.springframework.web.bind.annotation.PostMapping("/{userId}/phone")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "绑定手机号",
            description = "使用验证码绑定手机号到用户账户。"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "绑定手机号请求",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = com.campus.marketplace.dto.BindPhoneRequest.class)
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "绑定成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "验证码错误或手机号已被使用"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录或 Token 失效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权操作其他用户的手机号")
    })
    @Parameter(name = "userId", description = "用户 ID", example = "10001")
    public ApiResponse<Void> bindPhone(@PathVariable Long userId, @Valid @RequestBody com.campus.marketplace.dto.BindPhoneRequest request) {
        log.info("绑定手机号: userId={}, phone={}", userId, request.getPhone());
        userService.bindPhone(userId, request);
        return ApiResponse.success("手机号绑定成功", null);
    }

    // ==================== 两步验证（2FA）====================

    /**
     * 启用两步验证
     *
     * POST /api/users/{userId}/2fa/enable
     */
    @org.springframework.web.bind.annotation.PostMapping("/{userId}/2fa/enable")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "启用两步验证",
            description = "生成 TOTP 密钥和二维码 URL，用户需要使用 Google Authenticator 等应用扫描二维码。"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "生成成功",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录或 Token 失效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权操作其他用户的两步验证")
    })
    @Parameter(name = "userId", description = "用户 ID", example = "10001")
    public ApiResponse<com.campus.marketplace.dto.TwoFactorResponse> enableTwoFactor(@PathVariable Long userId) {
        log.info("启用两步验证: userId={}", userId);
        com.campus.marketplace.dto.TwoFactorResponse response = userService.enableTwoFactor(userId);
        return ApiResponse.success(response);
    }

    /**
     * 验证并确认两步验证
     *
     * POST /api/users/{userId}/2fa/verify
     */
    @org.springframework.web.bind.annotation.PostMapping("/{userId}/2fa/verify")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "验证并确认两步验证",
            description = "输入 Google Authenticator 中的 6 位验证码，验证成功后启用两步验证。"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "两步验证请求",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = com.campus.marketplace.dto.TwoFactorRequest.class)
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "验证成功，两步验证已启用"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "验证码错误"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录或 Token 失效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权操作其他用户的两步验证")
    })
    @Parameter(name = "userId", description = "用户 ID", example = "10001")
    public ApiResponse<Void> verifyTwoFactor(@PathVariable Long userId, @Valid @RequestBody com.campus.marketplace.dto.TwoFactorRequest request) {
        log.info("验证两步验证: userId={}", userId);
        userService.verifyTwoFactor(userId, request);
        return ApiResponse.success("两步验证已启用", null);
    }

    /**
     * 关闭两步验证
     *
     * POST /api/users/{userId}/2fa/disable
     */
    @org.springframework.web.bind.annotation.PostMapping("/{userId}/2fa/disable")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "关闭两步验证",
            description = "关闭用户的两步验证功能。"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "关闭成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录或 Token 失效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权操作其他用户的两步验证")
    })
    @Parameter(name = "userId", description = "用户 ID", example = "10001")
    public ApiResponse<Void> disableTwoFactor(@PathVariable Long userId) {
        log.info("关闭两步验证: userId={}", userId);
        userService.disableTwoFactor(userId);
        return ApiResponse.success("两步验证已关闭", null);
    }
}
