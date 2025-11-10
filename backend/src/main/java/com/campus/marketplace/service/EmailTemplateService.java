package com.campus.marketplace.service;

import java.util.Map;

/**
 * 邮件模板服务 - 支持HTML邮件发送 🎨
 *
 * @author BaSui 😎
 * @date 2025-11-10
 */
public interface EmailTemplateService {

    /**
     * 发送验证码邮件（HTML美化版）
     *
     * @param to      收件人邮箱
     * @param code    验证码
     * @param purpose 用途（REGISTER/RESET）
     */
    void sendVerificationCode(String to, String code, String purpose);

    /**
     * 发送登录通知邮件（HTML美化版）
     *
     * @param to         收件人邮箱
     * @param username   用户名
     * @param deviceName 设备名称
     * @param ip         IP地址
     * @param location   登录地点
     * @param loginTime  登录时间
     * @param isNewDevice 是否新设备
     */
    void sendLoginNotification(String to, String username, String deviceName,
                               String ip, String location, String loginTime, boolean isNewDevice);

    /**
     * 发送通用通知邮件（HTML美化版）
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param title   通知标题
     * @param content 通知内容
     * @param link    跳转链接（可选）
     */
    void sendNotification(String to, String subject, String title, String content, String link);

    /**
     * 发送HTML邮件（通用方法）
     *
     * @param to           收件人邮箱
     * @param subject      邮件主题
     * @param templateName 模板名称
     * @param variables    模板变量
     */
    void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables);
}
