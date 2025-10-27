package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 发送消息请求DTO
 *
 * @author BaSui
 * @date 2025-10-27
 */
public record SendMessageRequest(

        /**
         * 接收者ID
         */
        @NotNull(message = "接收者ID不能为空")
        Long receiverId,

        /**
         * 消息类型
         */
        @NotNull(message = "消息类型不能为空")
        MessageType messageType,

        /**
         * 消息内容
         */
        @NotBlank(message = "消息内容不能为空")
        @Size(max = 5000, message = "消息内容不能超过5000字符")
        String content
) {
}
