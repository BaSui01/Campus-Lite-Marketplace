package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.TopicTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 话题标签关联数据访问接口
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Repository
public interface TopicTagRepository extends JpaRepository<TopicTag, Long> {

    /**
     * 根据话题ID查询所有关联的标签
     */
    List<TopicTag> findByTopicId(Long topicId);

    /**
     * 根据帖子ID查询所有关联的标签
     */
    List<TopicTag> findByPostId(Long postId);

    /**
     * 删除帖子的所有话题标签
     */
    void deleteByPostId(Long postId);

    /**
     * 检查帖子和话题是否已关联
     */
    boolean existsByPostIdAndTopicId(Long postId, Long topicId);
}
