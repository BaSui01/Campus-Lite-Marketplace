package com.campus.marketplace.service;

/**
 * 验证码服务（基于 Redis），支持邮箱/短信。
 *
 * @author BaSui
 * @date 2025-10-29
 */

public interface VerificationCodeService {

    void sendEmailCode(String email, String purpose);

    void sendSmsCode(String phone, String purpose);

    boolean validateEmailCode(String email, String purpose, String code);

    boolean validateSmsCode(String phone, String purpose, String code);
}
