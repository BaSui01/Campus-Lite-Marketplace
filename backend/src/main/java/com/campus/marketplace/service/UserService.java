package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.UpdatePasswordRequest;
import com.campus.marketplace.common.dto.request.UpdateProfileRequest;
import com.campus.marketplace.common.dto.response.UserProfileResponse;

/**
 * 用户服务接口
 * 
 * @author BaSui
 * @date 2025-10-25
 */
public interface UserService {

    /**
     * 获取用户资料
     * 
     * @param userId 用户 ID
     * @return 用户资料
     */
    UserProfileResponse getUserProfile(Long userId);

    /**
     * 获取当前登录用户资料
     * 
     * @return 用户资料
     */
    UserProfileResponse getCurrentUserProfile();

    /**
     * 更新用户资料
     * 
     * @param request 更新请求
     */
    void updateProfile(UpdateProfileRequest request);

    /**
     * 修改密码
     * 
     * @param request 修改密码请求
     */
    void updatePassword(UpdatePasswordRequest request);

    /**
     * 封禁用户
     * 
     * @param request 封禁请求
     */
    void banUser(com.campus.marketplace.common.dto.request.BanUserRequest request);

    /**
     * 解封用户
     * 
     * @param userId 用户ID
     */
    void unbanUser(Long userId);

    /**
     * 自动解封过期用户
     *
     * @return 解封的用户数量
     */
    int autoUnbanExpiredUsers();

    /**
     * 查询封禁记录列表（分页）
     *
     * @param userId 用户ID（可选）
     * @param isUnbanned 是否已解封（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 封禁记录分页结果
     */
    org.springframework.data.domain.Page<com.campus.marketplace.common.dto.response.BanLogResponse> listBannedUsers(
            Long userId, Boolean isUnbanned, int page, int size);

    // ==================== 登录设备管理 ====================

    /**
     * 获取用户的登录设备列表
     *
     * @param userId 用户ID
     * @return 登录设备列表
     */
    java.util.List<com.campus.marketplace.dto.LoginDeviceDTO> getLoginDevices(Long userId);

    /**
     * 记录登录设备
     *
     * @param userId 用户ID
     * @param request HTTP请求（用于提取设备信息）
     */
    void recordLoginDevice(Long userId, jakarta.servlet.http.HttpServletRequest request);

    /**
     * 踢出登录设备
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     */
    void kickDevice(Long userId, Long deviceId);

    // ==================== 邮箱/手机验证 ====================

    /**
     * 发送邮箱验证码
     *
     * @param email 邮箱地址
     */
    void sendEmailCode(String email);

    /**
     * 发送手机验证码
     *
     * @param phone 手机号
     */
    void sendPhoneCode(String phone);

    /**
     * 绑定邮箱
     *
     * @param userId 用户ID
     * @param request 绑定请求
     */
    void bindEmail(Long userId, com.campus.marketplace.dto.BindEmailRequest request);

    /**
     * 绑定手机号
     *
     * @param userId 用户ID
     * @param request 绑定请求
     */
    void bindPhone(Long userId, com.campus.marketplace.dto.BindPhoneRequest request);

    // ==================== 两步验证（2FA）====================

    /**
     * 启用两步验证
     *
     * @param userId 用户ID
     * @return 两步验证响应（包含密钥和二维码URL）
     */
    com.campus.marketplace.dto.TwoFactorResponse enableTwoFactor(Long userId);

    /**
     * 验证并确认两步验证
     *
     * @param userId 用户ID
     * @param request 验证请求
     */
    void verifyTwoFactor(Long userId, com.campus.marketplace.dto.TwoFactorRequest request);

    /**
     * 关闭两步验证
     *
     * @param userId 用户ID
     */
    void disableTwoFactor(Long userId);
}
