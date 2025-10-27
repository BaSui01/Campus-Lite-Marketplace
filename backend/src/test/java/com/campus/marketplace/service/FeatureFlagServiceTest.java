package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.FeatureFlag;
import com.campus.marketplace.repository.FeatureFlagRepository;
import com.campus.marketplace.service.impl.FeatureFlagServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("FeatureFlag 评估与缓存测试")
class FeatureFlagServiceTest {

    private FeatureFlagRepository repository;
    private FeatureFlagServiceImpl service;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(FeatureFlagRepository.class);
        service = new FeatureFlagServiceImpl(repository);
        ReflectionTestUtils.setField(service, "activeEnv", "dev");
    }

    @Test
    void disabled_by_default_when_absent() {
        when(repository.findByKey("exp.new-ui")).thenReturn(Optional.empty());
        assertFalse(service.isEnabled("exp.new-ui", 1L, 1L, "dev"));
    }

    @Test
    void enabled_with_basic_rule() {
        FeatureFlag flag = FeatureFlag.builder()
                .key("exp.new-ui")
                .enabled(true)
                .rulesJson("{\"allowEnvs\":[\"dev\"]}")
                .updatedAt(Instant.now())
                .build();
        when(repository.findByKey("exp.new-ui")).thenReturn(Optional.of(flag));
        assertTrue(service.isEnabled("exp.new-ui", null, null, "dev"));
        assertFalse(service.isEnabled("exp.new-ui", null, null, "prod"));
    }

    @Test
    void cache_refresh_works() {
        FeatureFlag flag = FeatureFlag.builder()
                .key("exp.cache-test")
                .enabled(false)
                .updatedAt(Instant.now())
                .build();
        when(repository.findByKey("exp.cache-test")).thenReturn(Optional.of(flag));
        assertFalse(service.isEnabled("exp.cache-test", null, null, null));

        // enable and refresh
        flag.setEnabled(true);
        service.refresh("exp.cache-test");
        assertTrue(service.isEnabled("exp.cache-test", null, null, null));
    }
}
