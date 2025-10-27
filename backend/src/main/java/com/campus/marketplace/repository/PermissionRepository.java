package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 权限 Repository
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * 根据权限名称查询
     */
    Optional<Permission> findByName(String name);

    /**
     * 检查权限名称是否存在
     */
    boolean existsByName(String name);
}
