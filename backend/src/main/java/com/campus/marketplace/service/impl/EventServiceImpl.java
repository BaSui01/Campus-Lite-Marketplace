package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.Event;
import com.campus.marketplace.common.entity.EventRegistration;
import com.campus.marketplace.common.enums.EventStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.EventRegistrationRepository;
import com.campus.marketplace.repository.EventRepository;
import com.campus.marketplace.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 活动服务实现
 * 
 * @author BaSui 😎
 * @date 2025-11-11
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Event> listEvents(int page, int size, String status, Long campusId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());

        if (status != null && campusId != null) {
            return eventRepository.findByStatusAndCampusId(EventStatus.valueOf(status), campusId, pageable);
        } else if (status != null) {
            return eventRepository.findByStatus(EventStatus.valueOf(status), pageable);
        } else if (campusId != null) {
            return eventRepository.findByCampusId(campusId, pageable);
        } else {
            return eventRepository.findAll(pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Event getEventDetail(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "活动不存在"));

        // 增加浏览量
        event.setViewCount(event.getViewCount() + 1);
        eventRepository.save(event);

        return event;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerEvent(Long eventId) {
        Long userId = SecurityUtil.getCurrentUserId();

        // 检查活动是否存在
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "活动不存在"));

        // 检查是否已报名
        if (registrationRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "您已经报名过该活动");
        }

        // 检查活动状态
        if (event.getStatus() != EventStatus.UPCOMING) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "该活动不在报名期间");
        }

        // 检查报名人数是否已满
        if (event.getMaxParticipants() > 0 && event.getCurrentParticipants() >= event.getMaxParticipants()) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "活动报名人数已满");
        }

        // 创建报名记录
        EventRegistration registration = EventRegistration.builder()
                .eventId(eventId)
                .userId(userId)
                .build();
        registrationRepository.save(registration);

        // 更新活动报名人数
        event.setCurrentParticipants(event.getCurrentParticipants() + 1);
        eventRepository.save(event);

        log.info("用户报名活动: userId={}, eventId={}", userId, eventId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelRegistration(Long eventId) {
        Long userId = SecurityUtil.getCurrentUserId();

        // 查询报名记录
        EventRegistration registration = registrationRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "未找到报名记录"));

        // 删除报名记录
        registrationRepository.delete(registration);

        // 更新活动报名人数
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "活动不存在"));
        event.setCurrentParticipants(Math.max(0, event.getCurrentParticipants() - 1));
        eventRepository.save(event);

        log.info("用户取消报名: userId={}, eventId={}", userId, eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRegistered(Long eventId) {
        Long userId = SecurityUtil.getCurrentUserId();
        return registrationRepository.existsByEventIdAndUserId(eventId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getMyRegisteredEvents() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<EventRegistration> registrations = registrationRepository.findByUserId(userId);
        
        List<Long> eventIds = registrations.stream()
                .map(EventRegistration::getEventId)
                .toList();

        return eventRepository.findAllById(eventIds);
    }
}
