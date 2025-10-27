package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Post;
import com.campus.marketplace.common.enums.GoodsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 帖子 Repository
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 根据 ID 查询帖子（包含作者信息）
     */
    @EntityGraph(attributePaths = {"author"})
    @Query("SELECT p FROM Post p WHERE p.id = :id")
    Optional<Post> findByIdWithAuthor(@Param("id") Long id);

    /**
     * 分页查询帖子（按状态筛选）
     */
    Page<Post> findByStatus(GoodsStatus status, Pageable pageable);

    /**
     * 查询作者的帖子
     */
    Page<Post> findByAuthorId(Long authorId, Pageable pageable);

    /**
     * 搜索帖子（标题或内容包含关键词）
     */
    @Query("SELECT p FROM Post p WHERE p.status = :status AND " +
           "(p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
    Page<Post> searchPosts(@Param("status") GoodsStatus status, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 查询热门帖子（按浏览量和回复数排序）
     */
    @Query("SELECT p FROM Post p WHERE p.status = :status " +
           "ORDER BY (p.viewCount * 0.6 + p.replyCount * 0.4) DESC")
    List<Post> findHotPosts(@Param("status") GoodsStatus status, Pageable pageable);

    /**
     * 统计指定状态的帖子数量
     */
    long countByStatus(GoodsStatus status);

    /**
     * 统计作者的帖子数量
     */
    long countByAuthorId(Long authorId);
}
