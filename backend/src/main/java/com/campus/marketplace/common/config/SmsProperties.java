package com.campus.marketplace.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 短信配置属性（阿里云）。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "sms.aliyun")
public class SmsProperties {
    private String accessKeyId;
    private String accessKeySecret;
    private String signName;
    // 各场景模板编码
    private String templateRegister;
    private String templateReset;
}
