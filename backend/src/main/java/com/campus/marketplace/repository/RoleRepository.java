package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 角色 Repository
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * 根据角色名称查询
     */
    Optional<Role> findByName(String name);

    /**
     * 根据角色名称查询（包含权限）
     */
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.name = :name")
    Optional<Role> findByNameWithPermissions(@Param("name") String name);

    /**
     * 检查角色名称是否存在
     */
    boolean existsByName(String name);
}
