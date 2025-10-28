package com.campus.marketplace.service.impl;

import com.campus.marketplace.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 开发环境日志短信实现。
 */
@Slf4j
@Service
@Profile("dev")
@ConditionalOnMissingBean(SmsService.class)
public class DevLoggingSmsService implements SmsService {
    @Override
    public void send(String phone, String templateCode, Map<String, String> params) {
        log.info("[DEV-SMS] phone={} template={} params={}", phone, templateCode, params);
    }
}
