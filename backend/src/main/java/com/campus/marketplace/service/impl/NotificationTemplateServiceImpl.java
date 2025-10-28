package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.NotificationTemplate;
import com.campus.marketplace.common.enums.NotificationChannel;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.NotificationTemplateRepository;
import com.campus.marketplace.service.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    private final NotificationTemplateRepository repository;
    private final MessageSource messageSource;

    @Override
    public Rendered render(String code, Locale locale, Map<String, Object> params) {
        NotificationTemplate tpl = repository.findByCode(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "模板不存在"));

        String titlePattern = messageSource.getMessage(tpl.getTitleKey(), null, locale);
        String contentPattern = messageSource.getMessage(tpl.getContentKey(), null, locale);

        String title = renderNamed(titlePattern, params);
        String content = renderNamed(contentPattern, params);

        Set<NotificationChannel> channels = Arrays.stream(tpl.getChannels().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(NotificationChannel::valueOf)
                .collect(Collectors.toSet());

        return new Rendered(title, content, channels);
    }

    private String renderNamed(String pattern, Map<String, Object> params) {
        if (pattern == null) return null;
        if (params == null || params.isEmpty()) return pattern;
        String rendered = pattern;
        for (Map.Entry<String, Object> e : params.entrySet()) {
            String key = e.getKey();
            String val = String.valueOf(e.getValue());
            rendered = rendered.replace("{" + key + "}", val);
        }
        return rendered;
    }
}
