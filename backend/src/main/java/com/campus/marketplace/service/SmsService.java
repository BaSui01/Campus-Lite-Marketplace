package com.campus.marketplace.service;

import java.util.Map;

/**
 * 短信发送服务（可插拔）。
 */
public interface SmsService {

    /**
     * 发送短信。
     * @param phone 手机号
     * @param templateCode 模板编码
     * @param params 模板变量
     */
    void send(String phone, String templateCode, Map<String, String> params);
}
