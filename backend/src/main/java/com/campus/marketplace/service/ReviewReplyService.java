package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.ReviewReply;
import com.campus.marketplace.common.enums.ReplyType;

import java.util.List;

/**
 * 评价回复服务接口
 *
 * Spec #7：回复功能，支持卖家回复和管理员回复
 *
 * @author BaSui 😎 - 沟通桥梁，让交易更有温度！
 * @since 2025-11-03
 */
public interface ReviewReplyService {

    /**
     * 创建评价回复（卖家或管理员）
     *
     * @param reviewId 评价ID
     * @param replierId 回复人ID
     * @param replyType 回复类型（SELLER_REPLY/ADMIN_REPLY）
     * @param content 回复内容
     * @param targetUserId 目标用户ID（评价人）
     * @return 保存的回复实体
     */
    ReviewReply createReply(Long reviewId, Long replierId, ReplyType replyType, String content, Long targetUserId);

    /**
     * 获取评价的所有回复
     *
     * @param reviewId 评价ID
     * @return 回复列表（按创建时间升序）
     */
    List<ReviewReply> getReviewReplies(Long reviewId);

    /**
     * 获取评价的指定类型回复
     *
     * @param reviewId 评价ID
     * @param replyType 回复类型
     * @return 回复列表
     */
    List<ReviewReply> getReviewRepliesByType(Long reviewId, ReplyType replyType);

    /**
     * 获取回复人的所有回复
     *
     * @param replierId 回复人ID
     * @return 回复列表
     */
    List<ReviewReply> getRepliesByReplier(Long replierId);

    /**
     * 获取用户的未读回复
     *
     * @param userId 用户ID
     * @return 未读回复列表
     */
    List<ReviewReply> getUnreadReplies(Long userId);

    /**
     * 统计用户的未读回复数量
     *
     * @param userId 用户ID
     * @return 未读回复数量
     */
    long countUnreadReplies(Long userId);

    /**
     * 标记回复为已读
     *
     * @param replyId 回复ID
     */
    void markReplyAsRead(Long replyId);

    /**
     * 批量标记用户的所有回复为已读
     *
     * @param userId 用户ID
     */
    void markAllRepliesAsRead(Long userId);

    /**
     * 删除评价回复
     *
     * @param replyId 回复ID
     */
    void deleteReply(Long replyId);

    /**
     * 删除评价的所有回复
     *
     * @param reviewId 评价ID
     */
    void deleteAllRepliesByReviewId(Long reviewId);

    /**
     * 检查评价是否有指定类型的回复
     *
     * @param reviewId 评价ID
     * @param replyType 回复类型
     * @return 是否存在
     */
    boolean hasReply(Long reviewId, ReplyType replyType);

    /**
     * 统计评价的回复数量
     *
     * @param reviewId 评价ID
     * @return 回复数量
     */
    long countReviewReplies(Long reviewId);
}
