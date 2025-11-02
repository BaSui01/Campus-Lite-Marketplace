package com.campus.marketplace.event;

import com.campus.marketplace.common.entity.Appeal;
import com.campus.marketplace.common.enums.AppealStatus;
import lombok.Getter;

/**
 * 申诉状态变更事件
 * 
 * 当申诉状态发生变化时触发此事件
 * 用于通知申诉人申诉处理进度
 * 
 * @author BaSui 😎
 * @date 2025-11-03
 */
@Getter
public class AppealStatusChangedEvent extends AppealEvent {

    /**
     * 旧状态
     */
    private final AppealStatus oldStatus;

    /**
     * 新状态
     */
    private final AppealStatus newStatus;

    /**
     * 构造申诉状态变更事件
     * 
     * @param source    事件源
     * @param appeal    申诉对象
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     */
    public AppealStatusChangedEvent(Object source, Appeal appeal, 
                                   AppealStatus oldStatus, AppealStatus newStatus) {
        super(source, appeal);
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    /**
     * 判断是否是特定的状态变更
     * 
     * @param from 源状态
     * @param to   目标状态
     * @return 是否匹配
     */
    public boolean isStatusChange(AppealStatus from, AppealStatus to) {
        return this.oldStatus == from && this.newStatus == to;
    }

    @Override
    public String toString() {
        return String.format("AppealStatusChangedEvent[appealId=%d, oldStatus=%s, newStatus=%s, timestamp=%d]",
            getAppealId(), oldStatus, newStatus, getTimestamp());
    }
}
