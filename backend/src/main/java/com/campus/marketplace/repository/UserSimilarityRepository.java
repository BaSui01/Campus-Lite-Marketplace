package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.UserSimilarity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户相似度数据访问接口
 *
 * @author BaSui 😎
 * @since 2025-11-04
 */
@Repository
public interface UserSimilarityRepository extends JpaRepository<UserSimilarity, Long> {

    /**
     * 查询与指定用户最相似的Top N用户
     */
    @Query("SELECT us FROM UserSimilarity us WHERE us.userId = :userId " +
           "ORDER BY us.similarityScore DESC")
    List<UserSimilarity> findTopSimilarUsers(@Param("userId") Long userId);

    /**
     * 查询指定用户的相似用户（相似度阈值过滤）
     */
    @Query("SELECT us FROM UserSimilarity us WHERE us.userId = :userId " +
           "AND us.similarityScore >= :threshold " +
           "ORDER BY us.similarityScore DESC")
    List<UserSimilarity> findSimilarUsersAboveThreshold(
            @Param("userId") Long userId,
            @Param("threshold") Double threshold
    );

    /**
     * 删除指定用户的相似度数据
     */
    void deleteByUserId(Long userId);

    /**
     * 查询需要重新计算的相似度记录（距离上次计算超过指定时间）
     */
    @Query("SELECT DISTINCT us.userId FROM UserSimilarity us " +
           "WHERE us.lastCalculatedAt < :threshold " +
           "OR us.lastCalculatedAt IS NULL")
    List<Long> findUsersNeedingRecalculation(@Param("threshold") LocalDateTime threshold);

    /**
     * 判断用户相似度数据是否存在
     */
    boolean existsByUserId(Long userId);

    /**
     * 统计用户相似度记录数量
     */
    long countByUserId(Long userId);
}
