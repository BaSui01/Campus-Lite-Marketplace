package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.ReviewMedia;
import com.campus.marketplace.common.enums.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 评价媒体数据访问接口
 *
 * @author BaSui 😎 - 图文视频管理，晒单必备！
 * @since 2025-11-03
 */
@Repository
public interface ReviewMediaRepository extends JpaRepository<ReviewMedia, Long> {

    /**
     * 根据评价ID查询所有媒体
     *
     * @param reviewId 评价ID
     * @return 媒体列表（按sortOrder排序）
     */
    List<ReviewMedia> findByReviewIdOrderBySortOrderAsc(Long reviewId);

    /**
     * 根据评价ID和媒体类型查询媒体
     *
     * @param reviewId 评价ID
     * @param mediaType 媒体类型
     * @return 媒体列表
     */
    List<ReviewMedia> findByReviewIdAndMediaType(Long reviewId, MediaType mediaType);

    /**
     * 统计评价的媒体数量
     *
     * @param reviewId 评价ID
     * @return 媒体数量
     */
    long countByReviewId(Long reviewId);

    /**
     * 统计评价的指定类型媒体数量
     *
     * @param reviewId 评价ID
     * @param mediaType 媒体类型
     * @return 媒体数量
     */
    long countByReviewIdAndMediaType(Long reviewId, MediaType mediaType);

    /**
     * 删除评价的所有媒体
     *
     * @param reviewId 评价ID
     */
    void deleteByReviewId(Long reviewId);

    /**
     * 检查评价是否有媒体
     *
     * @param reviewId 评价ID
     * @return 是否存在
     */
    boolean existsByReviewId(Long reviewId);
}
