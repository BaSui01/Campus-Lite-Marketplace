package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.ReviewReply;
import com.campus.marketplace.common.enums.ReplyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 评价回复数据访问接口
 *
 * @author BaSui 😎 - 卖家回复、管理员回复，沟通桥梁！
 * @since 2025-11-03
 */
@Repository
public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Long> {

    /**
     * 根据评价ID查询所有回复
     *
     * @param reviewId 评价ID
     * @return 回复列表（按创建时间升序）
     */
    List<ReviewReply> findByReviewIdOrderByCreatedAtAsc(Long reviewId);

    /**
     * 根据评价ID和回复类型查询回复
     *
     * @param reviewId 评价ID
     * @param replyType 回复类型
     * @return 回复列表
     */
    List<ReviewReply> findByReviewIdAndReplyType(Long reviewId, ReplyType replyType);

    /**
     * 根据回复人ID查询所有回复
     *
     * @param replierId 回复人ID
     * @return 回复列表
     */
    List<ReviewReply> findByReplierId(Long replierId);

    /**
     * 检查评价是否有卖家回复
     *
     * @param reviewId 评价ID
     * @return 是否存在
     */
    boolean existsByReviewIdAndReplyType(Long reviewId, ReplyType replyType);

    /**
     * 统计评价的回复数量
     *
     * @param reviewId 评价ID
     * @return 回复数量
     */
    long countByReviewId(Long reviewId);

    /**
     * 删除评价的所有回复
     *
     * @param reviewId 评价ID
     */
    void deleteByReviewId(Long reviewId);

    /**
     * 查询目标用户未读的回复数量
     *
     * @param targetUserId 目标用户ID
     * @param isRead 是否已读
     * @return 未读回复数量
     */
    long countByTargetUserIdAndIsRead(Long targetUserId, Boolean isRead);

    /**
     * 查询目标用户未读的回复列表
     *
     * @param targetUserId 目标用户ID
     * @param isRead 是否已读
     * @return 回复列表
     */
    List<ReviewReply> findByTargetUserIdAndIsRead(Long targetUserId, Boolean isRead);
}
