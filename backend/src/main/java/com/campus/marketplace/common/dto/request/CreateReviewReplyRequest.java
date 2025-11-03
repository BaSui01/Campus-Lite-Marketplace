package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.ReplyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建评价回复请求DTO
 *
 * Spec #7：回复功能请求参数（评价系统专用）
 *
 * @author BaSui 😎 - 卖家回复、管理员回复，沟通桥梁！
 * @since 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewReplyRequest {

    /**
     * 回复类型（SELLER_REPLY/ADMIN_REPLY）
     */
    @NotNull(message = "回复类型不能为空")
    private ReplyType replyType;

    /**
     * 回复内容
     */
    @NotBlank(message = "回复内容不能为空")
    @Size(min = 1, max = 500, message = "回复内容长度必须在1-500字之间")
    private String content;

    /**
     * 目标用户ID（评价人，用于发送通知）
     * 由后端自动设置，前端无需传递
     */
    private Long targetUserId;
}
