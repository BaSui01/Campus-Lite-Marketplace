package com.campus.marketplace.common.config;

import com.alipay.api.AlipayClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AlipayConfig 测试")
class AlipayConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(AlipayConfig.class)
            .withPropertyValues(
                    "alipay.app-id=test-app",
                    "alipay.private-key=pri",
                    "alipay.alipay-public-key=pub",
                    "alipay.gateway-url=https://sandbox",
                    "alipay.notify-url=https://notify",
                    "alipay.return-url=https://return"
            );

    @Test
    @DisplayName("属性绑定并创建 AlipayClient Bean")
    void shouldCreateAlipayClient() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AlipayConfig.class);
            AlipayConfig config = context.getBean(AlipayConfig.class);
            assertThat(config.getAppId()).isEqualTo("test-app");
            assertThat(config.getGatewayUrl()).isEqualTo("https://sandbox");

            assertThat(context).hasSingleBean(AlipayClient.class);
        });
    }
}
