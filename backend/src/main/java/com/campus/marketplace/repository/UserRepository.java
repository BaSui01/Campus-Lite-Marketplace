package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户 Repository（BaSui 修复版）
 *
 * 修复内容：
 * - 补充 findByUsernameWithRoles() 方法（任务 6 遗漏 - 已补充！）
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户（可能为空）
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据用户名查询用户（连同角色一起查询 - JOIN FETCH 优化！）
     *
     * 任务 6 遗漏方法 - 已补充！
     * 使用 JOIN FETCH 避免 N+1 查询问题！
     *
     * @param username 用户名
     * @return 用户及其角色（可能为空）
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    /**
     * 根据用户ID查询用户（连同角色和权限一起查询 - JOIN FETCH 优化！）
     *
     * 任务 4 遗漏方法 - 已补充！
     * 使用 JOIN FETCH 避免 N+1 查询问题！
     *
     * @param userId 用户ID
     * @return 用户及其角色和权限（可能为空）
     */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.id = :userId")
    Optional<User> findByIdWithRolesAndPermissions(@Param("userId") Long userId);

    /**
     * 检查用户名是否已存在
     *
     * @param username 用户名
     * @return true-存在，false-不存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否已存在
     *
     * 任务 4 遗漏方法 - 已补充！
     *
     * @param email 邮箱
     * @return true-存在，false-不存在
     */
    boolean existsByEmail(String email);

    /**
     * 根据邮箱查询用户（连同角色一起查询 - JOIN FETCH 优化！）
     *
     * BaSui 修复：添加 JOIN FETCH 避免 N+1 查询问题！📧
     *
     * @param email 邮箱
     * @return 用户及其角色（可能为空）
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户（连同角色一起查询 - JOIN FETCH 优化！）
     *
     * BaSui 新增：支持手机号登录 📱
     * 使用 JOIN FETCH 避免 N+1 查询问题！
     *
     * @param phone 手机号
     * @return 用户及其角色（可能为空）
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.phone = :phone")
    Optional<User> findByPhone(@Param("phone") String phone);

    /**
     * 统计校区下用户数量
     */
    long countByCampusId(Long campusId);

    /**
     * 批量迁移用户校区
     */
    @Modifying
    @Query("UPDATE User u SET u.campusId = :toCampusId WHERE u.campusId = :fromCampusId")
    int updateCampusByCampusId(@Param("fromCampusId") Long fromCampusId,
                               @Param("toCampusId") Long toCampusId);

    /**
     * 查询已删除超过指定时间的用户（用于定时清理）
     *
     * ✅ 真实实现：避免全量遍历，使用查询优化性能
     *
     * @param cutoffTime 截止时间（在此之前删除的用户）
     * @return 符合条件的已删除用户列表
     */
    @Query("SELECT u FROM User u WHERE u.status = com.campus.marketplace.common.enums.UserStatus.DELETED " +
           "AND u.deletedAt IS NOT NULL AND u.deletedAt < :cutoffTime")
    java.util.List<User> findDeletedUsersBefore(@Param("cutoffTime") java.time.LocalDateTime cutoffTime);

    /**
     * 统计绑定指定角色的用户数量。
     */
    long countByRoles_Name(String roleName);

    /**
     * 根据角色名称查询用户列表（用于查询仲裁员等）
     *
     * @param roleName 角色名称（如：ROLE_ADMIN）
     * @return 具有该角色的用户列表
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    java.util.List<User> findByRolesName(@Param("roleName") String roleName);

    /**
     * 统计校区下活跃用户数量（指定时间后登录过的用户）
     *
     * @param campusId 校区ID
     * @param since 截止时间
     * @return 活跃用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.campusId = :campusId AND u.lastLoginTime >= :since")
    long countActiveByCampusIdSince(@Param("campusId") Long campusId,
                                   @Param("since") java.time.LocalDateTime since);

    /**
     * 根据学号查询用户
     */
    Optional<User> findByStudentId(String studentId);

    // 用户列表查询（管理端 - 优化版：JOIN FETCH 避免 N+1 查询）
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.campus LEFT JOIN FETCH u.roles WHERE u.username LIKE %:keyword%")
    org.springframework.data.domain.Page<User> findByUsernameContaining(@Param("keyword") String keyword, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.campus LEFT JOIN FETCH u.roles WHERE u.status = :status")
    org.springframework.data.domain.Page<User> findByStatus(@Param("status") com.campus.marketplace.common.enums.UserStatus status, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.campus LEFT JOIN FETCH u.roles WHERE u.username LIKE %:keyword% AND u.status = :status")
    org.springframework.data.domain.Page<User> findByUsernameContainingAndStatus(@Param("keyword") String keyword, @Param("status") com.campus.marketplace.common.enums.UserStatus status, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.campus LEFT JOIN FETCH u.roles")
    org.springframework.data.domain.Page<User> findAllWithCampusAndRoles(org.springframework.data.domain.Pageable pageable);
}
