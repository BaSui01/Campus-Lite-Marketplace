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
 * 封装帖子完整信息（前端显示）
 *
 * @author BaSui
 * @date 2025-10-27
 * @updated 2025-11-09 - 添加前端必需字段（点赞数、收藏数、用户状态等）😎
 */
@Builder
public record PostResponse(
        // ==================== 基础信息 ====================
        Long id,
        String title,
        String content,

        // ==================== 作者信息 ====================
        Long authorId,
        String authorName,
        String authorAvatar, // 新增：作者头像

        // ==================== 状态信息 ====================
        GoodsStatus status,
        Boolean isTop, // 新增：是否置顶
        Boolean isHot, // 新增：是否热门

        // ==================== 统计信息 ====================
        Integer viewCount,
        Integer replyCount,
        Integer likeCount, // 新增：点赞数
        Integer collectCount, // 新增：收藏数

        // ==================== 用户状态（当前用户） ====================
        Boolean isLiked, // 新增：当前用户是否已点赞
        Boolean isCollected, // 新增：当前用户是否已收藏

        // ==================== 媒体信息 ====================
        List<String> images,

        // ==================== 关联信息 ====================
        String campusName, // 新增：校区名称

        // ==================== 时间信息 ====================
        LocalDateTime createdAt,
        LocalDateTime updatedAt // 新增：更新时间
) {

    /**
     * 从实体转换为 DTO（不含作者信息，不含用户状态）
     *
     * 用于列表展示（无需查询作者和用户状态）
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
                .authorAvatar(null)
                .status(post.getStatus())
                .isTop(post.getIsTop())
                .isHot(post.getIsHot())
                .viewCount(post.getViewCount())
                .replyCount(post.getReplyCount())
                .likeCount(post.getLikeCount())
                .collectCount(post.getCollectCount())
                .isLiked(null) // 列表不返回用户状态
                .isCollected(null)
                .images(post.getImages() != null ? Arrays.asList(post.getImages()) : List.of())
                .campusName(null)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    /**
     * 从实体转换为 DTO（含作者信息，不含用户状态）
     *
     * 用于列表展示（需要显示作者名和头像）
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
                .authorAvatar(post.getAuthor() != null ? post.getAuthor().getAvatar() : null)
                .status(post.getStatus())
                .isTop(post.getIsTop())
                .isHot(post.getIsHot())
                .viewCount(post.getViewCount())
                .replyCount(post.getReplyCount())
                .likeCount(post.getLikeCount())
                .collectCount(post.getCollectCount())
                .isLiked(null) // 列表不返回用户状态
                .isCollected(null)
                .images(post.getImages() != null ? Arrays.asList(post.getImages()) : List.of())
                .campusName(post.getCampus() != null ? post.getCampus().getName() : null)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    /**
     * 从实体转换为 DTO（含作者信息 + 用户状态）
     *
     * 用于详情页（需要显示完整信息和用户状态）
     *
     * @param post 帖子实体
     * @param isLiked 当前用户是否已点赞
     * @param isCollected 当前用户是否已收藏
     * @return 完整的帖子响应DTO
     *
     * @since 2025-11-09 - BaSui 😎
     */
    public static PostResponse fromWithUserContext(
            Post post,
            boolean isLiked,
            boolean isCollected
    ) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent()) // 详情页显示完整内容
                .authorId(post.getAuthorId())
                .authorName(post.getAuthor() != null ? post.getAuthor().getUsername() : null)
                .authorAvatar(post.getAuthor() != null ? post.getAuthor().getAvatar() : null)
                .status(post.getStatus())
                .isTop(post.getIsTop())
                .isHot(post.getIsHot())
                .viewCount(post.getViewCount())
                .replyCount(post.getReplyCount())
                .likeCount(post.getLikeCount())
                .collectCount(post.getCollectCount())
                .isLiked(isLiked) // 用户状态
                .isCollected(isCollected) // 用户状态
                .images(post.getImages() != null ? Arrays.asList(post.getImages()) : List.of())
                .campusName(post.getCampus() != null ? post.getCampus().getName() : null)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
