package com.campus.marketplace.common.dto;

import com.campus.marketplace.common.enums.FeedType;
import com.campus.marketplace.common.enums.TargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户动态展示用 DTO（精简版）
 * 仅包含前端展示所需字段，避免序列化实体导致的懒加载与冗余字段问题。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFeedDTO {
    private Long id;
    /** 发起人ID（用于跳转到用户主页） */
    private Long actorId;
    /** 显示名（优先昵称，缺省用用户名） */
    private String displayName;
    /** 头像地址 */
    private String avatarUrl;
    /** 动态类型（POST/REVIEW/COLLECT/LIKE） */
    private FeedType feedType;
    /** 目标类型（POST/GOODS） */
    private TargetType targetType;
    /** 目标ID（帖子ID/商品ID等） */
    private Long targetId;
    /** 创建时间 */
    private LocalDateTime createdAt;
}

