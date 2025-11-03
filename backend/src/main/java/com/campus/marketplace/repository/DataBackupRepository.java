package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.DataBackup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 数据备份 Repository
 * 
 * 提供数据备份的增删改查操作，支持：
 * 1. 根据实体类型和ID查询最新备份
 * 2. 备份版本管理
 * 3. 过期备份清理
 * 4. 备份数据统计
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Repository
public interface DataBackupRepository extends JpaRepository<DataBackup, Long> {

    /**
     * 查询最新的有效备份
     * 
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @return 最新备份
     */
    @Query("SELECT db FROM DataBackup db WHERE db.entityType = :entityType " +
           "AND db.entityId = :entityId AND db.isActive = true " +
           "ORDER BY db.backupVersion DESC LIMIT 1")
    Optional<DataBackup> findLatestByEntityTypeAndId(@Param("entityType") String entityType,
                                                      @Param("entityId") Long entityId);

    /**
     * 查询指定实体的所有有效备份
     */
    List<DataBackup> findByEntityTypeAndEntityIdAndIsActiveTrueOrderByBackupVersionDesc(
            String entityType, Long entityId);

    /**
     * 查询实体的最大备份版本号
     */
    @Query("SELECT MAX(db.backupVersion) FROM DataBackup db " +
           "WHERE db.entityType = :entityType AND db.entityId = :entityId")
    Optional<Integer> findMaxVersionByEntityTypeAndId(@Param("entityType") String entityType,
                                                       @Param("entityId") Long entityId);

    /**
     * 统计过期的有效备份数量
     */
    int countByExpireAtBeforeAndIsActiveTrue(LocalDateTime expireTime);

    /**
     * 删除过期的备份数据
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM DataBackup db WHERE db.expireAt < :expireTime AND db.isActive = true")
    int deleteByExpireAtBeforeAndIsActiveTrue(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 标记过期备份为失效（软删除）
     */
    @Transactional
    @Modifying
    @Query("UPDATE DataBackup db SET db.isActive = false WHERE db.expireAt < :expireTime")
    int markExpiredBackupsInactive(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 查询指定实体类型的备份总数
     */
    long countByEntityTypeAndIsActiveTrue(String entityType);

    /**
     * 查询指定实体类型的总备份大小
     */
    @Query("SELECT COALESCE(SUM(db.backupSize), 0) FROM DataBackup db " +
           "WHERE db.entityType = :entityType AND db.isActive = true")
    Long sumBackupSizeByEntityType(@Param("entityType") String entityType);

    /**
     * 查询即将过期的备份（7天内过期）
     */
    @Query("SELECT db FROM DataBackup db WHERE db.expireAt BETWEEN :now AND :futureTime " +
           "AND db.isActive = true ORDER BY db.expireAt ASC")
    List<DataBackup> findExpiringBackups(@Param("now") LocalDateTime now,
                                         @Param("futureTime") LocalDateTime futureTime);
}
