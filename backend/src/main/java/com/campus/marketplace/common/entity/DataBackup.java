package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 数据备份实体 - 支持撤销操作的数据恢复
 * 
 * 功能说明：
 * 1. 存储关键实体的完整备份数据（JSON格式）
 * 2. 支持多版本备份管理
 * 3. 自动过期清理机制
 * 4. 备份数据完整性验证
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Entity
@Table(name = "t_data_backup", indexes = {
        @Index(name = "idx_data_backup_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_data_backup_expire", columnList = "expire_at"),
        @Index(name = "idx_data_backup_active", columnList = "is_active"),
        @Index(name = "idx_data_backup_version", columnList = "entity_type, entity_id, backup_version")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_entity_version", columnNames = {"entity_type", "entity_id", "backup_version"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataBackup extends BaseEntity {

    /**
     * 实体类型（如 Goods、Order、User）
     */
    @NotNull
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    /**
     * 实体ID
     */
    @NotNull
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    /**
     * 备份数据（JSON格式的完整数据快照）
     */
    @Lob
    @Column(name = "backup_data", columnDefinition = "MEDIUMTEXT")
    private String backupData;

    /**
     * 备份版本号（同一实体的多次备份）
     */
    @Column(name = "backup_version", nullable = false)
    private Integer backupVersion;

    /**
     * 过期时间（过期后自动清理）
     */
    @NotNull
    @Column(name = "expire_at", nullable = false)
    private LocalDateTime expireAt;

    /**
     * 是否有效（用于标记过期或已失效的备份）
     */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 数据校验和（用于验证数据完整性）
     */
    @Column(name = "checksum", length = 64)
    private String checksum;

    /**
     * 备份大小（字节）
     */
    @Column(name = "backup_size")
    private Long backupSize;

    /**
     * 备份说明
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 标记备份为失效
     */
    public void markInactive() {
        this.isActive = false;
    }

    /**
     * 检查备份是否已过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expireAt);
    }
}
