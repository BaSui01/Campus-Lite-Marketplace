package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.UserBehaviorLog;
import com.campus.marketplace.common.enums.BehaviorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户行为日志数据访问接口
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Repository
public interface UserBehaviorLogRepository extends JpaRepository<UserBehaviorLog, Long> {

    /**
     * 根据用户ID查询行为日志（最近的优先）
     */
    List<UserBehaviorLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 根据用户ID和行为类型查询
     */
    List<UserBehaviorLog> findByUserIdAndBehaviorType(Long userId, BehaviorType behaviorType);

    /**
     * 根据用户ID和时间范围查询
     */
    @Query("SELECT b FROM UserBehaviorLog b WHERE b.userId = :userId " +
           "AND b.createdAt BETWEEN :startTime AND :endTime " +
           "ORDER BY b.createdAt DESC")
    List<UserBehaviorLog> findByUserIdAndTimeRange(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 根据用户ID、行为类型和时间范围查询
     */
    @Query("SELECT b FROM UserBehaviorLog b WHERE b.userId = :userId " +
           "AND b.behaviorType = :behaviorType " +
           "AND b.createdAt BETWEEN :startTime AND :endTime")
    List<UserBehaviorLog> findByUserIdAndBehaviorTypeAndTimeRange(
            @Param("userId") Long userId,
            @Param("behaviorType") BehaviorType behaviorType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计用户在指定时间范围内的行为次数
     */
    @Query("SELECT COUNT(b) FROM UserBehaviorLog b WHERE b.userId = :userId " +
           "AND b.behaviorType = :behaviorType " +
           "AND b.createdAt BETWEEN :startTime AND :endTime")
    long countByUserIdAndBehaviorTypeAndTimeRange(
            @Param("userId") Long userId,
            @Param("behaviorType") BehaviorType behaviorType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 删除指定时间之前的行为日志（数据归档）
     */
    void deleteByCreatedAtBefore(LocalDateTime threshold);

    /**
     * 查询指定目标的行为日志
     */
    List<UserBehaviorLog> findByTargetTypeAndTargetId(String targetType, Long targetId);

    /**
     * 统计指定目标的浏览次数
     */
    @Query("SELECT COUNT(b) FROM UserBehaviorLog b WHERE b.targetType = :targetType " +
           "AND b.targetId = :targetId AND b.behaviorType = :behaviorType")
    long countByTargetAndBehaviorType(
            @Param("targetType") String targetType,
            @Param("targetId") Long targetId,
            @Param("behaviorType") BehaviorType behaviorType
    );
}
