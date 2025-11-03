package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateRevertRequestDto;
import com.campus.marketplace.revert.dto.RevertExecutionResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 撤销服务接口
 * 
 * @author BaSui
 * @date 2025-11-03
 */
public interface RevertService {
    
    /**
     * 申请撤销操作
     * 
     * @param auditLogId 审计日志ID
     * @param request 撤销申请请求
     * @param applicantId 申请人ID
     * @return 撤销结果
     */
    RevertExecutionResult requestRevert(Long auditLogId, CreateRevertRequestDto request, Long applicantId);
    
    /**
     * 执行撤销操作（需审批通过）
     * 
     * @param revertRequestId 撤销请求ID
     * @param approverId 审批人ID
     * @return 执行结果
     */
    RevertExecutionResult executeRevert(Long revertRequestId, Long approverId);
    
    /**
     * 查询用户的撤销请求历史
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 撤销请求分页结果
     */
    Page<?> getUserRevertRequests(Long userId, Pageable pageable);
}
