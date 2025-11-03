package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.TopicFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 话题关注数据访问接口
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Repository
public interface TopicFollowRepository extends JpaRepository<TopicFollow, Long> {

    /**
     * 根据话题ID和用户ID查询关注记录
     */
    Optional<TopicFollow> findByTopicIdAndUserId(Long topicId, Long userId);

    /**
     * 根据用户ID查询所有关注的话题
     */
    List<TopicFollow> findByUserId(Long userId);

    /**
     * 根据话题ID查询所有关注者
     */
    List<TopicFollow> findByTopicId(Long topicId);

    /**
     * 检查用户是否已关注话题
     */
    boolean existsByTopicIdAndUserId(Long topicId, Long userId);

    /**
     * 统计话题的关注人数
     */
    long countByTopicId(Long topicId);
}
