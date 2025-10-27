package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 修改密码请求 DTO
 * 
 * @author BaSui
 * @date 2025-10-25
 */
public record UpdatePasswordRequest(
        
        @NotBlank(message = "旧密码不能为空")
        String oldPassword,
        
        @NotBlank(message = "新密码不能为空")
        @Size(min = 8, max = 32, message = "新密码长度必须在 8-32 个字符之间")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", 
                message = "新密码必须包含大小写字母和数字")
        String newPassword
) {
}
