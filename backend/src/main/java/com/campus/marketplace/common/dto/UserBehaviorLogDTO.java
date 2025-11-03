package com.campus.marketplace.common.dto;

import com.campus.marketplace.common.enums.BehaviorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户行为日志DTO
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehaviorLogDTO {

    /**
     * 行为日志ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 行为类型
     */
    private BehaviorType behaviorType;

    /**
     * 目标类型（Goods/Post/User）
     */
    private String targetType;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 来源（搜索/推荐/直接访问）
     */
    private String source;

    /**
     * 浏览时长（秒）
     */
    private Integer duration;

    /**
     * 额外数据（JSON）
     */
    private Map<String, Object> extraData;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
