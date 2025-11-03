package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.User;

import java.util.List;

/**
 * 用户关注服务接口
 * 
 * 提供用户关注功能：关注、取消关注、获取关注列表
 * 
 * @author BaSui
 * @date 2025-11-03
 */
public interface UserFollowService {

    /**
     * 关注用户
     * 
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     */
    void followUser(Long followerId, Long followingId);

    /**
     * 取消关注用户
     * 
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     */
    void unfollowUser(Long followerId, Long followingId);

    /**
     * 获取我关注的用户列表
     * 
     * @param userId 用户ID
     * @return 用户列表
     */
    List<User> getFollowingList(Long userId);

    /**
     * 获取我的粉丝列表
     * 
     * @param userId 用户ID
     * @return 用户列表
     */
    List<User> getFollowerList(Long userId);

    /**
     * 检查是否已关注
     * 
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     * @return 是否已关注
     */
    boolean isFollowing(Long followerId, Long followingId);

    /**
     * 获取关注数（我关注的人数）
     * 
     * @param userId 用户ID
     * @return 关注数
     */
    long getFollowingCount(Long userId);

    /**
     * 获取粉丝数（关注我的人数）
     * 
     * @param userId 用户ID
     * @return 粉丝数
     */
    long getFollowerCount(Long userId);
}
