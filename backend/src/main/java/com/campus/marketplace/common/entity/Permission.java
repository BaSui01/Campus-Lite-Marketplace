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
 * 权限实体
 * 
 * 存储权限信息（如 system:user:view）
 * 使用只读缓存（权限信息很少变化）
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Entity
@Table(name = "t_permission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class Permission implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 权限名称（唯一，如 system:user:view）
     */
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    /**
     * 权限描述
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
     * 拥有该权限的角色
     */
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
