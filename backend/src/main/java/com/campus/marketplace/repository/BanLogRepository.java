package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.BanLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 封禁日志数据访问接口
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Repository
public interface BanLogRepository extends JpaRepository<BanLog, Long> {

    /**
     * 查询需要自动解封的用户
     */
    @Query("SELECT b FROM BanLog b WHERE b.isUnbanned = false " +
           "AND b.unbanTime IS NOT NULL AND b.unbanTime <= :now")
    List<BanLog> findExpiredBans(@Param("now") LocalDateTime now);
}
