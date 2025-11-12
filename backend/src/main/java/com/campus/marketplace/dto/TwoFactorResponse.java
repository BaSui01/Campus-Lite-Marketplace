package com.campus.marketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 两步验证响应
 *
 * @author BaSui 😎
 * @date 2025-11-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "两步验证响应")
public class TwoFactorResponse {

    @Schema(description = "密钥（用于生成二维码）", example = "JBSWY3DPEHPK3PXP")
    private String secret;

    @Schema(description = "二维码 URL", example = "otpauth://totp/CampusMarketplace:user@example.com?secret=JBSWY3DPEHPK3PXP&issuer=CampusMarketplace")
    private String qrCodeUrl;
}
