package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户关注数据访问接口
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    /**
     * 根据关注者ID和被关注者ID查询关注记录
     */
    Optional<UserFollow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    /**
     * 根据关注者ID查询所有关注记录（我关注的人）
     */
    List<UserFollow> findByFollowerId(Long followerId);

    /**
     * 根据被关注者ID查询所有关注记录（我的粉丝）
     */
    List<UserFollow> findByFollowingId(Long followingId);

    /**
     * 检查是否已关注
     */
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    /**
     * 统计关注数（我关注的人数）
     */
    long countByFollowerId(Long followerId);

    /**
     * 统计粉丝数（关注我的人数）
     */
    long countByFollowingId(Long followingId);
}
