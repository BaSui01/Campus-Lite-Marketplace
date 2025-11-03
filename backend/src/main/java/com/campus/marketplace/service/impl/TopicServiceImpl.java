package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.Topic;
import com.campus.marketplace.common.entity.TopicFollow;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.TopicFollowRepository;
import com.campus.marketplace.repository.TopicRepository;
import com.campus.marketplace.service.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 话题服务实现类
 * 
 * 实现话题管理的核心功能：CRUD、关注、热门推荐
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;
    private final TopicFollowRepository topicFollowRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTopic(String name, String description) {
        log.info("创建话题: name={}, description={}", name, description);

        // 检查话题名称是否已存在
        if (topicRepository.existsByName(name)) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "话题名称已存在");
        }

        Topic topic = Topic.builder()
            .name(name)
            .description(description)
            .hotness(0)
            .postCount(0)
            .followerCount(0)
            .build();

        topic = topicRepository.save(topic);
        log.info("话题创建成功: topicId={}, name={}", topic.getId(), name);
        return topic.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTopic(Long topicId, String name, String description) {
        log.info("更新话题: topicId={}, name={}, description={}", topicId, name, description);

        Topic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "话题不存在"));

        // 如果修改了名称，检查新名称是否已存在
        if (!topic.getName().equals(name) && topicRepository.existsByName(name)) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "话题名称已存在");
        }

        topic.setName(name);
        topic.setDescription(description);
        topicRepository.save(topic);

        log.info("话题更新成功: topicId={}", topicId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTopic(Long topicId) {
        log.info("删除话题: topicId={}", topicId);

        Topic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "话题不存在"));

        topicRepository.delete(topic);
        log.info("话题删除成功: topicId={}", topicId);
    }

    @Override
    public Topic getTopicById(Long topicId) {
        return topicRepository.findById(topicId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "话题不存在"));
    }

    @Override
    public Topic getTopicByName(String name) {
        return topicRepository.findByName(name)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "话题不存在"));
    }

    @Override
    public List<Topic> getAllTopics() {
        log.info("获取所有话题");
        return topicRepository.findAll();
    }

    @Override
    public List<Topic> getHotTopics() {
        log.info("获取热门话题");
        return topicRepository.findTop10ByOrderByHotnessDesc();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void followTopic(Long topicId, Long userId) {
        log.info("关注话题: topicId={}, userId={}", topicId, userId);

        // 检查话题是否存在
        Topic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "话题不存在"));

        // 检查是否已关注
        if (topicFollowRepository.existsByTopicIdAndUserId(topicId, userId)) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "已经关注过了");
        }

        // 创建关注记录
        TopicFollow topicFollow = TopicFollow.builder()
            .topicId(topicId)
            .userId(userId)
            .build();
        topicFollowRepository.save(topicFollow);

        // 更新话题关注人数
        topic.incrementFollowerCount();
        topic.updateHotness();
        topicRepository.save(topic);

        log.info("关注话题成功: topicId={}, userId={}", topicId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollowTopic(Long topicId, Long userId) {
        log.info("取消关注话题: topicId={}, userId={}", topicId, userId);

        // 检查话题是否存在
        Topic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "话题不存在"));

        // 查找关注记录
        TopicFollow topicFollow = topicFollowRepository.findByTopicIdAndUserId(topicId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "关注记录不存在"));

        // 删除关注记录
        topicFollowRepository.delete(topicFollow);

        // 更新话题关注人数
        topic.decrementFollowerCount();
        topic.updateHotness();
        topicRepository.save(topic);

        log.info("取消关注话题成功: topicId={}, userId={}", topicId, userId);
    }

    @Override
    public List<Topic> getUserFollowedTopics(Long userId) {
        log.info("获取用户关注的话题: userId={}", userId);
        List<TopicFollow> follows = topicFollowRepository.findByUserId(userId);
        return follows.stream()
            .map(TopicFollow::getTopicId)
            .map(this::getTopicById)
            .collect(Collectors.toList());
    }

    @Override
    public boolean isTopicFollowedByUser(Long topicId, Long userId) {
        return topicFollowRepository.existsByTopicIdAndUserId(topicId, userId);
    }

    @Override
    public long getTopicFollowerCount(Long topicId) {
        return topicFollowRepository.countByTopicId(topicId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTopicHotness(Long topicId) {
        log.info("更新话题热度: topicId={}", topicId);

        Topic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "话题不存在"));

        topic.updateHotness();
        topicRepository.save(topic);

        log.info("话题热度更新成功: topicId={}, hotness={}", topicId, topic.getHotness());
    }
}
