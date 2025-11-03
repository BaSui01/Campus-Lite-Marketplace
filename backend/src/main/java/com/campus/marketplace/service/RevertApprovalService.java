package com.campus.marketplace.service;

/**
 * 撤销审批服务接口
 * 
 * @author BaSui
 * @date 2025-11-03
 */
public interface RevertApprovalService {
    
    /**
     * 审批撤销请求
     * 
     * @param revertRequestId 撤销请求ID
     * @param approverId 审批人ID
     * @param approved 是否批准
     * @param comment 审批意见
     */
    void approveRevertRequest(Long revertRequestId, Long approverId, boolean approved, String comment);
    
    /**
     * 检查撤销请求是否需要审批
     * 
     * @param revertRequestId 撤销请求ID
     * @return 是否需要审批
     */
    boolean requiresApproval(Long revertRequestId);
    
    /**
     * 获取待审批的撤销请求数量
     * 
     * @param approverId 审批人ID
     * @return 待审批数量
     */
    long getPendingApprovalCount(Long approverId);
}
