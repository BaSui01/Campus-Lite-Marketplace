package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.PostCollect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 帖子收藏数据访问接口
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Repository
public interface PostCollectRepository extends JpaRepository<PostCollect, Long> {

    /**
     * 根据帖子ID和用户ID查询收藏记录
     */
    Optional<PostCollect> findByPostIdAndUserId(Long postId, Long userId);

    /**
     * 根据帖子ID查询所有收藏记录
     */
    List<PostCollect> findByPostId(Long postId);

    /**
     * 根据用户ID查询所有收藏记录
     */
    List<PostCollect> findByUserId(Long userId);

    /**
     * 检查用户是否已收藏
     */
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    /**
     * 统计帖子的收藏数
     */
    long countByPostId(Long postId);
}
