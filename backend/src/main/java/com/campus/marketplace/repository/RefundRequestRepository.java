package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.RefundRequest;
import com.campus.marketplace.common.enums.RefundStatus;
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

    /**
     * 用户查询自己的退款列表（分页）
     */
    Page<RefundRequest> findByApplicantId(Long applicantId, Pageable pageable);

    /**
     * 用户查询自己的退款列表（按状态筛选）
     */
    Page<RefundRequest> findByApplicantIdAndStatus(Long applicantId, RefundStatus status, Pageable pageable);

    /**
     * 管理员查询所有退款（按状态筛选）
     */
    Page<RefundRequest> findByStatus(RefundStatus status, Pageable pageable);

    /**
     * 管理员查询所有退款（按关键词搜索：退款单号或订单号）
     */
    Page<RefundRequest> findByRefundNoContainingOrOrderNoContaining(String refundNo, String orderNo, Pageable pageable);
}
