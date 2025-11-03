package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.BatchTask;
import com.campus.marketplace.common.enums.BatchTaskStatus;
import com.campus.marketplace.common.enums.BatchType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 批量任务 Repository
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Repository
public interface BatchTaskRepository extends JpaRepository<BatchTask, Long> {

    /**
     * 根据任务编码查询
     */
    Optional<BatchTask> findByTaskCode(String taskCode);

    /**
     * 根据用户ID和状态分页查询
     */
    Page<BatchTask> findByUserIdAndStatus(Long userId, BatchTaskStatus status, Pageable pageable);

    /**
     * 根据用户ID分页查询
     */
    Page<BatchTask> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据批量类型查询
     */
    List<BatchTask> findByBatchType(BatchType batchType);

    /**
     * 根据状态查询
     */
    List<BatchTask> findByStatus(BatchTaskStatus status);

    /**
     * 查询指定时间范围内的任务
     */
    @Query("SELECT bt FROM BatchTask bt WHERE bt.createdAt BETWEEN :startTime AND :endTime")
    List<BatchTask> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 查询执行中的任务
     */
    @Query("SELECT bt FROM BatchTask bt WHERE bt.status = 'PROCESSING' ORDER BY bt.createdAt ASC")
    List<BatchTask> findProcessingTasks();

    /**
     * 查询待执行的任务
     */
    @Query("SELECT bt FROM BatchTask bt WHERE bt.status = 'PENDING' ORDER BY bt.createdAt ASC")
    List<BatchTask> findPendingTasks();

    /**
     * 统计用户的任务数量
     */
    long countByUserId(Long userId);

    /**
     * 统计指定状态的任务数量
     */
    long countByStatus(BatchTaskStatus status);

    /**
     * 统计用户指定状态的任务数量
     */
    long countByUserIdAndStatus(Long userId, BatchTaskStatus status);

    /**
     * 查询用户最近的任务
     */
    @Query("SELECT bt FROM BatchTask bt WHERE bt.userId = :userId ORDER BY bt.createdAt DESC")
    List<BatchTask> findRecentTasksByUser(@Param("userId") Long userId, Pageable pageable);

    /**
     * 查询超时的任务（执行时间超过预期）
     */
    @Query("SELECT bt FROM BatchTask bt WHERE bt.status = 'PROCESSING' " +
           "AND bt.startTime IS NOT NULL " +
           "AND TIMESTAMPDIFF(SECOND, bt.startTime, CURRENT_TIMESTAMP) > bt.estimatedDuration * 2")
    List<BatchTask> findTimeoutTasks();
}
