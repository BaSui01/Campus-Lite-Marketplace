package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Notification;
import com.campus.marketplace.common.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 通知数据访问接口
 *
 * @author BaSui 😎
 * @since 2025-10-27
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 分页查询用户的通知列表
     *
     * @param receiverId 接收者ID
     * @param status     通知状态（可选）
     * @param pageable   分页参数
     * @return 通知列表
     */
    @Query("SELECT n FROM Notification n WHERE n.receiverId = :receiverId " +
            "AND (:status IS NULL OR n.status = :status) " +
            "ORDER BY n.createdAt DESC")
    Page<Notification> findByReceiverIdAndStatus(
            @Param("receiverId") Long receiverId,
            @Param("status") NotificationStatus status,
            Pageable pageable
    );

    /**
     * 查询用户未读通知数量
     *
     * @param receiverId 接收者ID
     * @return 未读数量
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.receiverId = :receiverId AND n.status = 'UNREAD'")
    long countUnreadByReceiverId(@Param("receiverId") Long receiverId);

    /**
     * 批量标记为已读
     *
     * @param receiverId 接收者ID
     * @param ids        通知ID列表
     * @return 更新数量
     */
    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = CURRENT_TIMESTAMP " +
            "WHERE n.receiverId = :receiverId AND n.id IN :ids")
    int markAsRead(@Param("receiverId") Long receiverId, @Param("ids") java.util.List<Long> ids);

    /**
     * 全部标记为已读
     *
     * @param receiverId 接收者ID
     * @return 更新数量
     */
    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = CURRENT_TIMESTAMP " +
            "WHERE n.receiverId = :receiverId AND n.status = 'UNREAD'")
    int markAllAsRead(@Param("receiverId") Long receiverId);

    /**
     * 删除通知（软删除）
     *
     * @param receiverId 接收者ID
     * @param ids        通知ID列表
     * @return 更新数量
     */
    @Modifying
    @Query("UPDATE Notification n SET n.status = 'DELETED' WHERE n.receiverId = :receiverId AND n.id IN :ids")
    int deleteByIds(@Param("receiverId") Long receiverId, @Param("ids") java.util.List<Long> ids);
}
