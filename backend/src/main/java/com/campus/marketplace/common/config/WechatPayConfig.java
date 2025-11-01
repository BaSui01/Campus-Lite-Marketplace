package com.campus.marketplace.common.config;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 微信支付V3配置类 💰
 *
 * 配置微信支付APIv3，支持沙箱和正式环境
 *
 * <p>V3版本改进：
 * <ul>
 *   <li>使用RSA证书签名，安全性更高</li>
 *   <li>支持自动更新平台证书</li>
 *   <li>API响应更规范，JSON格式统一</li>
 *   <li>沙箱环境与正式环境API地址一致</li>
 * </ul>
 * </p>
 *
 * <p>必需配置：
 * <ul>
 *   <li>appId: 应用ID（公众号/小程序APPID）</li>
 *   <li>mchId: 商户号</li>
 *   <li>privateKeyPath: 商户API私钥路径（apiclient_key.pem）</li>
 *   <li>merchantSerialNumber: 商户证书序列号</li>
 *   <li>apiV3Key: APIv3密钥（用于回调通知解密）</li>
 *   <li>notifyUrl: 支付结果异步通知地址</li>
 * </ul>
 * </p>
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@Data
@Configuration
@Profile("prod")
@ConditionalOnProperty(name = "wechat.pay.version", havingValue = "v3", matchIfMissing = true)
@ConfigurationProperties(prefix = "wechat.pay")
public class WechatPayConfig {

    /**
     * 应用ID（公众号APPID或小程序APPID）
     */
    private String appId;

    /**
     * 商户号（MCHID）
     */
    private String mchId;

    /**
     * 商户API私钥路径
     * 格式：PEM格式的私钥文件（apiclient_key.pem）
     * 示例：/path/to/apiclient_key.pem
     */
    private String privateKeyPath;

    /**
     * 商户证书序列号
     * 在商户平台下载证书时获取，格式：16进制大写字符串
     * 示例：5157F09EFDC096DE15EBE81A47057A7212345678
     */
    private String merchantSerialNumber;

    /**
     * APIv3密钥
     * 在商户平台设置，用于回调通知的解密
     * 32位字符串
     */
    private String apiV3Key;

    /**
     * 支付结果异步通知地址
     * 必须是外网可访问的HTTPS地址
     * 本地开发可使用ngrok/natapp等内网穿透工具
     */
    private String notifyUrl;

    /**
     * 创建微信支付配置 Bean 🎯
     *
     * 使用RSAAutoCertificateConfig自动更新平台证书
     * 只有在必填配置项都存在时才初始化 Bean
     * 必填项：appId, mchId, privateKeyPath, merchantSerialNumber, apiV3Key
     *
     * @return Config 实例
     */
    @Bean(name = "wechatPayV3Config")
    @ConditionalOnProperty(name = "wechat.pay.app-id")
    public Config wechatPayV3Config() {
        log.info("🚀 初始化微信支付V3配置: merchantId={}", mchId);

        // ✅ BaSui：添加配置校验，避免空值导致启动失败
        if (appId == null || appId.trim().isEmpty() ||
            mchId == null || mchId.trim().isEmpty() ||
            privateKeyPath == null || privateKeyPath.trim().isEmpty() ||
            merchantSerialNumber == null || merchantSerialNumber.trim().isEmpty() ||
            apiV3Key == null || apiV3Key.trim().isEmpty()) {
            log.warn("⚠️ 微信支付V3配置不完整，跳过初始化。请检查 appId、mchId、privateKeyPath、merchantSerialNumber、apiV3Key 配置。");
            log.info("💡 开发环境推荐使用支付宝沙箱进行支付测试。");
            return null;
        }

        try {
            Config config = new RSAAutoCertificateConfig.Builder()
                    .merchantId(mchId)
                    .privateKeyFromPath(privateKeyPath)
                    .merchantSerialNumber(merchantSerialNumber)
                    .apiV3Key(apiV3Key)
                    .build();

            log.info("✅ 微信支付V3配置初始化成功");
            return config;

        } catch (Exception e) {
            log.error("💥 微信支付V3配置初始化失败: {}", e.getMessage(), e);
            throw new RuntimeException("微信支付配置初始化失败", e);
        }
    }

    /**
     * 创建Native支付服务 Bean 🎯
     *
     * 依赖 wechatPayV3Config，如果配置不存在则不创建此 Bean
     *
     * @param config 微信支付配置（可能为 null）
     * @return NativePayService 实例
     */
    @Bean
    @ConditionalOnProperty(name = "wechat.pay.app-id")
    public NativePayService nativePayService(Config config) {
        if (config == null) {
            log.warn("⚠️ 微信支付配置为空，跳过 NativePayService 初始化");
            return null;
        }
        log.info("🚀 初始化微信支付Native服务");
        return new NativePayService.Builder().config(config).build();
    }
}
