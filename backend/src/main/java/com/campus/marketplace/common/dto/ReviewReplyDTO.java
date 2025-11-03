package com.campus.marketplace.common.dto;

import com.campus.marketplace.common.enums.ReplyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评价回复响应DTO
 *
 * Spec #7：回复功能响应数据
 *
 * @author BaSui 😎 - 卖家回复、管理员回复，沟通更有温度！
 * @since 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReplyDTO {

    /**
     * 回复ID
     */
    private Long id;

    /**
     * 评价ID
     */
    private Long reviewId;

    /**
     * 回复人ID
     */
    private Long replierId;

    /**
     * 回复人用户名
     */
    private String replierUsername;

    /**
     * 回复人头像URL
     */
    private String replierAvatar;

    /**
     * 回复类型（SELLER_REPLY/ADMIN_REPLY）
     */
    private ReplyType replyType;

    /**
     * 回复内容
     */
    private String content;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 目标用户ID
     */
    private Long targetUserId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 回复类型显示文本
     */
    public String getReplyTypeText() {
        return replyType == ReplyType.SELLER_REPLY ? "卖家回复" : "管理员回复";
    }
}
