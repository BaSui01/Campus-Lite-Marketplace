package com.campus.marketplace.service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 登录通知服务接口
 *
 * @author BaSui 😎
 * @date 2025-11-09
 */
public interface LoginNotificationService {

    /**
     * 检测新设备登录并发送通知
     *
     * @param userId  用户 ID
     * @param request HTTP 请求（用于获取设备信息）
     */
    void detectAndNotifyNewDevice(Long userId, HttpServletRequest request);

    /**
     * 发送登录通知邮件
     *
     * @param userId       用户 ID
     * @param deviceName   设备名称
     * @param ip           IP 地址
     * @param location     地理位置
     * @param loginTime    登录时间
     * @param isNewDevice  是否新设备
     */
    void sendLoginNotificationEmail(
            Long userId,
            String deviceName,
            String ip,
            String location,
            String loginTime,
            boolean isNewDevice
    );
}
