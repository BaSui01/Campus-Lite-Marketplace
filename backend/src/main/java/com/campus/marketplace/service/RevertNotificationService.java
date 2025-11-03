package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.RevertRequest;
import com.campus.marketplace.revert.dto.RevertExecutionResult;

/**
 * 撤销通知服务接口
 * 
 * 功能说明：
 * 1. 撤销申请通知
 * 2. 撤销执行结果通知
 * 3. 审批结果通知
 * 4. 撤销警告通知
 * 
 * @author BaSui
 * @date 2025-11-03
 */
public interface RevertNotificationService {
    
    /**
     * 发送撤销申请通知
     * 
     * @param revertRequest 撤销请求
     */
    void sendRevertRequestNotification(RevertRequest revertRequest);
    
    /**
     * 发送撤销执行结果通知
     * 
     * @param revertRequest 撤销请求
     * @param result 执行结果
     */
    void sendRevertExecutionNotification(RevertRequest revertRequest, RevertExecutionResult result);
    
    /**
     * 发送审批结果通知
     * 
     * @param revertRequest 撤销请求
     * @param approved 是否批准
     */
    void sendApprovalNotification(RevertRequest revertRequest, boolean approved);
    
    /**
     * 发送撤销警告通知（涉及资金、权限等）
     * 
     * @param revertRequest 撤销请求
     * @param warningMessage 警告信息
     */
    void sendRevertWarningNotification(RevertRequest revertRequest, String warningMessage);
}
