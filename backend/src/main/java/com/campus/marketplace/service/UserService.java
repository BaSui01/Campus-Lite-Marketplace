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
}
