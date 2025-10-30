package com.campus.marketplace.common.dto.response;

import com.campus.marketplace.common.entity.Reply;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 回复响应 DTO
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Builder
public record ReplyResponse(
        Long id,
        Long postId,
        String content,
        Long authorId,
        String authorName,
        Long parentId,
        Long toUserId,
        String toUserName,
        Integer likeCount,
        LocalDateTime createdAt
) {
    
    /**
     * 从实体转换为 DTO（含作者信息）
     */
    public static ReplyResponse from(Reply reply) {
        return ReplyResponse.builder()
                .id(reply.getId())
                .postId(reply.getPostId())
                .content(reply.getContent())
                .authorId(reply.getAuthorId())
                .authorName(reply.getAuthor() != null ? reply.getAuthor().getUsername() : null)
                .parentId(reply.getParentId())
                .toUserId(reply.getToUserId())
                .toUserName(reply.getToUser() != null ? reply.getToUser().getUsername() : null)
                .likeCount(reply.getLikeCount())
                .createdAt(reply.getCreatedAt())
                .build();
    }
}