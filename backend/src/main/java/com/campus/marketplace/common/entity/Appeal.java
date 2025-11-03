package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.AppealStatus;
import com.campus.marketplace.common.enums.AppealTargetType;
import com.campus.marketplace.common.enums.AppealType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 申诉实体 - 支持用户申诉处理流程
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Entity
@Table(name = "t_appeal", indexes = {
        @Index(name = "idx_appeal_user", columnList = "user_id"),
        @Index(name = "idx_appeal_target", columnList = "target_type, target_id"),
        @Index(name = "idx_appeal_status", columnList = "status"),
        @Index(name = "idx_appeal_created_at", columnList = "created_at"),
        @Index(name = "idx_appeal_deadline", columnList = "deadline")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appeal extends BaseEntity {

    /**
     * 申诉用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 目标对象类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 50)
    private AppealTargetType targetType;

    /**
     * 目标对象ID
     */
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    /**
     * 申诉类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "appeal_type", nullable = false, length = 50)
    private AppealType appealType;

    /**
     * 申诉原因
     */
    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    private String reason;

    /**
     * 申诉状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AppealStatus status = AppealStatus.PENDING;

    /**
     * 截止时间
     */
    @Column(name = "deadline", nullable = false)
    private LocalDateTime deadline;

    /**
     * 审核人ID
     */
    @Column(name = "reviewer_id")
    private Long reviewerId;

    /**
     * 审核人用户名
     */
    @Column(name = "reviewer_name", length = 50)
    private String reviewerName;

    /**
     * 审核时间
     */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    /**
     * 审核意见
     */
    @Column(name = "review_comment", columnDefinition = "TEXT")
    private String reviewComment;

    /**
     * 附件列表（JSON格式）
     */
    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments;

    /**
     * 处理结果详情
     */
    @Column(name = "result_details", columnDefinition = "TEXT")
    private String resultDetails;

    

    /**
     * 创建申诉时自动设置截止时间
     */
    @PrePersist
    protected void onCreate() {
        if (deadline == null && targetType != null) {
            this.deadline = calculateDeadline(targetType);
        }
    }

    /**
     * 根据目标类型计算截止时间
     */
    private LocalDateTime calculateDeadline(AppealTargetType targetType) {
        LocalDateTime now = LocalDateTime.now();
        return switch (targetType) {
            case USER_BAN, USER_MUTE -> now.plusDays(7);  // 用户相关申诉7天
            case GOODS_DELETE, GOODS_OFFLINE -> now.plusDays(3);  // 商品相关申诉3天
            case POST_DELETE, REPLY_DELETE -> now.plusDays(5);  // 内容相关申诉5天
            case ORDER_CANCEL -> now.plusDays(2);   // 订单相关申诉2天
            default -> now.plusDays(5);  // 默认5天
        };
    }

    /**
     * 检查申诉是否过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(deadline);
    }

    /**
     * 检查申诉是否可以取消
     */
    public boolean canCancel() {
        return status == AppealStatus.PENDING && !isExpired();
    }

    /**
     * 检查申诉是否可以审核
     */
    public boolean canReview() {
        return status == AppealStatus.PENDING && !isExpired();
    }
}
