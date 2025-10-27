package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.NotificationPreference;
import com.campus.marketplace.common.entity.NotificationUnsubscribe;
import com.campus.marketplace.common.enums.NotificationChannel;
import com.campus.marketplace.repository.NotificationPreferenceRepository;
import com.campus.marketplace.repository.NotificationUnsubscribeRepository;
import com.campus.marketplace.service.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationUnsubscribeRepository unsubscribeRepository;

    @Override
    @Transactional
    public void setChannelEnabled(Long userId, NotificationChannel channel, boolean enabled) {
        NotificationPreference pref = preferenceRepository
                .findByUserIdAndChannel(userId, channel)
                .orElseGet(() -> NotificationPreference.builder()
                        .userId(userId)
                        .channel(channel)
                        .enabled(true)
                        .build());
        pref.setEnabled(enabled);
        preferenceRepository.save(pref);
    }

    @Override
    @Transactional
    public void setQuietHours(Long userId, NotificationChannel channel, LocalTime start, LocalTime end) {
        NotificationPreference pref = preferenceRepository
                .findByUserIdAndChannel(userId, channel)
                .orElseGet(() -> NotificationPreference.builder()
                        .userId(userId)
                        .channel(channel)
                        .enabled(true)
                        .build());
        pref.setQuietStart(start);
        pref.setQuietEnd(end);
        preferenceRepository.save(pref);
    }

    @Override
    public boolean isChannelEnabled(Long userId, NotificationChannel channel) {
        return preferenceRepository.findByUserIdAndChannel(userId, channel)
                .map(NotificationPreference::getEnabled)
                .orElse(true);
    }

    @Override
    public boolean isInQuietHours(Long userId, NotificationChannel channel, LocalTime now) {
        return preferenceRepository.findByUserIdAndChannel(userId, channel)
                .map(p -> inQuiet(now, p.getQuietStart(), p.getQuietEnd()))
                .orElse(false);
    }

    private boolean inQuiet(LocalTime now, LocalTime start, LocalTime end) {
        if (now == null || start == null || end == null) return false;
        if (start.equals(end)) return false;
        if (start.isBefore(end)) {
            return !now.isBefore(start) && now.isBefore(end);
        }
        // 跨午夜
        return now.isAfter(start) || now.isBefore(end);
    }

    @Override
    @Transactional
    public void unsubscribe(Long userId, String templateCode, NotificationChannel channel) {
        if (unsubscribeRepository.existsByUserIdAndTemplateCodeAndChannel(userId, templateCode, channel)) return;
        unsubscribeRepository.save(NotificationUnsubscribe.builder()
                .userId(userId)
                .templateCode(templateCode)
                .channel(channel)
                .build());
    }

    @Override
    @Transactional
    public void resubscribe(Long userId, String templateCode, NotificationChannel channel) {
        unsubscribeRepository.deleteByUserIdAndTemplateCodeAndChannel(userId, templateCode, channel);
    }

    @Override
    public boolean isUnsubscribed(Long userId, String templateCode, NotificationChannel channel) {
        return unsubscribeRepository.existsByUserIdAndTemplateCodeAndChannel(userId, templateCode, channel);
    }
}
