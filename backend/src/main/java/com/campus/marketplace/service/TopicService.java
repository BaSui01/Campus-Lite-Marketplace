package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.Topic;

import java.util.List;

/**
 * 话题服务接口
 * 
 * 提供话题管理的核心功能：CRUD、关注、热门推荐
 * 
 * @author BaSui
 * @date 2025-11-03
 */
public interface TopicService {

    /**
     * 创建话题
     * 
     * @param name 话题名称
     * @param description 话题描述
     * @return 话题ID
     */
    Long createTopic(String name, String description);

    /**
     * 更新话题
     * 
     * @param topicId 话题ID
     * @param name 话题名称
     * @param description 话题描述
     */
    void updateTopic(Long topicId, String name, String description);

    /**
     * 删除话题
     * 
     * @param topicId 话题ID
     */
    void deleteTopic(Long topicId);

    /**
     * 根据ID查询话题
     * 
     * @param topicId 话题ID
     * @return 话题实体
     */
    Topic getTopicById(Long topicId);

    /**
     * 根据名称查询话题
     * 
     * @param name 话题名称
     * @return 话题实体
     */
    Topic getTopicByName(String name);

    /**
     * 获取所有话题
     * 
     * @return 话题列表
     */
    List<Topic> getAllTopics();

    /**
     * 获取热门话题（前10个）
     * 
     * @return 热门话题列表
     */
    List<Topic> getHotTopics();

    /**
     * 关注话题
     * 
     * @param topicId 话题ID
     * @param userId 用户ID
     */
    void followTopic(Long topicId, Long userId);

    /**
     * 取消关注话题
     * 
     * @param topicId 话题ID
     * @param userId 用户ID
     */
    void unfollowTopic(Long topicId, Long userId);

    /**
     * 获取用户关注的所有话题
     * 
     * @param userId 用户ID
     * @return 话题列表
     */
    List<Topic> getUserFollowedTopics(Long userId);

    /**
     * 检查用户是否已关注话题
     * 
     * @param topicId 话题ID
     * @param userId 用户ID
     * @return 是否已关注
     */
    boolean isTopicFollowedByUser(Long topicId, Long userId);

    /**
     * 获取话题关注人数
     * 
     * @param topicId 话题ID
     * @return 关注人数
     */
    long getTopicFollowerCount(Long topicId);

    /**
     * 更新话题热度
     * 
     * @param topicId 话题ID
     */
    void updateTopicHotness(Long topicId);
}
