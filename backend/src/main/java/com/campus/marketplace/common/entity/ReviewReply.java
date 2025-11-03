package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.ReplyType;
import jakarta.persistence.*;
import lombok.*;

/**
 * 评价回复实体
 *
 * Spec #7：卖家/管理员可对买家评价进行回复
 *
 * @author BaSui 😎 - 有问题找卖家，有投诉找管理员！
 * @since 2025-11-03
 */
@Entity
@Table(name = "t_review_reply", indexes = {
        @Index(name = "idx_review_reply_review", columnList = "review_id"),
        @Index(name = "idx_review_reply_replier", columnList = "replier_id"),
        @Index(name = "idx_review_reply_type", columnList = "reply_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewReply extends BaseEntity {

    /**
     * 评价ID（外键）
     */
    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    /**
     * 回复人ID
     * 卖家回复=卖家用户ID，管理员回复=管理员用户ID
     */
    @Column(name = "replier_id", nullable = false)
    private Long replierId;

    /**
     * 回复类型（SELLER_REPLY卖家回复/ADMIN_REPLY管理员回复）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reply_type", nullable = false, length = 20)
    private ReplyType replyType;

    /**
     * 回复内容
     * 最长500字
     */
    @Column(name = "content", nullable = false, length = 500)
    private String content;

    /**
     * 是否已读（买家是否查看过回复）
     * 用于消息通知
     */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    /**
     * 回复目标用户ID
     * 通常是买家ID，用于通知
     */
    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;

    /**
     * 关联到Review实体（可选，用于ORM查询）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", insertable = false, updatable = false)
    private Review review;

    /**
     * 关联到回复人（可选，用于ORM查询）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replier_id", insertable = false, updatable = false)
    private User replier;
}
