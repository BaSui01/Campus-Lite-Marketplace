package com.campus.marketplace.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("I18nConfig 测试")
class I18nConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(I18nConfig.class);

    @Test
    @DisplayName("MessageSource 与 LocaleResolver Bean 生效")
    void shouldExposeBeans() {
        contextRunner.run(context -> {
            MessageSource messageSource = context.getBean(MessageSource.class);
            LocaleResolver localeResolver = context.getBean(LocaleResolver.class);
            MockHttpServletRequest request = new MockHttpServletRequest();

            assertThat(messageSource.getMessage("non.existing.code", null, Locale.CHINA))
                    .isEqualTo("non.existing.code");
            assertThat(localeResolver.resolveLocale(request)).isEqualTo(Locale.SIMPLIFIED_CHINESE);
        });
    }
}
