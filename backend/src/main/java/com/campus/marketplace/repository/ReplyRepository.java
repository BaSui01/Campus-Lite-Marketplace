package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Reply;
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
 * 回复 Repository
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {

    /**
     * 根据 ID 查询回复（包含作者信息）
     */
    @EntityGraph(attributePaths = {"author"})
    @Query("SELECT r FROM Reply r WHERE r.id = :id")
    Optional<Reply> findByIdWithAuthor(@Param("id") Long id);

    /**
     * 查询帖子的所有回复（分页）
     */
    @EntityGraph(attributePaths = {"author", "toUser"})
    @Query("SELECT r FROM Reply r WHERE r.postId = :postId AND r.parentId IS NULL ORDER BY r.createdAt ASC")
    Page<Reply> findByPostId(@Param("postId") Long postId, Pageable pageable);

    /**
     * 查询指定回复的子回复（楼中楼）
     */
    @EntityGraph(attributePaths = {"author", "toUser"})
    @Query("SELECT r FROM Reply r WHERE r.parentId = :parentId ORDER BY r.createdAt ASC")
    List<Reply> findByParentId(@Param("parentId") Long parentId);

    /**
     * 查询用户的所有回复
     */
    Page<Reply> findByAuthorId(Long authorId, Pageable pageable);

    /**
     * 统计帖子的回复数
     */
    long countByPostId(Long postId);

    /**
     * 统计用户的回复数
     */
    long countByAuthorId(Long authorId);
}