package com.campus.marketplace.common.mail;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;

/**
 * 开发环境用的日志邮件发送器：不真正发送邮件，只把内容输出到日志。
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
public class LoggingJavaMailSender implements JavaMailSender {

    @Override
    public MimeMessage createMimeMessage() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }

    @Override
    public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
        try {
            return new MimeMessage(Session.getInstance(new Properties()), contentStream);
        } catch (Exception e) {
            throw new org.springframework.mail.MailParseException("创建MimeMessage失败", e);
        }
    }

    @Override
    public void send(MimeMessage mimeMessage) throws MailException {
        try {
            log.info("[DEV-MAIL][MIME] to={}, subject={}, size={} bytes",
                    Arrays.toString(mimeMessage.getAllRecipients()),
                    mimeMessage.getSubject(),
                    mimeMessage.getSize());
        } catch (Exception e) {
            log.warn("[DEV-MAIL] 解析MimeMessage失败: {}", e.getMessage());
        }
    }

    @Override
    public void send(MimeMessage... mimeMessages) throws MailException {
        if (mimeMessages == null) return;
        for (MimeMessage m : mimeMessages) {
            send(m);
        }
    }

    @Override
    public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
        try {
            MimeMessage mm = createMimeMessage();
            mimeMessagePreparator.prepare(mm);
            send(mm);
        } catch (Exception e) {
            throw new org.springframework.mail.MailPreparationException("准备MimeMessage失败", e);
        }
    }

    @Override
    public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
        if (mimeMessagePreparators == null) return;
        for (MimeMessagePreparator prep : mimeMessagePreparators) {
            send(prep);
        }
    }

    @Override
    public void send(SimpleMailMessage simpleMessage) throws MailException {
        if (simpleMessage == null) return;
        log.info("[DEV-MAIL] to={}, subject={}, from={}, text={}",
                Arrays.toString(simpleMessage.getTo()),
                simpleMessage.getSubject(),
                simpleMessage.getFrom(),
                preview(simpleMessage.getText()));
    }

    @Override
    public void send(SimpleMailMessage... simpleMessages) throws MailException {
        if (simpleMessages == null) return;
        for (SimpleMailMessage msg : simpleMessages) {
            send(msg);
        }
    }

    private String preview(@Nullable String text) {
        if (text == null) return "";
        String t = text.replaceAll("\r?\n", " ");
        byte[] bytes = t.getBytes(StandardCharsets.UTF_8);
        int max = Math.min(bytes.length, 512);
        return new String(bytes, 0, max, StandardCharsets.UTF_8);
    }
}
