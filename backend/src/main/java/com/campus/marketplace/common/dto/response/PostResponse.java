package com.campus.marketplace.common.dto.response;

import com.campus.marketplace.common.entity.Post;
import com.campus.marketplace.common.enums.GoodsStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 帖子响应 DTO
 * 
 * 封装帖子基本信息（列表展示）
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Builder
public record PostResponse(
        Long id,
        String title,
        String content,
        Long authorId,
        String authorName,
        GoodsStatus status,
        Integer viewCount,
        Integer replyCount,
        List<String> images,
        LocalDateTime createdAt
) {
    
    /**
     * 从实体转换为 DTO（不含作者信息）
     */
    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent().length() > 200 
                        ? post.getContent().substring(0, 200) + "..." 
                        : post.getContent()) // 列表展示只显示前 200 字符
                .authorId(post.getAuthorId())
                .authorName(null) // 列表不返回作者名
                .status(post.getStatus())
                .viewCount(post.getViewCount())
                .replyCount(post.getReplyCount())
                .images(post.getImages() != null ? Arrays.asList(post.getImages()) : List.of())
                .createdAt(post.getCreatedAt())
                .build();
    }
    
    /**
     * 从实体转换为 DTO（含作者信息）
     */
    public static PostResponse fromWithAuthor(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent().length() > 200 
                        ? post.getContent().substring(0, 200) + "..." 
                        : post.getContent())
                .authorId(post.getAuthorId())
                .authorName(post.getAuthor() != null ? post.getAuthor().getUsername() : null)
                .status(post.getStatus())
                .viewCount(post.getViewCount())
                .replyCount(post.getReplyCount())
                .images(post.getImages() != null ? Arrays.asList(post.getImages()) : List.of())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
