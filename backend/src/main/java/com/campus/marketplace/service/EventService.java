package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.Event;
import org.springframework.data.domain.Page;

/**
 * 活动服务接口
 * 
 * @author BaSui 😎
 * @date 2025-11-11
 */
public interface EventService {

    /**
     * 查询活动列表（分页）
     */
    Page<Event> listEvents(int page, int size, String status, Long campusId);

    /**
     * 获取活动详情
     */
    Event getEventDetail(Long eventId);

    /**
     * 报名活动
     */
    void registerEvent(Long eventId);

    /**
     * 取消报名
     */
    void cancelRegistration(Long eventId);

    /**
     * 检查用户是否已报名
     */
    boolean isRegistered(Long eventId);

    /**
     * 获取用户报名的活动列表
     */
    java.util.List<Event> getMyRegisteredEvents();
}
