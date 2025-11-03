package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.Review;
import com.campus.marketplace.common.entity.ReviewReply;
import com.campus.marketplace.common.enums.ReplyType;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.enums.NotificationType;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.ReviewReplyRepository;
import com.campus.marketplace.repository.ReviewRepository;
import com.campus.marketplace.service.ReviewReplyService;
import com.campus.marketplace.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 评价回复服务实现
 *
 * Spec #7：回复功能，支持卖家回复和管理员回复
 *
 * @author BaSui 😎 - 卖家回复暖人心，管理员回复定纷争！
 * @since 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewReplyServiceImpl implements ReviewReplyService {

    private final ReviewReplyRepository reviewReplyRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationService notificationService;

    /**
     * 回复内容最大长度限制
     */
    private static final int MAX_CONTENT_LENGTH = 500;

    @Override
    @Transactional
    public ReviewReply createReply(Long reviewId, Long replierId, ReplyType replyType, String content, Long targetUserId) {
        // 验证参数
        validateReplyParams(reviewId, replierId, content, targetUserId);

        // 检查评价是否存在
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "评价不存在"));

        // 检查回复类型（卖家回复时验证身份）
        if (replyType == ReplyType.SELLER_REPLY) {
            if (!review.getSellerId().equals(replierId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "只有卖家本人可以回复此评价");
            }
        }

        // 创建回复记录
        ReviewReply reply = ReviewReply.builder()
                .reviewId(reviewId)
                .replierId(replierId)
                .replyType(replyType)
                .content(content)
                .targetUserId(targetUserId)
                .isRead(false)
                .build();

        ReviewReply savedReply = reviewReplyRepository.save(reply);

        // 更新评价的回复数量
        updateReviewReplyCount(reviewId);

        log.info("创建评价回复成功：评价ID={}，回复人={}，类型={}", reviewId, replierId, replyType);

        // 发送通知给目标用户
        sendReplyNotification(savedReply, review);

        return savedReply;
    }

    @Override
    public List<ReviewReply> getReviewReplies(Long reviewId) {
        return reviewReplyRepository.findByReviewIdOrderByCreatedAtAsc(reviewId);
    }

    @Override
    public List<ReviewReply> getReviewRepliesByType(Long reviewId, ReplyType replyType) {
        return reviewReplyRepository.findByReviewIdAndReplyType(reviewId, replyType);
    }

    @Override
    public List<ReviewReply> getRepliesByReplier(Long replierId) {
        return reviewReplyRepository.findByReplierId(replierId);
    }

    @Override
    public List<ReviewReply> getUnreadReplies(Long userId) {
        return reviewReplyRepository.findByTargetUserIdAndIsRead(userId, false);
    }

    @Override
    public long countUnreadReplies(Long userId) {
        return reviewReplyRepository.countByTargetUserIdAndIsRead(userId, false);
    }

    @Override
    @Transactional
    public void markReplyAsRead(Long replyId) {
        ReviewReply reply = reviewReplyRepository.findById(replyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "回复不存在"));

        if (Boolean.TRUE.equals(reply.getIsRead())) {
            log.debug("回复{}已经是已读状态，无需重复标记", replyId);
            return;
        }

        reply.setIsRead(true);
        reviewReplyRepository.save(reply);

        log.info("标记回复为已读：ID={}", replyId);
    }

    @Override
    @Transactional
    public void markAllRepliesAsRead(Long userId) {
        List<ReviewReply> unreadReplies = getUnreadReplies(userId);

        if (unreadReplies.isEmpty()) {
            log.debug("用户{}没有未读回复", userId);
            return;
        }

        unreadReplies.forEach(reply -> reply.setIsRead(true));
        reviewReplyRepository.saveAll(unreadReplies);

        log.info("批量标记用户{}的{}条回复为已读", userId, unreadReplies.size());
    }

    @Override
    @Transactional
    public void deleteReply(Long replyId) {
        ReviewReply reply = reviewReplyRepository.findById(replyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "回复不存在"));

        Long reviewId = reply.getReviewId();
        reviewReplyRepository.delete(reply);

        // 更新评价的回复数量
        updateReviewReplyCount(reviewId);

        log.info("删除回复成功：ID={}", replyId);
    }

    @Override
    @Transactional
    public void deleteAllRepliesByReviewId(Long reviewId) {
        long count = reviewReplyRepository.countByReviewId(reviewId);

        if (count == 0) {
            log.debug("评价{}没有回复，无需删除", reviewId);
            return;
        }

        reviewReplyRepository.deleteByReviewId(reviewId);

        // 更新评价的回复数量
        updateReviewReplyCount(reviewId);

        log.info("删除评价{}的所有回复成功，共{}条", reviewId, count);
    }

    @Override
    public boolean hasReply(Long reviewId, ReplyType replyType) {
        return reviewReplyRepository.existsByReviewIdAndReplyType(reviewId, replyType);
    }

    @Override
    public long countReviewReplies(Long reviewId) {
        return reviewReplyRepository.countByReviewId(reviewId);
    }

    /**
     * 验证回复参数
     */
    private void validateReplyParams(Long reviewId, Long replierId, String content, Long targetUserId) {
        if (reviewId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "评价ID不能为空");
        }

        if (replierId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "回复人ID不能为空");
        }

        if (targetUserId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "目标用户ID不能为空");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "回复内容不能为空");
        }

        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(ErrorCode.PARAM_ERROR,
                    String.format("回复内容不能超过%d字", MAX_CONTENT_LENGTH));
        }
    }

    /**
     * 更新评价的回复数量
     */
    private void updateReviewReplyCount(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "评价不存在"));

        long newCount = countReviewReplies(reviewId);
        review.setReplyCount((int) newCount);
        reviewRepository.save(review);
    }
    /**
     * 发送回复通知
     *
     * @param reply  回复实体
     * @param review 评价实体
     */
    private void sendReplyNotification(ReviewReply reply, Review review) {
        try {
            String title = "您的评价收到了新回复";
            String content = String.format("您对商品的评价收到了%s的回复",
                    reply.getReplyType() == ReplyType.SELLER_REPLY ? "卖家" : "管理员");

            notificationService.sendNotification(
                    reply.getTargetUserId(),
                    NotificationType.REVIEW_REPLIED,
                    title,
                    content,
                    reply.getId(),
                    "REVIEW_REPLY",
                    "/reviews/" + review.getId()
            );

            log.info("发送回复通知成功：目标用户={}，回复ID={}", reply.getTargetUserId(), reply.getId());
        } catch (Exception e) {
            log.error("发送回复通知失败：目标用户={}，回复ID={}，错误={}",
                    reply.getTargetUserId(), reply.getId(), e.getMessage());
            // 通知发送失败不影响回复创建流程
        }
    }

}
