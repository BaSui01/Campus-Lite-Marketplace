package com.campus.marketplace.common.component;

import com.campus.marketplace.common.entity.NotificationTemplate;
import com.campus.marketplace.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationTemplateInitializer 测试")
class NotificationTemplateInitializerTest {

    @Mock
    private NotificationTemplateRepository repository;

    private NotificationTemplateInitializer initializer;
    private ApplicationRunner runner;

    @BeforeEach
    void setUp() {
        initializer = new NotificationTemplateInitializer(repository);
        runner = initializer.initNotificationTemplates();
    }

    @Test
    @DisplayName("缺失模板时会自动保存预设配置")
    void shouldSeedTemplatesWhenMissing() throws Exception {
        when(repository.findByCode(any())).thenReturn(Optional.empty());

        runner.run(mock(ApplicationArguments.class));

        verify(repository, atLeast(5)).save(any(NotificationTemplate.class));
    }

    @Test
    @DisplayName("已存在模板时不会重复写入")
    void shouldSkipExistingTemplates() throws Exception {
        when(repository.findByCode(any())).thenReturn(Optional.of(NotificationTemplate.builder().code("EXIST").build()));

        runner.run(mock(ApplicationArguments.class));

        verify(repository, never()).save(any(NotificationTemplate.class));
    }
}
