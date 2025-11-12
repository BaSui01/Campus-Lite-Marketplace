package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.MessageSearchHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息搜索历史Repository
 *
 * @author BaSui 😎
 * @date 2025-11-07
 */
@Repository
public interface MessageSearchHistoryRepository extends JpaRepository<MessageSearchHistoryEntity, String> {

    /**
     * 查询用户的搜索历史
     */
    Page<MessageSearchHistoryEntity> findByUserIdAndDisputeIdOrderBySearchedAtDesc(
            Long userId, Long disputeId, Pageable pageable);

    /**
     * 查询用户在指定纠纷中的搜索历史
     */
    List<MessageSearchHistoryEntity> findTop10ByUserIdAndDisputeIdOrderBySearchedAtDesc(
            Long userId, Long disputeId);

    /**
     * 查找相似的关键词
     */
    @Query("SELECT h.keyword FROM MessageSearchHistoryEntity h " +
           "WHERE h.userId = :userId AND h.disputeId = :disputeId " +
           "AND h.keyword LIKE %:keyword% " +
           "GROUP BY h.keyword " +
           "ORDER BY COUNT(h.keyword) DESC " +
           "LIMIT 5")
    List<String> findSimilarKeywords(
            @Param("userId") Long userId,
            @Param("disputeId") Long disputeId,
            @Param("keyword") String keyword);

    /**
     * 查询热门关键词
     */
    @Query("SELECT h.keyword as keyword, COUNT(*) as count " +
           "FROM MessageSearchHistoryEntity h " +
           "WHERE h.disputeId = :disputeId " +
           "AND h.searchedAt >= :since " +
           "GROUP BY h.keyword " +
           "ORDER BY COUNT(*) DESC, h.keyword ASC " +
           "LIMIT 10")
    List<Object[]> findPopularKeywords(
            @Param("disputeId") Long disputeId,
            @Param("since") LocalDateTime since);

    /**
     * 统计用户搜索次数
     */
    @Query("SELECT COUNT(*) FROM MessageSearchHistoryEntity h " +
           "WHERE h.userId = :userId AND h.disputeId = :disputeId")
    Long countUserSearches(
            @Param("userId") Long userId,
            @Param("disputeId") Long disputeId);

    /**
     * 统计成功搜索次数
     */
    @Query("SELECT COUNT(*) FROM MessageSearchHistoryEntity h " +
           "WHERE h.userId = :userId AND h.disputeId = :disputeId " +
           "AND h.resultCount > 0")
    Long countSuccessfulSearches(
            @Param("userId") Long userId,
            @Param("disputeId") Long disputeId);

    /**
     * 删除用户在指定纠纷的搜索历史
     */
    void deleteByUserIdAndDisputeId(Long userId, Long disputeId);

    /**
     * 清理旧的搜索历史（保留最近30天）
     */
    @Modifying
    @Query("DELETE FROM MessageSearchHistoryEntity h " +
           "WHERE h.searchedAt < :cutoff")
    void deleteOldHistory(@Param("cutoff") LocalDateTime cutoff);
}