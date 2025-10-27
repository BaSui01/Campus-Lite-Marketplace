package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.HashSet;
import java.util.Set;

/**
 * 用户实体
 * 
 * 存储用户基本信息和关联的角色
 * 使用 Hibernate 二级缓存提升查询性能
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Entity
@Table(name = "t_user", indexes = {
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class User extends BaseEntity {

    /**
     * 用户名（唯一）
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 密码（BCrypt 加密）
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * 邮箱（唯一）
     */
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * 手机号（AES 加密存储）
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 学号
     */
    @Column(name = "student_id", length = 50)
    private String studentId;

    /**
     * 头像 URL
     */
    @Column(name = "avatar", length = 500)
    private String avatar;

    /**
     * 用户状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * 积分
     */
    @Column(name = "points", nullable = false)
    @Builder.Default
    private Integer points = 0;

    /**
     * 用户角色（多对多关联）
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "t_user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * 添加角色
     */
    public void addRole(Role role) {
        this.roles.add(role);
        role.getUsers().add(this);
    }

    /**
     * 移除角色
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getUsers().remove(this);
    }

    /**
     * 检查用户是否被封禁
     */
    public boolean isBanned() {
        return this.status == UserStatus.BANNED;
    }

    /**
     * 检查用户是否正常
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }
}
