package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.RefundRequest;
import com.campus.marketplace.common.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Refund Request Repository
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {

    Optional<RefundRequest> findByRefundNo(String refundNo);

    Optional<RefundRequest> findByOrderNo(String orderNo);

    boolean existsByOrderNoAndStatusIn(String orderNo, java.util.Collection<RefundStatus> statuses);

    @Query("SELECT COUNT(r) > 0 FROM RefundRequest r WHERE r.orderNo = :orderNo AND r.status <> 'FAILED'")
    boolean existsActiveByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 查询需要重试的失败退款（按更新时间早于阈值）
     */
    @Query("SELECT r FROM RefundRequest r WHERE r.status = 'FAILED' AND r.retryCount < :maxRetry AND r.updatedAt < :before ORDER BY r.updatedAt ASC")
    List<RefundRequest> findRetryCandidates(@Param("maxRetry") int maxRetry, @Param("before") LocalDateTime before);

    /**
     * 查询长时间处理中（可能卡住）的退款
     */
    @Query("SELECT r FROM RefundRequest r WHERE r.status = 'PROCESSING' AND r.updatedAt < :before ORDER BY r.updatedAt ASC")
    List<RefundRequest> findStuckProcessing(@Param("before") LocalDateTime before);
}
