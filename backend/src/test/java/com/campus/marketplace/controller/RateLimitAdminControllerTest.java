package com.campus.marketplace.controller;

import com.campus.marketplace.common.component.RateLimitRuleManager;
import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = RateLimitAdminController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("RateLimitAdminController MockMvc 测试")
class RateLimitAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RateLimitRuleManager ruleManager;

    @Test
    @DisplayName("管理员查询限流规则")
    @WithMockUser(authorities = "system:rate-limit:manage")
    void getRules_success() throws Exception {
        when(ruleManager.isEnabled()).thenReturn(true);
        when(ruleManager.getUserWhitelist()).thenReturn(Set.of(1L));
        when(ruleManager.getIpWhitelist()).thenReturn(Set.of("127.0.0.1"));
        when(ruleManager.getIpBlacklist()).thenReturn(Set.of("192.0.2.1"));

        mockMvc.perform(get("/api/admin/rate-limit/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.userWhitelist[0]").value(1));

        verify(ruleManager).isEnabled();
    }

    @Test
    @DisplayName("设置限流开关")
    @WithMockUser(authorities = "system:rate-limit:manage")
    void setEnabled_success() throws Exception {
        mockMvc.perform(post("/api/admin/rate-limit/enabled/{enabled}", false))
                .andExpect(status().isOk());

        verify(ruleManager).setEnabled(false);
    }

    @Test
    @DisplayName("维护用户白名单")
    @WithMockUser(authorities = "system:rate-limit:manage")
    void manageUserWhitelist_success() throws Exception {
        mockMvc.perform(post("/api/admin/rate-limit/whitelist/users/{userId}", 88L))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/admin/rate-limit/whitelist/users/{userId}", 88L))
                .andExpect(status().isOk());

        verify(ruleManager).addUserWhitelist(88L);
        verify(ruleManager).removeUserWhitelist(88L);
    }

    @Test
    @DisplayName("维护 IP 白名单与黑名单")
    @WithMockUser(authorities = "system:rate-limit:manage")
    void manageIpLists_success() throws Exception {
        mockMvc.perform(post("/api/admin/rate-limit/whitelist/ips/{ip}", "127.0.0.1"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/admin/rate-limit/whitelist/ips/{ip}", "127.0.0.1"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/admin/rate-limit/blacklist/ips/{ip}", "192.0.2.1"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/admin/rate-limit/blacklist/ips/{ip}", "192.0.2.1"))
                .andExpect(status().isOk());

        verify(ruleManager).addIpWhitelist("127.0.0.1");
        verify(ruleManager).removeIpWhitelist("127.0.0.1");
        verify(ruleManager).addIpBlacklist("192.0.2.1");
        verify(ruleManager).removeIpBlacklist("192.0.2.1");
    }
}
