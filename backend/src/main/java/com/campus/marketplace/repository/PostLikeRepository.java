package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 帖子点赞数据访问接口
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    /**
     * 根据帖子ID和用户ID查询点赞记录
     */
    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);

    /**
     * 根据帖子ID查询所有点赞记录
     */
    List<PostLike> findByPostId(Long postId);

    /**
     * 根据用户ID查询所有点赞记录
     */
    List<PostLike> findByUserId(Long userId);

    /**
     * 检查用户是否已点赞
     */
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    /**
     * 统计帖子的点赞数
     */
    long countByPostId(Long postId);
}
