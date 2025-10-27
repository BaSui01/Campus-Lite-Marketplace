package com.campus.marketplace.common.config;

import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 微信支付V2配置类（沙箱环境） 💰
 *
 * 仅在配置 wechat.pay.version=v2 时启用
 * 用于开发测试阶段的沙箱环境
 *
 * <p>V2沙箱环境特点：
 * <ul>
 *   <li>无需真实资金流转</li>
 *   <li>支持完整的支付测试流程</li>
 *   <li>需要获取沙箱密钥（sandbox_signkey）</li>
 *   <li>沙箱API地址：https://api.mch.weixin.qq.com/sandboxnew/...</li>
 * </ul>
 * </p>
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@Data
@Configuration
@ConditionalOnProperty(name = "wechat.pay.version", havingValue = "v2")
@ConfigurationProperties(prefix = "wechat.pay.v2")
public class WechatPayV2Config {

    /**
     * 应用ID（公众号APPID或小程序APPID）
     */
    private String appId;

    /**
     * 商户号（MCHID）
     */
    private String mchId;

    /**
     * 商户密钥（API密钥）
     * 沙箱环境：通过API获取沙箱密钥
     * 正式环境：在微信支付商户平台设置
     */
    private String mchKey;

    /**
     * 商户证书路径（可选）
     * 用于退款等需要证书的接口
     */
    private String keyPath;

    /**
     * 是否使用沙箱环境
     * true：沙箱环境，false：正式环境
     */
    private boolean useSandbox = true;

    /**
     * 支付结果异步通知地址
     */
    private String notifyUrl;

    /**
     * 创建微信支付V2服务 Bean 🎯
     *
     * @return WxPayService 实例
     */
    @Bean
    public WxPayService wxPayService() {
        log.info("🚀 初始化微信支付V2配置（沙箱环境）: merchantId={}, useSandbox={}", mchId, useSandbox);

        WxPayConfig payConfig = new WxPayConfig();
        payConfig.setAppId(appId);
        payConfig.setMchId(mchId);
        payConfig.setMchKey(mchKey);
        payConfig.setKeyPath(keyPath);
        payConfig.setUseSandboxEnv(useSandbox);
        payConfig.setNotifyUrl(notifyUrl);

        WxPayService wxPayService = new WxPayServiceImpl();
        wxPayService.setConfig(payConfig);

        log.info("✅ 微信支付V2配置初始化成功（沙箱模式）");
        return wxPayService;
    }
}
