package com.campus.marketplace.service.impl;

import com.campus.marketplace.service.EmailTemplateService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * 邮件模板服务实现 - HTML邮件美化版 🎨
 *
 * @author BaSui 😎
 * @date 2025-11-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.from:noreply@campus-marketplace.com}")
    private String mailFrom;

    @Override
    public void sendVerificationCode(String to, String code, String purpose) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("code", code);
        variables.put("purpose", purpose);
        variables.put("purposeText", "REGISTER".equals(purpose) ? "注册" : "重置密码");
        variables.put("validMinutes", 10);

        String subject = "REGISTER".equals(purpose) ? "【校园轻享集市】注册验证码" : "【校园轻享集市】重置密码验证码";
        sendHtmlEmail(to, subject, "verification-code", variables);
    }

    @Override
    public void sendLoginNotification(String to, String username, String deviceName,
                                      String ip, String location, String loginTime, boolean isNewDevice) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("username", username);
        variables.put("deviceName", deviceName);
        variables.put("ip", ip);
        variables.put("location", location);
        variables.put("loginTime", loginTime);
        variables.put("isNewDevice", isNewDevice);

        String subject = isNewDevice ? "【校园轻享集市】新设备登录通知" : "【校园轻享集市】登录通知";
        sendHtmlEmail(to, subject, "login-notification", variables);
    }

    @Override
    public void sendNotification(String to, String subject, String title, String content, String link) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("title", title);
        variables.put("content", content);
        variables.put("link", link);
        variables.put("hasLink", link != null && !link.isEmpty());

        sendHtmlEmail(to, subject, "notification", variables);
    }

    @Override
    public void sendPaymentSuccess(String to, String orderNo, String goodsTitle,
                                   String goodsDescription, String goodsPrice, String goodsImage,
                                   String actualAmount, String paymentMethod, String paymentTime,
                                   String transactionId, String sellerName) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("orderNo", orderNo);
        variables.put("goodsTitle", goodsTitle);
        variables.put("goodsDescription", goodsDescription);
        variables.put("goodsPrice", goodsPrice);
        variables.put("goodsImage", goodsImage);
        variables.put("actualAmount", actualAmount);
        variables.put("paymentMethod", paymentMethod);
        variables.put("paymentTime", paymentTime);
        variables.put("transactionId", transactionId);
        variables.put("sellerName", sellerName);

        String subject = "【校园轻享集市】支付成功通知";
        sendHtmlEmail(to, subject, "payment-success", variables);
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            // 🎯 创建 MimeMessage（支持HTML）
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(mailFrom);

            // 🎨 使用 Thymeleaf 渲染HTML模板
            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process("email/" + templateName, context);

            helper.setText(htmlContent, true); // true = HTML格式

            // 📧 发送邮件
            mailSender.send(message);
            log.info("✅ HTML邮件发送成功: to={}, subject={}, template={}", to, subject, templateName);

        } catch (MessagingException e) {
            log.error("❌ HTML邮件发送失败: to={}, subject={}, error={}", to, subject, e.getMessage(), e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }
}
