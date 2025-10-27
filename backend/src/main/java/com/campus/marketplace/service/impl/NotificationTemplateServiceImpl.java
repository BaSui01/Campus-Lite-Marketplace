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

import java.text.MessageFormat;
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

        Object[] values = toOrderedValues(params);
        String title = MessageFormat.format(titlePattern, values);
        String content = MessageFormat.format(contentPattern, values);

        Set<NotificationChannel> channels = Arrays.stream(tpl.getChannels().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(NotificationChannel::valueOf)
                .collect(Collectors.toSet());

        return new Rendered(title, content, channels);
    }

    private Object[] toOrderedValues(Map<String, Object> params) {
        if (params == null || params.isEmpty()) return new Object[0];
        // MessageFormat按索引取值；这里约定按key排序构造
        return params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .toArray();
    }
}
