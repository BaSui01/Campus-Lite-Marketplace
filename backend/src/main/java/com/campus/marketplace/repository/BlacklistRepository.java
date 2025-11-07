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

    // ==================== 管理员接口新增方法 ====================

    /**
     * 管理员查询：按用户ID和被拉黑用户ID筛选（分页）
     */
    Page<Blacklist> findByUserIdAndBlockedUserId(Long userId, Long blockedUserId, Pageable pageable);

    /**
     * 管理员查询：按用户ID筛选（分页）
     */
    Page<Blacklist> findByUserId(Long userId, Pageable pageable);

    /**
     * 管理员查询：按被拉黑用户ID筛选（分页）
     */
    Page<Blacklist> findByBlockedUserId(Long blockedUserId, Pageable pageable);

    /**
     * 查询指定用户拉黑了哪些人（返回被拉黑用户ID列表）
     */
    @Query("SELECT b.blockedUserId FROM Blacklist b WHERE b.userId = :userId")
    List<Long> findBlockedUserIdsByUserId(@Param("userId") Long userId);

    /**
     * 查询哪些人拉黑了指定用户（返回拉黑者ID列表）
     */
    @Query("SELECT b.userId FROM Blacklist b WHERE b.blockedUserId = :blockedUserId")
    List<Long> findUserIdsByBlockedUserId(@Param("blockedUserId") Long blockedUserId);

    /**
     * 统计活跃拉黑者数量（有拉黑行为的用户数）
     */
    @Query("SELECT COUNT(DISTINCT b.userId) FROM Blacklist b")
    long countDistinctUserId();

    /**
     * 查询被拉黑最多的用户ID
     */
    @Query("SELECT b.blockedUserId FROM Blacklist b GROUP BY b.blockedUserId ORDER BY COUNT(b) DESC LIMIT 1")
    Optional<Long> findMostBlockedUserId();

    /**
     * 统计指定用户被拉黑的次数
     */
    long countByBlockedUserId(Long blockedUserId);
}
