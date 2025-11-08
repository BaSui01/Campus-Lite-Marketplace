package com.campus.marketplace.common.dto.response;

import lombok.Builder;

import java.util.List;

/**
 * 帖子统计响应 DTO
 *
 * 封装帖子的详细统计信息
 *
 * @author BaSui
 * @date 2025-11-09
 */
@Builder
public record PostStatsResponse(
        Long postId,
        String title,
        Integer viewCount,
        Integer replyCount,
        Integer likeCount,
        Integer collectCount,
        List<UserBriefInfo> likeUsers,    // 点赞用户列表（最多10个）
        List<UserBriefInfo> collectUsers  // 收藏用户列表（最多10个）
) {

    /**
     * 用户简要信息
     */
    @Builder
    public record UserBriefInfo(
            Long userId,
            String username,
            String avatar
    ) {}
}
