package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Appeal;
import com.campus.marketplace.common.enums.AppealStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 申诉数据访问接口
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Repository
public interface AppealRepository extends JpaRepository<Appeal, Long> {

    /**
     * 检查用户是否对特定目标存在待处理的申诉
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @return 是否存在
     */
    boolean existsByUserIdAndTargetId(Long userId, Long targetId);

    /**
     * 查询用户的申诉列表，按创建时间倒序
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 申诉列表
     */
    Page<Appeal> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 查询特定状态的申诉列表
     * 
     * @param status 申诉状态
     * @param pageable 分页参数
     * @return 申诉列表
     */
    Page<Appeal> findByStatusOrderByCreatedAtDesc(AppealStatus status, Pageable pageable);

    /**
     * 查询已过期的申诉
     * 
     * @param now 当前时间
     * @return 过期的申诉列表
     */
    @Query("SELECT a FROM Appeal a WHERE a.deadline < :now AND a.status IN :statuses")
    List<Appeal> findExpiredAppeals(@Param("now") LocalDateTime now, 
                                   @Param("statuses") List<AppealStatus> statuses);

    /**
     * 查询需要处理的申诉数量
     * 
     * @param statuses 状态列表
     * @return 数量
     */
    @Query("SELECT COUNT(a) FROM Appeal a WHERE a.status IN :statuses")
    long countByStatusIn(@Param("statuses") List<AppealStatus> statuses);

    /**
     * 查询审核人的申诉统计
     * 
     * @param reviewerId 审核人ID
     * @return 统计信息
     */
    @Query("SELECT a.status, COUNT(a) FROM Appeal a WHERE a.reviewerId = :reviewerId GROUP BY a.status")
    List<Object[]> countByReviewerIdGroupByStatus(@Param("reviewerId") Long reviewerId);

    /**
     * 统计用户在指定时间之后的申诉数量
     * 
     * @param userId    用户ID
     * @param createdAt 创建时间
     * @return 申诉数量
     */
    long countByUserIdAndCreatedAtAfter(Long userId, LocalDateTime createdAt);
}
