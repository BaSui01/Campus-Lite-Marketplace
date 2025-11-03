package com.campus.marketplace.common.dto.request;

/**
 * 创建社区帖子回复请求DTO（Record类）
 *
 * 用于社区帖子功能的回复
 *
 * @param postId 帖子ID
 * @param content 回复内容
 * @param parentId 父回复ID（可选，用于嵌套回复）
 * @param toUserId 目标用户ID（可选，@某人时使用）
 *
 * @author BaSui 😎 - 社区互动，回复是桥梁！
 * @since 2025-11-03
 */
public record CreatePostReplyRequest(
        Long postId,
        String content,
        Long parentId,
        Long toUserId
) {
}
