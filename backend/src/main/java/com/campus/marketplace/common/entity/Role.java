package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 角色实体
 * 
 * 存储角色信息和关联的权限
 * 使用只读缓存（角色信息很少变化）
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Entity
@Table(name = "t_role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class Role implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 角色名称（唯一，如 ROLE_STUDENT）
     */
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    /**
     * 角色描述
     */
    @Column(name = "description", length = 200)
    private String description;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 拥有该角色的用户
     */
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<User> users = new HashSet<>();

    /**
     * 角色拥有的权限（多对多关联）
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "t_role_permission",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    /**
     * 添加权限
     */
    public void addPermission(Permission permission) {
        this.permissions.add(permission);
        permission.getRoles().add(this);
    }

    /**
     * 移除权限
     */
    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
        permission.getRoles().remove(this);
    }
}
