package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.UserProfileResponse;
import org.springframework.data.domain.Page;

/**
 * 黑名单服务接口
 *
 * 提供拉黑、解除拉黑、查询黑名单等功能
 *
 * @author BaSui
 * @date 2025-10-27
 */
public interface BlacklistService {

    /**
     * 添加到黑名单
     *
     * @param blockedUserId 被拉黑的用户ID
     * @param reason 拉黑原因
     */
    void addToBlacklist(Long blockedUserId, String reason);

    /**
     * 从黑名单移除
     *
     * @param blockedUserId 被拉黑的用户ID
     */
    void removeFromBlacklist(Long blockedUserId);

    /**
     * 查询黑名单列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 黑名单用户列表
     */
    Page<UserProfileResponse> listBlacklist(int page, int size);

    /**
     * 检查是否拉黑了某人
     *
     * @param blockedUserId 被检查的用户ID
     * @return true-已拉黑，false-未拉黑
     */
    boolean isBlocked(Long blockedUserId);

    /**
     * 检查两个用户之间是否存在拉黑关系
     *
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @return true-存在拉黑，false-不存在
     */
    boolean isBlockedBetween(Long userId1, Long userId2);
}
