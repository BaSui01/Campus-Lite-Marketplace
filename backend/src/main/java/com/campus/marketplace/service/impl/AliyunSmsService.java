package com.campus.marketplace.service.impl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.teaopenapi.models.Config;
import com.campus.marketplace.common.config.SmsProperties;
import com.campus.marketplace.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 阿里云短信实现（生产环境）。
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Service
@Profile("!dev")
@ConditionalOnProperty(name = "sms.provider", havingValue = "aliyun", matchIfMissing = true)
@RequiredArgsConstructor
public class AliyunSmsService implements SmsService {

    private final SmsProperties props;

    private Client buildClient() throws Exception {
        Config config = new Config()
                .setAccessKeyId(props.getAccessKeyId())
                .setAccessKeySecret(props.getAccessKeySecret());
        config.endpoint = "dysmsapi.aliyuncs.com";
        return new Client(config);
    }

    @Override
    public void send(String phone, String templateCode, Map<String, String> params) {
        try {
            Client client = buildClient();
            String jsonParams = com.fasterxml.jackson.databind.json.JsonMapper.builder().build().writeValueAsString(params);
            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName(props.getSignName())
                    .setTemplateCode(templateCode)
                    .setTemplateParam(jsonParams);
            var resp = client.sendSms(request);
            if (resp == null || resp.getBody() == null || !"OK".equalsIgnoreCase(resp.getBody().getCode())) {
                String msg = resp != null && resp.getBody() != null ? resp.getBody().getMessage() : "unknown";
                throw new RuntimeException("短信发送失败: " + msg);
            }
            log.info("阿里云短信发送成功 phone={} template={}", phone, templateCode);
        } catch (Exception e) {
            log.error("阿里云短信发送异常", e);
            throw new RuntimeException("短信发送异常");
        }
    }
}
