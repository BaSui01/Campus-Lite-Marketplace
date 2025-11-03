package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.utils.SensitiveWordFilter;
import com.campus.marketplace.service.ContentAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * 内容审核服务实现类
 * 
 * 实现内容审核功能：敏感词过滤、广告识别、垃圾内容检测
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentAuditServiceImpl implements ContentAuditService {

    private final SensitiveWordFilter sensitiveWordFilter;

    // 广告关键词正则（手机号、微信号、QQ号）
    private static final Pattern PHONE_PATTERN = Pattern.compile("1[3-9]\\d{9}");
    private static final Pattern WECHAT_PATTERN = Pattern.compile("(微信|wx|wechat)[：:号]?\\s*[a-zA-Z0-9_-]{5,20}", Pattern.CASE_INSENSITIVE);
    private static final Pattern QQ_PATTERN = Pattern.compile("(qq|扣扣)[：:号]?\\s*[0-9]{5,12}", Pattern.CASE_INSENSITIVE);
    
    // 垃圾内容关键词
    private static final Pattern[] SPAM_PATTERNS = {
        Pattern.compile("(加|扫码|联系).{0,5}(微信|qq)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(私聊|私信).{0,5}(我|联系)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(代购|代理|招聘).{0,5}(兼职|赚钱)", Pattern.CASE_INSENSITIVE)
    };

    @Override
    public boolean auditContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        // 检测广告
        if (detectAdvertisement(content)) {
            log.warn("内容审核失败: 检测到广告内容");
            return false;
        }

        // 检测敏感词
        String filtered = sensitiveWordFilter.filter(content);
        if (!filtered.equals(content)) {
            log.warn("内容审核失败: 包含敏感词");
            return false;
        }

        return true;
    }

    @Override
    public String filterSensitiveWords(String content) {
        if (content == null) {
            return null;
        }
        return sensitiveWordFilter.filter(content);
    }

    @Override
    public boolean detectAdvertisement(String content) {
        if (content == null) {
            return false;
        }

        // 检测手机号
        if (PHONE_PATTERN.matcher(content).find()) {
            log.debug("检测到手机号: {}", content);
            return true;
        }

        // 检测微信号
        if (WECHAT_PATTERN.matcher(content).find()) {
            log.debug("检测到微信号: {}", content);
            return true;
        }

        // 检测QQ号
        if (QQ_PATTERN.matcher(content).find()) {
            log.debug("检测到QQ号: {}", content);
            return true;
        }

        return false;
    }

    @Override
    public boolean detectSpam(Long userId, String content) {
        if (content == null) {
            return false;
        }

        // 检测垃圾内容关键词
        for (Pattern pattern : SPAM_PATTERNS) {
            if (pattern.matcher(content).find()) {
                log.debug("检测到垃圾内容: userId={}, content={}", userId, content);
                return true;
            }
        }

        // TODO: 检测重复发帖（需要查询数据库）
        // TODO: 检测灌水内容（短时间内大量发帖）

        return false;
    }
}
