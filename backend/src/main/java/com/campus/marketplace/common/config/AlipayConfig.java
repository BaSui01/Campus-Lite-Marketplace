package com.campus.marketplace.common.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝支付配置类 🎉
 *
 * 配置支付宝沙箱环境，用于开发和测试
 *
 * <p>沙箱环境说明：
 * <ul>
 *   <li>沙箱网关：https://openapi-sandbox.dl.alipaydev.com/gateway.do</li>
 *   <li>无需真实资金流转，适合开发测试</li>
 *   <li>需要先在支付宝开放平台创建沙箱应用获取配置信息</li>
 * </ul>
 * </p>
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {

    /**
     * 应用ID（APPID）
     * 在支付宝开放平台-开发者中心-沙箱应用中获取
     */
    private String appId;

    /**
     * 商户私钥
     * 使用支付宝密钥生成工具生成，用于请求签名
     */
    private String privateKey;

    /**
     * 支付宝公钥
     * 在支付宝开放平台上传应用公钥后获取，用于验签
     */
    private String alipayPublicKey;

    /**
     * 支付网关地址
     * 沙箱环境：https://openapi-sandbox.dl.alipaydev.com/gateway.do
     * 正式环境：https://openapi.alipay.com/gateway.do
     */
    private String gatewayUrl;

    /**
     * 字符编码格式
     * 默认：UTF-8
     */
    private String charset = "UTF-8";

    /**
     * 数据格式
     * 默认：json
     */
    private String format = "json";

    /**
     * 签名算法类型
     * RSA2（推荐）或 RSA
     */
    private String signType = "RSA2";

    /**
     * 支付结果异步通知地址
     * 支付完成后支付宝会主动回调这个地址
     */
    private String notifyUrl;

    /**
     * 支付结果同步跳转地址
     * 支付完成后用户会跳转到这个地址
     */
    private String returnUrl;

    /**
     * 创建支付宝客户端 Bean 🚀
     *
     * @return AlipayClient 实例
     */
    @Bean
    public AlipayClient alipayClient() {
        return new DefaultAlipayClient(
                gatewayUrl,
                appId,
                privateKey,
                format,
                charset,
                alipayPublicKey,
                signType
        );
    }
}
