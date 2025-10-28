package com.campus.marketplace.service.impl;

import com.campus.marketplace.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

/**
 * 验证码服务实现：Redis 存储，邮件真实发送，短信开发环境日志发送。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JavaMailSender mailSender;
    private final com.campus.marketplace.service.SmsService smsService;
    private final com.campus.marketplace.common.config.SmsProperties smsTemplates;

    @Value("${spring.mail.from:${spring.mail.username:}}")
    private String mailFrom;

    private static final SecureRandom RND = new SecureRandom();
    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    private static final Duration RATE_TTL = Duration.ofMinutes(1);

    private String keyEmail(String email, String purpose) {
        return "verif:email:" + purpose + ":" + email.toLowerCase();
    }

    private String keySms(String phone, String purpose) {
        return "verif:sms:" + purpose + ":" + phone;
    }

    private String keyRate(String channel, String id) {
        return "verif:rate:" + channel + ":" + id;
    }

    private String genCode() {
        int n = 100000 + RND.nextInt(900000);
        return String.valueOf(n);
    }

    private void checkRateLimit(String channel, String id, int maxPerMin) {
        try {
            String k = keyRate(channel, id);
            Long c = redisTemplate.opsForValue().increment(k);
            if (c != null && c == 1) {
                redisTemplate.expire(k, RATE_TTL);
            }
            if (c != null && c > maxPerMin) {
                throw new IllegalStateException("请求过于频繁，请稍后再试");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ignored) { }
    }

    @Override
    public void sendEmailCode(String email, String purpose) {
        checkRateLimit("email", email, 3);
        String code = genCode();
        String k = keyEmail(email, purpose);
        redisTemplate.opsForValue().set(k, code, CODE_TTL);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("校园轻享集市验证码");
        msg.setText("您的验证码为：" + code + "（10分钟内有效）。若非本人操作请忽略。");
        if (mailFrom != null && !mailFrom.isBlank()) msg.setFrom(mailFrom);
        mailSender.send(msg);
        log.info("已发送邮箱验证码 purpose={} email=***{}", purpose, email.substring(Math.max(0, email.length()-4)));
    }

    @Override
    public void sendSmsCode(String phone, String purpose) {
        checkRateLimit("sms", phone, 3);
        String code = genCode();
        String k = keySms(phone, purpose);
        redisTemplate.opsForValue().set(k, code, CODE_TTL);
        // 通过 SmsService 发送真实短信（dev 环境将打印日志）
        String template = switch (purpose) {
            case "REGISTER" -> smsTemplates.getTemplateRegister();
            case "RESET" -> smsTemplates.getTemplateReset();
            default -> smsTemplates.getTemplateReset();
        };
        smsService.send(phone, template, java.util.Map.of("code", code));
    }

    @Override
    public boolean validateEmailCode(String email, String purpose, String code) {
        String k = keyEmail(email, purpose);
        Object v = redisTemplate.opsForValue().get(k);
        return v != null && v.toString().equals(code);
    }

    @Override
    public boolean validateSmsCode(String phone, String purpose, String code) {
        String k = keySms(phone, purpose);
        Object v = redisTemplate.opsForValue().get(k);
        return v != null && v.toString().equals(code);
    }
}
