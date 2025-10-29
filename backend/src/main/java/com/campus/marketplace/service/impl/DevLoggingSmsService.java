package com.campus.marketplace.service.impl;

import com.campus.marketplace.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 开发/测试环境日志短信实现。
 *
 * 💡 BaSui 说明：
 * - dev 环境：本地开发，打印日志替代真实短信
 * - test 环境：单元测试/集成测试，避免调用阿里云 API
 * - prod 环境：使用 AliyunSmsService 发送真实短信
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Service
@Profile({"dev", "test"})  // ✅ BaSui修复：测试环境也使用日志实现
@ConditionalOnMissingBean(SmsService.class)
public class DevLoggingSmsService implements SmsService {
    @Override
    public void send(String phone, String templateCode, Map<String, String> params) {
        log.info("📱 [DEV-SMS] 短信已拦截（日志模式）| 手机号={}, 模板={}, 参数={}", phone, templateCode, params);
    }
}
