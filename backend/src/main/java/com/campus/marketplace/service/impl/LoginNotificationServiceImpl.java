package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.entity.LoginDevice;
import com.campus.marketplace.repository.LoginDeviceRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.LoginNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:${spring.mail.username:}}")
    private String mailFrom;

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

            // 3. 构建邮件内容
            String subject = isNewDevice ? "【校园轻享集市】新设备登录通知" : "【校园轻享集市】登录通知";
            String text = buildEmailContent(user.getUsername(), deviceName, ip, location, loginTime, isNewDevice);

            // 4. 发送邮件
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject(subject);
            message.setText(text);
            if (mailFrom != null && !mailFrom.isBlank()) {
                message.setFrom(mailFrom);
            }

            mailSender.send(message);

            log.info("✅ 登录通知邮件发送成功: userId={}, email={}", userId, user.getEmail());
        } catch (Exception e) {
            log.error("❌ 登录通知邮件发送失败: userId={}, error={}", userId, e.getMessage(), e);
            // 不抛出异常，避免影响登录流程
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 构建邮件内容
     */
    private String buildEmailContent(
            String username,
            String deviceName,
            String ip,
            String location,
            String loginTime,
            boolean isNewDevice
    ) {
        StringBuilder content = new StringBuilder();
        content.append("尊敬的 ").append(username).append("：\n\n");

        if (isNewDevice) {
            content.append("我们检测到您的账号在一个新设备上登录，详情如下：\n\n");
        } else {
            content.append("您的账号刚刚登录，详情如下：\n\n");
        }

        content.append("登录时间：").append(loginTime).append("\n");
        content.append("登录设备：").append(deviceName).append("\n");
        content.append("IP 地址：").append(ip).append("\n");
        content.append("地理位置：").append(location).append("\n\n");

        if (isNewDevice) {
            content.append("如果这不是您本人的操作，请立即：\n");
            content.append("1. 修改您的账号密码\n");
            content.append("2. 启用双因素认证（2FA）\n");
            content.append("3. 检查您的账号安全设置\n\n");
        }

        content.append("如有疑问，请联系客服。\n\n");
        content.append("此邮件由系统自动发送，请勿回复。\n\n");
        content.append("校园轻享集市团队\n");
        content.append(loginTime);

        return content.toString();
    }

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
     * 获取地理位置（简化版，生产环境建议使用 IP 地理位置服务）
     */
    private String getLocation(String ip) {
        // TODO: 集成 IP 地理位置服务（如 ip2region、GeoIP2 等）
        // 这里返回简化版本
        if (ip == null || ip.isEmpty()) {
            return "未知位置";
        }

        // 本地 IP
        if (ip.startsWith("127.") || ip.startsWith("192.168.") || ip.startsWith("10.") || ip.equals("0:0:0:0:0:0:0:1")) {
            return "本地网络";
        }

        // 生产环境应该调用 IP 地理位置服务
        return "未知位置（IP: " + ip + "）";
    }
}
