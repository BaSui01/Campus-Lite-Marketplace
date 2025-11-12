package com.campus.marketplace.common.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 更新用户资料请求 DTO
 *
 * @author BaSui
 * @date 2025-10-25
 */
@Schema(description = "更新用户资料请求体")
public record UpdateProfileRequest(

        @Schema(description = "昵称", example = "小明")
        @Size(min = 1, max = 20, message = "昵称长度必须在1-20个字符之间")
        String nickname,

        @Schema(description = "个人简介", example = "热爱生活，喜欢交友")
        @Size(max = 200, message = "个人简介不能超过200个字符")
        String bio,

        @Schema(description = "联系邮箱", example = "alice@example.com")
        @Email(message = "邮箱格式不正确")
        String email,

        @Schema(description = "联系手机号", example = "13812345678")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone,

        @Schema(description = "学号", example = "20240001")
        @Size(max = 20, message = "学号不能超过20个字符")
        String studentId,

        @Schema(description = "头像地址", example = "https://cdn.campus.com/avatar/u001.png")
        @Pattern(regexp = "^(https?://.*\\.(jpg|jpeg|png|gif|webp)|/uploads/.*)$", message = "头像URL格式不正确")
        String avatar
) {
}
