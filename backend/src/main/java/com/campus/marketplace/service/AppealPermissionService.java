package com.campus.marketplace.service;

/**
 * 申诉权限验证服务接口
 * 
 * 提供申诉相关的权限验证功能，包括：
 * - 数据权限控制（用户只能操作自己的申诉）
 * - 操作权限验证（查看、编辑、取消、处理）
 * - 申诉次数限制验证
 * 
 * @author BaSui 😎
 * @date 2025-11-03
 */
public interface AppealPermissionService {

    /**
     * 验证用户是否可以查看申诉
     * 
     * 权限规则：
     * - 用户可以查看自己的申诉
     * - 管理员可以查看所有申诉
     * 
     * @param userId   用户ID
     * @param appealId 申诉ID
     * @return 是否有权限
     */
    boolean canViewAppeal(Long userId, Long appealId);

    /**
     * 验证管理员是否可以查看申诉
     * 
     * @param adminUserId 管理员用户ID
     * @param appealId    申诉ID
     * @return 是否有权限
     */
    boolean canViewAppealAsAdmin(Long adminUserId, Long appealId);

    /**
     * 验证用户是否可以编辑申诉
     * 
     * 权限规则：
     * - 用户只能编辑自己的申诉
     * - 只能编辑待处理状态的申诉
     * 
     * @param userId   用户ID
     * @param appealId 申诉ID
     * @return 是否有权限
     */
    boolean canEditAppeal(Long userId, Long appealId);

    /**
     * 验证用户是否可以取消申诉
     * 
     * 权限规则：
     * - 用户只能取消自己的申诉
     * - 只能取消待处理状态的申诉
     * 
     * @param userId   用户ID
     * @param appealId 申诉ID
     * @return 是否有权限
     */
    boolean canCancelAppeal(Long userId, Long appealId);

    /**
     * 验证管理员是否可以处理申诉
     * 
     * 权限规则：
     * - 只有管理员可以处理申诉
     * - 不能重复处理已完成的申诉
     * 
     * @param adminUserId 管理员用户ID
     * @param appealId    申诉ID
     * @return 是否有权限
     */
    boolean canHandleAppeal(Long adminUserId, Long appealId);

    /**
     * 验证用户是否有申诉权限
     * 
     * 权限规则：
     * - 默认所有用户都有申诉权限
     * - 如果用户被封禁或限制，则无申诉权限
     * 
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasAppealPermission(Long userId);

    /**
     * 验证用户申诉次数是否在限制范围内
     * 
     * @param userId   用户ID
     * @param maxCount 最大次数限制
     * @return 是否在限制范围内
     */
    boolean isWithinAppealLimit(Long userId, int maxCount);

    /**
     * 获取用户当前申诉次数（本月）
     * 
     * @param userId 用户ID
     * @return 申诉次数
     */
    long getUserAppealCount(Long userId);
}
