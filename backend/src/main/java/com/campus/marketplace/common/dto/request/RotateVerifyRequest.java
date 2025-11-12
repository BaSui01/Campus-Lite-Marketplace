package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 旋转验证请求 DTO
 *
 * @author BaSui 😎
 * @date 2025-11-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RotateVerifyRequest {

    /**
     * 旋转验证码ID
     */
    @NotBlank(message = "旋转验证码ID不能为空")
    private String rotateId;

    /**
     * 用户旋转的角度（0-360度）
     */
    @NotNull(message = "旋转角度不能为空")
    private Integer angle;
}
