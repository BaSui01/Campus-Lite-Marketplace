package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户实体（完整版 - BaSui 修复版）
 *
 * 修复内容：
 * - 补充 email、phone、points 字段（任务 4 & 32 的遗漏！）
 * - 补充 status 字段（任务 29 的遗漏！）
 * - 补充 roles 关联（任务 4 的遗漏！）
 * - 新增 isBanned() 业务方法（检查封禁状态）
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_user")
@EntityListeners(AuditingEntityListener.class)
public class User {

    /**
     * 用户ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 密码（加密存储）
     */
    @Column(nullable = false)
    private String password;

    /**
     * 邮箱（任务 4 遗漏字段 - 已补充！）
     */
    @Column(unique = true, length = 100)
    private String email;

    /**
     * 手机号（任务 4 遗漏字段 - 已补充！）
     */
    @Column(length = 20)
    private String phone;

    /**
     * 头像URL
     */
    @Column(length = 500)
    private String avatar;

    /**
     * 昵称
     */
    @Column(length = 100)
    private String nickname;

    /**
     * 用户积分（任务 32 遗漏字段 - 已补充！）
     */
    @Builder.Default
    @Column(nullable = false)
    private Integer points = 0;

    /**
     * 用户信誉分（任务 17 遗漏字段 - 已补充！）
     * 初始值 100，范围 0-200
     */
    @Builder.Default
    @Column(nullable = false)
    private Integer creditScore = 100;

    /**
     * 校区 ID
     */
    @Column(name = "campus_id")
    private Long campusId;

    /**
     * 校区（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id", insertable = false, updatable = false)
    private Campus campus;

    /**
     * 学号（任务 4 遗漏字段 - 已补充！）
     * 用于校园用户认证
     */
    @Column(unique = true, length = 50)
    private String studentId;

    /**
     * 用户状态（任务 29 遗漏字段 - 已补充！）
     * ACTIVE - 正常
     * BANNED - 被封禁
     * DELETED - 已注销
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * 邮箱是否已验证
     */
    @Builder.Default
    @Column(name = "email_verified", nullable = false, columnDefinition = "boolean default false")
    private Boolean emailVerified = false;

    /**
     * 手机号是否已验证
     */
    @Builder.Default
    @Column(name = "phone_verified", nullable = false, columnDefinition = "boolean default false")
    private Boolean phoneVerified = false;

    /**
     * 两步验证是否启用
     */
    @Builder.Default
    @Column(name = "two_factor_enabled", nullable = false, columnDefinition = "boolean default false")
    private Boolean twoFactorEnabled = false;

    /**
     * 两步验证密钥（TOTP Secret）
     */
    @Column(name = "two_factor_secret", length = 32)
    private String twoFactorSecret;

    /**
     * 用户角色（任务 4 遗漏关联 - 已补充！）
     * Many-to-Many 关联
     */
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "t_user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 最后登录时间
     */
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 注销时间（软删除时间）
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 注销账户（软删除）
     */
    public void softDelete() {
        this.status = UserStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    // ==================== 业务方法 ====================

    /**
     * 检查用户是否被封禁
     *
     * @return true - 已封禁，false - 正常
     */
    public boolean isBanned() {
        return UserStatus.BANNED.equals(this.status);
    }

    /**
     * 检查用户是否处于激活状态
     *
     * @return true - 激活，false - 未激活
     */
    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }

    /**
     * 添加角色（任务 4 遗漏方法 - 已补充！）
     *
     * @param role 角色对象
     */
    public void addRole(Role role) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }

    /**
     * 移除角色
     *
     * @param role 角色对象
     */
    public void removeRole(Role role) {
        if (this.roles != null) {
            this.roles.remove(role);
        }
    }

    /**
     * 检查用户是否为管理员
     *
     * @return true - 是管理员，false - 不是
     */
    public boolean isAdmin() {
        if (this.roles == null || this.roles.isEmpty()) {
            return false;
        }
        return this.roles.stream()
                .anyMatch(role -> "ROLE_ADMIN".equals(role.getName()));
    }
}
