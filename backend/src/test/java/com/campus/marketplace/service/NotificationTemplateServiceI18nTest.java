package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.NotificationTemplate;
import com.campus.marketplace.common.enums.NotificationChannel;
import com.campus.marketplace.repository.NotificationTemplateRepository;
import com.campus.marketplace.service.impl.NotificationTemplateServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("通知模板 i18n 渲染测试")
class NotificationTemplateServiceI18nTest {

    @Mock
    NotificationTemplateRepository repo;

    @InjectMocks
    NotificationTemplateServiceImpl service;

    @org.junit.jupiter.api.BeforeEach
    void initMessageSource() throws Exception {
        var ms = new ReloadableResourceBundleMessageSource();
        ms.setBasename("classpath:messages");
        ms.setDefaultEncoding("UTF-8");
        var f = NotificationTemplateServiceImpl.class.getDeclaredField("messageSource");
        f.setAccessible(true);
        f.set(service, ms);
    }

    @Test
    @DisplayName("应按 locale 渲染不同语言")
    void render_multilang() {
        NotificationTemplate tpl = NotificationTemplate.builder()
                .code("ORDER_CREATED")
                .titleKey("tpl.order.created.title")
                .contentKey("tpl.order.created.content")
                .channels("IN_APP,EMAIL")
                .build();
        when(repo.findByCode(anyString())).thenReturn(Optional.of(tpl));

        var zh = service.render("ORDER_CREATED", Locale.SIMPLIFIED_CHINESE,
                java.util.Map.of("orderNo", "A001", "goodsTitle", "iPhone"));
        assertThat(zh.title()).contains("新订单");
        assertThat(zh.content()).contains("订单号 A001");
        assertThat(zh.channels()).contains(NotificationChannel.IN_APP);

        var en = service.render("ORDER_CREATED", Locale.US,
                java.util.Map.of("orderNo", "A001", "goodsTitle", "iPhone"));
        assertThat(en.title()).contains("New order");
        assertThat(en.content()).contains("Order No. A001");
    }
}
