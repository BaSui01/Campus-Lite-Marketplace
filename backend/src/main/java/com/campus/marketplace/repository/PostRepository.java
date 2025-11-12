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
import com.campus.marketplace.repository.projection.PostSearchProjection;

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
     * 按校区查询帖子
     */
    Page<Post> findByStatusAndCampusId(GoodsStatus status, Long campusId, Pageable pageable);

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

    @Query("SELECT p FROM Post p WHERE p.status = :status AND p.campusId = :campusId AND " +
           "(p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
    Page<Post> searchPostsWithCampus(@Param("status") GoodsStatus status,
                                     @Param("keyword") String keyword,
                                     @Param("campusId") Long campusId,
                                     Pageable pageable);

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

    /**
     * 按校区统计帖子数量
     */
    long countByCampusId(Long campusId);

    /**
     * 帖子全文检索（高亮 + 排序 + 分页 + 校区过滤）
     */
    @Query(value = "SELECT p.id as id, p.title as title, " +
            "ts_headline('chinese', coalesce(p.content, ''), plainto_tsquery('chinese', :q), 'MaxFragments=2, MaxWords=12, MinWords=4, StartSel=<em>, StopSel=</em>') as snippet, " +
            "ts_rank(p.search_vector, plainto_tsquery('chinese', :q)) as rank, " +
            "p.campus_id as campusId " +
            "FROM t_post p " +
            "WHERE p.status = 'APPROVED' " +
            "AND p.search_vector @@ plainto_tsquery('chinese', :q) " +
            "AND (:campusId IS NULL OR p.campus_id = :campusId) " +
            "ORDER BY rank DESC, p.created_at DESC",
            countQuery = "SELECT COUNT(*) FROM t_post p WHERE p.status='APPROVED' AND p.search_vector @@ plainto_tsquery('chinese', :q) AND (:campusId IS NULL OR p.campus_id = :campusId)",
            nativeQuery = true)
    Page<PostSearchProjection> searchPostsFts(@Param("q") String q,
                                              @Param("campusId") Long campusId,
                                              Pageable pageable);

    // ==================== 新增查询方法（2025-11-09 - BaSui 😎）====================

    /**
     * 查询热门帖子（按新的热度算法排序）
     *
     * 热度 = 点赞数 * 2 + 浏览量 + 回复数 * 3
     *
     * @param status 帖子状态
     * @param pageable 分页参数
     * @return 热门帖子分页结果
     * @since 2025-11-09
     */
    @EntityGraph(attributePaths = {"author", "campus"})
    @Query("SELECT p FROM Post p WHERE p.status = :status " +
           "ORDER BY (p.likeCount * 2 + p.viewCount + p.replyCount * 3) DESC, p.createdAt DESC")
    Page<Post> findHotPostsWithAuthor(@Param("status") GoodsStatus status, Pageable pageable);

    /**
     * 查询用户点赞的帖子ID列表
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 帖子ID列表
     * @since 2025-11-09
     */
    @Query("SELECT pl.postId FROM PostLike pl WHERE pl.userId = :userId AND pl.deleted = false ORDER BY pl.createdAt DESC")
    Page<Long> findLikedPostIdsByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 查询用户收藏的帖子ID列表
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 帖子ID列表
     * @since 2025-11-09
     */
    @Query("SELECT pc.postId FROM PostCollect pc WHERE pc.userId = :userId AND pc.deleted = false ORDER BY pc.createdAt DESC")
    Page<Long> findCollectedPostIdsByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 根据ID列表查询帖子（含作者信息）
     *
     * @param ids 帖子ID列表
     * @return 帖子列表
     * @since 2025-11-09
     */
    @EntityGraph(attributePaths = {"author", "campus"})
    @Query("SELECT p FROM Post p WHERE p.id IN :ids")
    List<Post> findByIdInWithAuthor(@Param("ids") List<Long> ids);
}
