package com.campus.marketplace.common.dto;

import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户缓存 DTO
 *
 * 专门用于 Redis 缓存，避免 Hibernate 懒加载序列化问题。
 * User 实体包含懒加载的 campus 和 roles 字段，必须使用 DTO 避免序列化异常。
 *
 * 为啥要用 DTO？🤔
 * 1. 避免 Hibernate 懒加载序列化问题（campus、roles 都是懒加载）
 * 2. 解耦 Entity 和缓存层，符合 DDD 设计原则
 * 3. 减少缓存数据量，只存储需要的字段（不缓存密码等敏感信息）
 * 4. 保护用户隐私，不缓存敏感字段
 *
 * @author BaSui 😎
 * @date 2025-10-31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCacheDTO implements Serializable {

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
     * 用户 ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像 URL
     */
    private String avatar;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 用户积分
     */
    private Integer points;

    /**
     * 用户信誉分
     */
    private Integer creditScore;

    /**
     * 校区 ID（避免懒加载 Campus 对象）
     */
    private Long campusId;

    /**
     * 校区名称（冗余字段，方便展示）
     */
    private String campusName;

    /**
     * 学号
     */
    private String studentId;

    /**
     * 用户状态
     */
    private UserStatus status;

    /**
     * 角色名称列表（避免懒加载 Role 对象）
     */
    private Set<String> roleNames;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 注销时间
     */
    private LocalDateTime deletedAt;

    /**
     * 从 User 实体转换为 DTO
     *
     * ⚠️ 注意：
     * - 不缓存密码字段（安全考虑）
     * - campus 和 roles 字段如果未初始化，则不会访问（避免懒加载异常）
     *
     * @param user 用户实体
     * @return 用户缓存 DTO
     */
    public static UserCacheDTO from(User user) {
        if (user == null) {
            return null;
        }

        return UserCacheDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .nickname(user.getNickname())
                .points(user.getPoints())
                .creditScore(user.getCreditScore())
                .campusId(user.getCampusId())
                // ⚠️ 安全访问懒加载字段：如果 campus 未初始化，则为 null
                .campusName(user.getCampus() != null ? user.getCampus().getName() : null)
                .studentId(user.getStudentId())
                .status(user.getStatus())
                // ⚠️ 安全访问懒加载字段：如果 roles 未初始化或为空，则为空 Set
                .roleNames(user.getRoles() != null && !user.getRoles().isEmpty()
                        ? user.getRoles().stream()
                                .map(role -> role.getName())
                                .collect(Collectors.toSet())
                        : Set.of())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deletedAt(user.getDeletedAt())
                .build();
    }

    /**
     * 检查用户是否被封禁
     */
    public boolean isBanned() {
        return UserStatus.BANNED == this.status;
    }

    /**
     * 检查用户是否处于激活状态
     */
    public boolean isActive() {
        return UserStatus.ACTIVE == this.status;
    }

    /**
     * 检查用户是否已注销
     */
    public boolean isDeleted() {
        return UserStatus.DELETED == this.status;
    }

    /**
     * 检查用户是否为管理员
     */
    public boolean isAdmin() {
        return roleNames != null && roleNames.contains("ROLE_ADMIN");
    }
}
