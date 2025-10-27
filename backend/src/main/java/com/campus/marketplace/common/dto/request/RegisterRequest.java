package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 用户注册请求 DTO
 * 
 * 使用 Java 21 Record 类型简化代码
 * 
 * @author BaSui
 * @date 2025-10-25
 */
public record RegisterRequest(
        
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 20, message = "用户名长度必须在 3-20 个字符之间")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
        String username,
        
        @NotBlank(message = "密码不能为空")
        @Size(min = 8, max = 32, message = "密码长度必须在 8-32 个字符之间")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", 
                message = "密码必须包含大小写字母和数字")
        String password,
        
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.(edu|edu\\.cn)$", 
                message = "必须使用校园邮箱注册")
        String email
) {
}
