package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.Topic;
import com.campus.marketplace.common.entity.UserFeed;

import java.util.List;

/**
 * 社区服务接口
 * 
 * 提供社区广场的核心功能：话题管理、动态流、互动功能
 * 
 * @author BaSui
 * @date 2025-11-03
 */
public interface CommunityService {

    /**
     * 获取热门话题（前10个）
     */
    List<Topic> getHotTopics();

    /**
     * 为帖子添加话题标签
     * 
     * @param postId 帖子ID
     * @param topicIds 话题ID列表（最多3个）
     */
    void addTopicTagsToPost(Long postId, List<Long> topicIds);

    /**
     * 移除帖子的话题标签
     * 
     * @param postId 帖子ID
     */
    void removeTopicTagsFromPost(Long postId);

    /**
     * 点赞帖子
     * 
     * @param postId 帖子ID
     * @param userId 用户ID
     */
    void likePost(Long postId, Long userId);

    /**
     * 取消点赞
     * 
     * @param postId 帖子ID
     * @param userId 用户ID
     */
    void unlikePost(Long postId, Long userId);

    /**
     * 收藏帖子
     * 
     * @param postId 帖子ID
     * @param userId 用户ID
     */
    void collectPost(Long postId, Long userId);

    /**
     * 取消收藏
     * 
     * @param postId 帖子ID
     * @param userId 用户ID
     */
    void uncollectPost(Long postId, Long userId);

    /**
     * 获取用户动态流
     * 
     * @param userId 用户ID
     * @return 动态列表
     */
    List<UserFeed> getUserFeed(Long userId);

    /**
     * 获取话题下的帖子ID列表
     * 
     * @param topicId 话题ID
     * @return 帖子ID列表
     */
    List<Long> getPostIdsByTopicId(Long topicId);

    /**
     * 检查用户是否已点赞
     * 
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 是否已点赞
     */
    boolean isPostLikedByUser(Long postId, Long userId);

    /**
     * 检查用户是否已收藏
     * 
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 是否已收藏
     */
    boolean isPostCollectedByUser(Long postId, Long userId);

    /**
     * 获取帖子点赞数
     * 
     * @param postId 帖子ID
     * @return 点赞数
     */
    long getPostLikeCount(Long postId);

    /**
     * 获取帖子收藏数
     * 
     * @param postId 帖子ID
     * @return 收藏数
     */
    long getPostCollectCount(Long postId);
}
