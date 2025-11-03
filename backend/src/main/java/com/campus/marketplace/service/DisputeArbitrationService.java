package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.ArbitrationDTO;
import com.campus.marketplace.common.dto.request.ArbitrateDisputeRequest;

import java.util.List;
import java.util.Optional;

/**
 * 纠纷仲裁服务接口
 *
 * 负责仲裁员分配、仲裁决定提交和执行
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
public interface DisputeArbitrationService {

    /**
     * 分配仲裁员给纠纷
     *
     * 将纠纷分配给仲裁员处理，并更新纠纷状态为"仲裁中"
     *
     * @param disputeId 纠纷ID
     * @param arbitratorId 仲裁员ID
     * @return 是否成功
     * @throws com.campus.marketplace.common.exception.BusinessException 纠纷不存在、已分配等
     */
    boolean assignArbitrator(Long disputeId, Long arbitratorId);

    /**
     * 提交仲裁决定
     *
     * 仲裁员提交对纠纷的最终裁决，包括退款金额和理由
     *
     * @param request 仲裁请求
     * @param arbitratorId 仲裁员ID
     * @return 仲裁记录ID
     * @throws com.campus.marketplace.common.exception.BusinessException 纠纷不存在、已有仲裁记录等
     */
    Long submitArbitration(ArbitrateDisputeRequest request, Long arbitratorId);

    /**
     * 查询纠纷的仲裁详情
     *
     * @param disputeId 纠纷ID
     * @return 仲裁详情（可能为空）
     */
    Optional<ArbitrationDTO> getArbitrationDetail(Long disputeId);

    /**
     * 查询仲裁员处理的案件列表
     *
     * @param arbitratorId 仲裁员ID
     * @return 案件列表
     */
    List<ArbitrationDTO> getArbitratorCases(Long arbitratorId);

    /**
     * 查询待执行的仲裁列表
     *
     * 返回所有需要退款但尚未执行的仲裁记录
     *
     * @return 待执行仲裁列表
     */
    List<ArbitrationDTO> getPendingExecutions();

    /**
     * 标记仲裁为已执行
     *
     * 标记退款已处理完成
     *
     * @param arbitrationId 仲裁ID
     * @param executionNote 执行说明
     * @return 是否成功
     * @throws com.campus.marketplace.common.exception.BusinessException 仲裁记录不存在
     */
    boolean markExecuted(Long arbitrationId, String executionNote);
}
