package com.campus.marketplace.service;

/**
 * 在线状态服务接口
 * 
 * @author BaSui
 * @date 2025-11-03
 */
public interface OnlineStatusService {

    /**
     * 更新用户最后活跃时间
     * 
     * @param userId 用户ID
     */
    void updateLastActiveTime(Long userId);

    /**
     * 检查用户是否在线
     * 
     * @param userId 用户ID
     * @return 是否在线（最近5分钟内活跃）
     */
    boolean isUserOnline(Long userId);

    /**
     * 批量检查用户在线状态
     * 
     * @param userIds 用户ID列表
     * @return 用户ID -> 在线状态的映射
     */
    java.util.Map<Long, Boolean> batchCheckOnlineStatus(java.util.List<Long> userIds);
}
