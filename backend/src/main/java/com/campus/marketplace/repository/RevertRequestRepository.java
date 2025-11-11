package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.RevertRequest;
import com.campus.marketplace.common.enums.RevertRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 撤销请求 Repository
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Repository
public interface RevertRequestRepository extends JpaRepository<RevertRequest, Long> {

    /**
     * 根据审计日志ID查询撤销请求
     */
    Optional<RevertRequest> findByAuditLogId(Long auditLogId);

    /**
     * 查询用户的撤销请求列表
     */
    Page<RevertRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId, Pageable pageable);

    /**
     * 查询待审批的撤销请求
     */
    Page<RevertRequest> findByStatusOrderByCreatedAtAsc(RevertRequestStatus status, Pageable pageable);

    /**
     * 查询用户特定状态的请求
     */
    List<RevertRequest> findByRequesterIdAndStatus(Long requesterId, RevertRequestStatus status);

    /**
     * 统计用户的请求数量
     */
    long countByRequesterIdAndStatus(Long requesterId, RevertRequestStatus status);

    /**
     * 统计特定状态的撤销请求数量（用于审批人查询待审批数量）
     */
    long countByStatus(RevertRequestStatus status);

    /**
     * 检查审计日志是否已有撤销请求
     */
    boolean existsByAuditLogId(Long auditLogId);

    /**
     * 管理员查询所有撤销请求（按创建时间倒序）
     */
    Page<RevertRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 管理员按状态查询撤销请求（按创建时间倒序）
     */
    Page<RevertRequest> findByStatusOrderByCreatedAtDesc(RevertRequestStatus status, Pageable pageable);

    /**
     * 统计指定状态和时间后的请求数量
     */
    Long countByStatusAndCreatedAtAfter(RevertRequestStatus status, java.time.LocalDateTime createdAt);
}
