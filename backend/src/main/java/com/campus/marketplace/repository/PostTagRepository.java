package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 帖子-标签关联关系仓库
 *
 * 负责帖子与标签绑定关系的增删查与批量操作
 * 📋 参考：GoodsTagRepository（商品-标签关联）
 *
 * @author BaSui 😎
 * @date 2025-11-08
 */
@Repository
public interface PostTagRepository extends JpaRepository<PostTag, Long> {

    /**
     * 根据帖子ID查询所有标签关联
     * @param postId 帖子ID
     * @return 标签关联列表
     */
    List<PostTag> findByPostId(Long postId);

    /**
     * 根据多个帖子ID查询所有标签关联（批量查询）
     * @param postIds 帖子ID集合
     * @return 标签关联列表
     */
    List<PostTag> findByPostIdIn(Collection<Long> postIds);

    /**
     * 删除帖子的指定标签之外的所有关联
     * @param postId 帖子ID
     * @param tagIds 保留的标签ID列表
     */
    @Modifying
    @Query("DELETE FROM PostTag pt WHERE pt.postId = :postId AND pt.tagId NOT IN (:tagIds)")
    void deleteByPostIdAndTagIdNotIn(@Param("postId") Long postId, @Param("tagIds") Collection<Long> tagIds);

    /**
     * 删除帖子的所有标签关联
     * @param postId 帖子ID
     */
    @Modifying
    void deleteByPostId(Long postId);

    /**
     * 查询帖子是否已关联指定标签
     * @param postId 帖子ID
     * @param tagId 标签ID
     * @return 关联记录（如果存在）
     */
    Optional<PostTag> findByPostIdAndTagId(Long postId, Long tagId);

    /**
     * 根据标签ID查询所有关联的帖子
     * @param tagId 标签ID
     * @return 关联列表
     */
    List<PostTag> findByTagId(Long tagId);

    /**
     * 统计使用该标签的帖子数量
     * @param tagId 标签ID
     * @return 帖子数量
     */
    long countByTagId(Long tagId);

    /**
     * 根据多个标签ID查询包含所有这些标签的帖子ID列表
     * @param tagIds 标签ID列表
     * @param tagCount 标签数量（用于验证帖子包含所有标签）
     * @return 帖子ID列表
     */
    @Query("""
        SELECT pt.postId
        FROM PostTag pt
        WHERE pt.tagId IN :tagIds
        GROUP BY pt.postId
        HAVING COUNT(DISTINCT pt.tagId) = :tagCount
        """)
    List<Long> findPostIdsByAllTagIds(
            @Param("tagIds") Collection<Long> tagIds,
            @Param("tagCount") long tagCount
    );
}
