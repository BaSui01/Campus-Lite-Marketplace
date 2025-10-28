package com.campus.marketplace.integration;

import com.campus.marketplace.common.component.RateLimitRuleManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("RateLimit 令牌桶集成测试")
class RateLimitTokenBucketIT extends IntegrationTestBase {

    @Autowired
    private RateLimitRuleManager ruleManager;

    private static final String PATH = "/auth/test/ratelimit/tb";

    @Test
    @DisplayName("IP 维度令牌桶容量 3：前 3 次 200，第 4 次 429，响应头包含 RateLimit-*")
    void tokenBucket_basic() throws Exception {
        String ip = "1.2.3.4";
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get(PATH)
                            .header("X-Forwarded-For", ip)
                            .accept(MediaType.TEXT_PLAIN))
                    .andExpect(status().isOk())
                    .andDo(r -> {
                        String limit = r.getResponse().getHeader("RateLimit-Limit");
                        String remaining = r.getResponse().getHeader("RateLimit-Remaining");
                        String reset = r.getResponse().getHeader("RateLimit-Reset");
                        assertThat(limit).isEqualTo("3");
                        assertThat(remaining).isNotNull();
                        assertThat(reset).isNotNull();
                    });
        }

        mockMvc.perform(get(PATH)
                        .header("X-Forwarded-For", ip)
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isTooManyRequests())
                .andDo(r -> {
                    assertThat(r.getResponse().getHeader("RateLimit-Limit")).isEqualTo("3");
                    assertThat(r.getResponse().getHeader("RateLimit-Remaining")).isEqualTo("0");
                });
    }

    @Test
    @DisplayName("白名单豁免：加入 IP 白名单后不再限流")
    void whitelist_exempt() throws Exception {
        String ip = "5.6.7.8";
        // 先打满
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get(PATH).header("X-Forwarded-For", ip))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(get(PATH).header("X-Forwarded-For", ip))
                .andExpect(status().isTooManyRequests());

        // 加入白名单后放行
        ruleManager.addIpWhitelist(ip);
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get(PATH).header("X-Forwarded-For", ip))
                    .andExpect(status().isOk());
        }
    }
}
