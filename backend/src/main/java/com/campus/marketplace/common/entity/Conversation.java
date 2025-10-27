package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 会话实体
 * 
 * 存储用户之间的聊天会话
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Entity
@Table(name = "t_conversation", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user1_id", "user2_id"}),
       indexes = {
        @Index(name = "idx_conversation_user1", columnList = "user1_id"),
        @Index(name = "idx_conversation_user2", columnList = "user2_id"),
        @Index(name = "idx_conversation_last_time", columnList = "last_message_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation extends BaseEntity {

    /**
     * 用户1 ID（较小的用户 ID）
     */
    @Column(name = "user1_id", nullable = false)
    private Long user1Id;

    /**
     * 用户1（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", insertable = false, updatable = false)
    private User user1;

    /**
     * 用户2 ID（较大的用户 ID）
     */
    @Column(name = "user2_id", nullable = false)
    private Long user2Id;

    /**
     * 用户2（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", insertable = false, updatable = false)
    private User user2;

    /**
     * 最后一条消息 ID
     */
    @Column(name = "last_message_id")
    private Long lastMessageId;

    /**
     * 最后一条消息时间
     */
    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime;

    /**
     * 更新最后消息信息
     */
    public void updateLastMessage(Long messageId, LocalDateTime messageTime) {
        this.lastMessageId = messageId;
        this.lastMessageTime = messageTime;
    }

    /**
     * 获取对方用户 ID
     */
    public Long getOtherUserId(Long currentUserId) {
        return currentUserId.equals(user1Id) ? user2Id : user1Id;
    }
}
