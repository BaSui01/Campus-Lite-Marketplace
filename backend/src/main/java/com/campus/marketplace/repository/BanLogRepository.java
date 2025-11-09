package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.BanLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 封禁日志数据访问接口
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Repository
public interface BanLogRepository extends JpaRepository<BanLog, Long> {

    /**
     * 查询需要自动解封的用户
     */
    @Query("SELECT b FROM BanLog b WHERE b.isUnbanned = false " +
           "AND b.unbanTime IS NOT NULL AND b.unbanTime <= :now")
    List<BanLog> findExpiredBans(@Param("now") LocalDateTime now);

    /**
     * 查询所有封禁记录（分页）
     */
    Page<BanLog> findAll(Pageable pageable);

    /**
     * 根据用户ID查询封禁记录（分页）
     */
    Page<BanLog> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据封禁状态查询（分页）
     */
    Page<BanLog> findByIsUnbanned(Boolean isUnbanned, Pageable pageable);

    /**
     * 根据用户ID和封禁状态查询（分页）
     */
    Page<BanLog> findByUserIdAndIsUnbanned(Long userId, Boolean isUnbanned, Pageable pageable);

    /**
     * 查询用户最新的未解封记录
     */
    @Query("SELECT b FROM BanLog b WHERE b.userId = :userId AND b.isUnbanned = false ORDER BY b.createdAt DESC")
    List<BanLog> findTopByUserIdAndIsUnbannedFalseOrderByCreatedAtDesc(@Param("userId") Long userId);
}
