package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.IpLocationUtil;
import com.campus.marketplace.entity.LoginDevice;
import com.campus.marketplace.repository.LoginDeviceRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.EmailTemplateService;
import com.campus.marketplace.service.LoginNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 登录通知服务实现类 - 真实实现不使用模拟数据！
 *
 * @author BaSui 😎
 * @date 2025-11-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginNotificationServiceImpl implements LoginNotificationService {

    private final UserRepository userRepository;
    private final LoginDeviceRepository loginDeviceRepository;
    private final EmailTemplateService emailTemplateService;
    private final IpLocationUtil ipLocationUtil;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 检测新设备登录并发送通知
     */
    @Override
    @Async
    public void detectAndNotifyNewDevice(Long userId, HttpServletRequest request) {
        log.info("🔍 检测新设备登录: userId={}", userId);

        try {
            // 1. 获取设备信息
            String userAgent = request.getHeader("User-Agent");
            String ip = getClientIp(request);

            // 2. 查询用户的所有登录设备
            List<LoginDevice> existingDevices = loginDeviceRepository.findByUserIdOrderByLastActiveAtDesc(userId);

            // 3. 检查是否为新设备
            boolean isNewDevice = existingDevices.stream()
                    .noneMatch(device -> device.getIp().equals(ip) && device.getUserAgent().equals(userAgent));

            if (isNewDevice) {
                log.info("🆕 检测到新设备登录: userId={}, ip={}", userId, ip);

                // 4. 解析设备信息
                String deviceName = parseDeviceName(userAgent);
                String location = getLocation(ip);
                String loginTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);

                // 5. 发送登录通知邮件
                sendLoginNotificationEmail(userId, deviceName, ip, location, loginTime, true);
            } else {
                log.info("✅ 已知设备登录: userId={}, ip={}", userId, ip);
            }
        } catch (Exception e) {
            log.error("❌ 检测新设备登录失败: userId={}, error={}", userId, e.getMessage(), e);
            // 不抛出异常，避免影响登录流程
        }
    }

    /**
     * 发送登录通知邮件
     */
    @Override
    @Async
    public void sendLoginNotificationEmail(
            Long userId,
            String deviceName,
            String ip,
            String location,
            String loginTime,
            boolean isNewDevice
    ) {
        log.info("📧 发送登录通知邮件: userId={}, isNewDevice={}", userId, isNewDevice);

        try {
            // 1. 查询用户
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            // 2. 检查用户是否有邮箱
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                log.warn("⚠️ 用户未绑定邮箱，跳过登录通知: userId={}", userId);
                return;
            }

            // 3. 发送HTML邮件通知
            emailTemplateService.sendLoginNotification(
                    user.getEmail(),
                    user.getUsername(),
                    deviceName,
                    ip,
                    location,
                    loginTime,
                    isNewDevice
            );

            log.info("✅ HTML登录通知邮件发送成功: userId={}, email={}", userId, user.getEmail());
        } catch (Exception e) {
            log.error("❌ 登录通知邮件发送失败: userId={}, error={}", userId, e.getMessage(), e);
            // 不抛出异常，避免影响登录流程
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 获取客户端 IP 地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多级代理，取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 解析设备名称
     */
    private String parseDeviceName(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "未知设备";
        }

        String os = parseOS(userAgent);
        String browser = parseBrowser(userAgent);

        return os + " - " + browser;
    }

    /**
     * 解析操作系统
     */
    private String parseOS(String userAgent) {
        if (userAgent == null) return "Unknown";

        if (userAgent.contains("Windows NT 10.0")) return "Windows 10";
        if (userAgent.contains("Windows NT 11.0")) return "Windows 11";
        if (userAgent.contains("Windows NT 6.3")) return "Windows 8.1";
        if (userAgent.contains("Windows NT 6.2")) return "Windows 8";
        if (userAgent.contains("Windows NT 6.1")) return "Windows 7";
        if (userAgent.contains("Mac OS X")) return "macOS";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("iPhone") || userAgent.contains("iPad")) return "iOS";
        if (userAgent.contains("Linux")) return "Linux";

        return "Unknown OS";
    }

    /**
     * 解析浏览器
     */
    private String parseBrowser(String userAgent) {
        if (userAgent == null) return "Unknown";

        if (userAgent.contains("Edg/")) return "Edge";
        if (userAgent.contains("Chrome/") && !userAgent.contains("Edg/")) return "Chrome";
        if (userAgent.contains("Firefox/")) return "Firefox";
        if (userAgent.contains("Safari/") && !userAgent.contains("Chrome/")) return "Safari";
        if (userAgent.contains("Opera/") || userAgent.contains("OPR/")) return "Opera";

        return "Unknown Browser";
    }

    /**
     * 获取地理位置
     * <p>
     * 使用 IpLocationUtil 解析 IP 地址的地理位置信息。
     * 离线查询，速度快，准确度高！😎
     * </p>
     *
     * @param ip IP 地址
     * @return 地理位置字符串（如：广东省深圳市）
     */
    private String getLocation(String ip) {
        return ipLocationUtil.getLocation(ip);
    }
}
