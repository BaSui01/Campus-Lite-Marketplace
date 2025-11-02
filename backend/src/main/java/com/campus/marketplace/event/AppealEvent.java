package com.campus.marketplace.event;

import com.campus.marketplace.common.entity.Appeal;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 申诉事件基类
 * 
 * 所有申诉相关事件的基类，包含基本的申诉信息
 * 
 * @author BaSui 😎
 * @date 2025-11-03
 */
@Getter
public abstract class AppealEvent extends ApplicationEvent {

    /**
     * 申诉对象
     */
    private final Appeal appeal;

    /**
     * 构造申诉事件
     * 
     * @param source 事件源
     * @param appeal 申诉对象
     */
    public AppealEvent(Object source, Appeal appeal) {
        super(source);
        this.appeal = appeal;
    }

    /**
     * 获取申诉ID
     * 
     * @return 申诉ID
     */
    public Long getAppealId() {
        return appeal != null ? appeal.getId() : null;
    }

    /**
     * 获取申诉用户ID
     * 
     * @return 用户ID
     */
    public Long getUserId() {
        return appeal != null ? appeal.getUserId() : null;
    }
}
