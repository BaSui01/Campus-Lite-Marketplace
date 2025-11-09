package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.Disable2FARequest;
import com.campus.marketplace.common.dto.request.Verify2FARequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.TwoFactorSetupResponse;
import com.campus.marketplace.service.TwoFactorAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 双因素认证控制器
 *
 * @author BaSui 😎
 * @date 2025-11-09
 */
@Slf4j
@RestController
@RequestMapping("/auth/2fa")
@RequiredArgsConstructor
@Tag(name = "双因素认证", description = "2FA 相关接口")
public class TwoFactorAuthController {

    private final TwoFactorAuthService twoFactorAuthService;

    /**
     * 启用 2FA（生成密钥和 QR 码）
     *
     * POST /api/auth/2fa/enable
     */
    @PostMapping("/enable")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "启用 2FA",
            description = "生成 TOTP 密钥和 QR 码，返回恢复码（仅显示一次）"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "启用成功",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录或 Token 失效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "2FA 已启用")
    })
    public ApiResponse<TwoFactorSetupResponse> enable2FA(Authentication authentication) {
        log.info("🔐 收到启用 2FA 请求: username={}", authentication.getName());

        Long userId = Long.parseLong(authentication.getName());
        TwoFactorSetupResponse response = twoFactorAuthService.enable2FA(userId);

        return ApiResponse.success("2FA 密钥生成成功，请扫描二维码并保存恢复码", response);
    }

    /**
     * 验证 2FA 代码并完成启用
     *
     * POST /api/auth/2fa/verify
     */
    @PostMapping("/verify")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "验证 2FA 代码并完成启用",
            description = "验证用户输入的 6 位数字验证码，验证成功后正式启用 2FA"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "验证成功",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录或 Token 失效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "验证码错误")
    })
    public ApiResponse<Void> verify2FA(
            Authentication authentication,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "验证码",
                    required = true
            )
            @Valid @RequestBody Verify2FARequest request
    ) {
        log.info("🔐 收到验证 2FA 代码请求: username={}", authentication.getName());

        Long userId = Long.parseLong(authentication.getName());
        boolean isValid = twoFactorAuthService.verify2FAAndEnable(userId, request.code());

        if (isValid) {
            return ApiResponse.success("2FA 启用成功", null);
        } else {
            return ApiResponse.error(400, "验证码错误，请重试");
        }
    }

    /**
     * 禁用 2FA
     *
     * POST /api/auth/2fa/disable
     */
    @PostMapping("/disable")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "禁用 2FA",
            description = "禁用双因素认证，需要验证用户密码"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "禁用成功",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录或 Token 失效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "密码错误")
    })
    public ApiResponse<Void> disable2FA(
            Authentication authentication,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "密码",
                    required = true
            )
            @Valid @RequestBody Disable2FARequest request
    ) {
        log.info("🔐 收到禁用 2FA 请求: username={}", authentication.getName());

        Long userId = Long.parseLong(authentication.getName());
        twoFactorAuthService.disable2FA(userId, request.password());

        return ApiResponse.success("2FA 禁用成功", null);
    }

    /**
     * 重新生成恢复码
     *
     * POST /api/auth/2fa/recovery-codes/regenerate
     */
    @PostMapping("/recovery-codes/regenerate")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "重新生成恢复码",
            description = "重新生成 2FA 恢复码，需要验证用户密码"
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
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "密码错误或 2FA 未启用")
    })
    public ApiResponse<List<String>> regenerateRecoveryCodes(
            Authentication authentication,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "密码",
                    required = true
            )
            @Valid @RequestBody Disable2FARequest request
    ) {
        log.info("🔐 收到重新生成恢复码请求: username={}", authentication.getName());

        Long userId = Long.parseLong(authentication.getName());
        List<String> recoveryCodes = twoFactorAuthService.regenerateRecoveryCodes(userId, request.password());

        return ApiResponse.success("恢复码重新生成成功，请妥善保存（仅显示一次）", recoveryCodes);
    }

    /**
     * 检查 2FA 状态
     *
     * GET /api/auth/2fa/status
     */
    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "检查 2FA 状态",
            description = "查询当前用户是否启用了 2FA"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "查询成功",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录或 Token 失效")
    })
    public ApiResponse<Boolean> check2FAStatus(Authentication authentication) {
        log.info("🔐 收到检查 2FA 状态请求: username={}", authentication.getName());

        Long userId = Long.parseLong(authentication.getName());
        boolean isEnabled = twoFactorAuthService.is2FAEnabled(userId);

        return ApiResponse.success(isEnabled);
    }
}
