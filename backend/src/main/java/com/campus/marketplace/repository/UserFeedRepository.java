package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.UserFeed;
import com.campus.marketplace.common.enums.FeedType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户动态数据访问接口
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Repository
public interface UserFeedRepository extends JpaRepository<UserFeed, Long> {

    /**
     * 根据用户ID查询动态流（按时间倒序）
     */
    List<UserFeed> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 根据用户ID和动态类型查询动态流
     */
    List<UserFeed> findByUserIdAndFeedTypeOrderByCreatedAtDesc(Long userId, FeedType feedType);

    /**
     * 根据动态发起人ID查询动态
     */
    List<UserFeed> findByActorId(Long actorId);

    /**
     * 删除用户的动态记录
     */
    void deleteByUserIdAndActorIdAndTargetId(Long userId, Long actorId, Long targetId);
}
