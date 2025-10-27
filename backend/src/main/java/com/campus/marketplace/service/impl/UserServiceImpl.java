package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.request.UpdatePasswordRequest;
import com.campus.marketplace.common.dto.request.UpdateProfileRequest;
import com.campus.marketplace.common.dto.response.UserProfileResponse;
import com.campus.marketplace.common.entity.Role;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.EncryptUtil;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 获取用户资料
     * 使用缓存提升性能
     */
    @Override
    @Cacheable(value = "user", key = "#userId")
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        log.info("获取用户资料: userId={}", userId);

        User user = userRepository.findByIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return buildUserProfileResponse(user);
    }

    /**
     * 获取当前登录用户资料
     */
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        String username = SecurityUtil.getCurrentUsername();
        log.info("获取当前用户资料: username={}", username);

        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return buildUserProfileResponse(user);
    }

    /**
     * 更新用户资料
     */
    @Override
    @CacheEvict(value = "user", key = "#result")
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(UpdateProfileRequest request) {
        String username = SecurityUtil.getCurrentUsername();
        log.info("更新用户资料: username={}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 更新邮箱
        if (request.email() != null && !request.email().equals(user.getEmail())) {
            // 检查新邮箱是否已被使用
            if (userRepository.existsByEmail(request.email())) {
                throw new BusinessException(ErrorCode.EMAIL_EXISTS);
            }
            user.setEmail(request.email());
            log.info("更新邮箱: userId={}, newEmail={}", user.getId(), request.email());
        }

        // 更新手机号（加密存储）
        if (request.phone() != null) {
            String encryptedPhone = EncryptUtil.aesEncrypt(request.phone());
            user.setPhone(encryptedPhone);
            log.info("更新手机号: userId={}", user.getId());
        }

        // 更新学号
        if (request.studentId() != null) {
            user.setStudentId(request.studentId());
        }

        // 更新头像
        if (request.avatar() != null) {
            user.setAvatar(request.avatar());
        }

        userRepository.save(user);
        log.info("用户资料更新成功: userId={}", user.getId());
    }

    /**
     * 修改密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UpdatePasswordRequest request) {
        String username = SecurityUtil.getCurrentUsername();
        log.info("修改密码: username={}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 验证旧密码
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR, "旧密码不正确");
        }

        // 检查新密码是否与旧密码相同
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "新密码不能与旧密码相同");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        log.info("密码修改成功: userId={}", user.getId());
    }

    /**
     * 构建用户资料响应
     */
    private UserProfileResponse buildUserProfileResponse(User user) {
        // 解密手机号并脱敏
        String maskedPhone = null;
        if (user.getPhone() != null) {
            String decryptedPhone = EncryptUtil.aesDecrypt(user.getPhone());
            maskedPhone = EncryptUtil.maskPhone(decryptedPhone);
        }

        // 邮箱脱敏
        String maskedEmail = EncryptUtil.maskEmail(user.getEmail());

        // 获取角色列表
        var roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(maskedEmail)
                .phone(maskedPhone)
                .studentId(user.getStudentId())
                .avatar(user.getAvatar())
                .status(user.getStatus().name())
                .points(user.getPoints())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
