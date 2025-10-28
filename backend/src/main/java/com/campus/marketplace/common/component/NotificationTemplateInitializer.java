package com.campus.marketplace.common.component;

import com.campus.marketplace.common.entity.NotificationTemplate;
import com.campus.marketplace.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.init", name = "seed", havingValue = "true", matchIfMissing = true)
public class NotificationTemplateInitializer {

    private final NotificationTemplateRepository repo;

    @Bean
    public ApplicationRunner initNotificationTemplates() {
        return args -> {
            Map<String, String[]> presets = Map.of(
                    "ORDER_CREATED", new String[]{"tpl.order.created.title", "tpl.order.created.content", "IN_APP,EMAIL,WEB_PUSH"},
                    "ORDER_PAID", new String[]{"tpl.order.paid.title", "tpl.order.paid.content", "IN_APP,EMAIL,WEB_PUSH"},
                    "ORDER_CANCELLED", new String[]{"tpl.order.cancelled.title", "tpl.order.cancelled.content", "IN_APP,EMAIL,WEB_PUSH"},
                    "POST_REPLIED", new String[]{"tpl.post.replied.title", "tpl.post.replied.content", "IN_APP,EMAIL,WEB_PUSH"},
                    "POST_MENTIONED", new String[]{"tpl.post.mentioned.title", "tpl.post.mentioned.content", "IN_APP,EMAIL,WEB_PUSH"}
            );
            for (var e : presets.entrySet()) {
                String code = e.getKey();
                String[] v = e.getValue();
                repo.findByCode(code).orElseGet(() -> {
                    NotificationTemplate tpl = NotificationTemplate.builder()
                            .code(code)
                            .titleKey(v[0])
                            .contentKey(v[1])
                            .channels(v[2])
                            .build();
                    repo.save(tpl);
                    log.info("初始化通知模板: {}", code);
                    return tpl;
                });
            }
        };
    }
}
