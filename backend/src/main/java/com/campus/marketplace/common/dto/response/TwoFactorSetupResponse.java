package com.campus.marketplace.common.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 2FA 设置响应 DTO
 *
 * @author BaSui 😎
 * @date 2025-11-09
 */
@Schema(description = "2FA 设置响应")
public record TwoFactorSetupResponse(

        @Schema(description = "TOTP 密钥（用于手动输入）", example = "JBSWY3DPEHPK3PXP")
        String secret,

        @Schema(description = "QR 码 URL（用于扫描）", example = "otpauth://totp/CampusMarketplace:user@example.com?secret=JBSWY3DPEHPK3PXP&issuer=CampusMarketplace")
        String qrCodeUrl,

        @Schema(description = "恢复码列表（一次性使用）", example = "[\"12345678\", \"87654321\", \"11223344\"]")
        List<String> recoveryCodes,

        @Schema(description = "提示信息", example = "请使用 Google Authenticator 扫描二维码，并保存恢复码")
        String message
) {
}
