package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.request.UpdatePasswordRequest;
import com.campus.marketplace.common.dto.request.UpdateProfileRequest;
import com.campus.marketplace.common.dto.response.UserProfileResponse;
import com.campus.marketplace.common.entity.Role;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.EncryptUtil;
import com.campus.marketplace.common.utils.IpLocationUtil;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.UserService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private final com.campus.marketplace.repository.BanLogRepository banLogRepository;
    private final EncryptUtil encryptUtil;
    private final com.campus.marketplace.repository.LoginDeviceRepository loginDeviceRepository;
    private final com.campus.marketplace.service.VerificationCodeService verificationCodeService;
    private final IpLocationUtil ipLocationUtil;

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

        // 更新昵称
        if (request.nickname() != null) {
            user.setNickname(request.nickname());
        }

        // 更新个人简介
        if (request.bio() != null) {
            user.setBio(request.bio());
        }

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
            String encryptedPhone = encryptUtil.aesEncrypt(request.phone());
            user.setPhone(encryptedPhone);
            log.info("更新手机号: userId={}", user.getId());
        }

        // 更新学号
        if (request.studentId() != null) {
            user.setStudentId(request.studentId());
        }

        // 更新头像
        if (request.avatar() != null) {
            // ✅ 验证头像 URL 是否合法（防止恶意 URL 注入）
            if (!isValidAvatarUrl(request.avatar())) {
                throw new BusinessException(ErrorCode.INVALID_PARAM, "头像URL不合法，只允许本站上传的图片");
            }
            user.setAvatar(request.avatar());
        }

        userRepository.save(user);
        log.info("用户资料更新成功: userId={}", user.getId());
    }

    /**
     * 验证头像 URL 是否合法
     * <p>
     * 只允许以下来源的头像：
     * - 本站上传的图片（/uploads/）
     * - 本地开发环境（localhost）
     * - 可信 CDN 域名
     * </p>
     *
     * @param avatarUrl 头像 URL
     * @return 是否合法
     */
    private boolean isValidAvatarUrl(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            return false;
        }

        // 允许的域名和路径前缀
        String[] allowedPatterns = {
            "/uploads/",                // 本地上传路径
            "localhost",                // 本地开发
            "127.0.0.1",                // 本地开发
            "cdn.campus.com",           // 生产 CDN（根据实际情况修改）
        };

        for (String pattern : allowedPatterns) {
            if (avatarUrl.contains(pattern)) {
                log.debug("头像 URL 验证通过: {}", avatarUrl);
                return true;
            }
        }

        log.warn("⚠️ 非法头像 URL 被拒绝: {}", avatarUrl);
        return false;
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
            String decryptedPhone = encryptUtil.aesDecrypt(user.getPhone());
            maskedPhone = encryptUtil.maskPhone(decryptedPhone);
        }

        // 邮箱脱敏
        String maskedEmail = encryptUtil.maskEmail(user.getEmail());

        // 获取角色列表
        var roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        // 获取校区名称
        String campusName = null;
        if (user.getCampus() != null) {
            campusName = user.getCampus().getName();
        }

        // 获取封禁原因
        String banReason = null;
        if (user.getStatus() == com.campus.marketplace.common.enums.UserStatus.BANNED) {
            List<com.campus.marketplace.common.entity.BanLog> banLogs = banLogRepository
                .findTopByUserIdAndIsUnbannedFalseOrderByCreatedAtDesc(user.getId());
            if (!banLogs.isEmpty()) {
                banReason = banLogs.get(0).getReason();
            }
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(maskedEmail)
                .phone(maskedPhone)
                .studentId(user.getStudentId())
                .avatar(user.getAvatar())
                .status(user.getStatus().name())
                .points(user.getPoints())
                .campusId(user.getCampusId())
                .campusName(campusName)
                .banReason(banReason)
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * 封禁用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void banUser(com.campus.marketplace.common.dto.request.BanUserRequest request) {
        log.info("封禁用户: userId={}, days={}, reason={}", request.userId(), request.days(), request.reason());

        String adminUsername = SecurityUtil.getCurrentUsername();
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.setStatus(com.campus.marketplace.common.enums.UserStatus.BANNED);
        userRepository.save(user);

        java.time.LocalDateTime unbanTime = null;
        if (request.days() > 0) {
            unbanTime = java.time.LocalDateTime.now().plusDays(request.days());
        }

        com.campus.marketplace.common.entity.BanLog banLog = com.campus.marketplace.common.entity.BanLog.builder()
                .userId(user.getId())
                .adminId(admin.getId())
                .reason(request.reason())
                .days(request.days())
                .unbanTime(unbanTime)
                .isUnbanned(false)
                .build();
        banLogRepository.save(banLog);

        log.info("用户封禁成功: userId={}, adminId={}, days={}, unbanTime={}",
                user.getId(), admin.getId(), request.days(), unbanTime);
    }

    /**
     * 解封用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbanUser(Long userId) {
        log.info("解封用户: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.setStatus(com.campus.marketplace.common.enums.UserStatus.ACTIVE);
        userRepository.save(user);

        log.info("用户解封成功: userId={}", userId);
    }

    /**
     * 自动解封过期用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int autoUnbanExpiredUsers() {
        log.info("开始自动解封过期用户");

        java.util.List<com.campus.marketplace.common.entity.BanLog> expiredBans =
                banLogRepository.findExpiredBans(java.time.LocalDateTime.now());

        int count = 0;
        for (com.campus.marketplace.common.entity.BanLog banLog : expiredBans) {
            User user = userRepository.findById(banLog.getUserId()).orElse(null);
            if (user != null && user.getStatus() == com.campus.marketplace.common.enums.UserStatus.BANNED) {
                user.setStatus(com.campus.marketplace.common.enums.UserStatus.ACTIVE);
                userRepository.save(user);

                banLog.setIsUnbanned(true);
                banLogRepository.save(banLog);

                count++;
                log.info("自动解封用户: userId={}", user.getId());
            }
        }

        log.info("自动解封完成: count={}", count);
        return count;
    }

    /**
     * 查询封禁记录列表（分页）
     */
    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<com.campus.marketplace.common.dto.response.BanLogResponse> listBannedUsers(
            Long userId, Boolean isUnbanned, int page, int size) {
        log.info("查询封禁记录列表: userId={}, isUnbanned={}, page={}, size={}", userId, isUnbanned, page, size);

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size,
                        org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));

        org.springframework.data.domain.Page<com.campus.marketplace.common.entity.BanLog> banLogPage;

        // 根据条件查询
        if (userId != null && isUnbanned != null) {
            banLogPage = banLogRepository.findByUserIdAndIsUnbanned(userId, isUnbanned, pageable);
        } else if (userId != null) {
            banLogPage = banLogRepository.findByUserId(userId, pageable);
        } else if (isUnbanned != null) {
            banLogPage = banLogRepository.findByIsUnbanned(isUnbanned, pageable);
        } else {
            banLogPage = banLogRepository.findAll(pageable);
        }

        // 转换为 BanLogResponse（填充用户名）
        return banLogPage.map(banLog -> {
            String username = userRepository.findById(banLog.getUserId())
                    .map(User::getUsername)
                    .orElse("未知用户");

            String adminUsername = userRepository.findById(banLog.getAdminId())
                    .map(User::getUsername)
                    .orElse("未知管理员");

            return com.campus.marketplace.common.dto.response.BanLogResponse.from(banLog, username, adminUsername);
        });
    }

    // ==================== 登录设备管理 ====================

    @Override
    public java.util.List<com.campus.marketplace.dto.LoginDeviceDTO> getLoginDevices(Long userId) {
        log.info("获取用户登录设备列表: userId={}", userId);

        // 权限检查：只能查看自己的设备
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            throw new com.campus.marketplace.common.exception.BusinessException(
                    com.campus.marketplace.common.exception.ErrorCode.PERMISSION_DENIED,
                    "无权查看其他用户的登录设备");
        }

        java.util.List<com.campus.marketplace.entity.LoginDevice> devices =
                loginDeviceRepository.findByUserIdOrderByLastActiveAtDesc(userId);

        return devices.stream()
                .map(device -> com.campus.marketplace.dto.LoginDeviceDTO.builder()
                        .id(device.getId())
                        .deviceName(device.getDeviceName())
                        .deviceType(device.getDeviceType())
                        .os(device.getOs())
                        .browser(device.getBrowser())
                        .ip(device.getIp())
                        .location(device.getLocation())
                        .lastActiveAt(device.getLastActiveAt())
                        .isCurrent(device.getIsCurrent())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void recordLoginDevice(Long userId, jakarta.servlet.http.HttpServletRequest request) {
        log.info("记录登录设备: userId={}", userId);

        // 解析设备信息
        String userAgent = request.getHeader("User-Agent");
        String ip = getClientIp(request);

        // 简单解析设备类型和浏览器（生产环境建议使用 user-agent-utils 库）
        String deviceType = parseDeviceType(userAgent);
        String os = parseOS(userAgent);
        String browser = parseBrowser(userAgent);
        String deviceName = os + " - " + browser;

        // 查找或创建设备记录
        java.util.List<com.campus.marketplace.entity.LoginDevice> existingDevices =
                loginDeviceRepository.findByUserIdOrderByLastActiveAtDesc(userId);

        com.campus.marketplace.entity.LoginDevice device = existingDevices.stream()
                .filter(d -> d.getIp().equals(ip) && d.getUserAgent().equals(userAgent))
                .findFirst()
                .orElse(null);

        if (device == null) {
            // 创建新设备记录
            device = com.campus.marketplace.entity.LoginDevice.builder()
                    .userId(userId)
                    .deviceName(deviceName)
                    .deviceType(deviceType)
                    .os(os)
                    .browser(browser)
                    .ip(ip)
                    .location(ipLocationUtil.getLocation(ip)) // ✅ 集成 IP 地理位置服务
                    .userAgent(userAgent)
                    .lastActiveAt(java.time.LocalDateTime.now())
                    .isCurrent(true)
                    .build();
        } else {
            // 更新现有设备
            device.setLastActiveAt(java.time.LocalDateTime.now());
            device.setIsCurrent(true);
        }

        // 将其他设备设置为非当前设备
        loginDeviceRepository.setAllDevicesNotCurrent(userId);

        // 保存设备记录
        loginDeviceRepository.save(device);

        log.info("登录设备记录成功: deviceId={}", device.getId());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void kickDevice(Long userId, Long deviceId) {
        log.info("踢出登录设备: userId={}, deviceId={}", userId, deviceId);

        // 权限检查：只能踢出自己的设备
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            throw new com.campus.marketplace.common.exception.BusinessException(
                    com.campus.marketplace.common.exception.ErrorCode.PERMISSION_DENIED,
                    "无权踢出其他用户的设备");
        }

        // 查找设备
        com.campus.marketplace.entity.LoginDevice device =
                loginDeviceRepository.findByIdAndUserId(deviceId, userId)
                        .orElseThrow(() -> new com.campus.marketplace.common.exception.BusinessException(
                                com.campus.marketplace.common.exception.ErrorCode.NOT_FOUND,
                                "设备不存在"));

        // 不能踢出当前设备
        if (device.getIsCurrent()) {
            throw new com.campus.marketplace.common.exception.BusinessException(
                    com.campus.marketplace.common.exception.ErrorCode.INVALID_OPERATION,
                    "不能踢出当前设备");
        }

        // 删除设备记录
        loginDeviceRepository.deleteByIdAndUserId(deviceId, userId);

        log.info("设备已踢出: deviceId={}", deviceId);
    }

    // ==================== 邮箱/手机验证 ====================

    @Override
    public void sendEmailCode(String email) {
        log.info("发送邮箱验证码: email={}", email);

        // 使用现有的 VerificationCodeService 发送邮箱验证码
        // purpose 使用 "BIND" 表示绑定邮箱场景
        verificationCodeService.sendEmailCode(email, "BIND");

        log.info("邮箱验证码已发送: email={}", email);
    }

    @Override
    public void sendPhoneCode(String phone) {
        log.info("发送手机验证码: phone={}", phone);

        // 使用现有的 VerificationCodeService 发送短信验证码
        // purpose 使用 "BIND" 表示绑定手机场景
        verificationCodeService.sendSmsCode(phone, "BIND");

        log.info("手机验证码已发送: phone={}", phone);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void bindEmail(Long userId, com.campus.marketplace.dto.BindEmailRequest request) {
        log.info("绑定邮箱: userId={}, email={}", userId, request.getEmail());

        // 权限检查
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            throw new com.campus.marketplace.common.exception.BusinessException(
                    com.campus.marketplace.common.exception.ErrorCode.PERMISSION_DENIED,
                    "无权操作其他用户的邮箱");
        }

        // 验证验证码（使用 VerificationCodeService）
        if (!verificationCodeService.validateEmailCode(request.getEmail(), "BIND", request.getCode())) {
            throw new com.campus.marketplace.common.exception.BusinessException(
                    com.campus.marketplace.common.exception.ErrorCode.INVALID_OPERATION,
                    "验证码错误或已过期");
        }

        // 检查邮箱是否已被使用
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new com.campus.marketplace.common.exception.BusinessException(
                    com.campus.marketplace.common.exception.ErrorCode.INVALID_OPERATION,
                    "该邮箱已被其他用户使用");
        }

        // 更新用户邮箱
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.campus.marketplace.common.exception.BusinessException(
                        com.campus.marketplace.common.exception.ErrorCode.USER_NOT_FOUND,
                        "用户不存在"));
        user.setEmail(request.getEmail());
        user.setEmailVerified(true);
        userRepository.save(user);

        log.info("邮箱绑定成功: userId={}, email={}", userId, request.getEmail());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void bindPhone(Long userId, com.campus.marketplace.dto.BindPhoneRequest request) {
        log.info("绑定手机号: userId={}, phone={}", userId, request.getPhone());

        // 权限检查
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            throw new com.campus.marketplace.common.exception.BusinessException(
                    com.campus.marketplace.common.exception.ErrorCode.PERMISSION_DENIED,
                    "无权操作其他用户的手机号");
        }

        // 验证验证码（使用 VerificationCodeService）
        if (!verificationCodeService.validateSmsCode(request.getPhone(), "BIND", request.getCode())) {
            throw new com.campus.marketplace.common.exception.BusinessException(
                    com.campus.marketplace.common.exception.ErrorCode.INVALID_OPERATION,
                    "验证码错误或已过期");
        }

        // 检查手机号是否已被使用
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new com.campus.marketplace.common.exception.BusinessException(
                    com.campus.marketplace.common.exception.ErrorCode.INVALID_OPERATION,
                    "该手机号已被其他用户使用");
        }

        // 更新用户手机号
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.campus.marketplace.common.exception.BusinessException(
                        com.campus.marketplace.common.exception.ErrorCode.USER_NOT_FOUND,
                        "用户不存在"));
        user.setPhone(request.getPhone());
        user.setPhoneVerified(true);
        userRepository.save(user);

        log.info("手机号绑定成功: userId={}, phone={}", userId, request.getPhone());
    }

    // ==================== 两步验证（2FA）====================

    @Override
    @org.springframework.transaction.annotation.Transactional
    public com.campus.marketplace.dto.TwoFactorResponse enableTwoFactor(Long userId) {
        log.info("启用两步验证: userId={}", userId);

        // 权限检查
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            throw new com.campus.marketplace.common.exception.BusinessException(
                    com.campus.marketplace.common.exception.ErrorCode.PERMISSION_DENIED,
                    "无权操作其他用户的两步验证");
        }

        // 生成 TOTP 密钥
        String secret = generateTOTPSecret();

        // 保存密钥到用户记录
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.campus.marketplace.common.exception.BusinessException(
                        com.campus.marketplace.common.exception.ErrorCode.USER_NOT_FOUND,
                        "用户不存在"));
        user.setTwoFactorSecret(secret);
        userRepository.save(user);

        // 生成二维码 URL
        String qrCodeUrl = String.format(
                "otpauth://totp/CampusMarketplace:%s?secret=%s&issuer=CampusMarketplace",
                user.getUsername(), secret
        );

        log.info("两步验证密钥已生成: userId={}", userId);

        return com.campus.marketplace.dto.TwoFactorResponse.builder()
                .secret(secret)
                .qrCodeUrl(qrCodeUrl)
                .build();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void verifyTwoFactor(Long userId, com.campus.marketplace.dto.TwoFactorRequest request) {
        log.info("验证两步验证: userId={}", userId);

        // 权限检查
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            throw new com.campus.marketplace.common.exception.BusinessException(
                    com.campus.marketplace.common.exception.ErrorCode.PERMISSION_DENIED,
                    "无权操作其他用户的两步验证");
        }

        // 获取用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.campus.marketplace.common.exception.BusinessException(
                        com.campus.marketplace.common.exception.ErrorCode.USER_NOT_FOUND,
                        "用户不存在"));

        // 验证 TOTP 码
        if (!verifyTOTPCode(user.getTwoFactorSecret(), request.getCode())) {
            throw new com.campus.marketplace.common.exception.BusinessException(
                    com.campus.marketplace.common.exception.ErrorCode.INVALID_OPERATION,
                    "验证码错误");
        }

        // 启用两步验证
        user.setTwoFactorEnabled(true);
        userRepository.save(user);

        log.info("两步验证已启用: userId={}", userId);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void disableTwoFactor(Long userId) {
        log.info("关闭两步验证: userId={}", userId);

        // 权限检查
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            throw new com.campus.marketplace.common.exception.BusinessException(
                    com.campus.marketplace.common.exception.ErrorCode.PERMISSION_DENIED,
                    "无权操作其他用户的两步验证");
        }

        // 获取用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.campus.marketplace.common.exception.BusinessException(
                        com.campus.marketplace.common.exception.ErrorCode.USER_NOT_FOUND,
                        "用户不存在"));

        // 关闭两步验证
        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);

        log.info("两步验证已关闭: userId={}", userId);
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new com.campus.marketplace.common.exception.BusinessException(
                    com.campus.marketplace.common.exception.ErrorCode.UNAUTHORIZED,
                    "未登录");
        }
        return Long.parseLong(authentication.getName());
    }

    /**
     * 获取客户端 IP
     */
    private String getClientIp(jakarta.servlet.http.HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 解析设备类型
     */
    private String parseDeviceType(String userAgent) {
        if (userAgent == null) return "unknown";
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
            return "mobile";
        } else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return "tablet";
        }
        return "desktop";
    }

    /**
     * 解析操作系统
     */
    private String parseOS(String userAgent) {
        if (userAgent == null) return "Unknown";
        if (userAgent.contains("Windows")) return "Windows";
        if (userAgent.contains("Mac OS")) return "macOS";
        if (userAgent.contains("Linux")) return "Linux";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("iOS") || userAgent.contains("iPhone") || userAgent.contains("iPad")) return "iOS";
        return "Unknown";
    }

    /**
     * 解析浏览器
     */
    private String parseBrowser(String userAgent) {
        if (userAgent == null) return "Unknown";
        if (userAgent.contains("Edg")) return "Edge";
        if (userAgent.contains("Chrome")) return "Chrome";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) return "Safari";
        return "Unknown";
    }

    /**
     * 生成 TOTP 密钥
     * <p>
     * 使用 GoogleAuthenticator 库生成符合 RFC 6238 标准的 TOTP 密钥。
     * </p>
     *
     * @return Base32 编码的密钥字符串
     */
    private String generateTOTPSecret() {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        String secret = key.getKey();
        log.info("✅ TOTP 密钥生成成功: secret=", secret);
        return secret;
    }

    /**
     * 验证 TOTP 码
     * <p>
     * 使用 GoogleAuthenticator 库验证用户输入的 6 位 TOTP 验证码。
     * 支持时间窗口容错（前后各 1 个时间窗口，共 3 个窗口）。
     * </p>
     *
     * @param secret TOTP 密钥（Base32 编码）
     * @param code   用户输入的 6 位验证码
     * @return 验证是否通过
     */
    private boolean verifyTOTPCode(String secret, String code) {
        if (secret == null || secret.isEmpty() || code == null || code.isEmpty()) {
            log.warn("⚠️ TOTP 验证失败：密钥或验证码为空");
            return false;
        }

        try {
            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            int codeInt = Integer.parseInt(code);
            boolean isValid = gAuth.authorize(secret, codeInt);

            if (isValid) {
                log.info("✅ TOTP 验证成功: code={}", code);
            } else {
                log.warn("⚠️ TOTP 验证失败: code={}", code);
            }

            return isValid;
        } catch (NumberFormatException e) {
            log.error("❌ TOTP 验证失败：验证码格式错误: code={}", code);
            return false;
        } catch (Exception e) {
            log.error("❌ TOTP 验证失败：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取用户列表（管理端）
     */
    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<UserProfileResponse> listUsers(String keyword, String status, int page, int size) {
        log.info("查询用户列表: keyword={}, status={}, page={}, size={}", keyword, status, page, size);

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));

        org.springframework.data.domain.Page<User> userPage;

        if (keyword != null && !keyword.isEmpty() && status != null && !status.isEmpty()) {
            userPage = userRepository.findByUsernameContainingAndStatus(keyword, com.campus.marketplace.common.enums.UserStatus.valueOf(status), pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            userPage = userRepository.findByUsernameContaining(keyword, pageable);
        } else if (status != null && !status.isEmpty()) {
            userPage = userRepository.findByStatus(com.campus.marketplace.common.enums.UserStatus.valueOf(status), pageable);
        } else {
            userPage = userRepository.findAllWithCampusAndRoles(pageable);
        }

        return userPage.map(this::convertToUserProfileResponse);
    }

    private UserProfileResponse convertToUserProfileResponse(User user) {
        // 获取校区名称
        String campusName = null;
        if (user.getCampus() != null) {
            campusName = user.getCampus().getName();
        }

        // 获取封禁原因
        String banReason = null;
        if (user.getStatus() == com.campus.marketplace.common.enums.UserStatus.BANNED) {
            List<com.campus.marketplace.common.entity.BanLog> banLogs = banLogRepository
                .findTopByUserIdAndIsUnbannedFalseOrderByCreatedAtDesc(user.getId());
            if (!banLogs.isEmpty()) {
                banReason = banLogs.get(0).getReason();
            }
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(encryptUtil.maskEmail(user.getEmail()))
                .phone(encryptUtil.maskPhone(user.getPhone()))
                .studentId(user.getStudentId())
                .avatar(user.getAvatar())
                .status(user.getStatus().name())
                .points(user.getPoints())
                .campusId(user.getCampusId())
                .campusName(campusName)
                .banReason(banReason)
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .createdAt(user.getCreatedAt())
                .build();
    }
}
