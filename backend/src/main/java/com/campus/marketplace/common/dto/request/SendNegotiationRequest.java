package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.*;

/**
 * 发送协商消息请求DTO（普通文字消息）
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
public record SendNegotiationRequest(

        @NotNull(message = "纠纷ID不能为空")
        Long disputeId,

        @NotBlank(message = "消息内容不能为空")
        @Size(min = 1, max = 1000, message = "消息内容长度必须在1-1000字符之间")
        String content
) {
}
