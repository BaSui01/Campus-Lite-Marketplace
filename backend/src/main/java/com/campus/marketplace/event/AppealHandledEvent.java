package com.campus.marketplace.event;

import com.campus.marketplace.common.entity.Appeal;
import lombok.Getter;

/**
 * 申诉处理完成事件
 * 
 * 当管理员处理完申诉（通过或驳回）时触发此事件
 * 用于通知申诉人最终处理结果
 * 
 * @author BaSui 😎
 * @date 2025-11-03
 */
@Getter
public class AppealHandledEvent extends AppealEvent {

    /**
     * 处理人ID
     */
    private final Long handlerId;

    /**
     * 处理人名称
     */
    private final String handlerName;

    /**
     * 构造申诉处理完成事件
     * 
     * @param source      事件源
     * @param appeal      申诉对象
     * @param handlerId   处理人ID
     * @param handlerName 处理人名称
     */
    public AppealHandledEvent(Object source, Appeal appeal, Long handlerId, String handlerName) {
        super(source, appeal);
        this.handlerId = handlerId;
        this.handlerName = handlerName;
    }

    /**
     * 判断申诉是否通过
     * 
     * @return 是否通过
     */
    public boolean isApproved() {
        Appeal appeal = getAppeal();
        return appeal != null && 
               com.campus.marketplace.common.enums.AppealStatus.APPROVED.equals(appeal.getStatus());
    }

    /**
     * 判断申诉是否驳回
     * 
     * @return 是否驳回
     */
    public boolean isRejected() {
        Appeal appeal = getAppeal();
        return appeal != null && 
               com.campus.marketplace.common.enums.AppealStatus.REJECTED.equals(appeal.getStatus());
    }

    @Override
    public String toString() {
        return String.format("AppealHandledEvent[appealId=%d, handlerId=%d, handlerName=%s, approved=%b, timestamp=%d]",
            getAppealId(), handlerId, handlerName, isApproved(), getTimestamp());
    }
}
