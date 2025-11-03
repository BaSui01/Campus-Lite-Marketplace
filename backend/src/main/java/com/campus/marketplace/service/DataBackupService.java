package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.DataBackup;

import java.util.List;
import java.util.Optional;

/**
 * 数据备份服务接口
 * 
 * 功能说明：
 * 1. 创建和管理实体数据备份
 * 2. 支持多版本备份管理
 * 3. 备份数据完整性验证
 * 4. 自动清理过期备份
 * 
 * @author BaSui
 * @date 2025-11-03
 */
public interface DataBackupService {

    /**
     * 创建数据备份
     * 
     * @param entityType 实体类型（如 Goods、Order、User）
     * @param entityId 实体ID
     * @param backupData 备份数据（JSON格式）
     * @param expireDays 过期天数
     * @return 创建的备份记录
     */
    DataBackup createBackup(String entityType, Long entityId, String backupData, int expireDays);

    /**
     * 创建数据备份（带描述）
     * 
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @param backupData 备份数据
     * @param expireDays 过期天数
     * @param description 备份说明
     * @return 创建的备份记录
     */
    DataBackup createBackup(String entityType, Long entityId, String backupData, 
                           int expireDays, String description);

    /**
     * 查询最新的有效备份
     * 
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @return 最新备份（如果存在）
     */
    Optional<DataBackup> findLatestBackup(String entityType, Long entityId);

    /**
     * 查询指定实体的所有有效备份
     * 
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @return 备份列表（按版本号降序）
     */
    List<DataBackup> findAllBackups(String entityType, Long entityId);

    /**
     * 获取下一个备份版本号
     * 
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @return 下一个版本号
     */
    int getNextBackupVersion(String entityType, Long entityId);

    /**
     * 验证备份数据完整性
     * 
     * @param backupData 备份数据
     * @return 是否有效
     */
    boolean validateBackupIntegrity(String backupData);

    /**
     * 清理过期的备份数据
     * 
     * @return 删除的备份数量
     */
    int cleanupExpiredBackups();

    /**
     * 标记备份为失效
     * 
     * @param backupId 备份ID
     */
    void markBackupInactive(Long backupId);

    /**
     * 恢复备份数据（反序列化为实体对象）
     * 
     * @param backup 备份记录
     * @param targetClass 目标类型
     * @param <T> 实体类型
     * @return 恢复的实体对象
     */
    <T> T restoreFromBackup(DataBackup backup, Class<T> targetClass);

    /**
     * 计算备份数据的校验和
     * 
     * @param backupData 备份数据
     * @return 校验和（SHA-256）
     */
    String calculateChecksum(String backupData);

    /**
     * 统计指定实体类型的备份数量
     * 
     * @param entityType 实体类型
     * @return 备份数量
     */
    long countBackupsByEntityType(String entityType);

    /**
     * 统计指定实体类型的总备份大小
     * 
     * @param entityType 实体类型
     * @return 总大小（字节）
     */
    Long getTotalBackupSize(String entityType);

    /**
     * 查询即将过期的备份（7天内）
     * 
     * @return 即将过期的备份列表
     */
    List<DataBackup> findExpiringBackups();
}
