package com.campus.marketplace.common.dto;

import com.campus.marketplace.common.entity.Campus;
import com.campus.marketplace.common.enums.CampusStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 校区缓存 DTO
 *
 * 专门用于 Redis 缓存，避免 Hibernate 懒加载序列化问题。
 * Campus 实体比较简单，没有复杂的关联关系，但为了统一缓存策略，
 * 仍然使用 DTO 模式。
 *
 * 为啥要用 DTO？🤔
 * 1. 统一缓存策略，所有实体都通过 DTO 缓存
 * 2. 解耦 Entity 和缓存层，符合 DDD 设计原则
 * 3. 减少缓存数据量，只存储需要的字段
 * 4. 防止未来添加懒加载字段时出现序列化问题
 *
 * @author BaSui 😎
 * @date 2025-10-31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampusCacheDTO implements Serializable {

    /**
     * 序列化版本号（用于版本兼容性检查）
     *
     * 版本变更规则：
     * - 增加字段：不需要修改版本号（向后兼容）
     * - 删除字段：必须修改版本号（不兼容）
     * - 修改字段类型：必须修改版本号（不兼容）
     * - 重命名字段：必须修改版本号（不兼容）
     *
     * 当前版本：1L (初始版本)
     */
    private static final long serialVersionUID = 1L;

    /**
     * 校区 ID
     */
    private Long id;

    /**
     * 校区编码（唯一）
     */
    private String code;

    /**
     * 校区名称
     */
    private String name;

    /**
     * 校区状态
     */
    private CampusStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 从 Campus 实体转换为 DTO
     *
     * @param campus 校区实体
     * @return 校区缓存 DTO
     */
    public static CampusCacheDTO from(Campus campus) {
        if (campus == null) {
            return null;
        }

        return CampusCacheDTO.builder()
                .id(campus.getId())
                .code(campus.getCode())
                .name(campus.getName())
                .status(campus.getStatus())
                .createdAt(campus.getCreatedAt())
                .updatedAt(campus.getUpdatedAt())
                .build();
    }

    /**
     * 检查校区是否激活
     */
    public boolean isActive() {
        return this.status == CampusStatus.ACTIVE;
    }

    /**
     * 检查校区是否未激活
     */
    public boolean isInactive() {
        return this.status == CampusStatus.INACTIVE;
    }
}
