package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.DataBackup;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.DataBackupRepository;
import com.campus.marketplace.service.DataBackupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/**
 * 数据备份服务实现
 * 
 * 核心功能：
 * 1. 自动版本管理：为每次备份生成递增版本号
 * 2. 数据完整性：使用SHA-256校验和验证数据完整性
 * 3. 生命周期管理：自动清理过期备份数据
 * 4. 序列化支持：JSON格式存储和恢复实体数据
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataBackupServiceImpl implements DataBackupService {

    private final DataBackupRepository dataBackupRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public DataBackup createBackup(String entityType, Long entityId, String backupData, int expireDays) {
        return createBackup(entityType, entityId, backupData, expireDays, null);
    }

    @Override
    @Transactional
    public DataBackup createBackup(String entityType, Long entityId, String backupData, 
                                   int expireDays, String description) {
        try {
            log.debug("创建数据备份: entityType={}, entityId={}, expireDays={}", 
                     entityType, entityId, expireDays);

            // 获取下一个版本号
            int nextVersion = getNextBackupVersion(entityType, entityId);

            // 计算校验和
            String checksum = calculateChecksum(backupData);

            // 计算备份大小
            long backupSize = backupData.getBytes(StandardCharsets.UTF_8).length;

            // 构建备份实体
            DataBackup backup = DataBackup.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .backupData(backupData)
                    .backupVersion(nextVersion)
                    .expireAt(LocalDateTime.now().plusDays(expireDays))
                    .isActive(true)
                    .checksum(checksum)
                    .backupSize(backupSize)
                    .description(description)
                    .build();

            DataBackup savedBackup = dataBackupRepository.save(backup);
            
            log.info("数据备份创建成功: id={}, entityType={}, entityId={}, version={}, size={} bytes",
                    savedBackup.getId(), entityType, entityId, nextVersion, backupSize);

            return savedBackup;

        } catch (Exception e) {
            log.error("创建数据备份失败: entityType={}, entityId={}", entityType, entityId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建数据备份失败");
        }
    }

    @Override
    public Optional<DataBackup> findLatestBackup(String entityType, Long entityId) {
        return dataBackupRepository.findLatestByEntityTypeAndId(entityType, entityId);
    }

    @Override
    public List<DataBackup> findAllBackups(String entityType, Long entityId) {
        return dataBackupRepository.findByEntityTypeAndEntityIdAndIsActiveTrueOrderByBackupVersionDesc(
                entityType, entityId);
    }

    @Override
    public int getNextBackupVersion(String entityType, Long entityId) {
        Optional<Integer> maxVersion = dataBackupRepository.findMaxVersionByEntityTypeAndId(
                entityType, entityId);
        return maxVersion.map(v -> v + 1).orElse(1);
    }

    @Override
    public boolean validateBackupIntegrity(String backupData) {
        if (backupData == null || backupData.trim().isEmpty()) {
            return false;
        }
        
        // 验证是否为有效的JSON格式
        try {
            objectMapper.readTree(backupData);
            return true;
        } catch (Exception e) {
            log.warn("备份数据格式验证失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public int cleanupExpiredBackups() {
        log.info("开始清理过期备份数据...");
        
        LocalDateTime now = LocalDateTime.now();
        int count = dataBackupRepository.countByExpireAtBeforeAndIsActiveTrue(now);
        
        if (count == 0) {
            log.info("没有需要清理的过期备份");
            return 0;
        }
        
        try {
            int deletedCount = dataBackupRepository.deleteByExpireAtBeforeAndIsActiveTrue(now);
            log.info("清理过期备份完成: 删除了 {} 条记录", deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.error("清理过期备份失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "清理过期备份失败");
        }
    }

    @Override
    @Transactional
    public void markBackupInactive(Long backupId) {
        DataBackup backup = dataBackupRepository.findById(backupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "备份记录不存在"));
        
        backup.markInactive();
        dataBackupRepository.save(backup);
        
        log.info("备份已标记为失效: backupId={}", backupId);
    }

    @Override
    public <T> T restoreFromBackup(DataBackup backup, Class<T> targetClass) {
        try {
            // 验证备份数据完整性
            String calculatedChecksum = calculateChecksum(backup.getBackupData());
            if (!calculatedChecksum.equals(backup.getChecksum())) {
                log.error("备份数据校验失败: backupId={}, expected={}, actual={}",
                         backup.getId(), backup.getChecksum(), calculatedChecksum);
                throw new BusinessException(ErrorCode.DATA_INTEGRITY_ERROR, "备份数据已损坏");
            }

            // 反序列化备份数据
            T restoredEntity = objectMapper.readValue(backup.getBackupData(), targetClass);
            
            log.info("备份数据恢复成功: backupId={}, entityType={}", 
                    backup.getId(), targetClass.getSimpleName());
            
            return restoredEntity;

        } catch (Exception e) {
            log.error("恢复备份数据失败: backupId={}", backup.getId(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "恢复备份数据失败");
        }
    }

    @Override
    public String calculateChecksum(String backupData) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(backupData.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("计算校验和失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "计算校验和失败");
        }
    }

    @Override
    public long countBackupsByEntityType(String entityType) {
        return dataBackupRepository.countByEntityTypeAndIsActiveTrue(entityType);
    }

    @Override
    public Long getTotalBackupSize(String entityType) {
        return dataBackupRepository.sumBackupSizeByEntityType(entityType);
    }

    @Override
    public List<DataBackup> findExpiringBackups() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureTime = now.plusDays(7);
        return dataBackupRepository.findExpiringBackups(now, futureTime);
    }
}
