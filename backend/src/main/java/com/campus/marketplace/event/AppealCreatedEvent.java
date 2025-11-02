package com.campus.marketplace.event;

import com.campus.marketplace.common.entity.Appeal;

/**
 * 申诉创建事件
 * 
 * 当用户提交新申诉时触发此事件
 * 用于通知管理员有新的申诉待处理
 * 
 * @author BaSui 😎
 * @date 2025-11-03
 */
public class AppealCreatedEvent extends AppealEvent {

    /**
     * 构造申诉创建事件
     * 
     * @param source 事件源
     * @param appeal 新创建的申诉
     */
    public AppealCreatedEvent(Object source, Appeal appeal) {
        super(source, appeal);
    }

    @Override
    public String toString() {
        return String.format("AppealCreatedEvent[appealId=%d, userId=%d, timestamp=%d]",
            getAppealId(), getUserId(), getTimestamp());
    }
}
