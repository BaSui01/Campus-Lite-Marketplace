package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.config.TestSecurityConfig;
import com.campus.marketplace.common.entity.FeatureFlag;
import com.campus.marketplace.repository.FeatureFlagRepository;
import com.campus.marketplace.service.FeatureFlagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = FeatureFlagController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("FeatureFlagController MockMvc 测试")
class FeatureFlagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FeatureFlagRepository repository;

    @MockBean
    private FeatureFlagService featureFlagService;

    @Test
    @DisplayName("查询功能开关列表")
    void list_success() throws Exception {
        FeatureFlag flag = FeatureFlag.builder()
                .id(1L)
                .key("new-ui")
                .enabled(true)
                .updatedAt(Instant.now())
                .build();
        when(repository.findAll()).thenReturn(List.of(flag));

        mockMvc.perform(get("/api/feature-flags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].key").value("new-ui"));

        verify(repository).findAll();
    }

    @Test
    @DisplayName("新增功能开关")
    void upsert_create() throws Exception {
        FeatureFlag body = FeatureFlag.builder()
                .key("beta-feature")
                .enabled(true)
                .rulesJson("{\"segments\":[]}")
                .description("测试开关")
                .build();
        FeatureFlag saved = FeatureFlag.builder()
                .id(2L)
                .key("beta-feature")
                .enabled(true)
                .rulesJson("{\"segments\":[]}")
                .description("测试开关")
                .updatedAt(Instant.now())
                .build();
        when(repository.findByKey("beta-feature")).thenReturn(Optional.empty());
        when(repository.save(any(FeatureFlag.class))).thenReturn(saved);

        mockMvc.perform(post("/api/feature-flags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(2));

        ArgumentCaptor<FeatureFlag> captor = ArgumentCaptor.forClass(FeatureFlag.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getKey()).isEqualTo("beta-feature");
        verify(featureFlagService).refresh("beta-feature");
    }

    @Test
    @DisplayName("更新已有功能开关")
    void upsert_updateExisting() throws Exception {
        FeatureFlag existing = FeatureFlag.builder()
                .id(5L)
                .key("old-flag")
                .enabled(false)
                .rulesJson("{}")
                .description("旧描述")
                .updatedAt(Instant.EPOCH)
                .build();
        FeatureFlag body = FeatureFlag.builder()
                .key("old-flag")
                .enabled(true)
                .rulesJson("{\"rule\":1}")
                .description("更新描述")
                .build();
        FeatureFlag saved = FeatureFlag.builder()
                .id(5L)
                .key("old-flag")
                .enabled(true)
                .rulesJson("{\"rule\":1}")
                .description("更新描述")
                .updatedAt(Instant.now())
                .build();

        when(repository.findByKey("old-flag")).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(saved);

        mockMvc.perform(post("/api/feature-flags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(true));

        assertThat(existing.isEnabled()).isTrue();
        assertThat(existing.getDescription()).isEqualTo("更新描述");
        assertThat(existing.getRulesJson()).isEqualTo("{\"rule\":1}");
        verify(featureFlagService).refresh("old-flag");
    }

    @Test
    @DisplayName("刷新所有开关缓存")
    void refreshAll_success() throws Exception {
        mockMvc.perform(post("/api/feature-flags/refresh"))
                .andExpect(status().isOk());

        verify(featureFlagService).refreshAll();
    }

    @Test
    @DisplayName("删除功能开关并刷新单个缓存")
    void delete_success() throws Exception {
        FeatureFlag flag = FeatureFlag.builder()
                .id(9L)
                .key("to-delete")
                .enabled(false)
                .updatedAt(Instant.now())
                .build();
        when(repository.findByKey("to-delete")).thenReturn(Optional.of(flag));

        mockMvc.perform(delete("/api/feature-flags/{key}", "to-delete"))
                .andExpect(status().isOk());

        verify(repository).delete(flag);
        verify(featureFlagService).refresh("to-delete");
    }
}
