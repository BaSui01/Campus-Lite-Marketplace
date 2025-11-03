package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.UserPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户画像数据访问接口
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Repository
public interface UserPersonaRepository extends JpaRepository<UserPersona, Long> {

    /**
     * 根据用户ID查询画像
     */
    Optional<UserPersona> findByUserId(Long userId);

    /**
     * 判断用户画像是否存在
     */
    boolean existsByUserId(Long userId);

    /**
     * 根据用户分群查询
     */
    List<UserPersona> findByUserSegment(String userSegment);

    /**
     * 查询需要更新的画像（距离上次更新超过指定时间）
     */
    @Query("SELECT p FROM UserPersona p WHERE p.lastUpdatedTime < :threshold " +
           "OR p.lastUpdatedTime IS NULL")
    List<UserPersona> findPendingUpdate(@Param("threshold") LocalDateTime threshold);

    /**
     * 查询校区偏好为指定校区的用户画像
     */
    List<UserPersona> findByCampusPreference(String campusPreference);

    /**
     * 统计各用户分群的数量
     */
    @Query("SELECT p.userSegment, COUNT(p) FROM UserPersona p GROUP BY p.userSegment")
    List<Object[]> countByUserSegment();

    /**
     * 删除用户画像
     */
    void deleteByUserId(Long userId);
}
