package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.TwoFactorSetupResponse;

import java.util.List;

/**
 * 双因素认证服务接口
 *
 * @author BaSui 😎
 * @date 2025-11-09
 */
public interface TwoFactorAuthService {

    /**
     * 启用 2FA（生成密钥和 QR 码）
     *
     * @param userId 用户 ID
     * @return 2FA 设置响应（包含密钥、QR 码 URL、恢复码）
     */
    TwoFactorSetupResponse enable2FA(Long userId);

    /**
     * 验证 2FA 代码并完成启用
     *
     * @param userId 用户 ID
     * @param code   6位数字验证码
     * @return 是否验证成功
     */
    boolean verify2FAAndEnable(Long userId, String code);

    /**
     * 禁用 2FA
     *
     * @param userId   用户 ID
     * @param password 用户密码（用于验证身份）
     */
    void disable2FA(Long userId, String password);

    /**
     * 验证 2FA 代码（登录时使用）
     *
     * @param userId 用户 ID
     * @param code   6位数字验证码
     * @return 是否验证成功
     */
    boolean verify2FACode(Long userId, String code);

    /**
     * 使用恢复码验证（当用户丢失 2FA 设备时）
     *
     * @param userId       用户 ID
     * @param recoveryCode 恢复码
     * @return 是否验证成功
     */
    boolean verifyRecoveryCode(Long userId, String recoveryCode);

    /**
     * 重新生成恢复码
     *
     * @param userId   用户 ID
     * @param password 用户密码（用于验证身份）
     * @return 新的恢复码列表
     */
    List<String> regenerateRecoveryCodes(Long userId, String password);

    /**
     * 检查用户是否启用了 2FA
     *
     * @param userId 用户 ID
     * @return 是否启用
     */
    boolean is2FAEnabled(Long userId);
}
