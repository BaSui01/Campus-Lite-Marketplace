package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.ReviewTag;
import com.campus.marketplace.common.enums.TagSource;
import com.campus.marketplace.common.enums.TagType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 评价标签数据访问接口
 *
 * @author BaSui 😎 - 标签查询，支持按评价ID/类型/来源筛选！
 * @since 2025-11-03
 */
@Repository
public interface ReviewTagRepository extends JpaRepository<ReviewTag, Long> {

    /**
     * 根据评价ID查询所有标签
     *
     * @param reviewId 评价ID
     * @return 标签列表
     */
    List<ReviewTag> findByReviewId(Long reviewId);

    /**
     * 根据评价ID和标签类型查询标签
     *
     * @param reviewId 评价ID
     * @param tagType 标签类型
     * @return 标签列表
     */
    List<ReviewTag> findByReviewIdAndTagType(Long reviewId, TagType tagType);

    /**
     * 根据评价ID和标签来源查询标签
     *
     * @param reviewId 评价ID
     * @param tagSource 标签来源（系统提取/用户输入）
     * @return 标签列表
     */
    List<ReviewTag> findByReviewIdAndTagSource(Long reviewId, TagSource tagSource);

    /**
     * 删除指定评价的所有标签
     *
     * @param reviewId 评价ID
     */
    void deleteByReviewId(Long reviewId);

    /**
     * 统计指定评价的标签数量
     *
     * @param reviewId 评价ID
     * @return 标签数量
     */
    long countByReviewId(Long reviewId);
}
