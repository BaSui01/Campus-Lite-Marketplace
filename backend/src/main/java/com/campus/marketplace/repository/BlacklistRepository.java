package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Blacklist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 黑名单 Repository
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Repository
public interface BlacklistRepository extends JpaRepository<Blacklist, Long> {

    /**
     * 查询用户是否拉黑了某人
     */
    Optional<Blacklist> findByUserIdAndBlockedUserId(Long userId, Long blockedUserId);

    /**
     * 检查用户是否拉黑了某人
     */
    boolean existsByUserIdAndBlockedUserId(Long userId, Long blockedUserId);

    /**
     * 查询用户的黑名单列表
     */
    Page<Blacklist> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 查询用户的黑名单列表（不分页）
     */
    List<Blacklist> findByUserId(Long userId);

    /**
     * 统计用户的黑名单数量
     */
    long countByUserId(Long userId);

    /**
     * 删除黑名单记录
     */
    void deleteByUserIdAndBlockedUserId(Long userId, Long blockedUserId);

    /**
     * 检查两个用户之间是否存在双向拉黑
     */
    @Query("SELECT COUNT(b) > 0 FROM Blacklist b WHERE " +
           "(b.userId = :userId1 AND b.blockedUserId = :userId2) OR " +
           "(b.userId = :userId2 AND b.blockedUserId = :userId1)")
    boolean existsMutualBlock(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
