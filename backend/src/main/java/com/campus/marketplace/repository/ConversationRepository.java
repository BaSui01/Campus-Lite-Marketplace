package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 会话 Repository
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * 查询两个用户之间的会话
     */
    @Query("SELECT c FROM Conversation c WHERE " +
           "(c.user1Id = :userId1 AND c.user2Id = :userId2) OR " +
           "(c.user1Id = :userId2 AND c.user2Id = :userId1)")
    Optional<Conversation> findByTwoUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * 查询用户的所有会话（按最后消息时间倒序）
     */
    @Query("SELECT c FROM Conversation c WHERE c.user1Id = :userId OR c.user2Id = :userId " +
           "ORDER BY c.lastMessageTime DESC")
    List<Conversation> findByUserId(@Param("userId") Long userId);

    /**
     * 统计用户的会话数量
     */
    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.user1Id = :userId OR c.user2Id = :userId")
    long countByUserId(@Param("userId") Long userId);
}
