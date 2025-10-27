package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Message;
import com.campus.marketplace.common.enums.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 消息 Repository
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 分页查询会话的消息
     */
    Page<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    /**
     * 查询会话的最新 N 条消息
     */
    List<Message> findTop50ByConversationIdOrderByCreatedAtDesc(Long conversationId);

    /**
     * 统计用户的未读消息数
     */
    long countByReceiverIdAndStatus(Long receiverId, MessageStatus status);

    /**
     * 统计会话的未读消息数
     */
    long countByConversationIdAndReceiverIdAndStatus(Long conversationId, Long receiverId, MessageStatus status);

    /**
     * 批量标记消息为已读
     */
    @Modifying
    @Query("UPDATE Message m SET m.status = :status, m.readAt = CURRENT_TIMESTAMP " +
           "WHERE m.conversationId = :conversationId AND m.receiverId = :receiverId AND m.status = 'UNREAD'")
    int markAsReadByConversation(@Param("conversationId") Long conversationId,
                                  @Param("receiverId") Long receiverId,
                                  @Param("status") MessageStatus status);

    /**
     * 搜索消息内容
     */
    @Query("SELECT m FROM Message m WHERE " +
           "(m.senderId = :userId OR m.receiverId = :userId) AND " +
           "m.content LIKE %:keyword% " +
           "ORDER BY m.createdAt DESC")
    Page<Message> searchMessages(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);
}
