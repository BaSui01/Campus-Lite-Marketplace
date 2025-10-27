package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

/**
 * 更新用户资料请求 DTO
 * 
 * @author BaSui
 * @date 2025-10-25
 */
public record UpdateProfileRequest(
        
        @Email(message = "邮箱格式不正确")
        String email,
        
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone,
        
        String studentId,
        
        String avatar
) {
}
