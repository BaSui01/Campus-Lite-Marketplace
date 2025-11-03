package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.BatchTaskItem;
import com.campus.marketplace.common.enums.BatchItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 批量任务项 Repository
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Repository
public interface BatchTaskItemRepository extends JpaRepository<BatchTaskItem, Long> {

    /**
     * 根据批量任务ID查询所有任务项
     */
    List<BatchTaskItem> findByBatchTaskId(Long batchTaskId);

    /**
     * 根据批量任务ID分页查询
     */
    Page<BatchTaskItem> findByBatchTaskId(Long batchTaskId, Pageable pageable);

    /**
     * 根据批量任务ID和状态查询
     */
    List<BatchTaskItem> findByBatchTaskIdAndStatus(Long batchTaskId, BatchItemStatus status);

    /**
     * 根据分片键查询
     */
    List<BatchTaskItem> findByShardKey(String shardKey);

    /**
     * 根据批量任务ID和分片键查询
     */
    List<BatchTaskItem> findByBatchTaskIdAndShardKey(Long batchTaskId, String shardKey);

    /**
     * 统计批量任务的任务项数量
     */
    long countByBatchTaskId(Long batchTaskId);

    /**
     * 统计批量任务的指定状态任务项数量
     */
    long countByBatchTaskIdAndStatus(Long batchTaskId, BatchItemStatus status);

    /**
     * 查询可重试的失败任务项
     */
    @Query("SELECT bti FROM BatchTaskItem bti WHERE bti.batchTaskId = :batchTaskId " +
           "AND bti.status = 'FAILED' AND bti.retryCount < :maxRetry")
    List<BatchTaskItem> findRetryableItems(@Param("batchTaskId") Long batchTaskId, 
                                          @Param("maxRetry") int maxRetry);

    /**
     * 查询待处理的任务项（按创建时间排序）
     */
    @Query("SELECT bti FROM BatchTaskItem bti WHERE bti.batchTaskId = :batchTaskId " +
           "AND bti.status = 'PENDING' ORDER BY bti.createdAt ASC")
    List<BatchTaskItem> findPendingItems(@Param("batchTaskId") Long batchTaskId);

    /**
     * 批量删除任务项
     */
    void deleteByBatchTaskId(Long batchTaskId);
}
